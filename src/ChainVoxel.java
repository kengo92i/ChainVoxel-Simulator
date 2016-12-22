
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * ChainVoxelを実装したクラス．<br>
 * <br>
 * negativeVoxelのvoxelチェインへの追加処理を簡略化するために，<br>
 * negativeVoxelをposIDに対応するvoxelチェインとは独立して管理する実装になっています．<br>
 * voxelチェインはこのクラスではvoxelのリストとして実装されています．
 * <br>
 * K. Imae and N. Hayashibara, 
 * “ChainVoxel: A Data Structure for Scalable Distributed Collaborative Editing for 3D Models” 
 * The 14th IEEE International Conference on Dependable, Autonomic and Secure Computing, 8-12 Aug. 2016.
 *
 * @author kengo92i
 */
public class ChainVoxel extends CRDT<TreeMap<String, ArrayList<Voxel>>, Operation> {
    /**
     * posIDに対応するvoxelのリストを管理するTreeMap
     */
    private TreeMap<String, ArrayList<Voxel>> atoms;

    /**
     * posIDに対応する負のvoxelを管理するTreeMap
     */
    private TreeMap<String, Voxel> negativeVoxels;


    /**
     * 構造管理のためのStrutureTable
     */
    private StructureTable stt;

    /**
     * ChainVoxelのコンストラクタ
     */
    public ChainVoxel() {
        this.atoms = new TreeMap<String, ArrayList<Voxel>>();
        this.negativeVoxels = new TreeMap<String, Voxel>();
        this.stt = new StructureTable();
    }

    /**
     * 操作オブジェクトに対応する操作を実行するメソッド
     * ChainVoxelに対する操作はapplyメソッドを用いて実行することを推奨しています．
     * @param op 操作オブジェクト
     */
    public void apply(Operation op) {
        String posID = op.getPosID();
        switch (op.getOpType()) {
            case Operation.INSERT:
                if (this.stt.isGrouped(posID)) break;
                this.insert(op);
                break;
            case Operation.DELETE:
                if (this.stt.isGrouped(posID)) break;
                this.delete(op);
                break;
            case Operation.CREATE:
                this.create(op);
                break;
            case Operation.JOIN:
                this.join(op);
                break;
            case Operation.LEAVE:
                this.leave(op);
                break;
            default:
                assert false;
        }
        return;
    }

    /**
     * ChainVoxel内にvoxelを挿入するメソッド
     * @param op 操作オブジェクト
     * @see Operation
     */
    public void insert(Operation op) {
        int id = op.getId();
        String posID = op.getPosID();
        long timestamp = op.getTimestamp();
        Voxel insertVoxel = new Voxel(id, timestamp);

        ArrayList<Voxel> voxelList = this.getVoxelList(posID);

        // step1: 負のvoxelの影響があるか調べる
        // 負のvoxelより新しいtsの場合は以降の処理に進む，そうではない場合は，ここで終了
        Voxel negativeVoxel = negativeVoxels.get(posID);
        if (negativeVoxel != null && negativeVoxel.getTimestamp() >= timestamp) {
            return; // 負のvoxelより前に挿入する操作は無駄な操作であるため
        }

        // step2: insertVoxelを挿入する
        voxelList.add(insertVoxel);
        Collections.sort(voxelList);
        return;
    }

    /**
     * ChainVoxel内の指定したvoxelを削除するメソッド
     * @param op 操作オブジェクト
     * @see Operation
     */
    public void delete(Operation op) {
        int id = op.getId();
        String posID = op.getPosID();
        long timestamp = op.getTimestamp();

        // step1: 負のvoxelをnegativeVoxelsに追加・更新
        Voxel negativeVoxel = negativeVoxels.get(posID);
        if (negativeVoxel == null || negativeVoxel.getTimestamp() < timestamp) {
            negativeVoxels.put(posID, new Voxel(timestamp));
        }

        ArrayList<Voxel> voxelList = this.getVoxelList(posID);

        // step2: 負のvoxelより古いvoxelを削除する
        negativeVoxel = negativeVoxels.get(posID);
        for (int i = voxelList.size() - 1; i >= 0; --i) { // 先頭から削除するとイテレータがおかしくなる
            if (negativeVoxel.getTimestamp() >= voxelList.get(i).getTimestamp()) {
                voxelList.remove(i); 
            }
        }

        Collections.sort(voxelList);
        return;
    }

    /**
     * 指定したグループを作成するメソッド
     * @param op 操作オブジェクト
     * @see Operation
     */
    public void create(Operation op) {
        String gid = (String) op.getParam("gid"); 
        this.stt.create(gid);
    }

    /**
     * 指定したグループにvoxelを参加させるメソッド
     * @param op 操作オブジェクト
     * @see Operation
     */
    public void join(Operation op) {
        long ts = op.getTimestamp(); 
        String posID = (String) op.getParam("posID"); 
        String gid = (String) op.getParam("gid"); 

        this.stt.join(ts, posID, gid);
    }

    /**
     * 指定したグループからvoxelを脱退させるメソッド
     * @param op 操作オブジェクト
     * @see Operation
     */
    public void leave(Operation op) {
        int sid = (int) op.getParam("sid"); 
        long ts = op.getTimestamp(); 
        String posID = (String) op.getParam("posID"); 
        String gid = (String) op.getParam("gid"); 

        this.stt.leave(sid, ts, posID, gid);
        this.insert(op);
    }

    /**
     * 指定したposIDに対応するprimaryVoxelを返すメソッド
     * @param posID voxelの識別子
     * @return posIDに対応するvoxel，posIDに対応するものがない場合はnullを返す．
     * @see Voxel
     */
    public Voxel getVoxel(String posID) {
        ArrayList<Voxel> voxelList = this.atoms.get(posID);
        if (voxelList == null) {
            return null;
        }
        return voxelList.get(0); // 先頭のvoxelがprimaryVoxel
    }

    /**
     * 指定したposIDに対応するvoxelのリストを返すメソッド
     * @param posID voxelの識別子
     * @return posIDに対応するvoxelのリスト
     * @see Voxel
     */
    public ArrayList<Voxel> getVoxelList(String posID) {
        ArrayList<Voxel> voxelList = this.atoms.get(posID);
        if (voxelList == null) {
            voxelList = new ArrayList<Voxel>();
            this.atoms.put(posID, voxelList);
        }
        return voxelList;
    }

    /**
     * ChainVoxelの総容量を返すメソッド
     * @return ChainVoxelの総容量
     */
    public int size() {
        int totalSize = 0;
        for (Map.Entry<String, ArrayList<Voxel>> e : this.atoms.entrySet()) {
           totalSize += e.getValue().size(); 
        }
        return totalSize;
    }

    /**
     * 指定されたposIDのvoxel数を返すメソッド
     * @param posID voxelの識別子
     * @return posIDに対応するvoxel数
     */
    public int size(String posID) {
        return this.atoms.get(posID).size();
    }

    /**
     * ChainVoxelをCollada形式でファイル出力するメソッド
     * @param filename 出力するファイル名
     */
    public void exportCollada(String filename) {
        File fileObject = new File("xml/collada.dae");
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = docBuilder.parse(fileObject); 
            Element rootElement = document.getDocumentElement();
            Element sceneElement = (Element) rootElement.getElementsByTagName("visual_scene").item(0);

            for (Map.Entry<String, ArrayList<Voxel>> e : this.atoms.entrySet()) {
                ArrayList<Voxel> voxelList = e.getValue(); 
                if (voxelList.size() == 0) continue;
                String posID = e.getKey();
                Element nodeElement = this.createNodeElement(document, posID);
                sceneElement.appendChild(nodeElement);
            }

            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer transformer = tfactory.newTransformer(); 

            File outputDirectory = new File("output");
            if (!outputDirectory.exists()) outputDirectory.mkdir();
            File outfile = new File("output/" + filename + ".dae");
            transformer.transform(new DOMSource(document), new StreamResult(outfile)); 

        } catch (IOException ie) {
            ie.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return; 
    }

    /**
     * 立方体を表すElement型のオブジェクトを作成する
     * @param document 対象とするXML文章
     * @param posID voxelの識別子
     * @return 立方体を表すElement型のオブジェクト
     * @see ChainVoxel#exportCollada
     */
    private Element createNodeElement(Document document, String posID) {
        Element nodeElement = document.createElement("node");
        nodeElement.setAttribute("id", posID);
        nodeElement.setAttribute("name", posID);
        nodeElement.setAttribute("type", "NODE");

        Element matrixElement = document.createElement("matrix");
        matrixElement.setAttribute("sid", "transform");
        nodeElement.appendChild(matrixElement);

        String[] position = posID.split(":");
        String voxelInfo = "0.5 0 0 " + position[0] + " 0 0.5 0 " + position[1] + " 0 0 0.5 " + position[2] + " 0 0 0 1";
        Text voxelText = document.createTextNode(voxelInfo);
        matrixElement.appendChild(voxelText);

        Element instanceGeometryElement = document.createElement("instance_geometry");
        instanceGeometryElement.setAttribute("url", "#Cube-mesh");
        nodeElement.appendChild(instanceGeometryElement);
        return nodeElement;
    }

    /**
     * ChainVoxelの状態を表示する
     */
    public void show() {
        for (Map.Entry<String, ArrayList<Voxel>> e : this.atoms.entrySet()) {
            if (e.getValue().size() == 0) continue;
            System.out.print("|" + e.getKey()+ "|");
            ArrayList<Voxel> voxelList = e.getValue();
            int n = voxelList.size();
            for (Voxel voxel : voxelList) {
                String id = Integer.toString(voxel.getId());
                String timestamp = Long.toString(voxel.getTimestamp());
                System.out.print(" -> (" + id + "," + timestamp + ")"); 
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * ChainVoxelのサンプル実行用
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        ChainVoxel cv = new ChainVoxel(); 
        cv.apply(new Operation(5, Operation.INSERT, "1:1:1"));
        cv.apply(new Operation(2, Operation.INSERT, "1:1:1"));
        cv.apply(new Operation(3, Operation.INSERT, "1:1:1"));
        cv.apply(new Operation(3, Operation.DELETE, "1:1:1"));
        cv.apply(new Operation(4, Operation.INSERT, "0:1:1"));
        cv.apply(new Operation(4, Operation.INSERT, "0:0:0"));
        cv.apply(new Operation(4, Operation.INSERT, "1:1:1"));
        cv.show();
        cv.exportCollada("sample");
    }
}
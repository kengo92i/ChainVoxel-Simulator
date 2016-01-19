
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
 * ChainVoxelを実装したクラス.
 * @author kengo92i
 */
public class ChainVoxel extends CRDT<TreeMap<String, ArrayList<Atom>>, Operation> {
    /**
     * posIDに対応するatomのリストを管理するTreeMap
     */
    private TreeMap<String, ArrayList<Atom>> atoms;

    /**
     * posIDに対応する負のatomを管理するTreeMap
     */
    private TreeMap<String, Atom> negativeAtoms;

    /**
     * ChainVoxelのコンストラクタ
     */
    public ChainVoxel() {
        this.atoms = new TreeMap<String, ArrayList<Atom>>();
        this.negativeAtoms = new TreeMap<String, Atom>();
    }

    /**
     * 操作オブジェクトに対応する操作を実行するメソッド
     * @param op 操作オブジェクト
     */
    public void apply(Operation op) {
        switch (op.getOpType()) {
            case Operation.INSERT:
                this.insert(op);      
                break;
            case Operation.DELETE:
                this.delete(op);
                break;
            default:
                assert false;
        }
        return;
    }

    /**
     * ChainVoxel内にatomを挿入するメソッド
     * @param op 操作オブジェクト
     * @see Operation
     */
    public void insert(Operation op) {
        int id = op.getId();
        String posID = op.getPosID();
        long timestamp = op.getTimestamp();
        Atom insertAtom = new Atom(id, timestamp);

        ArrayList<Atom> atomList = this.getAtomList(posID);

        // step1: 負のatomの影響があるか調べる
        // 負のatomより新しいtsの場合は以降の処理に進む，そうではない場合は，ここで終了
        Atom negativeAtom = negativeAtoms.get(posID);
        if (negativeAtom != null && negativeAtom.getTimestamp() >= timestamp) {
            return;
        }

        // step2: Node(atom)を挿入する
        atomList.add(insertAtom);
        Collections.sort(atomList, new ChainVoxelComparator());
        return;
    }

    /**
     * ChainVoxel内の指定したatomを削除するメソッド
     * @param op 操作オブジェクト
     * @see Operation
     */
    public void delete(Operation op) {
        int id = op.getId();
        String posID = op.getPosID();
        long timestamp = op.getTimestamp();

        // step1: 負のatomをnegativeAtomsに追加・更新
        Atom negativeAtom = negativeAtoms.get(posID);
        if (negativeAtom == null || negativeAtom.getTimestamp() < timestamp) {
            negativeAtoms.put(posID, new Atom(timestamp));
        }

        ArrayList<Atom> atomList = this.getAtomList(posID);

        // step2: 負のatomより古いatomを削除する
        negativeAtom = negativeAtoms.get(posID);
        for (int i = atomList.size() - 1; i >= 0; --i) { // 先頭から削除するとイテレータがおかしくなる
            if (negativeAtom.getTimestamp() >= atomList.get(i).getTimestamp()) {
                atomList.remove(i); 
            }
        }

        Collections.sort(atomList, new ChainVoxelComparator());
        return;
    }

    /**
     * 指定したposIDに対応するatomのリストを返すメソッド
     * @param posID atomの識別子
     * @return posIDに対応するatomのリスト
     */
    public ArrayList<Atom> getAtomList(String posID) {
        ArrayList<Atom> atomList = this.atoms.get(posID);
        if (atomList == null) {
            atomList = new ArrayList<Atom>();
            this.atoms.put(posID, atomList);
        }
        return atomList;
    }

    /**
     * ChainVoxelの総容量を返すメソッド
     * @return ChainVoxelの総容量
     */
    public int size() {
        int totalSize = 0;
        for (Map.Entry<String, ArrayList<Atom>> e : this.atoms.entrySet()) {
           totalSize += e.getValue().size(); 
        }
        return totalSize;
    }

    /**
     * 指定されたposIDのatom数を返すメソッド
     * @param posID atomの識別子
     * @return posIDに対応するatom数
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

            for (Map.Entry<String, ArrayList<Atom>> e : this.atoms.entrySet()) {
                ArrayList<Atom> atomList = e.getValue(); 
                if (atomList.size() == 0) continue;
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
     * 立方体を表すElement型のオブジェクトを返す
     * @param document 対象とするXML文章
     * @param posID atomの識別子
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
        for (Map.Entry<String, ArrayList<Atom>> e : this.atoms.entrySet()) {
            if (e.getValue().size() == 0) continue;
            System.out.print("|" + e.getKey()+ "|");
            ArrayList<Atom> atomList = e.getValue();
            int n = atomList.size();
            for (Atom atom : atomList) {
                String id = Integer.toString(atom.getId());
                String timestamp = Long.toString(atom.getTimestamp());
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
        cv.apply(new Operation(1, Operation.INSERT, "1:1:1"));
        cv.apply(new Operation(2, Operation.INSERT, "1:1:1"));
        cv.apply(new Operation(3, Operation.INSERT, "1:1:1"));
        cv.apply(new Operation(3, Operation.DELETE, "1:1:1"));
        cv.apply(new Operation(4, Operation.INSERT, "1:1:1"));
        cv.apply(new Operation(4, Operation.INSERT, "0:1:1"));
        cv.apply(new Operation(4, Operation.INSERT, "0:0:0"));
        cv.show();
        cv.exportCollada("sample");
    }
}

/**
 * ChainVoxelのための比較器
 */
class ChainVoxelComparator implements Comparator<Atom> {
    /**
     * Atomをタイムスタンプの昇順にソートする．同じタイムスタンプの場合は識別子の昇順で順位付け． 
     * @param l Atom型
     * @param r Atom型
     */
    @Override
    public int compare(Atom l, Atom r) { 
        if (l.getTimestamp() < r.getTimestamp()) {
            return -1;
        }
        else if (l.getTimestamp() > r.getTimestamp()) {
            return 1;
        }
        else {
            if (l.getId() < r.getId()) {
                return -1;
            }
            else if (l.getId() > r.getId()) {
                return 1; 
            }
            else {
                return 0;
            }
        }
    }
}
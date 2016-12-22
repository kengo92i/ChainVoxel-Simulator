import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

/**
 * 構造層のためのStructureTableを実装したクラス．<br>
 * <br>
 * ChainVoxelのための構造層を実現したクラス．ChainVoxelクラスと組み合わせて使う．<br>
 * StructureTableへの操作はcreate，join，leaveの3種類をサポートしている．
 * @author kengo92i
 */
public class StructureTable {
    /**
     * グループ(gid)に属するグループメンバー(posID)を管理するためのテーブル
     */
    TreeMap<String, TreeSet<String>> groupMembersTable; 

    /**
     * voxel(posID)が所属しているグループ(gid, ts)を管理するテーブル
     */
    TreeMap<String, TreeSet<GroupEntry<String, Long>>> groupEntriesTable;

    /**
     * Structure Table のコンストラクタ
     */
    public StructureTable() {
        this.groupMembersTable = new TreeMap<String, TreeSet<String>>(); 
        this.groupEntriesTable = new TreeMap<String, TreeSet<GroupEntry<String, Long>>>();
    }

    /**
     * Structure Table にグループ(gid)を作成する．
     * 既に作成されたグループのgidの場合は実行されない．
     * @param gid グループ識別子
     * @see Operation
     */
    public void create(String gid) {
        if (groupMembersTable.containsKey(gid)) { // 既にグループ(gid)が存在する
            return;
        }
        groupMembersTable.put(gid, new TreeSet<String>());
    }

    /**
     * グループ(gid)にvoxel(posID)を参加させる
     * @param ts タイムスタンプ
     * @param posID voxel識別子
     * @param gid グループ識別子
     * @see Operation
     */
    public void join(long ts, String posID, String gid) {
        GroupEntry<String, Long> aGroupEntry = new GroupEntry<String, Long>(gid, ts);
        if (!this.groupMembersTable.containsKey(gid) || Math.abs(this.getTimestamp(posID, gid)) >= ts) {
            return;
        }
        
        // groupEntriesTable に GroupEntry(gid, ts) を追加
        if (!this.groupEntriesTable.containsKey(posID)) {
            this.groupEntriesTable.put(posID, new TreeSet<GroupEntry<String, Long>>());
        }
        this.groupEntriesTable.get(posID).add(aGroupEntry); 

        // groupMembersTable に posID を追加
        this.groupMembersTable.get(gid).add(posID);

        // タイムスタンプの値を最新の値に更新する
        long maxTs = Math.max(ts, this.getTimestamp(posID, gid));
        this.setTimestamp(maxTs, posID, gid);
    }

    /**
     * グループ(gid)からvoxel(posID)を脱退させる
     * @param sid site識別子
     * @param ts タイムスタンプ
     * @param posID voxel識別子
     * @param gid グループ識別子
     * @see Operation
     */
    public void leave(int sid, long ts, String posID, String gid) {
        GroupEntry<String, Long> aGroupEntry = new GroupEntry<String, Long>(gid, ts);
        if (!this.groupEntriesTable.get(posID).contains(aGroupEntry) || Math.abs(this.getTimestamp(posID, gid)) >= ts) {
            return;
        } 

        // groupMembersTable から posID を削除 (グループからの脱退)
        this.groupMembersTable.get(gid).remove(posID);

        // タイムスタンプの更新 + tombstone化
        long minTs = Math.min(-1L * ts, this.getTimestamp(posID, gid));
        this.setTimestamp(minTs, posID, gid);
    }

    /**
     * グループ(gid)のグループメンバーの集合を取得する
     * @param gid グループ識別子
     * @return グループメンバー(posIDの集合)
     */
    public TreeSet<String> getGroupMembersSet(String gid) {
        return this.groupMembersTable.get(gid);
    }

    /**
     * posIDが所属しているのグループ集合を取得する
     * @param posID voxel識別子
     * @return グループの集合
     */
    public TreeSet<GroupEntry<String, Long>> getGroupEntriesSet(String posID) {
        return this.groupEntriesTable.get(posID);
    }

    /**
     * posIDに関連したグループ(gid)のタイムスタンプを取得
     * @param posID voxel識別子
     * @param gid グループ識別子
     * @return posIDが関連しているgidのタイムスタンプ，存在しない場合は0を返す．
     */
    private long getTimestamp(String posID, String gid) {
        TreeSet<GroupEntry<String, Long>> groupEntries = groupEntriesTable.get(posID);
        if (groupEntries == null) { return 0; }

        for (GroupEntry<String, Long> aGroupEntry : groupEntries) {
            if (!Objects.equals(aGroupEntry.getKey(), gid)) {
                continue;
            }
            return aGroupEntry.getValue();
        }
        return 0;
    }

    /**
     * posIDに関連したグループ(gid)のタイムスタンプを設定
     * @param ts 更新するタイムスタンプ
     * @param posID voxel識別子
     * @param gid グループ識別子
     * @return 値の更新に成功した場合はtrueを返す．失敗した場合はfalseを返す．
     */
    private boolean setTimestamp(long ts, String posID, String gid) {
        TreeSet<GroupEntry<String, Long>> groupEntries = groupEntriesTable.get(posID);
        if (groupEntries == null) { return false; }

        for (GroupEntry<String, Long> aGroupEntry : groupEntries) {
            if (Objects.equals(aGroupEntry.getKey(), gid)) {
                aGroupEntry.setValue(ts); 
                return true;
            } 
        }
        return false;
    }

    /**
     * posIDに関連したグループ(gid)が墓石か判定する
     * @param posID voxel識別子
     * @param gid グループ識別子
     * @return 墓石ならtrueを返す．それ以外はfalseを返す．
     */
    private boolean isTombstone(String posID, String gid) {
        return this.getTimestamp(posID, gid) < 0; 
    }

    /**
     * 指定したvoxelがグループ化中であるか判定する．
     * @param posID voxel識別子
     * @return グループ化中ならばtrue，そうでないならfalseを返す．
     */
    public boolean isGrouped(String posID) {
        TreeSet<GroupEntry<String, Long>> groupEntries = groupEntriesTable.get(posID);
        if (groupEntries == null) { return false; }

        for (GroupEntry<String, Long> aGroupEntry : groupEntries) {
            if (aGroupEntry.getValue() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Structure Table の状態を出力する
     */
    public void show() {
        System.out.println("groupMembersTable:");
        for (Map.Entry<String, TreeSet<String>> entry : this.groupMembersTable.entrySet()) {
            System.out.println("| " + entry.getKey() + " | -> " + entry.getValue());
        }
        System.out.println("");

        System.out.println("groupEntriesTable:");
        for (Map.Entry<String, TreeSet<GroupEntry<String, Long>>> entry : this.groupEntriesTable.entrySet()) {
            System.out.println("| " + entry.getKey() + " | -> " + entry.getValue());
        }
        System.out.println("---\n");
    }

    /**
     * Structure Table の状態を出力する
     * @param ダンプメッセージ
     */
    public void show(String dumpMsg) {
        System.out.println(dumpMsg);
        this.show();
    }

    /**
     * Structure Table のサンプル実行用
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        StructureTable stt = new StructureTable();
        stt.show("初期状態"); // 初期状態

        List<String> gids =  new ArrayList<String>();
        List<String> posIDs = Arrays.<String>asList("1:1:1", "1:2:3", "5:1:9", "7:8:0", "9:4:1");
        for (int i = 0; i < 5; ++i) {
            gids.add(UUID.randomUUID().toString());
        }

        stt.create(gids.get(0));
        stt.create(gids.get(0)); // 既にあるグループを作成

        stt.join(1L, posIDs.get(0), gids.get(0));
        stt.join(2L, posIDs.get(1), gids.get(0));
        stt.join(3L, posIDs.get(2), gids.get(0));
        stt.leave(1, 4L, posIDs.get(1), gids.get(0));
        stt.show("正常な参加・脱退");

        stt.join(5L, posIDs.get(0), gids.get(1)); // 存在しないグループへの参加
        stt.leave(1, 5L, posIDs.get(1), gids.get(1)); // 参加していないグループからの脱退
        stt.show("不正な参加・脱退");

        stt.create(gids.get(1));
        stt.create(gids.get(2));
        stt.create(gids.get(3));
        stt.join(6L, posIDs.get(0), gids.get(1));
        stt.join(7L, posIDs.get(3), gids.get(3));
        stt.leave(1, 8L, posIDs.get(3), gids.get(3));
        stt.show("グループの追加");

        stt.join(8L, posIDs.get(1), gids.get(2));
        stt.join(9L, posIDs.get(1), gids.get(2));
        stt.join(7L, posIDs.get(1), gids.get(2));
        stt.leave(1, 8L, posIDs.get(0), gids.get(0));
        stt.leave(1, 10L, posIDs.get(0), gids.get(0));
        stt.leave(1, 11L, posIDs.get(0), gids.get(1));
        stt.show("複数のjoin・leaveの収束結果");

        for (int i = 0; i < 5; ++i) {
            System.out.println(posIDs.get(i) + " isGrouped() = " + stt.isGrouped(posIDs.get(i)));
        }

    }
}
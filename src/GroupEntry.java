import java.util.AbstractMap.SimpleEntry;
import java.util.Objects;
import java.util.TreeSet;

/**
 * StructureTableで管理するGroupEntryクラス．
 * (gid, ts)としてgidにタイムスタンプを紐付けるために作成．tsの状態は同値判定に影響しない．
 *
 * @author kengo92i
 */
public class GroupEntry<K, V> extends SimpleEntry<K, V> implements Comparable<GroupEntry> {

    public GroupEntry(K k, V v) {
        super(k, v);
    }

    /**
     * 他のオブジェクトとの同一判定 
     * 同一判定はkeyの値だけで判断する． 
     * @param obj 比較対象
     */
    @Override 
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof GroupEntry) {
            final GroupEntry ge = (GroupEntry) obj;
            return Objects.equals(this.getKey(), ge.getKey());
        } 
        return false;
    }

    /**
     * 自分と他のGroupEntryを比較する．keyの値だけで判定を行う
     * @param ge 自分と比較する他のGroupEntry
     */
    @Override
    public int compareTo(GroupEntry ge) { 
        if (!Objects.equals(this.getKey(), ge.getKey())) {
            String gid1 = (String)this.getKey();
            String gid2 = (String)ge.getKey();
            return gid1.compareTo(gid2);
        }
        return 0;
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode(); 
    }

    @Override
    public String toString() {
        return "(" + this.getKey().toString() + ", " + this.getValue().toString() + ")";
    }

    public static void main(String[] args) {
        TreeSet<GroupEntry<String, Integer>> set = new TreeSet<GroupEntry<String, Integer>>();
        set.add(new GroupEntry<String, Integer>("a", 1));
        set.add(new GroupEntry<String, Integer>("abc", 5));

        GroupEntry<String, Integer> ge1 = new GroupEntry<String, Integer>("abc", 10);
        GroupEntry<String, Integer> ge2 = new GroupEntry<String, Integer>("abcd", 11);

        System.out.println(set.contains(ge1)); 
        System.out.println(set.contains(ge2)); 
        System.out.println(set.contains("abc")); 
    }
}
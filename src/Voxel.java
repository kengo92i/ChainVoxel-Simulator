/**
 * Voxelを表すクラス.
 * @author kengo92i
 */
public class Voxel implements Comparable<Voxel> {
    /**
     * Voxelを挿入したSiteID
     */
    private int id;

    /**
     * Voxelのタイムスタンプ
     */
    private long timestamp;

    /**
     * 負のVoxelを作成する．
     * @param timestamp タイムスタンプ
     */
    public Voxel(long timestamp) {
        this.id = -1; // not exists site id.
        this.timestamp = timestamp;
    }

    /**
     * 作成したSiteの識別子を持つVoxelを作成する
     * @param id Siteの識別子
     * @param timestamp タイムスタンプ
     */
    public Voxel(int id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    /**
     * Voxelを作成したSiteの識別子を返す．
     * @return Voxelを作成したSiteの識別子
     */
    public int getId() {
        return this.id;
    }

    /**
     * Voxelを作成したSiteの識別子を設定する．
     * @param id SiteID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Voxelのタイムスタンプを返す．
     * @return タイムスタンプ
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Voxelのタイムスタンプを設定する．
     * @param timestamp タイムスタンプ
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 自分と他のVoxelをタイムスタンプの昇順にソートする．同じタイムスタンプの場合は識別子の昇順で順位付けをする． 
     * @param aVoxel 自分と比較する他のVoxel
     */
    @Override
    public int compareTo(Voxel aVoxel) { 
        if (this.getTimestamp() < aVoxel.getTimestamp()) { return -1; }
        if (this.getTimestamp() > aVoxel.getTimestamp()) { return 1; }
        // timestampが同じ場合
        if (this.getId() < aVoxel.getId()) { return -1; }
        if (this.getId() > aVoxel.getId()) { return 1; }
        return 0;
    }
}

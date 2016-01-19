/**
 * Atomを表すクラス.
 * @author kengo92i
 */
public class Atom {
    /**
     * Atomを挿入したSiteID
     */
    private int id;

    /**
     * Atomのタイムスタンプ
     */
    private long timestamp;

    /**
     * 負のAtomを作成する．
     * @param timestamp タイムスタンプ
     */
    public Atom(long timestamp) {
        this.id = -1; // not exists site id.
        this.timestamp = timestamp;
    }

    /**
     * 作成したSiteの識別子を持つAtomを作成する
     * @param id Siteの識別子
     * @param timestamp タイムスタンプ
     */
    public Atom(int id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    /**
     * Atomを作成したSiteの識別子を返す．
     * @return Atomを作成したSiteの識別子
     */
    public int getId() {
        return this.id;
    }

    /**
     * Atomを作成したSiteの識別子を設定する．
     * @param id SiteID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Atomのタイムスタンプを返す．
     * @return タイムスタンプ
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Atomのタイムスタンプを設定する．
     * @param timestamp タイムスタンプ
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}

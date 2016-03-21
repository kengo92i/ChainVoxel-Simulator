/**
 * 操作を表すクラス．<br>
 * Operationクラスを利用して，ChainVoxelクラスの操作を実行する．<br>
 * Operationクラスは内部状態の変更されてはいけないため，setterを実装しない．
 * @author kengo92i
 */
public class Operation {
    /**
     * insert操作を示す定数
     */
    public static final int INSERT = 0;

    /**
     * delete操作を示す定数
     */
    public static final int DELETE = 1;

    /**
     * requestを示す定数（２層コミットのために使用）
     */
    public static final int REQUEST = 127;

    /**
     * 確認応答を示す定数（２層コミットのために使用） 
     */
    public static final int ACK = 128;

    /**
     * 操作を行なったSiteの識別子
     */
    private int id;

    /**
     * 操作オブジェクトが表す操作を指定する整数．
     * 操作タイプは{@link Operation#INSERT INSERT}と{@link Operation#DELETE DELETE}が存在．
     */
    private int opType; // 0:insert, 1:delete

    /**
     * voxelの識別子を示す文字列（形式: "X:Y:Z"）
     */
    private String posID;

    /**
     * 操作のタイムスタンプ（作成時に自動的に設定される）
     */
    private long timestamp;

    /**
     * 指定されたタイプの操作オブジェクトを作成する．
     * @param id 操作を作成したSiteの識別子
     * @param opType 操作のタイプ
     * @param posID voxelの識別子
     */
    public Operation(int id, int opType, String posID) {
        this.id = id;
        this.opType = opType;
        this.posID = posID;
        this.timestamp = System.currentTimeMillis();
    }

    /* Not exist setter method. Because, class field should not be changed since init. */

    /**
     * 操作を行なったSiteの識別子を返す．
     * @return Siteの識別子
     */
    public int getId() {
        return this.id;
    }

    /**
     * 操作のタイプを返す．
     * @return 操作のタイプを示す整数
     */
    public int getOpType() {
        return this.opType;
    }

    /**
     * voxelの識別子を返す．
     * @return voxelの識別子
     */
    public String getPosID() {
        return this.posID;
    }

    /**
     * 操作のタイムスタンプを返す．
     * @return 操作のタイムスタンプ
     */
    public long getTimestamp() {
        return this.timestamp;
    }
}

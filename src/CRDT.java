/**
 * CRDT型の抽象クラス<br>
 * atomの集合atomsを定義し，操作を実装する．<br>
 * <br>
 * M. Letia, N. Preguica, and M. Shapiro, 
 * “Consistency without concurrency control in large, dynamic systems,” 
 * ACM SIGOPS Operating System Review, vol.44, no.2, pp. 22–34, 2010.
 *
 * @author kengo92i
 */
abstract class CRDT<T, O> {
    /**
     * 任意のデータ構造で構成されるatomの集合
     */
    private T atoms;

    /**
     * atomsに新しいatomを識別子と対応付けるように挿入する<br>
     * 操作を実行するために必要なデータを保持するクラス(O)を作成して使用する．
     * @param op 操作に必要なデータを保持するクラス
     */
    abstract public void insert(O op);

    /**
     * atomsから識別子に対応するatomを削除する<br>
     * 操作を実行するために必要なデータを保持するクラス(O)を作成して使用する．
     * @param op 操作に必要なデータを保持するクラス
     */
    abstract public void delete(O op);
}

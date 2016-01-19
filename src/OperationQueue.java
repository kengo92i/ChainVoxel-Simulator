
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;
import java.util.ArrayList;

/**
 * Simulator上で共有される操作オブジェクトを管理するクラス
 * Simulator上でSiteを作成する場合に，このクラスのインスタンスを渡すことで，操作の共有が行なえる．
 * @author kengo92i
 */
public class OperationQueue {
	/**
	 * Siteの総数
	 */
	int numberOfSites = 0;
	
	/**
	 * 操作オブジェクトを管理するQueueのリスト	
	 */
	List<LinkedBlockingQueue<Operation>> opq;

	/**
	 * OperationQueueのコンストラクタ
	 * @param n 使用するQueueの総数
	 */	
	public OperationQueue(int n) {
		this.numberOfSites = n;	
		this.opq = new ArrayList<LinkedBlockingQueue<Operation>>();
	
		for (int i = 0; i < this.numberOfSites; ++i) {
			this.opq.add(new LinkedBlockingQueue<Operation>());
		}
	}
	
	/**
	 * 現在シュミレータ上に存在しているSiteの総数を返す.
	 * @return Siteの総数
	 */
	public int getNumberOfSites(){
		return this.numberOfSites;
	}
	
	/**
	 * 識別子に対応するQueueに操作オブジェクトをenqueueする．
	 * @param dest Queueの識別子
	 * @param op 操作オブジェクト
	 */
	public synchronized void enqueue(int dest, Operation op) {
		(this.opq.get(dest)).add(op);
	}
	
	/**
	 * 識別子に対応するQueueから操作オブジェクトをdequeueする．
	 * @param id Queueの識別子
	 * @return 操作オブジェクト
	 */
	public synchronized Operation dequeue(int id) {
		return (this.opq.get(id)).poll();
	}

	/**
	 * 識別子に対応するQueueを空にする．
	 * @param id Queueの識別子
	 */	
	public synchronized void clear(int id) {
		(this.opq.get(id)).clear();
	}

	/**
	 * 識別子に対応するQueueの容量を返す
	 * @param id Queueの識別子
	 * @return Queueの容量
	 */
	public int size(int id) {
		return (this.opq.get(id)).size();
	}

	/**
	 * 識別子に対応するQueueが空であるか確認するメソッド．
	 * @param id Queueの識別子
	 * @return 空の場合にtrueを返す．それ以外はfalse.
	 */
	public boolean isEmpty(int id) {
		return this.size(id) == 0;	
	}

}

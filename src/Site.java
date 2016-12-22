
import java.util.ArrayList;
import java.util.Random;

/**
 * Siteを表すクラス
 * @author kengo92i
 */
public class Site extends Thread {    
    /**
     * Siteの識別子
     */
    private int id;

    /**
     * ChainVoxel型のオブジェクト
     */        
    private ChainVoxel chainVoxel;

    /**
     * オペレーションキュー
     */
    private OperationQueue opq;
    
    /**
     * 操作の実行回数
     */
    private int numberOfOperations;    

    /**
     * XYZ座標軸の限界値
     */
    private int limitOfRange;    

    /**
     * ステップ数
     */
    public int numberOfSteps;

    /**
     * メッセージ総数
     */
    public int numberOfMessages;

    /**
     * 指定された操作数を実行するSiteを作成します．
     * @param id Siteの識別子
     * @param opq オペレーションキュー
     * @param numberOfOperations 操作の実行回数
     * @param limitOfRange XYZ座標軸の限界値
     * @see OperationQueue
     */
    Site(int id, OperationQueue opq, int numberOfOperations, int limitOfRange) {
        this.id = id;
        this.opq = opq;
        this.numberOfOperations = numberOfOperations;
        this.limitOfRange = limitOfRange;
        this.chainVoxel = new ChainVoxel();
        this.numberOfSteps = 0;
        this.numberOfMessages = 0;
    }
    
    /**
     * 指定した宛先に操作オブジェクトを送信する
     * @param dest 宛先Siteの識別子
     * @param op 操作オブジェクト
     * @see Operation
     * @see OperationQueue
     */
    public void send(int dest, Operation op) {
        this.opq.enqueue(dest, op);
    }

    /**
     * 操作を他のSiteに共有するメソッド
     * @param op 操作オブジェクト
     * @see Site#send
     */
    public void broadcast(Operation op) {
        int n = this.opq.getNumberOfSites();
        for (int i = 0; i < n; ++i) {
            if (this.id == i) continue;
            // this.delay();     
            this.send(i, op); // remote operation
        }
    }
    
    /**
     * 操作オブジェクトを受信するメソッド
     * @return 操作オブジェクト
     * @see Operation
     * @see OperationQueue
     */
    public Operation receive() {
        return opq.dequeue(id);
    }

    /**
     * Siteの識別子を取得する
     * @return Siteの識別子
     */
    public int getSiteId() {
        return this.id;
    }

    /**
     * Siteが保持するChainVoxelを取得する
     * @return ChainVoxel型のオブジェクト
     */
    public ChainVoxel getChainVoxel() {
        return this.chainVoxel;
    }
    
    /**
     * Siteに遅延を発生させるメソッド
     */
    public void delay() {
        try {
            Thread.sleep((long) Math.ceil(Math.random()*10));
        } catch (InterruptedException ie) {
            ie.printStackTrace();        
        }
    }

    /**
     * ChainVoxelに操作を適用するメソッド
     * @return ChainVoxelの容量
     * @see ChainVoxel
     * @see Operation
     */
    public int applyOperation() {
        while (!opq.isEmpty(this.id)) {
            Operation op = receive();
            chainVoxel.apply(op);
        }
        // cv.show();
        chainVoxel.exportCollada(Integer.toString(this.id));
        return chainVoxel.size();
    }

    /**
     * [-limitOfRange, limitOfRange]の範囲内の整数を返すメソッド
     * @return [-limitOfRange, limitOfRange]の範囲内の整数
     */
    private int randomIntRange() {
        return (new Random()).nextInt(2 * this.limitOfRange + 1) - this.limitOfRange;
    }

    /**
     * 操作をランダムに生成するメソッド
     * @return 操作オブジェクト
     */
    private Operation generateRandomOperation() {
        int opType = (new Random()).nextInt(2);
        String x = Integer.toString(this.randomIntRange()); 
        String y = Integer.toString(this.randomIntRange()); 
        String z = Integer.toString(this.randomIntRange()); 
        String posID = x + ":" + y + ":" + z;
        Operation op = new Operation(this.id, opType, posID);
        return op;
    }

    /**
     * 操作を指定数受け取るまで待機するメソッド
     * @param num 操作を受け取る数
     * @return 受信した操作のリスト
     */
    public ArrayList<Operation> waitReceiveOperation(int num) {
        Operation op = null; int count=0;
        ArrayList<Operation> operationList = new ArrayList<Operation>();
        while (count < num) {
            if((op = receive()) != null) {
                operationList.add(op);
                count++;
            }
        }
        return operationList;
    }


    /**
     * Raft 時のsiteの振る舞いを実行する<br>
     * <br>
     * 全ての操作をRaft に基づいて実行する．siteの故障は起きないためLeaderの選出は１度しか行わない．<br>
     * また，一貫性の収束にかかるステップ数とメッセージ数の評価が目的のため，ログレプリケーションやハートビートといった操作も考えない．<br>
     * Raftの場合は全ての操作をLeaderを介して行うため，Leaderのメッセージ数を測定することで総メッセージ数が測定できる．
     * @see Operation
     */
    private void runBehaviorOfRaft() {
        // Leaderの選出 
        int numberOfSites = this.opq.getNumberOfSites();
        if (this.id == 0) { // idが0の人がCandidateになる
            // step1: FollowerにrequestVoteを送信する
            Operation requestVote = new Operation(this.id, Operation.REQUEST_VOTE, "");
            this.send(0, requestVote);
            this.broadcast(requestVote);
            this.numberOfSteps++;
            this.numberOfMessages += numberOfSites;

            // step2: Followerからの投票を待つ
            this.waitReceiveOperation(numberOfSites);
            this.numberOfSteps++;
            this.numberOfMessages += numberOfSites; // (Leaderになるためには過半数の合意が必要)

            // step3: FollowerにLeaderになったことを報告
            Operation appendEntries = new Operation(this.id, Operation.APPEND_ENTRIES, "");
            this.broadcast(appendEntries);
            this.numberOfSteps++;
            this.numberOfMessages += numberOfSites - 1;
        } else {
            // step1: CandidateからのrequestVoteを待つ
            this.waitReceiveOperation(1);
            this.numberOfSteps++;
            this.numberOfMessages++;

            // step2: 送信元，Candidateに投票する
            Operation vote = new Operation(this.id, Operation.VOTE, "");
            this.send(0, vote);
            this.numberOfSteps++;
            this.numberOfMessages++;

            // step3: LeaderからのAppendEntriesを待つ
            this.waitReceiveOperation(1);
            this.numberOfSteps++;
            this.numberOfMessages++;
        }

        // 操作の実行を行う
        int maxTurn = this.numberOfOperations * numberOfSites;
        for (int turn = 0; turn < maxTurn; ++turn) {
            if (turn % numberOfSites == this.id) { // 操作を実行する人 
                // step0: 操作をLeaderに送信する
                Operation op = this.generateRandomOperation();
                this.send(0, op);
            }

            if (this.id == 0) { // Leaderの動作
                // step1: 送信された操作を受け取る
                Operation op = this.waitReceiveOperation(1).get(0);
                this.numberOfSteps++;
                this.numberOfMessages++;

                // step2: 操作をFollowerに共有する
                // this.send(this.id, op); // local operation は省略
                this.broadcast(op); 
                this.numberOfSteps++;
                this.numberOfMessages += numberOfSites;
            } else { // Followerの動作
                this.waitReceiveOperation(1);
                // local operation は省略
                this.numberOfSteps++;
                this.numberOfMessages++;
            }
        }
        return;
    }

    /**
     * two-phase commit 時のsiteの振る舞いを実行する <br>
     * <br>
     * 全ての操作を２層コミットに基づいて実行する．siteが故障することは考えない．
     * @see Operation
     */
    private void runBehaviorOfTwoPhaseCommit() {
        int numberOfSites = this.opq.getNumberOfSites();
        int maxTurn = this.numberOfOperations * numberOfSites;
        for (int turn = 0; turn < maxTurn; ++turn) {
            if (turn % numberOfSites == this.id) { // 調停者の動作
                // step1: 参加者にコミットの準備を求める
                Operation request = new Operation(this.id, Operation.REQUEST, "");
                this.broadcast(request);
                this.numberOfSteps++;
                this.numberOfMessages += numberOfSites - 1;

                // step2: 参加者からの確認応答を待つ
                this.waitReceiveOperation(numberOfSites - 1);
                this.numberOfSteps++;

                // step3: 操作を全員に送信する
                Operation op = this.generateRandomOperation();
                this.chainVoxel.apply(op); // local operation
                this.broadcast(op); // remote operation
                this.numberOfSteps++;
                this.numberOfMessages += numberOfSites - 1;

            } else { // 参加者の動作
                // step1: requestを待つ
                this.waitReceiveOperation(1);
                this.numberOfSteps++;

                // step2: 確認応答を返す
                Operation ack = new Operation(this.id, Operation.ACK, "");
                this.send(turn % numberOfSites, ack);
                this.numberOfSteps++;
                this.numberOfMessages += 1;

                // step3: 操作を適用する
                Operation op = null;
                while (true) {
                    op = this.waitReceiveOperation(1).get(0);
                    if (op.getOpType() == Operation.INSERT || op.getOpType() == Operation.DELETE) {
                        break;        
                    }
                    this.send(this.id, op); // 先行した調停者のREQUESTだったので元に戻す。
                }
                this.chainVoxel.apply(op);
                this.numberOfSteps++;
            }
        }

        return;
    }

    /**
     * ChainVoxel時のSiteの振る舞いを実行する
     * @see ChainVoxel
     * @see Operation
     */
    private void runBehaviorOfChainVoxel() {
        int numberOfSites = this.opq.getNumberOfSites();
        for (int i = 0; i < this.numberOfOperations; ++i) {
            Operation op = this.generateRandomOperation();
            this.chainVoxel.apply(op); // local operation
            this.broadcast(op); // remote operation
            this.numberOfSteps++;
            this.numberOfMessages += numberOfSites - 1;
        }

        return;
    }

    /**
     * Siteの動作を記述するメソッド
     * {@inheritDoc}
     */
    @Override
    public void run() {    
        //this.delay();     
        
        this.runBehaviorOfChainVoxel();
        // this.runBehaviorOfTwoPhaseCommit();
        // this.runBehaviorOfRaft();        

        return;
    }
}

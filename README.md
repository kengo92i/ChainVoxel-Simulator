# ChainVoxel-Simulator
ChainVoxelの動作を確認するためのシミュレータです．グローバルキュー(`OperationQueueクラス`)を使い，シミュレータ内のSite間で操作の共有を行います．ChainVoxelとの比較として，2相コミットベースのシステムとRaftベースのシステムのふるまいが実装されています．

## 環境

- 言語: Java1.8
- OS: MacOSX 10.11.6

## 実行方法
ChainVoxel-Simulatorのコンパイル・実行をするためにMakefileが作成されています．シミュレータのコンパイル・実行時は，ターミナルに以下のようにコマンドを入力して下さい．

    $ make
    $ make test

また，シミュレータ実行のためのパラメータ値(Site数，Site毎の操作数，ChainVoxelの扱う領域の最大値)はデフォルトでは以下のようになっています．

- Site数: 10台
- Site毎の操作数: 100回
- 扱う領域の最大値: -1 >= (x, y, z) >= 1

それぞれのパラメータ値を指定して実行する場合は，以下のようにコマンドを入力します．

    $ make
    $ make test SITES=20
    
    $ make
    $ make test SITES=5 OPERATIONS=10 LIMIT=3

`SITES`がSite数，`OPERATIONS`がSite毎の操作数，`LIMIT`が扱う領域の最大値を示しています．実行時に指定されなかったパラメータ値はデフォルト値が設定されます．

## その他の機能

### ディレクトリを掃除する
シミュレータの実行を行った後はディレクトリ内に実行ファイルなどが生成されます．コンパイル以前の状態に初期化するには以下のコマンドを入力します．

    $ make clean

上記のコマンドを入力することで，実行ファイルなどが削除されコンパイル時に作成されたファイルなどが削除されます．また，javadocなどのファイルも削除されます．

### javadoc を生成する
ChainVoxel-SImulatorのjavadocを確認する場合は，以下のコマンドを入力します．

    $ make javadoc

このコマンドを実行することで，ディレクトリ内にjavadocディレクトリが作成されます．このディレクトリ内にシミュレータのjavadocファイルが生成されています．javadocが不要になった場合は，`make clean` を実行することで削除されます．

## Site間の操作の共有
Site間の操作の共有は`Site#sendメソッド` ，`Site#broadcastメソッド` と `Site#receiveメソッド` を使用します．

それぞれのSiteには，Site生成時にidが割り当てられています．Site数が$n$の場合はそれぞれのSiteに$0 \sim n-1$までの番号が割り当てられます．`sendメソッド` では送信先Siteのidを指定することで，操作の送信を行うことができます．

    Operation op = this.randomOperation();
    this.send(5, op); // id=5のSiteにopを送信

`broadcastメソッド` は自身を除く，シミュレータ内の全てのSiteに指定した操作を送信します．

`receiveメソッド` は受信した操作を取得するメソッドです．`receiveメソッド` は受信した操作を１つ返します．受信した操作が空の場合は，nullを返します．

    Operation op = this.receive(); // 操作を１つ受信

受信している操作を全て受信したい場合は，`receiveメソッド` がnullを返すまで実行して下さい．

    List<Operation> aList = new ArrayList<Operation>();

    while (true) {
        if ((op = receive()) != null) {
            aList.add(op); // 操作を取得する
        }
    }



## Siteのふるまいを記述する
Siteのふるまいは`Site#runメソッド`に記述します．標準では３種類のふるまいが実装されているので，実行するふるまいのコメントアウトを解除してください．

    /**
     * Siteの動作を記述するメソッド．
     * {@inheritDoc}
     */
     @Override
     public void run() {    
        // this.delay();     

        this.runBehaviorOfChainVoxel();
        // this.runBehaviorOfTwoPhaseCommit();
        // this.runBehaviorOfRaft();        

        return;
     }

新たなSiteのふるまいを記述する場合は，`Siteクラス` 内に `runBehaviorOfXXXXXX` の命名規則に沿ったメソッドを実装し，`runメソッド` 内からメソッドを実行します．

### 新たな操作を定義する
ChainVoxel内の操作は`ChainVoxelクラス`と`Operationクラス`によって定義されています．`ChainVoxelクラス`では操作の処理を実装し，`Operationクラス`は操作タイプと操作に必要なパラメータを保持します．現在のシミュレータには5種類の操作が定義されています．

新たな操作を定義する場合には以下の手順を行ってください．

1. `Operationクラス` に新しい操作のための定数を定義する
2. `Operation#satisfyRequirementsメソッド`を編集する
3. `ChainVoxelクラス` に新たな操作を実装する
4. `ChainVoxel#applyメソッド` を編集する

操作タイプのための定数は1から昇順に使用しています．最後に追加された操作番号が5の場合は，次の操作には6を使用します．また，128からは降順でその他のシステムのための定数を定義しています．

`Operation#satisfyRequirementsメソッド` は操作生成時のパラメータ条件を満たしているかを確認するメソッドです．操作生成時にパラメータ条件を満たしていない場合は，`IllegalStateException` を投げます．

`ChainVoxel#applyメソッド` は操作実行用のラッパーメソッドです．引数にOperation型のオブジェクトを取り，操作タイプに合わせたメソッドを実行します．

--- kengo92i








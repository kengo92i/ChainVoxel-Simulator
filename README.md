# ChainVoxel-Simulator
ChainVoxelの動作を確認するためのシミュレータです．ChainVoxelとの比較として，2相コミットベースのシステムとRaftベースのシステムのふるまいが実装されています．シミュレータはJava1.8で実装されています．

## 実行方法
ChainVoxel-Simulatorのコンパイル・実行をするためにMakefileが作成されています．シミュレータのコンパイル・実行時は，ターミナルに以下のようにコマンドを入力して下さい．

    $ make
    $ make test

また，シミュレータ実行のためのパラメータ値(site数，site毎の操作数，ChainVoxelの扱う領域の最大値)はデフォルトでは以下のようになっています．

* site数: 10台
* site毎の操作数: 100回
* 扱う領域の最大値: -1 >= (x, y, z) >= 1

それぞれのパラメータ値を指定して実行する場合は，以下のようにコマンドを入力します．

    $ make
    $ make test SITES=20
    
    $ make
    $ make test SITES=5 OPERATIONS=10 LIMIT=3

実行時に指定されなかったパラメータ値はデフォルト値が設定されます．

## ディレクトリを掃除する
シミュレータの実行を行った後はディレクトリ内に実行ファイルなどが生成されます．コンパイル以前の状態に初期化するには以下のコマンドを入力します．

   $ make clean

上記のコマンドを入力することで，実行ファイルなどが削除されコンパイル時に作成されたファイルなどが削除されます．また，javadocなどのファイルも削除されます．

## javadoc を生成する
シミュレータ全体のjavadocを確認する場合は，以下のコマンドを入力します．

    $ make javadoc

このコマンドを実行することで，ディレクトリ内にjavadocディレクトリが作成されます．このディレクトリ内にシミュレータのjavadocファイルが生成されています．javadocが不要になった場合は，`make clean` を実行することで削除されます．

## Siteのふるまいを記述する
SiteのふるまいはSiteクラスのrunメソッドに記述します．標準では３種類のふるまいが実装されているので，実行するふるまいのコメントアウトを解除してください．

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

## 新たな操作を定義する
ChainVoxel内の操作はChainVoxelクラスとOperationクラスによって定義されています．ChainVoxelクラスでは操作自体を定義し，Operationクラスは操作に必要なパラメータを保持します．現在のシミュレータには5種類の操作が定義されています．

新たな操作を定義する場合には以下の手順を取ります．

1. Operationクラス: 新しい操作のための定数を定義する
2. Operationクラス: satisfyRequirementsメソッドを編集する
3. ChainVoxelクラス: 新たな操作を実装する

操作タイプのための定数は1から昇順に使用しています．最後に追加された操作番号が5の場合は，次の操作には6を使用します．また，128からは降順でその他のシステムのための定数を定義しています．

--- kengo92i

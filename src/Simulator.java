
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * ChainVoxelシミュレータを実行するためのクラス.
 * usage: java Simulator [number of sites] [number of operations] [limit of range]
 * @author kengo92i
 */
public class Simulator {
    /**
     * シミュレータ上で動作するsite総数
     */
    int numberOfSites;

    /**
     * 個々のsiteが行なう操作回数
     */
    int numberOfOperations;

    /**
     * ChainVoxelが扱う領域の最大値[-limitOfRange, limitOfRange]
     */
    int limitOfRange;

    /**
     * グローバルキュー
     */        
    OperationQueue opq;

    /**
     * siteを管理するためのリスト
     */        
    List<Site> sites;

    /**
     * Simulatorの処理を記述する
     * @param args コマンドライン引数
     */
    public void run(String[] args) {         
        this.numberOfSites = Integer.parseInt(args[0]);
        this.numberOfOperations = Integer.parseInt(args[1]);
        this.limitOfRange = Integer.parseInt(args[2]);
        //while (this.numberOfSites <= 20) {
        while (this.numberOfOperations <= 10000) {

            this.opq = new OperationQueue(numberOfSites);

            this.sites = new ArrayList<Site>();

            try {
                for (int i = 0; i < this.numberOfSites; i++) {
                    Site site = new Site(i, opq, numberOfOperations, limitOfRange);
                    site.start();
                    this.sites.add(site);
                }

                for (Site site : this.sites) {
                    site.join();
                }
                
                /* 
                System.out.println(
                   this.numberOfOperations * this.numberOfSites + 
                   " " + sites.get(0).numberOfSteps + " " + 
                   sites.get(0).numberOfMessages * this.numberOfSites);
                */

                /*
                System.out.println(
                   this.numberOfOperations * this.numberOfSites + 
                   " " + sites.get(0).numberOfSteps + " " + 
                   sites.get(0).numberOfMessages);
                */
                
                /*
                for (Site site : this.sites) {
                    int res = site.applyOperation();
                    System.out.println("site" + site.getSiteId() + ".size() = " + res);
                }
                */
                int res = sites.get(0).applyOperation();
                System.out.println(this.numberOfOperations * this.numberOfSites + " " + res);


            } catch (RuntimeException re) {
                re.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            this.numberOfOperations += 100;
            if (this.numberOfOperations % 500 == 0) this.limitOfRange += 1;
            //this.numberOfSites += 1;
        }
    }


    /**
     * シュミレータを起動する
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        if(args.length < 3) {
            System.out.println("usage: java Simulator [number of sites] [number of operations] [limit of range]");
            System.exit(1);
        }
        Simulator simulator = new Simulator();
        simulator.run(args);    
    }
}

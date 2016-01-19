
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

	public static void main(String[] args) {
		if(args.length < 3) {
			System.out.println("usage: java Simulator [number of sites] [number of operations] [limit of range]");
			System.exit(1);
		}
		
		int numberOfSites = Integer.parseInt(args[0]);
		int numberOfOperations = Integer.parseInt(args[1]);
		int limitOfRange = Integer.parseInt(args[2]);
		
		OperationQueue opq = new OperationQueue(numberOfSites);

		List<Site> sites = new ArrayList<Site>();
		
		try {
			for (int i = 0; i < numberOfSites; i++) {
				Site site = new Site(i, opq, numberOfOperations, limitOfRange);
				site.start();
				sites.add(site);
			}

			for (Site site : sites) {
				site.join();
			}

			for (Site site : sites) {
				int res = site.executeOperation();
				System.out.println("site" + site.getSiteId() + ".size() = " + res);
			}

			System.out.println("exit(0);");
		} catch (RuntimeException re) {
			re.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
}

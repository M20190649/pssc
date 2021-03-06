package threads;

import data.DataConnector;
import data.DataConnector4File;
import data.Point;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author GYZ
 * @DESCRIPTION Test the speedup of building similar matrix
 * The actual speedup is depending on your hardware configuration
 * You can set threads' number according to your Core and CPU
 * @create 2018-01-23 18:40
 **/
public class SimiMatrixThreadTest {

  // init dataConnector
  private static DataConnector dataConnector = DataConnector4File.getInstance();

  /*
    * 
    * Calculate time cost of parallel building
    * Similar matrix
    * @author gyz
    * @date 2018/1/24 22:15
    * @param [number of threads]
    * @return long  
    */  
  private static long parallelBuildSimiMatrix (int thNum) throws  IOException, InterruptedException{
    System.out.println("Starting parallel computing");
    long t1 = System.currentTimeMillis();
    // init data
    List<List<Point>> trajs = dataConnector.getTrajData();
    int len = trajs.size();
    double[][] w = new double[len][len];
    // Threads pool
    ExecutorService threadPool = Executors.newFixedThreadPool(thNum);
    CountDownLatch threadSignal = new CountDownLatch(thNum);
    // Starting calculating
    for(int i = 0; i < thNum; i++){
      threadPool.execute(new SimiMatrixThread(20, w, trajs, (i * len) / thNum,
          (i + 1) * len / thNum, threadSignal));
    }
    threadSignal.await();
    threadPool.shutdown();
    long t2 = System.currentTimeMillis();
    // Ending multi threading calculating
    System.out.println("Ending parallel calculating");
    return t2 - t1;
  }

  /*
    *
    * Calculate time cost of serial building
    * Similar matrix
    * @author gyz
    * @date 2018/1/24 22:19
    * @param []
    * @return long
    */
  private static long serialBuildSimiMatrix() throws  IOException, InterruptedException{
    System.out.println("Starting serial computing");
    long t1 = System.currentTimeMillis();
    // init data
    List<List<Point>> trajs = dataConnector.getTrajData();
    int len = trajs.size();
    double[][] w = new double[len][len];
    CountDownLatch singleSignal = new CountDownLatch(1);
    Thread th = new SimiMatrixThread(20, w, trajs, 0,
        len, singleSignal);
    th.start();
    singleSignal.await();
    long t2 = System.currentTimeMillis();
    System.out.println("Ending serial computing");
    return t2 - t1;
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.println("-----START TEST-----");
    long pTime = parallelBuildSimiMatrix(32);
    long sTime = serialBuildSimiMatrix();
    System.out.println("Speedup = " + 1.0 *  sTime / pTime );
    System.out.println("-----END TEST-------");
  }

}

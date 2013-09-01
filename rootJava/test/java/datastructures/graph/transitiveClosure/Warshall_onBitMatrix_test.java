package datastructures.graph.transitiveClosure;


import datastructures.graph.basic.GraphAdjMatrixBit;
import org.junit.Assert;
import org.junit.Assert.*;
import org.junit.Test;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/1/13
 * Time: 8:41 AM
 */
public class Warshall_onBitMatrix_test {
  private Logger log =
          Logger.getLogger(Warshall_onBitMatrix_test.class);


  /**
   * Warshall_onBitMatrix_test,main:31 -  final transitive graph
   * Warshall_onBitMatrix_test,main:32 - 01 1110000000 0000000000 0000000000
   * Warshall_onBitMatrix_test,main:33 - 01 1110000000 0000000000 0000000000
   * Warshall_onBitMatrix_test,main:34 - 00 0000000000 0000000000 0000000000
   * Warshall_onBitMatrix_test,main:35 - 01 1110000000 0000000000 0000000000
   */
  @Test
  public void test00() {
    GraphAdjMatrixBit g = graph00();
    Warshall_onBitMatrix w = new Warshall_onBitMatrix(g);

    w.transitiveClosure();

    if (isP()) {
      log.debug(" final transitive graph");
      log.debug(w.printRow(1));
      log.debug(w.printRow(2));
      log.debug(w.printRow(3));
      log.debug(w.printRow(4));
    }

    // exhaustive check
    t(w, 1, 1);
    t(w, 1, 2);
    t(w, 1, 3);
    t(w, 1, 4);

    t(w, 2, 1);
    t(w, 2, 2);
    t(w, 2, 3);
    t(w, 2, 4);

    f(w, 3, 1);
    f(w, 3, 2);
    f(w, 3, 3);
    f(w, 3, 4);

    t(w, 4, 1);
    t(w, 4, 2);
    t(w, 4, 3);
    t(w, 4, 4);

  }


  private void t(Warshall_onBitMatrix g, int v, int w) {
    Assert.assertTrue("Expected " + v + "-->" + w + " to be connected but they are NOT! ", g.connected(v, w));
  }

  private void f(Warshall_onBitMatrix g, int v, int w) {
    Assert.assertFalse("Expected " + v + "-->" + w + " to be NOT connected but they are ! ", g.connected(v, w));
  }

  /**
   * simplest digraph
   * <p/>
   * 1  --->   2
   * <       |
   * \      |
   * \     |
   * \    |
   * \   |
   * \ \/
   * 3 <------ 4
   *
   * @return
   */
  private GraphAdjMatrixBit graph00() {
    GraphAdjMatrixBit g = new GraphAdjMatrixBit(32, true);

    g.insert(1, 2);
    g.insert(2, 4);
    g.insert(4, 3);
    g.insert(4, 1);
    return g;

  }

  private boolean isP() {
    return log.isDebugEnabled();
  }
}

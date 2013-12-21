package datastructures.MST;

import datastructures.graph.basic.CloneableInteger;
import datastructures.graph.basic.Edge;
import datastructures.graph.basic.GraphAdjList;
import datastructures.graph.minimumCuts.CollapsableGraphAdjList;
import org.junit.Assert;
import org.junit.Test;
import utils.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/7/13
 * Time: 2:41 PM
 */
public abstract class MST_base_test {

  //@Test
  public void simpleTest() {
    GraphAdjList graph = createSimpleGraph();

    MST_testable testImpl = getMST_testable();
    int result = testImpl.compute(graph);
    int expected = 7;

    Assert.assertTrue("Expected " + expected + ", but we got " + result, (expected == result));
  }

  @Test
  public void sedgewickTest() {
    GraphAdjList graph = createSedgewickGraph();

    MST_testable testImpl = getMST_testable();
    int result = testImpl.compute(graph);
    int expected = 204;

    Assert.assertTrue("Expected " + expected + ", but we got " + result, (expected == result));
  }




  /**
   * // set up a simple test graph
   * //
   * //   4 vertex undirected graph
   * //
   * //             1
   * //         2 ---- 1
   * //         |   /  |
   * //      2  |  /3  | 4
   * //         | /    |
   * //         3 ---- 4
   * //             5
   * //
   * //   MST path length = 1 + 2 + 4 = 7
   *
   * @return
   */
  protected GraphAdjList createSimpleGraph() {

    // create a String representation of the adj list representation of the array
    // then read that into our Java representation
    String[] sa = new String[5];

    sa[0] = ("1 2 1");
    sa[1] = ("2 3 2");
    sa[2] = ("3 4 5");
    sa[3] = ("1 4 4");

    return graphFromStringArray(sa);
  }


  /**
   * Graph from Sedgewick Figure 20.2 scaled up to integer
   *
   * min path weight = 29 + 34 + 18 + 46 + 31 + 25 + 21 =  204
   *
   * @return
   */
  protected GraphAdjList createSedgewickGraph() {
    String[] sa = new String[12];

    sa[0] = ("0 6 51");
    sa[1] = ("0 1 32");
    sa[2] = ("0 2 29");
    sa[3] = ("4 3 34");
    sa[4] = ("5 3 18");
    sa[5] = ("7 4 46");
    sa[6] = ("5 4 40");
    sa[7] = ("0 5 60");
    sa[8] = ("6 4 51");
    sa[9] = ("7 0 31");
    sa[10] = ("7 6 25");
    sa[11] = ("7 1 21");

    return graphFromStringArray(sa);
  }


  private GraphAdjList graphFromStringArray(String[] sa) {
    int arraySize = sa.length + 1;
    GraphAdjList graph = new GraphAdjList(arraySize, false);

    for (int i = 0; i < sa.length; i++) {
      //System.err.println("read line '" + sa[i] + "'");
      if (sa[i] != null) {
        String[] s = sa[i].split("\\s+");
        int[] in = StringUtils.stringArrayToIntArray(s);
        //System.err.println("int array length=" + in.length);

        CloneableInteger weight = new CloneableInteger(in[2]);

        //System.err.println(" in[2]=" + in[2] + ",  edge " + in[0] + "-" + in[1] + "  w=" + weight);

        graph.insert(new Edge(in[0], in[1], weight));
        graph.insert(new Edge(in[1], in[0], weight));
        //System.err.println(StringUtils.printStringArray(s));
      }
    }
    return graph;

  }

  protected abstract MST_testable getMST_testable();
}

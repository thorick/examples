package datastructures.graph.shortestPath;


import datastructures.graph.basic.GraphAdjList;
import datastructures.graph.util.GraphUtils;

import org.junit.Assert;
import org.junit.Test;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/3/13
 * Time: 7:53 PM
 */
public class Dijkstra_test {

  private Logger log =
          Logger.getLogger(Dijkstra_test.class);


  @Test
  public void test00_Dasgupta() {
    GraphAdjList graph = createDasguptaGraph();

    if (isP()) {
      log.debug("loaded graph "+graph.toString());
    }

    //Dijkstra_00_Naive dijkstra = new Dijkstra_00_Naive(graph);
    //Dijkstra_01_Heap dijkstra = new Dijkstra_01_Heap(graph);
    Dijkstra_02_Heap dijkstra = new Dijkstra_02_Heap(graph);

    // distances from vertex 1
    dijkstra.compute(1);


    int[] expected = new int[] {Integer.MAX_VALUE, 0, 3, 2, 5, 6};
    int[] found = dijkstra.getDistArray();
    checkResults(expected, found);

  }


  //@Test
  public void test01_Sedgewick() {
    GraphAdjList graph = createSedgewickGraph();

    if (isP()) {
      log.debug("loaded graph "+graph.toString());
    }

    //Dijkstra_00_Naive dijkstra = new Dijkstra_00_Naive(graph);
    //Dijkstra_01_Heap dijkstra = new Dijkstra_01_Heap(graph);
    Dijkstra_02_Heap dijkstra = new Dijkstra_02_Heap(graph);


    // distances from vertex 0
    dijkstra.compute(0);

    int[] expected = new int[] {0, 41, 82, 86, 50, 29};
    int[] found = dijkstra.getDistArray();
    checkResults(expected, found);

  }

  private void checkResults(int[] exp, int[] r) {
    if (r.length < exp.length) Assert.assertTrue("result length="+r.length+", less than expected="+exp.length, false);

    for (int i=0 ; i<exp.length ; i++) {
      if (r[i] != exp[i]) {
        Assert.assertTrue("result at vertex="+i+" "+r[i]+", does not match expected "+exp[i], false);
      }
    }
  }

  /**
   * Create the very simple graph from the Dasgupta example
   *
   * @return
   */
  private GraphAdjList createDasguptaGraph() {

    GraphAdjList graph = new GraphAdjList(6, true);

    String[] all = new String[4];
    all[0] = "1";  all[1]="2,4";  all[2]="3,2";
    GraphUtils.loadWeightedVertex(graph, all);


    all[0] = "2";  all[1] ="3,3";  all[2]="4,2";  all[3]="5,3";
    GraphUtils.loadWeightedVertex(graph, all);


    all[0] = "3";  all[1]="2,1";  all[2]="4,4";  all[3]="5,5";
    GraphUtils.loadWeightedVertex(graph, all);

    all[0]="4";  all[1]=null;  all[2]=null;  all[3]=null;
    GraphUtils.loadWeightedVertex(graph, all);

    all[0]="5";  all[1]="4,1";
    GraphUtils.loadWeightedVertex(graph, all);

    return graph;
  }


  /**
   * Create the very simple graph from the Sedgewick example  ch 21
   *
   * edge weights scaled up to whole integers
   *
   * @return
   */
  private GraphAdjList createSedgewickGraph() {

    GraphAdjList graph = new GraphAdjList(6, true);

    String[] all = new String[3];
    all[0] = "0";  all[1]="1,41";  all[2]="5,29";
    GraphUtils.loadWeightedVertex(graph, all);


    all[0] = "1";  all[1] ="2,51";  all[2]="4,32";
    GraphUtils.loadWeightedVertex(graph, all);


    all[0] = "2";  all[1]="3,50";  all[2]=null;
    GraphUtils.loadWeightedVertex(graph, all);

    all[0]="3";  all[1]="5,38";  all[2]="0,45";
    GraphUtils.loadWeightedVertex(graph, all);

    all[0]="4";  all[1]="3,36";  all[2]="2,32";
    GraphUtils.loadWeightedVertex(graph, all);

    all[0]="5";  all[1]="4,21";  all[2]="1,29";
    GraphUtils.loadWeightedVertex(graph, all);

    return graph;
  }

  protected boolean isP() {
    return log.isDebugEnabled();
  }
}

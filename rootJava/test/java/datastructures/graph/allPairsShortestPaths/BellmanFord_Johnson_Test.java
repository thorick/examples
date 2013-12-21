package datastructures.graph.allPairsShortestPaths;


import datastructures.graph.basic.CloneableInteger;
import datastructures.graph.basic.Edge;
import datastructures.graph.basic.GraphAdjList;
import datastructures.graph.basic.GraphAdjList_digraph;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/30/13
 * Time: 8:41 PM
 */
public class BellmanFord_Johnson_Test {

  private Logger log =
          Logger.getLogger(BellmanFord_Johnson_Test.class);


  ///////////////////////////
  //   Johnson  tests
  //

  //@Test
  public void testJ_simple() {
    GraphAdjList_digraph g = createSimpleGraph();
    Johnsons_APSP johnsons = getJohnsons_APSP(g);
    johnsons.compute();

    int expected = 1;
    int shortestShortestPathLength = johnsons.getShortestShortestPathLength();
    Assert.assertEquals("expected shortestShortestPathLength="+expected+
    ", instead we got="+shortestShortestPathLength, expected, shortestShortestPathLength);
  }

  //@Test
  public void testJ_negative_undetectableCycle() {
    GraphAdjList_digraph g = createSingleNegativeCycleGraph();
    Johnsons_APSP johnsons = getJohnsons_APSP(g);
    johnsons.compute();

    int expected = 1;
    int shortestShortestPathLength = johnsons.getShortestShortestPathLength();
    Assert.assertEquals("expected shortestShortestPathLength="+expected+
    ", instead we got="+shortestShortestPathLength, expected, shortestShortestPathLength);
  }

  @Test
  public void testJ_simpleLectureGraph() {
     GraphAdjList_digraph g = createSimpleJohnsonLectureGraph();
     Johnsons_APSP johnsons = getJohnsons_APSP(g);
     johnsons.compute();

     int expected = -6;
     int shortestShortestPathLength = johnsons.getShortestShortestPathLength();
     Assert.assertEquals("expected shortestShortestPathLength="+expected+
     ", instead we got="+shortestShortestPathLength, expected, shortestShortestPathLength);
   }


  ///////////////////////////
  //   Bellman Ford tests
  //
  /*
  BellmanFord_Johnson_Test,main:42 -  pre negative cycle check  shortest paths from 1

  BellmanFord_Johnson_Test,main:44 -  shortest path: 1->2 = 2, path = 1 -> 1 -> 2

  BellmanFord_Johnson_Test,main:44 -  shortest path: 1->3 = 3, path = 1 -> 1 -> 2 -> 3

  BellmanFord_Johnson_Test,main:44 -  shortest path: 1->4 = 4, path = 1 -> 1 -> 2 -> 4

  BellmanFord_Johnson_Test,main:44 -  shortest path: 1->5 = 6, path = 1 -> 1 -> 2 -> 4 -> 5

   */
  //@Test
  public void testBF_simple() {
    GraphAdjList g = createSimpleGraph();
    BellmanFord bf = getBellmanFord(g);
    int sourceVertex = 1;
    bf.compute(sourceVertex);

    int[] toVertex = new int[]{1, 2, 3, 4, 5};
    int[] expected = new int[]{0, 2, 3, 4, 6};
    for (int i = 1; i < toVertex.length; i++) {
      int computedDist = bf.shortestDistanceTo(toVertex[i]);
      Assert.assertEquals("From sourceVertex=" + sourceVertex + " toVertex=" + toVertex +
              ", expected shortestDistance=" + expected[i] + ", instead we got=" +
              computedDist, expected[i], computedDist);
    }

    log.debug(" pre negative cycle check  shortest paths from " + sourceVertex);
    for (int i = 2; i <= 5; i++) {
      log.debug(" shortest path: " + sourceVertex + "->" + i + " = " + bf.shortestDistanceTo(i) +
              ", path = " + bf.printPathTo(i));
    }

    boolean hasNegativeCycle = bf.hasNegativeCycle();
    Assert.assertFalse("Expected to find NO negative cycles, but we did !", hasNegativeCycle);
  }


  //@Test
  public void testSingleNegativeCycle_fails() {
    GraphAdjList g = createSingleNegativeCycleGraph();
    BellmanFord bf = getBellmanFord(g);
    int sourceVertex = 1;
    bf.compute(sourceVertex);


    boolean hasNegativeCycle = bf.hasNegativeCycle();
    Assert.assertTrue("Expected to find negative cycle, but we did NOT !", hasNegativeCycle);
  }


  /*
  BellmanFord_Johnson_Test,main:68 -  pre negative cycle check  shortest paths from 1

  BellmanFord_Johnson_Test,main:70 -  shortest path: 1->2 = -1, path = 1 -> 4 -> 3 -> 2

  BellmanFord_Johnson_Test,main:70 -  shortest path: 1->3 = -4, path = 1 -> 2 -> 4 -> 3

  BellmanFord_Johnson_Test,main:70 -  shortest path: 1->4 = 4, path = 1 -> 3 -> 2 -> 4

  BellmanFord_Johnson_Test,main:70 -  shortest path: 1->5 = 9, path = 1 -> 3 -> 2 -> 4 -> 5

   */
  //@Test
  public void testBF_singleNegativeCycle2() {
    GraphAdjList g = createSingleNegativeCycleGraph2();
    BellmanFord bf = getBellmanFord(g);
    int sourceVertex = 1;
    bf.compute(sourceVertex);

    log.debug(" pre negative cycle check  shortest paths from " + sourceVertex);
    for (int i = 2; i <= 5; i++) {
      log.debug(" shortest path: " + sourceVertex + "->" + i + " = " + bf.shortestDistanceTo(i) +
              ", path = " + bf.printPathTo(i));
    }


    boolean hasNegativeCycle = bf.hasNegativeCycle();
    Assert.assertTrue("Expected to find negative cycle, but we did NOT !", hasNegativeCycle);
  }


  ///////////////////////////////


  /*
     Simple Lecture graph

   */
  private GraphAdjList_digraph createSimpleGraph() {
    GraphAdjList_digraph g = new GraphAdjList_digraph(10);
    insert(1, 2, 2, g);
    insert(1, 3, 4, g);
    insert(2, 3, 1, g);
    insert(2, 4, 2, g);
    insert(3, 5, 4, g);
    insert(4, 5, 2, g);

    log.debug("simpleGraph=\n"+g.toString());
    //System.err.println("simpleGraph=\n"+g.toString());
    return g;
  }


  /*
    Simple graph with a single negative cycle

    we are currently unable to detect this  !!

   */
  private GraphAdjList_digraph createSingleNegativeCycleGraph() {
    GraphAdjList_digraph g = new GraphAdjList_digraph(10);
    insert(1, 2, 2, g);
    insert(1, 4, 4, g);
    insert(2, 3, 1, g);
    insert(2, 4, 1, g);
    insert(3, 5, -4, g);
    insert(3, 6, 3, g);
    insert(4, 7, 3, g);
    insert(7, 6, 2, g);
    return g;
  }


  /*
      Simple graph with a single negative cycle

      we are currently unable to detect this  !!

     */
  private GraphAdjList_digraph createSingleNegativeCycleGraph2() {
    GraphAdjList_digraph g = new GraphAdjList_digraph(10);
    insert(1, 2, 1, g);
    insert(1, 3, 2, g);
    insert(2, 4, 4, g);
    insert(3, 2, 3, g);
    insert(4, 3, -8, g);
    insert(4, 5, 5, g);
    return g;
  }


  /*
    lecture graph
    a - 1
    b - 2
    c - 3
    x - 4
    y - 5
    z - 6

   */
  private GraphAdjList_digraph createSimpleJohnsonLectureGraph() {
    GraphAdjList_digraph g = new GraphAdjList_digraph(10);
    insert(1, 2, -2, g);
    insert(2, 3, -1, g);
    insert(3, 1, 4, g);
    insert(3, 4, 2, g);
    insert(3, 5, -3, g);
    insert(6, 4, 1, g);
    insert(6, 5, -4, g);
    return g;
  }


  /**
   * convenience method to insert directional edge(s)
   *
   * @param v
   * @param w
   * @param g
   */
  private void insert(int v, int w, int weight, GraphAdjList g) {
    CloneableInteger i = new CloneableInteger(weight);
    Edge e = new Edge(v, w, i);
    g.insert(e);
  }

  private BellmanFord getBellmanFord(GraphAdjList g) {
    return new BellmanFord_edgeArray_strict_wNegativeCycleDetection(g);
  }

  private Johnsons_APSP getJohnsons_APSP(GraphAdjList_digraph g) {
    return new Johnsons_AllPairsShortestPaths_Stanford_Coursera(g);
  }
}

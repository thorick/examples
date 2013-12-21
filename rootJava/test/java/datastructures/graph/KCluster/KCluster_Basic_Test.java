package datastructures.graph.KCluster;

import datastructures.graph.basic.CloneableInteger;
import datastructures.graph.basic.GraphAdjList;
import org.junit.Assert;
import org.junit.Test;
import org.apache.log4j.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/18/13
 * Time: 6:14 PM
 */
public class KCluster_Basic_Test {

  private Logger log =
          Logger.getLogger(KCluster_Basic_Test.class);


  @Test
  public void testSimpleK2() {
    int KCount = 2;
    int expected = 13;

    GraphAdjList g = createSimpleLectureGraph();
    KCluster4_Stanford_Coursera prog = new KCluster4_Stanford_Coursera();
    int result = prog.compute(g, KCount);
    Assert.assertEquals("Error !  for K=" + KCount + ", expected " + expected + " but instead we got " + result, result, expected);
  }


  @Test
  public void testSimpleK3() {
    int KCount = 3;
    int expected = 7;

    GraphAdjList g = createSimpleLectureGraph();
    KCluster4_Stanford_Coursera prog = new KCluster4_Stanford_Coursera();
    int result = prog.compute(g, KCount);
    Assert.assertEquals("Error !  for K=" + KCount + ", expected " + expected + " but instead we got " + result, result, expected);
  }


  @Test
  public void testSimpleK4() {
    int KCount = 4;
    int expected = 5;

    GraphAdjList g = createSimpleLectureGraph();
    KCluster4_Stanford_Coursera prog = new KCluster4_Stanford_Coursera();
    int result = prog.compute(g, KCount);
    Assert.assertEquals("Error !  for K=" + KCount + ", expected " + expected + " but instead we got " + result, result, expected);
  }


  GraphAdjList createSimpleLectureGraph() {
    GraphAdjList graph = new GraphAdjList(10, false);

    insertEdge(1, 2, 5, graph);
    insertEdge(1, 3, 15, graph);
    insertEdge(1, 4, 20, graph);

    insertEdge(2, 4, 13, graph);
    insertEdge(3, 5, 3, graph);
    insertEdge(4, 5, 11, graph);

    insertEdge(4, 3, 7, graph);
    insertEdge(4, 6, 1, graph);
    insertEdge(5, 6, 9, graph);

    return graph;

  }

  void insertEdge(int u, int v, int weight, GraphAdjList g) {
    g.insert(new ComparableEdge(u, v, new CloneableInteger(weight)));
    g.insert(new ComparableEdge(v, u, new CloneableInteger(weight)));
  }
}

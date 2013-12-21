package algorithms.NPComplete.travelingSalesman;

import datastructures.graph.basic.CloneableInteger;
import datastructures.graph.basic.Edge;
import datastructures.graph.basic.GraphAdjList;
import org.apache.log4j.Logger;
import org.junit.Test;

import org.junit.Assert;


import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 10/6/13
 * Time: 4:12 PM
 */
public class TravelingSalesmanTest {

  private Logger log =
          Logger.getLogger(TravelingSalesmanTest.class);

  GraphAdjList edgeGraph;    // to hold test edge distances

  @Test
  public void testSimpleLectureGraph() {
    City[] c = createSimpleLectureGraph();

    TravelingSalesman_DP prog = new TravelingSalesman_DP(c);

    double result = prog.compute();
    double expected = 13.0;
    System.err.println(" result=" + result);
    Assert.assertEquals("expected=" + expected + ", but we got=" + result, expected, result, 0.00001);

  }

  //@Test
  public void testSimpleLectureGraphSwapped() {
    City[] c = createSimpleLectureGraphSwapped();

    TravelingSalesman_DP prog = new TravelingSalesman_DP(c);

    double result = prog.compute();
    double expected = 13.0;
    System.err.println(" result=" + result);
    Assert.assertEquals("expected=" + expected + ", but we got=" + result, expected, result, 0.00001);

  }


  private City[] createSimpleLectureGraph() {
    edgeGraph = new GraphAdjList(10, false);

    int[] v = new int[]{1, 2, 3, 4};
    City[] cities = loadVertices(v, edgeGraph);

    loadBidirectionalEdge(1, 2, 1);
    loadBidirectionalEdge(1, 3, 2);
    loadBidirectionalEdge(1, 4, 4);
    loadBidirectionalEdge(2, 3, 3);
    loadBidirectionalEdge(2, 4, 6);
    loadBidirectionalEdge(3, 4, 5);
    if (isP()) {
      log.debug(" simple lecture graph is\n" + edgeGraph.toString());
    }

    return cities;
  }

  private City[] createSimpleLectureGraphSwapped() {
    edgeGraph = new GraphAdjList(10, false);

    //int[] v = new int[] {1, 2, 3, 4};
    int[] v = new int[]{2, 4, 3, 1};
    City[] cities = loadVertices(v, edgeGraph);

    loadBidirectionalEdge(1, 2, 1);
    loadBidirectionalEdge(1, 3, 2);
    loadBidirectionalEdge(1, 4, 4);
    loadBidirectionalEdge(2, 3, 3);
    loadBidirectionalEdge(2, 4, 6);
    loadBidirectionalEdge(3, 4, 5);
    if (isP()) {
      log.debug(" simple lecture graph is\n" + edgeGraph.toString());
    }

    return cities;
  }

  private void loadBidirectionalEdge(int v, int w, int weight) {
    CloneableInteger edgeW = new CloneableInteger(weight);
    Edge e = new Edge(v, w, edgeW);
    edgeGraph.insert(e);

    e = new Edge(w, v, edgeW);
    edgeGraph.insert(e);
  }

  private City[] loadVertices(int[] v, GraphAdjList g) {
    City[] retVal = new City_Edge[v.length];
    int index = 0;
    for (int i : v) {
      retVal[index++] = new City_Edge(i, g);
    }
    return retVal;
  }

  private boolean isP() {
    return log.isDebugEnabled();
  }

  /**
   * A City class that has only distances not coordinates
   * The distances need to be stored elsewhere and referenced
   * by all instances of this class
   */
  protected class City_Edge implements City {
    protected int id;
    protected GraphAdjList graph;

    protected City_Edge(int id, GraphAdjList g) {
      this.id = id;
      graph = g;
    }

    public int getId() {
      return id;
    }

    public double distance(City o) {
      City_Edge other = (City_Edge) o;
      List<Edge> l = graph.getEdges(id, o.getId());


      Edge e = l.get(0);
      int weight = ((CloneableInteger) e.getData()).intValue();
      return weight;    // java type coercion !
    }
  }


}

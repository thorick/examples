package datastructures.graph.AdjacencyMatrix;

import datastructures.graph.basic.GraphAdjMatrixBit;
import org.junit.Assert;
import org.junit.Test;
import org.apache.log4j.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/30/13
 * Time: 5:59 PM
 */
public class testGraphAdjArrayBits {

  private Logger log =
          Logger.getLogger(testGraphAdjArrayBits.class);

  @Test
  public void basicArray() {
    GraphAdjMatrixBit graph = new GraphAdjMatrixBit(64, true);

    graph.insert(0, 1);
    Assert.assertTrue("expected edge 0-1, did not get it !", graph.edge(0, 1));
    Assert.assertFalse("did NOT expect edge 0-2, but we got it !", graph.edge(0, 2));

    log.debug("????? after insert of 0-1 row 0 is " + graph.printRow(0));
    log.debug("????? after insert of 0-1 col 1 is " + graph.printCol(1));

    graph.insert(0, 4);
    Assert.assertTrue("expected edge 0-4, did not get it !", graph.edge(0, 4));

    graph.insert(1, 2);
    Assert.assertTrue("expected edge 1-2, did not get it !", graph.edge(1, 2));

    graph.insert(3, 2);
    Assert.assertTrue("expected edge 3-2, did not get it !", graph.edge(3, 2));
  }

  @Test
  public void basicAxisElements() {
    GraphAdjMatrixBit graph = new GraphAdjMatrixBit(64, true);

    graph.insert(4, 1);
    graph.insert(4, 3);
    graph.insert(4, 5);

    GraphAdjMatrixBit.AxisElements ae4 = graph.new AxisElements(true, 4);
    ae4.resetIterator();

    Assert.assertFalse("expected (4,0) to be false !", ae4.nextValue());
    Assert.assertTrue("expected (4,1) to be true !", ae4.nextValue());
    Assert.assertFalse("expected (4,2) to be false !", ae4.nextValue());
    Assert.assertTrue("expected (4,3) to be false !", ae4.nextValue());
    Assert.assertFalse("expected (4,4) to be false !", ae4.nextValue());
    Assert.assertTrue("expected (4,5) to be true !", ae4.nextValue());
    Assert.assertFalse("expected (4,6) to be false !", ae4.nextValue());

    log.debug("+++++++ begin Column 1 test");
    GraphAdjMatrixBit.AxisElements ae1 = graph.new AxisElements(false, 1);
    ae1.resetIterator();
    Assert.assertFalse("expected (1,0) to be false !", ae1.nextValue());
    Assert.assertFalse("expected (1,1) to be false !", ae1.nextValue());
    Assert.assertFalse("expected (1,2) to be false !", ae1.nextValue());
    Assert.assertFalse("expected (1,3) to be false !", ae1.nextValue());
    Assert.assertTrue("expected (1,4) to be true !", ae1.nextValue());
    Assert.assertFalse("expected (1,5) to be false !", ae1.nextValue());
  }

  @Test
  public void twoElementGraph() {
    GraphAdjMatrixBit graph = makeTwoElementGraph();

    t(graph, 1, 32);
    t(graph, 1, 42);
    t(graph, 1, 10);
    t(graph, 5, 10);
    t(graph, 55, 2);
    t(graph, 56, 12);
    t(graph, 56, 42);
    t(graph, 56, 60);

    f(graph, 1, 31);
    f(graph, 1, 30);
    f(graph, 55, 0);

    GraphAdjMatrixBit.AxisElements ae1 = graph.new AxisElements(true, 1);
    for (int i = 0; i <= 9; i++) {
      Assert.assertFalse("expected (1," + i + ") to be false !", ae1.nextValue());
    }
    Assert.assertTrue("expected (1,10) to be true !", ae1.nextValue());

    for (int i = 11; i <= 31; i++) {
      Assert.assertFalse("expected (1," + i + ") to be false !", ae1.nextValue());
    }
    Assert.assertTrue("expected (1,32) to be true !", ae1.nextValue());

    log.debug("resume curr index=" + ae1.currIndex());

    for (int i = 33; i <= 41; i++) {
      Assert.assertFalse("expected (1," + i + ") to be false !", ae1.nextValue());
    }
    Assert.assertTrue("expected (1,42) to be true !", ae1.nextValue());

  }

  /**
   * create a graph that will require 2 ints per row or column
   * be sure that the vertices span the 2 ints
   *
   * @return
   */
  private GraphAdjMatrixBit makeTwoElementGraph() {
    GraphAdjMatrixBit graph = new GraphAdjMatrixBit(64, true);

    graph.insert(1, 32);
    graph.insert(1, 42);
    graph.insert(1, 10);
    graph.insert(5, 10);

    graph.insert(55, 2);
    graph.insert(56, 12);
    graph.insert(56, 42);
    graph.insert(56, 60);
    return graph;
  }


  private void t(GraphAdjMatrixBit g, int v, int w) {
    Assert.assertTrue("Expected " + v + "-->" + w + " to exist but it does NOT! ", g.edge(v, w));
  }

  private void f(GraphAdjMatrixBit g, int v, int w) {
    Assert.assertFalse("Expected " + v + "-->" + w + " to not exist but it DOES ! ", g.edge(v, w));
  }


}


package datastructures.graph.minimumCuts;


import datastructures.graph.basic.Edge;
import datastructures.graph.basic.Graph;
import datastructures.graph.basic.LinkedNode;
import org.junit.Assert;
import org.junit.Test;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/25/13
 * Time: 2:53 PM
 * <p/>
 * Various tests to exercise the AdjacencyList Matrix classes
 * with Collapsable Vertices that are intended to be used
 * to execute the Karger Graph Minimum Cut Algorithm
 */
public class CollapseEdgeTest {

  private Logger log =
          Logger.getLogger(CollapseEdgeTest.class);


  /**
   * Test collapse of a single node.
   * Where both the collapsedFrom and collapsedTo nodes both have
   * an edge to the same vertex head H.
   * <p/>
   * The collapsed node should yield us a pair of parallel edges:
   * one from the collapsedFrom node to the head H
   * one from the collapsedTo   node to the head H
   */
  //@Test
  public void testCollapse01() {
    String m = "testCollapse01: ";

    CollapsableGraphAdjList_EdgeLists g = initGraph00();
    List<CollapsableEdge> listE = g.getCollapsableEdges();

    if (isP()) {
      log.debug(m + " initialized graph is " + g.toString());
    }
    // collapse vertex 2 into vertex 3
    int collapseFrom = 2;
    int collapseTo = 3;
    CollapsableEdge e = g.newEdge(collapseFrom, collapseTo, null);
    g.collapseEdge(e, listE);


    if (isP()) {
      log.debug(m + " after collapse 2 into 3, the graph is" + g.toString() + "\n");
      log.debug(m + " now check that there is NO vertex 2...   get Edges for vertex=" + collapseFrom);
    }
    // now check the collapsed vertices
    // there should be NO vertex 2
    LinkedNode n = g.getAdjList(collapseFrom);

    Assert.assertTrue("expected vertex 2 to be gone, it's not !", (n == null));

    // check that there are now 3 edges from collapseTo node 3

    log.debug("now check that there are 3 edges from vertex=" + collapseTo);

    n = g.getAdjList(collapseTo);

    log.debug("nodeList for vertex=" + collapseTo + ":  " + n.printNodeChain());

    int count = 0;
    while (n != null) {
      count++;
      n = n.next();
    }

    Assert.assertTrue("expected vertex 3 to have 3 edges, instead we found " + count, (count == 3));

    log.debug("now verify edges from " + collapseTo);

    // now verify the edges
    int count1 = 0;
    int count4 = 0;

    n = g.getAdjList(collapseTo);
    log.debug("now verify edges from " + collapseTo + ":  " + n.printNodeChain());


    while (n != null) {
      int v = n.vertexTailNumber();
      if (v != collapseTo) {
        Assert.assertTrue("Error !  found node with wrong tail=" + v + ", expected " + collapseTo, false);
      }
      int w = n.vertexHeadNumber();
      if (w == 1) {
        count1++;
      } else if (w == 4) {
        count4++;
      } else {
        Assert.assertTrue("Error ! found node with unexpected head=" + w + ", expected 1 or 4", false);
      }
      n = n.next();
    }
    if (count1 != 2) {
      Assert.assertTrue("Error ! expected 2 edges to '1' instead we got " + count1, false);
    }
    if (count4 != 1) {
      Assert.assertTrue("Error ! expected 1 edges to '1' instead we got " + count4, false);
    }


  }

  /**
   * Collapse down to 2 remaining nodes.
   * Do the operations in testCollapse01
   * then add more until we've only 2 vertices remaining.
   */
  @Test
  public void testCollapse02() {
    String m = "testCollapse02: ";

    CollapsableGraphAdjList_EdgeLists g = initGraph00();
    List<CollapsableEdge> listE = g.getCollapsableEdges();
    //Set<Edge> setE = makeSet(listE);


    if (isP()) {
      log.debug(m + " initialized graph is " + g.toString());
    }
    // collapse vertex 2 into vertex 3
    int collapseFrom = 2;
    int collapseTo = 3;
    CollapsableEdge e = g.newEdge(collapseFrom, collapseTo, null);
    g.collapseEdge(e, listE);

    if (isP()) {
      log.debug(m + " after collapse 2 into 3, the graph is" + g.toString());
    }

    collapseFrom = 1;
    collapseTo = 4;
    e = g.newEdge(collapseFrom, collapseTo, null);
    g.collapseEdge(e, listE);

    if (isP()) {
      log.debug(m + " after collapse 1 into 4, the graph is" + g.toString());
    }


    // now check the collapsed vertices
    // there should be NO vertex 2 nor vertex 1
    LinkedNode n = g.getAdjList(2);

    Assert.assertTrue("expected vertex 2 to be gone, it's not !", (n == null));

    n = g.getAdjList(1);
    Assert.assertTrue("expected vertex 1 to be gone, it's not !", (n == null));


    // check that there are now 3 edges from node 4
    n = g.getAdjList(3);
    int count = 0;
    while (n != null) {
      count++;
      n = n.next();
    }

    Assert.assertTrue("expected vertex 3 to have 3 edges, instead we found " + count, (count == 3));


    // check that there are now 3 edges from node 4
    n = g.getAdjList(4);
    count = 0;
    while (n != null) {
      count++;
      n = n.next();
    }

    Assert.assertTrue("expected vertex 4 to have 3 edges, instead we found " + count, (count == 3));


    // now verify the edges from vertex 3
    //  we expect to find an edge and 2 collapsed edges
    //    v = 3  original edges  2-1, 3-1   edge 3 -4
    //    4 = 4  original edges  1-2, 1-3   edge 4 -3

    n = g.getAdjList(3);
    int count3normal = 0;
    int count3orig21 = 0;
    int count3orig31 = 0;
    while (n != null) {
      int tail = n.vertexTailNumber();
      Assert.assertTrue("expected tail=" + 3 + " got " + tail + " instead", (tail == 3));
      int head = n.vertexHeadNumber();
      Assert.assertTrue("expected head=" + 4 + " got " + head + " instead", (head == 4));

      e = (CollapsableEdge) n.edge();

      List<CollapsableEdge> originalEdges = e.getOriginalEdges();

      //if (e.getOriginalEdge() == null) {
      if (originalEdges == null || originalEdges.size() == 0) {
        count3normal++;
      } else if (originalEdges != null && originalEdges.size() == 1) {
        CollapsableEdge eo = originalEdges.get(0);
        if (eo.v == 3) {
          if (eo.w == 1) {
            count3orig31++;
          } else {
            Assert.assertTrue("unexpected original edge " + eo + ", expected 3-1", false);
          }
        }
      } else if (originalEdges != null && originalEdges.size() == 2) {
        for (CollapsableEdge eo : originalEdges) {
          if (eo.v == 2) {
            if (eo.w != 1) {
              Assert.assertTrue("unexpected original edge " + eo + " expected 2-1", false);
            }
            count3orig21++;
          } else if (eo.v == 3) {
            if (eo.w != 1) {
              Assert.assertTrue("unexpected original edge " + eo + " expected 2-1", false);

            }
            count3orig21++;
          } else {
            Assert.assertTrue("unexpected original edge " + eo + ", expected 2-1 or 3-1", false);
          }
        }
      } else {
        Assert.assertTrue("unexpected Original edge size=" + originalEdges.size() + " expected 0, 1, 2", false);
      }
      n = n.next();
    }

    Assert.assertTrue("no native edge for 3-4 !", (count3normal == 1));
    Assert.assertTrue("no native edge for 3-4 o(3-1) !", (count3orig31 == 1));
    Assert.assertTrue("no native edge for 3-4 o(2-1), o(3-1) !", (count3orig21 == 2));
  }

  // set up a simple test graph
  //
  //   4 vertex undirected graph
  //
  //
  //         2 -- 1
  //         |  / |
  //         | /  |
  //         |/   |
  //         3 -- 4
  //

  private CollapsableGraphAdjList_EdgeLists initGraph00() {
    CollapsableGraphAdjList_EdgeLists g = new CollapsableGraphAdjList_EdgeLists(5, false);

    insert(2, 3, g);
    insert(2, 1, g);
    insert(3, 1, g);
    insert(3, 4, g);
    insert(1, 4, g);
    return g;
  }

  /**
   * convenience method to insert bidirectional edge(s)
   *
   * @param v
   * @param w
   * @param g
   */
  private void insert(int v, int w, CollapsableGraphAdjList_EdgeLists g) {
    CollapsableEdge e = g.newEdge(v, w, null);
    g.insert(e);
    e = g.newEdge(w, v, null);
    g.insert(e);
  }


  private Set<Edge> makeSet(List<Edge> l) {
    HashSet<Edge> h = new HashSet<Edge>();
    for (Edge e : l) {
      h.add(e);
    }
    return h;
  }

  protected boolean isP() {
    return log.isDebugEnabled();
  }


}

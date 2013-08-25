package datastructures.graph.minimumCuts;


import datastructures.graph.basic.Edge;
import datastructures.graph.basic.Graph;
import datastructures.graph.basic.LinkedNode;
import org.junit.Assert;
import org.junit.Test;
import org.apache.log4j.Logger;

import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/25/13
 * Time: 2:53 PM
 *
 * Various tests to exercise the AdjacencyList Matrix classes
 * with Collapsable Vertices that are intended to be used
 * to execute the Karger Graph Minimum Cut Algorithm
 *
 *
 */
public class CollapseVertexTest {

  private Logger log =
          Logger.getLogger(CollapseVertexTest.class);


  /**
   *  Test collapse of a single node.
   *  Where both the collapsedFrom and collapsedTo nodes both have
   *  an edge to the same vertex head H.
   *
   *  The collapsed node should yield us a pair of parallel edges:
   *    one from the collapsedFrom node to the head H
   *    one from the collapsedTo   node to the head H
   *
   */
  //@Test
  public void testCollapse01() {
    String m = "testCollapse01: ";

    CollapsableGraphAdjList g = initGraph00();

    if (isP()) {
      p(m+" initialized graph is "+g.toString());
    }
    // collapse vertex 2 into vertex 3
    int collapseFrom = 2;
    int collapseTo   = 3;
    g.collapseVertex(collapseFrom, collapseTo);


    if (isP()) {
      p(m+" after collapse 2 into 3, the graph is"+g.toString());
    }
    // now check the collapsed vertices
    // there should be NO vertex 2
    LinkedNode n = g.getAdjList(collapseFrom);

    Assert.assertTrue("expected vertex 2 to be gone, it's not !", (n == null));

    // check that there are now 3 edges from collapseTo node 3
    n = g.getAdjList(collapseTo);
    int count = 0;
    while (n != null) {
      count++;
      n = n.next();
    }

    Assert.assertTrue("expected vertex 3 to have 3 edges, instead we found "+count, (count == 3));

    // now verify the edges
    int count1 = 0;
    int count4 = 0;

    n = g.getAdjList(collapseTo);
    while (n != null) {
      int v = n.vertexTailNumber();
      if (v != collapseTo) {
        Assert.assertTrue("Error !  found node with wrong tail="+v+", expected "+collapseTo, false);
      }
      int w = n.vertexHeadNumber();
      if (w == 1) {
        count1++;
      }
      else if (w == 4) {
        count4++;
      }
      else {
        Assert.assertTrue("Error ! found node with unexpected head="+w+", expected 1 or 4", false);
      }

    }
    if (count1 != 2) {
       Assert.assertTrue("Error ! expected 2 edges to '1' instead we got "+count1, false);
    }
    if (count4 != 1) {
      Assert.assertTrue("Error ! expected 1 edges to '1' instead we got "+count4, false);
    }


  }

  /**
   *
   * Collapse down to 2 remaining nodes.
   * Do the operations in testCollapse01
   *   then add more until we've only 2 vertices remaining.
   *
   *
   */
  @Test
  public void testCollapse02() {
    String m = "testCollapse02: ";

    CollapsableGraphAdjList g = initGraph00();

    if (isP()) {
      p(m+" initialized graph is "+g.toString());
    }
    // collapse vertex 2 into vertex 3
    int collapseFrom = 2;
    int collapseTo   = 3;
    g.collapseVertex(collapseFrom, collapseTo);

    if (isP()) {
        p(m+" after collapse 2 into 3, the graph is"+g.toString());
      }

    collapseFrom = 1;
    collapseTo   = 4;
    g.collapseVertex(collapseFrom, collapseTo);

    if (isP()) {
        p(m+" after collapse 1 into 4, the graph is"+g.toString());
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

    Assert.assertTrue("expected vertex 3 to have 3 edges, instead we found "+count, (count == 3));


    // check that there are now 3 edges from node 4
    n = g.getAdjList(4);
    count = 0;
    while (n != null) {
      count++;
      n = n.next();
    }

    Assert.assertTrue("expected vertex 4 to have 3 edges, instead we found "+count, (count == 3));


    // now verify the edges from vertex 3
    //  we expect to find an edge and 2 collapsed edges
    //    v = 3  original edges  2-1, 3-1   edge 3 -4
    //    4 = 4  original edges  1-2, 1-3   edge 4 -3

    n = g.getAdjList(3);
    int count3normal = 0;
    int count3orig21 = 0;
    int count3orig31 = 0;
    while (n != null) {
      int tail  = n.vertexTailNumber();
      Assert.assertTrue("expected tail="+3+" got "+tail+" instead", (tail == 3));
      int head  = n.vertexHeadNumber();
      Assert.assertTrue("expected head="+4+" got "+head+" instead", (head == 4));

      CollapsableEdge e = (CollapsableEdge)n.edge();

      List<CollapsableEdge> originalEdges = e.getOriginalEdges();
      //if (e.getOriginalEdges() == null) {
      if (originalEdges.size() == 0) {
        count3normal++;
      }
      else {

        // todo: broken
        Edge eo = null;
        //Edge eo = e.getOriginalEdge();
        if (eo.v == 2) {
          if (eo.w == 1) {
            count3orig21++;
          }
          else {
            Assert.assertTrue("edge: "+e+", originalEdge: "+eo+", expected originalEdge 2-1", false);
          }
        }
        else  if (eo.v == 3) {
          if (eo.w == 1) {
            count3orig31++;
          }
          else {
            Assert.assertTrue("edge: "+e+", originalEdge: "+eo+", expected originalEdge 3-1", false);
          }
        }
        else {
          Assert.assertTrue("edge: "+e+", originalEdge: "+eo+" unexpected tail="+eo.v, false);
        }
      }

    }
    Assert.assertTrue("no native edge for 3-4 !", (count3normal == 1));

    //    4 = 4  original edges  1-2, 1-3   edge 4 -3

    n = g.getAdjList(4);
    int count4normal = 0;
    int count4orig12 = 0;
    int count4orig13 = 0;
    while (n != null) {
      int tail  = n.vertexTailNumber();
      Assert.assertTrue("expected tail="+4+" got "+tail+" instead", (tail == 4));
      int head  = n.vertexHeadNumber();
      Assert.assertTrue("expected head="+3+" got "+head+" instead", (head == 3));

      CollapsableEdge e = (CollapsableEdge)n.edge();

      //if (e.getOriginalEdge() == null) {
      if (true) {
        count4normal++;
      }
      else {
        Edge eo = null;
        //Edge eo = e.getOriginalEdge();
        if (eo.v == 1) {
          if (eo.w == 2) {
            count4orig12++;
          }
          else {
            Assert.assertTrue("edge: "+e+", originalEdge: "+eo+", expected originalEdge 1-2", false);
          }
        }
        else  if (eo.v == 1) {
          if (eo.w == 3) {
            count4orig13++;
          }
          else {
            Assert.assertTrue("edge: "+e+", originalEdge: "+eo+", expected originalEdge 1-3", false);
          }
        }
        else {
          Assert.assertTrue("edge: "+e+", originalEdge: "+eo+" unexpected tail="+eo.v, false);
        }
      }

    }
    Assert.assertTrue("no native edge for 3-4 !", (count4normal == 1));

    /*
    int count1 = 0;
    int count4 = 0;

    n = g.getAdjList(collapseTo);
    while (n != null) {
      int v = n.vertexTailNumber();
      if (v != collapseTo) {
        Assert.assertTrue("Error !  found node with wrong tail="+v+", expected "+collapseTo, false);
      }
      int w = n.vertexHeadNumber();
      if (w == 1) {
        count1++;
      }
      else if (w == 4) {
        count4++;
      }
      else {
        Assert.assertTrue("Error ! found node with unexpected head="+w+", expected 1 or 4", false);
      }

    }
    if (count1 != 2) {
       Assert.assertTrue("Error ! expected 2 edges to '1' instead we got "+count1, false);
    }
    if (count4 != 1) {
      Assert.assertTrue("Error ! expected 1 edges to '1' instead we got "+count4, false);
    }
    */

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

  private CollapsableGraphAdjList initGraph00() {
    CollapsableGraphAdjList g = new CollapsableGraphAdjList(5, false);

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
  private void insert(int v, int w, Graph g) {
    //CollapsableEdge e = new CollapsableEdge(v, w, null);
    CollapsableEdge e = null;
    g.insert(e);
    //e = new CollapsableEdge(w, v, null);
    g.insert(e);
  }



  protected void p(String s) {
    log.debug(s);
  }

  protected boolean isP() {
    return log.isDebugEnabled();
  }


}

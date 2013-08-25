package datastructures.graph.minimumCuts;

import datastructures.graph.basic.CloneableData;
import datastructures.graph.basic.Edge;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/24/13
 * Time: 6:54 PM
 * <p/>
 * This is an Edge specially for the Karger Minimum Cuts algorithm.
 * <p/>
 * When a vertex A is collapsed into another vertex B resulting
 * in the 'disappearance' of the vertex A.
 * <p/>
 * Any edge from A to some C is now an edge from B to C.
 * This 'new' edge is parallel to any existing edges that may already
 * exist from B to C.
 * <p/>
 * The 'new' edge must remember it's original tail and head that it had
 * *before* collapsing.  This is so that we can know what cuts the 'collapsed'
 * edge represents from the original graph.
 */
public class CollapsableEdge extends Edge {

  private Logger log =
          Logger.getLogger(CollapsableEdge.class);

  private List<CollapsableEdge> originalEdges;  // the first edge ever associated with the pre-collapsed nodes
  public long stamp;

  // only the owning graph should construct the edge,
  // the graph contains the edge's unique stamp
  //
  CollapsableEdge(int v, int w, long stamp, CloneableData d) {
    super(v, w, d);
    this.stamp = stamp;
  }

  /**
   * This edge is morphed from it's old vertices to it's new ones
   *
   * The new ones are assumed to already exist
   *   so when this finishes the number edges in the graph has decreased by 1
   *
   * <p/>
   * All original edges must be saved for posterity.
   *
   * @param oldTail
   * @param newTail
   * @param oldHead
   * @param newHead
   */
  public void shrink(int oldTail, int newTail, int oldHead, int newHead, long newStamp) {
    if (oldTail != v || oldHead != w) throw new RuntimeException("Error attempt to shrink wrong " +
            "edge tail=" + v + ", head=" + w + "  but we were told that oldTail=" + oldTail + ", and oldHead=" + oldHead);

    String m = "shrink oldT=" + oldTail + ", newT=" + newTail + ", oldH=" + oldHead + ", newH=" + newHead + ", newStamp=" + newStamp;
    if (isP()) {
      p(m);
    }
    // if there is no saved off originalEdge, then save off the original
    if (originalEdges == null) {
      originalEdges = new LinkedList<CollapsableEdge>();
    }
    CollapsableEdge originalEdge = new CollapsableEdge(v, w, stamp, data);
    if (isP()) {
      p(m + " adding originalEdge=" + originalEdge.toString());
    }
    originalEdges.add(originalEdge);


    v = newTail;
    w = newHead;
    stamp = newStamp;
    if (isP()) {
      p(m + " edge is now " + v + "-" + w + "--" + stamp);
    }
  }

  public boolean equals(Object other) {

    if (!(other instanceof CollapsableEdge)) return false;
    CollapsableEdge eo = (CollapsableEdge) other;
    if (eo.v != v) return false;
    if (eo.w != w) return false;
    if (eo.stamp != stamp)  return false;
    return true;
  }

  public CollapsableEdge clone() {
    CloneableData newData = null;
    if (newData != null) {
      try {
        newData = data.clone();
      } catch (CloneNotSupportedException e) {
      }
    }
    return new CollapsableEdge(v, w, stamp, newData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Edge: "+v+"-"+w);
    if (originalEdges != null) {
      CollapsableEdge e = originalEdges.get(0);
      if (e != null)   sb.append(" o("+e.v+","+e.w+")");
      /*
      boolean first = true;
      for (CollapsableEdge e: originalEdges) {
        if (!first)  sb.append(",");
        sb.append(" o("+e.v+","+e.w+")");
      }
      */
    }
    sb.append("-s").append(stamp).append(".");
    return sb.toString();
  }

  public List<CollapsableEdge> getOriginalEdges() {
    return originalEdges;
  }


  protected boolean isP() {
    return log.isDebugEnabled();
  }


  protected void p(String s) {
    log.debug(s);
  }


  /*

  Here's a result that is actually correct:  there are 2 edges  the original  10-11 IS the edge

  we are miscounting the edges and should be saving only the 'original' original

  each collapsed edge should only count as one edge not multiple as shown here


  start iteration: 0 on copy
  iteration: 0 found 4 edges.  min is now: 4
  1: 4
  1: 4
  -- minimum 4
    edges:
  Edge: 10-15 o(10,11) o(10,12)-s317.Edge: 15-10 o(11,10) o(12,10)-s318.

  java.lang.AssertionError: expected 2 bd edges (1 undirected), instead we got 4 bd.


   */
}
package datastructures.graph.minimumCuts;

import datastructures.graph.basic.*;


import datastructures.graph.util.GraphUtils;
import org.apache.log4j.Logger;


import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/24/13
 * Time: 7:33 PM
 *
 * Special Characteristics of this Graph:
 *
 * Each each has a unique LONG ID, this is so that parallel edges can be distinguished
 * and a given parallel edge can identified be operated on without affecting the others.
 *
 * A Collapsed Edge retains a list of it's chain of original edges.
 *
 */
public class CollapsableGraphAdjList_EdgeLists extends GraphAdjList {

  private Logger log =
          Logger.getLogger(CollapsableGraphAdjList_EdgeLists.class);


  // unique stamp to distinguish parallel edges
  private AtomicLong edgeStamp = new AtomicLong(0);

  // non-ThreadSafe reusable collapseResult
  private CollapseResult collapseResult;

  public CollapsableGraphAdjList_EdgeLists(int size, boolean isDigraph) {
    super(size, isDigraph);
    collapseResult = new CollapseResult();
  }

  /*
  @Override
  public synchronized int insert(Edge e) {
    int v = e.v;
    int w = e.w;

    if (adj[v] == null) {
      vCount++;
    }

    // handle registered vertex with no edge.
    // insert a self-edge if there is not already one

    // link new node into the head of the list
    adj[v] = new LinkedNodeImpl(v, e, adj[v]);
    return ++eCount;
  }
   */

  /**
   * Collapse the given vertex 'collapseFrom' into 'collapseTo':
   * <p/>
   * Look up the 'collapseFrom' vertex in the graph.
   * Collect all the old edges with the 'collapseFrom' vertex as the tail.
   * <p/>
   * Remove any (new self) edge that points from 'collapseFrom' to 'collapseTo'
   * Remove any (new self) edge that points from 'collapseTo'   to 'collapseFrom'.
   * <p/>
   * Each of the heads of old edges will have to be looked up and:
   * For each head point the head's head to the new 'collapseTo' vertex
   * save the old edge for the head and create the new edge.
   * <p/>
   * Now do the collapse on the 'collapseFrom' vertex
   * Collect all the old edges in the collapseFrom vertex
   * <p/>
   * save the original vertex if there is not yet an original vertex
   * set the new vertex on the 'collapseTo' vertex
   * <p/>
   * If there are any edges between 'collapseFrom' and 'collapseTo'
   * these are now self edges to the collapsed Vertext.
   * Delete these edges.
   */
  public CollapseResult collapseEdge(CollapsableEdge edge, List<CollapsableEdge> edges) {
    collapseResult.init();

    LinkedNode nFrom = adj[edge.v];
    if (nFrom != null) {
      LinkedNode nTo = adj[edge.w];
      if (nTo != null) {
        String m = "collapseEdge:  collapse vertices in " + edge + ": ";
        //
        // remove all self edges that will be created as a consequence of the collapse
        // note that if the self edge is at the head of the edge list
        // then we will have a *new* head of the edge list so we'll have to
        // re-get the head of the list afterwards
        //
        if (isP()) {
          log.debug("\n\n\n\n\n  BEGIN collapse vertices in edge  " + edge + ", collapse from=" + edge.v + " in to=" + edge.w + "\n\n\n");
        }

        removeAllEdges(edge, edges);
        removeAll(edge);

        if (isP()) {
          log.debug(m + "after removal of SELF EDGE " + edge + " edges.size=" + edges.size() + ", graph has " + eCount() + " edges.   nodeList of " + edge.v + " is " +
                  printNodeList(adj[edge.v]) + "\n");
          log.debug(m + "  graph is " + toString());
          log.debug(m + "  edges is " + GraphUtils.printListCollapsableEdge(edges));
          checkEdgeCounts(edges);
        }

        // remove the reverse self edge

        Edge edgeR = newSimpleEdge(edge.w, edge.v);
        removeAllEdges(edgeR, edges);
        removeAll(edgeR);

        if (isP()) {
          log.debug(m + "after removal of BACKWARDS SELF EDGE  " + edgeR + " edges.size=" + edges.size() + ", graph has " + eCount() + " edges.   nodeList of " + edgeR.v + " is " +
                  printNodeList(adj[edgeR.v]) + "\n");
          log.debug(m + "  graph is " + toString());
          log.debug(m + "  edges is " + GraphUtils.printListCollapsableEdge(edges) + "\n\n");
          checkEdgeCounts(edges);
        }

        // reget roots as they may have been deleted during previous 'removes'
        nFrom = adj[edge.v];
        nTo = adj[edge.w];

        if (isP()) {
          log.debug(m + " after reset roots " + edge.v + " is " + printNodeList(nFrom));
          log.debug(m + " after reset roots " + edge.w + " is " + printNodeList(nTo) + "\n\n");
        }

        //
        // process all collapseFrom edges now
        //   look up each of the pointed to head nodes
        //
        LinkedNode currNode = nFrom;
        while (currNode != null) {
          // look up head vertex number
          int currHeadNumber = currNode.edge().w;

          if (isP()) {
            log.debug(m + "processing BACK EDGES  currNode edge " + currNode.edge() + " backEdges from " + currHeadNumber + "-" + edge.v + "\n");
          }
          //
          // BACK EDGES

          // now deal with the to be replaced vertex/edge back edges [currHeadNumber, collapseFrom]
          // replace them with [currHeadNumber, collapseTo]
          // and if there is no saved 'original' edge, then save that now

          List<CollapsableEdge> backList = getCollapsableEdges(currHeadNumber, edge.v);

          if (isP()) {
            log.debug(m + "  BACK EDGES at start we have the following back edges to process: " + GraphUtils.printListCollapsableEdge(backList) + "\n\n\n");
          }

          for (CollapsableEdge backEdge : backList) {
            CollapsableEdge cBackEdge = (CollapsableEdge) backEdge;    // sorry about the cast

            // remove the old back edge connecting the old vertices
            if (isP()) {
              log.debug(m + " remove backedge " + cBackEdge);
              log.debug(m + " before remove " + cBackEdge + " edges contains: " + GraphUtils.printListCollapsableEdge(edges));
              log.debug(m + "  graph is " + toString());
              log.debug(m + "  edges is " + GraphUtils.printListCollapsableEdge(edges));
              checkEdgeCounts(edges);
            }

            removeEdge(cBackEdge, edges);
            removeSingle(cBackEdge);

            if (isP()) {
              log.debug(m + " PRE-SHRINK  after removal of edge " + cBackEdge + " edges.size=" + edges.size() + ", graph has " + eCount() + " edges.");
              log.debug(m + "  graph is " + toString());
              log.debug(m + "  edges is " + GraphUtils.printListCollapsableEdge(edges));
              checkEdgeCounts(edges);
            }

            // the shrunken edge is the original edge instance
            //  but now it's  vertices are different
            //
            cBackEdge.shrink(currHeadNumber, currHeadNumber, edge.v, edge.w, edgeStamp.incrementAndGet());

            // re-insert the shrunken back edge to the new vertices
            if (isP()) {
              log.debug(m + " insert new POST-SHRINK backedge " + cBackEdge);
            }

            // you have to add back in the edge with new vertices
            boolean found = edges.add(cBackEdge);
            insert(cBackEdge);

            if (isP()) {
              log.debug(m + " POST-SHRINK after insert edges.size=" + edges.size() + ", graph has " + eCount() + " edges.  new vertex list " + printNodeList(adj[currHeadNumber]));
              log.debug(m + "  graph is " + toString());
              log.debug(m + "  edges is " + GraphUtils.printListCollapsableEdge(edges) + "\n\n");
              checkEdgeCounts(edges);
            }
          }

          // FORWARD EDGES
          //
          // all forward edges have to have their tails readjusted so
          // that they point FROM the new collapseTo vertex
          //
          List<CollapsableEdge> forwardList = getCollapsableEdges(edge.v, currHeadNumber);

          if (isP()) {
            log.debug(m + " process all FORWARD EDGES   " + edge.v + "-" + currHeadNumber +
                    "  " + GraphUtils.printListCollapsableEdge(forwardList) + "\n\n\n");
          }
          for (CollapsableEdge forwardEdge : forwardList) {
            CollapsableEdge cForwardEdge = (CollapsableEdge) forwardEdge;

            // remove the old forward edge connecting the old vertices
            // remove the old back edge connecting the old vertices
            if (isP()) {
              log.debug(m + " remove forward edge " + cForwardEdge);
            }

            removeEdge(cForwardEdge, edges);
            removeSingle(cForwardEdge);

            if (isP()) {
              log.debug(m + " PRE-SHRINK  after removal of edge " + edgeR + " edges.size=" + edges.size() + ", graph has " + eCount() + " edges");
              log.debug(m + "  graph is " + toString());
              log.debug(m + "  edges is " + GraphUtils.printListCollapsableEdge(edges));
              checkEdgeCounts(edges);
            }
            // shrink the forward edge from the old vertices
            //
            // the shrunken edge is the original edge instance
            //  but now it's  vertices are different
            //
            cForwardEdge.shrink(edge.v, edge.w, currHeadNumber, currHeadNumber, edgeStamp.incrementAndGet());
            // re-insert the shrunken forward edge to the new vertices
            if (isP()) {
              log.debug(m + " insert new forwardedge " + cForwardEdge);
            }

            boolean found = edges.add(cForwardEdge);
            insert(cForwardEdge);

            if (isP()) {
              log.debug(m + " POST-SHRINK after insert edges.size=" + edges.size() + ", graph has " + eCount() + " edges.  new vertex list " + printNodeList(adj[edge.w]));
              log.debug(m + "  graph is " + toString());
              log.debug(m + "  edges is " + GraphUtils.printListCollapsableEdge(edges));
              checkEdgeCounts(edges);
            }
          }
          if (isP()) {
            log.debug(m + " after process all forward edges  " + edge.v + "-" + currHeadNumber +
                    " edges at " + edge.v + ": " + printNodeList(adj[edge.v]) + "\n\n");
          }
          // on to the next vertex from the old-tail to some head
          currNode = currNode.next();
        }

        if (isP()) {
          log.debug("\n\n\n   END  after collapseEdge  edges.size=" + edges.size() + ", graph edge count=" + eCount() + "\n\n\n");
        }
      } else {
        throw new RuntimeException("Error !  null collapseTo node in edge " + edge);
      }
    } else {
      throw new RuntimeException("Error !  null collapseFrom node in edge " + edge);
    }
    return collapseResult;
  }

  public synchronized List<CollapsableEdge> getCollapsableEdges() {
    List<CollapsableEdge> retVal = new LinkedList<CollapsableEdge>();
    for (int i = 0; i < adj.length; i++) {
      LinkedNode n = adj[i];
      while (n != null) {
        retVal.add((CollapsableEdge) n.edge());
        n = n.next();
      }
    }
    return retVal;
  }


  public synchronized List<CollapsableEdge> getCollapsableEdges(int v, int w) {
    List<CollapsableEdge> retVal = new LinkedList<CollapsableEdge>();
    if (v > sizeLimit || w > sizeLimit) {
      log.debug("Warning !  received request for edge(" + v + ", " + w + ") with a vertex " +
              "that exceeds this graphs preset vertex limit=" + vCount);

      return retVal;
    }
    LinkedNode n = adj[v];
    if (n == null) return retVal;

    while (n != null) {
      if (n.vertexHeadNumber() == w) {
        retVal.add((CollapsableEdge) n.edge());
      }
      n = n.next();
    }
    return retVal;
  }


  /**
   * requires a CollapsableEdge with a unique edge identifier
   *
   * @param e
   */
  public synchronized void removeSingle(CollapsableEdge e) {
    removeInternal(e, false);
  }

  /**
   * does not require a CollapsableEdge as ALL parallel edges
   * will be removed indiscriminately
   *
   * @param e
   */
  public synchronized void removeAll(Edge e) {
    removeInternal(e, true);
  }


  public synchronized void removeInternal(Edge e, boolean all) {
    LinkedNode n = adj[e.v];
    if (n == null) return;

    String m = "remove: ";
    if (isP())
      log.debug(m + "tail=" + e.v + ", head=" + e.w + ". start nodelist: " + printNodeList(n));

    LinkedNode tempRoot = new LinkedNodeImpl(e.v, new Edge(e.v, -1), n);

    if (isP()) {
      log.debug(m + "created tempRoot next = nodelist above " + printNodeList(tempRoot));
      LinkedNode tn = tempRoot.next();
      log.debug(m + "tempRoot.next yields " + printNodeList(tn));
    }

    /*
    // special case target edge is the root
    // replace the root with the next node
    if (n.vertexHeadNumber() == e.w) {
      adj[e.v] = n.next();
      return;
    }
     */

    // now look for edge and remove it
    LinkedNode prev = tempRoot;
    boolean done = false;
    while (!done && (prev != null)) {
      n = prev.next();
      if (n != null) {
        // there can be parallel edges, get them all
        CollapsableEdge edge = (CollapsableEdge) n.edge();

        if (isP())
          log.debug(m + " " + edge + "  and e.w=" + e.w);
        if (n.vertexHeadNumber() == e.w) {
          if (isP())
            log.debug(m + " " + edge + "  inside  remove part");
          if (!all) {

            // if we are not removing ALL parallel edges
            // then we must check to see that we are removing
            //   ONLY the edge instance specified
            CollapsableEdge cInputEdge = (CollapsableEdge) e;

            if (edge.stamp == cInputEdge.stamp) {
              eCount--;
              prev.setNext(n.next()); // can be null
              done = true;

              if (isP())
                log.debug(m + " in NOT ALL removed " + edge + " DONE.");
            }
          } else {
            // remove ALL parallel edges between v and w
            //  indiscriminately
            eCount--;
            prev.setNext(n.next()); // can be null

            if (isP())
              log.debug(m + " in ALL removed " + edge);
            if (n.next() != null) {
              log.debug(m + ",  n.next=" + n.next().edge() + ",  prev.next is now " + prev.next().edge());
            }
          }
        } else {
          prev = prev.next();
        }
      } else {
        prev = null;   // end of chain we're done so signal
      }
    }
    adj[e.v] = tempRoot.next();   // can be null

    // only decrement the vertex count if we have removed ALL
    //  edges
    //
    // todo:  umm  don't we have to be sure that back edges are gone too ?
    //
    if (adj[e.v] == null) {
      --vCount;
      if (isP()) {
        log.debug(m + "edge list for " + e.v + " is now NULL,  decremented vCount to " + vCount());
      }
    }

    if (isP())
      log.debug(m + "tail=" + e.v + ", head=" + e.w + ". ending nodelist: " + printNodeList(adj[e.v]) + "\n");
  }


  public synchronized CollapsableGraphAdjList_EdgeLists copy() {
    int size = adj.length;
    CollapsableGraphAdjList_EdgeLists copy = newGraph(size, isDigraph);
    // Clone all vertex entries
    //  Edges must be copies because someone might
    //  change the Edge data in the new Graph
    //
    for (int i = 0; i < size; i++) {
      LinkedNode l = adj[i];
      while (l != null) {
        CollapsableEdge e = (CollapsableEdge) l.edge();

        // todo: write the clone to allow cloning of edge data
        //Edge cloneEdge = e.clone();
        CollapsableEdge cloneEdge = newEdge(e.v, e.w, null);
        copy.insert(cloneEdge);
        l = l.next();
      }
    }
    return copy;
  }


  protected CollapsableGraphAdjList_EdgeLists newGraph(int size, boolean isDigraph) {
    return new CollapsableGraphAdjList_EdgeLists(size, isDigraph);
  }

  @Override
  public CollapsableEdge newEdge(int i, int j, CloneableData d) {
    return new CollapsableEdge(i, j, edgeStamp.incrementAndGet(), d);
  }

  protected Edge newSimpleEdge(int v, int w) {
    return new Edge(v, w);
  }

  protected boolean removeEdge(Edge e, List<CollapsableEdge> edges) {
    return edges.remove(e);
  }


  protected void removeAllEdges(Edge e, List<CollapsableEdge> edges) {
    // stupid linear search through all the edges !
    //   do something better !
    Iterator it = edges.iterator();
    while (it.hasNext()) {
      Edge le = (Edge) it.next();
      if (le.v == e.v) {
        if (le.w == e.w) {
          it.remove();
        }
      }
    }
  }

  protected boolean checkEdgeCounts(List<CollapsableEdge> edges) {
    if (edges.size() == eCount()) return true;
    if (isP()) {
      log.debug("\n\n\n\n");
      log.debug("    ERROR !  edges has " + edges.size() + ", but graph has " + eCount() + "\n\n\n");
    }
    return false;
  }

  public class CollapseResult {
    List<CollapsableEdge> removed;
    List<CollapsableEdge> added;

    public CollapseResult() {
      removed = new ArrayList<CollapsableEdge>();
      added = new ArrayList<CollapsableEdge>();
    }

    public void init() {
      removed.clear();
      added.clear();
    }

    public List<CollapsableEdge> getRemoved() {
      return removed;
    }

    public List<CollapsableEdge> getAdded() {
      return added;
    }

    public void addToRemoved(CollapsableEdge e) {
      removed.add(e);
    }

    public void addToAdded(CollapsableEdge e) {
      added.add(e);
    }
  }


  /**
   * Of the input list of lists find the one with the least number of edges
   *
   * @param in
   * @return
   */
  public List<CollapsableEdge> getMinList(List<List<CollapsableEdge>> in) {
    List<CollapsableEdge> minList = null;
    int minCount = Integer.MAX_VALUE;
    for (List<CollapsableEdge> list : in) {
      int count = countList(list);
      if (count < minCount) {
        minCount = count;
        minList = list;
      }
    }
    return minList;
  }


  /**
   * count total edges represented in the list
   *
   * the total number of edges is ONLY the number of edges in the list
   * @param list
   * @return
   */
  public int countList(List<CollapsableEdge> list) {
    return list.size();
   }


  /**
   * count total edges represented in list
   * if an edge has no 'original edges' then it counts as 1
   * else
   * the number of original edges is the number of edges
   *
   * @param list
   * @return
   */
  public int countList_bad(List<CollapsableEdge> list) {
    HashSet hs = new HashSet();
    int edgeCount = 0;
    for (CollapsableEdge ce : list) {
      List<CollapsableEdge> oel = ce.getOriginalEdges();
      if (oel == null) {
        String s = makeVW(ce);
        if (hs.add(s))
          edgeCount++;
      } else {
        for (CollapsableEdge e : oel) {
          String s = makeVW(e);
          if (hs.add(s)) {
            edgeCount++;
          }
        }
      }
    }
    return edgeCount;
  }

  private String makeVW(CollapsableEdge e) {
    return e.v + "-" + e.w;
  }

}

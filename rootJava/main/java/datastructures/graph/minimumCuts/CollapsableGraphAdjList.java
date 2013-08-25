package datastructures.graph.minimumCuts;

import datastructures.graph.basic.*;


import org.apache.log4j.Logger;


import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/24/13
 * Time: 7:33 PM
 *
 * OLD incorrect version:
 *   we don't want remove(Edge e)
 *     to remove ALL parallel edges, remove only the indicated edge
 *
 */
public class CollapsableGraphAdjList extends GraphAdjList {

  private Logger log =
          Logger.getLogger(CollapsableGraphAdjList.class);


  // non-ThreadSafe reusable collapseResult
  private CollapseResult collapseResult;

  public CollapsableGraphAdjList(int size, boolean isDigraph) {
    super(size, isDigraph);
    collapseResult = new CollapseResult();
  }


  public synchronized void removeSingle(CollapsableEdge_00 e) {
    removeInternal(e, false);
  }

  public synchronized void removeAll(CollapsableEdge_00 e) {

    removeInternal(e, true);
  }
  //
  //  we don't want remove to remove all parallel edges !
  //
  public synchronized void removeInternal(CollapsableEdge_00 e, boolean all) {
    LinkedNode n = adj[e.v];
    if (n == null)  return;

    String m = "remove: ";
    if (isP())
       log.debug(m+"tail="+e.v+", head="+e.w+". start nodelist: "+printNodeList(n));

    LinkedNode tempRoot = new LinkedNodeImpl(e.v, new Edge(e.v, -1), n);

    if (isP()) {
      log.debug(m+"created tempRoot next = nodelist above "+printNodeList(tempRoot));
          LinkedNode tn = tempRoot.next();
      log.debug(m+"tempRoot.next yields "+printNodeList(tn));
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
    while(prev != null) {
    //while (prev.hasNext()) {
      n = prev.next();
      if (n != null) {
        // there can be parallel edges, get them all
        CollapsableEdge_00 edge = (CollapsableEdge_00) n.edge();



        if (n.vertexHeadNumber() == e.w) {
          if (!all) {

            // if we are not removing ALL parallel edges
            // then we must check to see that we are removing
            //   ONLY the edge instance specified

            //  todo:  edge.stamp  would need to be fixed, but this is class is an abandoned approach to the this problem

            //if (edge.stamp == e.stamp) {
              eCount--;
              prev.setNext(n.next()); // can be null
            //}
          }
          else {
            // remove ALL parallel edges between v and w
            //  indiscriminatn
            eCount--;
            prev.setNext(n.next()); // can be null
          }
        }
      }
      prev = prev.next();
    }
    adj[e.v] = tempRoot.next();   // can be null
    if (adj[e.v] == null)  { vCount--; }

    if (isP())
      log.debug(m+"tail="+e.v+", head="+e.w+". ending nodelist: "+printNodeList(adj[e.v])+"\n");
  }

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
   *
   */
  public CollapseResult collapseVertex(int collapseFrom, int collapseTo) {
    collapseResult.init();

    LinkedNode nFrom = adj[collapseFrom];
    if (nFrom != null) {
      LinkedNode nTo = adj[collapseTo];
      if (nTo != null) {

        String m = "collapseEdge " + collapseFrom + "-into-" + collapseTo + ": ";

        //
        // remove all self edges that will be created as a consequence of the collapse
        // note that if the self edge is at the head of the edge list
        // then we will have a *new* head of the edge list so we'll have to
        // re-get the head of the list afterwards
        //
        CollapsableEdge_00 edge = newEdge(collapseFrom, collapseTo, null);
        //collapseResult.addToRemoved(edge);
        removeAll(edge);

        if (isP())
          log.debug(m + "after removal of edge " + collapseFrom + "-" + collapseTo + " nodeList of " + collapseFrom + " is " +
                  printNodeList(adj[collapseFrom]) + "\n");


        edge = newEdge(collapseTo, collapseFrom, null);
        collapseResult.addToRemoved(edge);
        removeAll(edge);

        if (isP())
          log.debug(m + "after removal of edge " + collapseTo + "-" + collapseFrom + " nodeList of " + collapseTo + " is " +
                  printNodeList(adj[collapseTo]) + "\n");

        // reget roots as they may have been deleted during previous 'removes'
        nFrom = adj[collapseFrom];
        nTo = adj[collapseTo];


        if (isP()) {
          log.debug(m + " after reset roots " + collapseFrom + " is " + printNodeList(nFrom));
          log.debug(m + " after reset roots " + collapseTo + " is " + printNodeList(nTo) + "\n");
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
            log.debug(m + "processing currNode edge " + currNode.edge() + " backEdges from " + currHeadNumber + "-" + collapseFrom);
          }
          //
          // BACK EDGES

          // now deal with the to be replaced vertex/edge back edges [currHeadNumber, collapseFrom]
          // replace them with [currHeadNumber, collapseTo]
          // and if there is no saved 'original' edge, then save that now

          List<Edge> backList = getEdges(currHeadNumber, collapseFrom);

          for (Edge backEdge : backList) {
            CollapsableEdge_00 cBackEdge = (CollapsableEdge_00) backEdge;    // sorry about the cast

            // remove the old back edge connecting the old vertices
            if (isP()) {
              log.debug(m + " remove backedge " + cBackEdge);
            }

            CollapsableEdge_00 copy = cBackEdge.clone();
            collapseResult.addToRemoved(cBackEdge);
            removeSingle(cBackEdge);


            cBackEdge.shrink(currHeadNumber, currHeadNumber, collapseFrom, collapseTo);

            // re-insert the shrunken back edge to the new vertices
            if (isP()) {
              log.debug(m + " insert new backedge " + cBackEdge);
            }
            collapseResult.addToAdded(cBackEdge);
            insert(cBackEdge);

            if (isP()) {
              log.debug(m + " after insert new vertex list " + printNodeList(adj[currHeadNumber]));
            }

          }

          // FORWARD EDGES
          //
          // all forward edges have to have their tails readjusted so
          // that they point FROM the new collapseTo vertex
          //
          List<Edge> forwardList = getEdges(collapseFrom, currHeadNumber);

          if (isP()) {
            log.debug(m + " process all forward edges  " + collapseFrom + "-" + currHeadNumber+
            "  "+printEdgeList(forwardList));
          }
          for (Edge forwardEdge : forwardList) {
            CollapsableEdge_00 cForwardEdge = (CollapsableEdge_00) forwardEdge;

            // remove the old forward edge connecting the old vertices
            // remove the old back edge connecting the old vertices
            if (isP()) {
              log.debug(m + " remove forward edge " + cForwardEdge);
            }
            CollapsableEdge_00 copy = cForwardEdge.clone();
            collapseResult.addToRemoved(copy);
            removeSingle(cForwardEdge);

            // shrink the forward edge from the old vertices
            cForwardEdge.shrink(collapseFrom, collapseTo, currHeadNumber, currHeadNumber);

            // re-insert the shrunken forward edge to the new vertices
            if (isP()) {
              log.debug(m + " insert new forwardedge " + cForwardEdge);
            }
            collapseResult.addToRemoved(cForwardEdge);
            insert(cForwardEdge);

            if (isP()) {
              log.debug(m + " after insert new vertex list " + printNodeList(adj[collapseTo]));
            }

          }
          if (isP()) {
            log.debug(m + " after process all forward edges  " + collapseFrom + "-" + currHeadNumber +
                    " edges at " + collapseFrom + ": " + printNodeList(adj[collapseFrom]));
          }

          // on to the next vertex from the old-tail to some head
          currNode = currNode.next();
        }


      } else {
        throw new RuntimeException("Error !  null collapseTo node=" + collapseTo);
      }
    } else {
      throw new RuntimeException("Error !  null collapseFrom node=" + collapseFrom);
    }
    return collapseResult;
  }

  public synchronized CollapsableGraphAdjList copy() {
    int size = adj.length;
      CollapsableGraphAdjList copy = newGraph(size, isDigraph);
      // Clone all vertex entries
      //  Edges must be copies because someone might
      //  change the Edge data in the new Graph
      //
      for (int i=0; i<size; i++) {
        LinkedNode l = adj[i];
        while (l != null) {
          CollapsableEdge_00 e = (CollapsableEdge_00)l.edge();

          // todo: write the clone to allow cloning of edge data
          //Edge cloneEdge = e.clone();
          CollapsableEdge_00 cloneEdge = newEdge(e.v, e.w, null);
          copy.insert(cloneEdge);
          l = l.next();
        }
      }
      return copy;
  }


  protected CollapsableGraphAdjList newGraph(int size, boolean isDigraph) {
    return new CollapsableGraphAdjList(size, isDigraph);
  }

  @Override
  protected CollapsableEdge_00 newEdge(int i, int j, CloneableData d) {
    return new CollapsableEdge_00(i, j, d);
  }

  public class CollapseResult {
    List<CollapsableEdge_00> removed;
    List<CollapsableEdge_00> added;

    public CollapseResult() {
      removed = new ArrayList<CollapsableEdge_00>();
      added = new ArrayList<CollapsableEdge_00>();
    }

    public void init() {
      removed.clear();
      added.clear();
    }

    public List<CollapsableEdge_00> getRemoved() {
      return removed;
    }

    public List<CollapsableEdge_00> getAdded() {
         return added;
    }

    public void addToRemoved(CollapsableEdge_00 e) {
      removed.add(e);
    }

    public void addToAdded(CollapsableEdge_00 e) {
      added.add(e);
    }
  }

}

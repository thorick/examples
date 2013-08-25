package datastructures.graph.basic;

import java.util.LinkedList;
import java.util.List;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: thorick chow
 * Date: 6/11/13
 * Time: 7:18 PM
 *
 * Adjacency List impl of Graph ADT
 * mostly per Sedgewick
 *
 * enhanced to allow parallel edges
 *
 * Implementation note:
 *   we allow sparse graphs with empty vertex slots
 *   therefore we take the actual size of the graph
 *   to be indicated by the highest numbered
 *   vertex that is ever has an edge inserted.
 *
 *
 *
 *
 */
public class GraphAdjList implements Graph {

  private Logger log =
          Logger.getLogger(GraphAdjList.class);

  protected int highestVertexNumber = -1;

  protected int sizeLimit;
  protected int vCount;
  protected int eCount;
  protected boolean isDigraph = false;
  protected LinkedNode[] adj;

  public GraphAdjList(int sizeLimit, boolean isDigraph) {
    this.sizeLimit = sizeLimit;
    this.isDigraph = isDigraph;
    adj = new LinkedNodeImpl[sizeLimit];
  }

  public int vCount() {
    return vCount;
  }

  public int eCount() {
    return eCount;
  }

  public boolean isDigraph() {
    return isDigraph;
  }


  public int getHighestVertexNumber() { return highestVertexNumber; }

  public synchronized int insert(Edge e) {
    int v = e.v;
    int w = e.w;


    // track the highest vertex number that the graph has
    if (v > highestVertexNumber)  highestVertexNumber = v;
    if (w > highestVertexNumber)  highestVertexNumber = w;


    //   created subclass GraphAdjList_Kosaraju_SCC
    //  specifically to handle this as it's
    //  probably not generally a good idea right now
    //
    // this way of counting vertices is required for
    //    SCC_Stanford_Coursera
    //  probably to do with being able to specify a
    //  vertex that has NO edges

    //if ((v+1) > vCount) { vCount = v + 1; }
    //if ((w+1) > vCount) { vCount = w + 1; }


    // restored:  this way of doing it is NOT in
    //   GraphAdjList_Kosaraju_SCC
    //
    if (adj[v] == null)  {
      vCount++;
    }


    // handle registered vertex with no edge.
    // insert a self-edge if there is not already one


    // link new node into the head of the list
    adj[v] = new LinkedNodeImpl(v, e, adj[v]);
    return ++eCount;
  }

  public synchronized List<Edge> getEdges(int v, int w) {
    List<Edge> retVal = new LinkedList<Edge>();
    if (v > sizeLimit || w > sizeLimit) {
      log.debug("Warning !  received request for edge("+v+", "+w+") with a vertex "+
        "that exceeds this graphs preset vertex limit="+vCount);

      return retVal;
    }

    LinkedNode n = adj[v];
    if (n == null)  return retVal;

    //List<Edge> retVal = new LinkedList<Edge>();
    while (n != null) {
    //while (n.hasNext()) {
    //  n = n.next();
      if (n.vertexHeadNumber() == w) {
        retVal.add(n.edge());
      }
      n = n.next();
    }
    return retVal;
  }

  public synchronized List<Edge> getEdges() {
    List<Edge> retVal = new LinkedList<Edge>();
    for (int i=0; i < vCount; i++) {
      LinkedNode n = adj[i];
      while (n != null) {
        retVal.add(n.edge());
        n = n.next();
      }
    }
    return retVal;
  }

  public synchronized  boolean edge(int v, int w) {
    return ((getEdges(v, w) != null) && (!getEdges(v, w).isEmpty()));
  }

  public synchronized void remove(Edge e) {
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

    // now look for one matching edge and remove it
    // any parallel edge remains
    //
    LinkedNode prev = tempRoot;
    boolean gotIt = false;
    while (!gotIt && (prev != null)) {
    //while (prev.hasNext()) {
      n = prev.next();
      if (n != null) {
        if (n.vertexHeadNumber() == e.w) {
          gotIt = true;
          eCount--;
          prev.setNext(n.next()); // can be null
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
   * self edges are ignored
   *
   * @param in
   */
  public synchronized void loadEdges(int[] in) {
    if (in == null || in.length <= 0) return;

    int v = in[0];

    /*
    // no edge
    if (in.length == 1) {
      Edge e = newEdge(v, v, null);
      insert(e);
      return;
    }
    */

    for (int j=1; j<in.length; j++) {
      Edge e = newEdge(v, in[j], null);
      eCount++;
      insert(e);
    }
  }


  public synchronized GraphAdjList reverse() {
    if (!isDigraph) {
      return this;
    }
    GraphAdjList rev = newGraph(sizeLimit, isDigraph);
      for (int i=0 ; i < adj.length ; i++) {
        LinkedNode l = adj[i];
        while (l != null) {

          // no the edge need to be reversed !
          Edge e = l.edge();

          log.debug("process edge "+e.toString());

          // lose any edge data on reversal
          Edge newEdge = new Edge(e.w, e.v, null);

          log.debug("insert into reverse graph new edge "+newEdge.toString());

          rev.insert(newEdge);
          l = l.next();
      }
    }
    return rev;
  }

  public synchronized  GraphAdjList copy() {
    int size = adj.length;
    GraphAdjList copy = newGraph(size, isDigraph);
    //GraphAdjList copy = new GraphAdjList(size, isDigraph);
    // Clone all vertex entries
    //  Edges must be copies because someone might
    //  change the Edge data in the new Graph
    //
    for (int i=0; i<size; i++) {
      LinkedNode l = adj[i];
      while (l != null) {
        Edge e = l.edge();
        Edge cloneEdge = e.clone();
        copy.insert(cloneEdge);
      }
    }
    return copy;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<adj.length; i++) {
      LinkedNode n = adj[i];
      if (n != null)  {
        sb.append("v="+i+": edges: ");
        while (n != null) {
          sb.append(n.edge().toString()).append(", ");
          //sb.append(n.edge().w).append(", ");
          n = n.next();
        }
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  public LinkedNode getAdjList(int v) {
    return adj[v];
  }

  protected GraphAdjList newGraph(int size, boolean isDigraph) {
    return new GraphAdjList(size, isDigraph);
  }

  protected Edge newEdge(int i, int j, CloneableData cloneableData) {
    return new Edge(i, j, cloneableData);
  }

  protected String printNodeList(LinkedNode n) {
    if (n == null) return "NULL";
    StringBuilder sb = new StringBuilder();

    //sb.append("vertex list for "+n.edge().v+": ");
    sb.append("vertex list for "+n.vertexTailNumber()+": ");
    sb.append(n.vertexHeadNumber()).append(", ");
    while (n.hasNext()) {
      n = n.next();
      sb.append(n.vertexHeadNumber()).append(", ");
    }
    return sb.toString();
  }


  protected String printEdgeList(List<Edge> e) {
    if (e==null) return "";
    StringBuilder sb = new StringBuilder();

    sb.append("edge list: ");
    for (Edge edge: e) {
      sb.append(e.toString()).append(", ");
    }
    return sb.toString();
  }


  protected boolean isP() {
    return log.isDebugEnabled();
  }

}

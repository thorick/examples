package algorithms.twoSAT.KosarajuSCC;


import datastructures.graph.basic.*;
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
 *
 *
 */
public class GraphAdjList_Kosaraju_SCC_vertexLists extends
        GraphAdjList implements Graph {

  private Logger log =
          Logger.getLogger(GraphAdjList_Kosaraju_SCC_vertexLists.class);


  public GraphAdjList_Kosaraju_SCC_vertexLists(int sizeLimit, boolean isDigraph) {
    super(sizeLimit, isDigraph);
  }


  /**
   * WARNING:  This is an insert method specific to this impl
   *
   * the vertex count here is the highest numbered vertex + 1.
   *
   * so in a sparse graph with missing vertices the vCount is
   *  can be less than the actual number of vertices present
   *  in the graph.
   *
   *  Currently the DFS routines run off the vCount as the vertex number boundary
   *  this is a mistake and needs to be fixed, so that vCount can remain really
   *  being the count of the number of vertices in the graph.
   *
   *
   *
   * @param e
   * @return
   */
  public synchronized int insert(Edge e) {
    int v = e.v;
    int w = e.w;

    // track the highest vertex number that the graph has
    if (v > highestVertexNumber)  highestVertexNumber = v;
    if (w > highestVertexNumber)  highestVertexNumber = w;

    if (adj[v] == null)  {
      vCount++;
    }

    // link new node into the head of the list
    adj[v] = new LinkedNodeImpl(v, e, adj[v]);
    return ++eCount;
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

    // now look for one matching edge and remove it
    // any parallel edge remains
    //
    LinkedNode prev = tempRoot;
    boolean gotIt = false;
    while (!gotIt && (prev != null)) {
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

    for (int j=1; j<in.length; j++) {
      Edge e = newEdge(v, in[j], null);
      eCount++;
      insert(e);
    }
  }


  public synchronized GraphAdjList_Kosaraju_SCC_vertexLists reverse() {
    if (!isDigraph) {
      return this;
    }
    GraphAdjList_Kosaraju_SCC_vertexLists rev = newGraph(sizeLimit, isDigraph);
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

  public synchronized GraphAdjList_Kosaraju_SCC_vertexLists copy() {
    int size = adj.length;
    GraphAdjList_Kosaraju_SCC_vertexLists copy = newGraph(size, isDigraph);

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

  protected GraphAdjList_Kosaraju_SCC_vertexLists newGraph(int size, boolean isDigraph) {
    return new GraphAdjList_Kosaraju_SCC_vertexLists(size, isDigraph);
  }

  protected Edge newEdge(int i, int j, CloneableData cloneableData) {
    return new Edge(i, j, cloneableData);
  }

  protected boolean isP() {
    return log.isDebugEnabled();
  }

}

package graphsTrees.strongComponents.tarjan;

/**
 * Created with IntelliJ IDEA.
 * User: thorick chow
 * Date: 6/11/13
 * Time: 7:18 PM
 *
 * Adjacency List impl of Graph ADT
 * mostly per Sedgewick
 *
 */
public class GraphAdjList implements Graph {

  private int vCount;
  private int eCount;
  private boolean isDigraph = false;
  private Node[] adj;

  GraphAdjList(int vCount, boolean isDigraph) {
    this.vCount = vCount;
    this.isDigraph = isDigraph;
    adj = new Node[vCount];
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

  public int insert(Edge e) {
    int v = e.v;
    int w = e.w;
    adj[v] = new Node(w, adj[v]);
    return ++eCount;
  }

  public boolean edge(int v, int w) {
    if (v > vCount || w > vCount) {
      p("Warning !  received request for edge("+v+", "+w+") with a vertex "+
        "that exceeds this graphs preset vertex limit="+vCount);

      return false;
    }
    if (v == w)  return true;   // always a self edge

    Node n = adj[v];
    if (n == null)  return false;

    while (n.next != null) {
      n = n.next;
      if (n.v == w) return true;
    }
    return false;
  }

  public void remove(Edge e) {
    Node n = adj[e.v];
    if (n == null)  return;

    // now look for edge and remove it
    Node prev = n;
    while (prev.next != null) {
      n = prev.next;
      if (n.v == e.w) {
        prev.next = n.next; // can be null
        return;
      }
      prev = prev.next;
    }
  }

  public AdjList getAdjList(int v) {
    return new AdjLinkedList(v);
  }

  private void p(String s) {
    System.err.println("GraphAdjList: "+s);
  }

  private class Node{
    int v;       // vertex number
    Node next;

    Node(int v, Node next)  {
      this.v = v;
      this.next = next;
    }
  }

  private class AdjLinkedList implements AdjList {
    private int v;
    private Node n;

    AdjLinkedList(int v) {
      this.v = v;   // list for this vertex 'v'
      n = null;
    }

    public int head() {
      n = adj[v];   // start of list for this vertex 'v'
      return n == null ? -1 : n.v;
    }

    public int next() {
      if (n != null)  n = n.next;
      return n == null ?  -1 : n.v;
    }

    public boolean isEnd() {
      return n == null;
    }

  }
}

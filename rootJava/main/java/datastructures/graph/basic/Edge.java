package datastructures.graph.basic;

/**
 * Created with IntelliJ IDEA.
 * User: thorick chow
 * Date: 6/11/13
 * Time: 7:10 PM
 *
 * Graph Edge impl
 *
 *    extended from Sedgewick  to include more real-world stuff (like edge data)
 *
 */
public class Edge implements Cloneable {
  public int v;
  public int w;

  // for now the data is stored in the Edge
  // this isn't great when data is not unique per edge
  // in that case there'd be some kind of pointer
  // referencing other storage

  // if you don't need edge data and the graph
  // contains many edges, you might want to get rid
  // of the unused data reference for the space that it
  // uses up.

  protected CloneableData data;

  public Edge(int v, int w) {
    this(v, w, null);
  }

  public Edge(int v, int w, CloneableData data) {
    this.v = v;
    this.w = w;
    this.data = data;
  }

  public Object getData() {
    return data;
  }

  public Edge clone() {
    CloneableData newData = null;
    if (newData != null) {
      try {
        newData = data.clone();
      } catch (CloneNotSupportedException e) {}
    }
    return new Edge(v, w, newData);
  }

  @Override
  public String toString() {
    return "Edge: "+v+"-"+w+(data != null ? (", data="+data.toString()) : (""));
  }
}

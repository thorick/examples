package datastructures.graph.KCluster;

import datastructures.graph.basic.Edge;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 10/17/13
 * Time: 4:18 PM
 * <p/>
 * Limited Support Graph
 * <p/>
 * - no parallel edges in the same direction:  dup inserts ignored
 */
public class LightGraphImpl implements LightGraph {


  protected int sizeLimit;
  protected VertexAdjList[] vertices;
  protected int vCount;
  protected int eCount;
  protected int highestVertexNumber;


  public LightGraphImpl(int sizeLimit) {
    this.sizeLimit = sizeLimit;
    vertices = new VertexAdjList[sizeLimit];

  }

  /**
   * count of vertices in the graph
   *
   * count will be off.  will not include isolated vertices with no edges.
   *
   * @return
   */
  public int vCount() {
    return vCount;
  }

  /**
   * count of edges in the graph
   *
   * @return
   */
  public int eCount() {
    return eCount;
  }

  //boolean isDigraph()

  public int insert(Edge e) {
    VertexAdjList val = vertices[e.v];
    if (val == null) {
      val = new VertexAdjList();
      vertices[e.v] = val;
      vCount++;
    }
    val.addEdge(e);
    return eCount;
  }

  /**
   * remove ALL edges whose start and end vertices match that
   * of the input Edge.
   * <p/>
   * All parallel edges will be deleted.
   *
   * @param e
   */
  public void remove(Edge e) {
    throw new RuntimeException("NYI");
  }

  /**
   * List of Edges between tail v  and head w
   * Parallel edges allowed so return a list
   * <p/>
   * Self-edges are ignored and not returned !
   *
   * @param v
   * @param w
   * @return
   */
  public List<Edge> getEdges(int v, int w) {
    throw new RuntimeException("NYI");
  }

  public boolean edge(int v, int w) {
    VertexAdjList val = vertices[v];
    if (val == null)  return false;

    return val.edge(w);
  }

  /**
   * can be null
   * valid entries until the first -1
   *
   * @param v
   * @return
   */
  public int[] getAdjArray(int v) {
    return vertices[v].getEdgeVertexList();
  }

  private class VertexAdjList {

    int[] vertexAdjList;    // all invalid entries = -1;
    int lastEntry = -1;
    int allocChunkSize = 10;
    int lastAllocIndex = allocChunkSize - 1;

    private VertexAdjList() {
      vertexAdjList = new int[allocChunkSize];
      for (int i=0; i<vertexAdjList.length; i++) {
        vertexAdjList[i] = -1;
      }
    }

    protected void addEdge(Edge e) {
      // assume sparse, linear scan for now
      int head = e.w;
      if (lastEntry >= 0) {
        for (int i = 0; i <= lastEntry; i++) {
          if (vertexAdjList[i] == head)
            return;    // do not insert duplicate edges
        }
      }

      if (lastEntry >= lastAllocIndex) {
        int newLastIndex = lastAllocIndex + allocChunkSize;
        int[] newV = new int[newLastIndex + 1];
        System.arraycopy(vertexAdjList, 0, newV, 0, lastAllocIndex + 1);
        vertexAdjList = newV;
        for (int i=(lastAllocIndex+1); i<vertexAdjList.length; i++) {
          vertexAdjList[i] = -1;
        }
        lastAllocIndex = newLastIndex;
      }
      lastEntry++;
      vertexAdjList[lastEntry] = head;
      eCount++;
    }

    protected boolean edge(int w) {
      if (lastEntry >= 0) {
        for (int i = 0; i <= lastEntry; i++) {
          if (vertexAdjList[i] == w)
            return true;    // do not insert duplicate edges
        }
      }
      return false;
    }

    protected int[] getEdgeVertexList() {
      return vertexAdjList;
    }

  }
}

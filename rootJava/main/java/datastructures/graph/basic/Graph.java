package datastructures.graph.basic;

import datastructures.graph.basic.AdjList;
import datastructures.graph.basic.Edge;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: thorick chow
 * Date: 6/11/13
 * Time: 7:05 PM
 *
 * Graph interface  extended from  Sedgewick
 *
 */
public interface Graph {

  /**
   * count of vertices in the graph
   *
   * @return
   */
  int vCount();

  /**
   * count of edges in the graph
   *
   * @return
   */
  int eCount();

  boolean isDigraph();

  int insert(Edge e);

  /**
   * remove ALL edges whose start and end vertices match that
   * of the input Edge.
   *
   * All parallel edges will be deleted.
   *
   * @param e
   */
  void remove(Edge e);

  /**
   * List of Edges between tail v  and head w
   * Parallel edges allowed so return a list
   *
   * Self-edges are ignored and not returned !
   *
   * @param v
   * @param w
   * @return
   */
  List<Edge> getEdges(int v, int w);

  boolean edge(int v, int w);

  LinkedNode getAdjList(int node);

  /**
   * convenience method to edges of a tail vertex
   *
   * 0th element is the source vertex
   * following elements are the connected head dest vertices
   *
   * @param in
   */
  void loadEdges(int[] in);


  /**
   * produce the reverse of the current graph
   *
   * all vertices in the reverse graph are unmarked
   *
   * for now.  a reversed graph will lose any edge data
   * stored in the edge as in general the meaning
   * a reversed payload is undefined.
   *
   * to take care of this, some kind of an impl
   * dependent plugin API could be added.
   *
   * if this is not a digraph
   * then you get back this graph and NOT a copy
   *
   * if you want a separate copy of this graph
   * then use the copy() method
   *
   * @return
   */
  Graph reverse();

  /**
   * convenience method to return a copy of this Graph
   *
   * all vertices in the copied graph are unmarked.
   *
   * @return
   */
  Graph copy();
}

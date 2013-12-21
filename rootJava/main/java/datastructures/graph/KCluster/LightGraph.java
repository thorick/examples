package datastructures.graph.KCluster;

import datastructures.graph.basic.Edge;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 10/17/13
 * Time: 4:15 PM
 *
 * Intended for lightweight graph that does not need a lot of features
 *
 */
public interface LightGraph {

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

}

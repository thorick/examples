package graphsTrees.strongComponents.tarjan;

/**
 * Created with IntelliJ IDEA.
 * User: thorick chow
 * Date: 6/11/13
 * Time: 7:05 PM
 *
 * Graph interface  per  Sedgewick
 *
 */
public interface Graph {

  int vCount();

  int eCount();

  boolean isDigraph();

  int insert(Edge e);

  void remove(Edge e);

  boolean edge(int v, int w);

  AdjList getAdjList(int node);

}

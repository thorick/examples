package graphsTrees.strongComponents.tarjan;

/**
 * Created with IntelliJ IDEA.
 * User: thorick chow
 * Date: 6/11/13
 * Time: 7:10 PM
 *
 * Graph Edge impl
 *    per Sedgewick
 *
 */
public class Edge {
  public int v;
  public int w;

  Edge(int v, int w) {
    this.v = v;
    this.w = w;
  }
}

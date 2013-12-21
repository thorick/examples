package datastructures.graph.allPairsShortestPaths;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/30/13
 * Time: 9:07 PM
 */
public interface BellmanFord {

  public int compute(int startVertex);

  public int shortestDistanceTo(int v);

  public int[] shortestDistanceToArray();

  public String printPathTo(int v);

  public boolean hasNegativeCycle();
}

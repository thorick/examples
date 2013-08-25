package datastructures.graph.shortestPath;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/8/13
 * Time: 8:15 AM
 * *
 * Container to hold Vertex Distance Value
 * <p/>
 * Intended for use with Heap based vertex selection
 * <p/>
 * These Objects are contained both in the Heap and in the Dijkstra tracking arrays
 */
public class DijkstraVertex implements Comparable<DijkstraVertex> {

  final int vertex;
  int value;
  boolean unExplored;
  int heapArrayPosition;

  DijkstraVertex(int vertex, int value) {
    this.vertex = vertex;
    this.value = value;
    unExplored = true;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int v) {
    value = v;
  }

  public boolean isUnExplored() {
    return unExplored;
  }

  public void setUnExplored(boolean b) {
    unExplored = b;
  }

  public int getHeapArrayPosition() {
    return heapArrayPosition;
  }

  public void setHeapArrayPosition(int i) {
    heapArrayPosition = i;
  }

  public int compareTo(DijkstraVertex other) {
    if (value == other.getValue()) return 0;
    if (value < other.getValue()) return -1;
    if (value > other.getValue()) return 1;
    return 0;   // never
  }

  @Override
  public String toString() {
    return "vertex(" + vertex + ")=" + value + ", unExplored=" + unExplored + ", HeapPos=" + heapArrayPosition;
  }
}

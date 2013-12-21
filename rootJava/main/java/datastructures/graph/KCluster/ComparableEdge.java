package datastructures.graph.KCluster;

import datastructures.graph.basic.CloneableData;
import datastructures.graph.basic.CloneableInteger;
import datastructures.graph.basic.Edge;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/16/13
 * Time: 12:48 PM
 */
public class ComparableEdge extends Edge
  implements Comparable<ComparableEdge> {

  public ComparableEdge(int v, int w, CloneableInteger weight) {
    super(v, w, weight);
  }

  //
  //  no parallel edges
  //  edge is defined by vertices u, v only
  //
  public String getKey() {
    return createKey(v, w);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ComparableEdge)) return false;

    ComparableEdge o = (ComparableEdge) other;
    if (o.v != v) return false;
    if (o.w != w) return false;
    if (compareTo(o) != 0)  return false;
    return true;
  }


  @Override
  public Edge clone() {
    CloneableData newData = null;
    //if (newData != null) {
    if (data != null) {
      try {
        newData = data.clone();
      } catch (CloneNotSupportedException e) {}
    }
    return new ComparableEdge(v, w, (CloneableInteger)newData);
  }

  /**
   * We want to sort the edges in increasing order so it's normal
   * sort ordering.
   *
   * @param o
   * @return
   */
  @Override
  public int compareTo(ComparableEdge o) {
    CloneableInteger thisW = (CloneableInteger) data;
    CloneableInteger otherW = (CloneableInteger) o.data;

    if (thisW.intValue() < otherW.intValue())  return -1;
    if (thisW.intValue() > otherW.intValue())  return 1;
    return 0;
  }

  public static String createKey(int v, int w) {
    return v + ":" + w;
  }
}

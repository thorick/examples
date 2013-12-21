package datastructures.MST.prims_ArrayHeap;

import datastructures.graph.basic.CloneableInteger;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/6/13
 * Time: 5:49 AM
 */
public class PrimsVertex implements Comparable<PrimsVertex> {

  public int thisVertex;
  public int otherVertex;     // only has meaning when PrimsVertex in VertexHeap
  public CloneableInteger weight;     // only has meaning when PrimsVertex in VertexHeap
  public int  heapArrayPosition;



  public PrimsVertex(int thisVertex, int otherVertex, CloneableInteger weight) {
    this.weight = weight;
    this.thisVertex = thisVertex;
    this.otherVertex = otherVertex;
  }

  public int compareTo(PrimsVertex other) {
    if ((weight == null) || other.weight == null)  return 0;
    if (weight.intValue() < other.weight.intValue()) return -1;
    if (weight.intValue() > other.weight.intValue()) return 1;
    return 0;
  }

  public Integer getVertexI() {
    return thisVertex;
  }
  /*
  public void setOtherVertex(PrimsVertex other) {
    otherVertex = other;
  }
  */
  public int getOtherVertex() {
    return otherVertex;
  }

  public Integer getOtherVertexI() {
    return otherVertex;
  }

  public void setOtherVertex(int i) {
    otherVertex = i;
  }

  public void setOtherVertexI(Integer I) {
    otherVertex = I;
  }

  public void setWeight(CloneableInteger w) {
    weight = w;
  }

  public CloneableInteger getWeight() {
    return weight;
  }

  public int getWeightAsInt() {
    return weight.intValue();
  }

  public int getHeapArrayPosition() {
    return heapArrayPosition;
  }

  public void setHeapArrayPosition(int i) {
    heapArrayPosition = i;
  }

  @Override
  public String toString() {
    return thisVertex+"-"+otherVertex+": "+ weight + ", index=" + heapArrayPosition;
  }
}

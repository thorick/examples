package datastructures.graph.basic;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/24/13
 * Time: 7:57 AM
 */
public class LinkedNodeImpl implements LinkedNode {
  final int v;       // tail vertex number
  final int w;       // head vertex number
  final Edge e;
  LinkedNode next;
  boolean marked;    // useful for graph search marking

  public LinkedNodeImpl(int v, Edge e, LinkedNode next)  {
    this.v = v;
    this.e = e;
    this.w = e.w;
    this.next = next;
  }

  public LinkedNode next() {
    return this.next;
  }

  public void setNext(LinkedNode n) {
    next = n;
  }

  public boolean hasNext() {
    return (next != null);
  }

  public int vertexHeadNumber() {
    return w;
  }

  public int vertexTailNumber() {
    return v;
  }

  public void mark(boolean marked) {
    this.marked = marked;
  }

  public boolean isMarked() {
    return marked;
  }

  public Edge edge() {
    return e;
  }

  public int length() {
    int len = 1;
    LinkedNode n = next();
    while (n != null) {
      len++;
      n = n.next();
    }
    return len;
  }

  public String printNodeChain() {
    StringBuilder sb = new StringBuilder();
    LinkedNode n = this;
    while (n != null)  {
      sb.append(n.vertexTailNumber()+"-"+n.vertexHeadNumber()).append(", ");
      n = n.next();
    }
    return sb.toString();
  }
}

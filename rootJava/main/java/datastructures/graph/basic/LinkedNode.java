package datastructures.graph.basic;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/24/13
 * Time: 7:54 AM
 *
 * LinkedNode is used in the Adjacency Lists Graph representation
 * It is a LinkedNode in the chain of vertices that are all connected
 * to a specific vertex.
 *
 *
 */
public interface LinkedNode {

  boolean isMarked();

  void mark(boolean marked);

  void setNext(LinkedNode next);

  LinkedNode next();

  boolean hasNext();

  int vertexHeadNumber();

  int vertexTailNumber();

  Edge edge();


  /**
   * How long is this chain including self
   *
   * @return
   */
  int length();

  //void removeNode(int i);

  /**
   * print this and all forward nodes
   *
   * @return
   */
  String printNodeChain();

}

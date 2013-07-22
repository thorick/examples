package datastructures.BST;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/4/13
 * Time: 9:55 AM
 *
 * Basic interface for a Binary Node
 *
 *
 * rotate[L|R]
 *      doing a rotation of a parent and 2 children changes the hierarchy positions of
 *      the nodes but maintains any ordering between the parents and the children
 *
 *
 * Other higher level constructs such as 'Binary Search Tree' vs 'Plain Binary Tree' are specifically
 * are NOT in here.
 *
 *
 */
public interface Node<T extends Comparable<T>> {

  /**
   *
   * @param other  other Node to compare keys with
   * @return   -1    less
   *            0    equal
   *            1    greater
   */
  int compareNode(Node<T> other);

  /**
   *
   * @return  this nodes Key
   *
   */
  T getKey();


  /**
   *
   * @return  this node's Value
   */
  Object getValue();

  Node<T> getL();

  Node<T> getR();

  void setL(Node<T> n);

  void setR(Node<T> n);

  int getCount();

  int getMaxDepth();

  /**
   *
   * @return
   */
  Node<T> rotateR();

  Node<T> rotateL();

  String printTree(int keylength_hint);

}

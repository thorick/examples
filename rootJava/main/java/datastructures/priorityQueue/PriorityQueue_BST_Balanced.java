package datastructures.priorityQueue;

import datastructures.BST.BST;
import datastructures.BST.Node;
import datastructures.BST.StringNode;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/4/13
 * Time: 9:43 AM
 *
 * Demo of Priority Queue implemented on top of a balanced BST
 *
 * A BST is not an optimal datastructure for this usecase
 *
 * A balanced BST is a bad choice for numerous reasons:
 *  0.  The high priority node is always the leaf node at the extreme end
 *        so you are guaranteed to always have to traverse all levels of the
 *        tree to get to the highest priority node
 *
 *  1.  It costs to keep the tree balanced.
 *
 *
 *
 */
public class PriorityQueue_BST_Balanced implements PriorityQ<String> {

  private BST<String> bst;

  public PriorityQueue_BST_Balanced() {
    bst = new BST<String>();
  }

  public boolean empty() {
    return bst.getCount() <= 0;
  }

  public int insert(String n) {
    Node<String> node = new StringNode(n, n);
    bst.insert(node);

    // now we have to balance the tree
    bst.balance();
    return 0;   // meaningless
  };

  /**
   * return max value (priority)
   * empty string if queue is empty.
   *
   * @return
   */
  public String removeTop() {
    // the max node is always the right most
    Node<String> maxNode = bst.getMax();

    if (maxNode == null) return "";
    bst.remove(maxNode);
    bst.balance();
    return maxNode.getKey();
  }

  public String peekTop() {
    Node<String> maxNode = bst.getMax();
    if (maxNode == null) return "";
    return maxNode.getKey();
  }

  public int size() {
    throw new RuntimeException("NYI");
  }

  public String removeNode(int index) {
    throw new RuntimeException("NYI");
  }

  public String getNode(int index) {
    throw new RuntimeException("NYI");
  }

  public  int repositionNode(int index) {
    throw new RuntimeException("NYI");
  }
}

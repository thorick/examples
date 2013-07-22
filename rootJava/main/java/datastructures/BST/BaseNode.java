package datastructures.BST;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/4/13
 * Time: 10:30 AM
 */

//
// Java Generic syntax note:  you only have to specify the wildcard bound once
//
public abstract class BaseNode<T extends Comparable<T>> implements Node<T> {

  private Node<T> left;
  private Node<T> right;

  protected Object value;

  public Node<T> getL() {
    return left;
  }

  public Node<T> getR() {
    return right;
  }

  public void setL(Node<T> n) {
    left = n;
  }

  public void setR(Node<T> n) {
    right = n;
  }

  public Object getValue() {
    return value;
  }


  public int compareNode(Node<T> other) {
    if (other == null) return -1;

    int retVal = getKey().compareTo(other.getKey());
    if (retVal == 0) return retVal;

    Object thisVal = getValue();
    Object otherVal = other.getValue();
    return getValue().equals(other.getValue()) ? 0 : -1;
  }

  abstract public T getKey();

  public String toString() {
    if (getKey() == null) {
      return "NULL";
    }
    return getKey().toString();
  }

  /**
   * Count of the number of nodes in this subtree
   *
   * @return
   */
  public int getCount() {
    int lCount = 0;
    if (getL() != null) {
      lCount = getL().getCount();
    }
    int rCount = 0;
    if (getR() != null) {
      rCount = getR().getCount();
    }
    return lCount + rCount + 1;
  }

  /**
   * perform a Right Rotation starting with this node as the Root.
   * If the L children is null, then NO rotation and return self.
   */
  public Node rotateR() {
    if (getL() == null) return this;

    Node newRoot = getL();         //  our LEFT child to ROOT
    //      this is the meaning of 'rotate RIGHT'
    setL(newRoot.getR());          //  our new LEFT child is our LEFT child's RIGHT
    //      all nodes to the LEFT of us are less than us
    //      including our LEFT child's RIGHT
    newRoot.setR(this);            //  ROOT RIGHT is now US  because WE are greater than OUR LEFT

    return newRoot;                //  new ROOT was our LEFT

  }

  /**
   * perform a Right Rotation starting with this node as the Root.
   * If the R child is null, then NO rotation and return self.
   */
  public Node rotateL() {
    if (getR() == null) return this;

    Node newRoot = getR();         //  our RIGHT child to ROOT
    //     this is the meaning of 'rotate LEFT'
    setR(newRoot.getL());          //  our new RIGHT child is our RIGHT child's LEFT
    //     all nodes to the RIGHT of us are greater than us
    //     including our RIGHT child's LEFT
    newRoot.setL(this);            //  ROOT LEFT is now US  because we are less than OUR RIGHT

    return newRoot;                //  new ROOT was our RIGHT

  }

  /**
   * perform a Right Rotation starting with this node as the Root.
   * If any of L OR R children are null, then NO rotation and return self.
   */
  public Node rotateR_old() {
    if (getL() == null || getR() == null) return this;

    Node newRoot = getL();         //  our LEFT child to ROOT
    //      this is the meaning of 'rotate RIGHT'
    setL(newRoot.getR());          //  our new LEFT child is our LEFT child's RIGHT
    //      all nodes to the LEFT of us are less than us
    //      including our LEFT child's RIGHT
    newRoot.setR(this);            //  ROOT RIGHT is now US  because WE are greater than OUR LEFT

    return newRoot;                //  new ROOT was our LEFT

  }

  /**
   * perform a Right Rotation starting with this node as the Root.
   * If any of L OR R children are null, then NO rotation and return self.
   */
  public Node rotateL_old() {
    if (getL() == null || getR() == null) return this;

    Node newRoot = getR();         //  our RIGHT child to ROOT
    //     this is the meaning of 'rotate LEFT'
    setR(newRoot.getL());          //  our new RIGHT child is our RIGHT child's LEFT
    //     all nodes to the RIGHT of us are greater than us
    //     including our RIGHT child's LEFT
    newRoot.setL(this);            //  ROOT LEFT is now US  because we are less than OUR RIGHT

    return newRoot;                //  new ROOT was our RIGHT

  }

  /**
   * get the max depth of any branch from this node, we count as '1'
   * <p/>
   * Do DFS and keep a local max depth count, return it when we're done.
   * <p/>
   * Specifically NON-RECURSIVE here.
   *
   * @param
   * @return
   */
  public int getMaxDepth() {
    return getMaxDepth(this, 1);
  }


  public int getMaxDepth(Node<T> n, int currDepth) {
    int maxDepth = currDepth;
    int treeDepth = currDepth;
    currDepth++;
    Node<T> l = n.getL();
    if (l != null) {
      treeDepth = getMaxDepth(l, currDepth);
      if (treeDepth > maxDepth) maxDepth = treeDepth;
    }
    Node<T> r = n.getR();
    if (r != null) {
      treeDepth = getMaxDepth(r, currDepth);
      if (treeDepth > maxDepth) maxDepth = treeDepth;
    }
    return maxDepth;
  }

  public String printTree(int keylength_hint) {
    return printTree(this, keylength_hint);
  }


  /**
   * Output a String representation of the tree's keys
   * <p/>
   * The keylength hint will affect the layout, use a decent average length
   *
   * @param n
   * @return
   */
  private String printTree(Node<T> n, int keylength_hint) {

    // find the max depth of the tree
    int maxDepth = getMaxDepth(n, 0);

    //  print the tree one level at a time starting with the root.
    //
    // in a balanced tree
    // the number of entries on level L = 2**(L-1),  first level = 0.
    //  pad over the non-leaf levels accordingly
    //
    //  padding(currLevel) = 1/2 * (keylength + 2) * 2**(L - currLevel - 1)
    //
    int currLevel = 0;

    StringBuilder sb = new StringBuilder("\n");

    Queue<Node> nodeQueue = new ConcurrentLinkedQueue();
    nodeQueue.add(n);

    do {
      String printPad = computePrintPad(maxDepth, currLevel, keylength_hint + 2);
      sb.append(printPad);
      nodeQueue = printLevel(nodeQueue, currLevel, sb);
      currLevel++;
    } while (!nodeQueue.isEmpty());

    return sb.toString();


  }

  //
  // add level to the String  and setup the next level for printing
  //
  private Queue printLevel(Queue<Node> nodeQueue, int currLevel, StringBuilder sb) {

    // Now it's Bread First to print each level
    // To keep the level prints even  NULL nodes are inserted to print NULL
    Queue<Node> nextNodeQueue = new ConcurrentLinkedQueue<Node>();

    // left pad for this level


    int nonNullCount = 0;

    while (!nodeQueue.isEmpty()) {
      Node<T> n = nodeQueue.remove();
      sb.append(n.toString()).append("  ");

      Node<T> lNode = n.getL();
      if (lNode == null) {
        lNode = new NullNode<T>();
      } else {
        nonNullCount++;
      }
      Node<T> rNode = n.getR();
      if (rNode == null) {
        rNode = new NullNode<T>();
      } else {
        nonNullCount++;
      }

      nextNodeQueue.add(lNode);
      nextNodeQueue.add(rNode);
    }
    sb.append("\n");

    if (nonNullCount <= 0) {
      nextNodeQueue.clear();
    }
    return nextNodeQueue;
  }

  private String computePrintPad(int maxDepth, int currDepth, int padLength) {
    int exponent = maxDepth - currDepth;
    //exponent = exponent - 1;     // / 2
    if (exponent <= 0) return "";

    int pos = 1 << (exponent);
    String pad = " ";
    while (pos > 0) {
      pad = pad + pad;
      pos = pos >> 1;
    }
    return pad;    // punt for now

  }

  private class NullNode<T extends Comparable<T>> extends BaseNode<T> {

    private T key;

    public NullNode() {
    }

    public int compareNode(Node<T> other) {
      if (other == null) return -1;

      int retVal = getKey().compareTo(other.getKey());
      if (retVal == 0) return retVal;

      Object thisVal = getValue();
      Object otherVal = other.getValue();
      return getValue().equals(other.getValue()) ? 0 : -1;
    }

    public T getKey() {
      return key;
    }
  }

  /*
  private class NullNodeLeft<T extends Comparable<T>> extends NullNode<T> {
    public NullNodeLeft() {
    }

    public String toString() {
      return "-L--";
    }
  }
  */


  //public abstract int compareNode(Node<T> other);

}

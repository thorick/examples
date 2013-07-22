package datastructures.BST;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/4/13
 * Time: 11:29 AM
 *
 * <p/>
 * In this BST Impl
 * All Nodes to the LEFT are LESS than the parent
 * else  they are to the RIGHT of the parent
 * <p/>
 * Primary Top Level methods:
 *
 * <p/>
 * insert
 * do a plain natural order INSERT into the tree
 * <p/>
 * <p/>
 * insertRoot
 * do an insert followed by rotations to bring the inserted
 * node up to the root, if rotation is possible
 * <p/>
 * <p/>
 * partition(k)
 * set the (k+1)th smallest key as the new root of the tree
 * <p/>
 * <p/>
 * balance
 * balance the tree
 */
public class BST<T extends Comparable<T>> {

  static Logger log = LogManager.getLogger(BST.class.getName());

  private Node<T> root;

  public BST() {
  }

  public int getCount() {
    return root.getCount();
  }

  public Node<T> getRoot() {
    return root;
  }

  public void setRoot(Node<T> n) {
    root = n;
  }

  /**
   * Root Insertion
   * <p/>
   * Insert new Node into the BST
   * Then perform any rotations required to move the new Node
   * to be the ROOT of the Tree
   */
  public Node<T> insertRoot(Node<T> n) {
    if (root == null) {
      root = n;
      return n;
    }

    root = insertLocalRoot(root, n);
    return root;
  }

  /**
   * recursively navigate down the tree doing zig zags until we
   * find the correct NULL spot in which to link in our newly
   * inserted node.
   *
   * then wind back up doing the rotations required to
   * bubble the newly inserted node back up to be the
   * new root of the tree !
   *
   *
   * @param localHead
   * @param n
   * @return
   */
  private Node<T> insertLocalRoot(Node<T> localHead, Node<T> n) {
    p("insertLocalHead:   insert node=" + n.toString());
    if (localHead == null) {
      p("        parent is NULL  so return n="+n.toString());
      return n;
    }

    int compare = n.compareNode(localHead);
    if (compare < 0) {
      p("at node=" + localHead.toString() + "  insert LEFT node=" + n.toString());

      localHead.setL(insertLocalRoot(localHead.getL(), n));
      p("at node=" + localHead.toString() + "  after insert LEFT of n="+n.toString()+", local tree is now " + localHead.printTree(4));

      // we know that because we inserted L, we need to rotate R to bring the new node to the top
      localHead = localHead.rotateR();
      p("at node=" + localHead.toString() + "  after rotateR, local tree is now " + localHead.printTree(4));
    } else {
      p("at node=" + localHead.toString() + "  insert RIGHT node=" + n.toString());

      localHead.setR(insertLocalRoot(localHead.getR(), n));
      p("at node=" + localHead.toString() + "  after insert RIGHT of n="+n.toString()+", local tree is now " + localHead.printTree(4));

      // we know that because we inserted R, we need to rotate L to bring the new node to the top
      localHead = localHead.rotateL();
      p("at node=" + localHead.toString() + "  after rotateL, local tree is now " + localHead.printTree(4));

    }
    return localHead;
  }


  /**
   * Plain search and insert in natural order.
   * <p/>
   * No reordering, no shuffling
   *
   * @param n
   * @return
   */
  public Node<T> insert(Node<T> n) {
    if (root == null) {
      return root = n;
    }
    return insertLocal(n, root);
  }

  /**
   * The new node must be placed on the correct side of its parent tree nodes
   * navigate down the hierarchy and find the first free spot
   *
   * @param newN
   * @param atN
   * @return
   */
  private Node<T> insertLocal(Node<T> newN, Node<T> atN) {
    if (atN == null) throw new RuntimeException("NULL parent node !");

    p("insertLocal for node=" + newN.toString() + ", at node=" + atN.toString());
    if (newN.compareNode(atN) < 0) {
      if (atN.getL() == null) {
        p("insertLocal for node=" + newN.toString() + ", set as LEFT child of " + atN.toString());
        atN.setL(newN);
        return newN;
      }
      p("insertLocal for node=" + newN.toString() + ", next check LEFT child");

      return insertLocal(newN, atN.getL());
    } else {
      if (atN.getR() == null) {
        p("insertLocal for node=" + newN.toString() + ", set as RIGHT child of " + atN.toString());

        atN.setR(newN);
        return newN;
      }
      p("insertLocal for node=" + newN.toString() + ", next check RIGHT child");

      return insertLocal(newN, atN.getR());
    }
  }


  public Node<T> getMax() {

    // max is the right most leaf

    Node<T> n = root;
    if (root == null)  return null;

    Node<T> result = root;
    while (n != null) {
      n = n.getR();
      if (n != null) {
        result = n;
      }
    }
    return result;
  }

  public void remove(Node<T> n) {
    root = removeLocal(root, n);
  }

  private Node<T> removeLocal(Node<T> head, Node<T> n)  {
    if (head == null) {
      p("removeLocal:  head is NULL return NULL");
      return null;
    }

    int compareHN = head.compareNode(n);
    if (compareHN < 0) {
      p("head="+head.toString()+" less than node="+n.toString()+" do remove on head RIGHT and reset RIGHT");
      head.setR(removeLocal(head.getR(), n));
    }
    else if (compareHN > 0) {
      p("head="+head.toString()+" greater than node="+n.toString()+" do remove on head LEFT and reset LEFT");
      head.setL(removeLocal(head.getL(), n));
    }
    else {
      // we have found the node to be deleted
      // join together it's children and return that in the deleted node's place
      //
      p("head="+head.toString()+" equals node="+n.toString()+" do joinLeftRight on LEFT and RIGHT");
      head = joinLeftRight(head.getL(), head.getR());
    }
    p("head="+(head == null ? "NULL" : head.toString())+" returning head");
    return head;
  }

  private Node<T> removeLocal_old(Node<T> head, Node<T> n)  {
    if (head == null) {
      p("removeLocal:  head is NULL return NULL");
      return null;
    }

    int compareHN = head.compareNode(n);
    if (compareHN < 0) {
      p("head="+head.toString()+" less than node="+n.toString()+" do remove on head RIGHT");
      removeLocal(head.getR(), n);
    }
    else if (compareHN > 0) {
      p("head="+head.toString()+" greater than node="+n.toString()+" do remove on head LEFT");
      removeLocal(head.getL(), n);
    }
    else {
      // we have found the node to be deleted
      // join together it's children and return that in the deleted node's place
      //
      p("head="+head.toString()+" equals node="+n.toString()+" do joinLeftRight on LEFT and RIGHT");
      head = joinLeftRight(head.getL(), head.getR());
    }
    p("head="+head.toString()+" returning head");
    return head;
  }



  private Node<T> joinLeftRight(Node<T> l, Node<T> r) {
    String m = "joinLeftRight: ";
    //p(m+" l="+l.toString());
    if (r == null) {
      p(m+" r=NULL   return l="+(l==null ? "NULL" : l.toString()));
      return l;
    }

    p(m+" partition r="+r.toString()+" at 0");
    r = partition(r, 0);

    p(m+" set r.LEFT to l="+l.toString()+" and return r");
    r.setL(l);
    return r;
  }


  public Node<T> balance() {
    return root = balance(root);
  }

  /**
   * Balance the tree at n
   *
   * Find the total count of nodes under subtree n
   * partition the subtree so that the middle node becomes the root
   *
   * This upper level division is correct to the LEFT are less, to the RIGHT are greater
   *   recurse and do this for all subtrees until we done.
   *
   * Since each subtree is partitioned by 1/2 the resulting tree is balanced
   *
   *
   * @return
   */
  private Node<T> balance(Node<T> n) {
    if (n == null) return n;
    int count = n.getCount();
    if (count == 1) return n;

    p("balance:  n=" + n.toString() + ", partition at" + (count / 2) + n.printTree(4));
    n = partition(n, count / 2);

    p("balance:  after partition n=" + n.toString() + ", at " + (count / 2) + n.printTree(4));

    Node<T> l = n.getL();
    if (l != null) {
      p("balance:  n=" + n.toString() + ", do setL on balance L=" + n.getL().toString());
      n.setL(balance(n.getL()));
    } else {
      p("balance:  n=" + n.toString() + ", L is null skip balance L");
    }

    Node<T> r = n.getR();
    if (r != null) {
      p("balance:  n=" + n.toString() + ", do setR on balance R=" + n.getR().toString());
      n.setR(balance(n.getR()));
    } else {
      p("balance:  n=" + n.toString() + ", R is null skip balance R");
    }
    return n;
  }

  /**
   * Partition tree
   * put k+1  smallest key at the root
   *
   * @param k
   * @return
   */
  public Node<T> partition(int k) {
    return root = partition(root, k);
  }

  /**
   *
   * zig zig down the tree comparing our desired partition value
   * versus the number of (ordered) nodes to the LEFT or RIGHT of the current node.
   * Eventually, we will get to the target node corresponding to k
   *
   * As we wind back, do rotations to bring that kth node back up to be
   * the root of the tree.
   *
   * as we
   * @param n
   * @param k
   * @return
   */
  private Node<T> partition(Node<T> n, int k) {

    p("partition: n=" + n.toString() + ", k=" + k);
    int t = 0;
    if (n.getL() != null) {
      t = n.getL().getCount();
      p("partition: n=" + n.toString() + ", t=count of L=" + t);
    }
    p("partition: n=" + n.toString() + ", t=" + t + ", k=" + k);
    if (t > k) {
      p("partition:  t > k.  partition(L, k)");
      Node<T> l = partition(n.getL(), k);

      p("partition: n=" + n.toString() + " after partition L subtree is now: " + l.printTree(4));
      p("partition: n=" + n.toString() + " set new L subtree on n");
      n.setL(l);

      // we partitioned LEFT so rotate RIGHT
      n = n.rotateR();

      p("partition: after rotateR, n subtee is now: " + n.printTree(4));
    } else if (t < k) {
      p("partition:  n=" + n.toString() + ", t < k.");
      Node<T> r = n.getR();
      if (r != null) {
        p("partition:  n=" + n.toString() + ", R != NULL so partition on R with k=" + (k - t - 1));
        r = partition(r, k - t - 1);

        p("partition:  n=" + n.toString() + ", after partition R subtree is now " + r.printTree(4));
        p("partition: n=" + n.toString() + " set new R subtree on n");
        n.setR(r);

        // we partitioned RIGHT so rotate LEFT
        n = n.rotateL();

        p("partition: after rotateL, n subtree is now: " + n.printTree(4));
      }
    }
    return n;
  }


  private Node<T> partition_old00(Node<T> n, int k) {

    p("partition: n=" + n.toString() + ", k=" + k);
    int t = 0;
    if (n.getL() != null) {
      t = n.getL().getCount();
      p("partition: n=" + n.toString() + ", t=count of L=" + t);
    }
    p("partition: n=" + n.toString() + ", t=" + t + ", k=" + k);
    if (t > k) {
      p("partition:  t > k.  partition(L, k)");
      partition(n.getL(), k);

      p("partition: n=" + n.toString() + " subtree is now: " + n.printTree(4));
      n = n.rotateR();

      p("partition: after rotateR, n subtee is now: " + n.printTree(4));
    } else if (t < k) {
      p("partition:  n=" + n.toString() + ", t < k.");
      Node<T> r = n.getR();
      if (r != null) {
        p("partition:  n=" + n.toString() + ", R != NULL so partition on R with k=" + (k - t - 1));
        partition(r, k - t - 1);

        p("partition:  n=" + n.toString() + ", after partition subtree is now " + n.printTree(4));
        n = n.rotateL();

        p("partition: after rotateL, n subtree is now: " + n.printTree(4));
      }
    }
    return n;
  }

  public int getMaxDepth() {
    return root.getMaxDepth();
  }


  /**
   * Compare other BST to us.
   * First check:  must have the same number of nodes
   *                if NOT, then return int -1: other has more,  +1: other has less
   *
   * Second check: all node keys and values must match, return 0 if so, else return -1;
   *
   * @param other
   * @return
   */
  public int compareTo(BST<T> other) {
    if (other == null || other.getRoot()== null) { return 1; }
    if (root == null ) { return -1; }

    if (root.getCount() < other.getRoot().getCount()) { return -1; }
    if (root.getCount() > other.getRoot().getCount()) { return 1; }

    // same counts
    int retVal = -1;

    return compareToLocal(root, other.getRoot());
  }

  private int compareToLocal(Node<T> us, Node<T> other) {
    if (us == null)  {
      if (other == null) {
        return 0;
      }
      return -1;
    }
    if (other == null)  { return -1; }

    int cVal = us.compareNode(other);
    if (cVal != 0)   return cVal;   // we're done

    cVal = compareToLocal(us.getL(), other.getL());
    if (cVal != 0)   return cVal;   // we're done

    return compareToLocal(us.getR(), other.getR());
  }


  public String printTree() {
    return root.printTree(4);
  }

  private void p(String s) {
    log.debug(s);
  }


}

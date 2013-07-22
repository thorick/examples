package datastructures.BST;

import org.junit.Assert;
import org.junit.Assert.*;
import org.junit.Test;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/4/13
 * Time: 3:47 PM
 */
public class BST_test {

  private static Logger log =
         LogManager.getLogger(BST_test.class);


  /**
   * Natural insert nodes such that the tree is balanced on insert
   *
   */
  //@Test
  public void test_orderedNaturalInsertion() {
    p("\n\n    start test  test_orderedNaturalInsertion");

    BST<String> bst = make3levelBalanced();
  }


  //@Test
  public void test_rootInsertion() {
    p("\n\n    start test  test_rootInsertion");
    p("      first step:  create a balanced base tree");
    BST<String> bst = make3levelBalanced();


    p("\n\n    now do root insert of new node");
    Node<String> n = new StringNode("AAGG", "AAGG");
    bst.insertRoot(n);

    String treeString = bst.printTree();
    p("after insert of "+n.toString()+", tree is ");
    p(treeString);
  }




  //@Test
  public void test_remove00_fromMiddle() {
    p("\n\n    start test  test_remove00_fromMiddle");
    p("      first step:  create a balanced base tree");
    BST<String> bst = make3levelBalanced();


    Node<String> n = new StringNode("AANN", "AANN");
    p("\n\n    now do remove of node="+n.toString());
    bst.remove(n);

    p("\n\n    after removal tree is now"+bst.printTree());

  }


  //@Test
  public void test_remove01_fromLeaf() {
    p("\n\n    start test  test_remove01_fromLeaf");
    p("      first step:  create a balanced base tree");
    BST<String> bst = make3levelBalanced();


    Node<String> n = new StringNode("AAKK", "AAKK");
    p("\n\n    now do remove of node="+n.toString());
    bst.remove(n);

    p("\n\n    after removal tree is now"+bst.printTree());

  }




  @Test
  public void test_remove02_fromRoot() {
    p("\n\n    start test  test_remove02_fromRoot");
    p("      first step:  create a balanced base tree");
    BST<String> bst = make3levelBalanced();


    Node<String> n = new StringNode("AAII", "AAII");
    p("\n\n    now do remove of node="+n.toString());
    bst.remove(n);

    p("\n\n    after removal tree is now"+bst.printTree());

    // construct the expected result tree

    //          AAKK
    //     AAEE      AANN
    // AACC  AAFF  NULL  AATT

    BST<String> expected = new BST<String>();
    Node<String> node = new StringNode("AAKK", "AAKK");
    expected.insertRoot(node);

    node = new StringNode("AAEE", "AAEE");
    expected.insert(node);

    node = new StringNode("AANN", "AANN");
    expected.insert(node);

    node = new StringNode("AACC", "AACC");
    expected.insert(node);

    node = new StringNode("AAFF", "AAFF");
    expected.insert(node);

    node = new StringNode("AATT", "AATT");
    expected.insert(node);

    int retVal = bst.compareTo(expected);
    String message = "";
    if (retVal != 0) {
      message = "expected tree"+expected.printTree()+
             "\n      got tree"+bst.printTree();
    }
    Assert.assertEquals(message, 0, retVal);

    /*
    Node<String> t = bst.getRoot();
    String expected = "AAKK";
    Assert.assertEquals("expected root=" + expected + ", but we got " + t.toString(), expected, t);

    Node<String> tL = t.getL();
    expected = "AAEE";
    Assert.assertEquals("expected node=" + expected + ", but we got " + tL.toString(), expected, tL);

    Node<String> tR = t.getR();
    expected = "AANN";
    Assert.assertEquals("expected node=" + expected + ", but we got " + tR.toString(), expected, tR);
     */

  }

  //@Test
  public void test_partition01() {
    p("\n\n    start test  test_partition01");
    p("      first step:  create a balanced base tree");
    BST<String> bst = make3levelBalanced();

    p("\n      now partition at k=2, set the 3rd smallest key as the root");
    Node<String> newRoot = bst.partition(2);
    p("       newly partitioned tree"+newRoot.printTree(4));

  }

  //@Test
  public void test_partition02() {
    p("\n\n    start test  test_partition02");
    p("      first step:  create a balanced base tree");
    BST<String> bst = make3levelBalanced();

    p("\n      now partition at k=5, set the 6th smallest key as the root");
    Node<String> newRoot = bst.partition(5);
    p("       newly partitioned tree"+newRoot.printTree(4));

  }

  //@Test
  public void test_balance00() {
    p("\n\n    start test  test_balance00");
    p("      first step:  create a balanced base tree\n\n");
    BST<String> bst = make3levelBalanced();

    p("\n      now partition at k=5, set the 6th smallest key as the root\n\n");
    Node<String> newRoot = bst.partition(5);
    p("       newly partitioned tree"+newRoot.printTree(4));

    p("\n      now rebalance the partitioned tree\n\n");
    bst.setRoot(newRoot);
    newRoot = bst.balance();
    p("\n      balanced tree is"+newRoot.printTree(4));
  }


  //@Test
  public void test_balance01() {
    p("\n\n    start test  test_balance01\n\n");
    p("      create a worst case linear tree\n");
    BST<String> bst = new BST<String>();
    Node <String> n = new StringNode("AAAA", "AAAA");
    bst.insert(n);

    n = new StringNode("CCCC", "CCCC");
    bst.insert(n);

    n = new StringNode("EEEE", "EEEE");
    bst.insert(n);

    n = new StringNode("GGGG", "GGGG");
    bst.insert(n);

    n = new StringNode("IIII", "IIII");
    bst.insert(n);

    n = new StringNode("KKKK", "KKKK");
    bst.insert(n);

    p("     linear tree is"+bst.printTree());
    bst.balance();

    p("     balanced tree is"+bst.printTree());

  }



  public BST make3levelBalanced() {
    BST<String> bst = new BST<String>();

    // root node
    Node<String> n = new StringNode("AAII", "AAII");
    bst.insert(n);

    String treeString = bst.printTree();
    p("after insert of "+n.toString()+", tree is ");
    p(treeString);

    // now left child
    n = new StringNode("AAEE", "AAEE");
    bst.insert(n);

    treeString = bst.printTree();
    p("after insert of "+n.toString()+", tree is ");
    p(treeString);

    // now right child
    n = new StringNode("AANN", "AANN");
    bst.insert(n);

    treeString = bst.printTree();
    p("after insert of "+n.toString()+", tree is ");
    p(treeString);


    // start 3rd leaf level
    n = new StringNode("AACC", "AACC");
    bst.insert(n);

    treeString = bst.printTree();
    p("after insert of "+n.toString()+", tree is ");
    p(treeString);


    n = new StringNode("AAFF", "AAFF");
    bst.insert(n);

    treeString = bst.printTree();
    p("after insert of "+n.toString()+", tree is ");
    p(treeString);


    n = new StringNode("AAKK", "AAKK");
    bst.insert(n);

    treeString = bst.printTree();
    p("after insert of "+n.toString()+", tree is ");
    p(treeString);

    n = new StringNode("AATT", "AATT");
    bst.insert(n);

    treeString = bst.printTree();
    p("after insert of "+n.toString()+", tree is ");
    p(treeString);
    return bst;
  }


  private void p(String s) {
    log.debug(s);
  }
}

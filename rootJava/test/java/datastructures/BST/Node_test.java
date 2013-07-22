package datastructures.BST;


import org.junit.Test;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/5/13
 * Time: 9:29 AM
 */
public class Node_test {

  private static Logger log =
          LogManager.getLogger(Node_test.class);

  @Test
  public void test_rotateL() {
    String m = "test_rotateL: ";

    // root node
    Node<String> root = new StringNode("AAGG", "AAGG");

    // NULL Left, non-NULL Right
    Node<String> n = new StringNode("AAMM", "AAMM");
    root.setR(n);

    p(m + "before rotateL  tree is \n" + root.printTree(4));
    root = root.rotateL();
    p(m + "after rotateL  tree is\n" + root.printTree(4));
  }

  @Test
  public void test_rotateR() {
    String m = "test_rotateR: ";

    // root node
    Node<String> root = new StringNode("AAGG", "AAGG");

    // non-NULL Left, NULL Right
    Node<String> n = new StringNode("AAEE", "AAEE");
    root.setL(n);

    p(m + "before rotateR  tree is \n" + root.printTree(4));
    root = root.rotateR();
    p(m + "after rotateR  tree is\n" + root.printTree(4));
  }


  private void p(String s) {
    log.debug(s);
  }
}

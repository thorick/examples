package datastructures.BST;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/6/13
 * Time: 11:53 AM
 */
public class NodeFactory<T extends Comparable<T>> {

  static public Node newNode(String key) {
    return new StringNode(key);
  }

}

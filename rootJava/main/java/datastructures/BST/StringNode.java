package datastructures.BST;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/4/13
 * Time: 10:11 AM
 *
 * String is a natural Comparable and easy for humans to read, let's use it.
 *
 */
public class StringNode extends BaseNode<String>
        implements Node<String> {

  private final String key;

  public StringNode(String key) {
    this.key = key;
  }

  public StringNode(String k, Object v) {
    if (k==null) throw new RuntimeException("NULL key !");
    key = k;
    value = v;
  }

  public String getKey() { return key; }

  public Object getValue()  { return value; }

  @Override
  public int compareNode(Node<String> other) {
    return key.compareTo(other.getKey());
  }
}

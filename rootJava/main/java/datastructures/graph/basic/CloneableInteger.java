package datastructures.graph.basic;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/4/13
 * Time: 9:56 AM
 */
public class CloneableInteger implements CloneableData {

  Integer delegate;


  public CloneableInteger(int i) {
    delegate = new Integer(i);
  }

  public CloneableData clone() {
    return new CloneableInteger(this.delegate.intValue());
  }

  public int intValue() {
    return delegate.intValue();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}

package datastructures.graph.basic;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/24/13
 * Time: 10:54 AM
 */
public interface CloneableData extends Cloneable {
  public CloneableData clone() throws CloneNotSupportedException;
}

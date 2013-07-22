package datastructures.priorityQueue;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/4/13
 * Time: 9:34 AM
 *
 * http://pages.cs.wisc.edu/~vernon/cs367/notes/11.PRIORITY-Q.html
 *
 *
 */
public interface PriorityQ <T extends Comparable<T>> {
  boolean empty();

  void insert(T n);

  T removeMax();
}

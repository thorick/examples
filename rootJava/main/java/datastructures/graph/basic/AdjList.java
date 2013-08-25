package datastructures.graph.basic;

/**
 * Created with IntelliJ IDEA.
 * User: thorick chow
 * Date: 6/11/13
 * Time: 7:09 PM
 *
 * Adjacency List for vertices connected to the vertex AdjList owner
 *
 * For a given start vertex, this is will provide List access
 * to all the vertices that start is connected to.
 *
 */
public interface AdjList {

  LinkedNode head();

  LinkedNode next();

  boolean isEnd();
}

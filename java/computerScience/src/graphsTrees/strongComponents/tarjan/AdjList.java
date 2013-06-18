package graphsTrees.strongComponents.tarjan;

/**
 * Created with IntelliJ IDEA.
 * User: thorick chow
 * Date: 6/11/13
 * Time: 7:09 PM
 *
 * Adjacency List for vertices connected to the vertex AdjList owner
 *
 */
public interface AdjList {

  int head();

  int next();

  boolean isEnd();
}

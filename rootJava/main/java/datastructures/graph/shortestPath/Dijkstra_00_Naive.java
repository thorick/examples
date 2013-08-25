package datastructures.graph.shortestPath;


import datastructures.graph.basic.CloneableInteger;
import datastructures.graph.basic.GraphAdjList;
import datastructures.graph.basic.LinkedNode;
import org.apache.log4j.Logger;
import utils.StackIntArray;
import utils.StringUtils;

import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/3/13
 * Time: 2:47 PM
 * <p/>
 * Do the naive implementation of Dijkstra's Shortest Path
 * with no fancy datastructures, just a straightfoward linear
 * scan of all neighbors on each vertex iteration.
 * <p/>
 * Assumptions: vertex numbers non-negative
 */
public class Dijkstra_00_Naive implements Dijkstra {

  private Logger log =
          Logger.getLogger(Dijkstra_00_Naive.class);

  GraphAdjList g;
  int highestVertexNumber = -1;
  int vertexCount;
  int[] dist;             // distances from root
  boolean[] unExplored;   // left to explore, true = unexplored
  StackIntArray stack;    // neighbors to process

  // I gave up and wimped out to use the Java lib this time
  LinkedList<Integer> candidateVertices;  // list of possible next Edges

  public Dijkstra_00_Naive(GraphAdjList g) {
    this.g = g;
    vertexCount = g.vCount();
    highestVertexNumber = g.getHighestVertexNumber();

    log.debug("vertexCount=" + vertexCount+", highestVertexNumber="+highestVertexNumber);

    dist = new int[highestVertexNumber+1];
    unExplored = new boolean[highestVertexNumber+1];
    //dist = new int[vertexCount];
        //unExplored = new boolean[vertexCount];
  }

  /**
   * Start at the designated root.
   * <p/>
   * For vertex v:
   * for all neighbors z
   * if (dist[z]  >  dist[v] + length(v->z)
   * then  dist(z) =  dist[v] + length(v->z)
   * (if we are keeping shortest PATH info, we'd store the
   * new shortest path in the vertex z now)
   * <p/>
   * remove v from unExplored
   *
   * @param rootVertex
   */
  public void compute(int rootVertex) {
    init(rootVertex);

    // one time kick off
    int v = rootVertex;
    dist[v] = 0;


    // load up queue of v's neighbors to process
    // then process those that are in the unexplored zone
    // when the queue is exhausted, v is taken off the unexplored list
    //
    // remember which of the neighbors from the unexplored list
    // yielded the smallest distance from v
    // this neighbor becomes the next vertex to explore.
    //
    while (v >= 0) {

      // load up the v neighbors
      LinkedNode node = g.getAdjList(v);

      if (isP())
        log.debug("next v=" + v + " neighbors: " + (node != null ? (node.printNodeChain()) : "null"));

      while (node != null) {
        int z = node.vertexHeadNumber();
        addCandidateVertex(z);

        if (isP())
          log.debug("added new candidateVertex, are now " + StringUtils.printListInteger(candidateVertices));

        int edgeDist = ((CloneableInteger) node.edge().getData()).intValue();
        int trialDist = dist[v] + edgeDist;

        if (isP())
          log.debug("neighbor=" + z + ", edgeDist=" + edgeDist + ", trialDist=" + trialDist +
                  ", dist[z]=" + dist[z]);

        if (trialDist < dist[z]) {
          dist[z] = trialDist;

          if (isP())
            log.debug("set lower dist[" + z + "]=" + trialDist);
        }
        node = node.next();
      }
      // done with the neighbors  so move 'v' to the explored set
      unExplored[v] = false;
      removeCandidateVertex(v);

      v = findSmallestCandidateVertex();

      if (isP())
        log.debug("smallest candidate is now " + v);
    }
  }

  public int dist(int v) {
    return dist[v];
  }

  public int[] getDistArray() {
    return dist;
  }

  private void init(int rootVertex) {
    //for (int i = 0; i < vertexCount; i++) {
    for (int i = 0; i <= highestVertexNumber; i++) {
      dist[i] = Integer.MAX_VALUE;
      unExplored[i] = true;
    }
    stack = new StackIntArray(highestVertexNumber+1);
    //stack = new StackIntArray(vertexCount);

    // set the candidate vertices
    candidateVertices = new LinkedList<Integer>();
    candidateVertices.add(new Integer(rootVertex));

  }

  private void addCandidateVertex(int v) {
    if (unExplored[v] == false) return;

    Integer val = new Integer(v);
    if (!candidateVertices.contains(val)) {
      candidateVertices.add(val);
    }
  }

  private void removeCandidateVertex(int v) {

    Integer val = new Integer(v);

    if (isP())
      log.debug("remove candidate "+v);
    if (candidateVertices.contains(val)) {
      candidateVertices.remove(val);
    }
  }


  // this is what makes this the naive implementation
  // a linear scan for the smallest candidate edge to use on the next round
  //
  // this makes the entire algorithm run in O(n*m) time
  //
  private int findSmallestCandidateVertex() {
    int minVal = Integer.MAX_VALUE;
    int theVertex = -1;
    for (Integer i : candidateVertices) {
      int v = i.intValue();
      if (unExplored[v] == true) {   // should never be false really
        int val = dist[v];
        if (val < minVal) {
          minVal = val;
          theVertex = i;
        }
      }
    }
    return theVertex;
  }

  protected boolean isP() {
    return log.isDebugEnabled();
  }
}

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
public class Dijkstra_02_Heap implements Dijkstra {

  private Logger log =
          Logger.getLogger(Dijkstra_02_Heap.class);

  GraphAdjList g;
  int vertexCount;
  int highestVertexNumber;
  DijkstraVertex[] vertices;    // all vertex metadata
  StackIntArray stack;    // neighbors to process

  // I gave up and wimped out to use the Java lib this time
  PriorityQueue_ArrayHeapMin_DijkstraVertex candidateVertices;  // Heap of possible next Edges

  public Dijkstra_02_Heap(GraphAdjList g) {
    this.g = g;
    vertexCount = g.vCount();
    highestVertexNumber = g.getHighestVertexNumber();
    vertices = new DijkstraVertex[highestVertexNumber+1];
    log.debug("vertexCount=" + vertexCount);
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
    String m = "compute ";
    init(rootVertex);

    // one time kick off
    int v = rootVertex;


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
        log.debug(m + "NEXT   v=" + v + " neighbors: " + (node != null ? (node.printNodeChain()) : "null"));

      while (node != null) {
        int z = node.vertexHeadNumber();

        if (isP()) {
          log.debug(m + "next neighbor " + v + "-" + z);
          log.debug(m + "added new candidateVertex " + z + ", are now " + candidateVertices.toString());
        }
        int edgeDist = ((CloneableInteger) node.edge().getData()).intValue();
        int trialDist = getDist(v) + edgeDist;

        if (isP())
          log.debug(m + "neighbor=" + z + ", edgeDist=" + edgeDist + ", trialDist=" + trialDist +
                  ", dist[z]=" + getDist(z));

        if (trialDist < getDist(z)) {
          setDist(z, trialDist);

          if (isP())
            log.debug(m + "set lower dist[" + z + "]=" + trialDist);
        }

        // you MUST wait until you set the distance BEFORE you try to add
        int heapArrayIndex = vertices[z].getHeapArrayPosition();
        if (heapArrayIndex == 0) {
          addCandidateVertex(z);
        } else {
          // already in Heap update
          if (isP())
            log.debug(m + "got heapIndex=" + heapArrayIndex + ", for vertices[" + z + "]");

          // it's possible that we've not yet placed this node in the Heap yet
          if (heapArrayIndex != 0) {
            candidateVertices.repositionNode(z);
          }
        }
        node = node.next();

        if (isP())
          log.debug(m + "DONE  with neighbor " + v + "-" + z + "\n\n\n\n");
      }

      // done with the neighbors  so move 'v' to the explored set
      setUnExplored(v, false);

      if (isP())
        log.debug(m + " done with exploring remove from Heap " + v + "\n\n\n\n\n");

      removeCandidateVertex(v);

      v = findSmallestCandidateVertex();

      if (isP())
        log.debug(m + "DONE   smallest candidate for next iteration is now " + v + "\n\n\n\n\n\n");
    }
  }

  public int dist(int v) {
    return vertices[v].getValue();
  }

  public int[] getDistArray() {
    int[] retVal = new int[vertices.length];
    for (int i = 0; i < vertices.length; i++) {
      retVal[i] = vertices[i].getValue();
    }
    return retVal;
  }

  private void init(int rootVertex) {
    for (int i = 0; i <= highestVertexNumber; i++) {
      vertices[i] = new DijkstraVertex(i, Integer.MAX_VALUE);
    }
    stack = new StackIntArray(highestVertexNumber);
    candidateVertices = new PriorityQueue_ArrayHeapMin_DijkstraVertex(highestVertexNumber);

    // sanity, really should not have to do this
    vertices[rootVertex].setValue(0);
    candidateVertices.insert(vertices[rootVertex]);
  }

  private void setDist(int v, int val) {
    vertices[v].setValue(val);
  }

  private int getDist(int v) {
    return vertices[v].getValue();
  }

  private void setUnExplored(int v, boolean b) {
    vertices[v].setUnExplored(b);
  }

  private boolean isUnExplored(int v) {
    return vertices[v].isUnExplored();
  }

  private void addCandidateVertex(int v) {
    if (isUnExplored(v) == false) return;

    candidateVertices.insert(vertices[v]);
  }

  private void removeCandidateVertex(int v) {
    DijkstraVertex dv = vertices[v];
    int heapIndex = dv.getHeapArrayPosition();
    if (heapIndex <= 0) return;   // not in Heap !

    if (isP())
      log.debug("remove candidate " + v);

    candidateVertices.removeNode(heapIndex);
  }


  //
  //  pluck the smallest candidate from the top of the Heap
  //
  private int findSmallestCandidateVertex() {
    String m = "findSmallest  ";
    DijkstraVertex dv = candidateVertices.removeTop();
    if (dv == null) return -1;      // signal to stop

    dv.setHeapArrayPosition(0);      // zero means not on heap

    // now re-adjust all tentative distances to account for
    // removing the smallest candidate from the heap
    //   scan all vertices connected to 'dv' and recompute the current
    //   min tentative distance
    // load up the v neighbors
    LinkedNode node = g.getAdjList(dv.vertex);

    while (node != null) {
      int edgeHead = node.edge().w;

      if (isP()) {
        log.debug(m+" picked from heap vertex="+dv+", next node.edge="+node.edge()+", about to check if edgeHead "+edgeHead+" is unexplored.");
      }
      // check to see if this edge is already in the explored set, skip if it is
      if (isUnExplored(edgeHead)) {
        int edgeDist = ((CloneableInteger) node.edge().getData()).intValue();
        int trialDist = getDist(dv.vertex) + edgeDist;

        if (isP())
          log.debug(m + "neighbor=" + vertices[edgeHead] + ", edgeDist=" + edgeDist + ", trialDist=" + trialDist +
                  ", dist[z]=" + getDist(edgeHead));

        if (trialDist < getDist(edgeHead)) {
          setDist(edgeHead, trialDist);
          int heapArrayIndex = vertices[edgeHead].getHeapArrayPosition();

          if (isP())
            log.debug(m + "got heapIndex=" + heapArrayIndex + ", for vertices[" + edgeHead + "]=" + vertices[edgeHead]);

          // it's possible that we've not yet placed this node in the Heap yet
          if (heapArrayIndex != 0) {
            candidateVertices.repositionNode(heapArrayIndex);
          } else {
            candidateVertices.insert(vertices[edgeHead]);
          }
          if (isP())
            log.debug(m + "set lower dist[" + edgeHead + "]=" + trialDist + "\n");
        }
      }
      node = node.next();
    }

    if (isP())
      log.debug(m + "DONE.  smallest vertex is " + dv + "\n\n\n\n");

    return dv.vertex;
  }

  protected boolean isP() {
    return log.isDebugEnabled();
  }


}

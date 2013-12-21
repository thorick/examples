package datastructures.MST.prims_ArrayHeap;

import datastructures.MST.MST_testable;
import datastructures.graph.basic.CloneableInteger;
import datastructures.graph.basic.Edge;
import datastructures.graph.basic.GraphAdjList;
import datastructures.graph.basic.LinkedNode;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/6/13
 * Time: 5:35 AM
 * <p/>
 * Question 3
 * <p/>
 * In this programming problem you'll code up Prim's minimum spanning tree algorithm.
 * <p/>
 * Download the text file here. This file describes an undirected graph with integer edge costs.
 * It has the format
 * <p/>
 * [number_of_nodes] [number_of_edges]
 * [one_node_of_edge_1] [other_node_of_edge_1] [edge_1_cost]
 * [one_node_of_edge_2] [other_node_of_edge_2] [edge_2_cost]
 * ...
 * For example, the third line of the file is "2 3 -8874", indicating that there is
 * an edge connecting vertex #2 and vertex #3 that has cost -8874.
 * <p/>
 * You should NOT assume that edge costs are positive, nor should you assume that they are distinct.
 * Your task is to run Prim's minimum spanning tree algorithm on this graph.
 * <p/>
 * You should report the overall cost of a minimum spanning tree --- an integer, which may or may not be negative ---
 * <p/>
 * in the box below.
 * <p/>
 * IMPLEMENTATION NOTES: This graph is small enough that the straightforward O(mn) time implementation
 * of Prim's algorithm should work fine.
 * <p/>
 * OPTIONAL: For those of you seeking an additional challenge, try implementing a heap-based version.
 * The simpler approach, which should already give you a healthy speed-up,
 * is to maintain relevant edges in a heap (with keys = edge costs).
 * <p/>
 * The superior approach stores the unprocessed vertices in the heap, as described in lecture.
 * <p/>
 * Note this requires a heap that supports deletions, and you'll probably need to maintain
 * some kind of mapping between vertices and their positions in the heap.
 * <p/>
 * <p/>
 * NOTE:  Prim's here is for an UNDIRECTED GRAPH.
 *
 *  numVertices=500, numEdges=2184

  ===  result: 2143917597
 *
 *
 */
public class MST_Prims_vertexHeaps_startEmpty_Stanford_Coursera extends
        MST_Prims_base_Stanford_Coursera implements MST_testable {


  // debug:  halt heap replacement after this many inserts.
  //         negative value means do not halt
  public static int heapReplaceHalt = -1;
  private int heapReplaceCount;


  private Logger log =
          Logger.getLogger(MST_Prims_vertexHeaps_startEmpty_Stanford_Coursera.class);


  private Set<Integer> mappedVertices;     // tracks the vertices already selected for the MST
  private Map<Integer, PrimsVertex> unmappedVertices;
  private GraphAdjList MST;       // resulting MST
  private int sumOfMSTEdges = 0;

  protected GraphAdjList newGraph(int size, boolean isDigraph) {
    return new GraphAdjList(size, isDigraph);
  }


  protected Edge newEdge(int v, int w, CloneableInteger weight) {
    return new Edge(v, w, weight);
  }

  /**
   * Initialize the Vertex Heap, Heap of *Heads*:
   * <p/>
   * for each Vertex Head (by interpreting the Vertex list as the Head we are sure to get them all consistently):
   * scan it's Edge List for the lowest Edge value
   * add that vertex and it's edge weight + target vertex to the Heap
   * <p/>
   * <p/>
   * Select an initial Vertex
   * make it easy, just pick the lowest numbered one.
   * add to SELECTED Hash SET
   * <p/>
   * Scan the Heap for the next lowest numbered vertex whose TAIL belongs to the SELECTED SET
   *
   * @param g
   * @return
   */
  public int compute(GraphAdjList g) {
    heapReplaceCount = 0;
    int highestNumberedVertex = g.getHighestVertexNumber();

    log.debug("highest vertex number = " + highestNumberedVertex);

    // the result MST
    MST = new GraphAdjList(600, false);

    mappedVertices = new HashSet<Integer>();
    unmappedVertices = new HashMap<Integer, PrimsVertex>();

    PriorityQueue_ArrayHeapMin_PrimsVertex heap =
            new PriorityQueue_ArrayHeapMin_PrimsVertex(600);


    // construct the initial PrimsVertex heap:
    //   no vertices in the selected set
    //   so all vertices go into the Heap but with
    //   MAX weight   and a meaningless edge (we'll make it a self edge)
    //
    CloneableInteger maxEdge = new CloneableInteger(Integer.MAX_VALUE);
    int firstVertex = -1;
    PrimsVertex firstPV = null;

    for (int i = 0; i <= highestNumberedVertex; i++) {
      LinkedNode n = g.getAdjList(i);
      if (n == null) continue;


      StringBuilder sb = new StringBuilder();

      PrimsVertex pv = new PrimsVertex(i, i, maxEdge);
      unmappedVertices.put(new Integer(i), pv);
      if (firstVertex < 0) {
        firstVertex = i;
        firstPV = pv;
      }
      heap.insert(pv);
    }


    // in the HEAP  the edge vertices are  Unmapped (vertex) = v    Mapped (otherVertex) = w

    // handle initial vertex U
    // remove U from Heap
    // add to mappedVertices
    // update heap with lowest cost edge from U
    //   we are now primed to start pulling vertices off of the heap
    PrimsVertex firstPVsanity = unmappedVertices.remove(firstVertex);
    if (firstPV.thisVertex != firstPVsanity.thisVertex) throw new RuntimeException("ERROR mismatched first vertex: " +
            firstPV.thisVertex + " != " + firstPVsanity.thisVertex);

    int heapIndex = firstPV.getHeapArrayPosition();

    log.debug("\n\n  ---------  initial heap " + heap.toString());
    log.debug("\n\n\n\n ????????     process initial vertex: " + firstPV);

    PrimsVertex pvRemoved = heap.removeNode(heapIndex);
    log.debug("  -----------  after removing vertex: " + pvRemoved + ", heap is now " + heap.toString());

    if (firstPV.thisVertex != pvRemoved.thisVertex) throw new RuntimeException("ERROR mismatched first vertex: " +
            firstPV.thisVertex + " != " + pvRemoved.thisVertex);

    Integer vI = firstPV.getVertexI();
    mappedVertices.add(vI);              // accounting

    // look up lowest cost edge from plucked vertex now in Mapped  it's  Mapped = v    Unmapped = w
    Edge minEdge = handleMinEdgeFrom(g, heap, vI.intValue());
    if (minEdge == null) throw new RuntimeException("ERROR !  got NULL initial min edge ! for initial vertex=" + vI);

    log.debug("  -----------  after handling first vertex minEdge, heap is now " + heap.toString());


    // until heap empty:
    //   pick next vertex from heap
    //   tally weight
    //   add to mappedVertices
    //   remove edge from candidate graph
    //   add edge to MST
    //   from U update new lowest edge from candidate graph (if any), update heap
    //   from V update new lowest edge from candidate graph (if any), update heap

    PrimsVertex nextPV = heap.removeTop();
    while (nextPV != null) {

      sumOfMSTEdges += nextPV.getWeightAsInt();
      log.debug(" ++++++  after adding " + nextPV.getWeightAsInt() + ":  sumOfMSTEdges=" + sumOfMSTEdges);

      Integer nextPVI = nextPV.getVertexI();             // unmapped vertex
      Integer nextPVotherI = nextPV.getOtherVertexI();    // mapped vertex

      // handle bidirectional edge
      Edge f = new Edge(nextPVI.intValue(), nextPVotherI.intValue());
      Edge r = new Edge(nextPVotherI.intValue(), nextPVI.intValue());
      g.remove(f);
      g.remove(r);

      MST.insert(f);
      MST.insert(r);

      // trial move to AFTER the minEdge checks , worked
      // doing it here,  also worked.
      mappedVertices.add(nextPVI);
      unmappedVertices.remove(nextPVI);

      log.debug("\n ----------  handleMinEdgeFrom  former unMapped vertex, now a newly mapped vertex: " + nextPVI.intValue());
      Edge retEdge = minEdge = handleMinEdgeFrom(g, heap, nextPVI.intValue());
      log.debug("\n ----------  after handleMinEdgeFrom  former unMapped vertex, now a newly mapped vertex: " + nextPVI.intValue() +
              ", chosen edge=" + (retEdge == null ? "NULL" : retEdge) + ", heap is " + heap.toString());

      log.debug("\n ----------  handleMinEdgeFrom  mapped vertex: " + nextPVotherI.intValue());
      retEdge = minEdge = handleMinEdgeFrom(g, heap, nextPVotherI.intValue());
      log.debug("\n ----------  handleMinEdgeFrom  mapped vertex: " + nextPVotherI.intValue() +
              ", chosen edge=" + (retEdge == null ? "NULL" : retEdge) + ", heap is " + heap.toString());

      if (heapReplaceHalt > 0) {
        heapReplaceCount++;
        log.debug(" ++++++++++  DONE WITH DEBUG HEAP ITERATION "+heapReplaceCount+"\n\n");
        if (heapReplaceCount >= heapReplaceHalt) {
          throw new RuntimeException("HALTING program for debug at heap interation " + heapReplaceCount);
        }
      }
      //log.debug("\n\n  ++++++++  after handling minEdge: "+minEdge+", Heap is now "+heap.toString());

      // trial when uncommented, worked
      // mappedVertices.add(nextPVI);
      //unmappedVertices.remove(nextPVI);

      nextPV = heap.removeTop();
      log.debug("\n\n\n ~~~~~~~~~~~~~   NEXT Vertex from heap: " +
              (nextPV == null ? "NULL" : nextPV.toString()));
    }
    return sumOfMSTEdges;
  }

  /**
   * Intended to be called immediately after a Vertex has been pulled from the unmapped to the mapped
   * set by Prim's.
   * <p/>
   * <p/>
   * ???
   * It is assumed that the candidate edge graph, the MST and all of the Vertex tracking lookups
   * have already been updated !
   *
   * @param graph
   * @param heap
   * @param fromVertex
   * @return
   */
  protected Edge handleMinEdgeFrom(GraphAdjList graph, PriorityQueue_ArrayHeapMin_PrimsVertex heap, int fromVertex) {
    log.debug("  ============  find minEdge from vertex=" + fromVertex);
    Edge minEdge = findMinEdgeFrom(graph, heap, fromVertex);
    if (minEdge != null) {
      int unmappedW = minEdge.w;
      PrimsVertex chosenPM = unmappedVertices.get(new Integer(unmappedW));
      log.debug("  ===  minEdge=" + minEdge + ",  chosenPM=" + (chosenPM == null ? "NULL" : chosenPM.toString()));

      if (chosenPM != null) {
        // update the minWeight of the chosen PrimsVertex to that of the chosen minEdge
        chosenPM.setWeight((CloneableInteger) minEdge.getData());

        heap.removeNode(chosenPM.getHeapArrayPosition());
        chosenPM.setWeight((CloneableInteger) minEdge.getData());
        chosenPM.setOtherVertex(minEdge.v);    //  set pointer in Heap's edge from Unmapped  to Mapped (other)
        heap.insert(chosenPM);                 //  reinsert to update heap
      } else {

        //  NO it is possible to not have a reachable unmappedVertex even if the heap is not empty, we hit a dead end.
        if (!heap.empty())
          log.debug("  ===  no more unmappedVertices from " + fromVertex + ".  nothing to return.");
        //
        // throw new RuntimeException(" handleMinEdge: no unmappedVertex=" + minEdge.w + ", but heap is NOT empty !" + heap.toString());
      }
    }
    return minEdge;
  }


  protected Edge findMinEdgeFrom(GraphAdjList graph, PriorityQueue_ArrayHeapMin_PrimsVertex heap, int fromVertex) {
    LinkedNode n = graph.getAdjList(fromVertex);
    if (n == null) {
      log.debug("  !!!!  no more edges from vertex=" + fromVertex);
      return null;   // no more edges from this vertex
    }

    StringBuilder sb = null;
    if (isP()) sb = new StringBuilder();

    Edge minEdge = n.edge();
    int minWeight = ((CloneableInteger) minEdge.getData()).intValue();
    log.debug("  !!!!  set first minEdge=" + minEdge + ", minWeight=" + minWeight);

    while (n.hasNext()) {
      n = n.next();
      // if the vertex head from this mapped vertex points to another mapped vertex, then this
      //    would be an edge that would create a cycle, so disregard it !
      Edge e = n.edge();
      Integer headVertexI = new Integer(e.w);
      log.debug("  !!!!  next candidate edge: " + e);

      if (!mappedVertices.contains(headVertexI)) {
        int weight = ((CloneableInteger) e.getData()).intValue();
        if (isP())
          sb.append(", " + n.edge().v + "-" + n.edge().w + " w=" + weight);

        if (weight < minWeight) {
          minWeight = weight;
          minEdge = e;
          if (isP())
            sb.append("  new minEdge=" + minEdge);
        }
      } else {
        if (isP())
          log.debug(" skipping edge that would create a cycle.  edge: " + e);
      }
    }

    if (isP()) {
      sb.append("  final minEdge=" + minEdge);
      log.debug(sb.toString());
    }

    // todo:  verify this is OK to do outside of loop that checks all candidate edges

    // check:  if the target vertex head 'w' is already in the heap, then we keep the lowest weight edge
    Integer wI = minEdge.w;
    if (unmappedVertices.containsKey(wI)) {
      PrimsVertex alternatePV = unmappedVertices.get(wI);
      if (alternatePV.getWeightAsInt() <= minWeight) {
        log.debug(" =====  detected in heap vertex " + alternatePV + " has lower or equal weight to " + minEdge + ", keeping heap as is.");
        minEdge = null;    // keep the heap as is
      }
    }
    return minEdge;
  }


  private boolean isP() {
    return log.isDebugEnabled();
  }



  public static void main(String[] args) {
    MST_Prims_vertexHeaps_startEmpty_Stanford_Coursera mst =
            new MST_Prims_vertexHeaps_startEmpty_Stanford_Coursera();

    GraphAdjList g = mst.readDataFile(null);
    int retVal = mst.compute(g);
    System.err.println(" ===  result: " + retVal);
  }

}

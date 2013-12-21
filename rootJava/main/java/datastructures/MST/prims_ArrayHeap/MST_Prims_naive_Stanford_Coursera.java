package datastructures.MST.prims_ArrayHeap;

import datastructures.MST.MST_testable;
import datastructures.graph.basic.CloneableInteger;
import datastructures.graph.basic.Edge;
import datastructures.graph.basic.GraphAdjList;
import datastructures.graph.basic.LinkedNode;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/7/13
 * Time: 8:54 AM
 * <p/>
 * Naive implementation does straight forward
 * greedy next vertex + edge selection
 * <p/>
 * Price paid for continually scanning edges for the lowest of a qualified set.
 */
public class MST_Prims_naive_Stanford_Coursera extends
        MST_Prims_base_Stanford_Coursera implements MST_testable {


  private Logger log =
          Logger.getLogger(MST_Prims_naive_Stanford_Coursera.class);

  private Set mappedVertices;     // tracks the vertices already selected for the MST
  private Set unMappedVertices;   // tracks the vertices not yet selected for the MST
  private GraphAdjList MST;       // resulting MST
  private int sumOfMSTEdges = 0;
  private List cycleEdges;

  protected GraphAdjList newGraph(int size, boolean isDigraph) {
    return new GraphAdjList(size, isDigraph);
  }

  protected Edge newEdge(int v, int w, CloneableInteger weight) {
    return new Edge(v, w, weight);
  }

  public int compute(GraphAdjList inGraph) {

    if (isP())
      log.debug("begin compute   input g " + inGraph.toString());

    // operate on a copy of the original graph
    // since we will be taking it apart as we construct the MST
    //
    GraphAdjList graph = inGraph.copy();

    int startVertex = -1;
    int highestVertexNumber = graph.getHighestVertexNumber();
    unMappedVertices = new HashSet(numVertices);
    mappedVertices = new HashSet();
    MST = new GraphAdjList(highestVertexNumber + 1, false);
    cycleEdges = new LinkedList();

    for (int i = 0; i <= highestVertexNumber; i++) {
      if (graph.getAdjList(i) != null) {
        if (startVertex == -1) startVertex = i;
        unMappedVertices.add(new Integer(i));
      }
    }


    // though we can pick any vertex to start, to have some predictable results
    // we'll start with the lowest numbered vertex
    int currVertex = startVertex;
    Integer currVertexI = new Integer(currVertex);
    unMappedVertices.remove(currVertexI);
    mappedVertices.add(currVertexI);

    if (isP())
      log.debug(" set initial vertex " + currVertexI);

    while (!unMappedVertices.isEmpty()) {

      // search for a vertex to add to a mapped vertex

      // for Prim's greedy algorithm
      //   we must pick the lowest cost edge from the complete set of edges:
      //   any selected vertex to any unselected vertex.
      //
      // this means that you must scan the entire sets before you know that you
      // have the lowest cost edge.
      //
      int lowWeight = Integer.MAX_VALUE;
      int lowVertexV = -1;
      int lowVertexW = -1;
      Edge lowestCostEdge = null;
      cycleEdges.clear();

      Iterator it = unMappedVertices.iterator();
      while (it.hasNext()) {
        Integer w = (Integer) it.next();    // auto unboxing rules
        if (isP())
          log.debug("check unMapped w=" + w);

        LinkedNode n = graph.getAdjList(w);

        if (isP())
          if (n != null)
            log.debug("  node chain for unmapped w=" + w + ": " + n.printNodeChain());

        while (n != null) {
          Integer v = n.vertexHeadNumber();
          if (isP())
            log.debug(" -- from " + w + "-" + v + ", check next v=" + v);

          if (mappedVertices.contains(v)) {
            if (isP())
              log.debug("  -- got a hit for " + v + "-" + w + " w=" + ((CloneableInteger) n.edge().getData()).intValue());

            int weight = ((CloneableInteger) n.edge().getData()).intValue();
            if (weight < lowWeight) {
              lowWeight = ((CloneableInteger) n.edge().getData()).intValue();
              lowVertexV = v;
              lowVertexW = w;
              lowestCostEdge = n.edge();
              if (isP())
                log.debug("  ---  new lowest edge " + v + "-" + w + ", w=" + lowWeight);
            }
          }
          n = n.next();
        }
      }

      // now process the lowest cost edge that we found.
      //
      if (isP())
        log.debug("  DONE  selected lowest cost edge " + lowestCostEdge + "\n");

      Edge lowestCostReverseEdge = new Edge(lowestCostEdge.w, lowestCostEdge.v, lowestCostEdge.getData());

      sumOfMSTEdges += lowWeight;
      mappedVertices.add(lowVertexW);
      unMappedVertices.remove(lowVertexW);
      MST.insert(lowestCostEdge);
      MST.insert(lowestCostReverseEdge);

      // remove the selected edge from the candidate edge graph
      graph.remove(lowestCostEdge);
      graph.remove(lowestCostReverseEdge);

      // now remove any cycles from the candidate edges graph
      // these will be any edges from the newly selected vertex lowVertexW BACK to any other selected vertex
      LinkedNode n = graph.getAdjList(lowVertexW);
      log.debug(" ??? check cycle edges for w=" + lowVertexW);
      while (n != null) {
        int vertexV = n.vertexHeadNumber();
        if (isP())
          log.debug(" ???  check v=" + vertexV);
        if (mappedVertices.contains(vertexV)) {
          if (isP())
            log.debug("  ???   v=" + vertexV + " is IN the mapped vertices Set");
          cycleEdges.add(n.edge());
        }
        n = n.next();
      }

      if (!cycleEdges.isEmpty()) {
        it = cycleEdges.iterator();
        while (it.hasNext()) {
          Edge e = (Edge) it.next();
          if (isP())
            log.debug("  ======= remove cycle edge " + e);
          graph.remove(e);
          graph.remove(new Edge(e.w, e.v, null));
        }
      }
      if (isP())
        log.debug(" -- DONE   process selected edge " + lowVertexV + "-" + lowVertexW + " w=" + lowWeight);
    }
    return sumOfMSTEdges;
  }

  private boolean isP() {
    return log.isDebugEnabled();
  }

  /**
   * numVertices=500, numEdges=2184

    resulting MST path weight=-3612829

   * @param args
   */
  public static void main(String[] args) {
    MST_Prims_naive_Stanford_Coursera mst = new MST_Prims_naive_Stanford_Coursera();

    GraphAdjList g = mst.readDataFile(null);
    int result = mst.compute(g);
    System.err.println(" resulting MST path weight="+result);
  }
}

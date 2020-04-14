package datastructures.graph.allPairsShortestPaths;

import datastructures.graph.basic.*;
import datastructures.graph.shortestPath.Dijkstra;
import datastructures.graph.shortestPath.Dijkstra_03_Heap_ignoreSelfEdges;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 10/2/13
 * Time: 7:44 PM
 * <p/>
 * In this assignment you will implement one or more algorithms for the all-pairs shortest-path problem.
 * <p/>
 * Here are data files describing three graphs: graph #1; graph #2; graph #3.
 * <p/>
 * The first line indicates the number of vertices and edges, respectively.
 * Each subsequent line describes an edge (the first two numbers are its tail and head, respectively)
 * and its length (the third number). NOTE: some of the edge lengths are negative.
 * <p/>
 * NOTE: These graphs may or may not have negative-cost cycles.
 * <p/>
 * Your task is to compute the "shortest shortest path".
 * <p/>
 * Precisely, you must first identify which, if any, of the three graphs have no negative cycles.
 * <p/>
 * For each such graph, you should compute all-pairs shortest paths and remember the smallest one
 * <p/>
 * (i.e., compute minu,v∈Vd(u,v), where d(u,v) denotes the shortest-path distance from u to v).
 * <p/>
 * <p/>
 * If each of the three graphs has a negative-cost cycle, then enter "NULL" in the box below.
 * <p/>
 * If exactly one graph has no negative-cost cycles, then enter the length of its shortest shortest path in the box below.
 * <p/>
 * If two or more of the graphs have no negative-cost cycles,
 * then enter the smallest of the lengths of their shortest shortest paths in the box below.
 * <p/>
 * OPTIONAL: You can use whatever algorithm you like to solve this question.
 * If you have extra time, try comparing the performance of different all-pairs shortest-path algorithms!
 * <p/>
 * OPTIONAL: If you want a bigger data set to play with,
 * try computing the shortest shortest path for this graph.
 *
 *
 *
 *

 Graph 3:

 =========== Shortest Shortest Path Length ========
  399 -> -> 904
                  length=-19


 */
public class Johnsons_AllPairsShortestPaths  {

  private Logger log =
          Logger.getLogger(Johnsons_AllPairsShortestPaths.class);


  private GraphAdjList graphOriginal;
  private int startVertex;


  /////////////
  //  Bellman Ford vars
  private int graphVertexSize;
  private int edgesInGraph;


  private GraphAdjList graph;
  private int[] currDistTo;

  private Edge[] edgeTo;       // latest min edge incoming on vertex
  private Edge[] edgeProcessingArray;

  // Edge preprocessing tempVars for building faster access ordered Edge Array
  private Queue<Edge> edgeQueue;
  private Queue<Edge> nextEdgeQueue;

  private int maxEdgeLimit;    // stop normal BF after this many edges (V - 1)

  private int sourceVertex = -1;  // keep track of who we ran this on.
  private boolean ranOnce;     // we ran complete BF at least once

  private boolean foundLowerWeight;

  //////////////


  /////////
  //  re-weight vars
  private int origGraphHighestNumberedVertex;
  private int reweightVertex;

  private Edge[] reweightedEdgeArray;


  //////////////


  /////////
  //  Dijkstra vars
  //

  // shortest path weight to:
  //
  // i = source vertex
  // j = dest vertex
  //
  private int[][] djShortestPaths;

  private int shortestShortestPathStartVertex = -1;
  private int shortestShortestPathEndVertex = -1;

  // this is the shifted path length w/o negative edges
  private int shortestShortestPathLength = Integer.MAX_VALUE;


  //////////


  //
  // the shortest paths from djikstra
  //  converted to the actual path lengths converting back from weight shifted edge weights
  //
  private int[][] actualShortestPaths;
  int actualShortestShortestPathLength = Integer.MAX_VALUE;


  public Johnsons_AllPairsShortestPaths(GraphAdjList_digraph g) {
    graphOriginal = g;
    origGraphHighestNumberedVertex = graphOriginal.getHighestVertexNumber();
    graph = g.copy();

    startVertex = -1;
    LinkedNode n = null;
    while (n == null) {
      startVertex++;
      n = g.getAdjList(startVertex);
    }
  }

  public void compute() {

    addZeroEdgeVertex();

    computeBF();
    log.debug(" *****  BF complete ");

    if (isP()) {
      int[] shortest = shortestDistanceToArray();
      StringBuilder sb = new StringBuilder("\n                    Bellman Ford computed shortest distances from " +
              reweightVertex + "\n");
      for (int i = 0; i < currDistTo.length; i++) {
        sb.append("to[" + i + "]=" + currDistTo[i] + ", ");

      }
      sb.append("\n");
      log.debug(sb.toString());
    }

    setReweightedEdges();
    log.debug(" *****  Reweight Edges complete ");

    runAllDijkstras();
    log.debug(" *****  Run all Dijkstra's complete ");

    /*
    // find shortest shortest path
    // we know the start vertex and end vertex computed from the shifted weights
    //   now back compute what the actual weight is for the path spanning the 2 vertices
    //
    //  c(e) = c'(e) + p(v) - p(u)
    //
    //

    */

    System.err.println("\n\n\n  =========== Shortest Shortest Path Length ========\n" +

            "   " + shortestShortestPathStartVertex + " -> -> " + shortestShortestPathEndVertex +
            "\n                   length=" + shortestShortestPathLength);
  }

  public int getShortestShortestPathLength() {
    return shortestShortestPathLength;
  }

  // find shortest shortest path
  // we know the start vertex and end vertex computed from the shifted weights
  //   now back compute what the actual weight is for the path spanning the 2 vertices
  //
  //  c(e) = c'(e) + p(v) - p(u)
  //
  //
  public int convertToActualShortestPathLength(int shiftedPathLength, int startVertex, int endVertex) {
    return shiftedPathLength +
            currDistTo[endVertex] -
            currDistTo[startVertex];
  }

  private void addZeroEdgeVertex() {
    reweightVertex = origGraphHighestNumberedVertex + 1;

    // add zero-length edges to compute graph
    for (int origVertex = 0; origVertex <= origGraphHighestNumberedVertex; origVertex++) {
      LinkedNode n = graph.getAdjList(origVertex);

      // note that self-edges count.  a self-edge only vertex might have only incoming edges
      if (n != null) {
        Edge emptyEdge = new Edge(reweightVertex, origVertex, new CloneableInteger(0));
        graph.insert(emptyEdge);
      }
    }
  }

  /**
   * We've run Bellman Ford on the augmented graph
   * This has given us the information with which to transform
   * the original input graph edge weights to eliminate ALL negative
   * edges, while preserving relative compound path lengths.
   * (Setting us up to be able to run Dijkstra's Shortest Path algo)
   * <p/>
   * c'(e) = c(e) + p(u) - p(v)
   * <p/>
   * c'(e) = transformed guaranteed non-negative graph edge weight
   * c(e) = original graph edge weight
   * p(u) = result of Bellman Ford on augmented graph for tail 'u'
   * p(v) = result of Bellman Ford on augmented graph for head 'v'
   */
  private void setReweightedEdges() {
    // run the edge reweight off of the edgeProcessingArray
    //   ignoring any edges from the reweightVertex

    reweightedEdgeArray = new Edge[edgeProcessingArray.length];

    for (int edgeIndex = 0; edgeIndex < edgeProcessingArray.length; edgeIndex++) {
      Edge e = edgeProcessingArray[edgeIndex];
      if (e.v == reweightVertex) {
        reweightedEdgeArray[edgeIndex] = e;    // these edges don't matter
      } else {
        int adjustedW = ((CloneableInteger) e.getData()).intValue() +
                currDistTo[e.v] - currDistTo[e.w];

        Edge newEdge = new Edge(e.v, e.w, new CloneableInteger(adjustedW));
        reweightedEdgeArray[edgeIndex] = newEdge;
        graph.remove(e);

        // BUG !  need to insert the NEW edge
        //graph.insert(e);
        graph.insert(newEdge);
      }
    }
    if (isP()) {
      log.debug("\n");
      log.debug(" ++++++  graph with reweighted edges: \n" +
              graph.toString() + "\n");
    }
  }


  /**
   * Run single source shortest path Dijkstra on all sources
   * on the transformed graph will all non-negative edge weights
   * <p/>
   * keep track of the shortest path that we found.
   * <p/>
   * (excluding the artificial augmented edge)
   * <p/>
   * Found during testing:
   * There may be many ties in the shortest paths found during Dijkstras on the modified graphs
   * each of the ties for the lowest cost paths has to be resolved to find the *actual* lowest cost
   * path on the UNADJUSTED graph !
   */
  private void runAllDijkstras() {
    djShortestPaths = new int[origGraphHighestNumberedVertex + 1][];
    actualShortestPaths = new int[origGraphHighestNumberedVertex + 1][];

    //for (int vertex = 0; vertex <= origGraphHighestNumberedVertex; vertex++) {
    for (int vertex = startVertex; vertex <= origGraphHighestNumberedVertex; vertex++) {
      LinkedNode n = graph.getAdjList(vertex);

      // process only if there is a non-self edge
      boolean hasNonSelfEdge = false;
      while (n != null) {
        Edge e = n.edge();
        if (e.v != e.w) {
          hasNonSelfEdge = true;
          break;
        }
        n = n.next();
      }
      if (hasNonSelfEdge) {
        if (isP()) {
          log.debug("\n\n\n                                   -------- run Dijstra on s=" + vertex);
        }
        Dijkstra dj = newDijkstra(graph);
        dj.compute(vertex);
        djShortestPaths[vertex] = dj.getDistArray();
        actualShortestPaths[vertex] = new int[djShortestPaths[vertex].length];
        //for (int i = 0; i < djShortestPaths[vertex].length; i++) {
        for (int i = startVertex; i < djShortestPaths[vertex].length; i++) {
          int unshiftedLength = djShortestPaths[vertex][i];
          int shiftedLength = Integer.MAX_VALUE;      // be sure to ignore irrelevant edges
          if (unshiftedLength != Integer.MAX_VALUE) {
            shiftedLength = convertToActualShortestPathLength(unshiftedLength, vertex, i);
          }
          actualShortestPaths[vertex][i] = shiftedLength;

          if (isP()) {
            log.debug(" --- [" + vertex + "] -> [" + i + "] shortest path unadjusted=" + unshiftedLength + ", adjusted=" + shiftedLength);
          }
          if (shiftedLength < shortestShortestPathLength) {
            shortestShortestPathLength = shiftedLength;
            shortestShortestPathStartVertex = vertex;
            shortestShortestPathEndVertex = i;
            if (isP()) {
              log.debug(" ----  found new shortest shortest path: " + vertex + " -> -> " + i +
                      ", shifted non-negative edge length=" + shortestShortestPathLength);
            }
          }
        }
      }
    }
  }


  private boolean isP() {
    return log.isDebugEnabled();
  }


  private Dijkstra newDijkstra(GraphAdjList g) {
    return new Dijkstra_03_Heap_ignoreSelfEdges(g);
  }

  //////////////////////////////
  //
  //   Bellman Ford  section
  //

  //  cut and pasted working BF
  //  if there's a problem be aware of fixing where this was cut from
  //    G:\b\notes\java\my_examples\src\main\java\datastructures\graph\allPairsShortestPaths\BellmanFord_edgeArray_strict_wNegativeCycleDetection.java
  //
  private void computeBF() {
    sourceVertex = reweightVertex;

    // vertices start at 1 (and not 0) in the course problems
    graphVertexSize = graph.getHighestVertexNumber() + 1;

    // we only need this many max path lengths to cover the entire graph minus any cycles
    maxEdgeLimit = graph.vCount() - 1;
    edgesInGraph = graph.eCount();
    edgeTo = new Edge[graphVertexSize];
    currDistTo = new int[graphVertexSize];

    for (int v = 0; v <= graph.getHighestVertexNumber(); v++) {
      currDistTo[v] = Integer.MAX_VALUE;
    }

    // since we will be running this algo multiple times on the same sequence
    // of edges.
    // Take some time up front to put the edges into an ordered array
    // so that we just march along the array on each iteration
    // order is roughly Breadth First Search excluding cycles (no edge queued twice)
    //
    fillEdgeProcessingArray();

    // Bellman-Ford algorithm
    //   me:  figure out single source shortest paths from input 's'
    //            for starters 's' is the ONLY vertex on the processing queue
    //         note use of queue indicator array on vertices  'onQueue[]'
    //
    foundLowerWeight = false;

    log.debug("begin BellMan Ford for source vertex=" + sourceVertex +
            ", number of vertices=" + graph.vCount() +
            ", do for n-1 iterations n-1=" + maxEdgeLimit);

    runCompleteBF();

    //return 0;
  }

  private void runCompleteBF() {
    currDistTo[sourceVertex] = 0;

    // do BellMan Ford iterations for path length limit (numberVertices -1)
    for (int currPathLenLimit = 1; currPathLenLimit <= maxEdgeLimit; currPathLenLimit++) {
      System.err.println(" --- begin bf=" + currPathLenLimit + " out of " + maxEdgeLimit);
      bf();
    }
    ranOnce = true;
  }

  private int[] shortestDistanceToArray() {
    return currDistTo;
  }

  private void bf() {
    //
    //  me:  process all of 'v's  *edges* (children)
    //

    int recalcCount = 0;
    for (int edgeIndex = 0; edgeIndex < edgeProcessingArray.length; edgeIndex++) {
      Edge e = edgeProcessingArray[edgeIndex];
      int v = e.v;   // tail parent
      int w = e.w;   // head child

      // now compute the DP result for this target vertex
      int edgeWeight = ((CloneableInteger) e.getData()).intValue();
      if (isP()) {
        if (edgeWeight < 0) {
          log.debug(" $$$ processing negative edge weight  edge=" + e +
                  ", prevDistTo[" + w + "]=" + currDistTo[w] +
                  ", prevDistTo[" + v + "]=" + currDistTo[v] +
                  ",  calc check value=" + (currDistTo[v] + edgeWeight));
        }
      }
      if (currDistTo[w] > currDistTo[v] + edgeWeight) {
        recalcCount++;

        // got a new minimum set the next DP value
        currDistTo[w] = currDistTo[v] + edgeWeight;
        edgeTo[w] = e;    // remember tha min edge
        foundLowerWeight = true;
        if (isP()) {
          log.debug(" -- found lower weight at vertex=" + w + " for edge=" + e + ", prev=" + currDistTo[w] +
                  ", new=" + currDistTo[w] +
                  ", edgeTo[" + w + "]=" + e);
        }
      }
    }
    System.err.println(" recalculated " + recalcCount + " vertices");
  }


  protected void fillEdgeProcessingArray() {
    Set queuedEdges = new HashSet<Edge>();
    nextEdgeQueue = new LinkedList<Edge>();
    edgeProcessingArray = new Edge[edgesInGraph];
    int index = 0;
    int level = 0;

    // prime the pump
    LinkedNode n = graph.getAdjList(sourceVertex);
    while (n != null) {
      Edge e = n.edge();
      queuedEdges.add(e);
      nextEdgeQueue.add(e);
      edgeProcessingArray[index++] = e;

      if (isP()) {
        log.debug("  ====  added new edge at level " + (level) + " " + e);
      }
      n = n.next();
    }

    // now pump till dry
    while (!nextEdgeQueue.isEmpty()) {
      if (isP()) {
        log.debug(" --- level=" + level + " produced " + nextEdgeQueue.size() + " edges to check.");
      }
      edgeQueue = nextEdgeQueue;
      nextEdgeQueue = new LinkedList<Edge>();

      while (!edgeQueue.isEmpty()) {
        Edge e = edgeQueue.remove();
        n = graph.getAdjList(e.w);
        if (e.v == e.w) {
          if (isP()) {
            // log.debug("  @@@@@@@@ detected self-edge " + e);
          }
          n = null;
        }
        while (n != null) {
          Edge e1 = n.edge();
          if (!queuedEdges.contains(e1)) {
            queuedEdges.add(e1);
            edgeProcessingArray[index++] = e1;
            nextEdgeQueue.add(e1);
            if (isP()) {
              //  log.debug("  ====  added new edge at level "+(level+1)+" "+e1);
            }
          }
          n = n.next();
        }
      }
      level++;
    }
    if (isP()) {
      log.debug(" filled edge processing array with " + index + " ordrered edges.");
    }
  }


  public int shortestDistanceTo(int v) {
    return currDistTo[v];
  }

  //
  //   Bellman Ford  section    END
  //
  //////////////////////////////

  protected static GraphAdjList_digraph readDataFile(String inputFName) {
    //
    //  Graph of no more than 1000 vertices
    //
    GraphAdjList_digraph graph = new GraphAdjList_digraph(1010);

    FileReader fileR = null;
    String f = "g3_stanford_coursera";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\graph\\allPairsShortestPaths";

    String fileName = d + "\\" + f;
    if (inputFName != null && inputFName.length() > 0) {
      fileName = inputFName;
    }
    int negativeEdgeCount = 0;
    try {
      fileR = new FileReader(fileName);
    } catch (FileNotFoundException e) {
      System.err.println(" cannot open data file " + fileName);
    }

    // get count so that we can build only the array we need
    try {
      BufferedReader br = new BufferedReader(fileR);
      String line;

      line = br.readLine();
      String[] s = line.split("\\s+");
      int numVertices = Integer.valueOf(s[0]);
      int numEdges = Integer.valueOf(s[1]);

      System.err.println(" num vertices=" + numVertices + ", num edges=" + numEdges);
      boolean hasNegativeEdge = false;

      int i = 0;
      while ((line = br.readLine()) != null) {
        //System.err.println("read line '" + line + "'");
        s = line.split("\\s+");
        int v = Integer.valueOf(s[0]);
        int w = Integer.valueOf(s[1]);
        int weight = Integer.valueOf(s[2]);
        Edge e = new Edge(v, w, new CloneableInteger(weight));
        graph.insert(e);

        if (weight < 0) negativeEdgeCount++;

        i++;
        if (i % 10 == 0) {
          //  System.err.println("read vertex #" + i + " = " + i);
        }
      }
      br.close();
      System.err.println(" read " + i + " edges.  read " + negativeEdgeCount + " negative edges.");
    } catch (IOException e) {
      System.err.println("exception " + e.getMessage());
    }
    return graph;
  }


  public static void main(String[] args) {
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\graph\\allPairsShortestPaths";
    String f = "g3_stanford_coursera";


    String fileName = d + "\\" + f;
    GraphAdjList_digraph g = readDataFile(fileName);

    Johnsons_AllPairsShortestPaths prog = new Johnsons_AllPairsShortestPaths(g);
    prog.compute();

    // boolean hasNegativeCycle = prog.hasNegativeCycle();


  }


}

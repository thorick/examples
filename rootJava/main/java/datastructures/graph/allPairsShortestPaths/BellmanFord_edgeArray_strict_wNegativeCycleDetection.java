package datastructures.graph.allPairsShortestPaths;

import datastructures.graph.basic.*;
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
 * Date: 9/29/13
 * Time: 4:51 PM
 * <p/>
 * <p/>
 * Runs Bellman Ford  on my standard high overhead digraph classes
 * <p/>
 * This is *strict* Bellman Ford that counts number of DP iterations
 * and begins each round by starting at the source vertex 's'.
 * <p/>
 * Not the most efficient algorithm (as compared to those based on say Breadth First Search).
 * But as an exercise, this algo will be correct.
 * <p/>
 * <p/>
 * <p/>
 * Will detect negative cycles by running past the maximum number of required vertex processing
 * (unless it stops early by detecting no changes before then)
 * <p/>
 * Some key datastructures based on ideas from Sedgewick's implementation
 */
public class BellmanFord_edgeArray_strict_wNegativeCycleDetection implements BellmanFord {

  private Logger log =
          Logger.getLogger(BellmanFord_edgeArray_strict_wNegativeCycleDetection.class);

  private int graphVertexSize;
  private int edgesInGraph;

  private GraphAdjList graph;
  private int[] currDistTo;

  private Edge[] edgeTo;       // latest min edge incoming on vertex
  private Edge[] edgeProcessingArray;

  private Queue<Edge> edgeQueue;
  private Queue<Edge> nextEdgeQueue;

  private int maxEdgeLimit;    // stop normal BF after this many edges (V - 1)

  private int sourceVertex = -1;  // keep track of who we ran this on.
  private boolean ranOnce;     // we ran complete BF at least once

  private boolean foundLowerWeight;


  public BellmanFord_edgeArray_strict_wNegativeCycleDetection(GraphAdjList graph) {

    // operate on a copy of the input graph
    this.graph = graph.copy();

    if (isP()) {
      log.debug("input graph: vCount=" + graph.vCount() + "\n" + graph.toString());
    }
  }

  /**
   * @param s starting vertex
   * @return 0  compute OK
   *         -1  negative cycle detected
   */
  public int compute(int s) {
    sourceVertex = s;

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
    return 0;
  }


  private void runCompleteBF() {

    // todo: seed source vertex edge
    currDistTo[sourceVertex] = 0;

    // do BellMan Ford iterations for path length limit (numberVertices -1)
    for (int currPathLenLimit = 1; currPathLenLimit <= maxEdgeLimit; currPathLenLimit++) {
      System.err.println(" --- begin bf="+currPathLenLimit+" out of "+maxEdgeLimit);
      bf();
    }
    ranOnce = true;
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
                  ", edgeTo["+w+"]="+e);
        }
      }
    }
    System.err.println(" recalculated "+recalcCount+" vertices");
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

  public int[] shortestDistanceToArray() {
    return currDistTo;
  }

  public String printPathTo(int w) {
    Set tempSet = new HashSet();
    tempSet.add(w);
    String path = Integer.toString(w);
    Edge e = edgeTo[w];
    while (e != null && (e.v != e.w)) {
      if (tempSet.contains(e.v)) {
        // cycle done.
        break;
      }
      tempSet.add(e.v);
      path = Integer.toString(e.v) + " -> "+ path;
      int v = e.v;
      e = edgeTo[v];
    }
    path = sourceVertex + " -> " + path;
    return path;
  }


  //
  // run complete BF a second time and see if we found ANY lower weights
  // if we have, then there is a negative cycle
  //
  public boolean hasNegativeCycle() {
    if (!ranOnce) {
      throw new RuntimeException("Need to have run compute at least once before you can check for negative cycles.");
    }
    log.debug("\n\n ===== start hasNegativeCycle check ======");

    foundLowerWeight = false;

    // run a complete Bellman Ford again
    // if we found a lower weight this time
    // then there's a negative cycle in there somewhere
    runCompleteBF();
    log.debug(" === done with extra BF run.");

    if (foundLowerWeight) return true;
    return false;
  }

  private boolean isP() {
    return log.isDebugEnabled();
  }


  protected static GraphAdjList_digraph readDataFile(String inputFName) {
    //
    //  Graph of no more than 1000 vertices
    //
    GraphAdjList_digraph graph = new GraphAdjList_digraph(1001);

    FileReader fileR = null;
    String f = "g1_stanford_coursera";
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

    // negative cycle check

    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\graph\\allPairsShortestPaths";

    //String f = "g1_stanford_coursera";
    //String f = "g2_stanford_coursera";
    String f = "g3_stanford_coursera";


    String fileName = d + "\\" + f;
    GraphAdjList_digraph g = readDataFile(fileName);

    BellmanFord_edgeArray_strict_wNegativeCycleDetection prog = new BellmanFord_edgeArray_strict_wNegativeCycleDetection(g);
    prog.compute(1);

    boolean hasNegativeCycle = prog.hasNegativeCycle();
    System.err.println(" graph " + f + " hasNegativeCycle=" + hasNegativeCycle);


  }

}

package datastructures.graph.allPairsShortestPaths;

import datastructures.graph.basic.*;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;


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
public class BellmanFord_edgeQueue_strict_wNegativeCycleDetection implements BellmanFord {

  private Logger log =
          Logger.getLogger(BellmanFord_edgeArray_strict_wNegativeCycleDetection.class);

  private int graphVertexSize;
  private int edgesInGraph;

  private GraphAdjList g;
  private boolean currIsA = true;
  private int[] currDistTo;
  private int[] prevDistTo;

  private int[] distanceToA;
  private int[] distanceToB;

  private Edge[] edgeTo;       // latest min edge incoming on vertex
  private Queue<Integer> vertexQueue;
  private Queue<Integer> nextVertexQueue;

  private Queue<Edge> edgeQueue;
  private Queue<Edge> nextEdgeQueue;

  private boolean[] onQueue;   // is this vertex on the BFS queue ?
  private int maxEdgeLimit;    // stop normal BF after this many edges (V - 1)

  private int sourceVertex = -1;  // keep track of who we ran this on.
  private boolean ranOnce;     // we ran complete BF at least once


  private boolean foundLowerWeight;


  public BellmanFord_edgeQueue_strict_wNegativeCycleDetection(GraphAdjList graph) {

    // operate on a copy of the input graph
    g = graph.copy();

    if (isP()) {
      log.debug("input graph: vCount="+g.vCount()+"\n"+g.toString());
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
    graphVertexSize = g.getHighestVertexNumber() + 1;

    // we only need this many max path lengths to cover the entire graph minus any cycles
    maxEdgeLimit = g.vCount() - 1;
    edgesInGraph = g.eCount();
    edgeTo = new Edge[graphVertexSize];
    distanceToA = new int[graphVertexSize];
    distanceToB = new int[graphVertexSize];
    currDistTo = distanceToA;
    currIsA = true;
    prevDistTo = distanceToB;

    for (int v = 0; v <= g.getHighestVertexNumber(); v++) {
      distanceToA[v] = Integer.MAX_VALUE;
      distanceToB[v] = Integer.MAX_VALUE;
    }
    vertexQueue = new LinkedList<Integer>();
    // Bellman-Ford algorithm
    //   me:  figure out single source shortest paths from input 's'
    //            for starters 's' is the ONLY vertex on the processing queue
    //         note use of queue indicator array on vertices  'onQueue[]'
    //
    foundLowerWeight = false;

    log.debug("begin BellMan Ford for source vertex="+sourceVertex+
            ", number of vertices="+g.vCount()+
            ", do for n-1 iterations n-1="+maxEdgeLimit);

    runCompleteBF(sourceVertex, maxEdgeLimit);

    /*
    for (int currPathLenLimit = 1; currPathLenLimit <= maxEdgeLimit; currPathLenLimit++) {

      // do BellMan Ford iterations for path length limit
      bf(s, currPathLenLimit);
    }
    ranOnce = true;
    */
    return 0;
  }


  private void runCompleteBF(int sourceVertex, int maxEdgeLimit) {
    for (int currPathLenLimit = 1; currPathLenLimit <= maxEdgeLimit; currPathLenLimit++) {

      System.err.println(" $$$$  BF for "+currPathLenLimit+" out of "+maxEdgeLimit);
      if (isP()) {
        log.debug(" $$$$  BF for "+currPathLenLimit+" out of "+maxEdgeLimit);
      }
      // do BellMan Ford iterations for path length limit
      bf(sourceVertex, currPathLenLimit);
    }
    ranOnce = true;
  }


  private void bf(int sourceVertex, int pathLenLimit) {
    //
    //  me:  process all of 'v's  *edges* (children)
    //
    int currPathLen = 0;
    while (currPathLen <= pathLenLimit) {

      // bootstrap special case: source vertex   currPathLen == 0
      //  there is always a next pathLen == 1  so set it up now.
      if (currPathLen == 0) {
        currDistTo[sourceVertex] = 0;
        prevDistTo[sourceVertex] = Integer.MAX_VALUE;

        nextEdgeQueue = new LinkedList<Edge>();

        // queue up all the edges for the next path length == 1
        // from this single source vertex
        LinkedNode n = g.getAdjList(sourceVertex);
        while (n != null) {
          Edge e = n.edge();
          nextEdgeQueue.add(e);
          n = n.next();
        }
      } else {
        System.err.println("  at pathLen="+currPathLen+" queued up "+nextEdgeQueue.size()+" edges to check");

        edgeQueue = nextEdgeQueue;    // pick next round of edges
        nextEdgeQueue = new LinkedList<Edge>();

        // set previous distTo to be the updated values from the previous iteration
        boolean diffFound = false;
        for (int i = 0; i < prevDistTo.length; i++) {
          if (prevDistTo[i] != currDistTo[i]) diffFound = true;
          prevDistTo[i] = currDistTo[i];
        }

        if (!diffFound) {
          System.err.println(" found NO weight diffs at currPathLen="+currPathLen+"  terminate early maybe ?");
        }
        // process all edges at this currPathLen
        while (!edgeQueue.isEmpty()) {
          Edge e = edgeQueue.remove();
          int v = e.v;   // tail parent
          int w = e.w;   // head child

          // first queue up all the edges for the next iteration (if there is a next iteration)
          // those are the edges from the current vertex
          if (currPathLen < pathLenLimit) {
            LinkedNode n = g.getAdjList(w);
            while (n != null) {
              Edge e1 = n.edge();
              if (!nextEdgeQueue.contains(e1))
                nextEdgeQueue.add(e1);
              n = n.next();
            }
          }
          // now compute the DP result for this target vertex
          int edgeWeight = ((CloneableInteger) e.getData()).intValue();
          if (isP()) {
            if (edgeWeight < 0) {
              log.debug(" $$$ processing negative edge weight  edge="+e+
               ", prevDistTo["+w+"]="+prevDistTo[w]+
              ", prevDistTo["+v+"]="+prevDistTo[v]+
                      ",  calc check value="+(prevDistTo[v] + edgeWeight));
            }
          }
          if (prevDistTo[w] > prevDistTo[v] + edgeWeight) {

            // got a new minimum set the next DP value
            // currDistTo[w] = currDistTo[v] + edgeWeight;    BUG !  wrong value !

            currDistTo[w] = prevDistTo[v] + edgeWeight;
            edgeTo[w] = e;    // remember tha min edge
            foundLowerWeight = true;
            if (isP()) {
              log.debug(" -- found lower weight at vertex="+w+" for edge="+e+", prev="+prevDistTo[w]+
              ", new="+currDistTo[w]);
            }
          }
        }
      }
      currPathLen++;
    }
  }

  public int shortestDistanceTo(int v) {
    return currDistTo[v];
  }


  public int[] shortestDistanceToArray() {
    return currDistTo;
  }

  public String printPathTo(int v) {
    throw new RuntimeException("NYI");
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
    runCompleteBF(sourceVertex, maxEdgeLimit);
    log.debug(" === done with extra BF run.");

    if (foundLowerWeight)  return true;
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

      System.err.println(" num vertices="+numVertices+", num edges="+numEdges);
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

        if (weight < 0)  negativeEdgeCount++;

        i++;
        if (i % 10 == 0) {
          //  System.err.println("read vertex #" + i + " = " + i);
        }
      }
      br.close();
      System.err.println(" read "+i+" edges.  read "+negativeEdgeCount+" negative edges.");
    } catch (IOException e) {
      System.err.println("exception " + e.getMessage());
    }
    return graph;
  }


  public static void main(String[] args) {

    // negative cycle check

    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\graph\\allPairsShortestPaths";

    String f = "g1_stanford_coursera";

    String fileName = d + "\\" + f;
    GraphAdjList_digraph g = readDataFile(fileName);

    BellmanFord_edgeArray_strict_wNegativeCycleDetection prog = new BellmanFord_edgeArray_strict_wNegativeCycleDetection(g);
    prog.compute(1);

    boolean hasNegativeCycle = prog.hasNegativeCycle();
    System.err.println(" graph "+f+" hasNegativeCycle="+hasNegativeCycle);


  }

}

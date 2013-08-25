package datastructures.graph.minimumCuts;

import datastructures.graph.basic.Edge;
import datastructures.graph.basic.LinkedNode;
import utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


import org.apache.log4j.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/23/13
 * Time: 7:51 AM
 * <p/>
 * Download the text file here. (Right click and save link as)
 * <p/>
 * G:\b\notes\java\my_examples\src\main\resources\datastructures\graph\minimumCuts\Coursera_kargerMinCut
 * <p/>
 * <p/>
 * The file contains the adjacency list representation of a simple undirected graph.
 * There are 200 vertices labeled 1 to 200.
 * The first column in the file represents the vertex label,
 * and the particular row (other entries except the first column) tells all
 * the vertices that the vertex is adjacent to.
 * <p/>
 * So for example, the 6th row looks like : "6 155 56 52 120 ......".
 * <p/>
 * This just means that the vertex with label 6 is adjacent to
 * (i.e., shares an edge with) the vertices with labels 155,56,52,120,......,etc
 * <p/>
 * Your task is to code up and run the randomized contraction algorithm
 * for the min cut problem and use it on the above graph to compute the min cut.
 * <p/>
 * (HINT: Note that you'll have to figure out an implementation of edge contractions.
 * Initially, you might want to do this naively,
 * creating a new graph from the old every time there's an edge contraction.
 * But you should also think about more efficient implementations.)
 * <p/>
 * (WARNING: As per the video lectures, please make sure to run the algorithm
 * many times with different random seeds, and remember the smallest cut
 * that you ever find.)
 */
public class MinimumCuts_RandomizedContraction_Edges_Stanford_Coursera {


  private Logger log =
          Logger.getLogger(MinimumCuts_RandomizedContraction_Edges_Stanford_Coursera.class);


  // not a great thing to use..  settle for now
  protected List<CollapsableEdge> edges;

  protected int edgeCount;

  // the number of live vertices in the vertices Array
  protected int vertexCount;

  protected Random rand;

  public MinimumCuts_RandomizedContraction_Edges_Stanford_Coursera() {
  }

  public List<List<CollapsableEdge>> runStatistical(CollapsableGraphAdjList_EdgeLists g, int maxIterations) {

    rand = new Random(System.currentTimeMillis());

    List<List<CollapsableEdge>> resultList = new LinkedList<List<CollapsableEdge>>();
    List<Integer> countList = new LinkedList<Integer>();

    int minimum = Integer.MAX_VALUE;
    List<CollapsableEdge> minList = null;
    for (int i = 0; i < maxIterations; i++) {
      CollapsableGraphAdjList_EdgeLists gCopy = g.copy();

      // trial  re-read from scratch each time
      //CollapsableGraphAdjList_EdgeLists gCopy = readDataFile(null);

      System.err.println("start iteration: " + i + " on copy");

      List<CollapsableEdge> l = contractionCuts(gCopy);

      int size = g.countList(l);
      countList.add(new Integer(size));
      if (size > 0 && size < minimum) {
        minimum = size;
        minList = l;
        resultList.add(l);
      }
      System.err.println("iteration: " + i + " found " + size + " edges.  min is now: " + minimum);
    }

    int count = 0;
    StringBuilder sb = new StringBuilder();

    for (Integer integer : countList) {
      count++;
      sb.append(count + ": " + integer.toString()).append("\n");
    }

    count = 0;
    for (List<CollapsableEdge> l : resultList) {
      count++;
      sb.append(count + ": " + g.countList(l)).append("\n");
    }
    sb.append("-- minimum " + minimum).append("\n  edges: \n");

    count = 0;
    for (CollapsableEdge e : minList) {
      CollapsableEdge ce = (CollapsableEdge) e;
      sb.append(ce.toString());
    }
    System.err.println(sb.toString());

    return resultList;
  }

  /**
   * Return the List of (parallel) remaining edges connecting the
   * last 2 remaining vertices from the contraction.
   * <p/>
   * The List of edges represents the cut produced with the edges
   * being the set of bridge edges.
   * <p/>
   * When you collapse 2 connected vertices any 3rd vertex and pair of edges
   * that connected the collapsed vertices to the 3rd vertex become
   * a pair of parallel edges.  These generated parallel edges MUST
   * remember their original vertices before being collapsed.
   * <p/>
   * When we arrive at the final pair of vertices, the parallel edges
   * will represent the bridge edges and to be useful they have
   * to remember their original vertices so that they can tell
   * us how we would cut the original graph.
   * <p/>
   * When you collapse 2 vertices self edges are deleted.
   *
   * @param g
   * @return
   */
  public List<CollapsableEdge> contractionCuts(CollapsableGraphAdjList_EdgeLists g) {
    String m = "contractionCuts: ";
    int v = g.vCount();
    edges = g.getCollapsableEdges();
    edgeCount = edges.size();

    // keep contracting until we are down to only 2 vertices
    while (chooseAndContractPair(g)) {  }

    if (isP()) {
      p(m + " found " + edges.size() + " edges this time");
    }
    return edges;
  }

  /**
   * Randomly choose a pair of remaining connected vertices in the Graph g
   * and contract them.
   * <p/>
   * If there are only 2 connected vertices left
   * Then take no action and return false.
   *
   * @param g the Graph
   * @return false if are were only 2 connected vertices left
   */
  protected boolean chooseAndContractPair(CollapsableGraphAdjList_EdgeLists g) {
    String m = "chooseAndContractPair: ";
    if (g.vCount() <= 2) {
      return false;
    }
    edgeCount = edges.size();

    int edgeIndex = (int) Math.round(rand.nextFloat() * (edgeCount - 1));
    CollapsableEdge edge = (CollapsableEdge) edges.get(edgeIndex);

    if (isP())
      log.debug("\n\n\n\n" + m + "there are : " + g.vCount() + " vertices and " + edges.size() + " candidate edges.  graph has " + g.eCount() +
              " edges.  contract edge: " + edge);

    int vB = g.vCount();
    int eB = g.eCount();
    if (isP()) {
      log.debug("BEFORE:  collapse of '" + edge + ", vCount=" + g.vCount() + ", eCount=" + g.eCount());
    }
    g.collapseEdge(edge, edges);
    if (isP()) {
      log.debug("AFTER:  collapse of '" + edge + ", vCount=" + vB + " to " + g.vCount() + ", eCount=" + eB + " to " + g.eCount() + "\n\n\n\n\n");
    }
    return true;
  }


  protected void p(String s) {
    log.debug(s);
  }

  protected boolean isP() {
    return log.isDebugEnabled();
  }


  public static void main(String[] args) {
    MinimumCuts_RandomizedContraction_Edges_Stanford_Coursera runner = new
            MinimumCuts_RandomizedContraction_Edges_Stanford_Coursera();

    // for 200 vertices
    // if we want a 1/200 chance of failure
    // then we need to try  n**2 log n  iterations
    // that is:  4 * 10**4  *  7.6
    //
    //    40000 * 8 = 320000

    int maxIterations = 50;

    CollapsableGraphAdjList_EdgeLists g = readDataFile(null);

    System.err.println("input graph:\n" + g.toString());
    runner.runStatistical(g, maxIterations);
  }

  protected static CollapsableGraphAdjList_EdgeLists readDataFile(String inputFName) {
    //
    //  Graph of no more than 250 vertices
    //
    CollapsableGraphAdjList_EdgeLists graph = new CollapsableGraphAdjList_EdgeLists(250, false);

    FileReader fileR = null;
    String f = "Coursera_kargerMinCut";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\graph\\minimumCuts";
    //String d = "e:\\my_examples\\src\\main\\resources\\datastructures\\graph\\minimumCuts";
    String fileName = d + "\\" + f;
    if (inputFName != null && inputFName.length() > 0) {
      fileName = inputFName;
    }
    try {
      fileR = new FileReader(fileName);
    } catch (FileNotFoundException e) {
      System.err.println(" cannot open data file " + fileName);
    }

    // get count so that we can build only the array we need
    try {
      BufferedReader br = new BufferedReader(fileR);
      String line;

      int i = 0;
      while ((line = br.readLine()) != null) {
        //System.err.println("read line '" + line + "'");
        String[] s = line.split("\\s+");
        int[] in = StringUtils.stringArrayToIntArray(s);
        graph.loadEdges(in);
        //System.err.println(StringUtils.printStringArray(s));

        i++;
        if (i % 10 == 0) {
          //  System.err.println("read vertex #" + i + " = " + i);
        }
      }
      br.close();
    } catch (IOException e) {
      System.err.println("exception " + e.getMessage());
    }
    return graph;
  }

/*
    Some sample output tails:

49: 42
50: 40


-- minimum 34
  edges:
Edge: 105-40 o(186,37)-s38553.
Edge: 105-40 o(36,122)-s38554.
Edge: 105-40 o(116,164)-s38555.
Edge: 105-40 o(41,108)-s38556.
Edge: 105-40 o(161,149)-s38557.
Edge: 105-40 o(10,185)-s38558.
Edge: 105-40 o(134,80)-s38559.
Edge: 105-40 o(169,27)-s38560.
Edge: 105-40 o(109,86)-s38561.
Edge: 105-40 o(24,53)-s38562.
Edge: 105-40 o(141,178)-s38563.
Edge: 105-40 o(163,79)-s38564.
Edge: 105-40 o(184,171)-s38565.
Edge: 105-40 o(188,58)-s38566.
Edge: 105-40 o(123,156)-s38567.
Edge: 105-40 o(165,91)-s38568.
Edge: 105-40 o(48,193)-s38569.

Edge: 40-105 o(37,186)-s38570.
Edge: 40-105 o(122,36)-s38571.
Edge: 40-105 o(164,116)-s38572.
Edge: 40-105 o(108,41)-s38573.
Edge: 40-105 o(149,161)-s38574.
Edge: 40-105 o(185,10)-s38575.
Edge: 40-105 o(80,134)-s38576.
Edge: 40-105 o(27,169)-s38577.
Edge: 40-105 o(86,109)-s38578.
Edge: 40-105 o(53,24)-s38579.
Edge: 40-105 o(178,141)-s38580.
Edge: 40-105 o(79,163)-s38581.
Edge: 40-105 o(171,184)-s38582.
Edge: 40-105 o(58,188)-s38583.
Edge: 40-105 o(156,123)-s38584.
Edge: 40-105 o(91,165)-s38585.
Edge: 40-105 o(193,48)-s38586.





 */
}

package datastructures.graph.shortestPath;

import datastructures.graph.basic.GraphAdjList;
import datastructures.graph.util.GraphUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/2/13
 * Time: 9:47 AM
 * <p/>
 * In this programming problem you'll code up Dijkstra's shortest-path algorithm.
 * Download the text file here. (Right click and save link as).
 * <p/>
 * The file contains an adjacency list representation of an undirected weighted graph
 * with 200 vertices labeled 1 to 200.
 * <p/>
 * Each row consists of the node tuples that are adjacent to that particular
 * vertex along with the length of that edge.
 * <p/>
 * For example, the 6th row has 6 as the first entry indicating that
 * this row corresponds to the vertex labeled 6.
 * <p/>
 * The next entry of this row "141,8200" indicates that there is an edge
 * between vertex 6 and vertex 141 that has length 8200.
 * <p/>
 * The rest of the pairs of this row indicate the other vertices adjacent
 * to vertex 6 and the lengths of the corresponding edges.
 * <p/>
 * Your task is to run Dijkstra's shortest-path algorithm on this graph,
 * using 1 (the first vertex) as the source vertex, and to compute the
 * shortest-path distances between 1 and every other vertex of the graph.
 * <p/>
 * If there is no path between a vertex v and vertex 1, we'll define
 * the shortest-path distance between 1 and v to be 1000000.
 * <p/>
 * You should report the shortest-path distances to the following ten vertices,
 * in order: 7,37,59,82,99,115,133,165,188,197.
 * <p/>
 * You should encode the distances as a comma-separated string of integers.
 * So if you find that all ten of these vertices except 115 are at distance 1000
 * away from vertex 1 and 115 is 2000 distance away,
 * <p/>
 * then your answer should be 1000,1000,1000,1000,1000,2000,1000,1000,1000,1000.
 * <p/>
 * Remember the order of reporting DOES MATTER, and the string should be in
 * the same order in which the above ten vertices are given.
 * <p/>
 * Please type your answer in the space provided.
 * <p/>
 * <p/>
 * IMPLEMENTATION NOTES: This graph is small enough that the straightforward O(mn) time implementation of Dijkstra's algorithm should work fine. OPTIONAL: For those of you seeking an additional challenge, try implementing the heap-based version. Note this requires a heap that supports deletions, and you'll probably need to maintain some kind of mapping between vertices and their positions in the heap.
 */
public abstract class Dijkstra_Stanford_Coursera_Base {

  protected static int[] resultVertices = new int[]{7, 37, 59, 82, 99, 115, 133, 165, 188, 197};


  protected static void printAlways(String s) {
    System.err.println(s);
  }

  // default:  the naive impl
  //
  protected static Dijkstra newDjikstra(GraphAdjList graph) {
    return new Dijkstra_00_Naive(graph);
  }

  protected static void runAssignment() {
    GraphAdjList graph = readDataFile(null);

        Dijkstra dijkstra = newDjikstra(graph);

        //Dijkstra_00_Naive dijkstra = new Dijkstra_00_Naive(graph);

        // distances from vertex 1
        long start = System.currentTimeMillis();

        dijkstra.compute(1);

        long end = System.currentTimeMillis();

        long elapsed = end - start;

        printAlways("computed Dijkstra in "+elapsed+" millis. ");

        checkResult(dijkstra);
  }
  protected static void checkResult(Dijkstra dijkstra) {
    StringBuilder sb = new StringBuilder();
    StringBuilder sb2 = new StringBuilder();

    for (int i = 0; i < resultVertices.length; i++) {
      int v = resultVertices[i];
      int val = dijkstra.dist(v);
      if (val == Integer.MAX_VALUE) val = 1000000;   // no path default value

      sb.append("["+v+"]=" + val + ", ");
      sb2.append(val).append(",");
    }
    printAlways(sb.toString());
    printAlways(sb2.toString());
  }


  protected static GraphAdjList readDataFile(String inputFName) {
    //
    //  Graph of no more than 875720 vertices
    //
    //       sample input lines:
    //  1 4
    //  2 47646
    //  2 47647
    //
    long start = System.currentTimeMillis();

    GraphAdjList graph = new GraphAdjList(875720, true);


    FileReader fileR = null;
    String f = "dijkstraData.txt";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\graph\\shortestPath";
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

    // todo:  major:  need to load the edge values now !
    //        Edge data will be Integer Object

    try {
      BufferedReader br = new BufferedReader(fileR);
      String line;

      int i = 0;
      while ((line = br.readLine()) != null) {
        System.err.println("read line '" + line + "'");
        String[] s = line.split("\\s+");

        //int[] in = StringUtils.stringArrayToIntArray(s);

        GraphUtils.loadWeightedVertex(graph, s);
        //graph.loadEdges(in);
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
    long end = System.currentTimeMillis();
    printAlways("load graph elapsed time: " + (end - start) + " millis.");
    return graph;
  }

}

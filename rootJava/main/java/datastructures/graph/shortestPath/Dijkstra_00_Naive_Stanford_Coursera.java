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
 *
 * In this programming problem you'll code up Dijkstra's shortest-path algorithm.
 Download the text file here. (Right click and save link as).

 The file contains an adjacency list representation of an undirected weighted graph
 with 200 vertices labeled 1 to 200.

 Each row consists of the node tuples that are adjacent to that particular
 vertex along with the length of that edge.

 For example, the 6th row has 6 as the first entry indicating that
 this row corresponds to the vertex labeled 6.

 The next entry of this row "141,8200" indicates that there is an edge
 between vertex 6 and vertex 141 that has length 8200.

 The rest of the pairs of this row indicate the other vertices adjacent
 to vertex 6 and the lengths of the corresponding edges.

 Your task is to run Dijkstra's shortest-path algorithm on this graph,
 using 1 (the first vertex) as the source vertex, and to compute the
 shortest-path distances between 1 and every other vertex of the graph.

 If there is no path between a vertex v and vertex 1, we'll define
 the shortest-path distance between 1 and v to be 1000000.

 You should report the shortest-path distances to the following ten vertices,
 in order: 7,37,59,82,99,115,133,165,188,197.

 You should encode the distances as a comma-separated string of integers.
 So if you find that all ten of these vertices except 115 are at distance 1000
 away from vertex 1 and 115 is 2000 distance away,

 then your answer should be 1000,1000,1000,1000,1000,2000,1000,1000,1000,1000.

 Remember the order of reporting DOES MATTER, and the string should be in
 the same order in which the above ten vertices are given.

 Please type your answer in the space provided.


  IMPLEMENTATION NOTES: This graph is small enough that the straightforward O(mn) time implementation of Dijkstra's algorithm should work fine. OPTIONAL: For those of you seeking an additional challenge, try implementing the heap-based version. Note this requires a heap that supports deletions, and you'll probably need to maintain some kind of mapping between vertices and their positions in the heap.

 */
public class Dijkstra_00_Naive_Stanford_Coursera extends Dijkstra_Stanford_Coursera_Base {

  private Logger log =
          Logger.getLogger(Dijkstra_00_Naive_Stanford_Coursera.class);



  protected boolean isP() {
    return log.isDebugEnabled();
  }

  public static Dijkstra newDjikstra(GraphAdjList graph) {
    return new Dijkstra_00_Naive(graph);
  }

  public static void main (String[] args) {
    runAssignment();
  }


  /*  first attempt
  load graph elapsed time: 112 millis.
  computed Dijkstra in 11 millis.

  [v]=2599, [v]=2610, [v]=2947, [v]=2052, [v]=2367, [v]=2399, [v]=2029, [v]=2442, [v]=2505, [v]=3068,

  2599,2610,2947,2052,2367,2399,2029,2442,2505,3068

   */

}

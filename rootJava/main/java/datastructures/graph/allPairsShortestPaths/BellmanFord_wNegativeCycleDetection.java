package datastructures.graph.allPairsShortestPaths;

import datastructures.graph.basic.CloneableInteger;
import datastructures.graph.basic.Edge;
import datastructures.graph.basic.GraphAdjList;
import datastructures.graph.basic.LinkedNode;
import org.apache.log4j.Logger;

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
 *
 * This is not strictly Bellman Ford in that it does not start over
 * from the source vertex with each DP pass.
 * It also knows NOT to pursue back edges that lead to cycles.
 * This algo is optimized to do Bellman Ford via Breadth First Search
 *   (not starting over each time).
 *
 * <p/>
 * Will detect negative cycles by running past the maximum number of required vertex processing
 * (unless it stops early by detecting no changes before then)
 * <p/>
 * Some key datastructures based on ideas from Sedgewick's implementation
 */
public class BellmanFord_wNegativeCycleDetection {

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

  private Edge[] edgeTo;    // latest min edge incoming on vertex
  private Queue<Integer> vertexQueue;   // for BFS
  private boolean[] onQueue;   // is this vertex on the BFS queue ?



  private boolean foundLowerWeight;


  public BellmanFord_wNegativeCycleDetection(GraphAdjList graph) {

    // operate on a copy of the input graph
    g = graph.copy();

  }

  /**
   * @param s starting vertex
   * @return 0  compute OK
   *         -1  negative cycle detected
   */
  public int compute(int s) {
    graphVertexSize = g.getHighestVertexNumber() + 1;
    edgesInGraph = g.eCount();
    distanceToA = new int[graphVertexSize];
    distanceToB = new int[graphVertexSize];
    currIsA = true;

    for (int v = 0; v <= g.getHighestVertexNumber(); v++) {
      distanceToA[v] = Integer.MAX_VALUE;
    }

    // Bellman-Ford algorithm
    //   me:  figure out single source shortest paths from input 's'
    //            for starters 's' is the ONLY vertex on the processing queue
    //         note use of queue indicator array on vertices  'onQueue[]'
    //
    foundLowerWeight = false;
    vertexQueue = new LinkedList<Integer>();
    vertexQueue.add(s);
    onQueue[s] = true;
    while (!vertexQueue.isEmpty()) {

      //
      //  me:  pull off next to process
      //        start the main algorithm from source vertex
      //
      int v = vertexQueue.remove();
      onQueue[v] = false;
      bf(v);


    }
    return 0;
  }

  private void bf(int v) {
    //
    //  me:  process all of 'v's  *edges* (children)
    //

    LinkedNode linkedNode = g.getAdjList(v);
    while (linkedNode != null) {
      Edge edge = linkedNode.edge();
      int w = edge.w;
      int edgeWeight = ((CloneableInteger) edge.getData()).intValue();
      if (distanceToA[w] > distanceToA[v] + edgeWeight) {

        // me:  set new lower distTo value
        //
        distanceToA[w] = distanceToA[v] + edgeWeight;

        // me:   keep track of current lowest value edge contributor to child 'w'
        //
        edgeTo[w] = edge;
        foundLowerWeight = true;

        // me:   if 'w' is already on the queue
        //       then we've encountered a cycle
        //       and we avoid requeueing the cycle vertex.
        //
        //       later if we want to actually traverse
        //       cycles to detect negative cycles we
        //       have to queue up the cycles so that
        //       we can detect their effect on decreasing
        //       vertex costs *after* we have run the entire graph

        if (!onQueue[w]) {
          vertexQueue.add(w);
          onQueue[w] = true;
        }
      }


      linkedNode = linkedNode.next();
    }
    /*
     for (DirectedEdge e : G.adj(v)) {

			//  me:  'w' is the child of 'v' at the head of the edge 'e'
			//
            int w = e.to();

			//
			// me:  check the child's current 'to' incoming weight
			//        if the parent (tail) 'to' (v) distTo + the weight of the current edge
			//             is less than the current value of the child 'distTo' then adjust
			//          note that the child may have many incoming edges not just the current one.
			//
            if (distTo[w] > distTo[v] + e.weight()) {
				StdOut.println("  - successfully relaxing edge " + v + "->" + w);

				// me:  set new lower distTo value
				//
                distTo[w] = distTo[v] + e.weight();

				// me:   keep track of current lowest value edge contributor to child 'w'
				//
                edgeTo[w] = e;

				// me: if the current child 'w' is not already queued for processing
				//     then enqueue it now
				//        this is the equivalent of doing DP on the next iteration of vertices
				//     it is also the equivalent of doing BFS on the graph
				//
                if (!onQueue[w]) {
                    queue.enqueue(w);
					StdOut.println("  - enqueueing vertex " + w);
                    onQueue[w] = true;
                }
            }

			// me:  increment number of calls to algo
			//      if we've done all vertices (n - 1)
			//      then check for negative cycles, that would happen if we can run the algo
			//         past this limit and still get a lower path cost on some vertex
			//         indicating that we gone around a negative cycle somewhere
			//
            if (cost++ % G.V() == 0)
                findNegativeCycle();
        }



     */

  }

}

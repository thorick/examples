package datastructures.graph.topologicalSort;


import datastructures.graph.basic.GraphAdjList;
import datastructures.graph.basic.LinkedNode;
import datastructures.graph.stronglyConnected.GraphAdjList_Kosaraju_SCC;
import org.apache.log4j.Logger;
import utils.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/1/13
 * Time: 4:20 PM
 *
 * Straight DFS to produce Topological sort:
 *
 * dfs postorder number
 *  every directed edge points from higher number to lower number
 *
 * So the lower numbered vertices are 'depended upon'
 *
 *
 *
 */
public class GraphAdjList_TopoSort {


  private Logger log =
          Logger.getLogger(GraphAdjList_TopoSort.class);

  //
  //  The 'NOT_PROCESSED' flag must be less than the lowest possible vertex number
  //
  static int LOWEST_VERTEX = 0;
  static int NOT_PROCESSED = -1;

  boolean ran = false;
  GraphAdjList g;
  int highestVertexNumber = -1;
  int[] dfsProcessed;        // DFS track processed vertices
  int[] postOrder;           // DFS post order of dfs
  int currPostOrder;         // DFS  track current postOrder
  int componentCount;


  GraphAdjList_TopoSort(GraphAdjList g) {
    this.g = g;
    highestVertexNumber = g.getHighestVertexNumber();
        postOrder = new int[highestVertexNumber+1];
    dfsProcessed = new int[highestVertexNumber+1];
  }


  public int[] getPostorderArray() {
    if (!ran) throw new RuntimeException("Topological sort has not been performed !   run 'sort()' first.");
    return postOrder;
  }

  public void sort() {
    if (ran) throw new RuntimeException("cannot run findSCCs more than once");

      ran = true;


      if (isP()) {
        log.debug("GRAPH: \n" + g.toString());
      }

      // keeps track of the vertices found during a branch exploration of DFS
      StringBuilder dfsBranchVertices = new StringBuilder();

      if (isP())
        log.debug("GRAPH  begin dfs on vertices to " + (highestVertexNumber));

      // DFS on the graph
      currPostOrder = LOWEST_VERTEX;      //  vertex array is 1-based, not zero based
      for (int v = LOWEST_VERTEX; v <= highestVertexNumber; v++) {
        if (dfsProcessed[v] == NOT_PROCESSED) {
          log.debug("start top level dfs on [" + v + "]");

          dfs(g, v, dfsBranchVertices);
        }
      }

  }

  private void dfs(GraphAdjList g, int v, StringBuilder branchVertices) {

     if (isP()) {
       log.debug("begin dfs for vertex " + v);
     }
     dfsProcessed[v] = componentCount;
     LinkedNode n = g.getAdjList(v);

     if (isP()) {
       String nList = "EMPTY";
       if (n != null) {
         nList = n.printNodeChain();
       }
       log.debug("begin dfs for vertex [" + v + "] with target vertices " + nList);
     }

     if (n != null) {
       while (n != null) {
         if (dfsProcessed[n.vertexHeadNumber()] == NOT_PROCESSED) {
           branchVertices.append(n.vertexHeadNumber()).append(",");
           if (isP()) {
             log.debug("begin dfs on vertex [" + n.vertexHeadNumber() + "], vertices in group so far " + branchVertices.toString());
           }
           dfs(g, n.vertexHeadNumber(), branchVertices);
         }
         n = n.next();
       }
       if (isP())
         log.debug("\ndone with dfs on[" + v + "] set " + v + " at postOrder[" + currPostOrder + "]");
       postOrder[currPostOrder] = v;
       currPostOrder++;
     }
   }

  protected String printIntArray(int[] a) {
    return StringUtils.printIntArray(a);
   }

   protected boolean isP() {
     return log.isDebugEnabled();
   }


}

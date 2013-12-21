package algorithms.twoSAT.KosarajuSCC;


import datastructures.graph.basic.LinkedNode;
import org.apache.log4j.Logger;
import utils.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/28/13
 * Time: 3:19 PM
 * <p/>
 * Kosaraju's 2 pass algorithm for calculating SCCs
 * <p/>
 * <p/>
 * To use:
 * <p/>
 * 0. Load the Digraph into the GraphAdjList implementation
 * 1. Instantiate an instance of this Class with the graph
 * 2. run this.findSCCs();     this performs Kosaraju's analysis on the graph
 * 3. To get the ordered List of SCCs do  this.getSCCs()   in order of decreasing size
 *
 * 4. this.sameComponent(a, b);  To find out if vertex A and vertex B are in the same component
 *
 *
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * Assumptions:
 * the lowest graph vertex number is 1
 * if the lowest vertex number is 0,
 * then the postOrder arrays need to be initialized to -1
 * so that we can tell that the array position has not been processed
 * <p/>
 * <p/>
 *
 * This is a first and vanilla attempt at SCC
 *   could serve as a basis for more performant versions
 *   (since it basically works correctly)
 *
 */
public class Kosaraju_SCC_vertexLists {

  private Logger log =
          Logger.getLogger(Kosaraju_SCC_vertexLists.class);

  // for the assignment, we take advantage of the
  // fact that the vertices are numbered starting from '1' not '0'
  // thus we can use the JVM default int value '0' and not
  // have to explicitly initialize the dfsProcessed int array
  // before the first dfs run.
  // if the input array has a vertex 0
  // then these constants have to be altered
  //
  static int LOWEST_VERTEX = 1;
  static int NOT_PROCESSED = 0;

  boolean ran = false;
  GraphAdjList_Kosaraju_SCC_vertexLists g;
  int highestVertexNumber = -1;
  int[] dfsProcessed;        // DFS track processed vertices
  int[] postOrder;           // DFS post order of dfs
  int[] secondDFSOrder;      // DFS search order of the 2nd phase forward graph

  int currPostOrder;         // DFS  track current postOrder
  int componentCount;

  // list of the discovered SCC components
  // kept in decreasing order of size
  // do the insertions in order
  SCC_vertexLists sccList;

  public Kosaraju_SCC_vertexLists(GraphAdjList_Kosaraju_SCC_vertexLists g) {
    this.g = g;
    highestVertexNumber = g.getHighestVertexNumber();
    postOrder = new int[highestVertexNumber+1];
    secondDFSOrder = new int[highestVertexNumber+1];
    dfsProcessed = new int[highestVertexNumber+1];
    log.debug("input graph has " + highestVertexNumber + " vertices by highest numbered.");
    log.debug("constructed graph processing arrays of size " + highestVertexNumber);
  }

  public boolean sameSCC(int a, int b) {
    return dfsProcessed[a] == dfsProcessed[b];
  }

  public void findSCCs() {
    if (ran) throw new RuntimeException("cannot run findSCCs more than once");

    ran = true;
    GraphAdjList_Kosaraju_SCC_vertexLists rg = g.reverse();

    if (isP()) {
      log.debug("REVERSED GRAPH: \n" + rg.toString());
    }

    // keeps track of the vertices found during a branch exploration of DFS
    StringBuilder dfsBranchVertices = new StringBuilder();
    List<Integer> sccList = new ArrayList<Integer>();

    if (isP())
      log.debug("REVERSED GRAPH  begin dfs on vertices 1 to " + (highestVertexNumber));

    // DFS on the reverse graph starting at vertex '1' at array [1]
    componentCount = 1;
    currPostOrder = 1;      //  vertex array is 1-based, not zero based
    for (int v = LOWEST_VERTEX; v <= highestVertexNumber; v++) {
      if (dfsProcessed[v] == NOT_PROCESSED) {
        log.debug("start top level dfs on [" + v + "]");

        dfs(rg, v, dfsBranchVertices, sccList);
      }
    }

    if (isP()) {
      log.debug("DONE with GRAPH.  after dfs reverse postOrder=" + printIntArray(postOrder) + "\n\n");
    }

    // reset processed array
    for (int i = 1; i <= highestVertexNumber; i++) dfsProcessed[i] = NOT_PROCESSED;

    // transfer the order array from the reversed graph to the forward graph
    //   (postOrder array will be overwritten by the second call to DFS)

    for (int i = LOWEST_VERTEX; i <= highestVertexNumber; i++) {
      secondDFSOrder[i] = postOrder[i];
    }

    // process highest last post order, first
    // this is so that we examine the SCCs in 'sink' order
    //   thus are guaranteed that once we identify a SCC and process it
    //   via DFS, we will not look at that component again
    //   as DFS will know that we already examined those vertices
    //
    if (isP()) {
      log.debug("FORWARD GRAPH begin processing " + g.toString());
    }

    dfsBranchVertices = new StringBuilder(",");
    sccList = new ArrayList<Integer>();

    componentCount = 1;   // be sure to set the initial component count
    currPostOrder = 1;
    int stopAt = LOWEST_VERTEX - 1;
    for (int v = highestVertexNumber; v > stopAt; v--) {

      if (isP())
        log.debug("check v=" + v + ", vertex: " + secondDFSOrder[v]);
      if (dfsProcessed[secondDFSOrder[v]] == NOT_PROCESSED) {

        // well, we know that after we come out of here, the
        // SCC id will be bumped up..  so the vertex that
        // we entered dfs with must belong to an identified component

        if (isP())
          log.debug(" -- start dfs vertex: " + secondDFSOrder[v] + " - component: " + componentCount);
        dfsBranchVertices.append(secondDFSOrder[v]).append(",");
        sccList.add(secondDFSOrder[v]);

        dfs(g, secondDFSOrder[v], dfsBranchVertices, sccList);

        if (isP())
          log.debug("--   DONE dfs vertex: " + secondDFSOrder[v] + " addSCC component using input '+" +
                dfsBranchVertices.toString() + "'");

        addSCC(componentCount, g.getAdjList(secondDFSOrder[v]), dfsBranchVertices.toString(), sccList);

        if (isP()) {
          log.debug(" -- done  dfs vertex: " + secondDFSOrder[v] + " - component: " + componentCount);
          log.debug(" -- sccList is now " + sccList.toString());
        }

        componentCount++;
        dfsBranchVertices = new StringBuilder(",");
        sccList = new ArrayList<Integer>();
      }
    }
    if (isP())
      log.debug("FORWARD GRAPH  processing complete.");
  }

  public SCC_vertexLists getSCCs() {
    return sccList;
  }

  private void dfs(GraphAdjList_Kosaraju_SCC_vertexLists g, int v, StringBuilder branchVertices, List<Integer> sccList) {

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
          sccList.add(n.vertexHeadNumber());
          if (isP()) {
            log.debug("begin dfs on vertex [" + n.vertexHeadNumber() + "], vertices in group so far " + branchVertices.toString());
          }
          dfs(g, n.vertexHeadNumber(), branchVertices, sccList);
        }
        n = n.next();
      }
      if (isP())
        log.debug("\ndone with dfs on[" + v + "] set " + v + " at postOrder[" + currPostOrder + "]");
      postOrder[currPostOrder] = v;
      currPostOrder++;
    }
  }

  /**
   * Add new SCC Component to list, in order
   * largest SCC first
   *
   * @param id
   * @param head
   * @return
   */
  private SCC_vertexLists addSCC(int id, LinkedNode head, String vList, List<Integer> vIntList) {

    SCC_vertexLists c = new SCC_vertexLists(id, head, vList, vIntList);
    if (sccList == null) {
      sccList = c;
      return c;
    }
    // the new SCC belongs at the head of the list
    if (c.size >= sccList.size) {
      c.setNext(sccList);
      sccList = c;
      return c;
    }
    // keep marching along the list until we find the first
    // element that we meet or exceed, insert
    SCC_vertexLists prev = sccList;
    // todo: implement


    while (prev != null) {
      // we are always less than prev at this point
      SCC_vertexLists next = prev.next;
      if (next == null) {
        prev.setNext(c);
        return c;
      }

      // we meet or exceed next, insert before next
      if (c.size >= next.size) {
        c.setNext(next);
        prev.setNext(c);
        return c;
      }
      prev = next;
    }
    return c;
  }

  protected String printIntArray(int[] a) {
    return StringUtils.printIntArray(a);
  }

  protected boolean isP() {
    return log.isDebugEnabled();
  }


  /**
   * Holder for SCC component
   */
  public class SCC_vertexLists implements Comparable<SCC_vertexLists> {
    private final int id;   // unique identifier
    private final LinkedNode head;   // chosen head of the cycle: the 'leader'
    int size;  // number of vertices in this cycle
    String vList;  // list of vertices in this Component
    List<Integer> vIntList;  // list of vertices
    SCC_vertexLists next;     // linked

    private SCC_vertexLists(int id, LinkedNode head, String vList, List<Integer> vIntList) {
      this.id = id;
      this.head = head;     // note: for a single vertex component this will be NULL !
      this.vList = vList;
      this.vIntList = vIntList;

      // given a comma separated list of vertices in a String with
      // a trailing comma after the last vertex
      // the size of the component will be the number of commas

      int index = 1;
      while (index > 0) {
        index = vList.indexOf(",", index);
        if (index != -1) {
          size++;
          index++;
        }
      }
    }

    public LinkedNode getHead() {
      return head;
    }

    public void setNext(SCC_vertexLists s) {
      next = s;
    }

    public SCC_vertexLists next() {
      return next;
    }

    public int size() {
      return size;
    }

    public String vertexListString() {
      return vList;
    }

    public List<Integer> getVertexIntList() {
      return vIntList;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      SCC_vertexLists c = this;
      while (c != null) {
        sb.append(c.singleToString()).append("\n");
        c = c.next;
      }
      return sb.toString();
    }

    private String singleToString() {
      StringBuilder sb = new StringBuilder();
      sb.append("SCC:" + id).append(" size=").append(size).append(" vertex ");
      if (size == 1) {
        // single vertex component
        sb.append(vList);
      } else {
        sb.append(" head vertex:").
                append(head.vertexTailNumber()).append(" vertex list: ").append(vList);
      }
      return sb.toString();
    }

    public int compareTo(SCC_vertexLists other) {
      // todo: implement
      return 0;
    }
  }

}

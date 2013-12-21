package datastructures.graph.KCluster;

import datastructures.graph.basic.CloneableInteger;
import datastructures.graph.basic.Edge;
import datastructures.graph.basic.GraphAdjList;
import datastructures.graph.basic.LinkedNode;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/15/13
 * Time: 4:45 PM
 * <p/>
 * In this programming problem and the next you'll code up the clustering algorithm from lecture for computing
 * a max-spacing k-clustering. Download the text file here.
 * <p/>
 * This file describes a distance function (equivalently, a complete graph with edge costs).
 * <p/>
 * It has the following format:
 * <p/>
 * [number_of_nodes]
 * [edge 1 node 1] [edge 1 node 2] [edge 1 cost]
 * [edge 2 node 1] [edge 2 node 2] [edge 2 cost]
 * ...
 * There is one edge (i,j) for each choice of 1≤i<j≤n, where n is the number of nodes.
 * <p/>
 * For example, the third line of the file is "1 3 5250", indicating that the distance between nodes 1 and 3
 * (equivalently, the cost of the edge (1,3)) is 5250. You can assume that distances are positive,
 * but you should NOT assume that they are distinct.
 * <p/>
 * Your task in this problem is to run the clustering algorithm from lecture on this data set,
 * where the target number k of clusters is set to 4.
 * <p/>
 * What is the maximum spacing of a 4-clustering?
 * <p/>
 * <p/>
 * 500 vertices numbered 1 - 500
 * <p/>
 * <p/>
 * 0.  Read graph
 * 1.  Sort Edges by increasing weight
 * (or Heapify, no once we sort then it's just a linear scan of the *fixed* result)
 * 2.  Set up Union-Find structures
 * 3.  Loop start adding edges until you *just* make *3* groups
 * 3.1  Kruskal:  need to run DFS to check if a candidate edge would create a cycle
 * <p/>
 * 4.  Back off the last edge and put it back, you now have the fullest 4-groups that you can have
 * 5.  I think the length of the put back edge is the maximum spacing of 4-cluster ?
 * 5a.  As you keep adding edges, their lengths get greater.  To get the largest spacing keep going as long as you can.
 * <p/>
 * <p/>
 * The Cluster Groups will be identified by their leader vertex numbers.
 * A Cluster Group Class will hold metadata about the Groups (group size)
 * A Hash Lookup keyed by Group ID will provide fast lookup.
 *
 *
 *
 *
 * numVertices=500, numEdges=124750



  =====  Max 4 Cluster distance: 106

 */
public class KCluster4_Stanford_Coursera {

  private Logger log =
          Logger.getLogger(KCluster4_Stanford_Coursera.class);


  int highestVertexNumber;
  int numVertices;
  int numEdges;
  GraphAdjList graph;
  GraphAdjList MST;
  ComparableEdge[] edges;
  int[] vertexUnionLeaders;     // array of union group leaders
  Map<Integer, ClusterGroup> clusterGroupMap;  // hold all groups of size > 1   individual vertex groups not in map.
  int clusterCount;


  public KCluster4_Stanford_Coursera() { }


  public int compute(GraphAdjList g, int KCount) {
    if (KCount <= 0)  throw new RuntimeException(" Cannot ask for a KCluster of size <= 0");
    if (KCount == 1)  return 0;
    graph = g;
    highestVertexNumber = g.getHighestVertexNumber();
    numVertices = g.vCount();
    numEdges = g.eCount();
    log.debug("sanity graph vCount=" + numVertices + ", highestVertexNumber=" +
             highestVertexNumber+"..  read method count=" + numVertices);



    // remove duplicate edges from undirected graph copy
    GraphAdjList gCopy = graph.copy();

    List<Edge> edgeList = gCopy.getEdges();
    for (Edge e: edgeList) {
       int v = e.v;
      int w = e.w;
      if (gCopy.edge(v,w) && gCopy.edge(w, v)) {
        gCopy.remove(e);
        log.debug("removing from graph copy dup edge "+e);
      }
    }

    // setup sorted Edge array
    List<Edge> lEdges = gCopy.getEdges();
    if (lEdges.size() != numEdges/2) throw new RuntimeException("graph has " + lEdges.size() + ", expected=" + numEdges/2);



    edges = new ComparableEdge[numEdges/2];
    int i = 0;
    for (Edge e : lEdges) {
      edges[i++] = (ComparableEdge) e;

    }
    Arrays.sort(edges);

    log.debug(" sorted "+numEdges/2+" edges in array");

    // setup vertex union leader array
    int vertexUnionArraySize = highestVertexNumber + 1;   // assume one based vertex numbers
    vertexUnionLeaders = new int[vertexUnionArraySize];
    for (i = 0; i < vertexUnionArraySize; i++) {
      vertexUnionLeaders[i] = i;
    }

    clusterGroupMap = new HashMap<Integer, ClusterGroup>();
    MST = new GraphAdjList(vertexUnionArraySize, false);

    clusterCount = numVertices;
    int edgeIndex = 0;
    int prevEdgeWeight = Integer.MIN_VALUE;


    // run until you JUST exceed the KCount
    // then take back the edge that 'put you over'
    //   this covers throwing away short edges that would have created cycles, but were thrown away !
    while (clusterCount >= KCount) {
    //while (clusterCount > KCount) {

      log.debug("\n\n BEGIN clusterCount="+clusterCount);

      // select next smallest candidate edge
      ComparableEdge currEdge = edges[edgeIndex];
      int currEdgeWeight = ((CloneableInteger) currEdge.getData()).intValue();

      log.debug("   edge index="+edgeIndex+", current edge: "+currEdge);

      if (currEdgeWeight < prevEdgeWeight) throw new RuntimeException("ERROR !  edge[" + i + "]: " + currEdge +
              ", weight is LESS than prev edge weight=" + prevEdgeWeight);


      // do DFS check on MST for cycle, throw out if it would create cycle
      if (!dfs(currEdge.v, currEdge.w, MST)) {


        // check both edge vertices, which belongs to the larger UNION group ?
        // merge the smaller group into the LARGER
        // update clusterGroupMap entries
        int vGroupSize = 1;
        Integer vI = new Integer(currEdge.v);
        ClusterGroup vcg = clusterGroupMap.get(vI);
        if (vcg != null) {
          vGroupSize = vcg.groupSize;
        }

        int wGroupSize = 1;
        Integer wI = new Integer(currEdge.w);
        ClusterGroup wcg = clusterGroupMap.get(wI);
        if (wcg != null) {
          wGroupSize = wcg.groupSize;
        }

        int largeGroup = vI.intValue();
        int smallGroup = wI.intValue();
        if (wGroupSize > vGroupSize)  {
          largeGroup = wI;
          smallGroup = vI;
        }

        // do the group UNION including all bookkeeping
        unionSmallLarge(smallGroup, largeGroup);
        clusterCount--;

        //
        // add edge to MST undirected
        MST.insert(currEdge);
        MST.insert(new ComparableEdge(currEdge.w, currEdge.v, (CloneableInteger)currEdge.getData()));
      }
      edgeIndex++;
      prevEdgeWeight = currEdgeWeight;
    }

    // size of the next edge IS the max spacing of the 4 cluster
    //   because we built the graph picking the smallest edges possible
    //   the distances 'left over' are the largest possible.

    log.debug(" $$$$ just exceeded target K size of "+KCount+
            ", backing off 1 edge whose weight is the minimum cluster distance.");
    int finalEdgeIndex  = edgeIndex - 1;
    ComparableEdge finalEdge = edges[finalEdgeIndex];
    //ComparableEdge finalEdge = edges[edgeIndex];
    if (finalEdge == null)  throw new RuntimeException("ERROR !   no more edges left !  at edgeIndex="+edgeIndex);
    int retVal = ((CloneableInteger)finalEdge.getData()).intValue();
    log.debug(" $$$$$$  returning "+retVal);
    return retVal;
  }

  /**
   * Basic DFS to check for possible cycles IF we added an edge from u, v
   * to the input MST.
   * <p/>
   * We assume that the input MST IS always cycle free.
   * <p/>
   * MST is undirected so it doesn't matter whether we start at u or v, pick u.
   *
   * @param sourceVertex
   * @param destVertex
   * @param MST
   * @return true  if u is reachable from v  (adding edge u-v would create a cycle)
   */
  protected boolean dfs(int sourceVertex, int destVertex, GraphAdjList MST) {
    log.debug(" =========  check reachability: "+sourceVertex+"->"+destVertex);

    LinkedNode n = MST.getAdjList(sourceVertex);
    if (n == null)  return false;
    //if (n == null) throw new RuntimeException("Unexpected NULL vertex=" + u + " in MST when starting DFS ! ");
    while (n != null) {
      boolean retVal =  dfs_recurse(sourceVertex, destVertex, MST, n);
      if (retVal)  return retVal;
      n = n.next();
    }
    return false;
  }

  private boolean dfs_recurse(int sourceVertex, int destVertex, GraphAdjList MST, LinkedNode n) {

    boolean dfsDONE = false;
    boolean retVal = false;
    log.debug(" ======== dfs enter recurse for "+n.edge().v+"-"+n.edge().w+"  against source="+sourceVertex+
        ", dest="+destVertex);
    if (n.edge().w == destVertex) {
      if (n.edge().v != sourceVertex) {
      retVal = true;
      if (isP())
        log.debug(" =========  MST reachability CYCLE detected: " + n.edge() + ", returning true.");
      return retVal;
      }
    }

    LinkedNode origEdge = n;

    //LinkedNode nextLevel = MST.getAdjList(n.edge().v);
    log.debug(" ========= dfs get adjList for vertex="+n.edge().w+"  from "+n.edge().v);
    LinkedNode nextLevel = MST.getAdjList(n.edge().w);
    while (nextLevel != null) {

      // todo:  no, need to check that the next edge is NOT  a direct backtrack of how we got here !

      // reject ANY direct backtracking edge from this undirected graph back edge:
      //   if we got to this level via  A --> B   then we specifically DO NOT consider  B --> A

      // which will always exist if sourceVertex -> destVertex exists
      if (!(nextLevel.edge().v == origEdge.edge().w && nextLevel.edge().w == origEdge.edge().v))  {
      retVal = dfs_recurse(sourceVertex, destVertex, MST, nextLevel);
      if (retVal)  return retVal;
      }
      else {
        log.debug("  ==========   detected backedge "+nextLevel.edge().v+"-"+nextLevel.edge().w+", ignoring for DFS.");
      }
      nextLevel = nextLevel.next();
    }
    return false;
  }

/*   bad  wrong edge exclusion
private boolean dfs_recurse(int sourceVertex, int destVertex, GraphAdjList MST, LinkedNode n) {

    boolean dfsDONE = false;
    boolean retVal = false;
    log.debug(" ======== dfs enter recurse for "+n.toString()+",  check w="+n.edge().w+"  against source="+sourceVertex+
    "dest="+destVertex);
    if (n.edge().w == destVertex) {
      if (n.edge().v != sourceVertex) {
      retVal = true;
      if (isP())
        log.debug(" =========  MST reachability detected: " + n.edge() + ", returning true.");
      return retVal;
      }
    }

    //LinkedNode nextLevel = MST.getAdjList(n.edge().v);
    log.debug(" ========= dfs get adjList for vertex="+n.edge().w);
    LinkedNode nextLevel = MST.getAdjList(n.edge().w);
    while (nextLevel != null) {

      // todo:  no, need to check that the next edge is NOT  a direct backtrack of how we got here !

      // reject back edge:  destVertex -> sourceVertex
      // which will always exist if sourceVertex -> destVertex exists
      if (!(nextLevel.edge().v == destVertex && nextLevel.edge().w == sourceVertex))  {
      retVal = dfs_recurse(sourceVertex, destVertex, MST, nextLevel);
      if (retVal)  return retVal;
      }
      nextLevel = nextLevel.next();
    }
    return false;
  }
  */

  /*
  protected boolean dfs(int u, int v, GraphAdjList MST) {
     log.debug(" =========  check reachability: "+u+"->"+v);

     LinkedNode n = MST.getAdjList(u);
     if (n == null)  return false;
     //if (n == null) throw new RuntimeException("Unexpected NULL vertex=" + u + " in MST when starting DFS ! ");

     return dfs_recurse(v, MST, n);
   }

   private boolean dfs_recurse(int v, GraphAdjList MST, LinkedNode n) {

     boolean dfsDONE = false;
     boolean retVal = false;
     if (n.edge().w == v) {
       retVal = true;
       if (isP())
         log.debug("MST reachability detected: " + n.edge() + ", returning true.");
       return retVal;
     }
     LinkedNode currNode = n.next();
     //LinkedNode prevNode = n;
     while (currNode != null) {
       retVal = dfs_recurse(v, MST, currNode);
       if (retVal) return retVal;

       //prevNode = currNode;
       currNode = currNode.next();
     }
     return false;
   }
   */

  /**
   * Do the simple straightforward linear time union:
   *    all small leaders are converted to the large leader
   *
   * @param small
   * @param large
   */
  private void unionSmallLarge(int small, int large) {

    log.debug(" --- union small="+small+", large="+large);

    int moveCount = 0;
    for (int i=0; i<vertexUnionLeaders.length; i++) {
      if (vertexUnionLeaders[i] == small) {
        vertexUnionLeaders[i] = large;
        moveCount++;
      }
    }

    // update the large group member count
    ClusterGroup lGroup = clusterGroupMap.get(large);
    if (lGroup == null) {
      lGroup = new ClusterGroup(large, 1);
      clusterGroupMap.put(large, lGroup);
    }
    int lSize = lGroup.groupSize;
    lSize += moveCount;
    lGroup.groupSize = lSize;

    // now remove the small group from the map
    clusterGroupMap.remove(small);
  }


  protected Edge newEdge(int u, int v, CloneableInteger w) {
    return new ComparableEdge(u, v, w);
  }


  protected GraphAdjList readDataFile(String inputFName) {
    GraphAdjList graph = new GraphAdjList(501, false);

    FileReader fileR = null;
    String f = "KCluster4_clustering1_Stanford_Coursera.txt";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\KCluster";
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

      // first record is the vertex count
      line = br.readLine();
      String[] s = line.split("\\s+");

      numVertices = Integer.valueOf(s[0]);

      int i = 0;
      while ((line = br.readLine()) != null) {
        //System.err.println("read line '" + line + "'");
        s = line.split("\\s+");
        int u = Integer.valueOf(s[0]);
        int v = Integer.valueOf(s[1]);
        CloneableInteger weight = new CloneableInteger(new Integer(s[2]));
        Edge e = new Edge(u, v, weight);
        graph.insert(e);

        e = new ComparableEdge(v, u, weight);
        graph.insert(e);
        numEdges++;

        i++;
        if (i % 10 == 0) {
          //  System.err.println("read vertex #" + i + " = " + i);
        }
      }

      System.err.println("\nnumVertices=" + numVertices + ", numEdges=" + numEdges + "\n");

      br.close();
    } catch (IOException e) {
      System.err.println("exception " + e.getMessage());
    }
    return graph;
  }

  private boolean isP() {
    return log.isDebugEnabled();
  }

  class ClusterGroup {
    public int groupID;      // group leader id:  vertex number
    public int groupSize;    // number of members

    ClusterGroup(int id, int size) {
      this.groupID = id;
      this.groupSize = size;
    }


  }

  public static  void main(String[] args) {
    KCluster4_Stanford_Coursera prog = new KCluster4_Stanford_Coursera();

    GraphAdjList g = prog.readDataFile(null);
    int result = prog.compute(g, 4);

    System.err.println("\n\n =====  Max 4 Cluster distance: "+result);

  }

}

package datastructures.graph.KCluster;

import datastructures.graph.basic.CloneableInteger;
import datastructures.graph.basic.Edge;
import datastructures.graph.KCluster.LightGraphImpl;
import datastructures.graph.basic.LinkedNode;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/15/13
 * Time: 5:00 PM
 * <p/>
 * <p/>
 * In this question your task is again to run the clustering algorithm from lecture, but on a MUCH bigger graph.
 * <p/>
 * So big, in fact, that the distances (i.e., edge costs) are only defined implicitly,
 * rather than being provided as an explicit list.
 * <p/>
 * The data set is here. The format is:
 * [# of nodes] [# of bits for each node's label]
 * [first bit of node 1] ... [last bit of node 1]
 * [first bit of node 2] ... [last bit of node 2]
 * ...
 * For example, the third line of the file "0 1 1 0 0 1 1 0 0 1 0 1 1 1 1 1 1 0 1 0 1 1 0 1"
 * denotes the 24 bits associated with node #2.
 * <p/>
 * The distance between two nodes u and v in this problem is defined as
 * <p/>
 * the Hamming distance--- the number of differing bits --- between the two nodes' labels.
 * <p/>
 * For example, the Hamming distance between the 24-bit label of node #2 above
 * and the label "0 1 0 0 0 1 0 0 0 1 0 1 1 1 1 1 1 0 1 0 0 1 0 1"
 * <p/>
 * is 3 (since they differ in the 3rd, 7th, and 21st bits).
 * <p/>
 * The question is: what is the largest value of k such that there is a k-clustering with
 * spacing at least 3?
 * <p/>
 * That is, how many clusters are needed to ensure that no pair of nodes with all but 2 bits in common get
 * <p/>
 * split into different clusters?
 * <p/>
 * NOTE: The graph implicitly defined by the data file is so big that you probably can't write it out explicitly,
 * let alone sort the edges by cost. So you will have to be a little creative to complete this part of the question.
 * <p/>
 * For example, is there some way you can identify the smallest distances
 * without explicitly looking at every pair of nodes?
 * <p/>
 * <p/>
 * 200000 24
 * 1 1 1 0 0 0 0 0 1 1 0 1 0 0 1 1 1 1 0 0 1 1 1 1
 *
 *


 -- clusterCount=6118, clusterGroupMapValue size=6118


  =====  Min Cluster distance: 3, KCluster k=6118


 */
public class KCluster3_HammingDistance_Stanford_Coursera {

  private Logger log =
          Logger.getLogger(KCluster3_HammingDistance_Stanford_Coursera.class);

  // timing stuff
  private long startAll;

  private long unionStart;
  private long unionEnd;

  // 1 Map for each 'missing bit pair' combination
  //  which are the 'missing bit pairs' is implicit in the array ordering
  private Map<Integer, List<Integer>>[] missingBitsVertexMaps;


  private int maxWeight;   // max weight of clustered vertices  (one less than cluster distance)
  private int labelSize;
  private HammingVertex[] vertices;

  int[] vertexUnionLeaders;     // array of union group leaders
  LightGraphImpl MST;
  Map<Integer, ClusterGroup> clusterGroupMap;  // hold all groups of size > 1   individual vertex groups not in map.
  int clusterCount;
  ComparableEdge[] candidateEdges;   // Kruskal Algorithm driver


  // alternate:  the 'missing' 2 bits match up
  //   a key that consists of:  a code for the missing 2 bit positions +
  //      the compacted vertex label WITHOUT the missing 2 bits
  //   values:  list of vertices that match that bit pattern !
  //
  // so when we have a candidate vertex
  //   we try looking up all the 'missing 2 bit' keys for this candidate
  //   any hits are either:  match, diff1 or diff2
  //
  public KCluster3_HammingDistance_Stanford_Coursera() {

  }

  /**
   * @param v The array of Hamming Vertices
   *          The array MUST contain exactly the vertices in the graph no more no less.
   * @return
   */
  public int compute(HammingVertex[] v, int clusteredWeight) {
    labelSize = 24;
    maxWeight = clusteredWeight;
    vertices = v;

    startAll = System.currentTimeMillis();

    missingBitsVertexMaps = new HashMap[280];

    fillLabelHash();

    long time = System.currentTimeMillis();
    time = (time - startAll) / 1000;
    System.err.println("  !!!!!!!!!!   fillLabelHash  done at " + time + " s.");


    // now we are set to use Kruskal to build up an MST until we have complete groups of edge weight up to 2
    // the edge that puts us up to 3 is 'backed off'
    //  the group count is what we are after

    clusterGroupMap = new HashMap<Integer, ClusterGroup>();
    //
    // setup vertex union leader array
    int vertexUnionArraySize = vertices.length;   // assume one based vertex numbers
    vertexUnionLeaders = new int[vertexUnionArraySize];
    for (int i = 0; i < vertexUnionArraySize; i++) {
      vertexUnionLeaders[i] = i;
      clusterGroupMap.put(i, new ClusterGroup(i, 1));
    }

    // sorted edges distances 0 - maxWeight
    candidateEdges = fillEdgeArray(maxWeight);

    if (isP()) {
      log.debug(" ");
      log.debug(" ===== sorted candidate edges ====");
      for (int i = 0; i < candidateEdges.length; i++) {
        log.debug(i + ": " + candidateEdges[i]);
      }
      log.debug(" ===== sorted candidate edges  END ====");
    }
    time = System.currentTimeMillis();
    time = (time - startAll) / 1000;
    System.err.println("  !!!!!!!!!!   fillEdgeArray  done at " + time + " s.");


    MST = new LightGraphImpl(vertexUnionArraySize);


    clusterCount = vertexUnionArraySize;

    System.err.println("-- starting cluster count = " + clusterCount);

    int edgeIndex = 0;
    int hammingDistance = 0;


    // we've pre-selected all the required edges, in increasing weight order
    // now we just have to process them all
    for (int i = 0; i < candidateEdges.length; i++) {
      ComparableEdge currEdge = candidateEdges[i];

        // lookup v's group
        int vGroup = vertexUnionLeaders[currEdge.v];
        int wGroup = vertexUnionLeaders[currEdge.w];
        log.debug(" @@@@@  v=" + currEdge.v + " vGroup=" + vGroup + ", w=" + currEdge.w + " wGroup=" + wGroup);


      // if v and w belong to the same group,
      // then their edge is contained in the same tree
      //   including this edge would create a cycle, so skip it !
      if (vGroup != wGroup) {
      // do DFS check on MST for cycle, throw out if it would create cycle
      //if (!dfs(currEdge.v, currEdge.w, MST)) {


        // todo:  you have to look up what GROUP each vertex belongs to !
        // todo:  then you merge THOSE GROUPS !


        // check both edge vertices, which belongs to the larger UNION group ?
        // merge the smaller group into the LARGER
        // update clusterGroupMap entries
        int vGroupSize = 1;
        Integer vI = new Integer(vGroup);
        //Integer vI = new Integer(currEdge.v);
        ClusterGroup vcg = clusterGroupMap.get(vI);
        if (vcg != null) {
          vGroupSize = vcg.groupSize;
        }

        int wGroupSize = 1;
        Integer wI = new Integer(wGroup);
        //Integer wI = new Integer(currEdge.w);
        ClusterGroup wcg = clusterGroupMap.get(wI);
        if (wcg != null) {
          wGroupSize = wcg.groupSize;
        }

        int largeGroup = vI.intValue();
        int smallGroup = wI.intValue();
        int largeGroupSize = vGroupSize;
        int smallGroupSize = wGroupSize;
        if (wGroupSize > vGroupSize) {
          largeGroup = wI;
          smallGroup = vI;
          largeGroupSize = wGroupSize;
          smallGroupSize = vGroupSize;
        }

        if (clusterGroupMap.get(largeGroup) == null) {
          if (largeGroupSize == smallGroupSize) {
            if (isP()) {
              log.debug(" @@@@@ detected missing largeGroupID=" + largeGroup +
                      " with smallGroupID=" + smallGroup + " equal groupSizes=" + largeGroupSize +
                      ", swapping to largeGroupID=" + smallGroup);
            }
            int temp = largeGroup;
            largeGroup = smallGroup;
            smallGroup = temp;
          }
        }

        // do the group UNION including all bookkeeping
        if (isP()) {
          if (largeGroup == 0 || largeGroup == 5 || smallGroup == 0 || smallGroup == 5) {
            log.debug(" @@@@@@@@@@@@@  UNION on smallID=" + smallGroup + " smallSize=" +
                    smallGroupSize + ", largeID=" + largeGroup + " largeSize=" + largeGroupSize);

            if (clusterGroupMap.get(smallGroup) == null) log.debug(" @@@ THERE IS NO GROUP=" + smallGroup);
            if (clusterGroupMap.get(largeGroup) == null) log.debug(" @@@ THERE IS NO GROUP=" + largeGroup);
          }
        }

        // hack to get around problem doing dfs on bidirectional edges:
        //   if the large group doesn't exist
        //   then don't do the move
        //  this is happening when we fail to handle a reverse edge that we've already catalogued.
        //
        if (clusterGroupMap.get(largeGroup) != null) {
          unionSmallLarge(smallGroup, largeGroup);
          clusterCount--;

          if (clusterCount % 100 == 0) {
            System.err.println(" --- cluster count is now " + clusterCount + ", 1/100 sec dfsTime=" + ((dfsEnd - dfsStart) / 10) +
                    ", unionTime=" + ((unionEnd - unionStart) / 10));
          }
          log.debug(" --- cluster count is now " + clusterCount + "\n");
        } else {
          log.debug(" ==============  CANNOT FIND large group " + largeGroup + " !!! \n\n");
        }
        //
        // add edge to MST undirected
        MST.insert(currEdge);
        MST.insert(new ComparableEdge(currEdge.w, currEdge.v, (CloneableInteger) currEdge.getData()));
      }


    }

    /*
    Collection<ClusterGroup> groups = clusterGroupMap.values();
    System.err.println("\n\n ---  there are "+groups.size()+" cluster groups:");
    for (ClusterGroup cg : groups) {
      System.err.println(cg.toString());
    }
    System.err.println("\n\n");
    */

    System.err.println("\n\n");
    System.err.println(" -- vertex group leaders: " + printVertexUnionLeaders() + "\n");
    System.err.println(printGroups());
    System.err.println("\n\n");
    System.err.println(" -- clusterCount=" + clusterCount + ", clusterGroupMapValue size=" + clusterGroupMap.values().size());
    // OK, we're done.
    //  how many groups do we have ?
    //  that's the answer
    return clusterCount;
  }

  /**
   * Go through ALL of the vertices
   * for each possible combination of 'missing 2 bits' add an entry
   * to the missingBitsLabelHash.
   * <p/>
   * The 2 missing bits start at 0,1   RHS marches to the end, then LHS starts at 1, RHS marches from 2, etc..
   */
  void fillLabelHash() {
    log.debug(" begin fillLabelHash");
    for (int v = 0; v < vertices.length; v++) {
      log.debug(" ----  process vertex " + v);
      System.err.println(" ----  process vertex " + v);

      HammingVertex vertex = vertices[v];

      // all users of the array MUST use exactly the same algorithm lookup sequence
      int missingBitsVertexMapsIndex = 0;

      for (int i = 0; i < (labelSize - 1); i++) {
        for (int j = (i + 1); j < labelSize; j++) {

          Map missingBitsVertexMap = missingBitsVertexMaps[missingBitsVertexMapsIndex];
          if (missingBitsVertexMap == null) {
            missingBitsVertexMap = new HashMap<Integer, List<Integer>>();
            missingBitsVertexMaps[missingBitsVertexMapsIndex] = missingBitsVertexMap;
          }
          int compressedLabel = vertex.labelWithoutMissingBitsAsInt(i, j);
          List<Integer> list = (List<Integer>) missingBitsVertexMap.get(compressedLabel);
          if (list == null) {
            list = new LinkedList<Integer>();
            missingBitsVertexMap.put(compressedLabel, list);
          }
          list.add(vertex.vertexNumber);

          // next missing bit hashMap in sequence
          missingBitsVertexMapsIndex++;
        }
      }
    }
  }

  //
  //  Fill array of candidate edges of weight maxWeight or less
  //
  ComparableEdge[] fillEdgeArray(int maxWeight) {
    Map<String, ComparableEdge> edgeMap = new HashMap<String, ComparableEdge>();

    // process all vertex combinations so that we get the complete set
    // the edge hash that we constructed narrows the candidates for each
    // label pattern saving us from checking all possible edges.

    // for each vertex:
    //   look up the hashKey for each possible 'missing bit' pattern
    //   examine all matching candidates 'other vertices'
    //   create a ComparableEdge for each vertex at distance <= maxWeight
    //   add the ComparableEdges to the edgeList
    //
    // after processing all vertices:
    //   copy the edgeList to an Array and sort the Array.
    //   the Array is ready to drive the Kruskal Algorithm.
    //

    for (int i = 0; i < vertices.length; i++) {
      HammingVertex v = vertices[i];
      if (v == null) continue;    // should not happen

      log.debug(" **** vertex " + i);
      System.err.println(" **** vertex " + i);

      // all users of the array MUST use exactly the same algorithm lookup sequence
      int missingBitsVertexMapsIndex = 0;

      // check all 'missing 2 bit' combinations for this vertex
      for (int j = 0; j < (labelSize - 1); j++) {
        for (int k = (j + 1); k < labelSize; k++) {
          int missingBitsKey = v.labelWithoutMissingBitsAsInt(j, k);

          Map missingBitsVertexMap = missingBitsVertexMaps[missingBitsVertexMapsIndex];

          List<Integer> l = (List<Integer>) missingBitsVertexMap.get(missingBitsKey);
          addQualifedEdgesFromVertexList(v, l, edgeMap, 2);

          // next missing bit hashMap in sequence
          missingBitsVertexMapsIndex++;
        }
      }
    }

    //ComparableEdge[] e = (ComparableEdge[])edgeList.toArray();
    Collection<ComparableEdge> edgeSet = edgeMap.values();
    int numEdges = edgeSet.size();
    ComparableEdge[] e = new ComparableEdge[numEdges];
    int i = 0;
    for (ComparableEdge edge : edgeSet) {
      e[i] = edge;
      i++;
    }
    Arrays.sort(e);

    if (isP())
      log.debug(" --- there are " + e.length + " sorted edges.");
    return e;
  }


  //  Fill the toList with all edges that are a Hamming Distance
  // of maxDistance or less.
  //

  void addQualifedEdgesFromVertexList(HammingVertex v,
                                      List<Integer> fromList,  // list of Vertex indices
                                      Map<String, ComparableEdge> toMap, int maxDistance) {
    for (Integer candidateVertexNumber : fromList) {
      HammingVertex candidate = vertices[candidateVertexNumber];
      int dist = v.computeDistance(candidate);
      if (dist <= maxDistance) {
        // do not consider self edges
        // they are already accounted for in the starting group count == vertex count
        if (v.vertexNumber == candidate.vertexNumber) continue;

        String key = ComparableEdge.createKey(v.vertexNumber, candidate.vertexNumber);
        if (!(toMap.containsKey(key))) {
          ComparableEdge e =
                  new ComparableEdge(v.vertexNumber, candidate.vertexNumber, new CloneableInteger(dist));
          toMap.put(key, e);

        } else {
          if (isP()) {
            //log.debug(" ---  skip add of dup edge for key " + key);
          }
        }
      }
    }
  }


  /**
   * Do the simple straightforward linear time union:
   * all small leaders are converted to the large leader
   *
   * @param small
   * @param large
   */

  private void unionSmallLarge(int small, int large) {
    unionStart = System.currentTimeMillis();

    log.debug(" --- union smallID=" + small + ", largeID=" + large);

    int moveCount = 0;
    for (int i = 0; i < vertexUnionLeaders.length; i++) {
      if (vertexUnionLeaders[i] == small) {


        log.debug(" changing " + i + " from old smallID=" + small + " to new largeD=" + large);
        vertexUnionLeaders[i] = large;
        moveCount++;
      }
    }

    // update the large group member count
    ClusterGroup lGroup = clusterGroupMap.get(large);
    if (lGroup == null) {
      lGroup = new ClusterGroup(large, 1);
      clusterGroupMap.put(large, lGroup);
      log.debug(" created new ClusterGroup for largeID=" + large + ", size=1");
    }
    int lSize = lGroup.groupSize;
    log.debug(" old group size=" + lSize + ", about to add smallID size=" + moveCount);
    lSize += moveCount;
    lGroup.groupSize = lSize;
    log.debug(" groupID=" + lGroup.groupID + " size=" + lSize);

    // now remove the small group from the map
    clusterGroupMap.remove(small);
    unionEnd = System.currentTimeMillis();
  }


  protected Edge newEdge(int u, int v, CloneableInteger w) {
    return new ComparableEdge(u, v, w);
  }

  String printVertexUnionLeaders() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < vertexUnionLeaders.length; i++) {
      sb.append(i + "=" + vertexUnionLeaders[i] + ", ");
    }
    return sb.toString();
  }

  String printGroups() {
    StringBuilder sb = new StringBuilder();
    Collection<ClusterGroup> groups = clusterGroupMap.values();
    sb.append(" ---  there are " + groups.size() + " cluster groups:\n");
    for (ClusterGroup cg : groups) {
      int currGroupLeader = cg.groupID;
      sb.append(cg.toString()).append("  members: {");
      for (int i = 0; i < vertexUnionLeaders.length; i++) {
        if (vertexUnionLeaders[i] == currGroupLeader)
          sb.append(i + ",");
      }
      sb.append("}\n");
    }
    return sb.toString();
  }


  protected HammingVertex[] readDataFile(String inputFName) {
    HammingVertex[] retVal = null;
    int numVertices;
    int bitsPerLabel;

    FileReader fileR = null;
    String f = "KCluster3_HammingDistance_clustering_big_Stanford_Coursera.txt";
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

      // first record is 1: number of vertices  2: bits per vertex label
      line = br.readLine();
      String[] s = line.split("\\s+");

      numVertices = Integer.valueOf(s[0]);
      bitsPerLabel = Integer.valueOf(s[1]);

      retVal = new HammingVertex[numVertices];

      int i = 0;
      while ((line = br.readLine()) != null) {
        //System.err.println("read line '" + line + "'");
        HammingVertex v = new HammingVertex(i, line);
        retVal[i] = v;
        i++;
        if (i % 10 == 0) {
          //  System.err.println("read vertex #" + i + " = " + i);
        }
      }

      System.err.println("\nnumVertices=" + numVertices + "\n");

      br.close();
    } catch (IOException e) {
      System.err.println("exception " + e.getMessage());
    }
    return retVal;
  }


  private boolean isP() {
    return log.isDebugEnabled();
  }

  /**
   * String of 0's and 1's to an integer
   *
   * @param s
   * @return
   */
  public static int stringToBits(String s) {
    if (s.length() > 32) throw new RuntimeException("Cannot handle more than 32 bits.  input='" + s + "'");

    int retVal = 0;
    for (int i = 0; i <= (s.length() - 1); i++) {
      if (s.charAt(i) == '1') {
        retVal = retVal + 1;
      }
      if (i < (s.length() - 1))
        retVal <<= 1;
    }
    return retVal;
  }


  public static void main(String[] args) {
    KCluster3_HammingDistance_Stanford_Coursera prog =
            new KCluster3_HammingDistance_Stanford_Coursera();

    HammingVertex[] graph = prog.readDataFile(null);
    int minClusterDistance = 3;
    int result = prog.compute(graph, (minClusterDistance - 1));

    System.err.println("\n\n  =====  Min Cluster distance: " + minClusterDistance + ", KCluster k=" + result);

  }


  class HammingVertex {
    public int vertexNumber;
    public String vertexLabel;
    public int vertexLabelInt;

    HammingVertex(int v, String l) {
      vertexNumber = v;
      vertexLabel = removeBlanks(l);

      // convert label String to int representation
      vertexLabelInt = stringToBits(vertexLabel);
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof HammingVertex)) return false;

      if (vertexNumber == ((HammingVertex) other).vertexNumber) return true;
      return false;
    }

    @Override
    public String toString() {
      return vertexNumber + "='" + vertexLabel + "'";
    }

    public int labelAsInt() {
      return vertexLabelInt;
    }

    public int computeDistance(HammingVertex other) {
      int iOther = other.labelAsInt();
      int distance = 0;
      int mask = 1;
      for (int i = 0; i < 32; i++) {
        if (!((vertexLabelInt & mask) == (iOther & mask))) {
          distance++;
        }
        mask <<= 1;
      }
      return distance;
    }


    // for missing bits 5 and 11 the key is:
    //   "5-11:20394"    5 = missing bit 5, 11 = missing bit 11, number is value with bits removed and
    //                     binary number compacted
    //
    // first bit position is 0
    //
    public String bitKey(int bit1, int bit2) {
      int first = bit1;
      int second = bit2;
      if (bit1 > bit2) {
        first = bit2;
        second = bit1;
      }
      return bitKeyPrefix(first, second) + ":" + labelWithoutMissingBits(first, second);
    }

    public String bitKeyPrefix(int first, int second) {
      return Integer.toString(first) + "-" + Integer.toString(second);
    }

    public String labelWithoutMissingBits(int first, int second) {
      if (first == second || first < 0 || second < 0)
        throw new RuntimeException("Sorry bits must be distinct and >= 0");

      /*
      int first = bit1;
      int second = bit2;
      if (bit1 > bit2) {
        first = bit2;
        second = bit1;
      }
      */

      String retVal = "";
      if (first == 0) {
        // cutout the leading bit only
        //  NO String !
        //retVal = vertexLabel.substring(1);
      } else {
        retVal = vertexLabel.substring(0, first);
      }

      if (second == (first + 1)) {
        // there's only 1 piece left
        retVal = retVal + vertexLabel.substring(second + 1);
      } else {
        // need to chop the 2 remaining pieces
        retVal = retVal + vertexLabel.substring(first + 1, second);
        if (second < vertexLabel.length() + 1) {
          retVal = retVal + vertexLabel.substring(second + 1);
        } else {
          // second is at the end of the String so no action since we've already chopped it off
        }
      }
      return retVal;
    }

    public int labelWithoutMissingBitsAsInt(int first, int second) {
      String sVal = labelWithoutMissingBits(first, second);
      return stringToBits(sVal);
    }


    private String removeBlanks(String a) {
      while (a.indexOf(" ") != -1) {
        int index = a.indexOf(" ");
        a = a.substring(0, index) + (index >= (a.length() + 1) ? "" : a.substring(index + 1));
      }
      return a;
    }
  }

  class ClusterGroup {
    public int groupID;      // group leader id:  vertex number
    public int groupSize;    // number of members

    ClusterGroup(int id, int size) {
      this.groupID = id;
      this.groupSize = size;
    }

    @Override
    public String toString() {
      return "groupID=" + groupID + ", groupSize=" + groupSize;
    }
  }
}

package algorithms.twoSAT.KosarajuSCC;

import datastructures.graph.basic.Edge;
import datastructures.graph.basic.LinkedNode;
import algorithms.twoSAT.KosarajuSCC.GraphAdjList_Kosaraju_SCC_vertexLists;
import algorithms.twoSAT.KosarajuSCC.Kosaraju_SCC_vertexLists;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 10/14/13
 * Time: 9:06 PM
 *
 *
 * sat1:
 *
 */
public class KosarajuSCC_2SAT {

  private Logger log =
          Logger.getLogger(KosarajuSCC_2SAT.class);

  protected int numPairs;
  protected int[] pairs;
  protected int lowestVar;
  protected int highestVar;
  protected int graphVertexOffset = 0;  // add this to var to get graph vertex #

  protected int[] unionFind;   // for x and !x  same SCC detection  use graph vertex indices

  protected GraphAdjList_Kosaraju_SCC_vertexLists graph;
  public KosarajuSCC_2SAT() {

  }

  /**
   * Compute satisfiability of the input (x  V  y) condition pairs
   *
   * @return
   */
  public boolean compute() {

    graph = createGraph();

    Kosaraju_SCC_vertexLists kSCC = new Kosaraju_SCC_vertexLists(graph);
    log.debug("created Kosaraju_SCC instance");

    kSCC.findSCCs();
    log.debug("Kosaraju_SCC.findSCCs complete.");

    Kosaraju_SCC_vertexLists.SCC_vertexLists sccChain = kSCC.getSCCs();
    int sccCount = 0;
    while (sccChain != null) {
      sccCount++;
      sccChain = sccChain.next();
    }

    log.debug("found "+sccCount+" SCCs.");

    sccChain = kSCC.getSCCs();
    return checkSCCs(sccChain);
    //
    // return false;
  }

  public void setPairs(int[] p) {
    pairs = p;
    if (p.length % 2  != 0)  throw new RuntimeException("input contains an odd number of variables, must be even to create complete set of pairs !");

    numPairs = p.length/2;
    lowestVar = Integer.MAX_VALUE;
    highestVar = Integer.MIN_VALUE;
    for (int i=0; i<pairs.length; i++) {
      if (pairs[i] < lowestVar) lowestVar = pairs[i];
      if (pairs[i] > highestVar) highestVar = pairs[i];
    }
    if (lowestVar < 0) {
      graphVertexOffset = -lowestVar;
      // be sure to pick the highest magnitude value for the offset so that there is room for +/- extremes
      if (graphVertexOffset < highestVar) {
        graphVertexOffset = highestVar;
      }
    }
    else {
      if (isP()) {
        log.debug(" input set has no negated variables !");
      }
    }
    if (isP()) {
      log.debug(" number of var references="+pairs.length+
      ", lowest var="+lowestVar+", highest var="+highestVar+
      ", graphVertexOffset="+ graphVertexOffset);
    }
  }

  /**
   * check each SCC in the chain
   * if any of them contain both a variable AND it's complement
   * then we FAIL 2SAT
   *
   * fail fast:  quit the moment that we detect this condition
   *
   * fixed SCCs.  perfect for a simple union find
   *    requiring no indirection to handle new inserts !
   *
   *
   * within each SCC
   *   set the SCC membership leader vertex
   *
   *   for each graph vertex in the SCC:
   *     transform to the actual vertex
   *     check to see if the negated vertex has been seen in the this SCC (check the union-find array for a matching leader)
   *
   *     FAIL immediately if one is found !
   *
   *
   * @param sccChain
   * @return
   */
  protected boolean checkSCCs(Kosaraju_SCC_vertexLists.SCC_vertexLists sccChain) {
    unionFind = new int[pairs.length];   // indexed by the adjusted vertex.  value = leader number (an adjusted vertex)
    for (int i=0; i<unionFind.length; i++) {
      unionFind[i] = Integer.MIN_VALUE;
    }

    while (sccChain != null) {
      List<Integer> vList = sccChain.getVertexIntList();

      int leader = vList.get(0);

      /*
      LinkedNode ln = sccChain.getHead();
      if (ln == null) {
        log.debug(" got NULL sccChain.getHead !");
      }
      int leader = sccChain.getHead().vertexHeadNumber();
      */

      for (Integer i: vList) {
        int thisAdjustedVertex = i;
        int thisActualVertex = thisAdjustedVertex - graphVertexOffset;
        int otherActualVertex = -thisActualVertex;
        int otherAdjustedVertex = otherActualVertex + graphVertexOffset;

        if (unionFind[otherAdjustedVertex] == leader) {
          // found collision !  of  x  and  !x  !
          return false;
        }

        // OK now set this vertex' leader !
        unionFind[thisAdjustedVertex] = leader;
      }
      sccChain = sccChain.next();
    }
    return true;
  }


  protected GraphAdjList_Kosaraju_SCC_vertexLists createGraph() {
    GraphAdjList_Kosaraju_SCC_vertexLists graph = new GraphAdjList_Kosaraju_SCC_vertexLists(pairs.length*2, true);

    for (int i=0; i<numPairs; i++) {
      int f = getPairIndex(i);
      int first = pairs[f++];
      int second = pairs[f];
      int notFirst = -first;
      int notSecond = -second;
      first = first + graphVertexOffset;
      second = second + graphVertexOffset;
      notFirst = notFirst + graphVertexOffset;
      notSecond = notSecond + graphVertexOffset;

      Edge e = new Edge(notFirst, second);
      graph.insert(e);
      e = new Edge(notSecond, first);
      graph.insert(e);
    }
    if (isP()) {
      log.debug(" graph created with "+graph.eCount()+" edges.");
    }
    return graph;
  }

  /**
   * get index of first entry of pair #:  0 is the first pair
   *
   * @param pairNumber
   * @return
   */
  protected int getPairIndex(int pairNumber) {
    return pairNumber + pairNumber;
  }

  /**
   * read in clause pairs as signed integer pairs in a single array
   *
   * 2sat1.txt:
   *  num pairs=100000
   min number=-100000, max number=100000
    read 200000 entries.



   * @param inputFName
   * @return
   */
  protected int[] readDataFile(String inputFName) {
    int[] retVal = null;

    FileReader fileR = null;
    String f = "2sat1.txt";
    //String f = "2sat2.txt";
    //String f = "2sat3.txt";
    //String f = "2sat4.txt";
    //String f = "2sat5.txt";
    //String f = "2sat6.txt";




    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\algorithms\\twoSAT\\CourseraAlgo2";

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

      line = br.readLine();
      int numPairs = Integer.valueOf(line);

      // hack adjust arraySize for unbalanced vertex inputs
      // assumed to be small
      // that is, for a vertex -x say, there is no corresponding +x
      //
      int arraySize = numPairs + numPairs + 100;
      retVal = new int[arraySize];

      System.err.println("input: "+f+",  num pairs=" + numPairs);

      int max = Integer.MIN_VALUE;
      int min = Integer.MAX_VALUE;
      int i = 0;
      while ((line = br.readLine()) != null) {
        //System.err.println("read line '" + line + "'");
        String[] s = line.split("\\s+");
        int x = Integer.valueOf(s[0]);
        int y = Integer.valueOf(s[1]);
        retVal[i++] = x;
        retVal[i++] = y;
        if (x > max) max = x;
        if (y > max) max = y;
        if (x < min) min = x;
        if (y < min) min = y;
      }
      System.err.println("min number="+min+", max number="+max);
      br.close();

      System.err.println(" read " + i + " entries.");
    } catch (IOException e) {
      System.err.println("exception " + e.getMessage());
    }
    return retVal;
  }

  private boolean isP() {
    return log.isDebugEnabled();
  }

  public static void main(String[] args) {
    KosarajuSCC_2SAT prog = new KosarajuSCC_2SAT();

    int[] pairs = prog.readDataFile(null);
    prog.setPairs(pairs);
    boolean retVal = prog.compute();
    System.err.println(" result pass ? "+retVal);

  }

  /**
   *
   input: 2sat1.txt,  num pairs=100000
   min number=-100000, max number=100000
    read 200000 entries.
   KosarajuSCC_2SAT,main:96 -  number of var references=200100, lowest var=-100000, highest var=100000, graphVertexOffset=100000
   KosarajuSCC_2SAT,main:185 -  graph created with 200100 edges.
   KosarajuSCC_2SAT,main:52 - created Kosaraju_SCC instance
   KosarajuSCC_2SAT,main:55 - Kosaraju_SCC.findSCCs complete.
   KosarajuSCC_2SAT,main:64 - found 126499 SCCs.
    result pass ? true


   input: 2sat2.txt,  num pairs=200000
   min number=-200000, max number=200000
    read 400000 entries.
   KosarajuSCC_2SAT,main:96 -  number of var references=400100, lowest var=-200000, highest var=200000, graphVertexOffset=200000
   KosarajuSCC_2SAT,main:185 -  graph created with 400100 edges.
   KosarajuSCC_2SAT,main:52 - created Kosaraju_SCC instance
   KosarajuSCC_2SAT,main:55 - Kosaraju_SCC.findSCCs complete.
   KosarajuSCC_2SAT,main:64 - found 252958 SCCs.
    result pass ? false



   input: 2sat3.txt,  num pairs=400000
   min number=-400000, max number=399999
    read 800000 entries.
   KosarajuSCC_2SAT,main:96 -  number of var references=800100, lowest var=-400000, highest var=399999, graphVertexOffset=400000
   KosarajuSCC_2SAT,main:185 -  graph created with 800100 edges.
   KosarajuSCC_2SAT,main:52 - created Kosaraju_SCC instance
   KosarajuSCC_2SAT,main:55 - Kosaraju_SCC.findSCCs complete.
   KosarajuSCC_2SAT,main:64 - found 504779 SCCs.
    result pass ? true



   input: 2sat4.txt,  num pairs=600000
   min number=-599999, max number=600000
    read 1200000 entries.
   KosarajuSCC_2SAT,main:96 -  number of var references=1200100, lowest var=-599999, highest var=600000, graphVertexOffset=600000
   KosarajuSCC_2SAT,main:185 -  graph created with 1200100 edges.
   KosarajuSCC_2SAT,main:52 - created Kosaraju_SCC instance
   KosarajuSCC_2SAT,main:55 - Kosaraju_SCC.findSCCs complete.
   KosarajuSCC_2SAT,main:64 - found 758241 SCCs.
    result pass ? true


   input: 2sat5.txt,  num pairs=800000
   min number=-799999, max number=799996
    read 1600000 entries.
   KosarajuSCC_2SAT,main:96 -  number of var references=1600100, lowest var=-799999, highest var=799996, graphVertexOffset=799999
   KosarajuSCC_2SAT,main:185 -  graph created with 1600100 edges.
   KosarajuSCC_2SAT,main:52 - created Kosaraju_SCC instance
   KosarajuSCC_2SAT,main:55 - Kosaraju_SCC.findSCCs complete.
   KosarajuSCC_2SAT,main:64 - found 1011549 SCCs.
    result pass ? false


   input: 2sat6.txt,  num pairs=1000000
   min number=-999999, max number=999999
    read 2000000 entries.
   KosarajuSCC_2SAT,main:96 -  number of var references=2000100, lowest var=-999999, highest var=999999, graphVertexOffset=999999
   KosarajuSCC_2SAT,main:185 -  graph created with 2000100 edges.
   KosarajuSCC_2SAT,main:52 - created Kosaraju_SCC instance
   KosarajuSCC_2SAT,main:55 - Kosaraju_SCC.findSCCs complete.
   KosarajuSCC_2SAT,main:64 - found 1263802 SCCs.
    result pass ? false


   *
   */
}

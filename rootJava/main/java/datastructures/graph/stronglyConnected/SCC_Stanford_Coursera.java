package datastructures.graph.stronglyConnected;

import datastructures.graph.basic.GraphAdjList;
import utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


import org.apache.log4j.Logger;



/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/27/13
 * Time: 7:39 PM
 *
 *  The file contains the edges of a directed graph.
 *
 *  Vertices are labeled as positive integers from 1 to 875714.
 *
 *  Every row indicates an edge, the vertex label in first column is the tail
 *  and the vertex label in second column is the head (recall the graph is directed,
 *  and the edges are directed from the first column vertex to the second column vertex).
 *
 *  So for example, the 11th row looks liks : "2 47646".
 *
 *  This just means that the vertex with label 2 has an outgoing edge to the
 *  vertex with label 47646

 Your task is to code up the algorithm from the video lectures for
 computing strongly connected components (SCCs), and to run this algorithm
 on the given graph.

 Output Format: You should output the sizes of the 5 largest SCCs in
 the given graph, in decreasing order of sizes, separated by commas
 (avoid any spaces).

 So if your algorithm computes the sizes of the five largest SCCs to be

 500, 400, 300, 200 and 100, then your answer should be "500,400,300,200,100".

 If your algorithm finds less than 5 SCCs, then write 0 for the remaining terms.

 Thus, if your algorithm computes only 3 SCCs whose sizes are 400, 300, and 100,

 then your answer should be "400,300,100,0,0".

 WARNING: This is the most challenging programming assignment of the course.

 Because of the size of the graph you may have to manage memory carefully.

 The best way to do this depends on your programming language and environment,
 and we strongly suggest that you exchange tips for doing this on the discussion forums.




 *
 */
public class SCC_Stanford_Coursera {


  private Logger log =
          Logger.getLogger(SCC_Stanford_Coursera.class);




  public SCC_Stanford_Coursera(GraphAdjList_Kosaraju_SCC g) {

    Kosaraju_SCC kSCC = new Kosaraju_SCC(g);

    long start = System.currentTimeMillis();

    kSCC.findSCCs();

    long end = System.currentTimeMillis();

    printAlways("time to do both dfs and set SCC List: "+(end - start)+" millis.");

    Kosaraju_SCC.SCC scc = kSCC.getSCCs();

    int topK = 5;
    int count = 0;
    StringBuilder sb = new StringBuilder("The Top "+topK+" SCCs by member vertex count ");

    while (scc != null) {
      sb.append(scc.size()).append(", ");
      if (++count >= topK)  break;
      scc = scc.next();
    }

    printAlways(sb.toString());

  }



  protected static void printAlways(String s) {
     System.err.println(s);
  }

  protected boolean isP() {
    return log.isDebugEnabled();
  }


  public static void main(String[] args) {


    GraphAdjList_Kosaraju_SCC g = readDataFile(null);

    SCC_Stanford_Coursera runner = new
               SCC_Stanford_Coursera(g);


  }

  private static GraphAdjList_Kosaraju_SCC readDataFile(String inputFName) {
    //
    //  Graph of no more than 875720 vertices
    //
    //       sample input lines:
    //  1 4
    //  2 47646
    //  2 47647
    //
    long start = System.currentTimeMillis();

    GraphAdjList_Kosaraju_SCC graph = new GraphAdjList_Kosaraju_SCC(875720, true);


    FileReader fileR = null;
    String f = "SCC.txt";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\graph\\stronglyConnected\\Coursera_Stanford";
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
    long end = System.currentTimeMillis();
    printAlways("load graph elapsed time: "+(end - start)+ " millis.");
    return graph;
  }


}

/*
  MISC NOTES:

  attempt 1  yields:     (stack  -Xss1024m  -Xmx=6144m)

      The Top 5 SCCs by member vertex count 434821,968,459,313,211

  this is the correct result.


 */

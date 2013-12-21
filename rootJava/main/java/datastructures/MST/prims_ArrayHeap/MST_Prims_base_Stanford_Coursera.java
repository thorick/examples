package datastructures.MST.prims_ArrayHeap;

import datastructures.graph.basic.CloneableInteger;
import datastructures.graph.basic.Edge;
import datastructures.graph.basic.GraphAdjList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/7/13
 * Time: 8:47 AM
 */
public abstract class MST_Prims_base_Stanford_Coursera {

  protected int numVertices;
  protected int numEdges;


  protected abstract GraphAdjList newGraph(int size, boolean isDigraph);

  protected abstract Edge newEdge(int v, int w, CloneableInteger weight);

  protected GraphAdjList readDataFile(String inputFName) {
    //GraphAdjList graph = new GraphAdjList(600, false);
    GraphAdjList graph = newGraph(600, false);

    FileReader fileR = null;
    String f = "coursera_MST_edges";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\MST";
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

      // first record is the job count
      line = br.readLine();
      String[] s = line.split("\\s+");

      numVertices = Integer.valueOf(s[0]);
      numEdges = Integer.valueOf(s[1]);

      System.err.println("\nnumVertices=" + numVertices + ", numEdges=" + numEdges + "\n");
      //  numVertices=500, numEdges=2184

      int i = 0;
      while ((line = br.readLine()) != null) {
        //System.err.println("read line '" + line + "'");
        s = line.split("\\s+");
        int u = Integer.valueOf(s[0]);
        int v = Integer.valueOf(s[1]);
        CloneableInteger weight = new CloneableInteger(new Integer(s[2]));
        //Edge e = new Edge(u, v, weight);
        Edge e = newEdge(u, v, weight);
        graph.insert(e);

        e = new Edge(v, u, weight);
        graph.insert(e);

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

}

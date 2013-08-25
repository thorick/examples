package datastructures.graph.util;

import datastructures.graph.basic.CloneableInteger;
import datastructures.graph.basic.Edge;
import datastructures.graph.basic.GraphAdjList;

import datastructures.graph.minimumCuts.CollapsableEdge;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Set;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/3/13
 * Time: 7:13 PM
 */


public class GraphUtils {

  private static Logger log =
          Logger.getLogger(GraphUtils.class);

  /**
   * * Take as input a String Array that represents an Adjacency List for a vertex
   * <p/>
   * Non-uniform String Array element Format:
   * <p/>
   * [vertex]   [vertex,weight]  [vertex, weight] ...
   *
   * @param g
   * @param vertex
   */
  public static void loadWeightedVertex(GraphAdjList g, String[] vertex) {
    if (vertex == null || vertex.length <= 0) return;

    int tail = Integer.valueOf(vertex[0]);

    for (int i = 1; i < vertex.length; i++) {
      if (vertex[i] != null ) {
        String val = vertex[i];
        int index = val.indexOf(',');
        if (index <= 0) throw new RuntimeException("unexpected format missing ',' at element " + i);

        String headS = val.substring(0, index);
        String weightS = val.substring(index + 1);

        if (isP())
          log.debug("vertex="+tail+", headS='"+headS+"', weightS='"+weightS+"'");


        if (headS != null && weightS != null) {
        int head = Integer.valueOf(headS);
        int w = Integer.valueOf(weightS);
        CloneableInteger weight = new CloneableInteger(w);

        if (isP()) {
          log.debug("val='"+val+"', index="+index+", vertex v=" + tail + ", vPart='" + head + "', wPart='" + weight + "'");
        }
        Edge e = new Edge(tail, head, weight);
        g.insert(e);
        }
      }
    }

  }

  public static String printSetEdge(Set<Edge> s) {
    StringBuilder sb = new StringBuilder();
    for (Edge e: s) {
      sb.append(e.toString()).append(", ");
    }
    return sb.toString();
  }


  public static String printListEdge(List<Edge> s) {
    StringBuilder sb = new StringBuilder();
    for (Edge e: s) {
      sb.append(e.toString()).append(", ");
    }
    return sb.toString();
  }


  public static String printListCollapsableEdge(List<CollapsableEdge> s) {
    StringBuilder sb = new StringBuilder();
    for (CollapsableEdge e: s) {
      sb.append(e.toString()).append(", ");
    }
    return sb.toString();
  }

  protected static boolean isP() {
    return log.isDebugEnabled();
  }
}

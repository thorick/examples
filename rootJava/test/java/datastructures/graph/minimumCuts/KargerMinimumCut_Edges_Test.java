package datastructures.graph.minimumCuts;


import datastructures.graph.basic.Edge;
import datastructures.graph.util.GraphUtils;
import org.junit.Assert;
import org.junit.Test;
import org.apache.log4j.Logger;
import utils.StringUtils;

import java.util.HashSet;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/27/13
 * Time: 7:52 AM
 *
 * Test the variant of Karger which specifically
 * chooses Randomized Edges and NOT Randomized Vertices.
 *
 */


public class KargerMinimumCut_Edges_Test {

  private Logger log =
          Logger.getLogger(KargerMinimumCut_Edges_Test.class);


  /**
   *
   *
   *

   -- minimum 4
     edges:
   Edge: 2-5 o(2,3) o(2,1)-s7.Edge: 2-5 o(2,1)-s8.Edge: 5-2 o(3,2) o(1,2)-s9.Edge: 5-2 o(1,2)-s10.


   other solution:

   -- minimum 4
     edges:
   Edge: 1-5-s43.Edge: 5-1-s56.Edge: 5-1 o(5,4)-s5.Edge: 1-5 o(4,5)-s6.


   */
  //@Test
  public void test5Node2cut() {
    CollapsableGraphAdjList_EdgeLists graph = create5Node2CutGraph();

    if (isP()) {
      p("test graph is " + graph.toString());
    }


    MinimumCuts_RandomizedContraction_Edges_Stanford_Coursera runner =
            new MinimumCuts_RandomizedContraction_Edges_Stanford_Coursera();

            //List<List<Edge>> l = runner.runStatistical(graph, 2000);
    List<List<CollapsableEdge>> l = runner.runStatistical(graph, 100);




    List<CollapsableEdge> minList = graph.getMinList(l);
    int minCount = graph.countList(minList);
    if (isP()) {
      log.debug("\n\n\n\n\n\n ------  a Min Edge List: "+ GraphUtils.printListCollapsableEdge(minList)+"\n\n\n\n");
    }
    Assert.assertTrue("expected 4 bidirectional edges (2 unidirectional) , instead we got "+minCount+" bidirectional.", (minCount == 4));

  }



  /**
   * statistical analysis claims that if we
   * want a 1/n chance of failure to find the correct minimum
   * <p/>
   * we need to try the algorithm  n**2  logn  times
   * <p/>
   * 100 * 4 = 400
   * <p/>
   * sample success run:
   *
   * 1999: 2
   2000: 8
   1: 2
   -- minimum 2
     edges:
   Edge: 5-8 o(5,6)-s41.Edge: 8-5 o(6,5)-s42.

   */
  @Test
  public void test10NodeDense1cut() {
    CollapsableGraphAdjList_EdgeLists graph = create10Node1CutGraph();

    if (isP()) {
      p("test graph is " + graph.toString());
    }


    MinimumCuts_RandomizedContraction_Edges_Stanford_Coursera runner =
            new MinimumCuts_RandomizedContraction_Edges_Stanford_Coursera();

            //List<List<Edge>> l = runner.runStatistical(graph, 2000);
    List<List<CollapsableEdge>> l = runner.runStatistical(graph, 2000);




    List<CollapsableEdge> minList = graph.getMinList(l);
    int minCount = graph.countList(minList);


    Assert.assertTrue("expected 1 bidirectional edge ( 2 unidirectional), instead we got "+minCount+" bidirectional.", (minCount == 2));

  }


  /**
   * statistical analysis claims that if we
   * want a 1/n chance of failure to find the correct minimum
   * <p/>
   * we need to try the algorithm  n**2  logn  times
   * <p/>
   * 144 * 4 = 576
   *
   *
   *


   5997: 12
   5998: 14
   5999: 8
   6000: 14


   -- minimum 4
     edges:
   Edge: 5-6-s150.Edge: 6-5-s161.Edge: 5-6 o(5,7)-s37.Edge: 6-5 o(7,5)-s38.


   */
  @Test
  public void test12NodeDense2cut() {
    CollapsableGraphAdjList_EdgeLists graph = create12Node2CutGraph();

    if (isP()) {
      p("test graph is " + graph.toString());
    }

    MinimumCuts_RandomizedContraction_Edges_Stanford_Coursera runner =
            new MinimumCuts_RandomizedContraction_Edges_Stanford_Coursera();

    List<List<CollapsableEdge>> l = runner.runStatistical(graph, 6000);



    List<CollapsableEdge> minList = graph.getMinList(l);
    int minCount = graph.countList(minList);


    Assert.assertTrue("expected 4 bd edges (2 undirected), instead we got "+minCount+" bd.", (minCount == 4));

  }

  /**
   *
   *
   *  N**2 log N
   *
   *  400 * 5 = 2000
   *

   *
   *  5000: 2
   1: 2
   -- minimum 2
     edges:
   Edge: 10-17 o(10,11)-s153.Edge: 17-10 o(11,10)-s154.



   2000: 2
   1: 18
   2: 2
   -- minimum 2
     edges:
   Edge: 10-11-s446.Edge: 11-10-s465.


   *
   */
  @Test
  public void test20NodeDense1cut() {
    CollapsableGraphAdjList_EdgeLists graph = create20Node1CutGraph();

    if (isP()) {
      p("test graph is " + graph.toString());
    }

    MinimumCuts_RandomizedContraction_Edges_Stanford_Coursera runner =
            new MinimumCuts_RandomizedContraction_Edges_Stanford_Coursera();

    //List<List<CollapsableEdge>> l = runner.runStatistical (graph, 20000);
    List<List<CollapsableEdge>> l = runner.runStatistical (graph, 1);



    List<CollapsableEdge> minList = graph.getMinList(l);
    int minCount = graph.countList(minList);


    Assert.assertTrue("expected 2 bd edges (1 undirected), instead we got "+minCount+" bd.", (minCount == 2));


  }


  /**
   *
   *

   // set up a simple test graph
   //
   //   5 vertex undirected graph
   //
   //
   //         2 -- 1 -- 5
   //         |  / |   /
   //         | /  |  /
   //         |/   | /
   //         3 -- 4
   //


   * @return
   */
  private CollapsableGraphAdjList_EdgeLists create5Node2CutGraph() {

    // create a String representation of the adj list representation of the array
    // then read that into our Java representation
    String[] sa = new String[5];

    sa[0] = ("1 2 3 4 5");
    sa[1] = ("2 1 3");
    sa[2] = ("3 1 2 4");
    sa[3] = ("4 1 3 5");
    sa[4] = ("5 1 4");

    return graphFromStringArray(sa);
  }


  /**
   * Create a dense graph that has a 1 edge minimum cut
   * between 5-6.
   * <p/>
   * The 2 islands bridged by 5-6 are densely connected
   * making it more work for an algorithm to find the 5-6 bridge.
   *
   * @return
   */
  private CollapsableGraphAdjList_EdgeLists create10Node1CutGraph() {

    // create a String representation of the adj list representation of the array
    // then read that into our Java representation
    String[] sa = new String[10];

    sa[0] = ("1 2 3 4 5");
    sa[1] = ("2 1 3 4 5");
    sa[2] = ("3 1 2 4 5");
    sa[3] = ("4 1 2 3 5");

    sa[4] = ("5 1 2 3 4 6");

    sa[5] = ("6 5 7 8 9 10");

    sa[6] = ("7 6 8 9 10");
    sa[7] = ("8 6 7 9 10");
    sa[8] = ("9 6 7 8 10");
    sa[9] = ("10 6 7 8 9");

    return graphFromStringArray(sa);
  }


  /**
   * Create a dense graph that has a 2 edge minimum cut
   * between 5-6 and 5-7
   * <p/>
   * The 2 islands bridged by 5-6 are densely connected
   * making it more work for an algorithm to find the 5-6 bridge.
   *
   * @return
   */
  private CollapsableGraphAdjList_EdgeLists create12Node2CutGraph() {

    // create a String representation of the adj list representation of the array
    // then read that into our Java representation
    String[] sa = new String[13];

    sa[0] = ("1 2 3 4 5");
    sa[1] = ("2 1 3 4 5");
    sa[2] = ("3 1 2 4 5");
    sa[3] = ("4 1 2 3 5");

    sa[4] = ("5 1 2 3 4 6 7");

    sa[5] = ("6 5 7 8 9 10 11 12");

    sa[6] = ("7 5 6 8 9 10 11 12");
    sa[7] = ("8 6 7 9 10 11 12");
    sa[8] = ("9 6 7 8 10 11 12");
    sa[9] = ("10 6 7 8 9 11 12");
    sa[10] = ("11 6 7 8 9 10 12");
    sa[11] = "12 6 7 8 9 10 11";

    return graphFromStringArray(sa);
  }

  /**
    * Create a dense graph that has a 1 edge minimum cut
    * between 10-11.
    * <p/>
    * The 2 islands bridged by 10-11 are densely connected
    * making it more work for an algorithm to find the 10-11 bridge.
    *
    * @return
    */
   private CollapsableGraphAdjList_EdgeLists create20Node1CutGraph() {

     // create a String representation of the adj list representation of the array
     // then read that into our Java representation
     String[] sa = new String[21];

     sa[0] = ("1 2 3 4 5 6 7 8 9 10");
     sa[1] = ("2 1 3 4 5 6 7 8 9 10");
     sa[2] = ("3 1 2 4 5 6 7 8 9 10");
     sa[3] = ("4 1 2 3 5 6 7 8 9 10");
     sa[4] = ("5 1 2 3 4 6 7 8 9 10");
     sa[5] = ("6 1 2 3 4 5 7 8 9 10");
     sa[6] = ("7 1 2 3 4 5 6 8 9 10");
     sa[7] = ("8 1 2 3 4 5 6 7 9 10");
     sa[8] = ("9 1 2 3 4 5 6 7 8 10");

     sa[9] = ("10 1 2 3 4 5 6 7 8 9 11");
     sa[10] = ("11 10 12 13 14 15 16 17 18 19 20");

     sa[11] = ("12 11 13 14 15 16 17 18 19 20");
     sa[12] = ("13 11 12 14 15 16 17 18 19 20");
     sa[13] = ("14 11 12 13 15 16 17 18 19 20");
     sa[14] = ("15 11 12 13 14 16 17 18 19 20");
     sa[15] = ("16 11 12 13 14 15 17 18 19 20");
     sa[16] = ("17 11 12 13 14 15 16 18 19 20");
     sa[17] = ("18 11 12 13 14 15 16 17 19 20");
     sa[18] = ("19 11 12 13 14 15 16 17 18 20");
     sa[19] = ("20 11 12 13 14 15 16 17 18 19");




     return graphFromStringArray(sa);
   }


  private CollapsableGraphAdjList_EdgeLists graphFromStringArray(String[] sa) {
    int arraySize = sa.length + 1;
    CollapsableGraphAdjList_EdgeLists graph = new CollapsableGraphAdjList_EdgeLists(arraySize, false);

    for (int i = 0; i < sa.length; i++) {
      //System.err.println("read line '" + sa[i] + "'");
      if (sa[i] != null) {
        String[] s = sa[i].split("\\s+");
        int[] in = StringUtils.stringArrayToIntArray(s);
        graph.loadEdges(in);
        //System.err.println(StringUtils.printStringArray(s));
      }
    }
    return graph;

  }


  protected void p(String s) {
    log.debug(s);
  }

  protected boolean isP() {
    return log.isDebugEnabled();
  }


}

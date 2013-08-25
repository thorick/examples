package datastructures.graph.minimumCuts;


import datastructures.graph.basic.Edge;
import org.junit.Assert;
import org.junit.Test;
import org.apache.log4j.Logger;
import utils.StringUtils;

import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/27/13
 * Time: 7:52 AM
 */


public class KargerMinimumCutTest {

  private Logger log =
          Logger.getLogger(KargerMinimumCutTest.class);


  /**
   * statistical analysis claims that if we
   * want a 1/n chance of failure to find the correct minimum
   * <p/>
   * we need to try the algorithm  n**2  logn  times
   * <p/>
   * 100 * 4 = 400
   * <p/>
   * sample success run:

   * 2000: 8
   * -- minimum 1
   * edges:
   * 5-6,
   *
   */
  //@Test
  public void test10NodeDense1cut() {
    CollapsableGraphAdjList graph = create10Node1CutGraph();

    if (isP()) {
      p("test graph is " + graph.toString());
    }


    MinimumCuts_RandomizedContraction_Stanford_Coursera runner =
            new MinimumCuts_RandomizedContraction_Stanford_Coursera();

            List<List<Edge>> l = runner.runStatistical(graph, 2000);


    /*    dead end approach that doesn't work !
    MinimumCuts_PartiallyRandomizedContraction_Stanford_Coursera runner = new MinimumCuts_PartiallyRandomizedContraction_Stanford_Coursera();

    List<List<Edge>> l = runner.runStatistical(graph, 2000, 4);
    */

    int mincount = Integer.MAX_VALUE;
    for (List<Edge> list: l) {
      if (list.size() < mincount) {
        mincount = list.size();
      }
    }
    Assert.assertTrue("expected 1 edge, instead we got "+mincount, (mincount == 1));

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

   5998: 14
   5999: 17
   6000: 14
   -- minimum 2
     edges:
   5-7, 5-6,


   */
  //@Test
  public void test12NodeDense2cut() {
    CollapsableGraphAdjList graph = create12Node2CutGraph();

    if (isP()) {
      p("test graph is " + graph.toString());
    }

    MinimumCuts_RandomizedContraction_Stanford_Coursera runner =
            new MinimumCuts_RandomizedContraction_Stanford_Coursera();

    List<List<Edge>> l = runner.runStatistical(graph, 6000);

    int mincount = Integer.MAX_VALUE;
    for (List<Edge> list: l) {
      if (list.size() < mincount) {
        mincount = list.size();
      }
    }
    Assert.assertTrue("expected 2 edges, instead we got "+mincount, (mincount == 2));

  }

  /**
   *
   *
   *  N**2 log N
   *
   *  400 * 5 = 2000
   *
   *  note: one run of 6000  yielded a false result of 9 instead of 1
   *
   *  run at 50,000 happened to find result 1  but it doesn't happen every time !
   *
   *  a run at 100,000 did not find it !
   *
   *  a run at 200,000 did not find it !
   *
   *  Runs at 400,000:
   * #  success ?
   *  1  Y
   *  2  N
   *  3  Y
   *  4  Y
   *  5  N
   *  6  N
   *  7  N
   *  8  N
   *  9  N
   *  10 N
   *  11 Y
   *  12 Y
   *  13 Y
   *  14 N
   *  15 N
   *  16 N
   *  17 N
   *  18 N
   *  19 N
   *  20 N
   *
   *
   * Runs at 500,000
   * #  success ?
   *  1  N
   *  2  N
   *  3  N
   *  4  Y
   *  5  N
   *  6  N
   *  7  N
   *
   * Runs at 800,000
   * #  success ?
   *  1  N
   *  2  N
   *  3  Y
   *  4  N
   *  5  N
   *  6  N
   *
   * Runs at 1,000,000
   * #  success ?
   *  1  Y
   *  2  Y
   *  3  N
   *  4  Y
   *  5  N
   *  6  N
   *  7  N
   *  8  Y
   *  9  N
   *  10 N
   *
   */
  @Test
  public void test20NodeDense1cut() {
    CollapsableGraphAdjList graph = create20Node1CutGraph();

    if (isP()) {
      p("test graph is " + graph.toString());
    }

    MinimumCuts_RandomizedContraction_Stanford_Coursera runner =
            new MinimumCuts_RandomizedContraction_Stanford_Coursera();

    List<List<Edge>> l = runner.runStatistical (graph, 20000);
    int mincount = Integer.MAX_VALUE;
    for (List<Edge> list: l) {
      if (list.size() < mincount) {
        mincount = list.size();
      }
    }
    Assert.assertTrue("expected 1 edge, instead we got "+mincount, (mincount == 1));
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
  private CollapsableGraphAdjList create10Node1CutGraph() {

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
  private CollapsableGraphAdjList create12Node2CutGraph() {

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
   private CollapsableGraphAdjList create20Node1CutGraph() {

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


  private CollapsableGraphAdjList graphFromStringArray(String[] sa) {
    int arraySize = sa.length + 1;
    CollapsableGraphAdjList graph = new CollapsableGraphAdjList(arraySize, false);

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

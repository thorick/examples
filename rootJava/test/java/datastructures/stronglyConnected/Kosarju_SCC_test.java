package datastructures.stronglyConnected;


import datastructures.graph.basic.GraphAdjList;
import datastructures.graph.stronglyConnected.GraphAdjList_Kosaraju_SCC;
import datastructures.graph.stronglyConnected.Kosaraju_SCC;
import org.junit.Assert;
import org.junit.Test;
import org.apache.log4j.Logger;
import utils.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/30/13
 * Time: 10:37 AM
 *
 */
public class Kosarju_SCC_test {

  private Logger log =
           Logger.getLogger(Kosarju_SCC_test.class);


  @Test
  public void test01_LectureDemo() {
    GraphAdjList_Kosaraju_SCC g = createLectureDemoGraph();

    log.debug("loaded graph: "+g.toString());

    Kosaraju_SCC scc = new Kosaraju_SCC(g);

    scc.findSCCs();

    Kosaraju_SCC.SCC components = scc.getSCCs();
    log.debug("components: \n"+components.toString());

    List<List<String>>  expectedList = new ArrayList<List<String>>();
    List<String> l = new ArrayList<String>();
    l.add("8");  l.add("5");  l.add("2");
    expectedList.add(l);

    l = new ArrayList<String>();
    l.add("9");  l.add("3");  l.add("6");
    expectedList.add(l);

    l = new ArrayList<String>();
    l.add("1");  l.add("4");  l.add("7");
    expectedList.add(l) ;

    TestResult result = checkResults(expectedList, components);

    Assert.assertTrue(result.message(), result.passed());
  }


  @Test
  public void test02_Sedgewick() {
    GraphAdjList_Kosaraju_SCC g = createSedgewickGraph();

    log.debug("loaded graph: "+g.toString());

    Kosaraju_SCC scc = new Kosaraju_SCC(g);

    scc.findSCCs();

    Kosaraju_SCC.SCC components = scc.getSCCs();
    log.debug("components: \n"+components.toString());


    List<List<String>>  expectedList = new ArrayList<List<String>>();
    List<String> l = new ArrayList<String>();
    l.add("2");
    expectedList.add(l);

    l = new ArrayList<String>();
    l.add("10");  l.add("11");  l.add("12");    l.add("13");
    expectedList.add(l);

    l = new ArrayList<String>();
    l.add("1");  l.add("3");  l.add("4");  l.add("5");  l.add("6");   l.add("7");
    expectedList.add(l) ;

    l = new ArrayList<String>();
    l.add("8");  l.add("9");
    expectedList.add(l);

    TestResult result = checkResults(expectedList, components);

    Assert.assertTrue(result.message(), result.passed());
  }

  /**
    * Create the very simple graph from the Coursera Lecture
    *
    *  SCCs:
   *   9-3-6
   *   8-5-2
   *   7-1-4
   *
    * @return
    */
   private GraphAdjList_Kosaraju_SCC createLectureDemoGraph() {

     // create a String representation of the adj list representation of the array
     // then read that into our Java representation
     String[] sa = new String[9];

     sa[0] = ("1 4");
     sa[1] = ("2 8");
     sa[2] = ("3 6");
     sa[3] = ("4 7");
     sa[4] = ("5 2");
     sa[5] = ("6 9");
     sa[6] = ("7 1");
     sa[7] = ("8 5 6");
     sa[8] = ("9 3 7");

     return graphFromStringArray(sa);
   }


  /**
     * Create the very simple graph from the Coursera Lecture
     *
     *  SCCs:
    *   2
   *   10-11-12-13
    *   1-6-7-5-4-3
    *   8-9
    *
     * @return
     */
    private GraphAdjList_Kosaraju_SCC createSedgewickGraph() {

      // create a String representation of the adj list representation of the array
      // then read that into our Java representation
      String[] sa = new String[13];

      sa[0] = ("1 2 6 7");
      sa[1] = ("2");
      sa[2] = ("3 1 4");
      sa[3] = ("4 6 3");
      sa[4] = ("5 3 4 12");
      sa[5] = ("6 5");
      sa[6] = ("7 5 10");
      sa[7] = ("8 7 9");
      sa[8] = ("9 8 10");
      sa[9] = "10 11 12";
      sa[10] = "11 13";
      sa[11] = "12 13";
      sa[12] = "13 10";

      return graphFromStringArray(sa);
    }


  private TestResult checkResults(List<List<String>> expected, Kosaraju_SCC.SCC SCCs) {
    if (SCCs == null)  return new TestResult(false, "NULL SCC Component list");

    Kosaraju_SCC.SCC curr = SCCs;
    boolean passed = true;

    int expectedSCCcount = expected.size();
    int sccCount = 0;

    int prevSCCsize = Integer.MAX_VALUE;

    // check each SCC component list results against expected
    while (curr != null) {
      String currVList = curr.vertexListString();
      log.debug("\nverifying component "+currVList);
      List<String> expList = null;
      for (List<String> l : expected) {
        for (String s: l) {
          s = "," + s + ",";
          if (currVList.indexOf(s) != -1) {
            expList = l;
            log.debug("for expected char "+s+" from expected list '"+StringUtils.printListString(l)+" is found in currVList "+currVList);
            break;
          }
        }
      }
      if (expList == null) {
        return new TestResult(false, "could not find expected results for component "+curr.toString());
      }

      // found the appropriate expected result list, now check the actual result
      int currSize = curr.size();
      int expectedSize = expList.size();

      log.debug("using expected list "+StringUtils.printListString(expList));

      if (currSize != expectedSize)  {
          return new TestResult(false, "component "+curr.vertexListString()+" size="+currSize+", expected="+expectedSize);
      }

      for (String s: expList) {
        s = "," + s + ",";
        if (!currVList.contains(s)) {
          return new TestResult(false, "component "+curr.vertexListString()+" expected to find vertex="+s+", but we did not");
        }
        log.debug("vertex "+s+" verified.");
      }

      if (currSize > prevSCCsize) {
        return new TestResult(false, "SCCs out of order currSize="+currSize+" exceeds the previous size="+prevSCCsize);
      }
      prevSCCsize = currSize;
      sccCount++;
      curr = curr.next();
    }
    if (sccCount != expectedSCCcount) {
      return new TestResult(false, "expected to find "+expectedSCCcount+" SCCs, but instead we found "+sccCount);
    }
    return new TestResult(true, "found all expected components and their results");
  }

  /**
   * create graph with v vertices, starting at v=1
   * @param sa
   * @return
   */
  private GraphAdjList_Kosaraju_SCC graphFromStringArray(String[] sa) {
    int arraySize = sa.length + 1;
    GraphAdjList_Kosaraju_SCC graph = new GraphAdjList_Kosaraju_SCC(arraySize, true);

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


  protected boolean isP() {
    return log.isDebugEnabled();
  }

  private class TestResult {
    boolean pass;
    String message;
    TestResult(boolean b, String s) {
      pass = b;
      message = s;
    }

    boolean passed() {
      return pass;
    }

    String message() {
      return message;
    }

  }

}

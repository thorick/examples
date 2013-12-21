package datastructures.graph.KCluster;


import datastructures.graph.KCluster.KCluster3_HammingDistance_Stanford_Coursera.HammingVertex;
import org.junit.Assert;
import org.junit.Test;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/19/13
 * Time: 7:53 PM
 */
public class KCluster3_unitTest {


  private Logger log =
          Logger.getLogger(KCluster3_unitTest.class);


  //@Test
  public void singleVertex() {
    KCluster3_HammingDistance_Stanford_Coursera prog = new KCluster3_HammingDistance_Stanford_Coursera();
    int v = 0;
    String label = "0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 1 1 1 1 1 0 0 0 0";
    HammingVertex vertex = prog.new HammingVertex(v, label);

    HammingVertex[] vertices = new HammingVertex[1];
    vertices[0] = vertex;

    prog.compute(vertices, 2);
  }

  //@Test
  public void singleNumberedVertex() {
    KCluster3_HammingDistance_Stanford_Coursera prog = new KCluster3_HammingDistance_Stanford_Coursera();
    int v = 0;
    String label = "1 2 3 4 5 a b c d e 6 7 8 9 0 F G H I J w x y z";
    HammingVertex vertex = prog.new HammingVertex(v, label);

    int[] first = new int[]{0, 0, 0, 0, 0, 0};
    int[] second = new int[]{1, 2, 3, 4, 5, 6};
    String[] expected = new String[]{
            "0-1:345abcde67890FGHIJwxyz",
            "0-2:245abcde67890FGHIJwxyz",
            "0-3:235abcde67890FGHIJwxyz",
            "0-4:234abcde67890FGHIJwxyz",
            "0-5:2345bcde67890FGHIJwxyz",
            "0-6:2345acde67890FGHIJwxyz"
    };

    for (int i = 0; i < first.length; i++) {
      String bitKey = vertex.bitKey(first[i], second[i]);
      Assert.assertEquals("for " + first[i] + "-" + second[i] + ", expected '" + expected[i] + "' but got '" + bitKey, expected[i], bitKey);
    }

    first = new int[]{0, 0, 0, 0, 0, 0};
    second = new int[]{18, 19, 20, 21, 22, 23};
    expected = new String[]{
            "0-18:2345abcde67890FGHJwxyz",
            "0-19:2345abcde67890FGHIwxyz",
            "0-20:2345abcde67890FGHIJxyz",
            "0-21:2345abcde67890FGHIJwyz",
            "0-22:2345abcde67890FGHIJwxz",
            "0-23:2345abcde67890FGHIJwxy"
    };
    for (int i = 0; i < first.length; i++) {
      String bitKey = vertex.bitKey(first[i], second[i]);
      Assert.assertEquals("for " + first[i] + "-" + second[i] + ", expected '" + expected[i] + "' but got '" + bitKey, expected[i], bitKey);
    }

    first = new int[]{1, 1, 20, 21, 21, 22};
    second = new int[]{2, 3, 23, 22, 23, 23};
    expected = new String[]{
            "1-2:145abcde67890FGHIJwxyz",
            "1-3:135abcde67890FGHIJwxyz",
            "20-23:12345abcde67890FGHIJxy",
            "21-22:12345abcde67890FGHIJwz",
            "21-23:12345abcde67890FGHIJwy",
            "22-23:12345abcde67890FGHIJwx"
    };
    for (int i = 0; i < first.length; i++) {
      String bitKey = vertex.bitKey(first[i], second[i]);
      Assert.assertEquals("for " + first[i] + "-" + second[i] + ", expected '" + expected[i] + "' but got '" + bitKey, expected[i], bitKey);
    }


  }

  //@Test
  public void testDistance() {
    KCluster3_HammingDistance_Stanford_Coursera prog = new KCluster3_HammingDistance_Stanford_Coursera();

    int v0 = 0;
    String label0 = "0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 1 1 1 1 1 0 0 0 0";
    HammingVertex vertex0 = prog.new HammingVertex(v0, label0);

    /*
    int v1 = 1;
    String label1 = "0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 1 1 1 1 1 0 1 0 0";
    HammingVertex vertex1 = prog.new HammingVertex(v1, label1);

    int expected = 1;
    int d1 = vertex0.computeDistance(vertex1);
    Assert.assertEquals("expected Hamming Distance=" + expected + ", got=" + d1, expected, d1);
    */

    String[] lab1 = new String[]{
            "0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 1 1 1 1 1 0 0 0 0",
            "0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 1 1 1 1 1 0 1 0 0",
            "0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 1 0 1 1 1 1 0 0 0",
            "1 0 0 0 0 1 1 1 0 1 0 0 0 0 0 1 1 1 1 1 0 0 0 1",
            "0 0 1 1 0 1 1 1 0 0 0 0 0 0 0 1 1 1 1 1 0 0 0 0"
    };
    int[] expect = new int[]{0, 1, 2, 3, 4};

    for (int i = 0; i < 5; i++) {
      HammingVertex other = prog.new HammingVertex(expect[i], lab1[i]);
      int d = vertex0.computeDistance(other);
      Assert.assertEquals("case=" + i + ", expected Hamming Distance=" + expect[i] + ", got=" + d, expect[i], d);

    }

  }


  /**
   *

   ---  there are 4 cluster groups:
  groupID=0, groupSize=2  members: {5,}
  groupID=1, groupSize=3  members: {0,1,3,}
  groupID=2, groupSize=3  members: {2,4,}
  groupID=6, groupSize=2  members: {6,8,}


   */
  @Test
  public void testSimpleGraph() {
    HammingVertex[] v = buildSimpleGraph();

    KCluster3_HammingDistance_Stanford_Coursera prog =
             new KCluster3_HammingDistance_Stanford_Coursera();
    int retVal = prog.compute(v, 2);
    System.err.println("testSimpleGraph() result="+retVal);
  }


  /**
   * Simple Graph
   * distances (not all of them, but this subset is there)
   * 0:  2
   * 1:  2
   * 2:  2
   * 3:  2
   * 4:  2
   *
   * @return
   */
  HammingVertex[] buildSimpleGraph() {
    String[] labels = new String[]{
            "0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 1 1 1 1 1 0 0 0 0",
            "0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 1 1 1 1 1 0 0 0 0",

            "0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 1 1 1 1 1 0 1 0 0",
            "1 0 0 0 0 1 1 1 1 1 0 0 0 0 0 1 1 1 1 1 0 0 0 0",

            "0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 1 1 1 1 1 0 1 1 0",
            "0 0 0 0 0 1 0 0 1 1 0 0 0 0 0 1 1 1 1 1 0 0 0 0",

            "0 1 1 1 0 1 1 1 1 1 0 0 0 0 0 1 1 1 1 1 0 0 0 0",
            "0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 1 1 1 0 0 1 0 0 0",

            "0 1 1 1 1 1 1 1 1 1 0 0 0 0 0 1 1 1 1 1 0 0 0 0",
            "0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 1 0 0 0 0 0 0 0 0",
    };

    KCluster3_HammingDistance_Stanford_Coursera prog =
          new KCluster3_HammingDistance_Stanford_Coursera();
    HammingVertex[] vertices = new HammingVertex[labels.length];

    for (int i=0 ; i<labels.length; i++) {
      HammingVertex vertex = prog.new HammingVertex(i, labels[i]);
      vertices[i] = vertex;
    }
    return vertices;
  }

}

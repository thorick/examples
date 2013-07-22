package algorithms.sort;


import org.junit.Assert;
import org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/13/13
 * Time: 7:48 PM
 */
public class QuickSort_countComparisons_Stanford_Coursera_test {

  private Logger log =
          Logger.getLogger(QuickSort_countComparisons_Stanford_Coursera_test.class);

  private QuickSort_countComparisons_Stanford_Coursera target;

  @Before
  public void setup() {
    target = new QuickSort_countComparisons_Stanford_Coursera();
  }


  /**
   * This example from the lectures should count up 3 inversions
   * (3,2) (5,2) (5,4)
   *
   */
  //@Test
  public void test00_array0_pivotMethod1() {
    String m = "test00_lectureExample";

    int[] t = new int[6];
    t[0] = 1;
    t[1] = 3;
    t[2] = 5;
    t[3] = 2;
    t[4] = 4;
    t[5] = 6;


    p(m+"\n input array "+pInts(t));
    int pivotMethod = 1;
    long actual = target.quickSort(t, pivotMethod);

    // worst case 1+2+..+5 = 5*6/2 = 15
    long expected=11;


    p(m+"\n pivot method: "+pivotMethod+", result array "+pInts(t)+" number of comparisons="+actual);
    Assert.assertEquals("expected " + expected + " but got " + actual, expected, actual);
  }

  /**
   *
   * no splits at the top
   * 1, 4, 2, 3, 5, 9, 10, 7, 8, 6
   */
  //@Test
  public void test01_array1_pivotMethod1() {
    String m = "test01_array1_pivotMethod1";

    int[] t = new int[10];
    t[0] = 1;
    t[1] = 4;
    t[2] = 2;
    t[3] = 3;
    t[4] = 5;
    t[5] = 9;
    t[6] = 10;
    t[7] = 7;
    t[8] = 8;
    t[9] = 6;


    p(m+"\n input array "+pInts(t));
    int pivotMethod = 1;
    long actual = target.quickSort(t, pivotMethod);

    // worst case 1+2+..+9 = 9*10/2 = 45
    long expected=30;

    p(m+"\n pivot method: "+pivotMethod+", result array "+pInts(t)+" number of comparisons="+actual);
    Assert.assertEquals("expected " + expected + " but got " + actual, expected, actual);

   }


  /**
   *
   * 1 split at the top to make 9 inversions
   *
   * 2, 3, 4, 5, 6, 7, 8, 9, 10, 1
   *
   */
  //@Test
  public void test02_array2_pivotMethod1() {
    String m = "test02_array2_pivotMethod1";

    int[] t = new int[10];
    t[0] = 2;
    t[1] = 3;
    t[2] = 4;
    t[3] = 5;
    t[4] = 6;
    t[5] = 7;
    t[6] = 8;
    t[7] = 9;
    t[8] = 10;
    t[9] = 1;

    p(m+"\n input array "+pInts(t));


    p(m+"\n input array "+pInts(t));
    int pivotMethod = 1;
    long actual = target.quickSort(t, pivotMethod);

    // worst case 1+2+..+9 = 9*10/2 = 45
    long expected=25;

    p(m+"\n pivot method: "+pivotMethod+", result array "+pInts(t)+" number of comparisons="+actual);
    Assert.assertEquals("expected " + expected + " but got " + actual, expected, actual);
   }

  //@Test
  public void test00_array3_pivotMethod1() {
    String m = "test00_lectureExample";

    int[] t = new int[6];
    t[0] = 6;
    t[1] = 5;
    t[2] = 4;
    t[3] = 3;
    t[4] = 2;
    t[5] = 1;


    p(m+"\n input array "+pInts(t));
    int pivotMethod = 1;
    long actual = target.quickSort(t, pivotMethod);

    // worst case 1+2+..+5 = 5*6/2 = 15
    long expected=15;


    p(m+"\n pivot method: "+pivotMethod+", result array "+pInts(t)+" number of comparisons="+actual);
    Assert.assertEquals("expected " + expected + " but got " + actual, expected, actual);
  }
  /**
   *
   * 1 split at the top to make 8 inversions
   *
   * 2, 3, 4, 5, 6, 7, 8, 9, 1
   *
   * Here's a key thing that happens at the ROOT split of this case:
   *
   *
   TOP LEVEL ARRAY:

   CountArrayInversions: ROOT:0  Start countInv: ROOT:0  size=9, sL=4, sR=5
   input 2, 3, 4, 5, 6, 7, 8, 9, 1,


   LEFT SIDE  (WE FIND NO INVERSIONS):

   CountArrayInversions: cl:1  Start countInv: cl:1  size=4, sL=2, sR=2
   input 2, 3, 4, 5,
   CountArrayInversions: ROOT:0 L inversions=0


   RIGHT SIDE  (WE FIND 4 INVERSIONS):
   CountArrayInversions: cr:2  Start countInv: cr:2  size=5, sL=2, sR=3
   input 6, 7, 8, 9, 1,

   CountArrayInversions: CSplit:5   result=1, 6, 7, 8, 9,
   CountArrayInversions: ROOT:0 R inversions=4


   SEPARATELY LEFT AND RIGHT HAVE NOW CORRECTED 4 INVERSIONS, THEN WE MERGE LEFT AND RIGHT:

   CountArrayInversions: CSplit:3 lSize=4, rSize=5 totalSize=9
   L=2, 3, 4, 5,
   R=1, 6, 7, 8, 9,


   FIND 4 SPLIT INVERSIONS REQUIRED TO RECTIFY THE COMPLETE ARRAY,
   THIS BRINGS THE TOTAL INVERSIONS to 4 + 4 = 8
   THE KEY POINT IS THAT EACH REARRANGEMENT REQUIRED TO PROPERLY PLACE
   AN INVERTED ELEMENT IS LINEAR IN COST.  IT DOESN'T MATTER WHEN IT IS
   MOVED, ONLY THAT IS IT AT SOME POINT IN TIME.


   CountArrayInversions: ROOT:0 Split inversions=4

   CountArrayInversions_using_MergeSort_Stanford_Coursera_test,main:165 - ArrayInversionTest: test02_oneSplitInversionFirstLevel
   result array 1, 2, 3, 4, 5, 6, 7, 8, 9,  number of inversions found=8

   */
  /*
   @Test
   public void test03_oneSplitInversionFirstLevel_oddArray() {
     String m = "test02_oneSplitInversionFirstLevel";

     int[] t = new int[9];
     t[0] = 2;
     t[1] = 3;
     t[2] = 4;
     t[3] = 5;
     t[4] = 6;
     t[5] = 7;
     t[6] = 8;
     t[7] = 9;
     t[8] = 1;


     p(m+"\n input array "+pInts(t));

     CountArrayInversions_using_MergeSort_Stanford_Coursera.CountResult cr = target.countInversions(t, "ROOT", 0, 0);

     p(m+"\n result array "+pInts(cr.getDataOut())+" number of inversions found="+cr.getCount());

     long expected=8;
     long actual=cr.getCount();

     Assert.assertEquals("expected "+expected+" but got "+actual, expected, actual);
   }
   */

  /**
   *
   * no splits at the top
   * 1, 4, 2, 3, 5, 9, 10, 7, 8, 6
   */
  //@Test
  public void test01_array1_pivotMethod3() {
    String m = "test01_array1_pivotMethod3";

    int[] t = new int[10];
    t[0] = 1;
    t[1] = 4;
    t[2] = 2;
    t[3] = 3;
    t[4] = 5;
    t[5] = 9;
    t[6] = 10;
    t[7] = 7;
    t[8] = 8;
    t[9] = 6;


    p(m+"\n input array "+pInts(t));
    int pivotMethod = 3;
    long actual = target.quickSort(t, pivotMethod);

    // worst case 1+2+..+9 = 9*10/2 = 45
    long expected=19;

    p(m+"\n pivot method: "+pivotMethod+", result array "+pInts(t)+" number of comparisons="+actual);
    Assert.assertEquals("expected " + expected + " but got " + actual, expected, actual);

   }

  /**
   *
   * 1 split at the top to make 9 inversions
   *
   * 2, 3, 4, 5, 6, 7, 8, 9, 10, 1
   *
   */
  @Test
  public void test02_array2_pivotMethod3() {
    String m = "test02_array2_pivotMethod3";

    int[] t = new int[10];
    t[0] = 2;
    t[1] = 3;
    t[2] = 4;
    t[3] = 5;
    t[4] = 6;
    t[5] = 7;
    t[6] = 8;
    t[7] = 9;
    t[8] = 10;
    t[9] = 1;

    p(m+"\n input array "+pInts(t));


    p(m+"\n input array "+pInts(t));
    int pivotMethod = 3;
    long actual = target.quickSort(t, pivotMethod);

    // worst case 1+2+..+9 = 9*10/2 = 45
    long expected=25;

    p(m+"\n pivot method: "+pivotMethod+", result array "+pInts(t)+" number of comparisons="+actual);
    Assert.assertEquals("expected " + expected + " but got " + actual, expected, actual);
   }


  private void p(String s) {
    log.debug("QuickSortTest: " + s);
  }

  private String pInts(int[] in) {
    return pInts(in, 0, in.length-1);
  }

  private String pInts(int[] in, int start, int end) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < in.length; i++) {
      if (i >= start && i <= end)  {
        sb.append(Integer.toString(in[i])).append(", ");
      }
    }
    return sb.toString();
  }
}

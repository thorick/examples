package algorithms.dynamicProgramming.knapsack;


import bits.BitUtils;
import org.junit.Assert;
import org.junit.Test;
import org.apache.log4j.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/25/13
 * Time: 11:28 AM
 */
public class KnapsackTest {

  private Logger log =
          Logger.getLogger(KnapsackTest.class);

  private int capacity;
  private int numItems;
  private int[] values;
  private int[] weights;


  @Test
  public void testSimpleSack() {
    Knapsack sack = getSimpleSack();
    int retVal = sack.compute();
    int expected = 8;

    Assert.assertEquals("Simple Sack expected=" + expected + ", but got=" +
            retVal, expected, retVal);

    // check on retrieving compressed item data
    try {
      ItemColumnCompressed[] cols = sack.getCompressedItems();
      Assert.assertEquals("expected compressed results for 5 items, instead we got=" + cols.length, 5, cols.length);

      int[] c0 = new int[]{0, 0, 0, 0, 0, 0, 0};
      int[] c1 = new int[]{0, 0, 0, 0, 3, 3, 3};
      int[] c2 = new int[]{0, 0, 0, 2, 3, 3, 3};
      int[] c3 = new int[]{0, 0, 4, 4, 4, 6, 7};
      int[] c4 = new int[]{0, 0, 4, 4, 4, 8, 8};

      int[][] e = new int[][]{c0, c1, c2, c3, c4};

      for (int i = 0; i < 5; i++) {
        int[] exp = e[i];
        ItemColumnCompressed col = cols[i];
        for (int j = 0; j < 7; j++) {
          expected = exp[j];
          int got = col.valueAt(j);
          Assert.assertEquals("for item=" + i + ", capacity=" + j + ", expected=" + expected + " but we got=" + got +
                  ", compressedCol=" + col.toString(), expected, got);
        }
      }
    } catch (RuntimeException e) {
      System.err.println("skipping unimplemented feature  compressedItems");
    }

    int[][] itemsSelectedBitMaps = sack.itemsSelected();
    int[] itemSelectedBitMap = itemsSelectedBitMaps[0];

    String selectedString = BitUtils.printBits(itemSelectedBitMap[0]);
    System.err.println(" selected bitMap='"+selectedString+"'");

    int[] selected = sack.listSelectedItems();
    StringBuilder sb = new StringBuilder("Selected items: ");
    for (int i=0; i<selected.length; i++) {
      if (selected[i] >= 0) sb.append(selected[i]+", ");
    }
    System.err.println("\n\n"+sb.toString());

    expected = 112;
    Assert.assertEquals("expected the taken int to be="+expected+", instead we got="+itemSelectedBitMap[0], expected, itemSelectedBitMap[0]);
  }


  /**
   * Coursera Lecture Simple sack
   */
  private Knapsack getSimpleSack() {
    numItems = 4;
    capacity = 6;
    values = new int[]{3, 2, 4, 4};
    weights = new int[]{4, 3, 2, 3};

    return getKnapsack();
  }


  private Knapsack getKnapsack() {
    //Knapsack sack = new Knapsack_small_Stanford_Coursera();
    Knapsack sack = sackClass();
    sack.setKnapsackSize(capacity);
    sack.setNumberOfItems(numItems);
    sack.setValues(values);
    sack.setWeights(weights);
    return sack;
  }

  private Knapsack sackClass() {
    //return new Knapsack_small_Stanford_Coursera();

    //return new Knapsack_small_compression_Stanford_Coursera();
    return new Knapsack_large_alt00_Stanford_Coursera();

  }

}

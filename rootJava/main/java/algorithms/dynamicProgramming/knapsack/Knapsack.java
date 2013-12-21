package algorithms.dynamicProgramming.knapsack;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/25/13
 * Time: 11:36 AM
 */
interface Knapsack {


  public void setKnapsackSize(int i);

  public void setNumberOfItems(int i);

  public void setValues(int[] i);

  public void setWeights(int[] i);

  public int compute();

  public int[] listSelectedItems();

  public int[][] itemsSelected();

  // obsolete interface:  compression not good enough
  public ItemColumnCompressed[] getCompressedItems();

}

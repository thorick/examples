package algorithms.dynamicProgramming.knapsack;

import org.apache.log4j.Logger;
import utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/22/13
 * Time: 2:37 PM
 * <p/>
 * <p/>
 * This problem also asks you to solve a knapsack instance, but a much bigger one.
 * <p/>
 * Download the text file here. This file describes a knapsack instance,
 * and it has the following format:
 * <p/>
 * [knapsack_size][number_of_items]
 * [value_1] [weight_1]
 * [value_2] [weight_2]
 * ...
 * For example, the third line of the file is "50074 834558",
 * indicating that the second item has value 50074 and size 834558, respectively.
 * <p/>
 * As before, you should assume that item weights and the knapsack capacity are integers.
 * <p/>
 * This instance is so big that the straightforward iterative implemetation uses
 * an infeasible amount of time and space.
 * <p/>
 * So you will have to be creative to compute an optimal solution.
 * <p/>
 * One idea is to go back to a recursive implementation, solving subproblems --- and,
 * <p/>
 * of course, caching the results to avoid redundant work
 * --- only on an "as needed" basis.
 * <p/>
 * Also, be sure to think about appropriate data structures for storing
 * and looking up solutions to subproblems.
 * <p/>
 * In the box below, type in the value of the optimal solution.
 * <p/>
 * <p/>
 * size = 2000000
 * items = 2000
 * <p/>
 * first result:  4243395
 */
public class Knapsack_large_Stanford_Coursera implements Knapsack {


  private Logger log =
          Logger.getLogger(Knapsack_large_Stanford_Coursera.class);

  private int knapsackSize;
  private int numberOfItems;
  private int[] values;
  private int[] weights;

  private int minWeight;
  private int maxWeight;

  private int minValue;
  private int maxValue;

  private int weightRange;

  private ItemColumnInfo[] compressedItems;



  public int[] listSelectedItems() {
    throw new RuntimeException("NYI");
  }

  public void setKnapsackSize(int i) {
    knapsackSize = i;
  }

  public void setNumberOfItems(int i) {
    numberOfItems = i;
  }

  public void setValues(int[] i) {
    values = i;
  }

  public void setWeights(int[] i) {
    weights = i;
  }


  public ItemColumnCompressed[] getCompressedItems() {
    return compressedItems;
  }

  public int[][] itemsSelected() {
    throw new RuntimeException("NYI");
  }



  //
  //  DP requires that the sequence of operations be strictly ordered
  //  this is because 'future' computations rely on the saved results
  //  of 'past' computations.
  //  The order of computation here is by item position in the 2nd array position
  //    (an unconventional way, but done so that a diagram of a matrix
  //     in 'row' 'column' order will look like the lecture example.)
  //
  //  the 'weight' rows refer to the *available capacity* of the knapsack at that
  //  point in the computation.
  //  (WELL NO) thus when weight == 0 the sack is FULL, when weight == knapsackSize sack is empty.
  //  (WELL MAYBE) more sensible way to look at it:
  //    bottom up order is going backwards:
  //      so the 'weight' row at item 'i' is the available capacity of the sack.
  //      we can only add item 'i' if there is enough capacity (at that row for i)
  //      when the 'next item' is considered at some capacity, adding that
  //      next item was done on top of some other previous item..  we check
  //      the value at the previous item, corresponding to the current capacity
  //      minus the weight of the current item.
  //
  //  (DEFINITELY NO !) Another way to look at it:
  //      the weight row is the USED up weight..  NO because then if the weight
  //      row is the max at the right, then you should not be able to add
  //      anything new !  and that's not how the algorithm works !
  //
  //  the goal is that by the time we've finished considering the item selections
  //  IN ORDER, we used as much of the knapsack capacity as possible and have
  //  figured out which combinations of items yields the GREATEST cumulative value.
  //
  public int compute() {
    long start = System.currentTimeMillis();
    int maxValue = 0;   // keep it around
    weightRange = maxWeight - minWeight;
    compressedItems = new ItemColumnInfo[numberOfItems];

    int[] valuePrev;
    int[] valueCurr;
    int[] valueAA = new int[knapsackSize + 1];
    int[] valueBB = new int[knapsackSize + 1];

    // we'll process the weights in as read order
    // an interesting experiment would be to sort them cost first or value first to look at the useful sub calc ranges
    //

    // march along in item order first to last
    // filling in MAX cumulative value calculation for each each item's weight
    //
    // at each item and remaining capacity in the weight rows
    //   we can either:
    //     1.  NOT take the current item:  the capacity stays the same, 'inherit' value from previous item capacity
    //     2.  TAKE the current item:  in order to have had the current capacity to take,
    //           the *previous* capacity had to have been the current capacity minus the weight of the current item
    //           take the value calculated from 'curr capacity - weight of this item'.
    //

    //
    //  rewrite this to use  only  prev and curr column arrays
    //    after each prev is finished create and save off a compressed version of the prev array
    //    switch arrays for the next cycle
    //

    // start off set initial current item column
    valueCurr = valueAA;
    valuePrev = valueBB;
    boolean currIsA = true;
    for (int item = 0; item < numberOfItems; item++) {
      int prevCapacityIndex = 0;
      for (int remainingCapacityIndex = 0; remainingCapacityIndex <= knapsackSize; remainingCapacityIndex++) {

        // get value for #1  do NOT take current item, inherit value from same capacity and previous item
        int valueCase1 = 0;
        if (item > 0) {
          valueCase1 = valuePrev[remainingCapacityIndex];
          if (isP())
            log.debug("CASE 1  NO TAKE  item=" + item + ", cap=" + remainingCapacityIndex +
                    ", valueCase1=" + valueCase1);
        } else {
          // first item '0', there is no previous item
          // inherit value '0'
          if (isP())
            log.debug("CASE 1  NO TAKE  item=" + item + ", cap=" + remainingCapacityIndex +
                    ", first item:  valueCase1=" + valueCase1);
        }

        // get value for #2  TAKE current item add value to value from previous item at (capacity - curr weight)
        int valueCase2 = 0;
        int currItemWeight = weights[item];
        int currItemValue = values[item];

        if (currItemWeight <= remainingCapacityIndex) {
          // 'add' this item to the knapsack
          // to get value look up the value for 'reversed plucking'
          // previously computed value for curr capacity - 'weight of this item'
          prevCapacityIndex = remainingCapacityIndex - currItemWeight;
          int prevCapacityValue = 0;
          if (item > 0) {
            prevCapacityValue = valuePrev[prevCapacityIndex];

            if (isP())
              log.debug("CASE 2  TAKE  item=" + item + ", cap=" + remainingCapacityIndex +
                      ", prevCap from index=" + prevCapacityIndex + " is=" + prevCapacityValue);
            // first item so there is no previous capacity
          }
          valueCase2 = prevCapacityValue + currItemValue;
        } else {
          // it's impossible to select this item because there is no room
          // set value to '0'.   impossible to select makes this part of
          // an 'empty path'.  It's not possible to even land here as part
          // of any other 'non-empty' productive path
        }
        if (isP())
          log.debug("CASE 2  value=" + valueCase2);

        // check for and select MAX value for this cell  write cumulative value
        boolean take = false;
        int max = valueCase1;
        if (valueCase2 > valueCase1) {
          max = valueCase2;
          take = true;
        }
        valueCurr[remainingCapacityIndex] = max;
        if (isP())
          log.debug(" SETTING TAKE=" + take + ",  item=" + item + ", cap=" + remainingCapacityIndex +
                  ", MAX value=" + max + "\n");
        if (max > maxValue) maxValue = max;
      }

      // now do column compression on prev
      //   swap curr as new prev
      //   reinit curr (just to be sure)
      ItemColumnInfo ci = new ItemColumnInfo();
      ci.setCompression(valuePrev, item);
      compressedItems[item] = ci;

      valuePrev = valueCurr;
      if (currIsA) {
        valueCurr = valueBB;
        currIsA = false;
      } else {
        valueCurr = valueAA;
        currIsA = true;
      }
      for (int i = 0; i < valueCurr.length; i++) {
        valueCurr[i] = 0;
      }
      if (isP()) {
        log.debug("   DONE with item=" + item + "\n");
        log.debug(ci.toString());
      }
      System.err.println("   DONE with item=" + item + "\n");

      if (item == 500) {
        System.err.println("item 500 "+ci.toString());
        System.exit(0);
      }
    }
    ItemColumnInfo ci = new ItemColumnInfo();
    ci.setCompression(valuePrev, numberOfItems);
    compressedItems[numberOfItems] = ci;
    if (isP())
      log.debug(ci.toString());

    long end = System.currentTimeMillis();
    System.err.println("finished "+numberOfItems+" itmes in "+(end - start)/1000+" sec.");

    return maxValue;
  }

  private String printBooleanArray(boolean[] bA) {
    StringBuilder sb = new StringBuilder();
    int index = 0;
    for (boolean b : bA) {
      sb.append(index + "=" + b + ", ");
      index++;
    }
    sb.append("\n");
    return sb.toString();
  }

  protected void readDataFile(String inputFName) {

    FileReader fileR = null;
    String f = "knapsack_big_Stanford_Coursera";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\algorithms\\dynamicProgramming\\knapsack";
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

      // first record is 1: number of vertices  2: bits per vertex label
      line = br.readLine();
      String[] s = line.split("\\s+");

      knapsackSize = Integer.valueOf(s[0]);
      numberOfItems = Integer.valueOf(s[1]);

      values = new int[numberOfItems];
      weights = new int[numberOfItems];

      minWeight = Integer.MAX_VALUE;
      maxWeight = Integer.MIN_VALUE;
      minValue = Integer.MAX_VALUE;
      maxValue = Integer.MIN_VALUE;

      int i = 0;
      while ((line = br.readLine()) != null) {
        //System.err.println("read line '" + line + "'");
        s = line.split("\\s+");
        values[i] = Integer.valueOf(s[0]);
        weights[i] = Integer.valueOf(s[1]);
        if (weights[i] > maxWeight) maxWeight = weights[i];
        if (weights[i] < minWeight) minWeight = weights[i];
        if (values[i] > maxValue) maxValue = values[i];
        if (values[i] < minValue) minValue = values[i];
        i++;
        if (i % 10 == 0) {
          //  System.err.println("read vertex #" + i + " = " + i);
        }
      }

      System.err.println(
              "\nknapsackSize=" + knapsackSize +
                      "\nnumberOfItems=" + numberOfItems +
                      "\nminValue=" + minValue +
                      "\nmaxValue=" + maxValue +
                      "\nminWeight=" + minWeight +
                      "\nmaxWeight=" + maxWeight + "\n");

      br.close();
    } catch (IOException e) {
      System.err.println("exception " + e.getMessage());
    }
  }


  private boolean isP() {
    return log.isDebugEnabled();
  }

  public static void main(String[] args) {
    Knapsack_large_Stanford_Coursera prog = new Knapsack_large_Stanford_Coursera();

    prog.readDataFile(null);

    int retVal = prog.compute();

    System.err.println(" result=" + retVal);
  }

  class ItemColumnInfo implements ItemColumnCompressed {
    int itemNumber;          // convenience field
    int zeroRegionCeiling = -1;
    int midRegionCeiling = -1;
    int midRegionFloor = -1;
    int topRegionFloor = -1;
    int midRegionValue;
    int topRegionValue;

    boolean hasZeroRegion = false;
    boolean hasZeroRegionOnly = false;
    boolean hasMidRegion = false;
    boolean hasTopRegion = false;

    int[] uniqueValueRegion;

    public ItemColumnInfo() {

    }

    public void setZeroRegionCeiling(int i) {
      zeroRegionCeiling = i;
    }

    public void setMidRegionCeiling(int i) {
      midRegionCeiling = i;
    }

    public void setTopRegionFloor(int i) {
      topRegionFloor = i;
    }

    public int valueAt(int ind) {
      if (hasZeroRegionOnly) return 0;
      if (zeroRegionCeiling >= 0)
        if (ind <= zeroRegionCeiling) return 0;
      if (topRegionFloor >= 0)
        if (ind >= topRegionFloor) return topRegionValue;
      if (midRegionCeiling >= 0)
        if (ind >= midRegionFloor && ind <= midRegionCeiling) return midRegionValue;

      if (hasMidRegion && midRegionCeiling >= 0)
        return uniqueValueRegion[ind - midRegionCeiling - 1];

      if (hasZeroRegion && zeroRegionCeiling >= 0)
        return uniqueValueRegion[ind - zeroRegionCeiling - 1];

      return uniqueValueRegion[ind];
    }

    //
    //  region mappings
    //
    //  all zeroes:   zero region ONLY, means NO mid or top region
    //
    public void setCompression(int[] col, int itemNumber) {
      this.itemNumber = itemNumber;
      // check for bottom zero region
      int index = 0;
      if (col[0] == 0) {
        for (index = 0; index < col.length; index++) {
          if (col[index] != 0) break;
        }
        zeroRegionCeiling = index - 1;
        if (index == col.length) {
          // we ran through the entire array and got zeroes
          hasZeroRegionOnly = true;
        }
        hasZeroRegion = true;
      }

      // check for top region
      //  if there are NOT 2 of the same value at the very top
      //  then there is NO top region
      if (!hasZeroRegionOnly) {
        topRegionValue = col[col.length - 1];
        if (col[col.length - 2] == topRegionValue) {
          for (index = col.length - 2; index >= 0; index--) {
            if (col[index] != topRegionValue) break;
          }
          topRegionFloor = index + 1;

          //  we already check for zeroRegion ONLY
          //  so this is case all 'non-zero' column
          if (index == 1)
            if (col[0] == topRegionValue)
              topRegionFloor = 0;

          hasTopRegion = true;
        }
      }

      // check for mid region
      //   see that mid region is not actually top region  (there is no unique region in between)
      //if (topRegionFloor > 0) {
      midRegionFloor = 0;
      if (hasZeroRegion) {
        // if there's a zero region
        // then if there's a midregion, it'd be on top of the zero region
        midRegionFloor = zeroRegionCeiling + 1;
      }
      if (midRegionFloor >= col.length - 1) {
        hasMidRegion = false;
      } else {
        if (hasTopRegion && (midRegionFloor >= topRegionFloor)) {
          // midRegion is the Top Region
          hasMidRegion = false;
        } else {
          midRegionValue = col[midRegionFloor];
          for (index = midRegionFloor + 1; index < col.length; index++) {
            if (col[index] != midRegionValue) break;
          }
          midRegionCeiling = index - 1;
          hasMidRegion = true;
        }
      }
      if (midRegionCeiling == midRegionFloor) {
        hasMidRegion = false;
        midRegionValue = 0;
        midRegionCeiling = -1;
        midRegionFloor = -1;
      }

      // create the unique column
      if (!hasZeroRegionOnly) {
        int upperIndex = col.length - 1;
        if (hasTopRegion) {
          if (topRegionFloor == (col.length - 1)) {
            upperIndex = col.length - 1;
          } else {
            // just below the top region
            upperIndex = topRegionFloor - 1;
          }
        }

        int lowerIndex = 0;
        if (hasMidRegion) {
          lowerIndex = midRegionCeiling + 1;
        } else if (hasZeroRegion) {
          if (zeroRegionCeiling == (col.length - 1)) {
            lowerIndex = col.length - 1;
          } else {
            lowerIndex = zeroRegionCeiling + 1;
          }
        }
        int length = upperIndex - lowerIndex + 1;
        uniqueValueRegion = new int[length];
        System.arraycopy(col, lowerIndex, uniqueValueRegion, 0, length);
      }
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("item=" + itemNumber + "\n");
      sb.append("hasZeroRegion=" + hasZeroRegion + "\n");
      sb.append("hasZeroRegionOnly=" + hasZeroRegionOnly + "\n");
      sb.append("  ZR-end=" + zeroRegionCeiling + "\n");
      sb.append("hasMidRegion=" + hasMidRegion + "\n");
      sb.append("  MR-value=" + midRegionValue + "\n");
      sb.append("  MR-floor=" + midRegionFloor + "\n");
      sb.append("  MR-ceiling=" + midRegionCeiling + "\n");
      sb.append("hasTopRegion=" + hasTopRegion + "\n");
      sb.append("  TR-value=" + topRegionValue + "\n");
      sb.append("  TR-floor=" + topRegionFloor + "\n");
      if (uniqueValueRegion != null) {
      //  sb.append("unique region " + StringUtils.printIntArray(uniqueValueRegion));
      }
      return sb.toString();
    }
  }
}

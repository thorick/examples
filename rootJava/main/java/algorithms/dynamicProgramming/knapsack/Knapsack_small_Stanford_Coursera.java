package algorithms.dynamicProgramming.knapsack;

import org.apache.log4j.Logger;

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
 * Question 1
 * In this programming problem and the next you'll code up the knapsack algorithm from lecture. Let's start with a warm-up. Download the text file here. This file describes a knapsack instance, and it has the following format:
 * [knapsack_size][number_of_items]
 * [value_1] [weight_1]
 * [value_2] [weight_2]
 * ...
 * For example, the third line of the file is "50074 659",
 * indicating that the second item has value 50074 and size 659, respectively.
 * <p/>
 * You can assume that all numbers are positive.
 * <p/>
 * You should assume that item weights and the knapsack capacity are integers.
 * <p/>
 * In the box below, type in the value of the optimal solution.
 * <p/>
 *
 *
 * knapsackSize=10000
 numberOfItems=100
 minValue=261
 maxValue=99506
 minWeight=7
 maxWeight=982

  result=2493893


 *
 */
public class Knapsack_small_Stanford_Coursera implements Knapsack {

  private Logger log =
          Logger.getLogger(Knapsack_small_Stanford_Coursera.class);

  private int knapsackSize;
  private int numberOfItems;
  private int[] values;
  private int[] weights;

  private int minWeight;
  private int maxWeight;

  private int minValue;
  private int maxValue;

  private int weightRange;


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
    throw new RuntimeException("NOT implemented");
  }

  public int[][] itemsSelected() {
    throw new RuntimeException("NYI");
  }


  public int[] listSelectedItems() {
    throw new RuntimeException("NYI");
  }

  public int compute() {
    int maxValue = 0;   // keep it around
    weightRange = maxWeight - minWeight;
    int[][] valueA = new int[knapsackSize + 1][numberOfItems];

    // track the checkedWeights column of the previously processed item
    //   might use these stats to decide who needs to be kept around
    //   as computation proceeds
    boolean[] checkedWeights = new boolean[knapsackSize+1];

    // track how many weights of the previous column were ACTUALLY
    // used in the selected MAX of a current column
    boolean[] referencedWeights = new boolean[knapsackSize+1];

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
    for (int item = 0; item < numberOfItems; item++) {
      int prevCapacityIndex = 0;
      for (int b=0; b<checkedWeights.length; b++) {
        checkedWeights[b] = false;
        referencedWeights[b] = false;
      }

      for (int remainingCapacityIndex = 0; remainingCapacityIndex <= knapsackSize; remainingCapacityIndex++) {

        // get value for #1  do NOT take current item, inherit value from same capacity and previous item
        int valueCase1 = 0;
        if (item > 0) {
          valueCase1 = valueA[remainingCapacityIndex][item - 1];
          checkedWeights[remainingCapacityIndex] = true;

          log.debug("CASE 1  NO TAKE  item=" + item + ", cap=" + remainingCapacityIndex +
                  ", valueCase1=" + valueCase1);

        } else {
          // first item '0', there is no previous item
          // inherit value '0'
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
            prevCapacityValue = valueA[prevCapacityIndex][item - 1];
            checkedWeights[prevCapacityIndex] = true;

            log.debug("CASE 2  TAKE  item=" + item + ", cap=" + remainingCapacityIndex +
                    ", prevCap from index=" + prevCapacityIndex + " is=" + prevCapacityValue);
            // first item so there is no previous capacity
          }
          valueCase2 = prevCapacityValue + currItemValue;
          //valueCase2 = prevCapacityValue + currItemWeight;
        } else {
          // it's impossible to select this item because there is no room
          // set value to '0'.   impossible to select makes this part of
          // an 'empty path'.  It's not possible to even land here as part
          // of any other 'non-empty' productive path
        }

        log.debug("CASE 2  value=" + valueCase2);

        // check for and select MAX value for this cell  write cumulative value
        boolean take = false;
        int max = valueCase1;
        if (valueCase2 > valueCase1) {
          max = valueCase2;
          referencedWeights[prevCapacityIndex] = true;
          take = true;
        }
        else {
          referencedWeights[remainingCapacityIndex] = true;

        }
        valueA[remainingCapacityIndex][item] = max;

        log.debug(" SETTING TAKE="+take+",  item=" + item + ", cap=" + remainingCapacityIndex +
                ", MAX value=" + max + "\n");

        if (max > maxValue) maxValue = max;
      }
      log.debug(" DONE with item=" + item);

      log.debug("checked weights prevItem "+(item-1)+": "+printBooleanArray(checkedWeights)+"\n");
      log.debug("refercd weights prevItem "+(item-1)+": "+printBooleanArray(referencedWeights)+"\n\n");

    }

    return maxValue;
  }

  private String printBooleanArray(boolean[] bA) {
    StringBuilder sb = new StringBuilder();
    int index = 0;
    for (boolean b: bA) {
      sb.append(index+"="+b+", ");
      index++;
    }
    sb.append("\n");
    return sb.toString();

  }
  // linear off set  weight to row array index   visualization:  zero at bottom
  private int weightIndexForWeight(int weight) {
    return weight - minWeight;
  }

  private int weightForIndex(int index) {
    return index + minWeight;
  }

  protected void readDataFile(String inputFName) {

    FileReader fileR = null;
    String f = "knapsack1_Stanford_Coursera";
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
                      "\nminValue="+ minValue +
                      "\nmaxValue="+maxValue +
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
    Knapsack_small_Stanford_Coursera prog = new Knapsack_small_Stanford_Coursera();

    prog.readDataFile(null);

    int retVal = prog.compute();

    System.err.println(" result=" + retVal);
  }
}

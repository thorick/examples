package datastructures.HashTable;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/9/13
 * Time: 2:22 PM
 * <p/>
 * * The goal of this problem is to implement a variant of the 2-SUM algorithm
 * (covered in the Week 6 lecture on hash table applications).
 * <p/>
 * The file contains 1 million integers, both positive and negative
 * (there might be some repetitions!).
 * <p/>
 * This is your array of integers, with the ith row of the file specifying
 * the ith entry of the array.
 * <p/>
 * Your task is to compute the number of target values t in the interval
 * [-10000,10000] (inclusive)
 * such that there are distinct numbers x,y in the input file that satisfy x+y=t.
 * <p/>
 * (NOTE: ensuring distinctness requires a one-line addition to
 * the algorithm from lecture.)
 * <p/>
 * Write your numeric answer (an integer between 0 and 20001) in the space provided.
 * <p/>
 * OPTIONAL CHALLENGE: If this problem is too easy for you,
 * try implementing your own hash table for it.
 * <p/>
 * For example, you could compare performance under the chaining and
 * open addressing approaches to resolving collisions.
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * with the wide range of input values and the small range of result values,
 * there will be many different combinations of adding the input values
 * that may yield identical values of t.
 * We need a HashTable to know that we've already found some combination of x + y that has previously yielded t.
 * <p/>
 * <p/>
 * 1.  sort the input  O(n log(n))
 * 2.  since we want  -10000 <= t <= 10000  determine the absolute L-0 and R-0 array boundaries
 * 3.  start at L-0, for L-0 determine what R(L-0) is.  Use HashTable HS0 to count unique array vals.
 * the number of unique t in this region is n(n-1)/2
 * 4.  advance to L-1, remove L-0 from HS0, determine what R(L-1) is
 * and how many new unique values lie in the new range using a new HT1
 * the number of new t values is  unique(HS0) * unique(HT1)
 * <p/>
 * when the original HS0 HashTable becomes empty, then switch role
 * and HS0 becomes the HashTable to hold new values while HT1 becomes the table that
 * you remove old values from.
 * 5. repeat until you complete R-0: the original hard right boundary
 * 6. report the total number of computed unique sums
 */
public class HashTable_2SUM_03_ShrinkingWindow_Stanford_Coursera
        extends HashTable_2SUM_Stanford_Coursera_Base {


  private Logger log =
          Logger.getLogger(HashTable_2SUM_03_ShrinkingWindow_Stanford_Coursera.class);

  private HashSet<Long> HS0 = new HashSet<Long>();
  private HashSet<Long> HT1 = new HashSet<Long>();

  private int unique0 = 0;
  private int unique1 = 0;

  // input array boundaries
  private long valueInputArrayFirst;
  private long valueInputArrayLast;
  private int indexInputArrayLast;

  // the extremes of our calculation in the array
  //   to be computed.
  private long valueLeftMost = 0;
  private int indexLeftMost = 0;

  private long valueRightMost = 0;
  private int indexRightMost = 0;


  int indexLeftWindowStart = 0;
  int indexRightWindowStart = 0;

  int indexLeftCurrent = 0;
  int indexRightCurrent = 0;


  public HashTable_2SUM_03_ShrinkingWindow_Stanford_Coursera(FileResult fr) {
    inOrig = fr.array;
    int arraySize = fr.arraySize;


    // first step:  sort the input
    Arrays.sort(inOrig);

    valueInputArrayFirst = inOrig[0];
    indexInputArrayLast = arraySize - 1;
    valueInputArrayLast = inOrig[indexInputArrayLast];


    if (isP()) {
      // my sanity
      log.debug("print sorted input");
      for (int i = 0; i < indexInputArrayLast; i += 50) {
        log.debug("sorted[" + i + "]=" + inOrig[i]);
      }
    }


    boolean stop = false;


    if (isP())
      log.debug("input array first=" + valueInputArrayFirst + ", last[" + indexInputArrayLast + "]=" + valueInputArrayLast);

    // do the initial iteration
    // start at left = inOrig[0]
    // determine the optimum left to get a sum of -10000  keep value and index
    // determine the optimum right to get a sum of 10000  keep value and index
    //  count the number of unique values in the interval

    // next iterations
    // advance to next left number, decrement unique count
    // determine the new optimum left
    // determine the new optimum right


    // initial search
    long valueLeftCurrent = inOrig[indexLeftWindowStart];


    long valueRightStartIdeal = 0;

    if (valueLeftCurrent < 0) {
      // the idea first right value to add, will be the one that yields L + R <= UPPER  AND  L + R >= LOWER
      //    so R =  UPPER - L
      valueRightStartIdeal = UPPERSUM - valueLeftCurrent;
      if (isP())
        log.debug("initial valueRightStartIdeal is " + valueRightStartIdeal);

      // if RIGHT upper is beyond our reach, check that lower is within our reach, else we can't handle this left.
      if (valueRightStartIdeal > valueInputArrayLast) {
        long valueRightLowerBound = LOWERSUM - valueLeftCurrent;

        // even the LOWER bound is out of our range
        // find the FIRST right hand index that puts the LOWER bound within our reach
        while (valueRightLowerBound > valueInputArrayLast) {
          indexLeftWindowStart++;
          valueLeftCurrent = inOrig[indexLeftWindowStart];
          valueRightLowerBound = LOWERSUM - valueLeftCurrent;
        }
        // OK we have the smallest window start L and R that will enable us to meet the LOWER bound
        valueRightStartIdeal = valueRightLowerBound;
      }


      if (isP())
        log.debug("using indexLeftWindowStart[" + indexLeftWindowStart + "] for valueRightStartIdeal=" +
                valueRightStartIdeal);

      indexRightWindowStart = Arrays.binarySearch(inOrig, indexLeftWindowStart, indexInputArrayLast, valueRightStartIdeal);
      if (indexRightWindowStart < 0) {
        if (isP())
          log.debug("got negative index on search " + indexRightWindowStart)
                  ;
        // set the right start position, decode the returned index
        //  return value is (-(insertion point) - 1)   first index > key
        indexRightWindowStart = indexRightWindowStart + 1;
        indexRightWindowStart = -indexRightWindowStart;
      }

      if (isP())
        log.debug("using starting right index indexRightWindowStart[" + indexRightWindowStart + "]=" + inOrig[indexRightWindowStart] +
                ".  first sum will be " + (inOrig[indexLeftWindowStart] + inOrig[indexRightWindowStart] +
                "\n sanity if we had chosen the NEXT right element the first SUM would be: " +
                (inOrig[indexLeftWindowStart] + inOrig[indexRightWindowStart + 1])));
    } else {
      // the idea first right value to add, will be the one that yields L + R <= UPPER  AND  L + R >= LOWER
      //    so R =  UPPER - L

      // todo:  looks like a dup of logic above, make it common if so
      valueRightStartIdeal = UPPERSUM - valueLeftCurrent;
      if (isP())
        log.debug("initial valueRightStartIdeal is " + valueRightStartIdeal);

      // if RIGHT upper is beyond our reach, check that lower is within our reach, else we can't handle this left.
      if (valueRightStartIdeal > valueInputArrayLast) {
        long valueRightLowerBound = LOWERSUM - valueLeftCurrent;

        // even the LOWER bound is out of our range
        // find the FIRST right hand index that puts the LOWER bound within our reach
        while (valueRightLowerBound > valueInputArrayLast) {
          indexLeftWindowStart++;
          valueLeftCurrent = inOrig[indexLeftWindowStart];
          valueRightLowerBound = LOWERSUM - valueLeftCurrent;
        }
        // OK we have the smallest window start L and R that will enable us to meet the LOWER bound
        valueRightStartIdeal = valueRightLowerBound;
      }


      if (isP())
        log.debug("using indexLeftWindowStart[" + indexLeftWindowStart + "] for valueRightStartIdeal=" +
                valueRightStartIdeal);

      indexRightWindowStart = Arrays.binarySearch(inOrig, indexLeftWindowStart, indexInputArrayLast, valueRightStartIdeal);
      if (indexRightWindowStart < 0) {
        if (isP())
          log.debug("got negative index on search " + indexRightWindowStart)
                  ;
        // set the right start position, decode the returned index
        //  return value is (-(insertion point) - 1)   first index > key
        indexRightWindowStart = indexRightWindowStart + 1;
        indexRightWindowStart = -indexRightWindowStart;
      }

      if (isP())
        log.debug("using starting right index indexRightWindowStart[" + indexRightWindowStart + "]=" + inOrig[indexRightWindowStart] +
                ".  first sum will be " + (inOrig[indexLeftWindowStart] + inOrig[indexRightWindowStart] +
                "\n sanity if we had chosen the NEXT right element the first SUM would be: " +
                (inOrig[indexLeftWindowStart] + inOrig[indexRightWindowStart + 1])));


      //indexRightWindowStart = indexLeftWindowStart + 1;
    }

    indexLeftCurrent = indexLeftWindowStart;
    indexRightCurrent = indexRightWindowStart;
    while (!stop) {
      long sum = inOrig[indexLeftCurrent] + inOrig[indexRightCurrent];
      if (isP())
        log.debug("L[" + indexLeftCurrent + "] + R[" + indexRightCurrent + "] sum candidate=" + sum);

      if (sum >= LOWERSUM && sum <= UPPERSUM) {

        HS0.add(sum);
        indexRightCurrent--;
        if (indexRightCurrent > indexLeftCurrent)
          continue;    // next
      } else if (sum < LOWERSUM) {
        stop = setNextWindow();

      } else if (sum > UPPERSUM) {
        // sum not up to minimum try next right if we can
        indexRightCurrent--;
        if (indexRightCurrent <= indexLeftCurrent) {
          stop = setNextWindow();
        }
      }
    }

    int retVal = HS0.size();
    printAlways("found " + retVal + " unique values of t");


    // assuming negative values the overall left limit is
    //  find max negative left such that max-negative-left + this-right >= -10000
    //  thus if max-left is -20000 but max-right is 1000
    //  then max negative left has to start at -11000 and NOT at -20000

    // assuming positive value the overall right limit is
    //   the greater of 10000 OR (max-negative-left + (-(max-negative-left)) + 10000)
    //   find max right such that max-positive-right + this-left <= 10000
    //   thus if max-right is
    //computeLeftMost();

  }

  protected boolean isP() {
    return log.isDebugEnabled();
  }

  /**
   * shared code to set the next window for the next LEFT value
   * returns false if we are done processing
   *
   * @return
   */
  private boolean setNextWindow() {
    boolean stop = false;
    // we're done with this WINDOW corresponding to the fixed left end
    // go on until we are done
    indexLeftWindowStart++;

    if (isP())
      log.debug("try next left window[" + indexLeftWindowStart + "]=" + inOrig[indexLeftWindowStart]);

    if (indexLeftWindowStart >= indexInputArrayLast) {
      stop = true;
      return stop;
    }

    // we've fixed the new left side, now start from the previous right window start
    // and find the first index whose sum exceeds the minimum
    // this will be the new right window start
    // if we cannot do this without exceeding the maximum, then are done !
    //
    // note that since L is increasing in size,
    //   in order to find the new appropriate window we look for the next SMALLER R to fit our start criteria !
    //
    //indexRightWindowStart--;
    long candidateSum = inOrig[indexLeftWindowStart] + inOrig[indexRightWindowStart];
    if (isP())
      log.debug("candidateSum  L[" + indexLeftWindowStart + "] + R[" + indexRightWindowStart + "] = " + candidateSum);

    int indexRightWindowStartOrig = indexRightWindowStart;

    // linear search..   we can speed this up
    while (candidateSum > UPPERSUM) {
      indexRightWindowStart--;
      if (indexRightWindowStart <= indexLeftWindowStart) {

        // went through the entire interval and could not find < UPPERSUM
        // now we have to see if there's a smaller window
        // find the largest interval that exceeds the LOWERSUM
        while (candidateSum < LOWERSUM) {
          indexRightWindowStart++;
          if (indexRightWindowStart >= indexRightWindowStartOrig) {
            stop = true;
            return stop;
          }
          candidateSum = inOrig[indexLeftWindowStart] + inOrig[indexRightWindowStart];
        }

        // found the beginning of the LOWER bound now find out how far right we can go
        while (indexRightWindowStart <= indexInputArrayLast)  {
          indexRightWindowStart++;
          candidateSum = inOrig[indexLeftWindowStart] + inOrig[indexRightWindowStart];


        }

        stop = true;
        return stop;
      }
      candidateSum = inOrig[indexLeftWindowStart] + inOrig[indexRightWindowStart];
      if (isP())
        log.debug("candidateSum  L[" + indexLeftWindowStart + "] + R[" + indexRightWindowStart + "] = " + candidateSum);

    }
    // found the first index that has gone too far backwards to the left, so now nudge once over the right
    //indexRightWindowStart++;
    if (indexRightWindowStart <= indexLeftWindowStart) {
      stop = true;
      return stop;
    }

    /*
    while ((inOrig[indexLeftWindowStart] + inOrig[indexRightWindowStart]) < LOWERSUM) {
      indexRightWindowStart++;
      if (indexRightWindowStart > indexInputArrayLast) {
        stop = true;
        return stop;
      }
    }
     */
    candidateSum = inOrig[indexLeftWindowStart] + inOrig[indexRightWindowStart];
    if (isP())
      log.debug("candidate indexRightWindowStart=" + indexRightWindowStart +
              "first sum is " + candidateSum);

    if (candidateSum > UPPERSUM) {
      if (isP())
        log.debug("UPPER LIMIT EXCEEDED, WE'RE DONE !");
      stop = true;
    }
    indexLeftCurrent = indexLeftWindowStart;
    indexRightCurrent = indexRightWindowStart;
    return stop;
  }


  private void computeLeftMost() {
    valueLeftMost = valueInputArrayFirst;
    long valueLeft = valueLeftMost;
    int indexLeft = 0;

    // given the leftMost value see if there is a rightmost
    // if we are too far to the left,
    // then we set the left according to what there is on the right
    //
    if (valueInputArrayFirst < 0) {
      if (isP())
        log.debug("valueLeftMost=" + valueLeftMost + " is LESS than ZERO");

      // case the entire input array can be summed together with no restrictions
      long tentativeRight = valueLeftMost - valueLeftMost - valueLeftMost + UPPERSUM;

      if (isP())
        log.debug("tentativeRight is " + tentativeRight + ", valueLeftMost + tentativeRight = " +
                (tentativeRight + valueLeftMost));

      if (tentativeRight >= valueInputArrayLast) {

        if (isP())
          log.debug("tentativeRight exceeds largest value by " + (tentativeRight - valueInputArrayLast));

        valueRightMost = valueInputArrayLast;
        indexRightMost = indexInputArrayLast;
        valueLeftMost = valueInputArrayFirst;
        indexLeftMost = 0;
        if (isP())
          log.debug("entire array summable l[" + indexLeftMost + "]=" + inOrig[indexLeftMost] +
                  ", r[" + indexRightMost + "]=" + inOrig[indexRightMost] +
                  ", right + left = " + (valueInputArrayFirst + valueInputArrayLast));


      } else {
        // tentative right is INSIDE of the array be sure that the right side to add to get UPPERSUM
        // is in the input array


      }
    }
    // case left of input array is >= 0
    // the left most value must be no greater than UPPERSUM
    else {
      if (valueInputArrayFirst > UPPERSUM) throw new RuntimeException("smallest input value " + valueInputArrayFirst +
              " exceeds UPPERSUM limit " + UPPERSUM + " no results to be computed.");
      valueLeftMost = valueInputArrayFirst;
      indexLeftMost = 0;
    }

  }

  public static void main(String[] args) {
    FileResult fr = readDataFile(null);

    HashTable_2SUM_03_ShrinkingWindow_Stanford_Coursera runner = new HashTable_2SUM_03_ShrinkingWindow_Stanford_Coursera(fr);
  }

}

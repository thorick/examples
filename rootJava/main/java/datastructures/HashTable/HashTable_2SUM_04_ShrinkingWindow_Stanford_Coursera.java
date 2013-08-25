package datastructures.HashTable;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/9/13
 * Time: 2:22 PM
 *  
 * * The goal of this problem is to implement a variant of the 2-SUM algorithm
 * (covered in the Week 6 lecture on hash table applications).
 *  
 * The file contains 1 million integers, both positive and negative
 * (there might be some repetitions!).
 *  
 * This is your array of integers, with the ith row of the file specifying
 * the ith entry of the array.
 *  
 * Your task is to compute the number of target values t in the interval
 * [-10000,10000] (inclusive)
 * such that there are distinct numbers x,y in the input file that satisfy x+y=t.
 *  
 * (NOTE: ensuring distinctness requires a one-line addition to
 * the algorithm from lecture.)
 *  
 * Write your numeric answer (an integer between 0 and 20001) in the space provided.
 *  
 * OPTIONAL CHALLENGE: If this problem is too easy for you,
 * try implementing your own hash table for it.
 *  
 * For example, you could compare performance under the chaining and
 * open addressing approaches to resolving collisions.
 *  
 *  
 *  
 *  
 * with the wide range of input values and the small range of result values,
 * there will be many different combinations of adding the input values
 * that may yield identical values of t.
 * We need a HashTable to know that we've already found some combination of x + y that has previously yielded t.
 *  
 *  
 * 1.  sort the input  O(n log(n))
 * 2.  since we want  -10000 <= t <= 10000  determine the absolute L-0 and R-0 array boundaries
 * 3.  start at L-0, for L-0 determine what R(L-0) is.  Use HashTable HS0 to count unique array vals.
 * the number of unique t in this region is n(n-1)/2
 * 4.  advance to L-1, remove L-0 from HS0, determine what R(L-1) is
 * and how many new unique values lie in the new range using a new HT1
 * the number of new t values is  unique(HS0) * unique(HT1)
 *  
 * when the original HS0 HashTable becomes empty, then switch role
 * and HS0 becomes the HashTable to hold new values while HT1 becomes the table that
 * you remove old values from.
 * 5. repeat until you complete R-0: the original hard right boundary
 * 6. report the total number of computed unique sums
 */
public class HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera
        extends HashTable_2SUM_Stanford_Coursera_Base {


  private Logger log =
          Logger.getLogger(HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera.class);

  private HashSet<Long> HS0 = new HashSet<Long>();


  // input array boundaries
  private long valueInputArrayFirst;
  private long valueInputArrayLast;
  private int indexInputArrayLast;

  // the extremes of our calculation in the array
  //   to be computed.

  int indexLeftWindowStart = 0;
  int indexRightWindowStart = 0;

  int indexLeftCurrent = 0;
  int indexRightCurrent = 0;


  long valueLeftStopComputation = UPPERSUM/2 + 1;

  public HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera(FileResult fr) {
    inOrig = fr.array;
    int arraySize = fr.arraySize;

    // first step:  sort the input
    Arrays.sort(inOrig);

    valueInputArrayFirst = inOrig[0];
    indexInputArrayLast = arraySize - 1;
    valueInputArrayLast = inOrig[indexInputArrayLast];

    boolean stop = false;

    if (isP())
      log.debug("input array first=" + valueInputArrayFirst + ", last[" + indexInputArrayLast + "]=" + valueInputArrayLast);


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
          log.debug("got negative index on search " + indexRightWindowStart);
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
    }

    indexLeftCurrent = indexLeftWindowStart;
    indexRightCurrent = indexRightWindowStart;

    while (!stop) {

      // we really should have caught this before here, but we haven't
      if (indexLeftCurrent >= indexRightCurrent) {
        break;
      }
      long sum = inOrig[indexLeftCurrent] + inOrig[indexRightCurrent];
      if (isP()) {
        log.debug("sum candidate L[" + indexLeftCurrent + "] + R[" + indexRightCurrent + "] "+inOrig[indexLeftCurrent]+
                " + "+inOrig[indexRightCurrent]+" = " + sum);
      }
      if (sum >= LOWERSUM && sum <= UPPERSUM) {

        if (inOrig[indexLeftCurrent] != inOrig[indexRightCurrent])  {
        HS0.add(sum);
          if (isP())
                    log.debug("added sum "+sum+" to HashSet.  try next R["+indexRightCurrent+"]\n");
        }
        else {
          if (isP())
            log.debug("skipping sum = "+sum+" because L=R, L "+inOrig[indexLeftCurrent]+" = R "+inOrig[indexRightCurrent]);
        }

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
  }

  protected boolean isP() {
    return log.isDebugEnabled();
  }

  /**
   * shared code to set the next window for the next LEFT value
   * returns false if we are done processing
   *
   * @return  stop = true  if we're done
   */
  private boolean setNextWindow() {
    String m = "setNextWindow: ";
    boolean foundWindow = false;
    boolean stop = false;

    while (!foundWindow) {
      foundWindow = windowNextBiggerLeft();

      long leftValue = inOrig[indexLeftWindowStart];
      if (leftValue >= valueLeftStopComputation) {
        if (isP()) {
          log.debug("at L["+indexLeftWindowStart+"]="+leftValue+", UPPER="+UPPERSUM+", exceeds 1/2 UPPER="+valueLeftStopComputation+
          "  We are DONE.");
        }
        return true;
      }
      if (indexLeftWindowStart >= indexRightWindowStart) {
               // we've exhausted the array done 1/2, we're done
               return true;
             }
      if (!foundWindow) {
        if (indexLeftWindowStart >= indexRightWindowStart) {
          // we've exhausted the array done 1/2, we're done
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Does an trial search of a window for the NEXT left index array.
   *
   * @return true  if we found a window for the NEXT left index array
   *         false  if we have not
   */
  private boolean windowNextBiggerLeft() {
    String m = "windowNextBiggerLeft: ";
    boolean stop = false;
    int indexRightWindowStartOrig = indexRightWindowStart;

    // we're done with this WINDOW corresponding to the fixed left end
    // go on until we are done
    indexLeftWindowStart++;

    if (indexLeftWindowStart >= indexInputArrayLast) {
      return false;
    }

    // we've fixed the new left side, now start from the previous right window start
    // and find the first index whose sum exceeds the minimum
    // this will be the new right window start
    // if we cannot do this without exceeding the maximum, then are done !
    //
    // note that since L is increasing in size,
    //   in order to find the new appropriate window we look for the next SMALLER R to fit our start criteria !
    //
    // new RHS outer bound would satisfy  LwindowStart + RwindowStart = UPPERSUM
    //   so RwindowStart = UPPERSUM - LwindowStart

    long valueRightStartIdeal = UPPERSUM - inOrig[indexLeftWindowStart];
    indexRightWindowStart = Arrays.binarySearch(inOrig, indexLeftWindowStart, indexRightWindowStartOrig, valueRightStartIdeal);

    if (indexRightWindowStart < 0) {
      if (isP())
        log.debug(m + "got negative index on search " + indexRightWindowStart)
                ;
      // set the right start position, decode the returned index
      //  return value is (-(insertion point) - 1)   first index > key
      indexRightWindowStart = indexRightWindowStart + 1;
      indexRightWindowStart = -indexRightWindowStart;

      if (isP())
        log.debug(m+"R["+indexRightWindowStart+"], for sanity, push rightIndex to next largest value: R["+(indexRightWindowStart+1)+
                "]="+inOrig[indexRightWindowStart+1]);
      indexRightWindowStart++;
    }

    long candidateSum = inOrig[indexLeftWindowStart] + inOrig[indexRightWindowStart];

    if (isP())
      log.debug(m + "valueRStartIdeal=" + valueRightStartIdeal + ", got[" + indexRightWindowStart + "]=" + inOrig[indexRightWindowStart]);

    if (isP())
      log.debug( "    for L[" + indexLeftWindowStart + "]=" + inOrig[indexLeftWindowStart] +
              ", computed new R[" + indexRightWindowStart + "]=" + inOrig[indexRightWindowStart] +
              ", with sum=" + candidateSum);


    while (candidateSum > UPPERSUM) {
      indexRightWindowStart--;
      if (indexRightWindowStart <= indexLeftWindowStart) {
        if (isP())
          log.debug(m + " right index decreased to left index..  we're DONE.\n");
        return false;
      }
      candidateSum = inOrig[indexLeftWindowStart] + inOrig[indexRightWindowStart];
      if (isP()) {
        log.debug(m + "L[" + indexLeftWindowStart + "], next smaller R[" + indexRightWindowStart + "], got sum=" + candidateSum);
      }
    }

    if (indexRightWindowStart <= indexLeftWindowStart) {
      if (isP())
        log.debug(m + "rightIndex=" + indexRightWindowStart + " is LESS than leftIndex=" + indexLeftWindowStart + ". we are DONE.\n");
      return false;
    }

    if (candidateSum < LOWERSUM) {
      if (isP())
        log.debug(m + "DONE.  sum < " + LOWERSUM + " did not find a window for L[" + indexLeftWindowStart+"], R["+
        indexRightWindowStart+"]\n");
      indexRightWindowStart = indexRightWindowStartOrig;
      return false;
    }

    indexLeftCurrent = indexLeftWindowStart;
    indexRightCurrent = indexRightWindowStart;
    if (isP())
      log.debug(m + " DONE.  new window selected. L{"+indexLeftCurrent+"], R["+indexRightWindowStart+"]\n\n");
    return true;
  }

  public static void main(String[] args) {
    FileResult fr = readDataFile(null);

    HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera runner = new HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera(fr);
  }

  /**
   *
   *
   * CORRECT !
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:244 - L[499990] + R[499991] sum candidate=0
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:252 - added sum 0 to HashSet.  try next R[499990]

   load graph elapsed time: 595 millis.
   found 427 unique values of t

   */

  /**
   *
   *
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:316 - windowNextBiggerLeft: got negative index on search -500500
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:324 - windowNextBiggerLeft: R[500499], for sanity, push rightIndex to next largest value: R[500500]=104647828
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:332 - windowNextBiggerLeft: valueRStartIdeal=104115706, got[500500]=104647828
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:335 -     for L[499452]=-104105706, computed new R[500500]=104647828, with sum=542122
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:349 - windowNextBiggerLeft: L[499452], next smaller R[500499], got sum=459979
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:349 - windowNextBiggerLeft: L[499452], next smaller R[500498], got sum=371
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:370 - windowNextBiggerLeft:  DONE.  new window selected. L{499452], R[500498]


   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:208 - sum candidate L[499452] + R[500498] -104105706 + 104106077 = 371
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:215 - sanity with difference R[500498] and R[500497] = 317953
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:218 - sum - prevSum 371 - -9780 = 10151
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:223 - added sum 371 to HashSet.  try next R[500497]

   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:208 - sum candidate L[499452] + R[500497] -104105706 + 103788124 = -317582
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:316 - windowNextBiggerLeft: got negative index on search -500499
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:324 - windowNextBiggerLeft: R[500498], for sanity, push rightIndex to next largest value: R[500499]=104565685
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:332 - windowNextBiggerLeft: valueRStartIdeal=103805762, got[500499]=104565685
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:335 -     for L[499453]=-103795762, computed new R[500499]=104565685, with sum=769923
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:349 - windowNextBiggerLeft: L[499453], next smaller R[500498], got sum=310315
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:349 - windowNextBiggerLeft: L[499453], next smaller R[500497], got sum=-7638
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:370 - windowNextBiggerLeft:  DONE.  new window selected. L{499453], R[500497]


   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:208 - sum candidate L[499453] + R[500497] -103795762 + 103788124 = -7638
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:215 - sanity with difference R[500497] and R[500496] = 892766
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:218 - sum - prevSum -7638 - 371 = -8009
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:223 - added sum -7638 to HashSet.  try next R[500496]

   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:208 - sum candidate L[499453] + R[500496] -103795762 + 102895358 = -900404
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:316 - windowNextBiggerLeft: got negative index on search -500498
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:324 - windowNextBiggerLeft: R[500497], for sanity, push rightIndex to next largest value: R[500498]=104106077
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:332 - windowNextBiggerLeft: valueRStartIdeal=103720732, got[500498]=104106077
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:335 -     for L[499454]=-103710732, computed new R[500498]=104106077, with sum=395345
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:349 - windowNextBiggerLeft: L[499454], next smaller R[500497], got sum=77392
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:349 - windowNextBiggerLeft: L[499454], next smaller R[500496], got sum=-815374
   HashTable_2SUM_04_ShrinkingWindow_Stanford_Coursera,main:361 - windowNextBiggerLeft: DONE.  sum < -10000 did not find a window for L[499454], R[500496]



   */

}

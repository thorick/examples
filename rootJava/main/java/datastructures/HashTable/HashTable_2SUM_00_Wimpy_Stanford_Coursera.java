package datastructures.HashTable;

import datastructures.graph.basic.GraphAdjList;
import datastructures.graph.util.GraphUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/7/13
 * Time: 8:41 PM
 * <p/>
 * <p/>
 * The goal of this problem is to implement a variant of the 2-SUM algorithm
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
 * my notes:
 * t  is arbitrary and TDB, it's cardinality is what we want to know
 * thus our task is to find all the values of t.
 * <p/>
 * after we've found a pair x,y where x != y, such that x+y=t, don't waste any time
 * trying to find y+x=t.  If the array is sorted, then we don't have to worry about this.
 * <p/>
 *
 * THIS IS STUPID !   lot's of unnecessary addition
 *
 *   instead:  since you know the desired range of 't', you know the lower and upper
 *             boundaries that you are concerned about
 *             you need to determine these boundaries and only uniquely
 *             add together what will lie within the boundaries
 *             (and also skip redundant adds that you have already done).
 *
 *
 * the naive way is to:
 * sort the array and (don't do this if num dups is small, instead
 * just remember the last values and skip to next if already processed !
 * weed out duplicate values)
 * start at num[0] and march along num[i] adding each unique sum to a HashTable
 * then do it again for n[1] marching from n[2], etc...
 */

// Completely Wimpy version
//   use library HashTable
//   use library Sort
//
public class HashTable_2SUM_00_Wimpy_Stanford_Coursera extends HashTable_2SUM_Stanford_Coursera_Base{


  private Logger log =
          Logger.getLogger(HashTable_2SUM_00_Wimpy_Stanford_Coursera.class);





  public HashTable_2SUM_00_Wimpy_Stanford_Coursera(FileResult fr) {

    inOrig = fr.array;

    // first step:  sort the input
    Arrays.sort(inOrig);

    // now start the march
    int stop = inOrig.length - 1;
    long prevI = Long.MAX_VALUE;
    long prevJ = Long.MAX_VALUE;
    boolean dupCheckI = false;
    boolean dupCheckJ = false;
    for (int i = 0; i < stop; i++) {
      if (dupCheckI) {
        if (prevI == inOrig[i]) {
          continue;
        } else {
          prevI = inOrig[i];
          dupCheckI = true;
        }
      }
      if (i % 1000 == 0)
        log.debug("processing i="+i);

      for (int j = (i + 1); j < inOrig.length; j++) {
        if (dupCheckJ) {
          if (prevJ == inOrig[j]) {
            continue;
          } else {
            prevJ = inOrig[j];
            dupCheckJ = true;
          }
        }
        long sum = inOrig[i] + inOrig[j];
        if (sum >= -10000 && sum <= 10000) {
          Long Sum = new Long(inOrig[i] + inOrig[j]);

          tValues.add(sum);
        }

      }
    }

    // when we're done, we have all of the values of t
    int result = tValues.size();
    printAlways("found "+result+" t values meeting the crieria");
  }


  protected boolean isP() {
    return log.isDebugEnabled();
  }

  public static void main(String[] args) {
    FileResult fr = readDataFile(null);

    HashTable_2SUM_00_Wimpy_Stanford_Coursera runner = new HashTable_2SUM_00_Wimpy_Stanford_Coursera(fr);
  }




}

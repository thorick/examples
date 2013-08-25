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
public abstract class HashTable_2SUM_Stanford_Coursera_Base {


  protected final long LOWERSUM = -10000;
  protected final long UPPERSUM = 10000;

  protected long[] inOrig;
  private long[] sorted;

  // unique values of t
  protected HashSet<Long> tValues = new HashSet<Long>();

  protected static FileResult readDataFile(String inputFName) {
    //
    //   we already know that the input is 10**6 in size
    //
    long start = System.currentTimeMillis();

    long[] list = new long[1000002];



    FileReader fileR = null;
    String f = "algo1_programming_prob_2sum.txt";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\HashTable";
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

    // todo:  major:  need to load the edge values now !
    //        Edge data will be Integer Object
    int i=0;
    try {
      BufferedReader br = new BufferedReader(fileR);
      String line;

      while ((line = br.readLine()) != null) {
        if (i % 10000 == 0) {
          printAlways("read line '" + line + "'");
        }
        list[i] = Long.parseLong(line);

        i++;

      }
      br.close();
    } catch (IOException e) {
      System.err.println("exception " + e.getMessage());
    }
    long end = System.currentTimeMillis();
    printAlways("load graph elapsed time: " + (end - start) + " millis.");
    return new FileResult(i, list);
  }

  protected static Long[] readDataFileAsObjects(String inputFName) {
    //
    //   we already know that the input is 10**6 in size
    //
    long start = System.currentTimeMillis();

    Long[] list = new Long[1000002];

    //GraphAdjList graph = new GraphAdjList(875720, true);


    FileReader fileR = null;
    String f = "algo1_programming_prob_2sum.txt";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\HashTable";
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

    // todo:  major:  need to load the edge values now !
    //        Edge data will be Integer Object

    try {
      BufferedReader br = new BufferedReader(fileR);
      String line;

      int i = 0;
      while ((line = br.readLine()) != null) {
        System.err.println("read line '" + line + "'");
        list[i] = Long.parseLong(line);

        i++;
        if (i % 10000 == 0) {
          //  System.err.println("read vertex #" + i + " = " + i);
        }
      }
      br.close();
    } catch (IOException e) {
      System.err.println("exception " + e.getMessage());
    }
    long end = System.currentTimeMillis();
    printAlways("load graph elapsed time: " + (end - start) + " millis.");
    return list;
  }

  protected static void printAlways(String s) {
    System.err.println(s);
  }

  static class FileResult {
    int arraySize = 0;
    long[] array;

    FileResult(int i, long[] a) {
      arraySize = i;
      array = a;
    }
  }

  /*
  HashTable_2SUM_01_UniqueSections_Stanford_Coursera,main:85 - input array first=-99999887310, last[999999]=99999371636



  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[499350]=-126828673
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[499400]=-114276680
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[499450]=-104267570
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[499500]=-95336000
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[499550]=-87957690
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[499600]=-77286410
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[499650]=-68636472
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[499700]=-59662713
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[499750]=-49604895
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[499800]=-38046996
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[499850]=-26749587
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[499900]=-16555144
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[499950]=-8561882
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500000]=2770265
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500050]=12799305
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500100]=22427504
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500150]=32907491
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500200]=41171303
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500250]=50661014
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500300]=60111237
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500350]=70576509
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500400]=81263342
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500450]=91861607
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500500]=104647828
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500550]=112790009
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500600]=125085143
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500650]=134702725
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500700]=142325507
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500750]=149109354
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500800]=160716334
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500850]=167234195
  HashTable_2SUM_02_Windows_Stanford_Coursera,main:110 - sorted[500900]=179394008



   */

}

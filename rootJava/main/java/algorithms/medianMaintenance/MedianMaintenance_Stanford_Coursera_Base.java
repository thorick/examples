package algorithms.medianMaintenance;

import datastructures.priorityQueue.PriorityQueue_ArrayHeapMin_Integer;
import datastructures.priorityQueue.PriorityQueue_ArrayHeap_Integer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/7/13
 * Time: 8:41 PM
 *
 * The goal of this problem is to implement the "Median Maintenance" algorithm
 *  (covered in the Week 5 lecture on heap applications).
 *
 *  The text file contains a list of the integers from 1 to 10000 in unsorted order;
 *  you should treat this as a stream of numbers, arriving one by one. Letting xi denote
 *  the ith number of the file, the kth median mk is defined as the median of the numbers x1,…,xk.
 *
 *  (So, if k is odd, then mk is ((k+1)/2)th smallest number among x1,…,xk;
 *
 *  if k is even,
 *  then mk is the (k/2)th smallest number among x1,…,xk.)

 In the box below you should type the sum of these 10000 medians, modulo 10000

 (i.e., only the last 4 digits). That is, you should compute (m1+m2+m3+⋯+m10000)mod10000.


 OPTIONAL EXERCISE: Compare the performance achieved by heap-based and search-tree-based implementations of the algorithm.


 recall:  use 2 heaps a MIN heap and a MAX heap
            keep the  lowest  N/2 in MAX heap
            keep the  highest N/2 in the MIN heap

    keep heap sizes balanced, when one heap larger than the other, transfer objects from one heap
    to another.

 N even both heaps have median
 N odd  the larger heap has the median



 */

//
public abstract class MedianMaintenance_Stanford_Coursera_Base {


  protected int HEAPSIZE = 10051;



  protected static FileResult readDataFile(String inputFName) {
    //
    //   we already know that the input is 10**6 in size
    //
    long start = System.currentTimeMillis();

    int[] list = new int[10050];



    FileReader fileR = null;
    String f = "Median.txt";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\algorithms\\MedianMaintenance";
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
        if (i % 1000 == 0) {
          printAlways("read line '" + line + "'");
        }
        list[i] = Integer.parseInt(line);

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



  protected static void printAlways(String s) {
    System.err.println(s);
  }

  static class FileResult {
    int arraySize = 0;
    int[] array;

    FileResult(int i, int[] a) {
      arraySize = i;
      array = a;
    }
  }

  /*

   */

}

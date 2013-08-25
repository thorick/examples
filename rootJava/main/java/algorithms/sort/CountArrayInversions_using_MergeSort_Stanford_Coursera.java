package algorithms.sort;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/13/13
 * Time: 3:32 PM
 *
 * Coursera  Programming Assignment 1
 *
 * Question 1
 *  Download the text file here. (
 *  Right click and save link as)

 *  This file contains all of the 100,000 integers between 1 and 100,000
 *  (inclusive) in some order, with no integer repeated.

 *  Your task is to compute the number of inversions in the file given,
 *  where the ith row of the file indicates the ith entry of an array.
 *  Because of the large size of this array, you should implement the
 *  fast divide-and-conquer algorithm covered in the video lectures.
 */
public class
        CountArrayInversions_using_MergeSort_Stanford_Coursera {

  public long countTotalInversions(int[] input) {
    String rType = "ROOT";
    int  cNum = 0;
    int  recurseLevel = 0;
    CountResult result = countInversions(input, rType, recurseLevel, cNum);

    return result.getCount();
  }


  /**
   *
   * Recursive routine to count array inversions
   *
   * The input array is assumed to be larger than the size required
   * to hold the input to be processed.
   *
   * The returned CountResult contains a dataOut array that is exactly
   * sized to hold only the results.
   *
   * Recursion is done when there are 2 or less elements to consider:
   *   we handle that terminal case specially and return.
   *
   *
   * @param input
   * @param rType           for debugging   name for operation
   * @param recurseLevel    for debugging   recursion level
   * @param cNum            for debugging   unique stack frame number
   *
   * @return
   */
  public CountResult countInversions(int[] input, String rType, int recurseLevel, int cNum) {
    int size = input.length;
    int sizeL = size/2;
    int sizeR = size - sizeL;     //  sizeL and sizeR could be different
                                  //  is size is odd.

    String thisOp = rType+":"+cNum;

    p(thisOp+"  Start countInv: "+thisOp+"  size="+size+", sL="+sizeL+", sR="+sizeR+
    "\n   input "+pInts(input));

    recurseLevel++;
    long startTime = System.currentTimeMillis();

    q("Recursive Level "+recurseLevel+" START");

    // end of recursion
    // order and count inversions
    if (sizeL <= 1 && sizeR <= 1) {
      p(thisOp+"  final step !");
      long inversionCount = 0;
      int[] resultArray = new int[size];
      if (sizeR > 0) {
        if (sizeL > 0) {
          if (input[0] <= input[1]) {
            // non-inversion case L <= R
            resultArray[0] = input[0];
            resultArray[1] = input[1];
            p(thisOp+"   L < R  no inversion");
          }
          else {
            // inversion case L < R
            resultArray[0] = input[1];
            resultArray[1] = input[0];
            inversionCount++;
            p(thisOp+"  L > R  inversion, swap and tally");
          }
        }
        else {
          // R but no L
          resultArray[0] = input[0];
          p(thisOp+"  single element only.");
        }
      }
      else {
        // it's not possible for R < 1
        // the array splitting favors right RHS
        p(thisOp+"   ERROR in L only  section !");
      }
      CountResult cr = new CountResult();
      cr.setDataOut(resultArray);
      cr.setCount(inversionCount);
      p((thisOp+"  result="+pInts(resultArray)));

      long endTime = System.currentTimeMillis();
      long totalTime = (endTime - startTime) / 1000;
      q("Recursive Level "+recurseLevel+" (last level) DONE.   inversions="+inversionCount+" in "+totalTime+" seconds.");
      return cr;
    }

    //
    // recursive case
    //
    int[] arrayL = new int[sizeL];
    int[] arrayR = new int[sizeR];
    System.arraycopy(input, 0, arrayL, 0, sizeL);
    System.arraycopy(input, sizeL, arrayR, 0, sizeR);

    CountResult resultL = countInversions(arrayL, "cl", recurseLevel, ++cNum);
    long countL = resultL.getCount();
    p(thisOp+" L inversions="+countL);

    CountResult resultR = countInversions(arrayR, "cr", recurseLevel, ++cNum);
    long countR = resultR.getCount();
    p(thisOp+" R inversions="+countR);

    CountResult resultSplit = countSplitInversions(resultL, resultR, recurseLevel, ++cNum);
    long countSplit = resultSplit.getCount();
    p(thisOp+" Split inversions="+countSplit);

    resultSplit.setCount(countL + countR + countSplit);
    long endTime = System.currentTimeMillis();
    long totalTime = (endTime - startTime) / 1000;

    q("Recursive Level "+recurseLevel+" DONE.   inversions="+resultSplit.getCount()+" in "+totalTime+" seconds.");
    return resultSplit;

  }

  /**
   * This routine merges the sorted results and counts array inversions.
   *
   * The result array dataOut is the merged array.
   * dataOut is of the exact size of it's contents.
   *
   * This routine runs in O(n)  where n is the sum of the Left and Right array sizes.
   *
   *
   * @param resultL
   * @param resultR
   * @return
   */
  public CountResult countSplitInversions(CountResult resultL, CountResult resultR, int recurseLevel, int rNum) {
    int inversionCount = 0;

    int[] left = resultL.getDataOut();
    int[] right = resultR.getDataOut();


    int leftSize = left.length;
    int rightSize = right.length;
    int totalSize = leftSize + rightSize;
    int leftIndex = 0;
    int rightIndex = 0;

    int[] result = new int[totalSize];

    String thisOp = "CSplit:"+rNum;

    // we have to be careful that one array may be larger than the other
    int maxSize = (leftSize > rightSize) ? leftSize : rightSize;

    p(thisOp+" lSize="+leftSize+", rSize="+rightSize+" totalSize="+totalSize+
      "\n    L="+pInts(left)+
      "\n    R="+pInts(right));

    for (int resultIndex=0; resultIndex < totalSize; resultIndex++) {
      if (leftIndex >= leftSize) {
        // only right input remaining
        result[resultIndex] = right[rightIndex];
        p(thisOp+"  handle onlyR ");
        rightIndex++;
      }
      else if (rightIndex >= rightSize) {
        // only left input remaining
        result[resultIndex] = left[leftIndex];
        p(thisOp+"  handle onlyL");
        leftIndex++;
      }
      else if (left[leftIndex] < right[rightIndex]) {
        // case:  no inversion
        result[resultIndex] = left[leftIndex];
        p(thisOp+"  no inversion");
        leftIndex++;
      }
      else {
        // case: right >= left  inversions !
        //     note that equality is an inversion
        //        the equality case is different if L and R are each of size == 1
        //          because in that case it's not really an inversion
        //          but that special case is handled in the end-of-recursion case
        //
        //  tally the inversions !
        //  this right element is ahead of how many left elements ?
        //  that is the inversion tally.

        //  0 - 1 - 2  - 3
        //  if size is 4 and leftIndex is 1 then there are 4-1=3 inversions
        //

        inversionCount += (leftSize - leftIndex);
        result[resultIndex] = right[rightIndex];
        p(thisOp+"  default:   handle inversion: for R["+rightIndex+"]="+right[rightIndex]+" detected "+
                (leftSize - leftIndex)+" inversions.  tallied now "+inversionCount);
        rightIndex++;

      }
      p(thisOp+" result is now "+pInts(result));
    }

    CountResult retVal = new CountResult();
    retVal.setDataOut(result);
    retVal.setCount(inversionCount);
    p(thisOp+"   result="+pInts(result));
    return retVal;
  }


  public class CountResult {
    private long inversionCount;
    private int[] dataOut;   // only created for countSplitInversions

    private CountResult() { }

    public long getCount() {
      return inversionCount;
    }

    public void setCount(long i) {
      inversionCount = i;
    }

    public void setDataOut(int[] val) {
      dataOut = val;
    }

    public int[] getDataOut() {
      return dataOut;
    }
  }

  /*
CountArrayInversions: Recursive Level 17 START
CountArrayInversions: Recursive Level 17 (last level) DONE.   inversions=0 in 0 seconds.
CountArrayInversions: Recursive Level 16 DONE.   inversions=1 in 0 seconds.
CountArrayInversions: Recursive Level 15 DONE.   inversions=4 in 0 seconds.
CountArrayInversions: Recursive Level 14 DONE.   inversions=33 in 0 seconds.
CountArrayInversions: Recursive Level 13 DONE.   inversions=146 in 0 seconds.
CountArrayInversions: Recursive Level 12 DONE.   inversions=593 in 0 seconds.
CountArrayInversions: Recursive Level 11 DONE.   inversions=2480 in 0 seconds.
CountArrayInversions: Recursive Level 10 DONE.   inversions=9435 in 0 seconds.
CountArrayInversions: Recursive Level 9 DONE.   inversions=36968 in 0 seconds.
CountArrayInversions: Recursive Level 8 DONE.   inversions=151249 in 0 seconds.
CountArrayInversions: Recursive Level 7 DONE.   inversions=605286 in 0 seconds.
CountArrayInversions: Recursive Level 6 DONE.   inversions=2428363 in 1 seconds.
CountArrayInversions: Recursive Level 5 DONE.   inversions=9749297 in 4 seconds.
CountArrayInversions: Recursive Level 4 DONE.   inversions=39394307 in 16 seconds.
CountArrayInversions: Recursive Level 3 DONE.   inversions=159315221 in 64 seconds.
CountArrayInversions: Recursive Level 2 DONE.   inversions=631341882 in 256 seconds.
CountArrayInversions: Recursive Level 1 DONE.   inversions=2407905288 in 1035 seconds.
found 2407905288 inversions.  in 1035 seconds.

  This is the correct answer !

   */
  public static void main(String[] args) {
    // 100,000 is a little less than 2**17
    //           so 17 levels of recursion
    //
    int[] data = new int[110000];  // should be big enough

    FileReader fileR = null;
    String f = "CountArrayInversions_using_MergeSort_Stanford_Coursera_IntegerArray.txt";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources";
    String fileName = d + "\\" + f;
    if (args.length > 0) {
      fileName = args[0];
    }
    try {
      fileR = new FileReader(fileName);
    } catch (FileNotFoundException e) {
      System.err.println(" cannot open data file "+fileName);
    }

    // get count so that we can build only the array we need
    try {
      BufferedReader br = new BufferedReader(fileR);
      String line;

      int i = 0;
      while ((line = br.readLine()) != null) {
        int number = Integer.parseInt(line);
        data[i] = number;
        i++;
        if (i % 1000 == 0) {
          System.err.println("read integer #"+i+" = "+number);
        }
      }
      br.close();

      int[] dataNew = new int[i];
      System.arraycopy(data, 0, dataNew, 0, i);

      long startTime = System.currentTimeMillis();

      CountArrayInversions_using_MergeSort_Stanford_Coursera counter =
              new CountArrayInversions_using_MergeSort_Stanford_Coursera();

      long result = counter.countTotalInversions(dataNew);
      long endTime = System.currentTimeMillis();

      long totalTime = (endTime - startTime) / 1000;
      System.err.println("found "+result+" inversions.  in "+totalTime+" seconds.");

    }  catch (IOException e)  {
      System.err.println("exception "+e.getMessage());
    }

  }

  private static String pInts(int[] in) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < in.length; i++) {
      sb.append(Integer.toString(in[i])).append(", ");
    }
    return sb.toString();
  }

  private static void p(String s) {
    //System.err.println("CountArrayInversions: "+s);
  }

  private static void q(String s) {
    System.err.println("CountArrayInversions: "+s);
  }
}

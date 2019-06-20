package _codefights;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 6/30/2018
 * Time: 9:15 AM
 */
public class CodeFights_Interview_HeapStackQueue {

  // IMPORTANT CODE THING TO REMEMBER:
  //   WHEN CODING A HEAP FOR DOING THE SWAP DOWN
  //   YOU MUST CHOOSE THE LARGER OF THE TWO SIBLINGS TO SWAP WITH
  //
  //  #1
  //   https://codefights.com/interview-practice/task/BG94ZFECSNo6Kv7XW
  //
  /*
       Note: Avoid using built-in std::nth_element (or analogous built-in functions in languages other than C++)
       when solving this challenge. Implement them yourself, since this is what you would be asked to do during a real interview.

       Find the kth largest element in an unsorted array.
       This will be the kth largest element in sorted order, not the kth distinct element.

       Example

           For nums = [7, 6, 5, 4, 3, 2, 1] and k = 2, the output should be
           kthLargestElement(nums, k) = 6;
           For nums = [99, 99] and k = 1, the output should be
           kthLargestElement(nums, k) = 99.

   */

  //
  // for the exercise we write our own int array based heap and use it
  // The heap is more efficient than a full sort if we are not asking to
  // find the smallest number in the set (because that would require as many compares as a full sort)
  //  (I believe that's true, not going to prove it right now)
  //
  // Of course the heap is dynamic while sort is not.
  //
  static int kthLargestElement(int[] nums, int k) {
    if (nums == null || nums.length == 0) {
      return 0;
    }
    IntHeap heap = new IntHeap();
    for (int i : nums) {
      heap.put(i);
    }

    if (k > nums.length) {
      k = nums.length;
    }

    if (k == 1 && nums.length == 1) {
      return nums[0];
    }
    for (int i = 0; i < (k - 1); i++) {
      int throwaway = heap.get();
    }
    return heap.get();
  }

  static class IntHeap {
    int size;
    int[] elements;
    int nextIndex = 1;

    IntHeap() {
      size = 100002;     // because I'm a cautious sort...    be sure to be able to handle  10**5 inputs
      elements = new int[size + 1];
    }

    void put(int i) {
      int currIndex = nextIndex++;
      elements[currIndex] = i;
      boolean done = false;
      // swap up while you are greater than your parent
      while (!done && currIndex > 1) {
        int parentIndex = currIndex / 2;
        if (elements[currIndex] > elements[parentIndex]) {
          int temp = elements[currIndex];
          elements[currIndex] = elements[parentIndex];
          elements[parentIndex] = temp;
          currIndex = parentIndex;
        } else {
          done = true;
        }
      }
    }

    // -1   if empty
    int get() {
      if (nextIndex <= 1) {
        return -1;
      }
      int result = elements[1];
      int currIndex = 1;
      elements[currIndex] = elements[--nextIndex];
      boolean done = false;
      // swap down while you are less than your children
      while (!done && (currIndex * 2) < nextIndex) {
        int chosenIndex = currIndex * 2;
        if ((chosenIndex + 1) <= nextIndex) {
          // swap up the larger of 2 siblings
          if (elements[chosenIndex + 1] > elements[chosenIndex]) {
            chosenIndex = chosenIndex + 1;
          }
        }
        if (elements[chosenIndex] > elements[currIndex]) {
          int temp = elements[currIndex];
          elements[currIndex] = elements[chosenIndex];
          elements[chosenIndex] = temp;
          currIndex = chosenIndex;
        } else {
          done = true;
        }
      }
      return result;
    }
  }

  public static void main(String[] args) {
    String test = "";
    int[] input = null;
    int k = 0;
    int output = 0;
    String expected = "";


    test = "test1";
    input = new int[] {7, 6, 5, 4, 3, 2, 1};
    k =2;
    expected = "6";

    output = kthLargestElement(input, k);
    System.err.println(test+ " output="+output+", expected="+expected);


    test = "test8";
    input = new int[]{3, 2, 1, 5, 6, 4};
    k = 2;
    expected = "5";

    output = kthLargestElement(input, k);
    System.err.println(test + " output=" + output + ", expected=" + expected);


  }
}

package _codefights;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 2/13/2019
 * Time: 6:03 PM
 */
public class ArcadeGraphs19_contoursShifting_linearMethod {


  static public int[][] contoursShifting(int[][] matrix) {

    int rowCount = matrix.length;
    int columnCount = matrix[0].length;
    int leftBoundaryColumnIndex = 0;
    int rightBoundaryColumnIndex = columnCount - 1;
    int upperBoundaryRowIndex = 0;
    int lowerBoundaryRowIndex = rowCount - 1;

    if (rowCount == 0 && columnCount == 0) {
      return matrix;
    }
    if (rowCount == 1 && columnCount == 1) {
      return matrix;
    }

    if (rowCount == 1) {
      shiftSingleRowMatrix(matrix, 0, leftBoundaryColumnIndex, rightBoundaryColumnIndex, true, true);
      return matrix;
    } else if (columnCount == 1) {
      shiftSingleColumnMatrix(matrix, 0, upperBoundaryRowIndex, lowerBoundaryRowIndex, true, true);
      return matrix;
    }

    boolean shiftClockwise = true;
    int[] array = null;
    int[] shiftedArray = null;
    while (rightBoundaryColumnIndex > leftBoundaryColumnIndex &&
        lowerBoundaryRowIndex > upperBoundaryRowIndex) {

      shiftRing(matrix, array, shiftedArray, upperBoundaryRowIndex, lowerBoundaryRowIndex, leftBoundaryColumnIndex, rightBoundaryColumnIndex, shiftClockwise);
      rightBoundaryColumnIndex--;
      leftBoundaryColumnIndex++;
      lowerBoundaryRowIndex--;
      upperBoundaryRowIndex++;
      shiftClockwise = !shiftClockwise;
    }

    if (lowerBoundaryRowIndex < upperBoundaryRowIndex || leftBoundaryColumnIndex > rightBoundaryColumnIndex) {
      return matrix;
    }

    if (lowerBoundaryRowIndex == upperBoundaryRowIndex) {
      shiftSingleRowMatrix(matrix, lowerBoundaryRowIndex, leftBoundaryColumnIndex, rightBoundaryColumnIndex, false, shiftClockwise);
      return matrix;
    }
    if (leftBoundaryColumnIndex == rightBoundaryColumnIndex) {
      shiftSingleColumnMatrix(matrix, leftBoundaryColumnIndex, upperBoundaryRowIndex, lowerBoundaryRowIndex, false, shiftClockwise);
      return matrix;
    }
    return matrix;
  }

  static int computeRingLength(int upperBoundaryRowIndex, int lowerBoundaryRowIndex,
                          int leftBoundaryColumnIndex, int rightBoundaryColumnIndex) {
    int len = rightBoundaryColumnIndex - leftBoundaryColumnIndex + 1;
      len += lowerBoundaryRowIndex - upperBoundaryRowIndex;
      len += rightBoundaryColumnIndex - leftBoundaryColumnIndex;
      len += lowerBoundaryRowIndex - upperBoundaryRowIndex - 1;
      return len;
  }

  static void shiftRing(int[][] matrix,
                        int[] array,
                        int[] shiftedArray,
                        int upperBoundaryRowIndex, int lowerBoundaryRowIndex,
                        int leftBoundaryColumnIndex, int rightBoundaryColumnIndex,
                        boolean clockwise) {
    p("shiftRing:  upper=" + upperBoundaryRowIndex +
        "  lower=" + lowerBoundaryRowIndex +
        "  left=" + leftBoundaryColumnIndex +
        "  right=" + rightBoundaryColumnIndex +
        "  clockwise=" + clockwise);

    array = toArray(matrix, array, upperBoundaryRowIndex, lowerBoundaryRowIndex,
        leftBoundaryColumnIndex, rightBoundaryColumnIndex);

    int ringLength = computeRingLength(upperBoundaryRowIndex, lowerBoundaryRowIndex, leftBoundaryColumnIndex, rightBoundaryColumnIndex);
    shiftedArray = shiftArray(array, shiftedArray, ringLength, clockwise);

    arrayToMatrix(matrix, shiftedArray, upperBoundaryRowIndex, lowerBoundaryRowIndex,
        leftBoundaryColumnIndex, rightBoundaryColumnIndex);
  }

  static int[] toArray(int[][] matrix,  int[] array,
                       int upperBoundaryRowIndex, int lowerBoundaryRowIndex,
                       int leftBoundaryColumnIndex, int rightBoundaryColumnIndex) {

    // unpack the current ring into a linear array
    if (array == null) {
      int len = computeRingLength(upperBoundaryRowIndex, lowerBoundaryRowIndex, leftBoundaryColumnIndex, rightBoundaryColumnIndex);
      array = new int[len];
    }
    int count = 0;
    for (int i = leftBoundaryColumnIndex; i <= rightBoundaryColumnIndex; i++) {
      array[count++] = matrix[upperBoundaryRowIndex][i];
    }
    for (int i = upperBoundaryRowIndex + 1; i <= lowerBoundaryRowIndex; i++) {
      array[count++] = matrix[i][rightBoundaryColumnIndex];
    }
    for (int i = rightBoundaryColumnIndex - 1; i >= leftBoundaryColumnIndex; i--) {
      array[count++] = matrix[lowerBoundaryRowIndex][i];
    }
    for (int i = lowerBoundaryRowIndex - 1; i >= upperBoundaryRowIndex + 1; i--) {
      array[count++] = matrix[i][leftBoundaryColumnIndex];
    }
    p(" linear array: " + printIntArrayPlain(array));
    return array;
  }

  static int[] shiftArray(int[] array, int[] shiftedArray, int ringLength, boolean clockwise) {
    if (shiftedArray == null) {
      shiftedArray = new int[ringLength];
    }
    if (clockwise) {      // shift right
      shiftedArray[0] = array[array.length - 1];
      System.arraycopy(array, 0, shiftedArray, 1, ringLength-1);
    } else {              // shift left
      shiftedArray[array.length - 1] = array[0];
      System.arraycopy(array, 1, shiftedArray, 0, ringLength-1);
    }
    return shiftedArray;
  }

  static void arrayToMatrix(int[][] matrix, int[] array, int upperBoundaryRowIndex, int lowerBoundaryRowIndex,
                            int leftBoundaryColumnIndex, int rightBoundaryColumnIndex) {
    int count = 0;
    for (int i = leftBoundaryColumnIndex; i <= rightBoundaryColumnIndex; i++) {
      matrix[upperBoundaryRowIndex][i] = array[count++];
    }
    for (int i = upperBoundaryRowIndex + 1; i <= lowerBoundaryRowIndex; i++) {
      matrix[i][rightBoundaryColumnIndex] = array[count++];
    }
    for (int i = rightBoundaryColumnIndex - 1; i >= leftBoundaryColumnIndex; i--) {
      matrix[lowerBoundaryRowIndex][i] = array[count++];
    }
    for (int i = lowerBoundaryRowIndex - 1; i >= upperBoundaryRowIndex + 1; i--) {
      matrix[i][leftBoundaryColumnIndex] = array[count++];
    }
  }
  
  static void shiftSingleColumnMatrix(int[][] matrix, int columnIndex, int upperBoundaryRowIndex, int lowerBoundaryRowIndex,
                                      boolean entireMatrix, boolean clockwise) {
    // top to bottom         SINGLE COLUMN
    if (lowerBoundaryRowIndex - upperBoundaryRowIndex == 1) {
      // special case only 2 elements swap and done
      int tempVal = matrix[lowerBoundaryRowIndex][columnIndex];
      matrix[lowerBoundaryRowIndex][columnIndex] = matrix[upperBoundaryRowIndex][columnIndex];
      matrix[upperBoundaryRowIndex][columnIndex] = tempVal;
      return;
    }
    if (entireMatrix || clockwise) {
      //  shift down from top to bottom
      int newTop = matrix[lowerBoundaryRowIndex][columnIndex];
      for (int i = lowerBoundaryRowIndex; i > upperBoundaryRowIndex; i--) {
        matrix[i][columnIndex] = matrix[i - 1][columnIndex];
      }
      matrix[upperBoundaryRowIndex][columnIndex] = newTop;
    }
    else {
      // shift up from bottom to top
      int newBottom = matrix[upperBoundaryRowIndex][columnIndex];
      for (int i = upperBoundaryRowIndex; i < lowerBoundaryRowIndex; i++) {
        matrix[i][columnIndex] = matrix[i + 1][columnIndex];
      }
      matrix[lowerBoundaryRowIndex][columnIndex] = newBottom;
    }
  }

  static void shiftSingleRowMatrix(int[][] matrix, int rowIndex, int leftBoundaryColumnIndex, int rightBoundaryColumnIndex,
                                   boolean entireMatrix, boolean clockwise) {
    // left to right           SINGLE ROW
    if (rightBoundaryColumnIndex - leftBoundaryColumnIndex == 1) {
      int tempVal = matrix[rowIndex][leftBoundaryColumnIndex];
      matrix[rowIndex][leftBoundaryColumnIndex] = matrix[rowIndex][rightBoundaryColumnIndex];
      matrix[rowIndex][rightBoundaryColumnIndex] = tempVal;
      return;
    }
    if (entireMatrix || clockwise) {
      int newLeft = matrix[rowIndex][rightBoundaryColumnIndex];
      for (int i = rightBoundaryColumnIndex; i > leftBoundaryColumnIndex; i--) {
        matrix[rowIndex][i] = matrix[rowIndex][i - 1];
      }
      matrix[rowIndex][leftBoundaryColumnIndex] = newLeft;
    }
    else {
      // shift right to left
      int newRight = matrix[rowIndex][leftBoundaryColumnIndex];
      for (int i = leftBoundaryColumnIndex; i < rightBoundaryColumnIndex; i++) {
        matrix[rowIndex][i] = matrix[rowIndex][i + 1];
      }
      matrix[rowIndex][rightBoundaryColumnIndex] = newRight;
    }
  }


  public static String printIntArray2D(int[][] a) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < a.length; i++) {
      sb.append("[");
      int[] b = a[i];
      for (int j = 0; j < b.length; j++) {
        sb.append("").append(b[j]).append("");
        if (j >= (b.length - 1)) {
          sb.append("]");
        } else {
          sb.append(", ");
        }
      }
      sb.append("]");
      if (i < a.length - 1) {
        sb.append(",");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  public static String printIntArray2Drow(int[][] a, int row) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    int[] b = a[row];
    for (int j = 0; j < b.length; j++) {
      sb.append("").append(b[j]).append("");
      if (j >= (b.length - 1)) {
        sb.append("");
      } else {
        sb.append(", ");
      }
    }
    sb.append("]");
    sb.append("\n");
    return sb.toString();
  }

  public static String printIntArray2Dcolumn(int[][] a, int column) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < a.length; i++) {
      sb.append("[");
      int[] b = a[i];
      sb.append("").append(b[column]).append("");
      sb.append("]");
      if (i < a.length - 1) {
        sb.append(",");
      }
      sb.append("]\n");
    }
    return sb.toString();
  }

  public static String printIntArray2DInnerSubMatrix(int[][] a, int leftBound, int rightBound,
                                                     int upperBound, int lowerBound) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = upperBound; i <= lowerBound; i++) {
      sb.append("[");
      int[] b = a[i];
      for (int j = leftBound; j <= rightBound; j++) {
        sb.append("").append(b[j]).append("");
        if (j >= (lowerBound - 1)) {
          sb.append("]");
        } else {
          sb.append(", ");
        }
      }
      sb.append("]");
      if (i < rightBound - 1) {
        sb.append(",");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  public static String printIntArrayPlain(int[] a) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < a.length; i++) {
      sb.append(a[i]);
      if (i < (a.length - 1)) {
        sb.append(", ");
      } else {
        sb.append("]");
      }
    }
    return sb.toString();
  }

  // true = equal
  // false = not equal
  static boolean compare(int[][] a, int[][] b) {
    int rowsA = a.length;
    int rowsB = b.length;
    if (rowsA != rowsB) return false;

    int colsA = a[0].length;
    int colsB = b[0].length;
    if (colsA != colsB) return false;

    for (int i = 0; i < rowsA; i++) {
      for (int j = 0; j < colsA; j++) {
        if (a[i][j] != b[i][j]) return false;
      }
    }
    return true;
  }


  public static void p(String s) {
    System.err.println(s);
  }

  public static void main(String[] args) {
    String test = "t1";
    int[][] matrix = null;
    int n = 0;
    int[][] result = null;
    int[][] expected = null;
    boolean pass = true;


    test = "test1";
    matrix = new int[][]{
        {1, 2, 3, 4},
        {5, 6, 7, 8},
        {9, 10, 11, 12},
        {13, 14, 15, 16},
        {17, 18, 19, 20}
    };
    expected = new int[][]{
        {5, 1, 2, 3},
        {9, 7, 11, 4},
        {13, 6, 15, 8},
        {17, 10, 14, 12},
        {18, 19, 20, 16}
    };
    System.err.println(test);
    System.err.println("input:\n" + printIntArray2D(matrix));
    result = contoursShifting(matrix);
    pass = compare(result, expected);
    System.err.println("passed=" + pass);
    System.err.println("result:\n" + printIntArray2D(result));
    System.err.println("expected:\n" + printIntArray2D(expected) + "\n\n");


    test = "test2";
    matrix = new int[][]{
        {238, 239, 240, 241, 242, 243, 244, 245}
    };
    expected = new int[][]{
        {245, 238, 239, 240, 241, 242, 243, 244}
    };
    System.err.println(test);
    System.err.println("input:\n" + printIntArray2D(matrix));
    result = contoursShifting(matrix);
    pass = compare(result, expected);
    System.err.println("passed=" + pass);
    System.err.println("result:\n" + printIntArray2D(result));
    System.err.println("expected:\n" + printIntArray2D(expected) + "\n\n");


    test = "test3";
    matrix = new int[][]{
        {238},
        {239},
        {240},
        {241},
        {242},
        {243},
        {244},
        {245}
    };
    expected = new int[][]{
        {245},
        {238},
        {239},
        {240},
        {241},
        {242},
        {243},
        {244}

    };
    System.err.println(test);
    System.err.println("input:\n" + printIntArray2D(matrix));
    result = contoursShifting(matrix);
    pass = compare(result, expected);
    System.err.println("passed=" + pass);
    System.err.println("result:\n" + printIntArray2D(result));
    System.err.println("expected:\n" + printIntArray2D(expected) + "\n\n");


    test = "test4";
    matrix = new int[][]{
        {1, 2, 3},
        {4, 5, 6},
        {7, 8, 9},
        {10, 11, 12}
    };
    expected = new int[][]{
        {4, 1, 2},
        {7, 8, 3},
        {10, 5, 6},
        {11, 12, 9}
    };
    System.err.println(test);
    System.err.println("input:\n" + printIntArray2D(matrix));
    result = contoursShifting(matrix);
    pass = compare(result, expected);
    System.err.println("passed=" + pass);
    System.err.println("result:\n" + printIntArray2D(result));
    System.err.println("expected:\n" + printIntArray2D(expected) + "\n\n");


    test = "test5";
    matrix = new int[][]{
        {1, 2, 3, 4, 5},
        {6, 7, 8, 9, 10},
        {11, 12, 13, 14, 15},
        {16, 17, 18, 19, 20}
    };
    expected = new int[][]{
        {6, 1, 2, 3, 4},
        {11, 8, 9, 14, 5},
        {16, 7, 12, 13, 10},
        {17, 18, 19, 20, 15}
    };
    System.err.println(test);
    System.err.println("input:\n" + printIntArray2D(matrix));
    result = contoursShifting(matrix);
    pass = compare(result, expected);
    System.err.println("passed=" + pass);
    System.err.println("result:\n" + printIntArray2D(result));
    System.err.println("expected:\n" + printIntArray2D(expected) + "\n\n");

    test = "test6";
    matrix = new int[][]{
        {1, 2, 3, 4, 5},
        {6, 7, 8, 9, 10},
        {11, 12, 13, 14, 15}
    };
    expected = new int[][]{
        {6, 1, 2, 3, 4},
        {11, 8, 9, 7, 5},
        {12, 13, 14, 15, 10}
    };
    System.err.println(test);
    System.err.println("input:\n" + printIntArray2D(matrix));
    result = contoursShifting(matrix);
    pass = compare(result, expected);
    System.err.println("passed=" + pass);
    System.err.println("result:\n" + printIntArray2D(result));
    System.err.println("expected:\n" + printIntArray2D(expected) + "\n\n");


    test = "test7";
    matrix = new int[][]{
        {1, 2, 3},
        {6, 7, 8},
        {11, 12, 13},
        {16, 17, 18},
        {21, 22, 23},
        {24, 25, 26}
    };
    expected = new int[][]{
        {6, 1, 2},
        {11, 12, 3},
        {16, 17, 8},
        {21, 22, 13},
        {24, 7, 18},
        {25, 26, 23}
    };
    System.err.println(test);
    System.err.println("input:\n" + printIntArray2D(matrix));
    result = contoursShifting(matrix);
    pass = compare(result, expected);
    System.err.println("passed=" + pass);
    System.err.println("result:\n" + printIntArray2D(result));
    System.err.println("expected:\n" + printIntArray2D(expected) + "\n\n");


    test = "test8";
    matrix = new int[][]{
        {239}
    };
    expected = new int[][]{
        {239}
    };
    System.err.println(test);
    System.err.println("input:\n" + printIntArray2D(matrix));
    result = contoursShifting(matrix);
    pass = compare(result, expected);
    System.err.println("passed=" + pass);
    System.err.println("result:\n" + printIntArray2D(result));
    System.err.println("expected:\n" + printIntArray2D(expected) + "\n\n");
  }
}

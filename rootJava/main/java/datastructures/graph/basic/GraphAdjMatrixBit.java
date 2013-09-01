package datastructures.graph.basic;

import bits.BitUtils;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/30/13
 * Time: 1:04 PM
 * <p/>
 * Basic Adjacency Matrix Graph using bits packed into java ints
 * <p/>
 * So it's edge existence only here with no other edge data available
 * <p/>
 * Handles vertices starting at v=0.
 * <p/>
 * In this impl, bidirectional edges must be individually inserted
 * if inserting 2-3  you must insert  3-2
 * <p/>
 * digraph flag ignored
 */
public class GraphAdjMatrixBit implements Graph {
  private Logger log =
          Logger.getLogger(GraphAdjMatrixBit.class);

  protected int bitPack = 32;

  protected int highestVertexNumber = -1;

  protected int sizeLimit;    // max vertex count
  protected int vCount;
  protected int eCount;
  protected boolean isDigraph = false;

  // how many ints per row.  we're packing bits so it's less than the vertex size
  protected final int axisIntCount;
  protected int[] graphRows;
  protected int[] graphColumns;


  public GraphAdjMatrixBit(int sizeLimit, boolean isDigraph) {
    this.sizeLimit = sizeLimit;
    this.isDigraph = isDigraph;
    this.axisIntCount = sizeLimit / bitPack;
    this.graphRows = new int[axisIntCount * sizeLimit];
    this.graphColumns = new int[axisIntCount * sizeLimit];
  }


  public void insert(int v, int w) {
    insert(new Edge(v, w));
  }

  public int insert(Edge e) {
    // find the row element
    int v = e.v;
    int w = e.w;

    if (highestVertexNumber < v) highestVertexNumber = v;
    if (highestVertexNumber < w) highestVertexNumber = w;

    int rowIntIndex = getStartingAxisArrayIndexForVertex(v);
    rowIntIndex = rowIntIndex + getAxisArrayIndexOffSetForHead(w);

    int rowE = graphRows[rowIntIndex];
    int rowI = getVertexBitPosition(w);

    int mask = 1 << (31 - rowI);    // pos '0' is '1' shifted left 31 times
    rowE = rowE | mask;
    graphRows[rowIntIndex] = rowE;
    //log.debug("ROW   after set of v=" + v + ", w=" + w + ", rowIntIndex=" + rowIntIndex + ", rowIndex=" + rowI + ", row(" + v + ")='" + printRow(v));

    //log.debug("   now take care of the COLUMN array");
    int colIntIndex = getStartingAxisArrayIndexForVertex(w);
    colIntIndex = colIntIndex + getAxisArrayIndexOffSetForHead(v);

    int colE = graphColumns[colIntIndex];
    int colI = getVertexBitPosition(v);
    mask = 1 << (31 - colI);    // pos '0' is '1' shifted left 31 times

    colE = colE | mask;
    graphColumns[colIntIndex] = colE;

    //log.debug("COL   after set of v=" + v + ", w=" + w + ", colIntIndex=" + colIntIndex + ", colIndex=" + colI + ", column(" + w + ")='" + printCol(w) + "\n\n");
    return 0;   // meaningless
  }

  public boolean edge(int v, int w) {
    // check it on rows
    int rowIntIndex = getStartingAxisArrayIndexForVertex(v);
    rowIntIndex = rowIntIndex + getAxisArrayIndexOffSetForHead(w);

    log.debug(" --- edge(" + v + "," + w + ").  rowIntIndex=" + rowIntIndex);

    int rowE = graphRows[rowIntIndex];
    int rowI = getVertexBitPosition(w);
    int mask = 1 << (31 - rowI);

    log.debug(" ---  edge(" + v + "," + w + ").  rowE='" + BitUtils.printBits(rowE) + ",  mask(" + w + ")=" + BitUtils.printBits(mask));
    log.debug(" ---   rowE & mask ='" + BitUtils.printBits((rowE & mask)));

    if ((rowE & mask) != 0) {
      return true;
    }
    return false;
  }

  @Override
  public int vCount() {
    throw new RuntimeException("NYI");

  }

  @Override
  public int eCount() {
    throw new RuntimeException("NYI");

  }

  public int getHighestVertexNumber() {
    return highestVertexNumber;
  }

  @Override
  public boolean isDigraph() {
    return isDigraph;
  }


  @Override
  public void remove(Edge e) {
    throw new RuntimeException("NYI");
  }

  @Override
  public List<Edge> getEdges(int v, int w) {
    throw new RuntimeException("NYI");
  }

  @Override
  public LinkedNode getAdjList(int node) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void loadEdges(int[] in) {
    throw new RuntimeException("NYI");
  }

  @Override
  public Graph reverse() {
    throw new RuntimeException("NYI");
  }

  @Override
  public Graph copy() {
    GraphAdjMatrixBit copy = new GraphAdjMatrixBit(sizeLimit, isDigraph);
    copy.highestVertexNumber = highestVertexNumber;
    copy.vCount = vCount;
    copy.eCount = eCount;
    System.arraycopy(graphRows, 0, copy.graphRows, 0, (axisIntCount * sizeLimit));
    System.arraycopy(graphColumns, 0, copy.graphColumns, 0, (axisIntCount * sizeLimit));

    return copy;  //To change body of implemented methods use File | Settings | File Templates.
  }


  /**
   * Find the starting row or column index for a vertex v
   * Thus for v == 0,  index == 0
   * <p/>
   * for v == 1,  index == axisIntCount * v
   */
  private int getStartingAxisArrayIndexForVertex(int vertexTail) {
    return vertexTail * axisIntCount;
  }

  /**
   * Determine the array offset to find the array element that contains the
   * head vertex.
   *
   * @param vertexHead
   * @return
   */
  private int getAxisArrayIndexOffSetForHead(int vertexHead) {
    return vertexHead / bitPack;
  }

  /**
   * for a given vertex, find the index inside of the axis int[] element for this index
   * <p/>
   * so if vertex is '3'
   * then within int[0] it's index is '3'
   *
   * @param i
   * @return
   */
  private int getVertexBitPosition(int i) {
    if (i == 0) return 0;
    return i % bitPack;

  }

  public String printRow(int v) {
    StringBuilder sb = new StringBuilder();
    int rowIndex = getStartingAxisArrayIndexForVertex(v);
    log.debug(" ==  printRow " + v + " start rowIndex=" + rowIndex);

    for (int i = 0; i < axisIntCount; i++) {
      log.debug("row: " + v + ", append rowIndex=" + rowIndex + ", '" + BitUtils.printBits(graphRows[rowIndex]));
      sb.append(BitUtils.printBits(graphRows[rowIndex]));
      rowIndex++;
    }
    return sb.toString();
  }


  public String printCol(int w) {
    StringBuilder sb = new StringBuilder();
    int colIndex = getStartingAxisArrayIndexForVertex(w);
    log.debug(" ==  printCol " + w + " start colIndex=" + colIndex);

    for (int i = 0; i < axisIntCount; i++) {
      log.debug("col: " + w + ", append colIndex=" + colIndex + ", '" + BitUtils.printBits(graphColumns[colIndex]));
      sb.append(BitUtils.printBits(graphColumns[colIndex]));
      colIndex++;
    }
    return sb.toString();
  }

  /**
   * Serves as access and operations on a complete vertex Row or Column
   */
  public class AxisElements {
    private final boolean isRow;
    private final int vertex;
    private int currElement;
    private int currIndex;       // current vertex index into currElement: first vertex is 0.
    private int currArrayIndex;  // index of int in the element array
    private int startArrayIndex;
    private int endArrayIndex;   //  convenience max array index, so that we know when we're at the end
    private int mask;
    private boolean doneFirstIteration = false;


    public AxisElements(boolean isRow, int vertex) {
      this.isRow = isRow;
      this.vertex = vertex;
      startArrayIndex = getStartingAxisArrayIndexForVertex(vertex);
      endArrayIndex = currArrayIndex + axisIntCount - 1;
      currElement = isRow ? graphRows[startArrayIndex] : graphColumns[startArrayIndex];
      currArrayIndex = startArrayIndex;
      currIndex = -1;
      mask = 1 << 31;
    }

    public void resetIterator() {
      currArrayIndex = startArrayIndex;
      currIndex = -1;
      currElement = isRow ? graphRows[currArrayIndex] : graphColumns[currArrayIndex];
      mask = 1 << 31;
      doneFirstIteration = false;
    }

    public boolean hasNext() {
      return currIndex < (sizeLimit - 1);   // if sizeLimit is 64, if currIndex is 63 then we're done
    }


    public Object next() {
      throw new RuntimeException("N/A.    Not Implemented !  use 'nextValue()' instead.");
    }

    public boolean nextValue() {
      // since this returns boolean, we have to throw an exception
      if (!hasNext()) throw new RuntimeException("called nextBoolean after reaching the end of axis !");

      currIndex++;
      if (mask == 1) {
        currArrayIndex++;
        currElement = isRow ? graphRows[currArrayIndex] : graphColumns[currArrayIndex];
        mask = 1 << 31;
        //doneFirstIteration = false;
      } else {
        if (doneFirstIteration) {
          mask = mask >>> 1;
        } else {
          doneFirstIteration = true;
        }
      }
      //log.debug(" ------ nextValue:  vertex=" + vertex + ", mask=" + BitUtils.printBits(mask) + ", currArrayIndex=" + currArrayIndex + ", currElement='" +
      //        BitUtils.printBits(currElement));

      //log.debug(" ------  currElement & mask = '" + BitUtils.printBits((currElement & mask)));
      return !((currElement & mask) == 0);
    }

    /**
     * unset the current edge if it is set
     */
    public void remove() {
      throw new RuntimeException("remove  NYI");
    }

    public int currIndex() {
      return currIndex;
    }

    public boolean currValue() {
      return !((currElement & mask) == 0);
    }

    public boolean orCurrent(AxisElements otherAxis) {
      return currValue() | otherAxis.currValue();
    }

    public boolean andCurrent(AxisElements otherAxis) {
      return currValue() & otherAxis.currValue();
    }

  }


}

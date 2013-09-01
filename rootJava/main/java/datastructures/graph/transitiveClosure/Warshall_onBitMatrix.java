package datastructures.graph.transitiveClosure;

import datastructures.graph.basic.GraphAdjMatrixBit;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/1/13
 * Time: 7:50 AM
 *
 * Full on O(v**3)  Warshall Transitive Closure
 *  in this case done on an Adjacency Matrix Array of bits mapped to java ints
 *
 *
 */
public class Warshall_onBitMatrix {

  private Logger log =
          Logger.getLogger(Warshall_onBitMatrix.class);

  GraphAdjMatrixBit graph;

  public Warshall_onBitMatrix(GraphAdjMatrixBit graph) {
   this.graph = (GraphAdjMatrixBit)graph.copy();
  }

  /**
   * compute the transitive closure
   * using Warshall's algorithm
   *
   */
  public void transitiveClosure() {
    int lastVertex = graph.getHighestVertexNumber();

    // process along the diagonal
    for (int i=0; i<=lastVertex; i++) {

      log.debug("-- process diagonal="+i);
      // march along the row axis.  if we find a vertex, check the entire column axis and mark any hits
      GraphAdjMatrixBit.AxisElements rowAxis = graph. new AxisElements(true, i);
      while (rowAxis.hasNext()) {
        if (rowAxis.nextValue() == true)  {


          int rowIndex = rowAxis.currIndex();
          log.debug("row("+rowIndex+") has a hit");

          GraphAdjMatrixBit.AxisElements colAxis = graph. new AxisElements(false, i);
          while (colAxis.hasNext()) {
            if (colAxis.nextValue() == true) {
              int colIndex = colAxis.currIndex();
              log.debug("col("+colIndex+") has a hit, insert edge("+colIndex+","+rowIndex+")");
              graph.insert(colIndex, rowIndex);
            }
          }
        }
      }
      log.debug("--  done with diagonal="+i+"\n\n\n");
    }
  }

  /**
   * are v and w connected ?
   *
   * check the computed transitive closure graph
   *
   * @param v
   * @param w
   * @return
   */
  public boolean connected(int v, int w) {
    return graph.edge(v, w);
  }

  /**
   * return all the vertices reachable from v
   *
   * @param v
   * @return
   */
  public List<Integer> getReachableFrom(int v) {
    GraphAdjMatrixBit.AxisElements ae = graph. new AxisElements(true, v);
    List<Integer> list = new ArrayList<Integer>();
    while (ae.hasNext()) {
      ae.next();
      if (ae.currValue()) {
        list.add(new Integer(ae.currIndex()));
      }
    }
    return list;
  }

  public String printRow(int v) {
    return graph.printRow(v);
  }
}

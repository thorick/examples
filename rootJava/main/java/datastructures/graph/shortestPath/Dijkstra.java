package datastructures.graph.shortestPath;

import utils.StackIntArray;

import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 8/4/13
 * Time: 4:25 PM
 */
interface Dijkstra {

  void compute(int rootVertex);

  int dist(int v);

  public int[] getDistArray();

}

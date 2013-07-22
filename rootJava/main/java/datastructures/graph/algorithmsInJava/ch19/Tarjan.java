package datastructures.graph.algorithmsInJava.ch19;

import datastructures.graph.algorithmsInJava.AdjList;
import datastructures.graph.algorithmsInJava.Edge;
import datastructures.graph.algorithmsInJava.Graph;
import datastructures.graph.algorithmsInJava.GraphAdjList;

import java.util.Iterator;
import java.util.Stack;

/**
 * Created with IntelliJ IDEA.
 * User: thorick chow
 * Date: 6/11/13
 * Time: 7:00 PM
 *
 * Tarjan's Algorthm for finding Strong Components in a Digraph.
 * The implementation by  Sedgewick   Algorithms in Java  part 5   program 19.11
 *   http://ebookstipsl0g.eklablog.com/robert-sedgewick-algorithms-in-java-part-5-graph-algorithms-a91569941
 *
 * modified only slightly and with extra verbosity to print the processing steps.
 *
 * The example run by main(..)  corresponds exactly to the test case presented in the text
 * as figure 19.29 so that the output of this program can be compared directly to the text example.
 *
 *
 * Compared to some other Tarjan impls on the net, this version is pretty space efficient using primitive
 * int arrays rather than Object collections to hold the component ids, pre-order numbers and low pre-order processing storage.
 *
 * Using the per-node temporary 'min' value as a stack value rather than holding it in
 * a global datastructure is also a nice touch.
 *
 *
 *
 */
public class Tarjan {
  private static boolean VERBOSE=true;
  private Graph g;
  private int cnt;
  private int scnt;
  private int[] id;
  private int[] pre;
  private int[] low;
  private Stack<Integer> s;

  Tarjan(Graph g) {
    this.g = g;
    s = new Stack<Integer>();
    id = new int[g.vCount()];
    pre = new int[g.vCount()];
    low = new int[g.vCount()];

    for (int t = 0; t < g.vCount(); t++) {
      id[t] = -1;
      pre[t] = -1;
      low[t] = -1;
    }

    // start at first vertex
    for (int v = 0; v < g.vCount(); v++) {
      if (pre[v] == -1) scR(v);
    }
  }

  //
  //  Depth First Search
  //
  private void scR(int w) {
    int t;

    // min  holds the lowest pre-order value that this vertex 'w'
    //      has seen 'so far' as it finishes process each
    //      of it's children.
    //
    // low  is set for the vertex 'w' post-order and is the lowest
    //      pre-order value of any of the vertices below it in the hierarchy
    //
    // pre  the pre-order number for this vertex in this DFS.
    //
    int min = cnt++;
    low[w] = min;
    pre[w] = min;

    p("scr "+w+",  min="+min+", low["+w+"]="+min+", pre["+w+"]="+min);

    s.push(new Integer(w));  // vertex stack
    p("stack="+printStack()+"\n");

    AdjList a = g.getAdjList(w);
    for (t = a.head(); !a.isEnd();
         t = a.next()) {
      p("vertex "+w+" next child="+t);


      if (pre[t] == -1)
        scR(t);

      // set min:  keep the *lowest* pre-order number that we see 'so far'
      // for each of our children
      //
      p("vertex "+w+" done with child="+t+", low["+t+"]="+low[t]+", min="+min);
      if (low[t] < min) {
        p("vertex "+w+" low["+t+"]="+low[t]+"  <  min="+min+"  so set min="+low[t]);
        min = low[t];
      }
    }

    p("done with all children of vertex "+w+"\n");

    // all done with the children
    // now we have 2 choices depending on what we've found:
    //
    // if    the min pre-order of our children is less than our
    //  pre-order then we have not yet reached the 'boundary' of the
    //  cycle that we are in, so we mark our new low pre-order to be
    //  that of the children beneath us
    // else  we ARE the pre-order lower bound of this cycle
    //  so pop all stack contents including us and mark this strong component.
    //
    if (min < low[w]) {
      p("vertex "+w+" min="+min+"  <  low["+w+"]="+low[w]+"+ set low["+w+"]="+min+" and return from scr\n");
      low[w] = min;
      return;
    }

    // mark all connected
    // vertices in this set
    p("vertex "+w+" min="+min+"  NOT less than  low["+w+"]="+low[w]);
    p("        so mark all connected vertices in this connected set.  pop stack through to vertex w="+w);
    p(" stack="+printStack());
    do {
      id[t = s.pop().intValue()] = scnt;

      p("vertex="+t+" is in component "+scnt);

      // reset low[t] to max   to exclude vertex from any further consideration
      low[t] = g.vCount();
    }
    while (t != w);
    p("done with assignment stack is now="+printStack()+"\n");

    scnt++;
  }

  public int count() {
    return scnt;
  }

  public boolean stronglyreachable(int v, int w) {
    return id[v] == id[w];
  }



  /////////////////////
  //   methods below are not in the Sedgewick impl
  //   so they are separated in this section
  //

  public String printComponents() {


    if (scnt <= 0)  return "Graph has no strong components.";

    StringBuilder sb = new StringBuilder();
    // dumb loop to repeatedly scan the vertices by component #
    int vCount = g.vCount();
    for (int i=0 ; i < scnt ; i++) {
      sb.append("\nComponent "+i+"\n");
      for (int j=0 ; j < vCount; j++) {
        if (id[j] == i) {
          sb.append("  "+j);
        }
      }
      sb.append("\n");
    }
    return sb.toString();
  }


  private String printStack() {
    StringBuilder sb = new StringBuilder();
    Iterator<Integer> it = s.iterator();
    while (it.hasNext()) {
      Integer i = it.next();
      sb.append(i).append(" ");
    }
    return sb.toString();
  }


  /**
   * @param args
   */
  public static void main(String[] args) {
    Graph g = setupSedgewickFigure19_29();

    Tarjan tarjan = new Tarjan(g);

    p("\nresults: ");

    p("number of strong components = "+tarjan.count());
    p(" ");
    p("strong components and members");
    p(tarjan.printComponents());
    System.exit(0);
  }

  /**
   * sets up a Graph that will respond to Depth First Search
   * exactly as the Digraph in Sedgewick figure 19.29
   *
   *
   * @return
   */
  public static Graph setupSedgewickFigure19_29() {
    Graph g = new GraphAdjList(13,    // 13 vertices
                               true);  // isDigraph == true

    insertEdge(g, 0, 6);
    insertEdge(g, 6, 4);
    insertEdge(g, 6, 9);
    insertEdge(g, 0, 1);
    insertEdge(g, 0, 5);
    insertEdge(g, 5, 4);
    insertEdge(g, 4, 2);
    insertEdge(g, 4, 11);
    insertEdge(g, 11, 12);
    insertEdge(g, 12, 9);
    insertEdge(g, 9, 10);
    insertEdge(g, 9, 11);
    insertEdge(g, 10, 12);
    insertEdge(g, 4, 3);
    insertEdge(g, 3, 2);
    insertEdge(g, 2, 3);
    insertEdge(g, 2, 0);
    insertEdge(g, 3, 5);
    insertEdge(g, 7, 8);
    insertEdge(g, 8, 9);
    insertEdge(g, 8, 7);
    insertEdge(g, 7, 6);
    return g;
  }

  /**
   * We would have added this convenience method to the Graph interface
   * but I wanted to keep the interfaces as they were laid out in
   * Sedgewick.
   *
   *
   * @param g
   * @param v
   * @param w
   */
  public static void insertEdge(Graph g, int v, int w) {
    Edge e = new Edge(v, w);
    g.insert(e);
  }


  private static void p(String s) {
    if (VERBOSE) {
      System.out.println("tarjan: "+s);
    }
  }
}

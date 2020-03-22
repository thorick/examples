package datastructures.graph.allPairsShortestPaths;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/29/13
 * Time: 4:51 PM
 * <p/>
 * <p/>
 * Runs Bellman Ford
 * <p/>
 * This is *strict* Bellman Ford that counts number of DP iterations
 * and begins each round by starting at the source vertex 's'.
 * <p/>
 * <p/>
 * Will detect negative cycles by running past the maximum number of required vertex processing
 * (unless it stops early by detecting no changes before then)
 * <p/>
 * This implementation is biased towards gaining high performance obtained by increased use of memory.
 *
 * The edges to be processed are queued up in a pair of fixed size arrays each large enough to hold every edge in the graph
 * What you get in return for the memory use is extremely fast queueing and dequeuing of edges to be processed.
 * In a test run with 1 million edges and 20000 nodes the total run time was under a minute on my laptop.
 *
 * If one used more memory efficient linked lists to handle the edge queues that runtime would be more than 1 hour
 * because of all the time spend handling object pointers whereas the array based queues use direct pointers to array
 * elements which probably are implemented in blocks of contiguous memory by the JVM when it can do so.
 *
 *
 * Sample:   simpleGraph:
 * Input:
 * 5 6
 * 1 2 4
 * 1 3 2
 * 3 2 1
 * 2 5 4
 * 3 4 2
 * 4 5 2
 *
 * result:
 *  graph has Negative Cycle = false
 *
 * All Minimum Paths:
 * 1 to 2 (3): [1-3](2) -> [3-2](1)
 * 1 to 3 (2): [1-3](2)
 * 1 to 4 (4): [1-3](2) -> [3-4](2)
 * 1 to 5 (6): [1-3](2) -> [3-4](2) -> [4-5](2)
 *
 */
public class BellmanFord_SingleSourceAllShortestPaths {
    private boolean debug = false;                     // do not depend on setting loggers
    private boolean computeHasNegativeCycles = true;
    private boolean hasNegativeCycle = false;
    private final int NO_VALUE = Integer.MAX_VALUE;
    private int graphVertexSize;
    private GraphAdjList g;
    private int[] currDistTo;
    private int[] prevDistTo;
    private Edge[] edgeTo;       // latest min edge incoming on vertex

    private Set<Edge> seenEdge = new HashSet<>();
    private Set<Edge> addedEdge = new HashSet<>();
    private Edge[] eA;
    private Edge[] eB;
    private Edge[] eArray;
    private Edge[] nextEArray;
    private int nextEArrayIndex;
    private int nextNextEArrayIndex;
    private boolean eArrayIsA;

    private int maxEdgeLimit;    // stop normal BF after this many edges (V - 1)
    private int sourceVertex = -1;  // keep track of who we ran this on.
    private boolean ranOnce;     // we ran complete BF at least once
    private boolean foundLowerWeight;

    public BellmanFord_SingleSourceAllShortestPaths() { }

    /**
     * @param s starting vertex
     * @return 0  compute OK
     * -1  negative cycle detected
     */
    public int compute(int s) {
        sourceVertex = s;

        // vertices start at 1 (and not 0) in the problems
        graphVertexSize = g.getHighestVertexNumber() + 1;

        // we only need this many min path edges to cover the entire graph
        maxEdgeLimit = g.vCount() - 1;
        edgeTo = new Edge[graphVertexSize];
        currDistTo = new int[graphVertexSize];
        prevDistTo = new int[graphVertexSize];
        for (int v = 0; v <= g.getHighestVertexNumber(); v++) {
            currDistTo[v] = NO_VALUE;
            prevDistTo[v] = NO_VALUE;
        }
        int edgeArraySize = g.eCount() + 1;    // worst case:  we queue up EVERY edge in a single cycle
        eA = new Edge[edgeArraySize];
        eB = new Edge[edgeArraySize];
        eArray = eA;
        nextEArray = eB;
        nextEArrayIndex = 1;
        nextNextEArrayIndex = 1;
        eArrayIsA = true;
        foundLowerWeight = false;
        p("begin BellMan Ford for source vertex=" + sourceVertex +
                ", number of vertices=" + g.vCount() +
                ", do for n-1 iterations n-1=" + maxEdgeLimit);
        runCompleteBF(sourceVertex, maxEdgeLimit);
        return 0;
    }

    private void runCompleteBF(int sourceVertex, int maxEdgeLimit) {
        // do BellMan Ford iterations for path length limit
        bf(sourceVertex, maxEdgeLimit);
        ranOnce = true;
    }

    //
    //  This checks path lengths one newly considered edge at a time
    //  In breadth first order
    //  Thus for each level of node depth that we reach, we queue up the edges that lead to the next node depth
    //
    private void bf(int sourceVertex, int maxEdgeLimit) {
        int iterationLimit = maxEdgeLimit;
        if (computeHasNegativeCycles) {
            iterationLimit += 5;    // check for negative cycle is to try to add more than the number of edges if you can
                                     // and see if a duplicate edge results in a lower value, a sure indication
        }

        int iterationCount = -1;
        while (iterationCount <= iterationLimit) {
            foundLowerWeight = false;
            seenEdge.clear();
            // bootstrap special case: source vertex   currPathLen == 0
            //  there is always a next pathLen == 1  so set it up now.
            if (iterationCount == -1) {
                iterationCount++;
                currDistTo[sourceVertex] = 0;
                prevDistTo[sourceVertex] = NO_VALUE;

                // queue up all the edges for the next path length == 1
                // from this single source vertex
                LinkedNode n = g.getAdjList(sourceVertex);
                eArray = eA;
                nextEArray = eB;
                nextEArrayIndex = 1;
                nextNextEArrayIndex = 1;
                eArrayIsA = true;
                while (n != null) {
                    Edge e = n.edge();
                    nextEArray[nextNextEArrayIndex++] = e;
                    n = n.next();
                }
            } else {
                p("\n  at pathLen=" + iterationCount + " queued up " + (nextNextEArrayIndex-1) + " edges to check");
                // swap edge processing arrays
                if (eArrayIsA) {
                    eArrayIsA = false;
                    eArray = eB;
                    nextEArray = eA;
                    nextEArrayIndex = nextNextEArrayIndex;
                    nextNextEArrayIndex = 1;
                }
                else {
                    eArrayIsA = true;
                    eArray = eA;
                    nextEArray = eB;
                    nextEArrayIndex = nextNextEArrayIndex;
                    nextNextEArrayIndex = 1;
                }
                if (nextEArrayIndex <= 1) {
                    return;           // we've processed all edges (and there's no negative cycles)
                }
                // process all edges at this currPathLen
                while (nextEArrayIndex > 1) {
                    iterationCount++;
                    if (iterationCount % 5000 == 0) {
                        System.err.println("Edge #" + iterationCount);
                    }
                    Edge e = eArray[--nextEArrayIndex];
                    int v = e.v;   // tail parent
                    int w = e.w;   // head child

                    // first queue up all the edges for the next iteration (if there is a next iteration)
                    // those are the edges from the current vertex
                    LinkedNode n = g.getAdjList(w);
                    while (n != null) {
                        Edge e1 = n.edge();
                        if (!seenEdge.contains(e1)) {
                            nextEArray[nextNextEArrayIndex++] = e1;
                            seenEdge.add(e1);
                            p(" ******  added for next cycle: "+e1+".  Total set size now: "+(nextNextEArrayIndex-1));
                        }
                        n = n.next();
                    }
                    // now compute the DP result for this target vertex
                    int edgeWeight = e.getValue();
                    if (edgeWeight < 0) {
                        p(" $$$ processing negative edge weight  edge=" + e +
                                ", prevDistTo[" + w + "]=" + prevDistTo[w] +
                                ", prevDistTo[" + v + "]=" + prevDistTo[v] +
                                ",  calc check value=" + (prevDistTo[v] + edgeWeight));
                    }
                    int candWeight = prevDistTo[v] + edgeWeight;
                    if (prevDistTo[v] == NO_VALUE) {
                        candWeight = edgeWeight;
                    }
                    if (prevDistTo[w] > candWeight) {
                        p(" -- found lower weight="+candWeight+" at vertex=" + w + " for edge=" + e + ", prev=" + prevDistTo[w] +
                                ",  new min value="+candWeight);
                        // got a new minimum set the next DP value
                        currDistTo[w] = candWeight;
                        prevDistTo[w] = candWeight;    // we're the new minimum period
                        edgeTo[w] = e;    // remember tha min edge
                        if (addedEdge.contains(e)) {
                            p("  ===============>  addedEdge set detected duplicate edge add attempt: "+e);
                            hasNegativeCycle = true;
                            return;
                        }
                        else {
                            p("  ==>  adding edge to addedEdge set: "+e);
                            addedEdge.add(e);
                        }
                        foundLowerWeight = true;
                    }
                }
            }
        }
    }
    public int shortestDistanceTo(int v) {
        return currDistTo[v];
    }
    public int[] shortestDistanceToArray() {
        return currDistTo;
    }
    public String printPathTo(int v) {
        throw new RuntimeException("NYI");
    }

    //
    // run complete BF a second time and see if we found ANY lower weights
    // if we have, then there is a negative cycle
    //
    public boolean hasNegativeCycle() {
        if (!ranOnce) {
            throw new RuntimeException("Need to have run compute at least once before you can check for negative cycles.");
        }
        return hasNegativeCycle;
    }
    public String printMinimumPathEdges() {
        StringBuilder sb = new StringBuilder();
        for (Edge e : edgeTo) {
            if (e != null) {
                sb.append(e.toString() + "\n");
            }
        }
        return sb.toString();
    }
    public String printAllMinimumPaths() {
        StringBuilder sb = new StringBuilder();
        Stack<Edge> pathEdges = new Stack<>();
        int highestVertexNumber = g.highestVertexNumber;
        //int highestVertexNumber = 400;
        for (int i = 1; i <= highestVertexNumber; i++) {
            int currVertex = i;
            if (currVertex == sourceVertex) {
                continue;
            }
            int totalLength = 0;
            pathEdges.clear();
            while (currVertex != sourceVertex) {
                Edge currEdge = edgeTo[currVertex];
                if (currEdge == null) break;
                pathEdges.push(currEdge);
                totalLength += currEdge.value;
                currVertex = currEdge.v;
            }
            if (!pathEdges.isEmpty()) {
                StringBuilder singlePath = new StringBuilder(sourceVertex + " to " + i + " (" + totalLength + "): ");
                boolean first = true;
                while (!pathEdges.isEmpty()) {
                    Edge e = pathEdges.pop();
                    if (!first) {
                        singlePath.append(" -> ");
                    }
                    singlePath.append(e.toString());
                    first = false;
                }
                singlePath.append("\n");
                sb.append(singlePath.toString());
            }
        }
        return sb.toString();
    }

    public class Edge {
        final public int v;
        final public int w;
        final public int value;

        public Edge(int v, int w, int val) {
            this.v = v;
            this.w = w;
            this.value = val;
        }
        public int getValue() {
            return value;
        }
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Edge)) {
                return false;
            }
            Edge otherEdge = (Edge) other;
            if (!(otherEdge.v == v)) {
                return false;
            }
            if (!(otherEdge.w == w)) {
                return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            return v + w;
        }
        @Override
        public String toString() {
            return "[" + v + "-" + w + "](" + value + ")";
        }
    }

    /**
     * This is a directed graph
     */
    public class GraphAdjList {
        protected int lowestVertexNumber = NO_VALUE;     // for the START node
        protected int highestVertexNumber = -1;
        Set<Integer> vertices = new HashSet<>();
        protected int sizeLimit;
        protected int vCount;
        protected int eCount;
        protected boolean isDigraph = true;    // is directed graph
        protected LinkedNode[] adj;
        protected boolean[] marked;    // generic vertex marking.  use where needed

        public GraphAdjList() {
        }
        public GraphAdjList(int sizeLimit) {
            this.sizeLimit = sizeLimit;
            adj = new LinkedNode[sizeLimit];
        }
        public int vCount() {
            return vCount;
        }
        public int eCount() {
            return eCount;
        }
        public int getHighestVertexNumber() {
            return highestVertexNumber;
        }

        // directed graph so the edge goes from v to w
        public synchronized int insert(Edge e) {
            int v = e.v;
            int w = e.w;

            // track the highest vertex number that the graph has
            if (v > highestVertexNumber) highestVertexNumber = v;
            if (w > highestVertexNumber) highestVertexNumber = w;
            if (v < lowestVertexNumber) lowestVertexNumber = v;           // must be a start node
            if (!vertices.contains(v)) {
                vCount++;
                vertices.add(v);
            }
            if (!vertices.contains(w)) {
                vCount++;
                vertices.add(w);
            }
            // handle registered vertex with no edge.
            // insert a self-edge if there is not already one
            // link new node into the head of the list
            adj[v] = new LinkedNode(v, e, adj[v]);
            return ++eCount;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < adj.length; i++) {
                LinkedNode n = adj[i];
                if (n != null) {
                    sb.append("v=" + i + ": edges: ");
                    while (n != null) {
                        sb.append(n.edge().toString()).append(", ");
                        n = n.next();
                    }
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
        public LinkedNode getAdjList(int v) {
            return adj[v];
        }
        public Edge getEdge(int v, int w) {
            LinkedNode n = adj[v];
            if (n.w == w) {
                return n.e;
            }
            while (n.next != null) {
                n = n.next;
                if (n.w == w) {
                    return n.e;
                }
            }
            return null;
        }
        public int getEdgeValue(int v, int w) {
            Edge e = getEdge(v, w);
            if (e != null) {
                return e.value;
            }
            return NO_VALUE;
        }
        protected Edge newEdge(int i, int j, int value) {
            return new Edge(i, j, value);
        }
        protected String printNodeList(LinkedNode n) {
            if (n == null) return "NULL";
            StringBuilder sb = new StringBuilder();
            sb.append("vertex list for " + n.vertexTailNumber() + ": ");
            sb.append(n.vertexHeadNumber()).append(", ");
            while (n.hasNext()) {
                n = n.next();
                sb.append(n.vertexHeadNumber()).append(", ");
            }
            return sb.toString();
        }
        protected String printEdgeList(List<Edge> e) {
            if (e == null) return "";
            StringBuilder sb = new StringBuilder();
            sb.append("edge list: ");
            for (Edge edge : e) {
                sb.append(e.toString()).append(", ");
            }
            return sb.toString();
        }
    }

    public class LinkedNode {
        final int v;       // tail vertex number
        final int w;       // head vertex number
        final Edge e;
        LinkedNode next;

        public LinkedNode(int v, Edge e, LinkedNode next) {
            this.v = v;
            this.e = e;
            this.w = e.w;
            this.next = next;
        }
        public LinkedNode next() {
            return this.next;
        }
        public void setNext(LinkedNode n) {
            next = n;
        }
        public boolean hasNext() {
            return (next != null);
        }
        public int vertexHeadNumber() {
            return w;
        }
        public int vertexTailNumber() {
            return v;
        }
        public Edge edge() {
            return e;
        }
        public boolean isSelfEdge() {
            return e.w == e.v;
        }
        public int length() {
            int len = 1;
            LinkedNode n = next();
            while (n != null) {
                len++;
                n = n.next();
            }
            return len;
        }
        public String printNodeChain() {
            StringBuilder sb = new StringBuilder();
            LinkedNode n = this;
            while (n != null) {
                sb.append(n.vertexTailNumber() + "-" + n.vertexHeadNumber()).append(", ");
                n = n.next();
            }
            return sb.toString();
        }
    }

    static String simpleGraphAsString() {
        return "5 6\n" +
                "1 2 4\n" +
                "1 3 2\n" +
                "3 2 1\n" +
                "2 5 4\n" +
                "3 4 2\n" +
                "4 5 2";
    }

    static String simpleNegativeCycleGraphAsString() {
        return "5 6\n" +
                "1 2 2\n" +
                "1 3 1\n" +
                "3 4 5\n" +
                "2 4 -10\n" +
                "4 5 1\n" +
                "5 2 2";
    }

    private boolean isP() {
        return debug;
    }

    private void p(String s) {
        if (isP()) {
            System.err.println(s);
        }
    }

    protected void ingestData(String[] in) {
        if (in.length <= 0) {
            return;
        }
        String line = in[0];
        String[] s = line.split("\\s+");
        int numVertices = Integer.valueOf(s[0]);
        int numEdges = Integer.valueOf(s[1]);
        p(" num vertices=" + numVertices + ", num edges=" + numEdges);
        int lc = 0;
        for (int i = 1; i < in.length; i++) {
            line = in[i];
            //System.err.println("read line '" + line + "'");
            s = line.split("\\s+");
            int v = Integer.valueOf(s[0]);
            int w = Integer.valueOf(s[1]);
            int weight = Integer.valueOf(s[2]);
            Edge e = new Edge(v, w, weight);
            g.insert(e);
            lc++;
            if (lc % 10 == 0) {
                //  System.err.println("read vertex #" + i + " = " + i);
            }
        }
    }

    public void readDataString(String s) {
        g = new GraphAdjList(1001);
        String[] sa = s.split("\\n");
        ingestData(sa);
    }

    protected void readDataFile(String inputFName) {
        //
        //  Graph of no more than 1000 vertices
        //
        g = new GraphAdjList(100001);
        FileReader fileR = null;
        String f = "g1_stanford_coursera";
        String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\graph\\allPairsShortestPaths";

        String fileName = d + "\\" + f;
        if (inputFName != null && inputFName.length() > 0) {
            fileName = inputFName;
        }
        try {
            fileR = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            System.err.println(" cannot open data file " + fileName);
        }
        List<String> list = new ArrayList<>();
        // get count so that we can build only the array we need
        try {
            BufferedReader br = new BufferedReader(fileR);
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
            br.close();
        } catch (IOException e) {
            System.err.println("exception " + e.getMessage());
        }
        String[] data = list.toArray(new String[]{});
        ingestData(data);
    }

    public static void main(String[] args) {
        boolean readDataFromFile = false;
        //boolean readDataFromFile = true;
        BellmanFord_SingleSourceAllShortestPaths prog = new BellmanFord_SingleSourceAllShortestPaths();
        if (readDataFromFile) {
            String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\graph\\allPairsShortestPaths";
            //String f = "simpleGraph";
            //String f = "simpleNegativeCycleGraph";
            //String f = "g1_stanford_coursera";     // 1000 nodes    47978 edges
            String f = "large_stanford_coursera";
            String fileName = d + "\\" + f;
            prog.readDataFile(fileName);
        } else {
            System.out.print("Hi.  Welcome to the Bellman-Ford Dynamic Programming Single Source all pairs minimum path demo.\n" +
                    "  Given an input Directed Graph with Weighted Edges, starting from the lowest positive numbered vertex as the origin \n" +
                    "  Find the lowest cost paths from the origin node to each destination node.\n" +
                    "  The algo is invalid if the graph contains a negatively valued cycle.\n" +
                    "  If there is a negatively valued cycle, this algo will find it and report so.\n\n" +
                    "     Which demo would you like to run ?\n" +
                    "        1)  simple graph.\n" +
                    "        2)  simple graph with negative cycle.\n" +
                    "     Choice: ");
            Scanner s = new Scanner(System.in);
            int i = s.nextInt();
            if (i < 1 || i > 2) {
                System.out.println("\n you entered: " + i + ", you must enter '1' or '2'.  exiting.  try running demo again from scratch.");
            }
            String in;
            if (i == 1) {
                in = simpleGraphAsString();
            } else {
                in = simpleNegativeCycleGraphAsString();
            }
            prog.readDataString(in);
        }
        prog.compute(1);
        boolean hasNegativeCycle = prog.hasNegativeCycle();
        System.err.println(" graph has Negative Cycle = " + hasNegativeCycle + "\n\n");

        if (hasNegativeCycle) {
            System.err.println("Since the graph has a negative cycle, this algorithm cannot run successfully on it.");
            return;
        }
        // print the computed array
        //String edges = prog.printMinimumPathEdges();
        //System.err.println("Minimum path edges:\n" + edges + "\n");

        String paths = prog.printAllMinimumPaths();
        System.err.println("All Minimum Paths:\n" + paths + "\n");
    }
}

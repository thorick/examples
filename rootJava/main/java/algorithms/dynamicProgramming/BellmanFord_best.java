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
 * Runs Bellman Ford  on my standard high overhead digraph classes
 * <p/>
 * This implementation uses a very spare implementation of the DP algorithm using arrays
 * <p>
 * No Queues or preprocessing other than initialized node values
 * <p>
 * A true march forward implementation that only has to look back at the most recently
 * cached 'best' value for a previous iteration
 * This is *strict* Bellman Ford that counts number of DP iterations
 * and begins each round by starting at the source vertex 's'.
 * <p>
 * <p/>
 * <p/>
 * Will detect negative cycles by running past the maximum number of required vertex processing
 * (unless it stops early by detecting no changes before then)
 * <p/>
 *
 * simpleGraph:
 *
 * input graph:
 *
 * 5 6
 * 1 2 4
 * 1 3 2
 * 3 2 1
 * 2 5 4
 * 3 4 2
 * 4 5 2
 *
 * output:
 *
 * num vertices=5, num edges=6
 *  read 6 edges.  read 0 negative edges.
 * begin BellMan Ford for source vertex=1
 *   number of vertices n=5
 *   will check for negative cycles=true
 *   do for iterations=6
 *
 *  graph simpleGraph hasNegativeCycle=false
 *
 * 5  [2147483647] [2147483647] [2147483647] [         7] [         6] [         6]
 *    [2147483647] [2147483647] [         4] [2147483647] [2147483647] [2147483647]
 *    [2147483647] [         2] [2147483647] [2147483647] [2147483647] [2147483647]
 *    [2147483647] [         4] [         3] [2147483647] [2147483647] [2147483647]
 *    [         0] [         0] [2147483647] [2147483647] [2147483647] [2147483647]
 * 0  [2147483647] [2147483647] [2147483647] [2147483647] [2147483647] [2147483647]
 *              0            1            2            3            4            5
 *
 * Minimum path edges:
 * [1 - 3, value=2]
 * [3 - 2, value=1]
 * [3 - 4, value=2]
 * [4 - 5, value=2]
 *
 */
public class BellmanFord_best {
    private boolean debug = true;                     // do not depend on setting loggers
    private boolean readDataFile = false;             // local data for ease of download and demo
    private boolean computeHasNegativeCycles = true;
    private final int NO_VALUE = Integer.MAX_VALUE;
    private int graphVertexArraySize;
    private int minVertexNumber;
    private int maxVertexNumber;
    private int numberOfVertices;
    private boolean hasNegativeCycle = false;
    private GraphAdjList g;
    int[][] iterationValueArr;      // there can be a max of n-1 iterations where n = number of nodes
    private int sourceVertex = -1;  // keep track of who we ran this on.

    public BellmanFord_best() {
    }

    /**
     * @param s starting vertex
     * @return 0  compute OK
     * -1  negative cycle detected
     */
    public int compute() {
        sourceVertex = g.lowestVertexNumber;
        minVertexNumber = 1;
        maxVertexNumber = g.getHighestVertexNumber();
        numberOfVertices = maxVertexNumber;
        // vertices start at 1 (and not 0) in the input graphs
        graphVertexArraySize = maxVertexNumber + 1;
        if (computeHasNegativeCycles) {
            graphVertexArraySize++;      // requires space for an additional cycle
        }
        iterationValueArr = new int[graphVertexArraySize][graphVertexArraySize];

        // after iteration 0, any edge that reaches a node for the first time is the lowest cost edge
        // force the acceptance of the first edge.  NO_VALUE is the largest possible int
        for (int i = 0; i < graphVertexArraySize; i++) {
            for (int v = 0; v < graphVertexArraySize; v++) {
                iterationValueArr[i][v] = NO_VALUE;
            }
        }
        // the source vertex is a special case, it's minimum path cost is zero since it is the starting point
        iterationValueArr[0][sourceVertex] = 0;
        iterationValueArr[1][sourceVertex] = 0;

        int countDown = numberOfVertices;
        if (computeHasNegativeCycles) {
            countDown++;
        }
        p("begin BellMan Ford for source vertex=" + sourceVertex +
                "\n  number of vertices n=" + g.vCount() +
                "\n  will check for negative cycles=" + computeHasNegativeCycles +
                "\n  do for iterations=" + countDown+
                "\n");

        runCompleteBF(sourceVertex, countDown);
        if (computeHasNegativeCycles) {
            if (hasNegativeCycle) {
                return -1;
            }
        }
        return 0;
    }

    private void runCompleteBF(int sourceVertex, int countDown) {
        int iteration = 1;
        List<Integer> nextIterationNodeNumbers = new ArrayList<>();   // the nodes for the next iteration
        nextIterationNodeNumbers.add(sourceVertex);
        // now recurse
        bf(nextIterationNodeNumbers, iteration, countDown);
    }

    private void bf(List<Integer> nextIterationNodeNumbers, int iteration, int countDown) {
        if (countDown <= 0) {
            return;
        }
        boolean isNegativeCycleCheckIteration = (countDown == 1 && computeHasNegativeCycles);
        /*
        if (nextIterationNodeNumbers.size() <= 0) {
            return;
        }
        //  vertices start at number 1  not zero so the size of the array is 1 larger than the number of vertices
        //  the iteration limit is the number of vertices - 1
        if (iteration >= numberOfVertices) {
            return;      // done
        }
        */

        boolean foundLowerCostPath = false;
        List<Integer> nextNextIterationNodeNumbers = new ArrayList<>();   // the nodes for the next iteration
        for (Integer currIterationNodeNumber : nextIterationNodeNumbers) {
            LinkedNode node = g.getAdjList(currIterationNodeNumber);
            while (node != null) {
                int nodeNumber = node.v;
                if (isNegativeCycleCheckIteration) {
                    nextNextIterationNodeNumbers.add(nodeNumber);   // if we will be doing a negative cycle check
                                                                    // we have to have this node to run the next iteration on
                }
                if (nodeNumber != currIterationNodeNumber) {
                    throw new RuntimeException("Error !  for currIterationNodeNumber=" + currIterationNodeNumber + " got unexpected edge from: " +
                            nodeNumber);
                }
                Integer destNodeNumber = node.w;
                nextNextIterationNodeNumbers.add(destNodeNumber);
                int value = node.e.value;
                int prev_value = iterationValueArr[iteration - 1][nodeNumber];
                //  sometimes the valid previous lowest value was computed at more than 1 hop back
                // and we don't continuously update the entire array foward of that hop
                if (prev_value == NO_VALUE) {
                    int tempIter = iteration - 2;
                    while (tempIter > 0) {
                        if (iterationValueArr[tempIter][nodeNumber] < NO_VALUE) {
                            prev_value = iterationValueArr[tempIter][nodeNumber];
                            break;
                        }
                        tempIter--;
                    }
                }
                int candidate = prev_value + value;
                if (prev_value == NO_VALUE) {
                    candidate = NO_VALUE;
                }
                if (candidate < iterationValueArr[iteration - 1][destNodeNumber]) {
                    iterationValueArr[iteration][destNodeNumber] = candidate;
                    foundLowerCostPath = true;
                    if (isNegativeCycleCheckIteration) {
                        hasNegativeCycle = true;    // if we have done more than the whole graph and still get a lower value, then we have a negative cycle
                    }
                } else {
                    iterationValueArr[iteration][destNodeNumber] = iterationValueArr[iteration - 1][destNodeNumber];
                }
                node = node.next();    // next edge from current node
            }
            iteration++;
            countDown--;
            if (isNegativeCycleCheckIteration) {
                return;
            }
            // now recurse
            bf(nextNextIterationNodeNumbers, iteration, countDown);
        }
    }

    public String printMinimumPathEdges() {
        List<int[]> minPathEdges = getMinimumPathEdges();
        StringBuilder sb = new StringBuilder();
        for (int[] edge : minPathEdges) {
            sb.append("["+edge[0]+" - "+edge[1]+", value="+edge[2]+"]\n");
        }
        return sb.toString();
    }
    /**
     *
     * Produce a list of the edges that comprise the minimum paths from the source vertex.
     *
     * First go through the result matrix making a note of the valid path values for every node.
     * Because of the nature of the DP algorithm there may be more than one value assigned to a given node
     * across different iterations.
     * The valid value is the value marked in the later most iteration because this will always be the optimized value.
     *
     * Once we have the valid iteration number for each node value we can examine the nodes in iteration order
     * from start to finish.
     * At each iteration as we encounter a valid node value we seek the edge from a node in a previous iteration that
     * yields the optimal path value.   This is the value of the previous node plus the value of the edge to the current node.
     *
     * As we add new nodes to the list of confirmed paths these nodes become the start nodes for processing future iterations.
     *
     * Because of the nature of the DP algorithm we know that a node can be an 'active' source node for at most 2 iterations
     * which result in the addition of a new optimal edge, thus we keep running lists of the candidate source nodes:
     * those from 2 iterations ago, those from the previous iteration and
     * the current destination edge nodes that became the source nodes for the next iteration.
     *
     *
     * @return
     */
    public List<int[]> getMinimumPathEdges() {
        List<int[]> result = new ArrayList<>();
        Set<Integer> seenNodes = new HashSet<>();
        int[] values = new int[graphVertexArraySize];       // the first vertex is 1 not 0  the if there are 5 vertices 1-5 you need an array of size 6
        int maxIteration = numberOfVertices;    // the last iteration #    first iteration is 0  the first vertex is 1 NOT 0
        int[] validIterationForNode = new int[numberOfVertices + 1];
        for (int i = 0; i < maxIteration; i++) {
            validIterationForNode[i] = NO_VALUE;
        }
        int iteration = maxIteration;
        while (iteration > 0) {
            for (int i = minVertexNumber; i <= maxVertexNumber; i++) {
                if (!seenNodes.contains(i)) {
                    if (iterationValueArr[iteration][i] != NO_VALUE) {
                        values[i] = iterationValueArr[iteration][i];
                        validIterationForNode[i] = iteration;
                        seenNodes.add(i);
                    }
                }
            }
            iteration--;
        }
        // now start at the start node
        // and go forward through the iteration sequence linking up the valid edges linking the progression of nodes
        List<Integer> fromNodes = new LinkedList<>();
        fromNodes.add(sourceVertex);
        List<Integer> fromFromNodes = new LinkedList<>();     // we're at most always 2 hops away this is the age out
        List<Integer> nextNodes = new LinkedList<>();         // queue up the next source nodes
        iteration = 1;
        seenNodes.clear();
        seenNodes.add(sourceVertex);
        while (iteration <= numberOfVertices) {
            //p("\niteration " + iteration);
            boolean gotEdgeForIteration = false;
            for (int toNode = 1; toNode <= maxVertexNumber; toNode++) {
                int val = iterationValueArr[iteration][toNode];
                if (val != NO_VALUE) {
                    boolean foundEdge = false;
                    if (validIterationForNode[toNode] == iteration) {
                        // we've got a hit this is a valid new node  find the edge !
                        // go through all the source nodes and find our matching edge
                        for (Integer fromNode : fromFromNodes) {
                            int edgeValue = g.getEdgeValue(fromNode, toNode);
                            if (edgeValue != NO_VALUE) {
                                int candidateValue = values[fromNode] + edgeValue;
                                if (candidateValue == val) {
                                    // this is our edge  assuming that the values are unique
                                    Edge edge = g.getEdge(fromNode, toNode);
                                    if (edge != null) {
                                        int[] entry = new int[]{edge.v, edge.w, edge.value};
                                        result.add(entry);
                                        //p("Added optimal edge: [" + edge.v + " - " + edge.w + ", value=" + edge.value + "]");
                                        nextNodes.add(toNode);           // join the set of reached nodes
                                        foundEdge = true;
                                        gotEdgeForIteration = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!foundEdge) {
                            for (Integer node : fromNodes) {
                                int edgeValue = g.getEdgeValue(node, toNode);
                                if (edgeValue != NO_VALUE) {
                                    int candidateValue = values[node] + edgeValue;
                                    if (candidateValue == val) {
                                        // this is our edge  assuming that the values are unique
                                        Edge edge = g.getEdge(node, toNode);
                                        if (edge != null) {
                                            int[] entry = new int[]{edge.v, edge.w, edge.value};
                                            result.add(entry);
                                            //p("Added optimal edge: [" + edge.v + " - " + edge.w + ", value=" + edge.value + "]");
                                            nextNodes.add(toNode);        // join the set of reached nodes
                                            foundEdge = true;
                                            gotEdgeForIteration = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (gotEdgeForIteration) {
                for (Integer fromFromNode : fromFromNodes) {
                    seenNodes.add(fromFromNode);
                }
                fromFromNodes.clear();
                fromFromNodes.addAll(fromNodes);
                fromNodes.clear();
                fromNodes.addAll(nextNodes);
                nextNodes.clear();
            }
            //p("next fromFromNodes: " + printListOfInteger(fromFromNodes));
            //p("next fromNodes: " + printListOfInteger(fromNodes));
            iteration++;
        }
        return result;
    }

    public boolean hasNegativeCycle() {
        return hasNegativeCycle;
    }

    public void printResultArray() {
        String arrString = printPadded2DIntArray(iterationValueArr);
        System.err.println(arrString);
    }

    public String printListOfInteger(List<Integer> l) {
        StringBuilder sb = new StringBuilder("List: [");
        boolean first = true;
        for (Integer i : l) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(i);
        }
        sb.append("]");
        return sb.toString();
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
        for (int i=1; i<in.length; i++) {
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
        g = new GraphAdjList(1001);
        FileReader fileR = null;
        String f = "g1_stanford_coursera";
        String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\graph\\allPairsShortestPaths";

        String fileName = d + "\\" + f;
        if (inputFName != null && inputFName.length() > 0) {
            fileName = inputFName;
        }
        int negativeEdgeCount = 0;
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

    String printPadded2DIntArray(int[][] v) {
        int xlen = v.length;
        int ylen = v[0].length;
        int temp = ylen;
        int maxylen = 1;
        while (temp > 9) {
            temp /= 10;
            maxylen++;
        }
        StringBuilder tempSB = new StringBuilder();
        while (tempSB.length() < maxylen) {
            tempSB.append(" ");
        }
        String frontBlankYPad = tempSB.toString();
        StringBuilder sb = new StringBuilder();
        int maxSize = 0;
        for (int i = 0; i < v.length; i++) {
            for (int j = 0; j < v[i].length; j++) {
                if (v[i][j] > maxSize) {
                    maxSize = v[i][j];
                }
            }
        }
        int numSize = 1;
        while (maxSize > 9) {
            maxSize /= 10;
            numSize++;
        }
        for (int j = ylen - 1; j >= 0; j--) {
            if (j % 5 == 0) {
                sb.append(frontPad(String.valueOf(j), maxylen));
            } else {
                sb.append(frontBlankYPad);
            }
            sb.append("  ");
            for (int i = 0; i < xlen; i++) {
                int val = v[i][j];
                String sval = String.valueOf(val);
                if (val < 0) {
                    sval = "x";
                }
                sb.append("[").append(frontPad(sval, numSize)).append("] ");
            }
            sb.append("\n");
        }
        sb.append(frontBlankYPad);
        sb.append("  ");
        for (int i = 0; i < xlen; i++) {
            sb.append(" ").append(frontPad(String.valueOf(i), numSize)).append("  ");
        }
        sb.append("\n");
        return sb.toString();
    }


    String frontPad(String in, int len) {
        int diff = 0;
        if (in.length() < len) {
            diff = len - in.length();
        }
        if (diff > 0) {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < diff) {
                sb.append(" ");
            }
            return sb.toString() + in;
        } else {
            return in;
        }
    }

    /**
     * This is a directed graph
     */
    public class GraphAdjList {
        protected int lowestVertexNumber = Integer.MAX_VALUE;     // for the START node
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

    public class Edge {
        final public int v;
        final public int w;
        final public int value;

        // for now the data is stored in the Edge
        // this isn't great when data is not unique per edge
        // in that case there'd be some kind of pointer
        // referencing other storage

        // if you don't need edge data and the graph
        // contains many edges, you might want to get rid
        // of the unused data reference for the space that it
        // uses up.
        public Edge(int v, int w, int val) {
            this.v = v;
            this.w = w;
            this.value = val;
        }
        public int getValue() {
            return value;
        }
        @Override
        public String toString() {
            return "Edge: " + v + "-" + w + ", value=" + value;
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
    public static void main(String[] args) {
        boolean readDataFromFile = false;
        BellmanFord_best prog = new BellmanFord_best();
        if (readDataFromFile) {
            String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\datastructures\\graph\\allPairsShortestPaths";
            //String f = "simpleGraph";
            String f = "simpleNegativeCycleGraph";
            String fileName = d + "\\" + f;
            prog.readDataFile(fileName);
        }
        else {
            System.out.print("Hi.  Welcome to the Bellman-Ford Dynamic Programming Single Source all pairs minimum path demo.\n" +
                    "  Given an input Directed Graph with Weighted Edges, starting from the lowest positive numbered vertex as the origin \n" +
                    "  Find the lowest cost paths from the origin node to each destination node.\n"+
                    "  The algo is invalid if the graph contains a negatively valued cycle.\n"+
                    "  If there is a negatively valued cycle, this algo will find it and report so.\n\n"+
                    "     Which demo would you like to run ?\n"+
                    "        1)  simple graph.\n"+
                    "        2)  simple graph with negative cycle.\n"+
                    "     Choice: ");
            Scanner s = new Scanner(System.in);
            int i = s.nextInt();
            if (i < 1 || i > 2) {
                System.out.println("\n you entered: "+i+", you must enter '1' or '2'.  exiting.  try running demo again from scratch.");
            }
            String in;
            if (i == 1) {
                in = simpleGraphAsString();
            }
            else {
                in = simpleNegativeCycleGraphAsString();
            }
            prog.readDataString(in);
        }
        prog.compute();
        boolean hasNegativeCycle = prog.hasNegativeCycle();
        System.err.println(" graph has Negative Cycle = " + hasNegativeCycle+"\n\n");

        // print the computed array
        prog.printResultArray();
        String edges = prog.printMinimumPathEdges();
        System.err.println("Minimum path edges:\n"+edges+"\n");
    }
}

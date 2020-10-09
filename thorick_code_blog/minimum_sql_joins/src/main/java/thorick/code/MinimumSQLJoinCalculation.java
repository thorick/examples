package thorick.code;

//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;

import java.util.*;

public class MinimumSQLJoinCalculation {
    //protected final Logger log = LogManager.getLogger(MinimumSQLJoinCalculation.class);

    Graph originalGraph;

    public Set<Join> computeMinimumSQLJoins(Graph g, Set<String> targetNodes) {
        g.resetForCalculation();
        Graph mstGraph = computeSpanningTree(g, targetNodes);
        Graph finalGraph = unpackClosureMSTGraph(mstGraph);
        Set<Join> minimumJoinSet = unpackJoins(finalGraph);
        // logging
        //log.debug("\n\n ==========  JOINS:\n" + Join.printJoinList(minimumJoinSet, g));
        return minimumJoinSet;
    }

    public Graph computeSpanningTree(Graph g, Set<String> targetNodes) {
        Graph closureMSTGraph = computeClosureMSTGraph(g, targetNodes);
        Graph finalGraph = unpackClosureMSTGraph(closureMSTGraph);
        // logging
        //Set<Join> minimumJoinSet = unpackJoins(finalGraph);
        //log.debug("\n\n ==========  JOINS:\n" + Join.printJoinList(minimumJoinSet, g));
        return finalGraph;
    }

    // broken out this way in order to be unit testable
    public Graph computeClosureMSTGraph(Graph g, Set<String> targetNodes) {
        originalGraph = g;
        Graph closureGraph = constructClosureGraph(targetNodes);
        Graph closureMSTGraph = closureMSTGraph(closureGraph);
        // logging
        //Collection<SteinerEdge> edges = closureMSTGraph.getClosureGraphEdgeSet();
        //log.debug("\n\n   --  closureMSTGraph edges: " +
        //        "   Closure Graph2 Edge2 Set: \n" + SteinerEdge.printEdgeList(edges, originalGraph) + "\n\n");
        return closureMSTGraph;
    }

    public Graph constructClosureGraph(Set<String> targetNodes) {
        String[] nodes = targetNodes.toArray(new String[targetNodes.size()]);
        // logging
        //log.debug("enter constructMetricClosureGraph with input graph with " + nodes.length + " nodes.");
        if (nodes.length == 0) return new Graph();
        Graph closureGraph = new Graph();
        if (nodes.length == 1) {
            closureGraph.addNewNode(nodes[0]);
            return closureGraph;
        }
        Djikstra djikstra = new Djikstra(originalGraph);

        //
        // do shortest edges set between all pairs in the original graph
        // from each starting node we run Djikstra to find the single source shortest paths to each other node in the set.
        //
        for (int i = 0; i < (nodes.length - 1); i++) {
            String firstNodeName = nodes[i];
            Set<String> destNodes = new HashSet<String>();
            for (int j = (i + 1); j < nodes.length; j++) {
                destNodes.add(nodes[j]);
            }
            //
            // for this start node and the set of destNodes
            // run Djikstra's Shortest Path to get the set of shortest Edges in terms of the original Graph
            // convert each of these Shortest Paths into single Edges that will comprise the Metric Closure Graph
            // add the edges to the Metric Closure Graph
            //
            PrimDjikstraNode firstNode = originalGraph.getNode(firstNodeName);
            // logging
            //if (log.isDebugEnabled())
            //    log.debug("BEGIN closureGraph paths from: " + firstNode.toString(originalGraph));

            Djikstra.Result result = djikstra.shortestPathsFromSourceNode(firstNode, destNodes);
            Map<String, List<SteinerEdge>> shortestPathsMap = result.getShortestPathsMap();
            Iterator<String> it = shortestPathsMap.keySet().iterator();
            while (it.hasNext()) {
                String to = it.next();
                List<SteinerEdge> l = shortestPathsMap.get(to);
                if (l.size() <= 0) continue;  // not possible unless a disconnected graph
                SteinerEdge edge = null;
                if (l.size() == 1) {
                    SteinerEdge e = l.get(0);
                    // logging
                    //log.debug("---  next edge: " + e.toString(originalGraph));
                    edge = originalGraph.addNewEdgeAndNodesToDestGraphMaybe(e, originalGraph, closureGraph);
                } else {
                    edge = originalGraph.addNewCompositeEdgeAndNodesToDestGraphMaybe(l, originalGraph, closureGraph);
                }
                SteinerEdge opposite = originalGraph.addOppositeNewEdgeAndNodesToDestGraphMaybe(edge, originalGraph, closureGraph);
                closureGraph.addClosureGraphEdge(edge);
                closureGraph.addClosureGraphEdge(opposite);
                // logging
                //log.debug("     +++++ added Edge for " + firstNode.toString(originalGraph) +
                //        " -- " + to + " : " + edge.toString(originalGraph));
            }
        }
        // logging
        //log.debug("\n\n\n\n  find closureGraph COMPLETE.\n\n\n" +
        //         "   Closure Graph2 Edge2 Set: \n" + SteinerEdge.printEdgeList(closureGraph.getClosureGraphEdgeSet(), originalGraph) + "\n\n");
        return closureGraph;
    }

    private Graph closureMSTGraph(Graph closureGraph) {
        //log.debug("--- compute MST on closureGraph edges");
        PrimsMST prims = new PrimsMST(closureGraph);
        Graph closureMST = prims.computeMST();
        // MST graph is a subset of the input graph containing ONLY an MST of the original input graph
        return closureMST;
    }

    public void addJoinToGraphByTableName(String t1, String t2) {
        addJoinToGraphByTableName(t1, t2, null, null);
    }

    // default to inner join
    public void addJoinToGraphByTableName(String t1, String t2, Join.JoinType t, Integer weight) {
        if (t1 == null || t1.length() <= 0 || t2 == null || t2.length() <= 0) {
            throw new RuntimeException("table names cannot be null or empty");
        }
        if (originalGraph == null) {
            originalGraph = new Graph();   // problem solved !
        }
        if (t == null) t = Join.JoinType.INNER;
        if (weight == null || weight < 1) weight = 1;
        PrimDjikstraNode n1 = null;
        Integer num1 = originalGraph.nodeNameToTableNumMap.get(t1);
        if (num1 == null) {
            n1 = originalGraph.addNewNode(t1);
        }
        PrimDjikstraNode n2 = null;
        Integer num2 = originalGraph.nodeNameToTableNumMap.get(t2);
        if (num2 == null) {
            n2 = originalGraph.addNewNode(t2);
        }
        if (originalGraph.containsEdge(num1, num2)) return;
        Join join = new Join(num1, num2, weight, t);
        SteinerEdge edge = new SteinerEdge(join, false);
        originalGraph.addNewEdge(edge);
        return;
    }

    private Graph unpackClosureMSTGraph(Graph inputGraph) {
        Graph outputGraph = new Graph();
        Set<SteinerEdge> edgeSet = inputGraph.getClosureGraphEdgeSet();
        for (SteinerEdge edge : edgeSet) {
            if (edge.isComposite()) {
                List<SteinerEdge> compositeEdgeList = edge.getCompositeEdges();
                for (SteinerEdge cEdge : compositeEdgeList) {
                    outputGraph.addClosureGraphEdge(cEdge);
                }
            } else {
                outputGraph.addClosureGraphEdge(edge);
            }
        }
        return outputGraph;
    }

    private Set<Join> unpackJoins(Graph g) {
        Set<Join> set = new HashSet<Join>();
        Set<SteinerEdge> edgeSet = g.getClosureGraphEdgeSet();
        for (SteinerEdge edge : edgeSet) {
            Join join = edge.getJoin();
            set.add(join);
        }
        return set;
    }

    private static void demo_log(String s) {
        System.err.println(s);
    }

    public static void main(String[] args) {
        /*
        demo_log("\n\n===============================================================\n");
        demo_log("   BEGIN  Minimum SQL Join Demo\n");
        demo_log("===============================================================\n");
        Graph g = new Graph();
        SteinerEdge eAB = g.addEdgeToGraphByTableName("A", "B", Join.JoinType.LEFT_OUTER, 1);
        SteinerEdge eBC = g.addEdgeToGraphByTableName("B", "C");
        SteinerEdge eCD = g.addEdgeToGraphByTableName("C", "D");
        SteinerEdge eAD = g.addEdgeToGraphByTableName("A", "D");
        SteinerEdge eDE = g.addEdgeToGraphByTableName("D", "E");
        SteinerEdge eEF = g.addEdgeToGraphByTableName("E", "F");
        SteinerEdge eAG = g.addEdgeToGraphByTableName("A", "G");
        SteinerEdge eGD = g.addEdgeToGraphByTableName("G", "D");
        String allJoins = g.printAllJoins();
        demo_log(   "---------------------------       Demo JOIN set       -------------------------\n"+
                "This is a small and simple example to demonstrate the Steiner Graph algorithm.\n"+
                "There are 7 Tables: {A, B, C, D, E, F, G}\n"+
                "And 8 Joins connecting them:\n\n"+allJoins);
        demo_log("\n\n\n");

        StringBuilder sb = new StringBuilder("--------------------------  Demo Target Table Subset  ------------------------\n"+"{");
        Set<String> targetNodes = new HashSet<String>();
        targetNodes.add("A");
        sb.append("A, ");
        targetNodes.add("B");
        sb.append("B, ");
        targetNodes.add("F");
        sb.append("F}");
        demo_log(sb.toString()+"\n\n\n");
        MinimumSQLJoinCalculation gen = new MinimumSQLJoinCalculation();


        Set<Join> resultJoinSet = gen.computeMinimumSQLJoins(g, targetNodes);
        String resultString = Join.printJoinList(resultJoinSet, g);
        demo_log("Computed Minimum SQL Join Set to reach Target Tables:");
        demo_log(resultString+"\n");
        demo_log("   END    Minimum SQL Join Demo\n");
        demo_log("===============================================================\n");
*/


        demo_log("\n\n===============================================================\n");
        demo_log("   BEGIN  Minimum SQL Join Demo\n");
        demo_log("===============================================================\n");
        Graph g = new Graph();
        SteinerEdge eAB = g.addBidirectionalEdgeToGraphByTableName("A", "B", Join.JoinType.LEFT_OUTER, 1);
        SteinerEdge eAD = g.addBidirectionalEdgeToGraphByTableName("A", "D");
        SteinerEdge eAG = g.addBidirectionalEdgeToGraphByTableName("A", "G");
        SteinerEdge eAI = g.addBidirectionalEdgeToGraphByTableName("A", "I");
        SteinerEdge eAK = g.addBidirectionalEdgeToGraphByTableName("A", "K");
        SteinerEdge eBC = g.addBidirectionalEdgeToGraphByTableName("B", "C");
        SteinerEdge eBH = g.addBidirectionalEdgeToGraphByTableName("B", "H");
        SteinerEdge eCD = g.addBidirectionalEdgeToGraphByTableName("C", "D");
        SteinerEdge eDE = g.addBidirectionalEdgeToGraphByTableName("D", "E");
        SteinerEdge eDP = g.addBidirectionalEdgeToGraphByTableName("D", "P");
        SteinerEdge eEO = g.addBidirectionalEdgeToGraphByTableName("E", "O");
        SteinerEdge eGD = g.addBidirectionalEdgeToGraphByTableName("G", "D");
        SteinerEdge eHR = g.addBidirectionalEdgeToGraphByTableName("H", "R");
        SteinerEdge eKN = g.addBidirectionalEdgeToGraphByTableName("K", "N");
        SteinerEdge eNO = g.addBidirectionalEdgeToGraphByTableName("N", "O");
        SteinerEdge eOP = g.addBidirectionalEdgeToGraphByTableName("O", "P");
        SteinerEdge eOR = g.addBidirectionalEdgeToGraphByTableName("O", "R");




        //String allJoins = g.printAllJoins();
        String allJoins = g.printAllUniqueJoins();
        demo_log(   "---------------------------       Demo JOIN set       -------------------------\n"+
                "This is a small and simple example to demonstrate the Steiner Graph algorithm.\n"+
                "A handy diagram of this graph as well as algorithm results can be found at:\n"+
                "      http://thorick-code.blogspot.com/MinimalXXX.html\n\n"+
                "There are 13 Tables: {A, B, C, D, E, G, H, I, K, N, O, P, R}\n"+
                "And 17 Joins connecting them:\n\n"+allJoins);
        demo_log("\n\n\n");

        StringBuilder sb = new StringBuilder("--------------------------  Demo 1 Minimal Joins to Connect 2 Tables  ------------------------\n"+
                "We wish to find the Minimum Number of SQL Joins required to get\n"+
                "access to columns from the following Subset of Tables from our Set:\n\n");
        sb.append("< INPUT >\n");

        Set<String> targetNodes = new HashSet<String>();
        targetNodes.add("I");
        targetNodes.add("O");
        sb.append("     {");
        sb.append("I, ");
        sb.append("O}\n\n");


        sb.append("< Run Computation >\n");
        //targetNodes.add("F");
        //sb.append("F}");
        demo_log(sb.toString());
        MinimumSQLJoinCalculation gen = new MinimumSQLJoinCalculation();


        Set<Join> resultJoinSet = gen.computeMinimumSQLJoins(g, targetNodes);
        String resultString = Join.printJoinList(resultJoinSet, g);
        sb = new StringBuilder();
        sb.append("< RESULT >\n\n");
        sb.append("It turns out that in this case there are 3 unique Subsets of Joins that meet our Minimum Criteria\n"+
                "which of the three possible Subsets of Joins that are selected may depend on the\n"+
                "particular JVM that you are using to run this demo.\n"+
                "To see the 3 possible Subsets of Joins see the Demo Graph Diagram in the link: \n" +
                "      https://thorick-code.blogspot.com/2020/07/the-sql-join-minimal-subset-problem.html\n\n"+
                "each Subset of Minimum Joins is shown chained together in a different color.\n\n");
        sb.append("Computed Minimum SQL Join Set to reach Target Tables:\n");
        sb.append(resultString+"\n\n\n\n");
        sb.append("---------  Demo 2:  Add Another Table to the Target Table Subset And Recompute -------\n\n");
        //sb.append("--------------------------  Demo 2 Target Table Subset  ------------------------\n");
        sb.append("Now let's change the Target Table Set {I, O} by adding a new Table 'H' to it \n"+
                "and then rerun the algorithm on the new Target Table Set, we should get a different set of Joins:\n\n");
        targetNodes.add("H");
        sb.append("< INPUT >\n");
        sb.append("     {");
        sb.append("H, I, ");
        sb.append("O}\n");
        demo_log(sb.toString());
        gen = new MinimumSQLJoinCalculation();
        resultJoinSet = gen.computeMinimumSQLJoins(g, targetNodes);
        resultString = Join.printJoinList(resultJoinSet, g);
        sb = new StringBuilder();
        sb.append("< Run Computation >\n\n");
        sb.append("< RESULT >\n\n");
        sb.append("In this case there is only 1 Subset of 5 Joins that meet our Minimum Criteria\n\n"+
        "There is a graph showing these Joins in another Demo Graph Diagram in the link: \n" +
                "      https://thorick-code.blogspot.com/2020/07/the-sql-join-minimal-subset-problem.html\n\n");
        sb.append("Computed Minimum SQL Join Set to reach Target Tables:\n");
        sb.append(resultString+"\n\n");
        demo_log(sb.toString());
        demo_log("   END    Minimum SQL Join Demo\n");
        demo_log("===============================================================\n");
    }
}

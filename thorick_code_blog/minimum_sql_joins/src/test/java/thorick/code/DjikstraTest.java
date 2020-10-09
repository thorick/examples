package thorick.code;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.*;

import java.util.*;


public class DjikstraTest {
    static String tn = "unknown";
    @Test
    public void BasicTest() {
        tn = "BasicTest";
        Graph g = new Graph();
        SteinerEdge e01 = g.addEdgeToGraphByTableName("zero", "one", Join.JoinType.INNER, 1);
        PrimDjikstraNode n0 = g.getNode("zero");
        PrimDjikstraNode n1 = g.getNode("one");
        /*
        PrimDjikstraNode n0 = g.addNewNode("zero");
        PrimDjikstraNode n1 = g.addNewNode("one");
        Join j01 = new Join(n0, n1, 1);
        SteinerEdge e01 = new SteinerEdge(j01);
        g.addNewEdge(e01);
         */

        SteinerEdge e02 = g.addEdgeToGraphByTableName("zero", "two", Join.JoinType.INNER, 4);
        PrimDjikstraNode n2 = g.getNode("two");
        SteinerEdge e12 = g.addEdgeToGraphByTableName("one", "two", Join.JoinType.INNER, 2);
        SteinerEdge e13 = g.addEdgeToGraphByTableName("one", "three", Join.JoinType.INNER, 6);
        PrimDjikstraNode n3 = g.getNode("three");
        SteinerEdge e23 = g.addEdgeToGraphByTableName("two", "three", Join.JoinType.INNER, 3);

        Djikstra dj = new Djikstra(g);
        Djikstra.Result result = dj.shortestPathsFromSourceNode(n0, null);
        String djEdges = result.printDjikstraEdges();
        p("got result !\n"+djEdges);

        Set<SteinerEdge> resultEdgeSet = result.getDjikstraEdgeSet();
        Set<SteinerEdge> expectedEdgeSet = new HashSet<SteinerEdge>();
        expectedEdgeSet.add(e01);
        expectedEdgeSet.add(e12);
        expectedEdgeSet.add(e23);
        TestEval eval = checkResult(resultEdgeSet, expectedEdgeSet, g);
        assertTrue(eval.message, eval.passed);
    }

    // this is BasicTest but with join12 reversed to become join21 all else remains the same
    @Test
    public void BasicTestSingleReversedJoin() {
        tn = "BasicTestSingleReversedJoin";
        Graph g = new Graph();
        SteinerEdge e01 = g.addEdgeToGraphByTableName("zero", "one", Join.JoinType.INNER, 1);
        PrimDjikstraNode n0 = g.getNode("zero");
        PrimDjikstraNode n1 = g.getNode("one");
        SteinerEdge e02 = g.addEdgeToGraphByTableName("zero", "two", Join.JoinType.INNER, 4);
        PrimDjikstraNode n2 = g.getNode("two");
        SteinerEdge e21 = g.addEdgeToGraphByTableName("two", "one", Join.JoinType.INNER, 2);
        SteinerEdge e13 = g.addEdgeToGraphByTableName("one", "three", Join.JoinType.INNER, 6);
        PrimDjikstraNode n3 = g.getNode("three");
        SteinerEdge e23 = g.addEdgeToGraphByTableName("two", "three", Join.JoinType.INNER, 3);

        Djikstra dj = new Djikstra(g);
        Djikstra.Result result = dj.shortestPathsFromSourceNode(n0, null);
        String djEdges = result.printDjikstraEdges();
        p("got result !\n"+djEdges);

        Set<SteinerEdge> resultEdgeSet = result.getDjikstraEdgeSet();
        Set<SteinerEdge> expectedEdgeSet = new HashSet<SteinerEdge>();
        expectedEdgeSet.add(e01);
        expectedEdgeSet.add(e21);
        expectedEdgeSet.add(e23);
        TestEval eval = checkResult(resultEdgeSet, expectedEdgeSet, g);
        p("Expect a FAIL since we've reversed an edge from BASIC");
        p("    Test FAIL message: "+eval.message);
        assertFalse(eval.message, eval.passed);
    }

    @Test
    public void BasicTestCompleteManualBidirectional() {
        tn = "BasicTestCompleteManualBidirectional";
        Graph g = new Graph();
        PrimDjikstraNode n0 = g.addNewNode("zero");
        PrimDjikstraNode n1 = g.addNewNode("one");
        Join j01 = new Join(n0, n1, 1);
        SteinerEdge e01 = new SteinerEdge(j01);
        g.addNewEdge(e01);

        Join j10 = new Join(n1, n0, 1);
        SteinerEdge e10 = new SteinerEdge(j10);
        g.addNewEdge(e10);

        PrimDjikstraNode n2 = g.addNewNode("two");
        Join j02 = new Join(n0, n2, 4);
        SteinerEdge e02 = new SteinerEdge(j02);
        g.addNewEdge(e02);
        Join j12 = new Join(n1, n2, 2);
        SteinerEdge e12 = new SteinerEdge(j12);
        g.addNewEdge(e12);

        Join j20 = new Join(n2, n0, 4);
        SteinerEdge e20 = new SteinerEdge(j20);
        g.addNewEdge(e20);
        Join j21 = new Join(n2, n1, 2);
        SteinerEdge e21 = new SteinerEdge(j21);
        g.addNewEdge(e21);


        PrimDjikstraNode n3 = g.addNewNode("three");
        Join j13 = new Join(n1, n3, 6);
        SteinerEdge e13 = new SteinerEdge(j13);
        g.addNewEdge(e13);
        Join j23 = new Join(n2, n3, 3);
        SteinerEdge e23 = new SteinerEdge(j23);
        g.addNewEdge(e23);

        Join j31 = new Join(n3, n1, 6);
        SteinerEdge e31 = new SteinerEdge(j31);
        g.addNewEdge(e31);
        Join j32 = new Join(n3, n2, 3);
        SteinerEdge e32 = new SteinerEdge(j32);
        g.addNewEdge(e32);


        Djikstra dj = new Djikstra(g);
        Djikstra.Result result = dj.shortestPathsFromSourceNode(n0, null);
        String djEdges = result.printDjikstraEdges();
        p("got result !\n"+djEdges);

        Set<SteinerEdge> resultEdgeSet = result.getDjikstraEdgeSet();
        Set<SteinerEdge> expectedEdgeSet = new HashSet<SteinerEdge>();
        expectedEdgeSet.add(e01);
        expectedEdgeSet.add(e12);
        expectedEdgeSet.add(e23);
        TestEval eval = checkResult(resultEdgeSet, expectedEdgeSet, g);
        assertTrue(eval.message, eval.passed);
    }

    @Test
    public void BasicTestGraphAddBidirectional() {
        tn = "BasicTestGraphAddBidirectional";
        Graph g = new Graph();
        SteinerEdge e01 = g.addBidirectionalEdgeToGraphByTableName("zero", "one", Join.JoinType.INNER, 1);
        SteinerEdge e10 = g.getEdge("one", "zero");
        PrimDjikstraNode n0 = g.getNode("zero");
        PrimDjikstraNode n1 = g.getNode("one");
        SteinerEdge e02 = g.addBidirectionalEdgeToGraphByTableName("zero", "two", Join.JoinType.INNER, 4);
        SteinerEdge e20 = g.getEdge("two", "zero");
        PrimDjikstraNode n2 = g.getNode("two");
        SteinerEdge e12 = g.addBidirectionalEdgeToGraphByTableName("one", "two", Join.JoinType.INNER, 2);
        SteinerEdge e21 = g.getEdge("two", "one");
        SteinerEdge e13 = g.addBidirectionalEdgeToGraphByTableName("one", "three", Join.JoinType.INNER, 6);
        SteinerEdge e31 = g.getEdge("three", "one");
        PrimDjikstraNode n3 = g.getNode("three");
        SteinerEdge e23 = g.addBidirectionalEdgeToGraphByTableName("two", "three", Join.JoinType.INNER, 3);
        SteinerEdge e32 = g.getEdge("three", "two");

        Djikstra dj = new Djikstra(g);
        Djikstra.Result result = dj.shortestPathsFromSourceNode(n0, null);
        String djEdges = result.printDjikstraEdges();
        p("got result !\n"+djEdges);

        Set<SteinerEdge> resultEdgeSet = result.getDjikstraEdgeSet();
        Set<SteinerEdge> expectedEdgeSet = new HashSet<SteinerEdge>();
        expectedEdgeSet.add(e01);
        expectedEdgeSet.add(e12);
        expectedEdgeSet.add(e23);
        TestEval eval = checkResult(resultEdgeSet, expectedEdgeSet, g);
        assertTrue(eval.message, eval.passed);
    }

    @Test
    public void CSRitTest() {
        tn = "CSRitTest";
        Graph g = new Graph();
        SteinerEdge eAB = g.addBidirectionalEdgeToGraphByTableName("A", "B", Join.JoinType.INNER, 7);
        PrimDjikstraNode nA = g.getNode("A");
        SteinerEdge eBC = g.addBidirectionalEdgeToGraphByTableName("B", "C", Join.JoinType.INNER, 15);
        SteinerEdge eCD = g.addBidirectionalEdgeToGraphByTableName("C", "D", Join.JoinType.INNER, 4);
        SteinerEdge eDE = g.addBidirectionalEdgeToGraphByTableName("D", "E", Join.JoinType.INNER, 9);
        SteinerEdge eAE = g.addBidirectionalEdgeToGraphByTableName("A", "E", Join.JoinType.INNER, 14);
        SteinerEdge eAF = g.addBidirectionalEdgeToGraphByTableName("A", "F", Join.JoinType.INNER, 9);
        SteinerEdge eFE = g.addBidirectionalEdgeToGraphByTableName("F", "E", Join.JoinType.INNER, 2);
        SteinerEdge eBF = g.addBidirectionalEdgeToGraphByTableName("B", "F", Join.JoinType.INNER, 8);
        SteinerEdge eFC = g.addBidirectionalEdgeToGraphByTableName("F", "C", Join.JoinType.INNER, 6);

        Djikstra dj = new Djikstra(g);
        Djikstra.Result result = dj.shortestPathsFromSourceNode(nA, null);
        String djEdges = result.printDjikstraEdges();
        p("got result !\n"+djEdges);

        Set<SteinerEdge> resultEdgeSet = result.getDjikstraEdgeSet();
        Set<SteinerEdge> expectedEdgeSet = new HashSet<SteinerEdge>();
        expectedEdgeSet.add(eAB);
        expectedEdgeSet.add(eAF);
        expectedEdgeSet.add(eFC);
        expectedEdgeSet.add(eCD);
        expectedEdgeSet.add(eFE);
        TestEval eval = checkResult(resultEdgeSet, expectedEdgeSet, g);

        assertTrue(eval.message, eval.passed);
    }

    @Test
    public void GeekTest() {
        tn = "GeekTest";
        Graph g = new Graph();
        SteinerEdge e01 = g.addBidirectionalEdgeToGraphByTableName("0", "1", Join.JoinType.INNER, 4);
        SteinerEdge e12 = g.addBidirectionalEdgeToGraphByTableName("1", "2", Join.JoinType.INNER, 8);
        SteinerEdge e23 = g.addBidirectionalEdgeToGraphByTableName("2", "3", Join.JoinType.INNER, 7);
        SteinerEdge e34 = g.addBidirectionalEdgeToGraphByTableName("3", "4", Join.JoinType.INNER, 9);
        SteinerEdge e45 = g.addBidirectionalEdgeToGraphByTableName("4", "5", Join.JoinType.INNER, 10);
        SteinerEdge e35 = g.addBidirectionalEdgeToGraphByTableName("3", "5", Join.JoinType.INNER, 14);
        SteinerEdge e25 = g.addBidirectionalEdgeToGraphByTableName("2", "5", Join.JoinType.INNER, 4);
        SteinerEdge e56 = g.addBidirectionalEdgeToGraphByTableName("5", "6", Join.JoinType.INNER, 2);
        SteinerEdge e67 = g.addBidirectionalEdgeToGraphByTableName("6", "7", Join.JoinType.INNER, 1);
        SteinerEdge e70 = g.addBidirectionalEdgeToGraphByTableName("7", "0", Join.JoinType.INNER, 8);
        SteinerEdge e71 = g.addBidirectionalEdgeToGraphByTableName("7", "1", Join.JoinType.INNER, 11);
        SteinerEdge e78 = g.addBidirectionalEdgeToGraphByTableName("7", "8", Join.JoinType.INNER, 7);
        SteinerEdge e68 = g.addBidirectionalEdgeToGraphByTableName("6", "8", Join.JoinType.INNER, 6);
        SteinerEdge e28 = g.addBidirectionalEdgeToGraphByTableName("2", "8", Join.JoinType.INNER, 2);

        Djikstra dj = new Djikstra(g);
        Set<String> destNodes = new HashSet<String>();
        destNodes.add("8");
        destNodes.add("5");
        PrimDjikstraNode n0 = g.getNode("0");
        Djikstra.Result result = dj.shortestPathsFromSourceNode(n0, destNodes);
        String djEdges = result.printDjikstraEdges();
        p("got result !\n"+djEdges);
        p("shortest paths map: \n"+result.printShortestPathsMap());

        Set<SteinerEdge> resultEdgeSet = result.getDjikstraEdgeSet();
        Set<SteinerEdge> expectedEdgeSet = new HashSet<SteinerEdge>();
        SteinerEdge e54 = g.getEdge("5", "4");
        SteinerEdge e65 = g.getEdge("6", "5");
        SteinerEdge e76 = g.getEdge("7", "6");
        SteinerEdge e07 = g.getEdge("0", "7");
        expectedEdgeSet.add(e01);
        expectedEdgeSet.add(e12);
        expectedEdgeSet.add(e23);
        expectedEdgeSet.add(e54);
        expectedEdgeSet.add(e65);
        expectedEdgeSet.add(e76);
        expectedEdgeSet.add(e07);
        expectedEdgeSet.add(e28);
        Map<String, List<SteinerEdge>> expectedShortestPathMap =
                new HashMap<String, List<SteinerEdge>>();
        String key = "5";
        List<SteinerEdge> list = new ArrayList<SteinerEdge>();
        list.add(e07);
        list.add(e76);
        list.add(e65);
        expectedShortestPathMap.put(key, list);
        key = "8";
        list = new ArrayList<SteinerEdge>();
        list.add(e01);
        list.add(e12);
        list.add(e28);
        expectedShortestPathMap.put(key, list);
        TestEval eval = checkResult(resultEdgeSet, expectedEdgeSet, result.getShortestPathsMap(), expectedShortestPathMap, g);

        assertTrue(eval.message, eval.passed);
    }

    static TestEval checkResult(Set<SteinerEdge> result, Set<SteinerEdge> expected,
                                Graph g) {
        return checkResult(result, expected, null, null, g);
    }
    static TestEval checkResult(Set<SteinerEdge> result, Set<SteinerEdge> expected,
                                Map<String, List<SteinerEdge>> resultShortestPathMap,
                                Map<String, List<SteinerEdge>> expectedShortestPathMap, Graph g) {
        TestEval eval = new TestEval();
        if (result == null || expected == null)  {
            eval.passed = false;
            eval.message = "at least one of result or expected is null or empty";
            return eval;
        }
        if (result.size() != expected.size()) {
            eval.passed = false;
            eval.message = "result size="+result.size()+" !=  expected size="+expected.size();
            return eval;
        }

        StringBuilder sb = null;
        for (SteinerEdge exp : expected) {
           if (!result.contains(exp)) {
               eval.passed = false;
               if (sb == null) {
                   sb = new StringBuilder();
               }
               sb.append("expected to see Edge: "+exp.toString(g)+" in result but it is missing\n");
           }
        }
        if (expectedShortestPathMap != null) {
            Set<String> resultKeySet = resultShortestPathMap.keySet();
            for (String key : expectedShortestPathMap.keySet()) {
                if (!resultKeySet.contains(key)) {
                    sb.append("result shortestPathMap is missing key='"+key+"'");
                    eval.passed = false;
                    continue;
                }
                List<SteinerEdge> resultList = resultShortestPathMap.get(key);
                if (resultList == null) {
                    sb.append("result shortestPathMap is missing List for key='"+key+"'");
                    eval.passed = false;
                    continue;
                }
                List<SteinerEdge> list = expectedShortestPathMap.get(key);
                for (SteinerEdge edge : list) {
                    if (!resultList.contains(edge)) {
                        sb.append("result shortestPath List for key='"+key+"' is missing edge='"+edge.toString(g)+"'");
                        eval.passed = false;
                        continue;
                    }
                }
            }
        }
        if (!eval.passed)
            eval.message = sb.toString();
        return eval;
    }
    static void p(String s) {
        System.err.println(tn+": "+s);
    }

    static class TestEval {
        public boolean passed = true;
        public String message = "TestCase passed";
    }
}

package thorick.code;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.*;

import java.util.*;


public class MSTTest {
    static String tn = "unknown";
    @Test
    public void BasicTest() {
        tn = "BasicTest";
        Graph g = new Graph();
        SteinerEdge eAB = g.addEdgeToGraphByTableName("A", "B", Join.JoinType.LEFT_OUTER, 1);
        SteinerEdge eBC = g.addEdgeToGraphByTableName("B", "C");
        SteinerEdge eCD = g.addEdgeToGraphByTableName("C", "D");
        SteinerEdge eAD = g.addEdgeToGraphByTableName("A", "D");
        SteinerEdge eDE = g.addEdgeToGraphByTableName("D", "E");
        SteinerEdge eEF = g.addEdgeToGraphByTableName("E", "F");
        SteinerEdge eAG = g.addEdgeToGraphByTableName("A", "G");
        SteinerEdge eGD = g.addEdgeToGraphByTableName("G", "D");

        Set<String> targetNodes = new HashSet<String>();
        targetNodes.add("A");
        targetNodes.add("B");
        targetNodes.add("F");
        MinimumSQLJoinCalculation gen = new MinimumSQLJoinCalculation();

        Set<SteinerEdge> expected = new HashSet<SteinerEdge>();
        expected.add(eAB);

        List<SteinerEdge> expectedList = new ArrayList<SteinerEdge>();
        expectedList.add(eAD);
        expectedList.add(eDE);
        expectedList.add(eEF);
        SteinerEdge expectedCompositeEdge = new SteinerEdge(expectedList, false);
        expected.add(expectedCompositeEdge);

        Graph resultGraph = gen.computeClosureMSTGraph(g, targetNodes);
        Set<SteinerEdge> result = resultGraph.getClosureGraphEdgeSet();
        TestEval eval = checkResult(result, expected, g);

        assertTrue(eval.message, eval.passed == true);
    }

    static TestEval checkResult(Set<SteinerEdge> result, Set<SteinerEdge> expected, Graph g) {
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
            p("checking for: "+exp.toString(g));
            boolean found = false;
            for (SteinerEdge res : result) {
                if (res.leftNum == exp.leftNum) {
                    if (res.rightNum == exp.rightNum) {
                        found = true;
                    }
                }
            }
            if (!found) {
                eval.passed = false;
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append("expected to see Edge: "+exp.toString(g)+" in result but it is missing\n");
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

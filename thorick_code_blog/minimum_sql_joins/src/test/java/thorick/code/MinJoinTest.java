package thorick.code;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.*;

import java.util.*;


public class MinJoinTest {
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
        expected.add(eAD);
        expected.add(eDE);
        expected.add(eEF);

        Set<Join> resultJoinSet = gen.computeMinimumSQLJoins(g, targetNodes);
        TestEval eval = checkResult(resultJoinSet, expected, g);

        assertTrue(eval.message, eval.passed == true);
    }

    static TestEval checkResult(Set<Join> result, Set<SteinerEdge> expected, Graph g) {
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
            Join join = exp.getJoin();
            p("checking for: "+exp.toString(g));
            boolean found = false;
            for (Join res : result) {
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
                sb.append("expected to see Join: "+join.toString(g)+" in result but it is missing\n");
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

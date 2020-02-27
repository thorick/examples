package algorithms.dynamicProgramming;

import java.util.HashSet;
import java.util.Set;

/**
 * This is an implementation of the dynamic programming example from Stanford Coursera
 *
 * “G:\b\_computerScience\classes\algorithms_stanford_coursera2\problemsNotes\Coursera Algorithms 2-13 Weighted Independent Set  Path Graphs  Algo 2.odt”
 *
 *  you have a linear graph.
 *  each node contains a positive weight.
 *  you must choose the set of nodes from the graph that produces a maximum weight.
 *  no two of the selected nodes are allowed to be adjacent to each other
 *  thus you must skip at least every other node in your set selection.
 *
 *  Dynamic Programming Algorithm
 *
 *   You start from the bottom and keep either including w(i) into G''  or   NOT including w(i) in G',
 *   where G'' is (i-2) and  G' is (i-1)  per the case 1 and 2 described earlier.
 *   Because of the truncation of the complete set S arguments, you know that as long as you pick the MAX of these 2,
 *   you are picking the correct vertex 'i' (or excluding it) from what the MAX value MUST be (!).
 *
 *
 */
public class weightedIndependentSet {

    static final int START_INDEX = 2;
    static int[] graph = new int[]{5, 12, 6, 1, 100, 70, 2, 500, 700, 1000};

    //  note: that the result of the computation is an array of the computed values
    //  for each iteration of candidate node.
    //  being the results of each iteration along the graph nodes this facilitates the quick lookup of
    //  the values of previously computed subproblems which we use to evaluate the optimal choice
    //  between alternatives, which is a hallmark of DP
    //
    static int[] resultWeights = new int[graph.length];
    static Set<Integer> chosenNodeSet = new HashSet<>();

    static void computeMax() {
        // set initial values for DP
        int index = START_INDEX;
        for (int i = 0; i<index; i++) {
            resultWeights[i] = graph[i];
        }

        //
        // now do the DP algo
        // trying each possibility choose what yields the max weight:
        //    optionA:
        //    do not include node i, take value at node i-1
        //  or
        //    optionB:
        //    include node i  and  node i-2
        //
        //
        for (int i = index; i < graph.length; i++) {
            int optionA = resultWeights[i-1];
            int optionB = resultWeights[i-2] + graph[i];
            if (optionA > optionB) {
                resultWeights[i] = optionA;
            }
            else {
                resultWeights[i] = optionB;
            }
        }
    }

    static void computeChosenNodeList() {
        Set<Integer> prevSet = new HashSet<>();
        Set<Integer> prevPrevSet = new HashSet<>();
        Set<Integer> tempSet = new HashSet<>();

        // prime the first entry for graph[START_INDEX]
        if (resultWeights[START_INDEX] == (graph[START_INDEX-1])) {
            // we chose node 1
            prevPrevSet.add(START_INDEX-1);
            chosenNodeSet.add(START_INDEX-1);
        }
        else {
            // we chose node 0 and node 2
            chosenNodeSet.add(0);
            prevSet.add(0);
            chosenNodeSet.add(START_INDEX);
            prevSet.add(START_INDEX);
        }
        for (int i = START_INDEX+1; i<graph.length; i++) {
            // determine whether we decided to include node[i] into the maximal set
            // if we include node[i]
            p("\nstart  node: "+i+"  chosen node set: "+ps(chosenNodeSet));
            if (resultWeights[i] == (resultWeights[i-2] + graph[i])) {
                // any nodes added for [i-1] will be in the set prevSet, remove it
                p("      add node "+i+".   remove all [i-1] added nodes: "+ps(prevSet));
                chosenNodeSet.removeAll(prevSet);

                // add the weight of the current added node
                chosenNodeSet.add(i);

                // and restore any nodes added for [i-2]
                tempSet.clear();
                for (Integer ppi : prevPrevSet) {
                    if (!chosenNodeSet.contains(ppi)) {
                        tempSet.add(ppi);
                    }
                }
                p("      save off and add back all newly added [i-2] nodes: "+ps(tempSet));
                chosenNodeSet.addAll(tempSet);
                p("      chosen nodes now: "+ps(chosenNodeSet));
                // now set up the node change history for the next iteration
                prevPrevSet.clear();
                prevPrevSet.addAll(prevSet);
                p("      set next [i-2] added nodes: "+ps(prevPrevSet));
                prevSet.clear();
                prevSet.add(i);
                prevSet.addAll(tempSet);     // any restored nodes newly added for [i-2]
                p("      set next [i-1] added nodes: "+ps(prevSet));
            }
            else {
                // we add no new nodes and remove no previously added nodes
                prevPrevSet.clear();
                prevPrevSet.addAll(prevSet);
                prevSet.clear();
                p("      skip node "+i+".  clear added [i-1] nodes.  set [i-2] added nodes: "+ps(prevPrevSet));
            }
            p("total weight at "+i+": "+resultWeights[i]);
        }

    }

    static void p(String s) {
        System.err.println(s);
    }
    static <T> String ps(Set<T> set) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (T e : set) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(e.toString());
        }
        sb.append("}");
        return sb.toString();
    }
    public static void main(String[] args) {
        computeMax();
        p("Max value is: "+resultWeights[graph.length-1]);
        computeChosenNodeList();
        p("\n");
        p("DONE:  final chosen node set: "+ps(chosenNodeSet));
    }

}

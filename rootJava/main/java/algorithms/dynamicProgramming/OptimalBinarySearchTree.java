package algorithms.dynamicProgramming;

import org.apache.log4j.Logger;

/**
 * Binary Search Tree in which each Node is augmented with information about relatively how
 * frequently the node is accessed.
 * <p>
 * There are numNodes  ordered nodes
 * <p>
 * There is a subproblem C(i,j)  for  0 <= i <= j <= numNodes
 * C(i,j)
 *
 * For each subproblem we want to find which configuration yields the minimum cost for which node that is placed at the root
 * That configuration is chosen as the value to assign to C(i.j)
 *
 * So one at a time we choose a candidate root node starting from the left side of the interval at i up until the right
 * side of the interval at j.
 *
 * Each choice of root divides the the subgraph into 2 subtrees each of which have been evaluated in the same way.
 *
 * The candidate value is:   Sum(frequency of each node i to j) + C(i, r-1) + C(r+1, j)
 *
 * The Sum(frequency of each node i to j)
 *     The Sum of all probabilities is how many times the root node will be visited while seeking out each node at their specified frequency.
 *     Likewise for each subtree which happens to be the optimal subtree we will get how many times that subroot is accessed for each node in  its subtree.
 *
 * <p>
 * In order for DP to work all of the smallest subproblems must be done first.
 * This is so that the shortest subtree costs have already been computed and are available for future C(i,j) subproblems.
 * So this is a traversal by SIZE not strictly by the value of the indexes i or j.
 * It is the size of subtrees spanning the indexes i and j in order.
 * The final answer is the cost of the complete set of nodes numNodes.
 * <p>
 * <p>
 * We'll load an ordered set of nodes with integer values, each will have a search count associated with it, unscaled for simplicity
 * <p>
 * each node takes 2 array position:   0 value  1 frequency
 * so node pos is index * 2    frequence pos is  (index * 2) + 1
 * <p>
 * each tree entry takes 2 array positions for children:  0  LEFT  1 RIGHT
 * node LEFT CHILD pos  is index * 2  node RIGHT CHILD pos is  (index * 2) + 1
 *
 *
 * Here is sample output from running testcase0  nice !
 *
 * =====================================
 * LOAD TEST 0
 *
 *   Loaded nodes:
 * (0)  [val=0, freq=1]
 * (1)  [val=1, freq=4]
 * (2)  [val=2, freq=10]
 * (3)  [val=3, freq=1]
 * (4)  [val=4, freq=7]
 * (5)  [val=5, freq=3]
 *
 * -----------------------------
 *
 * ----------------------------------
 * final main value matrix
 * 5  [47] [44] [36] [15] [13] [ 3]
 *    [38] [35] [27] [ 9] [ 7] [ 0]
 *    [23] [20] [12] [ 1] [ 0] [ 0]
 *    [21] [18] [10] [ 0] [ 0] [ 0]
 *    [ 6] [ 4] [ 0] [ 0] [ 0] [ 0]
 * 0  [ 1] [ 0] [ 0] [ 0] [ 0] [ 0]
 *      0    1    2    3    4    5
 *
 * ----------------------------------
 *
 * ----------------------------------
 * final chosen root matrix
 * 5  [2] [2] [2] [4] [4] [0]
 *    [2] [2] [2] [4] [0] [0]
 *    [2] [2] [2] [0] [0] [0]
 *    [2] [2] [0] [0] [0] [0]
 *    [1] [0] [0] [0] [0] [0]
 * 0  [0] [0] [0] [0] [0] [0]
 *     0   1   2   3   4   5
 *
 * ----------------------------------
 *
 * ----------------------------------
 * final tree graph adjacency matrix where root node is 2
 *    [x] [x] [4] [x] [5] [x]
 * 0  [x] [0] [1] [x] [3] [x]
 *     0   1   2   3   4   5
 *
 * ----------------------------------
 * =====================================
 */
public class OptimalBinarySearchTree {
    private static Logger log =
            Logger.getLogger(OptimalBinarySearchTree.class);

    int numNodes;
    int[] orderedNodes;
    int[] tree;

    // the memoized min value of C(i,i+s)  corresponds to selected root r of the partitioned list i to i+s
    int[][] min_results;
    int[][] chosen_r;
    int[][] graphAdjList;    // result tree

    int getMinVal() {
        return min_results[0][numNodes - 1];
    }

    int getTopRoot() {
        return chosen_r[0][numNodes - 1];
    }

    //
    //   This is trying out all of the positions that the root of a proposed configuration can take
    //   Where k is the trial root position
    //
    //   C(i,j) = Min(k=i to j) {  Sum(P(i to j) + C(i, k-1) + C(k+1, j) }
    //
    //
    //
    void compute() {
        min_results = new int[numNodes][numNodes];
        chosen_r = new int[numNodes][numNodes];
        for (int i = 0; i < numNodes; i++) {
            min_results[i][i] = sumFreq(i, i);    // frequency of this single node is the min cost
        }

        //   outer loop controls the size of the subtree that we are computing
        for (int s = 2; s <= numNodes; s++) {
            if (log.isDebugEnabled()) {
                log.debug("total subset size=" + s);
            }
            // inner loop controls the index span of the sublist of contiguous indices
            for (int start = 0; start <= numNodes - s; start++) {
                doMinCsubProblem(s, start);
            }
        }

        String finalMatrix = printPadded2DIntArray(min_results);
        p("----------------------------------\nfinal main value matrix\n" + finalMatrix);
        p("----------------------------------\n");

        String rootMatrix = printPadded2DIntArray(chosen_r);
        p("----------------------------------\nfinal chosen root matrix\n" + rootMatrix);
        p("----------------------------------\n");

        int[][] resultTree = constructResultGraph();
        String s = printPadded2DIntArray(resultTree);
        p("----------------------------------\nfinal tree graph adjacency matrix where root node is " + chosen_r[0][numNodes - 1] + "\n" + s);
        p("----------------------------------");
        p("=====================================\n\n\n");

    }

    //
    //   this solves the subproblem C(i,j) =  C(startIndex, startIndex+size)
    //
    void doMinCsubProblem(int size, int startIndex) {
        int minVal = Integer.MAX_VALUE;
        int minRootIndex = -1;
        int leftIndex = startIndex;
        int rightIndex = startIndex + size - 1;
        int sumFreq = sumFreq(leftIndex, rightIndex);
        if (log.isDebugEnabled()) {
            log.debug("do leftIndex=" + leftIndex + ", rightIndex=" + rightIndex);
        }
        // try every position for the root along the contiguous index range
        for (int r = leftIndex; r <= rightIndex; r++) {
            int tempVal = sumFreq;
            if (r == leftIndex) {
                // there is only a right side tree
                tempVal += min_results[leftIndex + 1][rightIndex];
            } else if (r == rightIndex) {
                // there is only a left side tree
                tempVal += min_results[leftIndex][rightIndex - 1];
            } else {
                tempVal += min_results[leftIndex][r - 1];
                tempVal += min_results[r + 1][rightIndex];
            }
            if (tempVal < minVal) {
                minVal = tempVal;
                minRootIndex = r;
            }
        }
        min_results[leftIndex][rightIndex] = minVal;
        chosen_r[leftIndex][rightIndex] = minRootIndex;   // save off the root index
    }

    int sumFreq(int start, int end) {
        int total = 0;
        for (int i = start; i <= end; i++) {
            total += orderedNodes[(i * 2) + 1];
        }
        return total;
    }

    int[][] constructResultGraph() {
        graphAdjList = new int[numNodes][2];      // a binary search tree node can have at most 2 children
        for (int i=0; i<numNodes; i++) {
            graphAdjList[i][0] = -1;
            graphAdjList[i][1] = -1;
        }
        boolean[] visited = new boolean[numNodes];    // track nodes that we have already picked off
        boolean done = false;
        int[] parent_ij = new int[]{-1, -1};
        int[] currRoot_ij = new int[]{0, numNodes - 1};   // top node is the entry for the entire list   (i,j) = 0, numNodes-1
        do {
            done = processRoot(graphAdjList, visited, currRoot_ij, parent_ij);
        } while (!done);
        return graphAdjList;
    }

    boolean processRoot(int[][] graphAdjList, boolean[] visited, int[] currRoot_ij, int[] parent_ij) {
        int currNode = chosen_r[currRoot_ij[0]][currRoot_ij[1]];  // this is us
        // if we're not the top of the tree, then register us with our parent
        if (parent_ij[0] >= 0) {
            int parentNode = chosen_r[parent_ij[0]][parent_ij[1]];    // this is the computed parent
            if (currNode < parentNode) {
                // left
                graphAdjList[parentNode][0] = currNode;
            } else {
                // right
                graphAdjList[parentNode][1] = currNode;
            }
        }
        visited[currNode] = true;   // stake our claim

        // now handle our left subtree if there is one
        if (currNode > 0) {
            // find the breadth of our left subtree go left until we've reached an already visited node
            int nextNode = currNode - 1;
            int count = 0;
            while (nextNode >= 0) {
                if (visited[nextNode]) {
                    break;    // done
                }
                count++;
                nextNode--;
            }
            // now determine our subtree indexes
            int rightIndex = currNode - 1;
            int leftIndex = currNode - count;
            int size = rightIndex - leftIndex;
            if (size == 0) {
                // we've reached the leaf level
                visited[leftIndex] = true;
                graphAdjList[currNode][0] = leftIndex;
            } else if (size > 0) {
                // process the left subtree
                int[] nextRoot_ij = new int[]{leftIndex, rightIndex};
                processRoot(graphAdjList, visited, nextRoot_ij, currRoot_ij);
            }
        }

        // now handle our right subtree if there is one
        if (currNode < numNodes - 1) {
            // find the breadth of our right subtree go right until we've reached an already visited node
            int nextNode = currNode + 1;
            int count = 0;
            while (nextNode < numNodes) {
                if (visited[nextNode]) {
                    break;    // done
                }
                count++;
                nextNode++;
            }
            // now determine our subtree indexes
            int leftIndex = currNode + 1;
            int rightIndex = currNode + count;
            int size = rightIndex - leftIndex;
            if (size == 0) {
                // we've reached the leaf level
                visited[rightIndex] = true;
                graphAdjList[currNode][1] = rightIndex;
            } else if (size > 0) {
                // process the right subtree
                int[] nextRoot_ij = new int[]{leftIndex, rightIndex};
                processRoot(graphAdjList, visited, nextRoot_ij, currRoot_ij);
            }
        }
        return true;     // we are done
    }

    void loadTest0() {
        orderedNodes = new int[]{
                0, 1,
                1, 4,
                2, 10,
                3, 1,
                4, 7,
                5, 3
        };
        numNodes = orderedNodes.length / 2;

        String s = printNodeArray(orderedNodes);
        p("=====================================\nLOAD TEST 0\n");
        p("  Loaded nodes: \n" + s);
        p("-----------------------------\n");
    }

    static String printNodeArray(int[] n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n.length / 2; i++) {
            int valIndex = i * 2;
            int freqIndex = (i * 2) + 1;
            sb.append("(" + i + ")  [val=" + n[valIndex] + ", freq=" + n[freqIndex] + "]\n");
        }
        return sb.toString();
    }

    static void p(String s) {
        System.err.println(s);
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

    public static void main(String[] args) {
        OptimalBinarySearchTree prog = new OptimalBinarySearchTree();
        prog.loadTest0();
        prog.compute();
    }
}

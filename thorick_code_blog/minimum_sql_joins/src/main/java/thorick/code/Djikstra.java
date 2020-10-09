package thorick.code;

import java.util.*;

//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;

/**
 * Djikstra using an Array Based Heap to find Minimum Cost Nodes quickly.
 *
 * The input Graph remains untouched and is used as the model from which to run Djikstra from.
 *
 * Output is a new Graph consisting of the shortest paths from the given originalSource node.
 *
 * Additionally:
 *    Since this Djikstra impl is targeted to the Minimum SQL Join problem it keeps
 *    a Set of all of the Minimum Paths from the originalSource node to a Set of input destination Nodes.
 *    This is an example of how a standard algorithm:  Djikstra, can be tailored for efficient use to solve a specific problem.
 *
 *    The Inner Class Result  holds this information
 *
 *
 */
class Djikstra {
    //protected final Logger log = LogManager.getLogger(Djikstra.class);
    final Graph originalGraph;
    PrimDjikstraNode[] nodeArray;
    DjikstraHeap djikstraHeap;
    int heapSize = 100;

    public Djikstra(Graph g) {
        this.originalGraph = g;
        int numNodes = g.getNumNodes();
        nodeArray = new PrimDjikstraNode[numNodes];
        if (heapSize < numNodes * 2) {
            heapSize = numNodes * 2;
        }
        djikstraHeap = new DjikstraHeap(new PrimDjikstraNode(0), heapSize, originalGraph);
    }


    public Result shortestPathsFromSourceNode(PrimDjikstraNode originalSource, Set destNodes) {
        // logging
        //log.debug("\n\n Djikstra: find shortest paths from " + getTableAlias(originalSource) + " - " + getTableAlias(originalSource));

        resetForDjikstra();
        int currOrdinal = originalSource.getOrdinal();
        PrimDjikstraNode currNode = getOrCloneNodeByOrdinal(currOrdinal);
        Result result = new Result(currNode, originalGraph);
        currNode.djIsExplored = true;
        int currNodeExploredValue = 0;
        currNode.setDjikstraExploredValue(currNodeExploredValue);
        result.setDjikstraTreeRoot(currNode);
        while (currNode != null) {
            //log.debug("------  node: " + getTableAlias(currNode) + "  " + currNode.toString(originalGraph));
            currNodeExploredValue = currNode.getDjikstraExploredValue();
            Iterator<SteinerEdge> edgeIterator = currNode.getSteinerEdgeIterator();

            // examine all candidate edges from current node
            // replace with an edge from currNode if that results in a lower tentative path cost
            while (edgeIterator.hasNext()) {
                SteinerEdge trialEdge = edgeIterator.next();
                int rightOrdinal = trialEdge.getRightNum();
                PrimDjikstraNode trialNode = getOrCloneNodeByOrdinal(rightOrdinal);
                if (trialNode.isDjikstraExplored() == true) {
                    continue;       // this is a back edge that we have already selected
                }

                int trialEdgeDist = trialEdge.getWeight();
                int trialDist = currNodeExploredValue + trialEdgeDist;
                //log.debug("-- trialEdge: " + ", trialDist=" + trialDist + ", " + trialEdge.toString(originalGraph) +
                //       "trialNode=" + getTableAlias(trialNode) + ", currDist=" + trialNode.getDjikstraCandidateValue());

                boolean adjustedDist = false;
                if (trialDist < trialNode.getDjikstraCandidateValue()) {
                    // this edge from the current source node
                    // yields a lower tentative value than previously found, so update the heap selector
                    adjustedDist = true;
                    trialNode.setDjikstraCandidateValue(trialDist);
                    trialNode.setDjikstraCandidateEdge(trialEdge);
                    //log.debug("-- trialEdge: " + ", trialDist=" + trialDist + ", " + trialEdge.toString(originalGraph) +
                    //        "  ACCEPT(" + getTableAlias(trialNode) + ")");
                }

                // trial node-edge to heap
                // keeping the heap position in the node saves us from having to maintain
                // a separate external way of knowing whether the node is in the heap or not
                if (!trialNode.isInHeap()) {
                    int index = djikstraHeap.insert(trialNode);
                    //log.debug("-- trialEdge: " + trialEdge.toString(originalGraph) + "  heap INSERT index=" + index);
                } else {
                    if (adjustedDist) {
                        int index = djikstraHeap.updateNode(trialNode);
                        //log.debug("-- trialEdge: " + trialEdge.toString(originalGraph) + "  heap REPOSITION index=" + index);
                    }
                }
                //    String heapDump = djikstraHeap.printHeap(true);
                //log.debug("-----  after heap access heap:\n      " + heapDump);
            }   // next neighbor of currNode
            // we're done with currNode, so we need to pull any remaining node out of the selection heap
            // now pull the smallest distance edge dest node off of the min heap
            // this is our next edge that is now added to our Tree
            // the head node of this edge is also the next unexplored vertex that we will examine next
            // note the next unexplored vertex contributes it's edges to the candidate edge pool
            // and we choose from the ENTIRE pool of edges to the unconnected set and choose
            // the lowest cost path from the set of edges to a node in the unconnected set
            // this is not necessarily an edge from the vertex that we selected in the prior step
            // thus the heap of edges remains (culled for any edges that have since become within the selected set of vertices)
            PrimDjikstraNode minNode = findSmallestCandidateNode();
            currNode = minNode;
            if (minNode != null) {
                //    String heapDump = djikstraHeap.printHeap(true);
                //log.debug("\n\n        ===============  after heap access REMOVE of " + getTableAlias(minNode) + " heap:\n      " + heapDump);
                SteinerEdge minEdge = minNode.getDjikstraCandidateEdge();
                minNode.setDjikstraExploredValue(minNode.getDjikstraCandidateValue());
                minNode.setDjikstraEdgeToThisNode(minEdge);

                int minNodeExploredValue = minNode.getDjikstraExploredValue();
                String minNodeTableAliasId = getTableAlias(minNode.getOrdinal());
                minNode.djIsExplored = true;
                minNode.djExploredValue = minNodeExploredValue;
                result.addToDjikstraTree(minNode);
                //log.debug("minNodeTable=" + getTableAlias(minNode) + ", minNode=" + minNode.toString(originalGraph) + ", minEdge=" +
                //            minEdge.toString(originalGraph) + ", added result.addToDjikstraTree()");

                ///////////////////////////////////////////////////////////////
                //
                // piggy back  finding multiple shortest paths from single source here.
                //
                //
                // check if adding this edge gives us any intermediate shortest paths from the original source to the currNode
                if (destNodes != null && destNodes.contains(minNodeTableAliasId)) {
                    // backtrack to get the joinIsReversed path back to the source node
                    // these are the edges that form the shortest path from the source node to minNode
                    ArrayList<SteinerEdge> reversedEdges = new ArrayList<SteinerEdge>();
                    PrimDjikstraNode n = minNode;
                    SteinerEdge e = minNode.getDjikstraEdgeToThisNode();
                    while (e != null) {
                        reversedEdges.add(e);
                        int nextOrdinal = e.getLeftNum();
                        n = getOrCloneNodeByOrdinal(nextOrdinal);
                        if (n != null) {
                            e = n.getDjikstraEdgeToThisNode();
                        } else {
                            e = null;
                        }
                    }
                    // set in forwards order and save
                    int size = reversedEdges.size();
                    ArrayList<SteinerEdge> edges = new ArrayList<SteinerEdge>(size);
                    for (int i = 0; i < size; i++) {
                        SteinerEdge edge = reversedEdges.get((size - 1) - i);
                        edges.add(i, edge);
                    }
                    result.addShortestPathTo(minNodeTableAliasId, edges);
                    result.addShortestPathTo(minNode.getOrdinal(), edges);
                    //log.debug("Added to Djikstra Tree: " + minEdge);
                }
            }
        }
        return result;
    }

    private PrimDjikstraNode getOrCloneNodeByOrdinal(int ordinal) {
        if (ordinal < 0) return null;
        PrimDjikstraNode theNode = nodeArray[ordinal];
        if (theNode != null) {
            return theNode;
        }
        PrimDjikstraNode orig = originalGraph.getNode(ordinal);
        theNode = (PrimDjikstraNode) orig.clone();
        theNode.initForDjikstra();
        nodeArray[ordinal] = theNode;
        return theNode;
    }

    private PrimDjikstraNode findSmallestCandidateNode() {
        PrimDjikstraNode n = djikstraHeap.remove();
        if (n == null) return null;   // no more left
        n.setHeapPosition(0);
        return n;
    }

    private void resetForDjikstra() {
        int numNodes = originalGraph.getNumNodes();
        for (int i = 0; i < numNodes; i++) {
            PrimDjikstraNode n = nodeArray[i];
            if (n != null) {
                n.initForDjikstra();
            }
        }
        djikstraHeap.reset();
    }

    private String getTableAlias(int ordinal) {
        String alias = originalGraph.tableNumToNodeName[ordinal];
        if (alias == null || alias.length() <= 0) {
            alias = "UNKNOWN";
        }
        return alias;
    }

    private String getTableAlias(PrimDjikstraNode n) {
        String alias = originalGraph.tableNumToNodeName[n.getOrdinal()];
        if (alias == null || alias.length() <= 0) {
            alias = "UNKNOWN";
        }
        return alias;
    }

    public class Result {
        Graph originalGraph;
        PrimDjikstraNode source;
        PrimDjikstraNode djikstraTree;         // djikstraTree is a separate copy of a tree
        Map<String, PrimDjikstraNode> djikstraNodeMap;

        Map<String, List<SteinerEdge>> shortestPathsToDestNodes;
        Map<Integer, PrimDjikstraNode> djikstraOrdinalNodeMap;
        Map<Integer, List<SteinerEdge>> djikstraDestOrdinalShortestPathsMap;

        // we assume that the input source is a clone and not from the original graph
        Result(PrimDjikstraNode clonedSource, Graph g) {
            this.source = clonedSource;
            this.originalGraph = g;
            djikstraNodeMap = new HashMap<String, PrimDjikstraNode>();
            djikstraOrdinalNodeMap = new HashMap<Integer, PrimDjikstraNode>();
        }

        void addShortestPathTo(String destTableAlias, List<SteinerEdge> list) {
            if (shortestPathsToDestNodes == null) {
                shortestPathsToDestNodes = new HashMap<String, List<SteinerEdge>>();
            }
            shortestPathsToDestNodes.put(destTableAlias, list);
        }

        void addShortestPathTo(int destTableOrdinal, List<SteinerEdge> list) {
            if (djikstraDestOrdinalShortestPathsMap == null) {
                djikstraDestOrdinalShortestPathsMap = new HashMap<Integer, List<SteinerEdge>>();
            }
            djikstraDestOrdinalShortestPathsMap.put(destTableOrdinal, list);
        }

        void setDjikstraTreeRoot(PrimDjikstraNode n) {
            djikstraTree = n;
            djikstraNodeMap.put(getTableAlias(n.getOrdinal()), n);
            djikstraOrdinalNodeMap.put(djikstraTree.getOrdinal(), n);
        }

        //
        // step by step build up of Tree from algorithm
        // this is order dependent and must be run as the algorithm builds
        //
        void addToDjikstraTree(PrimDjikstraNode node) {
            if (node == null) return;
            djikstraOrdinalNodeMap.put(node.getOrdinal(), node);
            SteinerEdge edgeToThisNode = node.getDjikstraEdgeToThisNode();
            int exploredValue = node.getDjikstraExploredValue();
            int prevNodeOrdinal = edgeToThisNode.getLeftNum();
            if (prevNodeOrdinal >= 0) {
                PrimDjikstraNode leftNode = getOrCloneNodeByOrdinal(prevNodeOrdinal);
                leftNode.addEdge(edgeToThisNode);
            }
        }

        public PrimDjikstraNode getDjikstraNode(String key) {
            if (djikstraNodeMap == null) {
                djikstraNodeMap = new HashMap<String, PrimDjikstraNode>();
            }
            return djikstraNodeMap.get(key);
        }

        public PrimDjikstraNode getDjikstraNode(int ordinal) {
            if (ordinal < 0) return null;
            return nodeArray[ordinal];
        }

        public PrimDjikstraNode getDjikstraTree() {
            return djikstraTree;
        }

        /**
         * since this is a tree every node, except the root node will
         * have only 1 edge that point to it from another node
         *
         * @param a
         * @param b
         * @return
         */
        public boolean containsEdge(int a, int b) {
            String sa = Integer.toString(a);
            String sb = Integer.toString(b);

            PrimDjikstraNode n = nodeArray[a];
            if (n != null) {
                SteinerEdge e = n.getDjikstraEdgeToThisNode();
                if (e != null) {
                    int eFrom = e.getLeftNum();
                    if (eFrom == b) {
                        return true;
                    }
                }
            }
            // check the opposite direction
            n = nodeArray[b];
            if (n != null) {
                SteinerEdge e = n.getDjikstraEdgeToThisNode();
                if (e != null) {
                    int eFrom = e.getLeftNum();
                    if (eFrom == a) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Map<String, List<SteinerEdge>> getShortestPathsMap() {
            return shortestPathsToDestNodes;
        }

        public String printShortestPathsMap() {
            StringBuilder sb = new StringBuilder("Shortest Paths Map Entries for Source Node '"+source+"'\n");
            Map<String, List<SteinerEdge>> map = getShortestPathsMap();
            for (String dest : map.keySet()) {
                sb.append("  Dest: '"+dest+"'\n");
                List<SteinerEdge> list = map.get(dest);
                sb.append(SteinerEdge.printEdgeList(list, originalGraph)+"\n");
            }
            return sb.toString();
        }
        public List<SteinerEdge> getDjikstraEdgeList() {
            List<SteinerEdge> edgeList = new ArrayList<SteinerEdge>();
            for (Integer ordinal : djikstraOrdinalNodeMap.keySet()) {
                PrimDjikstraNode node = djikstraOrdinalNodeMap.get(ordinal);
                SteinerEdge edge = node.getDjikstraEdgeToThisNode();
                if (edge != null) {
                    edgeList.add(edge);
                }
            }
            return edgeList;
        }

        public Set<SteinerEdge> getDjikstraEdgeSet() {
            Set<SteinerEdge> edgeSet = new HashSet<SteinerEdge>();
            for (Integer ordinal : djikstraOrdinalNodeMap.keySet()) {
                PrimDjikstraNode node = djikstraOrdinalNodeMap.get(ordinal);
                SteinerEdge edge = node.getDjikstraEdgeToThisNode();
                if (edge != null) {
                    edgeSet.add(edge);
                }
            }
            return edgeSet;
        }

        public int getDjikstraTotalWeight() {
            List<SteinerEdge> edgeList = getDjikstraEdgeList();
            int totalWeight = 0;
            for (SteinerEdge e : edgeList) {
                totalWeight += e.getWeight();
            }
            return totalWeight;
        }

        public String printDjikstraEdges() {
            int totalWeight = 0;
            StringBuilder sb = new StringBuilder();
            List<SteinerEdge> edgeList = getDjikstraEdgeList();
            for (SteinerEdge e : edgeList) {
                sb.append(e.toString(originalGraph)).append("\n");
                int weight = e.getWeight();
                totalWeight += weight;
            }
            sb.append("  Total Tree Weight=" + totalWeight);
            return sb.toString();
        }
    }
}

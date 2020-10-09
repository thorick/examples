package thorick.code;

import java.util.*;


/**
 *
*  Outside of the graph, nodes have String identifiers for generality
*  Inside of the graph, nodes are mapped to integers for efficiency of computation
*
*  The Graph has methods and maintains maps to navigate between the 2 identifiers when desired.
*
*  There are uses of Graph which involve reading elements from one Graph instance
*  and applying an algorithm to construct a new Graph result
*  In order to facilitate these operations there are methods to clone existing Graph elements
*
*  NOTE:  This class is specifically tailored to support MinimumSQLJoinCalculations
*         the 'Steiner Graph' problem using a combination of both
*         Djikstra Single Source Shortest Paths and Prims Minimum Spanning Tree algorithms.
*         There are datastructures used in these calculations.
*         In order to reuse the fixed graph for calculation, the resetForCalculation() method must be called
*         to reset these supporting datastructures for each separate calculation on the graph.
*
 */
public class Graph {
    int nodeArraySize = 1000;
    int nextTableNum = 0;
    PrimDjikstraNode[] nodeArray;

    String tableNumToNodeName[];
    Map<String, Integer> nodeNameToTableNumMap;
    Set<SteinerEdge> closureGraphEdgeSet;                     // convenience for the closureGraph
    Map<Integer, List<SteinerEdge>> outboundEdgesFromNode;    // convenience for PrimsMST
    Map<Integer, List<SteinerEdge>> inboundEdgesToNode;       // convenience for PrimsMST

    public Graph() {
        tableNumToNodeName = new String[nodeArraySize];
        nodeArray = new PrimDjikstraNode[nodeArraySize];
        nodeNameToTableNumMap = new HashMap<String, Integer>();
    }

    public void resetForCalculation() {
        closureGraphEdgeSet = null;
        outboundEdgesFromNode = null;
        inboundEdgesToNode = null;
    }

    public int getNumNodes() {
        return nextTableNum;
    }

    public PrimDjikstraNode getNode(Integer key) {
        return nodeArray[key];
    }

    public PrimDjikstraNode getNode(String key) {
        int tableNum = nodeNameToTableNumMap.get(key);
        return nodeArray[tableNum];
    }

    public PrimDjikstraNode[] getNodeArray() {
        return nodeArray;
    }

    public PrimDjikstraNode addNewNode(String key) {
        if (nodeNameToTableNumMap.get(key) != null) {
            throw new IllegalArgumentException("attempt to add the same node twice: " + key);
        }
        if (nextTableNum >= tableNumToNodeName.length) {
            String[] _next = new String[tableNumToNodeName.length + nodeArraySize];
            System.arraycopy(tableNumToNodeName, 0, _next, 0, tableNumToNodeName.length);
            tableNumToNodeName = _next;
            PrimDjikstraNode[] _nextN = new PrimDjikstraNode[nodeArray.length + nodeArraySize];
            System.arraycopy(nodeArray, 0, _nextN, 0, nodeArray.length);
            nodeArray = _nextN;
        }
        int nodeNum = nextTableNum++;
        PrimDjikstraNode n = new PrimDjikstraNode(nodeNum);
        nodeNameToTableNumMap.put(key, nodeNum);
        tableNumToNodeName[nodeNum] = key;
        nodeArray[nodeNum] = n;
        return n;
    }

    // add a clone of graph edge with Djikstra calculation state cleaned out
    // NOTE:  does no integrity checking of edge closure.
    public PrimDjikstraNode addCleanCloneOfNode(PrimDjikstraNode orig, String tableName) {
        PrimDjikstraNode cloned = (PrimDjikstraNode) orig.clone();
        cloned.initForDjikstra();
        int ordinal = cloned.ordinal;
        nodeNameToTableNumMap.put(tableName, ordinal);
        tableNumToNodeName[ordinal] = tableName;
        nodeArray[ordinal] = cloned;
        return cloned;
    }

    public void addNewEdge(SteinerEdge e) {
        Integer fromNum = e.getLeftNum();
        if (nodeArray[fromNum] == null) {
            throw new IllegalArgumentException("cannot add SteinerEdge " + e + ", Tail PrimDjikstraNode '" + fromNum + "' not in graph.");
        }
        Integer toNum = e.getRightNum();
        if (nodeArray[toNum] == null) {
            throw new IllegalArgumentException("cannot add SteinerEdge " + e + ", Head PrimDjikstraNode '" + toNum + "' not in graph.");
        }
        PrimDjikstraNode fromNode = nodeArray[fromNum];
        fromNode.addEdge(e);
    }

    public SteinerEdge addBidirectionalEdgeToGraphByTableName(String t1, String t2) {
        return addEdgeToGraphByTableName(t1, t2, null, null, true);
    }
    public SteinerEdge addEdgeToGraphByTableName(String t1, String t2) {
        return addEdgeToGraphByTableName(t1, t2, null, null, false);
    }
    public SteinerEdge addEdgeToGraphByTableName(String t1, String t2, Join.JoinType t, Integer weight) {
        return addEdgeToGraphByTableName(t1, t2, t, weight, false);
    }
    public SteinerEdge addBidirectionalEdgeToGraphByTableName(String t1, String t2, Join.JoinType t, Integer weight) {
        return addEdgeToGraphByTableName(t1, t2, t, weight, true);
    }

    // default to inner join
    // returns SteinerEdge corresponding to edge [t1, t2]
    public SteinerEdge addEdgeToGraphByTableName(String t1, String t2, Join.JoinType t, Integer weight, boolean bidirectional) {
        if (t1 == null || t1.length() <= 0 || t2 == null || t2.length() <= 0) {
            throw new RuntimeException("table names cannot be null or empty");
        }
        if (t == null) t = Join.JoinType.INNER;
        if (weight == null || weight < 1) weight = 1;
        PrimDjikstraNode n1 = null;
        Integer num1 = nodeNameToTableNumMap.get(t1);
        if (num1 == null) {
            n1 = addNewNode(t1);
            num1 = n1.getOrdinal();
        }
        PrimDjikstraNode n2 = null;
        Integer num2 = nodeNameToTableNumMap.get(t2);
        if (num2 == null) {
            n2 = addNewNode(t2);
            num2 = n2.getOrdinal();
        }
        SteinerEdge edge = getEdge(num1, num2);
        //if (edge != null) return edge;
        if (edge == null) {
            Join join = new Join(num1, num2, weight, t);
            edge = new SteinerEdge(join, false);
            addNewEdge(edge);
        }
        if (!bidirectional) return edge;
        SteinerEdge oppositeEdge = getEdge(num2, num1);
        if (oppositeEdge == null) {
            // SteinerEdge contains a single edge and not a composite one,
            // it will be built as a fully normal edge with an unreversed Join
            Join.JoinType oppositeJoinType = Join.JoinType.getOppositeJoinType(t);
            Join join = new Join(num2, num1, weight, oppositeJoinType);
            oppositeEdge = new SteinerEdge(join, false);
            addNewEdge(oppositeEdge);
        }
        return edge;
    }

    public static SteinerEdge addNewCompositeEdgeAndNodesToDestGraphMaybe(List<SteinerEdge> list, Graph modelGraph, Graph destGraph) {
       SteinerEdge destEdge = null;
        List<SteinerEdge> newList = new ArrayList<SteinerEdge>();    // note: the newList edges are NOT clones !
        for (SteinerEdge edge : list) {
            SteinerEdge destInnerEdge = edge.cloneEdge(edge);
            newList.add(destInnerEdge);
        }
        SteinerEdge newEdge = new SteinerEdge(newList, false);
        destEdge = modelGraph.addNewEdgeAndNodesToDestGraphMaybe(newEdge, modelGraph, destGraph);
       return destEdge;
    }

    public static SteinerEdge addNewEdgeAndNodesToDestGraphMaybe(SteinerEdge e, Graph modelGraph, Graph destGraph) {
        Integer fromNum = e.getLeftNum();
        PrimDjikstraNode fromNode = checkAndAddNodeToDestGraphMaybe(fromNum, modelGraph, destGraph);
        Integer toNum = e.getRightNum();
        PrimDjikstraNode toNode = checkAndAddNodeToDestGraphMaybe(toNum, modelGraph, destGraph);
        SteinerEdge destEdge = destGraph.getEdge(fromNum, toNum);
        if (destEdge == null) {
            if (e.isComposite()) {
                List<SteinerEdge> list = e.getCompositeEdges();
                List<SteinerEdge> clonedList = new ArrayList<SteinerEdge>();
                for (SteinerEdge innerEdge : list) {
                    SteinerEdge clonedEdge = addNewEdgeAndNodesToDestGraphMaybe(innerEdge, modelGraph, destGraph);
                    clonedList.add(clonedEdge);
                }
                e.compositeEdges = clonedList;
            }
            destEdge = e.cloneEdge(e);
            destGraph.addNewEdge(destEdge);
        }
        return destEdge;
    }

    public static SteinerEdge  addOppositeNewEdgeAndNodesToDestGraphMaybe(SteinerEdge e, Graph modelGraph, Graph destGraph) {
        Integer fromNum = e.getLeftNum();
        PrimDjikstraNode fromNode = checkAndAddNodeToDestGraphMaybe(fromNum, modelGraph, destGraph);
        Integer toNum = e.getRightNum();
        PrimDjikstraNode toNode = checkAndAddNodeToDestGraphMaybe(toNum, modelGraph, destGraph);
        SteinerEdge destEdge = destGraph.getEdge(fromNum, toNum);
        if (destEdge == null) {
            throw new RuntimeException("Attempt to add opposite edge before normal edge present.  Edge: "+e.toString(modelGraph));
        }
        SteinerEdge destOppositeEdge = null;
        if (destGraph.containsEdge(toNum, fromNum) == false) {
            destOppositeEdge = destEdge.cloneAnOppositeBidirectionalEdge(destEdge);
            if (destOppositeEdge.isComposite()) {
                List<SteinerEdge> list = destOppositeEdge.getCompositeEdges();
                List<SteinerEdge> clonedList = new ArrayList<SteinerEdge>();
                for (SteinerEdge innerEdge : list) {
                    SteinerEdge clonedEdge = addNewEdgeAndNodesToDestGraphMaybe(innerEdge, modelGraph, destGraph);
                    clonedList.add(clonedEdge);
                }
                destOppositeEdge.compositeEdges = clonedList;
            }
            destGraph.addNewEdge(destOppositeEdge);
        }
        return destOppositeEdge;
    }

    public static PrimDjikstraNode checkAndAddNodeToDestGraphMaybe(Integer nodeNum, Graph modelGraph, Graph destGraph) {
        PrimDjikstraNode node = destGraph.getNode(nodeNum);
        if (node == null) {
            PrimDjikstraNode origNode = modelGraph.getNode(nodeNum);
            node = destGraph.addCleanCloneOfNode(origNode, modelGraph.tableNumToNodeName[nodeNum]);
        }
        return node;
    }

    public boolean containsEdge(Integer fromNum, Integer toNum) {
        return getEdge(fromNum, toNum) != null;
    }

    public SteinerEdge getEdge(Integer fromNum, Integer toNum) {
        PrimDjikstraNode nLookup = nodeArray[fromNum];
        if (nLookup == null) return null;
        return nLookup.getEdge(toNum);
    }

    public SteinerEdge getEdge(String fromName, String toName) {
        Integer fromNum = nodeNameToTableNumMap.get(fromName);
        Integer toNum = nodeNameToTableNumMap.get(toName);
        return getEdge(fromNum, toNum);
    }

    public Set<SteinerEdge> getAllEdges() {
        Set<SteinerEdge> set = new HashSet<SteinerEdge>();
        for (int i = 0; i < nextTableNum; i++) {
            PrimDjikstraNode node = nodeArray[i];
            if (node != null) {
                List<SteinerEdge> list = node.getSteinerEdgeList();
                for (SteinerEdge edge : list) {
                    set.add(edge);
                }
            }
        }
        return set;
    }


    public void addNewEdgeBidirectional(SteinerEdge e) {
        addNewEdge(e);
        SteinerEdge oppositeEdge = e.cloneAnOppositeBidirectionalEdge(e);
        addNewEdge(oppositeEdge);
    }


    public void addClosureGraphEdge(SteinerEdge e) {
        if (closureGraphEdgeSet == null) {
            closureGraphEdgeSet = new HashSet<SteinerEdge>();
            outboundEdgesFromNode = new HashMap<Integer, List<SteinerEdge>>();
            inboundEdgesToNode = new HashMap<Integer, List<SteinerEdge>>();
        }
        closureGraphEdgeSet.add(e);
        Integer leftNum = e.getLeftNum();
        List<SteinerEdge> fromList = outboundEdgesFromNode.get(leftNum);
        if (fromList == null) {
            fromList = new ArrayList<SteinerEdge>();
            outboundEdgesFromNode.put(leftNum, fromList);
        }
        fromList.add(e);
        Integer rightNum = e.getRightNum();
        List<SteinerEdge> toList = inboundEdgesToNode.get(rightNum);
        if (toList == null) {
            toList = new ArrayList<SteinerEdge>();
            inboundEdgesToNode.put(rightNum, toList);
        }
        toList.add(e);
    }

    public Set<SteinerEdge> getClosureGraphEdgeSet() {
        if (closureGraphEdgeSet == null) {
            closureGraphEdgeSet = new HashSet<SteinerEdge>();
        }
        return closureGraphEdgeSet;
    }

    //
    //  Get all Joins in the graph but ignore duplicates
    //  due to bidirectional edges
    //  In our current implementation each node contains its own edge object
    //  for an edge to another node
    //  If the edge is bidirectional then the other node has its own edge pointing in the opposite sense
    //  This is intended to get basic individual joins not composite joins in Steiner Edges
    //
    public Collection getAllJoins(boolean uniqueOnly) {
        Map<Integer,Set<Integer>> joinSets = new HashMap<Integer, Set<Integer>>();
        Collection<Join> joins = new ArrayList<Join>();
        for (int i=0; i<nextTableNum; i++) {
            PrimDjikstraNode n = nodeArray[i];
            Integer leftNum = n.getOrdinal();
            Collection<Join> c = n.getAllJoins(this);
            if (c.size() > 0) {
                if (uniqueOnly) {
                    Set<Integer> nodeSet = joinSets.get(leftNum);
                    if (nodeSet == null) {
                        nodeSet = new HashSet<Integer>();
                        joinSets.put(leftNum, nodeSet);
                    }
                    // check the join right side, did we already include this join in our unique List ?
                    for (Join join : c) {
                        Integer rightNum = join.getRightNum();
                        Set<Integer> rightNodeSet = joinSets.get(rightNum);
                        if (rightNodeSet != null) {
                            if (rightNodeSet.contains(leftNum)) continue;
                        }
                        joins.add(join);
                        nodeSet.add(rightNum);
                    }
                }
                else {
                    joins.addAll(c);
                }
            }
        }
        return joins;
    }
    public String printAllJoins() {
        return printJoinCollection(getAllJoins(false));
    }

    // print only one side of a bidirectional join
    public String printAllUniqueJoins() {
        return printJoinCollection(getAllJoins(true));
    }

    public String printJoinCollection(Collection<Join> joins) {
        StringBuilder sb = new StringBuilder();
        for (Join join : joins) {
            sb.append(join.toString(this)).append("\n");
        }
        return sb.toString();
    }
}

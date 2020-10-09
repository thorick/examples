package thorick.code;

//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;

import java.util.*;

public class PrimsMST {
    //protected final Logger log = LogManager.getLogger(PrimsMST.class);

    final Graph inputGraph;
    SteinerEdgeHeap edgeHeap;

    public PrimsMST(Graph g) {
        inputGraph = g;
    }

    //  Note:  every edge used to compute the MST MUST be a cloned edge of the inputGraph
    //         this is because the edge keeps heap position information in it
    //         which is specific to this computation
    public Graph computeMST() {
        Graph mst = new Graph();
        int numNodes = inputGraph.nodeNameToTableNumMap.keySet().size();
        if (numNodes <= 0)  { return mst; }    // empty graph
        Set<SteinerEdge> closureGraphEdges = inputGraph.getClosureGraphEdgeSet();
        if (closureGraphEdges.size() <= 1) { return inputGraph; }    // at most one edge

        Set<Integer> selectedNodes = new HashSet<Integer>();
        edgeHeap = new SteinerEdgeHeap(SteinerEdge.fakeEdge(), ((numNodes-1) * (numNodes-2) / 2), inputGraph);

        // pick a start edge, let's just pick the first one !
        SteinerEdge currEdge = closureGraphEdges.iterator().next();
        Integer currNode = currEdge.getLeftNum();
        while (currNode != null) {
            //log.debug("-------  BEGIN currNode("+currNode+"): "+inputGraph.tableNumToNodeName[currNode]);
            selectedNodes.add(currNode);
            List<SteinerEdge> list = inputGraph.outboundEdgesFromNode.get(currNode);
            Iterator<SteinerEdge> lit = list.iterator();
            while (lit.hasNext()) {
                SteinerEdge edge = lit.next();
                edgeHeap.insert(edge);
            }
            boolean foundNewEdge = false;
            SteinerEdge theEdge = null;
            Integer nextNodeNum = null;
            while (!foundNewEdge && currNode != null) {
                // pick the lowest cost edge out to unexplored heap, set next node
                theEdge = edgeHeap.remove();
                if (theEdge == null) {
                    currNode = null;
                    nextNodeNum = null;
                    continue;
                }
                nextNodeNum = theEdge.rightNum;
                if (selectedNodes.contains(nextNodeNum)) {
                    nextNodeNum = theEdge.leftNum;
                    if (selectedNodes.contains(nextNodeNum)) {
                        nextNodeNum = null;
                        continue;
                    }
                }
                foundNewEdge = true;
            }
            if (foundNewEdge) {
                inputGraph.addNewEdgeAndNodesToDestGraphMaybe(theEdge, inputGraph, mst);
                mst.addClosureGraphEdge(theEdge);
                selectedNodes.add(currNode);
            }
            currNode = nextNodeNum;
        }
        return mst;
    }
}

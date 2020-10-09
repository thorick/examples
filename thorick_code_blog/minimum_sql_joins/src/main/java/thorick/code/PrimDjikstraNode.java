package thorick.code;

import java.util.*;

public class PrimDjikstraNode
        implements HeapThing {
    final int ordinal;
    int heapPosition;
    List<SteinerEdge> steinerEdgeList;

    // all 'dj' members are used during the execution of the Djikstra shortest paths algorithm
    SteinerEdge djEdgeToThisNode;
    SteinerEdge djEdgeFromThisNode;
    int djCandidateValue;               // lowest path value
    SteinerEdge djCandidateEdge;        // edge for the value
    boolean djIsExplored;
    int djExploredValue;                // final value

    public PrimDjikstraNode(int ord) {
        ordinal = ord;
        djCandidateValue = Integer.MAX_VALUE;
    }

    public PrimDjikstraNode[] newHeapThingArray(int size) {
        return new PrimDjikstraNode[size];
    }
    public boolean isInHeap() { return getHeapPosition() > 0; }
    public int getHeapPosition() {
        return heapPosition;
    }
    public void setHeapPosition(int i) {
        heapPosition = i;
    }

    // takes the current state, whatever it is
    @Override
    public Object clone() {
        PrimDjikstraNode cloned = new PrimDjikstraNode(ordinal);
        List<SteinerEdge> origL = getSteinerEdgeList();
        List<SteinerEdge> clonedL = new ArrayList<SteinerEdge>();
        for (SteinerEdge edge : origL) {
            clonedL.add(edge.cloneEdge(edge));
        }
        cloned.steinerEdgeList = clonedL;
        cloned.djEdgeToThisNode = SteinerEdge.cloneEdge(djEdgeToThisNode);
        cloned.djEdgeFromThisNode = SteinerEdge.cloneEdge(djEdgeFromThisNode);
        cloned.djIsExplored = djIsExplored;
        cloned.heapPosition = heapPosition;   // this is actually nonsense here
        cloned.djCandidateValue = djCandidateValue;
        cloned.djExploredValue = djExploredValue;
        return cloned;
    }

    public void initForDjikstra() {
        djIsExplored = false;
        djExploredValue = Integer.MAX_VALUE;
        djEdgeToThisNode = null;
        djEdgeFromThisNode = null;
        heapPosition = 0;
        djCandidateValue = Integer.MAX_VALUE;
        djCandidateEdge = null;
    }

    @Override
    public String toString() {
        String candEdge = (djCandidateEdge == null ? "N/A" : djCandidateEdge.toString());
        String to = (djEdgeToThisNode == null ? "N/A" : djEdgeToThisNode.toString());
        String from = (djEdgeFromThisNode == null ? "N/A" : djEdgeFromThisNode.toString());
        return "PDjNode("+Integer.toString(ordinal)+ ")"+
                //", hash="+this.hashCode()+
                ", heapPosition="+heapPosition+
                ", candValue="+djCandidateValue+
                ", candEdge="+candEdge+
                ", exploredValue="+ Integer.toString(djExploredValue)+
                ", edgeToThis="+to+
                ", edgeFromThis="+from;
    }

    public String toString(Graph g) {
        String candEdge = (djCandidateEdge == null ? "N/A" : djCandidateEdge.toString());
        String to = (djEdgeToThisNode == null ? "N/A" : djEdgeToThisNode.toString(g));
        String from = (djEdgeFromThisNode == null ? "N/A" : djEdgeFromThisNode.toString(g));
        return "PDjNode("+g.tableNumToNodeName[ordinal]+ ")"+
                //", hash="+this.hashCode()+
                ", heapPosition="+heapPosition+
                ", candValue="+djCandidateValue+
                ", candEdge="+candEdge+
                ", exploredValue="+
                Integer.toString(djExploredValue)+
                ", edgeToThis="+to+
                ", edgeFromThis="+from;
    }

    public List<SteinerEdge> getSteinerEdgeList() {
        if (steinerEdgeList == null) {
            steinerEdgeList = new ArrayList<SteinerEdge>();
        }
        return steinerEdgeList;
    }

    public Iterator<SteinerEdge> getSteinerEdgeIterator() {
        return getSteinerEdgeList().iterator();
    }

    public int getOrdinal() {
        return ordinal;
    }

    public SteinerEdge getEdge(Integer toNum) {
        SteinerEdge theEdge = null;
        if (getSteinerEdgeList().size() <= 0) return theEdge;
        for (SteinerEdge e : getSteinerEdgeList()) {
            if (e.getRightNum().equals(toNum)) {
                theEdge = e;
            }
        }
        return theEdge;
    }
    public void addEdge(SteinerEdge e) {
        getSteinerEdgeList().add(e);
    }
    public void removeEdge(SteinerEdge e) {
        getSteinerEdgeList().remove(e);
    }
    public boolean containsEdgeTo(Integer toNum) {
        return getEdge(toNum) != null;
    }
    public SteinerEdge getDjikstraCandidateEdge() {
        return djCandidateEdge;
    }
    public void setDjikstraCandidateEdge(SteinerEdge e) {
        djCandidateEdge = e;
    }
    public int getDjikstraCandidateValue() {
        return djCandidateValue;
    }
    public void setDjikstraCandidateValue(int i) {
        djCandidateValue = i;
    }
    public boolean isDjikstraExplored() {
        return djIsExplored;
    }
    public void setDjikstraExplored(boolean b) {
        djIsExplored = b;
    }
    public int getDjikstraExploredValue() {
        return djExploredValue;
    }

    public void setDjikstraExploredValue(int i) {
        djExploredValue = i;
        djIsExplored = true;   // impure but safe and appropriate
    }

    public SteinerEdge getDjikstraEdgeToThisNode() {
        return djEdgeToThisNode;
    }

    public void setDjikstraEdgeToThisNode(SteinerEdge e) {
        djEdgeToThisNode = e;
    }
    public SteinerEdge getDjikstraEdgeFromThisNode() {
        return djEdgeFromThisNode;
    }
    public void setDjikstraEdgeFromThisNode(SteinerEdge e) {
        djEdgeFromThisNode = e;
    }

    public int compareTo(Object other) {
        if (!(other instanceof PrimDjikstraNode)) return -1;
        PrimDjikstraNode otherPDN = (PrimDjikstraNode) other;
        if (getDjikstraCandidateValue() > otherPDN.getDjikstraCandidateValue()) {
            return 1;
        } else if (getDjikstraCandidateValue() == otherPDN.getDjikstraCandidateValue()) {
            return 0;
        }
        return -1;
    }

    public String printAllJoins(Graph graph) {
        StringBuilder sb = new StringBuilder();
        if (steinerEdgeList != null && steinerEdgeList.size() > 0) {
            for (SteinerEdge edge : steinerEdgeList) {
                Join join = edge.getJoin();
                sb.append(join.toString(graph)).append("\n");
            }
        }
        return sb.toString();
    }

    public Collection<Join> getAllJoins(Graph graph) {
        Set<Join> s = new HashSet<Join>();
        if (steinerEdgeList != null && steinerEdgeList.size() > 0) {
            for (SteinerEdge edge : steinerEdgeList) {
                Join join = edge.getJoin();
                s.add(join);
            }
        }
        return s;
    }
    static PrimDjikstraNode fakeNode(int i) {
        return new PrimDjikstraNode(i);
    }
}


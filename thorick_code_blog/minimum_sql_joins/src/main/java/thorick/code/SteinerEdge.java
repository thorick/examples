package thorick.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A SteinerEdge is an edge in a graph that represents a Join between 2 tables.
 *
 * It is very specialized and is much more than just that though.
 * The Steiner Graph algorithm has a step which requires the use of composite edges
 * that connect 2 nodes.
 * A composite edge may contain a connected set of many single edges.
 * The composite edge is treated as though it were a single edge which connects the 2 nodes at its extremes.
 *
 * So a connected edge set
 *    [A-B][B-C][C-D][D-E]
 *
 * Can be a single SteinerEdge [A-E] which internally contains the connected edge set.
 *
 * Representing composite edges in this was allows us to use standard graph algorithms that are completely
 * unaware that they are operating on other than normal edges in a graph.
 *
 * A SteinerEdge also contains an optimization for locating its position in the RandomAccessHeap to facilitate
 * an Update in place operation within the Heap.
 *
 *
 * A wrinkle in this implementation is that a SteinerEdge might have been created from a SteinerEdge that
 * is its bidirectional opposite, so that we use [A-E] to create [E-A].  In this case we keep the same Joins
 * in the opposite but they are in the 'wrong' sense so we have a compensating flag 'joinIsReversed' to tell
 * us to interpret the composite Joins in the opposite sense.
 * It might have been simpler to make a completely consistent SteinerEdge with opposite internal Joins,
 * it's what we have now.
 *
 */
public class SteinerEdge
        implements HeapThing {
    final Join join;     // Joins are not cloned, they don't ever change
    Integer leftNum = -1;
    Integer rightNum = -1;
    Integer weight = 1;
    boolean joinIsReversed = false;   // edge joinIsReversed for bidirectional graphs, the Join then is in the opposite sense
    int heapPosition;

    // A composite SteinerEdge is an SteinerEdge that contains an ordered list of Edges
    // composition only goes 1 level deep.
    //
    boolean isComposite = false;
    List<SteinerEdge> compositeEdges;

    public SteinerEdge(Join j) {
        this(j, false);
    }

    public SteinerEdge(Join j, boolean joinIsReversed) {
        join = j;
        leftNum = j.getLeftNum();
        rightNum = j.getRightNum();
        weight = j.getWeight();
        this.joinIsReversed = joinIsReversed;

        if (joinIsReversed) {
            leftNum = j.getRightNum();
            rightNum = j.getLeftNum();
        }
    }

    public SteinerEdge(List<SteinerEdge> l, boolean joinIsReversed) {
        join = null;
        isComposite = true;
        this.joinIsReversed = joinIsReversed;
        compositeEdges = l;
        if (l.size() > 0) {
            if (joinIsReversed)
                System.err.println("SteinerEdge constr  reverseJoins may be wrong !");
            leftNum = l.get(0).leftNum;
            rightNum = l.get(l.size() - 1).rightNum;
            for (SteinerEdge e : l) {
                weight += e.getWeight();
            }
        }
    }
    public SteinerEdge[] newHeapThingArray(int size) {
        return new SteinerEdge[size];
    }
    public boolean isInHeap() { return getHeapPosition() > 0; }
    public int getHeapPosition() {
        return heapPosition;
    }
    public void setHeapPosition(int i) {
        heapPosition = i;
    }
    public int compareTo(Object o) {
        if (o == null)  return -1;
        if (!(o instanceof  SteinerEdge))  return -1;
        SteinerEdge otherEdge = (SteinerEdge) o;
        if (weight > otherEdge.getWeight())  return 1;
        if (weight < otherEdge.getWeight())  return -1;
        return 0;
    }
    @Override
    public int hashCode() {
        return leftNum + rightNum + weight;   // overflow OK
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!(other instanceof  SteinerEdge)) return false;
        SteinerEdge oe = (SteinerEdge) other;
        if (this.leftNum != oe.leftNum) return false;
        if (this.rightNum != oe.rightNum) return false;
        if (this.weight != oe.weight) return false;
        return true;
    }
    public boolean isComposite() {
        return isComposite;
    }
    public List<SteinerEdge> getCompositeEdges() {
        return compositeEdges;
    }
    public Join getJoin() {
        if (isComposite) {
            throw new RuntimeException("cannot ask for Join on a composite SteinerEdge");
        }
        return join;
    }
    public int getWeight() { return weight; }
    public Integer getLeftNum() { return leftNum; }
    public Integer getRightNum() { return rightNum; }
    public boolean isJoinIsReversed() {
        return joinIsReversed;
    }
    public void setJoinIsReversed(boolean joinIsReversed) {
        this.joinIsReversed = joinIsReversed;
    }
    public int compareTo(SteinerEdge other) {
        if (other == null) {
            throw new RuntimeException("compare got NULL other object !");
        }
        if (weight < other.getWeight()) {
            return -1;
        } else if (weight == other.getWeight()) {
            return 0;
        }
        return 1;
    }

    @Override
    public String toString() {
        return "{"+leftNum + "-" + rightNum + "}(" + weight + ")";
    }
    public String toString(Graph g) {
        if (g == null) return toString();
        String left = g.tableNumToNodeName[leftNum];
        String right = g.tableNumToNodeName[rightNum];
        return "{"+left + "-" + right + "}(" + weight + ")";
    }
    public static String printEdgeList(List<SteinerEdge> l) {
        return printEdgeList(l, null);
    }
    // if called with graph ref, use tableNames instead of node numbers
    public static String printEdgeList(Collection<SteinerEdge> l, Graph g) {
        if (l == null || l.size() <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (SteinerEdge e : l) {
            String es = (g == null ? e.toString() : e.toString(g));
            sb.append(es);
            if (e.isComposite()) {
                sb.append("  composite: [");
                List<SteinerEdge> clist = e.getCompositeEdges();
                if (clist != null) {
                    boolean first = true;
                    for (SteinerEdge ce : clist) {
                        String ces = (g == null ? ce.toString() : ce.toString(g));
                        if (!first) {
                            sb.append(", ");
                        }
                        sb.append(ces);
                        first = false;
                    }
                }
                sb.append("]");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public int computeEdgeListWeight(List<SteinerEdge> l) {
        if (l == null || l.size() <= 0) return 0;
        int totalWeight = 0;
        for (SteinerEdge e : l) {
            totalWeight += e.getWeight();
        }
        return totalWeight;
    }

    // for single edge only, skips composite edges
    private static void copySingleEdgeProps(SteinerEdge from, SteinerEdge to) {
        to.leftNum = from.getLeftNum();
        to.rightNum = from.getRightNum();
        to.weight = from.weight;
    }
    public static SteinerEdge cloneEdge(SteinerEdge e) {
        if (e == null) return null;
        boolean joinReversed = e.isJoinIsReversed();
        return cloneEdge(e, joinReversed);
    }

    public static SteinerEdge cloneEdge(SteinerEdge e, boolean joinReversed) {
        if (e == null) return null;
        // SteinerEdge knows if it is composite or elemental
        if (e.isComposite()) {
            List<SteinerEdge> clonedEdges = new ArrayList<SteinerEdge>();
            List<SteinerEdge> eEdges = e.getCompositeEdges();
            for (SteinerEdge edge : eEdges) {
                SteinerEdge clonedEdge = cloneEdge(edge, joinReversed);
                clonedEdges.add(clonedEdge);
            }
            SteinerEdge clonedEdge = new SteinerEdge(clonedEdges, joinReversed);
            copySingleEdgeProps(e, clonedEdge);
            return clonedEdge;
        } else {
            SteinerEdge newEdge = new SteinerEdge(e.getJoin(), joinReversed);
            copySingleEdgeProps(e, newEdge);
            return newEdge;
        }
    }
    public static SteinerEdge cloneAnOppositeBidirectionalEdge(SteinerEdge e) {
        if (e == null)  return null;
        if (e.isComposite()) {
            Integer originalLeftNum = e.getLeftNum();
            Integer originalRightNum = e.getRightNum();
            List<SteinerEdge> clonedEdges = new ArrayList<SteinerEdge>();
            List<SteinerEdge> eEdges = e.getCompositeEdges();
            int numEdges = eEdges.size();
            for (int i = 0; i < eEdges.size(); i++) {
                SteinerEdge edge = eEdges.get((numEdges - 1) - i);
                Join j = edge.getJoin();
                boolean e_joinIsReversed = e.isJoinIsReversed();
                boolean newReversed = !(e_joinIsReversed);       // be sure to propagate composite join reversal
                SteinerEdge newReversedEdge = new SteinerEdge(j, newReversed);
                clonedEdges.add(newReversedEdge);
            }
            SteinerEdge newClonedEdge = new SteinerEdge(clonedEdges, false);
            newClonedEdge.leftNum = originalRightNum;
            newClonedEdge.rightNum = originalLeftNum;
            return newClonedEdge;
        } else {
            boolean e_joinIsReversed = e.isJoinIsReversed();
            Join j = e.getJoin();
            boolean reversed = !(e_joinIsReversed);
            return new SteinerEdge(j, reversed);
        }
    }

    static SteinerEdge fakeEdge() {
        Join fakeJoin = Join.fakeJoin();
        return new SteinerEdge(fakeJoin, false);
    }
}


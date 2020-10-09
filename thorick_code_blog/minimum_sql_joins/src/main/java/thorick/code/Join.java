package thorick.code;

import java.util.Collection;

/**
 * It is vitally important that we know that once defined, a Join is immutable.
 */
public final class Join {
    public enum JoinType {
        INNER("INNER JOIN"), LEFT_OUTER("LEFT OUTER JOIN"), RIGHT_OUTER("RIGHT OUTER JOIN");
        final String type;
        JoinType(String s) {
            type = s;
        }

        public static JoinType getOppositeJoinType(JoinType inputType) {
            switch(inputType) {
                case INNER:
                    return INNER;
                case LEFT_OUTER:
                    return RIGHT_OUTER;
                case RIGHT_OUTER:
                    return LEFT_OUTER;
                default:
                    throw new RuntimeException("Handled JoinType: "+inputType+
                            " !  need to add new case to method Join!JoinType.getOppositeJoinType(JoinType inputType)");
            }
        }
        @Override
        public String toString() {
            return type;
        }
    }
    final Integer leftNum;
    final Integer rightNum;
    final JoinType type;
    final int weight;

    public Join(PrimDjikstraNode n0, PrimDjikstraNode n1, int w) {
        this(n0, n1, w, JoinType.INNER);
    }
    public Join(PrimDjikstraNode n0, PrimDjikstraNode n1, int w, JoinType t) {
        this( n0.getOrdinal(), n1.getOrdinal(), w, t);
    }
    public Join(Integer l, Integer r, int w, JoinType t) {
        leftNum = l;
        rightNum = r;
        type = t;
        weight = w;
    }

    public String getLeft(Graph g) { return g.tableNumToNodeName[leftNum]; }
    public String getRight(Graph g) { return g.tableNumToNodeName[rightNum]; }
    public Integer getLeftNum() {
        return leftNum;
    }
    public Integer getRightNum() {
        return rightNum;
    }
    public JoinType getType() {
        return type;
    }
    public int getWeight() {
        return weight;
    }

    public String toString(Graph graph) {
        StringBuilder sb = new StringBuilder();
        sb.append(getLeft(graph)).append(" ").append(type.toString());
        sb.append(" ").append(getRight(graph));
        return sb.toString();
    }
    static Join fakeJoin() {
        PrimDjikstraNode lNode = PrimDjikstraNode.fakeNode(0);
        PrimDjikstraNode rNode = PrimDjikstraNode.fakeNode(1);
        Join j = new Join(lNode, rNode, 1);
        return j;
    }

    static String printJoinList(Collection<Join> list, Graph graph) {
        StringBuilder sb = new StringBuilder();
        if (list != null || list.size() > 0) {
            for (Join join : list) {
                sb.append(join.toString(graph)).append("\n");
            }
        }
        return sb.toString();
    }
}



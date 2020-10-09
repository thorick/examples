package thorick.code;

/**
 * A standard Array Based Heap
 * Uses Java 5 Generics so that this serves as the basis for any Heap of 'HeapThings'
 *
 * HeapThings are special in that they are aware of  their current position in the Heap Array
 * This allows a program to access/modify a Heap Element in place which you cannot do in a normal Heap.
 *
 * This is an example of tailoring a well known standard Datastructure for a specific use.
 *
 *
 * @param <T>
 */
public abstract class RandomAccessHeap<T extends HeapThing> {
    final T[] things;
    Graph graph;
    protected int count;   // the NUMBER of elements.  First element at 1.

    RandomAccessHeap(T thing, int size, Graph g) {
        graph = g;
        things = (T[]) thing.newHeapThingArray(size);
    }

    // special random access for heap
    public int updateNode(T node) {
        int index = node.getHeapPosition();
        if (index == 0) {
            return insert(node);
        }
        return updateNodeAt(index);
    }
    public int updateNodeAt(int index) {
        if (index < 1 || index > count) {
            return 0;
        }
        if (index == 1) {
            if (count <= 1) return 1;
            if (less(2, 1)) {
                return nodeUp(2);
            } else if (count >= 3) {
                if (less(3, 1)) {
                    return nodeUp(3);
                } else {
                    // 1 < 2  and  1 < 3, no change
                    return 1;
                }
            } else {
                // count == 2 and index = 1 is LESS, no change
                return 1;
            }
        } else if (greater((index >>> 1), index)) {
            return nodeUp(index);
        } else {
            return nodeDown(index, count);
        }
    }

    public void reset() {
        count = 0;
    }
    public int insert(T n) {
        setNode(++count, n);
        int index = nodeUp(count);
        return index;
    }

    public T remove() {
        if (count <= 0) {
            return null;
        }
        int lastIndex = count;
        exchange(1, count);
        nodeDown(1, (count - 1));

        T retVal = getNode(count);
        retVal.setHeapPosition(0);
        count--;
        return retVal;
    }

    public T peek() { return getNode(1); }

    // special random access for heap
    public T removeNode(int index) {
        if (count <= 0) {
            return null;
        }
        exchange(index, count);
        nodeDown(index, (count - 1));
        T retVal = getNode(count);
        retVal.setHeapPosition(0);
        count--;
        return retVal;
    }

    public boolean empty() { return count == 0; }
    public int size() { return count; }
    private int incrementCount() { return ++count; }
    private int decrementCount() { return --count; }
    private int nodeUp(int index) {
        while (index > 1 && greater((index >>> 1), index)) {
            exchange(index, (index >>> 1));
            index = index / 2;
        }
        return index;
    }

    private int nodeDown(int i1, int i2) {
        int j = i1;

        //  (i1 * 2) the left child of parent i1
        while ((i1 << 1) <= i2) {
            j = i1 << 1;
            if (j < i2 && greater(j, j + 1)) {
                j++;
            }
            if (!greater(i1, j)) {
                break;
            }
            exchange(i1, j);
            i1 = j;
        }
        return j;
    }

    private boolean less(int i1, int i2) {
        //if (i1 > i2) throw new RuntimeException("Error !  i1=" + i1 + ", is greater than i2=" + i2);
        if (i1 > count) return false;
        if (i2 > count) return false;
        T v1 = getNode(i1);
        T v2 = getNode(i2);
        if (v1.compareTo(v2) < 0) {
            return true;
        }
        return false;
    }

    private boolean greater(int i1, int i2) {
        if (i1 > count) return false;
        if (i2 > count) return false;
        T v1 = getNode(i1);
        T v2 = getNode(i2);
        if (v1.compareTo(v2) > 0) {
            return true;
        }
        return false;
    }

    private boolean equal(int i1, int i2) {
        if (i1 > count) return false;
        if (i2 > count) return false;
        T v1 = getNode(i1);
        T v2 = getNode(i2);
        if (v1.compareTo(v2) == 0) {
            return true;
        }
        return false;
    }

    private void exchange(int i1, int i2) {
        T n1 = getNode(i1);
        T n2 = getNode(i2);
        setNode(i1, n2);
        setNode(i2, n1);
    }

    T getNode(int index) { return things[index]; }
    void setNode(int index, T value) {
        things[index] = value;
        value.setHeapPosition(index);
    }

    @Override
    public String toString() { return printHeap(false); }
    public String checkHeapAndFail() { return printHeap(true); }
    public String printHeap(boolean failOnHeapViolation) {
        StringBuilder sb = new StringBuilder("\nHeapArray:\n");
        if (count == 0) {
            sb.append(" EMPTY ");
            return sb.toString();
        }

        // first print the array by element
        int currLevel = 0;
        int nextLevelIndex = 2;
        for (int i = 1; i <= count; i++) {
            if (i == nextLevelIndex) {
                nextLevelIndex = nextLevelIndex << 1;
                currLevel++;
                sb.append("\n");

                //if (currLevel >= 8) {
                //  sb.append(" === halting print for debug. \n\n\n");
                //  return sb.toString();
                //}
            }
            sb.append(currLevel + ":  [" + i + "]=");
            sb.append(getNode(i).toString(graph));
            sb.append("\n");
        }
        sb.append("END.\n");
        return sb.toString();
    }

    private String printPad(int totalLevels, int level) {
        int repeat = totalLevels - level + 1;
        String pad = "    " + "  ";     // assume keySize=4  add 2 spaces
        for (int i = 0; i < repeat; i++) {
            pad = pad + pad;
        }
        return pad;
    }

    public static int levelForIndex(int i) {
        if (i <= 0) return -1;
        if (i == 1) return 0;
        int level = 0;
        int val = i;
        while (val > 0) {
            val = val >>> 1;
            level++;
        }
        level = level - 1;
        return level;
    }

    public static int endIndexForLevel(int level) {
        if (level < 0) return -1;
        if (level == 0) return 1;
        return (1 << level) - 1;
    }
}



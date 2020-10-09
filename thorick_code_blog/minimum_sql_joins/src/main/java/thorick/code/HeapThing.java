package thorick.code;

public interface HeapThing extends Comparable {
    HeapThing[] newHeapThingArray(int size);
    boolean isInHeap();
    int getHeapPosition();
    void setHeapPosition(int i);
    String toString(Graph g);
}

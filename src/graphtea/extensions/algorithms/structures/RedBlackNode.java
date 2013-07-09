package graphtea.extensions.algorithms.structures;

/**
 */ // class RedBlackNode
public class RedBlackNode<T extends Comparable<T>> {

    /**
     * Possible color for this node
     */
    public static final int BLACK = 0;
    /**
     * Possible color for this node
     */
    public static final int RED = 1;
    // the key of each node
    public T key;
    /**
     * Parent of node
     */
    public RedBlackNode<T> parent;
    /**
     * Left child
     */
    public RedBlackNode<T> left;
    /**
     * Right child
     */
    public RedBlackNode<T> right;
    // the number of elements to the left of each node
    public int numLeft = 0;
    // the number of elements to the right of each node
    public int numRight = 0;
    // the color of a node
    public int color;
    // some arbitrary data associated with this node
    public Object data;

    RedBlackNode() {
        color = BLACK;
        numLeft = 0;
        numRight = 0;
        parent = null;
        left = null;
        right = null;
        data = null;
    }

    // Constructor which sets key to the argument.
    RedBlackNode(T key) {
        this();
        this.key = key;
    }
}


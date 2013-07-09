/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtea.extensions.algorithms.structures;

import graphtea.graph.graph.GraphPoint;
import graphtea.graph.graph.Vertex;
import graphtea.graph.old.GShape;

/**
 *
 * @author Mohsen
 */
public class SegmentNode {

    public RedBlackNode<LineSegment> treeNode;
    public boolean isRightChild = false;
    public Vertex treePoint;
    int level = 0;
    public static final double TREE_LEVEL_DISTANCE = 50;
    public static final double INITIAL_X_DISTANCE = 75;
    public static final double Y_OFFSET = 150;

    public SegmentNode(RedBlackNode<LineSegment> parentNode, boolean isRightChild) {
        this.treeNode = parentNode;
        this.isRightChild = isRightChild;

        this.treePoint = new Vertex();
        this.treePoint.setShape(GShape.ROUNDRECT);
        this.treePoint.setSize(new GraphPoint(37, 25));
        this.treePoint.setLabelLocation(new GraphPoint(4, 0));
        this.treePoint.setColor(5);

        this.treePoint.setLabel(parentNode.key.toString());
    }

    public void setLocation(SegmentNode parent) {
        if (parent == null) {
            this.level = 0;
            // Location must be set independently 
        } else {
            this.level = parent.getLevel() + 1;
            this.treePoint.setLocation(
                    new GraphPoint(
                    parent.treePoint.getLocation().x
                    + (isRightChild ? 1 : -1) * (INITIAL_X_DISTANCE / this.level),
                    Y_OFFSET + this.level * TREE_LEVEL_DISTANCE));
        }
    }

    public int getLevel() {
        return this.level;
    }
}

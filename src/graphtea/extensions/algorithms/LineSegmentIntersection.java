/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtea.extensions.algorithms;

import graphtea.extensions.algorithms.utilities.CGUtil;
import graphtea.extensions.algorithms.structures.RedBlackNode;
import graphtea.extensions.algorithms.structures.LineSegment;
import graphtea.extensions.algorithms.structures.RedBlackTree;
import graphtea.extensions.algorithms.structures.LSVertex;
import graphtea.graph.graph.Edge;
import graphtea.graph.graph.GraphModel;
import graphtea.graph.graph.GraphPoint;
import graphtea.graph.graph.Vertex;
import graphtea.graph.old.GShape;
import graphtea.platform.core.BlackBoard;
import graphtea.plugins.algorithmanimator.core.GraphAlgorithm;
import graphtea.plugins.algorithmanimator.extension.AlgorithmExtension;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Mohsen Mansouryar (mansouryar@cs.sharif.edu)
 * @author Saman Jahangiri (saman_jahangiri@math.sharif.edu)
 */
public class LineSegmentIntersection extends GraphAlgorithm implements AlgorithmExtension {

    public LineSegmentIntersection(BlackBoard bBoard) {
        super(bBoard);
    }

    public void doAlgorithm() {
        step("Computing intersection points of a set of line "
                + "segments represented as a graph. Developed by"
                + " Saman Jahangiri and Mohsen Mansouryar (mmbrian)");


        // Getting graph model (which is currently a set of points)
        GraphModel g = graphData.getGraph();

        // Computing all line segments and adding them to a set.
        HashSet<LineSegment> lineSegments = new HashSet<LineSegment>();
        for (Edge edge : g.getEdges()) {
            lineSegments.add(new LineSegment(edge.source, edge.target));
        }

        step(lineSegments.size() + " line segments detected.\nComputing initial event queue...");

        double minX = Double.MAX_VALUE, maxX = 0;

        // Adding line segment endpoints to eventQueue
        RedBlackTree<LSVertex> eventQueue = new RedBlackTree<LSVertex>();
        for (LineSegment ls : lineSegments) {
            eventQueue.insert(ls.p);
            eventQueue.insert(ls.v);

            if (ls.p.getLocation().x > maxX) {
                maxX = ls.p.getLocation().x;
            } else if (ls.p.getLocation().x < minX) {
                minX = ls.p.getLocation().x;
            }
            if (ls.v.getLocation().x > maxX) {
                maxX = ls.v.getLocation().x;
            } else if (ls.v.getLocation().x < minX) {
                minX = ls.v.getLocation().x;
            }
        }

        String node_set = "";

        for (RedBlackNode<LSVertex> node : eventQueue.inOrder()) {
            node_set += node.key.getLabel().toString() + " ";
        }
        step("Initial event queue consists of all endpoints from top to bottom in this order:\n" + node_set);

        RedBlackTree<LineSegment> status = new RedBlackTree<LineSegment>();
        double sweepLineY = 0;
        LSVertex iNode; // used to hold an intersection point

        ////////////////////////////////////////////////////////
        Vertex sweepLineLeft = new Vertex();
        sweepLineLeft.setLocation(new GraphPoint(minX - 45, -50));
        sweepLineLeft.setLabel("L");
        sweepLineLeft.setShape(GShape.RIGHTWARDTRIANGLE);
        sweepLineLeft.setSize(new GraphPoint(35, 35));
        sweepLineLeft.setColor(2);
        g.insertVertex(sweepLineLeft);

        Vertex sweepLineRight = new Vertex();
        sweepLineRight.setLocation(new GraphPoint(maxX + 45, -50));
        sweepLineRight.setLabel("R");
        sweepLineRight.setShape(GShape.LEFTWARDTTRIANGLE);
        sweepLineRight.setSize(new GraphPoint(35, 35));
        sweepLineRight.setColor(2);
        g.insertVertex(sweepLineRight);

        g.insertEdge(sweepLineLeft, sweepLineRight);
        ////////////////////////////////////////////////////////

        ArrayList<Vertex> intersectionPts = new ArrayList<Vertex>();
        // Main loop. sweeping over all event points from top to bottom
        while (eventQueue.size() > 0) {
            step("Processing next event point...");
            RedBlackNode<LSVertex> currEvent = eventQueue.treeMinimum(eventQueue.getRoot());

            sweepLineY = currEvent.key.getLocation().y;
            sweepLineLeft.setLocation(new GraphPoint(sweepLineLeft.getLocation().x, sweepLineY));
            sweepLineRight.setLocation(new GraphPoint(sweepLineRight.getLocation().x, sweepLineY));

            step("Found node " + currEvent.key.getLabel() + ".\nsweep line is at y = " + sweepLineY);
            currEvent.key.setMark(true);

            if (currEvent.key.isIntersectionPoint) { // Intersection Point
                intersectionPts.add(currEvent.key);
                // Reporting intersection point
                step("Intersection point of line segments " + currEvent.key.fisrtSegment.toString() + " & " + currEvent.key.secondSegment.toString());

                ////////////////////////////////////////////////////////
                Vertex intersectionNode = new Vertex();
                intersectionNode.setSize(new GraphPoint(10, 10));
                intersectionNode.setLocation(new GraphPoint(currEvent.key.getLocation().x, sweepLineY));
                intersectionNode.setColor(5);
                intersectionNode.setLabel("");
                g.insertVertex(intersectionNode);
                ////////////////////////////////////////////////////////

                RedBlackNode<LineSegment> firstSegmentNode = status.search(currEvent.key.fisrtSegment);
                RedBlackNode<LineSegment> secondSegmentNode = status.search(currEvent.key.secondSegment);

                RedBlackNode<LineSegment> successor_1 = status.treeSuccessor(firstSegmentNode);
                RedBlackNode<LineSegment> predecessor_1 = status.treePredecessor(firstSegmentNode);
                RedBlackNode<LineSegment> successor_2 = status.treeSuccessor(secondSegmentNode);
                RedBlackNode<LineSegment> predecessor_2 = status.treePredecessor(secondSegmentNode);

                // Swapping the two intersected segments
                status.swapNodes(firstSegmentNode, secondSegmentNode);
//                firstSegmentNode.key.switchCompareNode();
//                secondSegmentNode.key.switchCompareNode();
                for (RedBlackNode<LineSegment> node : status.inOrder()) {
                    node.key.updateCompareNode(sweepLineY, 1);
                }
                step("Swapped the two line segments in status tree.");
//                step("status root is " + status.getRoot().key.toString());
//                if (!status.isNil(status.getRoot().left)) {
//                    step("status root's left child is " + status.getRoot().left.key.toString());
//                } else {
//                    step("status root's left child is nil");
//                }
//                if (!status.isNil(status.getRoot().right)) {
//                    step("status root's right child is " + status.getRoot().right.key.toString());
//                } else {
//                    step("status root's right child is nil");
//                }

                Vertex firstSegmentIP1 = null, firstSegmentIP2 = null,
                        secondSegmentIP1 = null, secondSegmentIP2 = null;
                // Checking firstSegment intersection with its successor and predecessor
                // Checking secondSegment intersection with its successor and predecessor
                // Adding intersection vertices ONLY if they are below the sweep line
                if (!status.isNil(predecessor_1)) {
                    secondSegmentIP1 = CGUtil.doIntersect(currEvent.key.secondSegment, predecessor_1.key);
                }
                if (!status.isNil(successor_1)) {
                    secondSegmentIP2 = CGUtil.doIntersect(currEvent.key.secondSegment, successor_1.key);
                }
                if (!status.isNil(predecessor_2)) {
                    firstSegmentIP1 = CGUtil.doIntersect(currEvent.key.fisrtSegment, predecessor_2.key);
                }
                if (!status.isNil(successor_2)) {
                    firstSegmentIP2 = CGUtil.doIntersect(currEvent.key.fisrtSegment, successor_2.key);
                }


                if (firstSegmentIP1 != null && firstSegmentIP1.getLocation().y > sweepLineY) {
                    iNode = new LSVertex(firstSegmentIP1, currEvent.key.fisrtSegment, predecessor_2.key);
                    if (eventQueue.search(iNode) == null) {
                        eventQueue.insert(iNode);
                    } else {
                        step("****************************\nIntersection point " + iNode.getLabel() + " detected. but it was already in event queue.\n****************************");
                    }
                }
                if (firstSegmentIP2 != null && firstSegmentIP2.getLocation().y > sweepLineY) {
                    iNode = new LSVertex(firstSegmentIP2, currEvent.key.fisrtSegment, successor_2.key);
                    if (eventQueue.search(iNode) == null) {
                        eventQueue.insert(iNode);
                    } else {
                        step("****************************\nIntersection point " + iNode.getLabel() + " detected. but it was already in event queue.\n****************************");
                    }
                }
                if (secondSegmentIP1 != null && secondSegmentIP1.getLocation().y > sweepLineY) {
                    iNode = new LSVertex(secondSegmentIP1, currEvent.key.secondSegment, predecessor_1.key);
                    if (eventQueue.search(iNode) == null) {
                        eventQueue.insert(iNode);
                    } else {
                        step("****************************\nIntersection point " + iNode.getLabel() + " detected. but it was already in event queue.\n****************************");
                    }
                }
                if (secondSegmentIP2 != null && secondSegmentIP2.getLocation().y > sweepLineY) {
                    iNode = new LSVertex(secondSegmentIP2, currEvent.key.secondSegment, successor_1.key);
                    if (eventQueue.search(iNode) == null) {
                        eventQueue.insert(iNode);
                    } else {
                        step("****************************\nIntersection point " + iNode.getLabel() + " detected. but it was already in event queue.\n****************************");
                    }
                }

            } else {


                LSVertex neighbor = currEvent.key.getNeighbor();
//                step("Neighbor is " + neighbor.getLabel());
                if (neighbor.getLocation().y > currEvent.key.getLocation().y) { // First Point (segment is not already in status tree)

                    for (RedBlackNode<LineSegment> node : status.inOrder()) {
                        node.key.updateCompareNode(sweepLineY, 0);
                    }

                    // Inserting the LineSegment holding this endpoint to status tree
                    status.insert(currEvent.key.parent);
                    step("Line segment " + currEvent.key.parent.toString() + " was added to status tree.");
//                    step("status root is " + status.getRoot().key.toString());
//                    if (!status.isNil(status.getRoot().left)) {
//                        step("status root's left child is " + status.getRoot().left.key.toString());
//                    } else {
//                        step("status root's left child is nil");
//                    }
//                    if (!status.isNil(status.getRoot().right)) {
//                        step("status root's right child is " + status.getRoot().right.key.toString());
//                    } else {
//                        step("status root's right child is nil");
//                    }
                    RedBlackNode<LineSegment> parentSegment = status.search(currEvent.key.parent);

//                    step("parent segment is " + parentSegment.key.toString());

                    RedBlackNode<LineSegment> successor = status.treeSuccessor(parentSegment);
                    RedBlackNode<LineSegment> predecessor = status.treePredecessor(parentSegment);

                    if (!status.isNil(successor)) {
                        // Checking for possible intersection
                        Vertex rightIntersection = CGUtil.doIntersect(currEvent.key.parent, successor.key);
                        if (rightIntersection != null && rightIntersection.getLocation().y > sweepLineY) {
                            iNode = new LSVertex(rightIntersection, currEvent.key.parent, successor.key);
                            if (eventQueue.search(iNode) == null) {
                                eventQueue.insert(iNode);
                            } else {
                                step("****************************\nIntersection point " + iNode.getLabel() + " detected. but it was already in event queue.\n****************************");
                            }
                        }

                    }

                    if (!status.isNil(predecessor)) {
                        // Checking for possible intersection
                        Vertex leftIntersection = CGUtil.doIntersect(currEvent.key.parent, predecessor.key);
                        if (leftIntersection != null && leftIntersection.getLocation().y > sweepLineY) {
                            iNode = new LSVertex(leftIntersection, currEvent.key.parent, predecessor.key);
                            if (eventQueue.search(iNode) == null) {
                                eventQueue.insert(iNode);
                            } else {
                                step("****************************\nIntersection point " + iNode.getLabel() + " detected. but it was already in event queue.\n****************************");
                            }
                        }
                    }

                } else { // Last Point

                    for (RedBlackNode<LineSegment> node : status.inOrder()) {
                        node.key.updateCompareNode(sweepLineY, 0);
                    }

                    step("Removing line segment " + currEvent.key.parent.toString() + " from status tree.");
//                    step("Parent is " + currEvent.key.parent.toString());
//                    step("status root is " + status.getRoot().key.toString());
//                    if (!status.isNil(status.getRoot().left)) {
//                        step("status root's left child is " + status.getRoot().left.key.toString());
//                    } else {
//                        step("status root's left child is nil");
//                    }
//                    if (!status.isNil(status.getRoot().right)) {
//                        step("status root's right child is " + status.getRoot().right.key.toString());
//                    } else {
//                        step("status root's right child is nil");
//                    }
                    RedBlackNode<LineSegment> parentSegment = status.search(currEvent.key.parent);

//                    step("Removing segment " + parentSegment.key.toString());
                    RedBlackNode<LineSegment> successor = status.treeSuccessor(parentSegment);
                    RedBlackNode<LineSegment> predecessor = status.treePredecessor(parentSegment);

                    if (!status.isNil(successor) && !status.isNil(predecessor)) {
                        // Checking for possible intersection
                        Vertex intersection = CGUtil.doIntersect(successor.key, predecessor.key);
                        if (intersection != null && intersection.getLocation().y > sweepLineY) {
                            iNode = new LSVertex(intersection, successor.key, predecessor.key);
                            if (eventQueue.search(iNode) == null) {
                                eventQueue.insert(iNode);
                            } else {
                                step("****************************\nIntersection point " + iNode.getLabel() + " detected. but it was already in event queue.\n****************************");
                            }
                        }
                    }

                    // Deleting the LineSegment holding this endpoint from status
                    status.remove(parentSegment);
//                    step(status.size() + " segments left.");
                }
            }

            currEvent.key.setMark(false);
            int size_before = eventQueue.size();
            eventQueue.remove(currEvent);
            int size_after = eventQueue.size();
            step("Removed " + currEvent.key.getLabel() + " from event queue. ");
            if (size_after == size_before) {
                break;
            }
        }

        step("Intersection points were:\n");
        String s = "";
        for (Vertex intersection : intersectionPts) {
            s += intersection.getLabel() + "\n";
        }
        step(s);
        step("Finished sweep line algorithm!");
    }

    public String getName() {
        return "Line Segment Intersection";
    }

    public String getDescription() {
        return "Line Segment Intersection Description";
    }
}

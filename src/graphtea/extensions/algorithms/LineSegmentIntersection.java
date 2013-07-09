/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtea.extensions.algorithms;

import graphtea.extensions.algorithms.utilities.CGUtil;
import graphtea.extensions.algorithms.structures.RedBlackNode;
import graphtea.extensions.algorithms.structures.LineSegment;
import graphtea.extensions.algorithms.structures.RedBlackTree;
import graphtea.extensions.algorithms.structures.SegmentNode;
import graphtea.extensions.algorithms.structures.SweepLine;
import graphtea.extensions.algorithms.utilities.CGAlgorithm;
import graphtea.extensions.algorithms.utilities.UIWrapper;
import graphtea.graph.graph.Edge;
import graphtea.graph.graph.GraphModel;
import graphtea.graph.graph.GraphPoint;
import graphtea.graph.graph.Vertex;
import graphtea.graph.old.GShape;
import graphtea.platform.core.BlackBoard;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

/**
 *
 * @author Mohsen Mansouryar (mansouryar@cs.sharif.edu)
 * @author Saman Jahangiri (saman_jahangiri@math.sharif.edu)
 */
public class LineSegmentIntersection extends CGAlgorithm {

    ArrayList<Vertex> statusTreeVertices;    
    ArrayList<Vertex> algorithmElements;

    public LineSegmentIntersection(BlackBoard bBoard) {
        super(bBoard);
    }

    public void doAlgorithm() {
        step("Computing intersection points of a set of line "
                + "segments represented as a graph. Developed by"
                + " Saman Jahangiri and Mohsen Mansouryar (mmbrian)");

        algorithmElements = new ArrayList<Vertex>();
        // Getting graph model (which is currently a set of points)
        GraphModel g = graphData.getGraph();

        Rectangle boundingBox = new Rectangle(50, 50, 640, 640);
        Rectangle statusTreeBoundingBox = new Rectangle(50 + 640 + 10, 50, 300, 400);

        GraphModel userInputBounds = UIWrapper.createBoundingBox("Input Panel", boundingBox);
        GraphModel statusTreeBounds = UIWrapper.createBoundingBox("Status Tree", statusTreeBoundingBox);

        g.addSubGraph(statusTreeBounds, statusTreeBoundingBox);
        g.addSubGraph(userInputBounds, boundingBox);
        
        for (Vertex v: userInputBounds.vertices())
            algorithmElements.add(v);
        for (Vertex v: statusTreeBounds.vertices())
            algorithmElements.add(v);

        Vertex start = UIWrapper.createStartButton(boundingBox);
        g.insertVertex(start);
        algorithmElements.add(start);

        Vertex tmp = requestVertex(g, "Draw your line segments & press '" + UIWrapper.START_BUTTON_LABEL + "' button when you are ready.");
        while (!tmp.getLabel().equals(start.getLabel())) {
            tmp = requestVertex(g, "Draw your line segments & press '" + UIWrapper.START_BUTTON_LABEL + "' button when you are ready.");
        }
        start.setSelected(false);

        g = graphData.getGraph();
        // Computing all line segments and adding them to a set.
        HashSet<LineSegment> lineSegments = new HashSet<LineSegment>();
        for (Edge edge : g.getEdges()) {
            if (!edge.source.getLabel().equals(UIWrapper.CORNER_VERTEX_LABEL)) {
                lineSegments.add(new LineSegment(edge.source, edge.target));
            }
        }

        step(lineSegments.size() + " line segments detected.\nComputing initial event queue...");

        double minX = Double.MAX_VALUE, maxX = 0;

        // Adding line segment endpoints to eventQueue
        RedBlackTree<LineSegment.Vertex> eventQueue = new RedBlackTree<LineSegment.Vertex>();
        for (LineSegment ls : lineSegments) {
            eventQueue.insert(ls.p);
            eventQueue.insert(ls.v);

//            if (ls.p.getLocation().x > maxX) {
//                maxX = ls.p.getLocation().x;
//            } else if (ls.p.getLocation().x < minX) {
//                minX = ls.p.getLocation().x;
//            }
//            if (ls.v.getLocation().x > maxX) {
//                maxX = ls.v.getLocation().x;
//            } else if (ls.v.getLocation().x < minX) {
//                minX = ls.v.getLocation().x;
//            }
            maxX = boundingBox.getMaxX();
            minX = boundingBox.getMinX();

        }

        String node_set = "";

        for (RedBlackNode<LineSegment.Vertex> node : eventQueue.inOrder()) {
            node_set += node.key.getLabel().toString() + " ";
        }
        step("Initial event queue consists of all endpoints from top to bottom in this order:\n" + node_set);

        RedBlackTree<LineSegment> status = new RedBlackTree<LineSegment>();
        LineSegment.Vertex iNode; // used to hold an intersection point

        ////////////////////////////////////////////////////////
        SweepLine sl = new SweepLine(minX, maxX, g);
        ////////////////////////////////////////////////////////

        ArrayList<Vertex> intersectionPts = new ArrayList<Vertex>();
        // Main loop. sweeping over all event points from top to bottom
        while (eventQueue.size() > 0) {
            step("Processing next event point...");

            RedBlackNode<LineSegment.Vertex> currEvent = eventQueue.treeMinimum(eventQueue.getRoot());

            sl.updateY(currEvent.key.getLocation().y);
            
            step("Found node " + currEvent.key.getLabel() + ".\nsweep line is at y = " + sl.getY());
            currEvent.key.setMark(true);

            if (currEvent.key.isIntersectionPoint) { // Intersection Point
                intersectionPts.add(currEvent.key);
                // Reporting intersection point
                step("Intersection point of line segments " + currEvent.key.fisrtSegment.toString() + " & " + currEvent.key.secondSegment.toString());

                ////////////////////////////////////////////////////////
                Vertex intersectionNode = new Vertex();
                intersectionNode.setSize(new GraphPoint(10, 10));
                intersectionNode.setLocation(new GraphPoint(currEvent.key.getLocation().x, sl.getY()));
                intersectionNode.setColor(5);
                intersectionNode.setLabel("");
                g.insertVertex(intersectionNode);
                algorithmElements.add(intersectionNode);
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
                    node.key.updateCompareNode(sl.getY(), 1);
                }

                // *********************************************************************
//                drawStatusTree(g, maxX + 75, 300, 300, status);
                drawStatusTree(statusTreeBoundingBox, status);
                // *********************************************************************
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


                if (firstSegmentIP1 != null && firstSegmentIP1.getLocation().y > sl.getY()) {
                    iNode = new LineSegment.Vertex(firstSegmentIP1, currEvent.key.fisrtSegment, predecessor_2.key);
                    if (eventQueue.search(iNode) == null) {
                        eventQueue.insert(iNode);
                    } else {
                        step("****************************\nIntersection point " + iNode.getLabel() + " detected. but it was already in event queue.\n****************************");
                    }
                }
                if (firstSegmentIP2 != null && firstSegmentIP2.getLocation().y > sl.getY()) {
                    iNode = new LineSegment.Vertex(firstSegmentIP2, currEvent.key.fisrtSegment, successor_2.key);
                    if (eventQueue.search(iNode) == null) {
                        eventQueue.insert(iNode);
                    } else {
                        step("****************************\nIntersection point " + iNode.getLabel() + " detected. but it was already in event queue.\n****************************");
                    }
                }
                if (secondSegmentIP1 != null && secondSegmentIP1.getLocation().y > sl.getY()) {
                    iNode = new LineSegment.Vertex(secondSegmentIP1, currEvent.key.secondSegment, predecessor_1.key);
                    if (eventQueue.search(iNode) == null) {
                        eventQueue.insert(iNode);
                    } else {
                        step("****************************\nIntersection point " + iNode.getLabel() + " detected. but it was already in event queue.\n****************************");
                    }
                }
                if (secondSegmentIP2 != null && secondSegmentIP2.getLocation().y > sl.getY()) {
                    iNode = new LineSegment.Vertex(secondSegmentIP2, currEvent.key.secondSegment, successor_1.key);
                    if (eventQueue.search(iNode) == null) {
                        eventQueue.insert(iNode);
                    } else {
                        step("****************************\nIntersection point " + iNode.getLabel() + " detected. but it was already in event queue.\n****************************");
                    }
                }

            } else {


                LineSegment.Vertex neighbor = currEvent.key.getNeighbor();
//                step("Neighbor is " + neighbor.getLabel());
                if (neighbor.getLocation().y > currEvent.key.getLocation().y) { // First Point (segment is not already in status tree)

                    for (RedBlackNode<LineSegment> node : status.inOrder()) {
                        node.key.updateCompareNode(sl.getY(), 0);
                    }

                    // Inserting the LineSegment holding this endpoint to status tree
                    status.insert(currEvent.key.parent);

                    // *********************************************************************
//                    drawStatusTree(g, maxX + 75, 300, 300, status);
                    drawStatusTree(statusTreeBoundingBox, status);
                    // *********************************************************************
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
                        if (rightIntersection != null && rightIntersection.getLocation().y > sl.getY()) {
                            iNode = new LineSegment.Vertex(rightIntersection, currEvent.key.parent, successor.key);
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
                        if (leftIntersection != null && leftIntersection.getLocation().y > sl.getY()) {
                            iNode = new LineSegment.Vertex(leftIntersection, currEvent.key.parent, predecessor.key);
                            if (eventQueue.search(iNode) == null) {
                                eventQueue.insert(iNode);
                            } else {
                                step("****************************\nIntersection point " + iNode.getLabel() + " detected. but it was already in event queue.\n****************************");
                            }
                        }
                    }

                } else { // Last Point

                    for (RedBlackNode<LineSegment> node : status.inOrder()) {
                        node.key.updateCompareNode(sl.getY(), 0);
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
                        if (intersection != null && intersection.getLocation().y > sl.getY()) {
                            iNode = new LineSegment.Vertex(intersection, successor.key, predecessor.key);
                            if (eventQueue.search(iNode) == null) {
                                eventQueue.insert(iNode);
                            } else {
                                step("****************************\nIntersection point " + iNode.getLabel() + " detected. but it was already in event queue.\n****************************");
                            }
                        }
                    }

                    // Deleting the LineSegment holding this endpoint from status
                    status.remove(parentSegment);
                    // *********************************************************************
//                    drawStatusTree(g, maxX + 75, 300, 300, status);
                    drawStatusTree(statusTreeBoundingBox, status);
                    // *********************************************************************
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

        algorithmElements.addAll(statusTreeVertices);
        
        Vertex clear = UIWrapper.createClearButton(boundingBox);
        g.insertVertex(clear);
        algorithmElements.add(clear);
        while (!tmp.getLabel().equals(clear.getLabel())) {
            tmp = requestVertex(g, "Press" + UIWrapper.CLEAR_BUTTON_LABEL + "' "
                    + "button to remove algorithm specific elements. "
                    + "Your input will be preserved.");
        }
        eraseAlgorithmElements();
 
    }

    public String getName() {
        return "Line Segment Intersection";
    }

    public String getDescription() {
        return "Line Segment Intersection Description";
    }

    public void eraseAlgorithmElements() {
        GraphModel g = graphData.getGraph();
        for (Vertex v: algorithmElements)
            g.removeVertex(v);
        
        statusTreeVertices = new ArrayList<Vertex>();
    }
    
    public void eraseStatusTree() {
        GraphModel g = graphData.getGraph();

        if (statusTreeVertices != null) {

            for (Vertex v : statusTreeVertices) {
                g.removeVertex(v);
            }
        }
//        g.repaint();
    }

    public void drawStatusTree(Rectangle boundingBox, RedBlackTree<LineSegment> status) {
        if (status.isNil(status.getRoot())) return;
        
        GraphModel g = graphData.getGraph();
        eraseStatusTree();

        GraphModel statusTree = new GraphModel(false);

        statusTreeVertices = new ArrayList<Vertex>();

        SegmentNode root = new SegmentNode(status.getRoot(), true);
        root.setLocation(null); // Required to initialize level
//        root.treePoint.setLocation(new GraphPoint(maxX + xbound / 2, SegmentNode.Y_OFFSET));
        root.treePoint.setLocation(new GraphPoint(boundingBox.width / 2, 0));
        statusTreeVertices.add(root.treePoint);
//        g.addVertex(root.treePoint);
        statusTree.insertVertex(root.treePoint);

        Stack<SegmentNode> segments = new Stack<SegmentNode>();
        if (!status.isNil(status.getRoot().left)) {
            SegmentNode left = new SegmentNode(status.getRoot().left, false);
            left.setLocation(root);
            segments.push(left);
            
//            g.addVertex(left.treePoint);
            statusTree.insertVertex(left.treePoint);
            statusTreeVertices.add(left.treePoint);
            statusTree.insertEdge(root.treePoint, left.treePoint);
        }
        if (!status.isNil(status.getRoot().right)) {
            SegmentNode right = new SegmentNode(status.getRoot().right, true);
            right.setLocation(root);
            segments.push(right);

//            g.addVertex(right.treePoint);
            statusTree.insertVertex(right.treePoint);
            statusTreeVertices.add(right.treePoint);
            statusTree.insertEdge(root.treePoint, right.treePoint);
        }

        SegmentNode next;
        while (!segments.empty()) {
            next = segments.pop();

            if (!status.isNil(next.treeNode.left)) {
                SegmentNode left = new SegmentNode(next.treeNode.left, false);
                left.setLocation(next);
                segments.push(left);

//                g.addVertex(left.treePoint);
                statusTree.insertVertex(left.treePoint);
                statusTreeVertices.add(left.treePoint);
                statusTree.insertEdge(next.treePoint, left.treePoint);
            }
            if (!status.isNil(next.treeNode.right)) {
                SegmentNode right = new SegmentNode(next.treeNode.right, true);
                right.setLocation(next);
                segments.push(right);

//                g.addVertex(right.treePoint);
                statusTree.insertVertex(right.treePoint);
                statusTreeVertices.add(right.treePoint);
                statusTree.insertEdge(next.treePoint, right.treePoint);
            }
        }

        g.addSubGraph(statusTree, boundingBox);
//        
//        g.repaint();
    }
}

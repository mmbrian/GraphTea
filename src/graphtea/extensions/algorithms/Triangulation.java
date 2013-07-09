package graphtea.extensions.algorithms;

import graphtea.extensions.algorithms.structures.DCEL;
import graphtea.extensions.algorithms.structures.LineSegment;
import graphtea.extensions.algorithms.structures.RedBlackNode;
import graphtea.extensions.algorithms.structures.RedBlackTree;
import graphtea.extensions.algorithms.structures.SweepLine;
import graphtea.extensions.algorithms.utilities.CGAlgorithm;
import graphtea.extensions.algorithms.utilities.CGUtil;
import graphtea.graph.graph.GraphModel;
import graphtea.graph.graph.GraphPoint;
import graphtea.graph.graph.Vertex;
import graphtea.platform.core.BlackBoard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

/**
 * @author Mohsen Mansouryar (mansouryar@cs.sharif.edu)
 * @author Saman Jahangiri (saman_jahangiri@math.sharif.edu)
 */
public class Triangulation extends CGAlgorithm {

    public Triangulation(BlackBoard blackBoard) {
        super(blackBoard);
    }

    @Override
    public void doAlgorithm() {
        step("Computing triangulation of a simple polygon."
                + " Developed by"
                + " Mohsen Mansouryar (mmbrian) & Saman Jahangiri");

        GraphModel g = graphData.getGraph();
        DCEL dcel = CGUtil.FindDCEL(g);

        // Creating event queue (since it doesn't need to change, we use a sorted arraylist)
        ArrayList<DCEL.Vertex> eventQueue = new ArrayList<DCEL.Vertex>();
        eventQueue.addAll(dcel.getVertexList());
        Collections.sort(eventQueue);
      
        DCEL.Vertex first = eventQueue.get(0);
        DCEL.Vertex next = null,
                prev = null;
        DCEL.HalfEdge edge;
        // Finding the inner halfedge that leaves first
         
//        // This code doesn't work since isOuter is not propertly set (always false)       
//        if (first.getHalfEdge().getFace().isOuter()) {
//            edge = first.getHalfEdge().getTwin().getNext();
//        } else {
//            System.out.println("associated halfedge was inner");
//            edge = first.getHalfEdge();
//        }
        edge = first.getHalfEdge();

        ArrayList<GraphPoint> points = new ArrayList<GraphPoint>();
        points.add(first.getP());

        while (!first.equals(next)) {
            edge = edge.getNext();
            next = edge.getOrigin();
            points.add(next.getP());
        }

        if (CGUtil.isPolygonClockwise(points)) {
            edge = first.getHalfEdge().getTwin().getNext();
        } else {
            edge = first.getHalfEdge();
        }

        // For each dcel vertex, this hashmap holds the next vertex in the CC
        // ordering (according to book, for Vi, it holds the other end of Ei)
        HashMap<DCEL.Vertex, DCEL.Vertex> ccNeighbour = new HashMap<DCEL.Vertex, DCEL.Vertex>();
        next = null;
        prev = first;
        while (!first.equals(next)) {
            edge = edge.getNext();
            next = edge.getOrigin();

            ccNeighbour.put(prev, next);
            prev = next;
        }

        RedBlackTree<LineSegment> status = new RedBlackTree<LineSegment>();

        // This is the status tree used in handling methods. it doesn't contain
        // all segments that intersect the sweep line.
        RedBlackTree<LineSegment> tStatus = new RedBlackTree<LineSegment>();

        
        double minX = Double.MAX_VALUE, maxX = 0;
        for (DCEL.Vertex v : eventQueue) {
            if (v.getP().x > maxX) {
                maxX = v.getP().x;
            } else if (v.getP().x < minX) {
                minX = v.getP().x;
            }
        }
        
        SweepLine sl = new SweepLine(minX, maxX, g);
        
        while (!eventQueue.isEmpty()) {

            DCEL.Vertex curr = eventQueue.get(0);
            for (RedBlackNode<LineSegment> node : status.inOrder()) {
                // This is required for status tree to work!
                node.key.updateCompareNode(curr.getP().getY(), 1);
            }
            for (RedBlackNode<LineSegment> node : tStatus.inOrder()) {
                // This is required for status tree to work!
                node.key.updateCompareNode(curr.getP().getY(), 1);
            }

            // Getting the two neighbours from the polygon
            DCEL.Vertex[] neighbours = getVertexNeighbours(curr);
            Vertex v = getGraphVertex(curr, g),
                    u = getGraphVertex(neighbours[0], g),
                    z = getGraphVertex(neighbours[1], g);
//            Edge e1 = g.getEdge(u, v),
//                 e2 = g.getEdge(v, z);
            
            sl.updateY(curr.getP().y);
            


            LineSegment ls1 = new LineSegment(v, u), // ls1 = vu is associated with neighbours[0]
                    ls2 = new LineSegment(v, z); // ls2 = vz is associated with neighbours[1]
            ls1.updateCompareNode(curr.getP().getY(), 1);
            ls2.updateCompareNode(curr.getP().getY(), 1);

            LineSegment eiKey, eim1Key;
            if (ccNeighbour.get(curr).equals(neighbours[0])) { // vu is ei, vz is ei-1
                eiKey = ls1;
                eim1Key = ls2;
            } else { // vice versa
                eiKey = ls2;
                eim1Key = ls1;
            }

            RedBlackNode<LineSegment> seg1 = status.search(ls1),
                    seg2 = status.search(ls2);
            if (status.isNil(seg1) || seg1 == null) { // If not found > add it to the status tree
                status.insert(ls1);
            } else {
                status.remove(seg1);
            }

            if (status.isNil(seg2) || seg2 == null) { // If not found > add it to the status tree
                status.insert(ls2);
            } else {
                status.remove(seg2);
            }

            int type = getVertexType(curr, dcel, status, g);
            try {
                switch (type) {
                    case 0: {
                        System.out.println("Vertex " + v.getLabel() + " is Start");
                        step("Sweep line reached vertex " + v.getLabel() + " > Start");
                        handleStartVertex(curr, tStatus, eiKey);
                        break;
                    }
                    case 1: {
                        System.out.println("Vertex " + v.getLabel() + " is Split");
                        step("Sweep line reached vertex " + v.getLabel() + " > Split");
                        handleSplitVertex(curr, dcel, tStatus, eiKey, g);
                        break;
                    }
                    case 2: {
                        System.out.println("Vertex " + v.getLabel() + " is End");
                        step("Sweep line reached vertex " + v.getLabel() + " > End");
                        handleEndVertex(curr, dcel, tStatus, status, eim1Key, g);
                        break;
                    }
                    case 3: {
                        System.out.println("Vertex " + v.getLabel() + " is Merge");
                        step("Sweep line reached vertex " + v.getLabel() + " > Merge");
                        handleMergeVertex(curr, dcel, tStatus, status, eiKey, eim1Key, g);
                        break;
                    }
                    case 4: {
                        System.out.println("Vertex " + v.getLabel() + " is Regular");
                        step("Sweep line reached vertex " + v.getLabel() + " > Regular");
                        handleRegularVertex(curr, dcel, tStatus, status, eiKey, eim1Key, g);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            eventQueue.remove(curr);
        }

        step("Finished splitting polygon into y-monotones");
        sl.destroy(g);
        dcel = CGUtil.FindDCEL(g);
//        int c = 1;
//        for (DCEL.Face face : dcel.getFaceList()) {
//            step("Coloring Nodes in Faces");
//            for (DCEL.HalfEdge he : face.getInnerComponent()) {
//                Vertex v = getGraphVertex(he.getOrigin(), g);
////                v.setColor(c);
//                v.setSelected(true);
//            }
//            step("");
//            for (DCEL.HalfEdge he : face.getInnerComponent()) {
//                Vertex v = getGraphVertex(he.getOrigin(), g);
//                v.setSelected(false);
//            }
//            step("");
//            c++;
//        }

        step("Begin triangulating y-monotones");
        for (int i = 0; i < dcel.getFaceList().size(); i++) { // skip 1st face
            DCEL.Face face = dcel.getFaceList().get(i);
            if (face.getInnerComponent().size() == dcel.getVertexList().size()) {
                continue;
            }
            
            for (DCEL.HalfEdge he : face.getInnerComponent()) {
                Vertex v = getGraphVertex(he.getOrigin(), g);
                v.setSelected(true);
            }
            step("Triangulating selected face");
            
            double minY = 10000, maxY = 0;
            int minYind = -1, maxYind = -1;
            ArrayList<GraphPoint> fPoints = new ArrayList<GraphPoint>();
            for (int j = 0; j < face.getInnerComponent().size(); j++) {
                DCEL.Vertex v = face.getInnerComponent().get(j).getOrigin();
                if (v.getP().y < minY) {
                    minY = v.getP().y;
                    minYind = j;
                }
                if (v.getP().y > maxY) {
                    maxY = v.getP().y;
                    maxYind = j;
                }
                fPoints.add(v.getP());
            }
            boolean isClockwise = CGUtil.isPolygonClockwise(fPoints);
            int chainId = (isClockwise) ? 1:-1; // 1 means right chain
            
            if (maxYind < minYind) {
                int tmp = maxYind;
                maxYind = minYind;
                minYind = tmp;
            }
            ArrayList<DCEL.Vertex> nodes = new ArrayList<DCEL.Vertex>();
            for (int j = 0; j < face.getInnerComponent().size(); j++) {
                DCEL.Vertex v = face.getInnerComponent().get(j).getOrigin();
                if (j >= minYind && j < maxYind) {
                    v.data = chainId; // chain id  
                } else {
                    v.data = chainId * -1;
                }

                nodes.add(v);
            }
            Collections.sort(nodes); // list of all vertices in sorted y order
            // left chain & right chain are merged in nodes & each node v in 
            // nodes holds its chain id

            Stack<DCEL.Vertex> s = new Stack<DCEL.Vertex>();
            s.push(nodes.get(0));
            s.push(nodes.get(1));
            for (int j = 2; j < nodes.size() - 1; j++) {
                if (nodes.get(j).data != s.lastElement().data) {
                    ArrayList<DCEL.Vertex> poped = new ArrayList<DCEL.Vertex>();
                    while (!s.isEmpty()) {
                        poped.add(s.pop());
                    }
                    for (int k = 0; k < poped.size() - 1; k++) {
                        g.insertEdge(
                                getGraphVertex(nodes.get(j), g),
                                getGraphVertex(poped.get(k), g));
                        step("");
                    }
                    s.push(nodes.get(j - 1));
                } else {
                    ArrayList<DCEL.Vertex> poped = new ArrayList<DCEL.Vertex>();
                    poped.add(s.pop());
                    if (!s.isEmpty()) {
                        DCEL.Vertex nextV = s.pop();
                        while (inMonotone(nodes.get(j), poped.get(poped.size() - 1), nextV, g)) {
                            poped.add(nextV);
                            g.insertEdge(
                                    getGraphVertex(nodes.get(j), g),
                                    getGraphVertex(nextV, g));
                            step("");
                            if (s.isEmpty()) {
                                break;
                            }
                            nextV = s.pop();
                        }
                        s.push(nextV);
                    }
                    s.push(poped.get(poped.size() - 1));
                }
                s.push(nodes.get(j));
            }

            s.pop();
            ArrayList<DCEL.Vertex> poped = new ArrayList<DCEL.Vertex>();
            while (!s.isEmpty()) {
                poped.add(s.pop());
            }
            for (int k = 0; k < poped.size() - 1; k++) {
                g.insertEdge(
                        getGraphVertex(nodes.get(nodes.size()-1), g),
                        getGraphVertex(poped.get(k), g));
                step("");
            }
            
            for (DCEL.HalfEdge he : face.getInnerComponent()) {
                Vertex v = getGraphVertex(he.getOrigin(), g);
                v.setSelected(false);
            }
            step("Finished triangulating face");
        }
    }

    private boolean inMonotone(DCEL.Vertex uj, DCEL.Vertex ujm1, DCEL.Vertex ujm2, GraphModel g) {
//        double v1x = uj.getP().x - ujm1.getP().x,
//                v1y = uj.getP().y - ujm1.getP().y,
//                v2x = ujm2.getP().x - ujm1.getP().x,
//                v2y = ujm2.getP().y - ujm1.getP().y;
        int ujChainId = Integer.parseInt(uj.data.toString());
//        return (v1x * v2y - v1y * v2x) * ujChainId < 0;
//      
        boolean right = CGUtil.isOnRight(
                getGraphVertex(uj, g), 
                getGraphVertex(ujm1, g), 
                getGraphVertex(ujm2, g));
        return (ujChainId == 1 && !right) || (ujChainId == -1 && right);
    }

    private void handleStartVertex(
            DCEL.Vertex v,
            RedBlackTree<LineSegment> tStatus,
            LineSegment eiKey) { // 0
        // 1. Inserting Ei in T
        RedBlackNode<LineSegment> ei = tStatus.insert(eiKey);
        // 1. & Setting its helper to Vi
        ei.data = v;
    }

    /**
     * This method is not exactly written as the pseudocode in book the order of
     * operations is modified
     *
     * @param v
     * @param dcel
     * @param tStatus
     * @param status
     * @param eiKey
     */
    private void handleSplitVertex(
            DCEL.Vertex v, DCEL dcel,
            RedBlackTree<LineSegment> tStatus,
            LineSegment eiKey,
            GraphModel g) throws Exception { // 1

        // 4. Inserting Ei in T
        RedBlackNode<LineSegment> ei = tStatus.insert(eiKey);
        // 4. & Setting its helper to Vi
        ei.data = v;
        // 1. Search in T to find Ej directly left of Vi
        RedBlackNode<LineSegment> ej = tStatus.treePredecessor(ei);

        System.out.println("split: Ej is null!? " + (ej == null || tStatus.isNil(ej)));
        System.out.println("handling split vertex. Ej is connecting "
                + ej.key.p.getLabel() + " to " + ej.key.v.getLabel());
        if (ej.data != null) {
            // 2. Insert diagonal connecting helper(Ej) to Vi
            dcel.addEdge(v, (DCEL.Vertex) ej.data);
            g.insertEdge(getGraphVertex(v, g), getGraphVertex((DCEL.Vertex) ej.data, g));
        } else {
            throw new Exception("Ej not found in the tree! "
                    + "while handling split vertex");
        }
        // 3. helper(Ej) <- Vi
        ej.data = v;
    }

    private void handleEndVertex(
            DCEL.Vertex v, DCEL dcel,
            RedBlackTree<LineSegment> tStatus,
            RedBlackTree<LineSegment> status,
            LineSegment eim1Key,
            GraphModel g) throws Exception { // 2

        System.out.println("End: Ei-1 is connecting " + eim1Key.p.getLabel() + " to " + eim1Key.v.getLabel());
        RedBlackNode<LineSegment> eim1 = tStatus.search(eim1Key);
        if (tStatus.isNil(eim1) || eim1 == null) {
            throw new Exception("Ei-1 not found in the tree! "
                    + "while handling end vertex");
        } else {
            if (eim1.data != null) {
                DCEL.Vertex hEim1 = (DCEL.Vertex) eim1.data;
                // 1. If helper of Ei-1 is a merge vertex
                if (getVertexType(hEim1, dcel, status, g) == 3) {
                    // 2. Insert diagonal connecting helper(Ei-1) to Vi
                    dcel.addEdge(v, hEim1);
                    g.insertEdge(getGraphVertex(v, g), getGraphVertex(hEim1, g));
                }
            } else {
                throw new Exception("Ei-1 had no helper! "
                        + "while handling end vertex");
            }
        }
        // 3. Removing Ei-1 from T
        tStatus.remove(eim1);
    }

    private void handleMergeVertex(
            DCEL.Vertex v, DCEL dcel,
            RedBlackTree<LineSegment> tStatus,
            RedBlackTree<LineSegment> status,
            LineSegment eiKey,
            LineSegment eim1Key,
            GraphModel g) throws Exception { // 3

        RedBlackNode<LineSegment> eim1 = tStatus.search(eim1Key);
        if (tStatus.isNil(eim1) || eim1 == null) {
            throw new Exception("Ei-1 not found in the tree! "
                    + "while handling merge vertex");
        } else {
            if (eim1.data != null) {
                DCEL.Vertex hEim1 = (DCEL.Vertex) eim1.data;
                // 1. If helper of Ei-1 is a merge vertex
                if (getVertexType(hEim1, dcel, status, g) == 3) {
                    // 2. Insert diagonal connecting helper(Ei-1) to Vi
                    // TODO: Insert v <-> hEim1 to dcel
                    g.insertEdge(getGraphVertex(v, g), getGraphVertex(hEim1, g));
                }
            } else {
                throw new Exception("Ei-1 had no helper! "
                        + "while handling merge vertex");
            }
//            // 3. Remove Ei-1 from T
//            tStatus.remove(eim1);
            // 4. Search in T for the edge Ej directly left of Vi
//            // Ej must be directly left of Ei, so first we should check if Ei
//            // exists in T, if not we should temporarily add it to T so that 
//            // we can find Ej, then we delete it.
//            RedBlackNode<LineSegment> ei = tStatus.search(eiKey);
//            RedBlackNode<LineSegment> ej;
//            if (tStatus.isNil(ei) || ei == null) { // Add Ei to T
//                ei = tStatus.insert(eiKey);
//                ej = tStatus.treePredecessor(ei);
//                tStatus.remove(ei);
//            } else { // Ei exists
//                ej = tStatus.treePredecessor(ei);
//            }
            RedBlackNode<LineSegment> ej = tStatus.treePredecessor(eim1);
            // 3. Remove Ei-1 from T
            tStatus.remove(eim1);

            System.out.println("merge: Ej is null!? " + (ej == null || tStatus.isNil(ej)));
            System.out.println("handling merge vertex. Ej is connecting "
                    + ej.key.p.getLabel() + " to " + ej.key.v.getLabel());
            if (ej.data != null) {
                DCEL.Vertex hEj = (DCEL.Vertex) ej.data;
                // 5. If helper of Ej is a merge vertex
                if (getVertexType(hEj, dcel, status, g) == 3) {
                    // 6. Insert diagonal connecting helper(Ej) to Vi
                    dcel.addEdge(v, hEj);
                    g.insertEdge(getGraphVertex(v, g), getGraphVertex(hEj, g));
                }
            } else {
                throw new Exception("Ej had no helper! "
                        + "while handling merge vertex");
            }
            // 7. helper(Ej) <- Vi
            ej.data = v;
        }
    }

    private void handleRegularVertex(
            DCEL.Vertex v, DCEL dcel,
            RedBlackTree<LineSegment> tStatus,
            RedBlackTree<LineSegment> status,
            LineSegment eiKey, LineSegment eim1Key,
            GraphModel g) throws Exception { // 4
        // 1. If the interior of P lies to the right of Vi
        if (isPolygonInteriorOnRight(v, status.inOrder(), g)) {
            RedBlackNode<LineSegment> eim1 = tStatus.search(eim1Key);
            if (tStatus.isNil(eim1) || eim1 == null) {
                throw new Exception("Ei-1 not found in the tree! "
                        + "while handling regular vertex");
            } else {
                if (eim1.data != null) {
                    DCEL.Vertex hEim1 = (DCEL.Vertex) eim1.data;
                    // 2. If helper of Ei-1 is a merge vertex
                    if (getVertexType(hEim1, dcel, status, g) == 3) {
                        // 3. Insert diagonal connecting helper(Ei-1) to Vi
                        dcel.addEdge(v, hEim1);
                        g.insertEdge(getGraphVertex(v, g), getGraphVertex(hEim1, g));
                    }
                } else {
                    throw new Exception("Ei-1 had no helper! "
                            + "while handling regular vertex");
                }
                // 4. Remove Ei-1 from T
                tStatus.remove(eim1);
                // 5. insert Ei in T
                RedBlackNode<LineSegment> ei = tStatus.insert(eiKey);
                // 5. & Set its helper to Vi
                ei.data = v;
            }
        } else {
            // 6. Search in T for the edge Ej directly left of Vi
            // Ej must be directly left of Ei-1, so first we should check if Ei-1
            // exists in T, if not we should temporarily add it to T so that 
            // we can find Ej, then we delete it.
            RedBlackNode<LineSegment> eim1 = tStatus.search(eim1Key);
            RedBlackNode<LineSegment> ej;
            if (tStatus.isNil(eim1) || eim1 == null) { // Add Ei-1 to T
                eim1 = tStatus.insert(eim1Key);
                ej = tStatus.treePredecessor(eim1);
                tStatus.remove(eim1);
            } else { // Ei exists
                ej = tStatus.treePredecessor(eim1);
            }

            System.out.println("regular: Ej is null!? " + (ej == null || tStatus.isNil(ej)));
            System.out.println("handling regular vertex. Ej is connecting "
                    + ej.key.p.getLabel() + " to " + ej.key.v.getLabel());
            if (ej.data != null) {
                DCEL.Vertex hEj = (DCEL.Vertex) ej.data;
                // 7. If helper of Ej is a merge vertex
                if (getVertexType(hEj, dcel, status, g) == 3) {
                    // 8. Insert diagonal connecting helper(Ej) to Vi
                    dcel.addEdge(v, hEj);
                    g.insertEdge(getGraphVertex(v, g), getGraphVertex(hEj, g));
                }
            } else {
                throw new Exception("Ej had no helper! "
                        + "while handling regular vertex");
            }
            // 9. helper(Ej) <- Vi
            ej.data = v;
        }
    }

    /**
     * Computes the type of vertex in a polygon given its DCEL and the status
     * tree which is built using the sweep line technique. (sweep line is
     * considered to be on the target vertex v) 0 = Start Vertex 1 = Split
     * Vertex 2 = End Vertex 3 = Merge Vertex 4 = Regular Vertex
     *
     * @param v
     * @param dcel
     * @param status
     * @return
     */
    private int getVertexType(DCEL.Vertex v, DCEL dcel, RedBlackTree<LineSegment> status, GraphModel g) {
        if (v.data != null) {
            return Integer.parseInt(v.data.toString());
        }

        int ret;
        DCEL.Vertex[] neighbours = getVertexNeighbours(v);
        if (neighbours[0].getP().getY() > v.getP().getY() && neighbours[1].getP().getY() > v.getP().getY()) { // split or start
            if (isPolygonInteriorOnRight(v, status.inOrder(), g)) {
                ret = 1; // split vertex
            } else {
                ret = 0; // start vertex
            }
        } else if (neighbours[0].getP().getY() < v.getP().getY() && neighbours[1].getP().getY() < v.getP().getY()) { // end or merge
            if (isPolygonInteriorOnRight(v, status.inOrder(), g)) {
                ret = 3; // merge vertex
            } else {
                ret = 2; // end vertex
            }
        } else {
            ret = 4; // regular
        }

        v.data = ret;
        return ret;
    }

    /**
     * Determines if the interior of a polygon lies on the right side of a node
     * by ray casting on left & right sides of the point and counting the number
     * of intersection on each side.
     *
     * @param v
     * @param intersectingSegments
     * @return returns true if the number of intersections on the right side is
     * odd
     */
    private boolean isPolygonInteriorOnRight(DCEL.Vertex v, ArrayList<RedBlackNode<LineSegment>> intersectingSegments, GraphModel g) {
        Vertex rightInf = new Vertex();
        rightInf.setLocation(new GraphPoint(+10000, v.getP().y));  // TODO: +10000 is considered positive x inf
        LineSegment ray = new LineSegment(getGraphVertex(v, g), rightInf);

        int intersections = 0;
        for (RedBlackNode<LineSegment> ls : intersectingSegments) {
            if (CGUtil.doIntersect(ray, ls.key) != null) {
                if (ls.key.p.getLocation().y == v.getP().y || ls.key.v.getLocation().y == v.getP().y) {
                    intersections += 2;
                } else {
                    intersections += 1;
                }
            }
        }
        return intersections % 2 == 1;
    }

    private DCEL.Vertex[] getVertexNeighbours(DCEL.Vertex v) {
        DCEL.Vertex[] ret = new DCEL.Vertex[2];
        DCEL.HalfEdge he = v.getHalfEdge();
        ret[0] = he.getNext().getOrigin();
        ret[1] = he.getTwin().getNext().getNext().getOrigin();
        return ret;
    }

    private Vertex getGraphVertex(DCEL.Vertex v, GraphModel g) {
        for (Vertex ver : g.getVertexArray()) {
            if (ver.getLocation().getX() == v.getP().getX() && ver.getLocation().getY() == v.getP().getY()) {
                return ver;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "Triangulation";
    }

    @Override
    public String getDescription() {
        return "Triangulation Description";
    }
}

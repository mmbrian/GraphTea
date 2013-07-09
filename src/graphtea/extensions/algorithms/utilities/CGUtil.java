/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtea.extensions.algorithms.utilities;

import graphtea.extensions.algorithms.structures.DCEL;
import graphtea.extensions.algorithms.structures.DCEL.Face;
import graphtea.extensions.algorithms.structures.Intersection;
import graphtea.extensions.algorithms.structures.LineSegment;
import graphtea.extensions.algorithms.structures.Segment;
import graphtea.graph.graph.Edge;
import graphtea.graph.graph.GraphModel;
import graphtea.graph.graph.GraphPoint;
import graphtea.graph.graph.Vertex;
import java.util.ArrayList;

/**
 *
 * @author Mohsen Mansouryar (mansouryar@cs.sharif.edu)
 * @author Saman Jahangiri (saman_jahangiri@math.sharif.edu)
 */
public class CGUtil {

    /**
     * Determines whether a polygon (convex or not) is Clockwise given some
     * ordering of its vertices. the first point and the last point in the list
     * should be the same.
     * @param points polygon points in C or CC order. ending points should be 
     * the same (so that they form a cycle)
     * @return 
     */
    public static boolean isPolygonClockwise(ArrayList<GraphPoint> points) {
        double sum = 0;
        for (int i = 1; i < points.size(); i++) {
            GraphPoint p = points.get(i);
            sum += (p.x - points.get(i-1).x) * (p.y + points.get(i-1).y);
        }
        return sum < 0;
    }
    
    /**
     * p -> q is the directed line for which we want to determine whether r lies
     * on its right. this is actually the determinant of the following matrix
     *
     * |1 p.x p.y| 
     * |1 q.x q.y| 
     * |1 r.x r.y|
     *
     * @param p
     * @param q
     * @param r
     * @return
     */
    public static boolean isOnRight(Vertex p, Vertex q, Vertex r) {
        // (qx ry - qy rx) - (px ry - py rx) + (px qy - py qx)
        return (q.getLocation().getX() * r.getLocation().getY() - q.getLocation().getY() * r.getLocation().getX())
                - (p.getLocation().getX() * r.getLocation().getY() - p.getLocation().getY() * r.getLocation().getX())
                + (p.getLocation().getX() * q.getLocation().getY() - p.getLocation().getY() * q.getLocation().getX()) > 0;
    }

    /**
     * @param s1
     * @param s2
     * @return Returns null if s1 and s2 do no intersect, otherwise returns the
     * intersection point
     */
    public static Vertex doIntersect(LineSegment s1, LineSegment s2) {
        boolean s1Okay = isOnRight(s2.p, s2.v, s1.p) ^ isOnRight(s2.p, s2.v, s1.v);
        boolean s2Okay = isOnRight(s1.p, s1.v, s2.p) ^ isOnRight(s1.p, s1.v, s2.v);
        if (s1Okay && s2Okay) {
            double x1 = s1.p.getLocation().x, y1 = s1.p.getLocation().y,
                    x2 = s1.v.getLocation().x, y2 = s1.v.getLocation().y,
                    x3 = s2.p.getLocation().x, y3 = s2.p.getLocation().y,
                    x4 = s2.v.getLocation().x, y4 = s2.v.getLocation().y;
            double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
            if (d == 0) {
                return null;
            }

            double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
            double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

            Vertex ret = new Vertex();
            ret.setLocation(new GraphPoint(xi, yi)); // TODO: center should be set to xi, yi
            return ret;
        }
        return null;
    }

    /**
     * Detects segments and intersection nodes of a graph and returns the
     * subdivision.
     *
     * @param g
     * @return
     */
    public static DCEL FindDCEL(GraphModel g) {
        ArrayList<Segment> segments = new ArrayList<Segment>();
        for (Edge e : g.getEdges()) {
            segments.add(new Segment(e.source, e.target));
        }

        ArrayList<Intersection> intersections = new ArrayList<Intersection>();
        for (Vertex v : g.getVertexArray()) {
            Intersection intNode = new Intersection(v.getLocation().getX(), v.getLocation().getY());

            ArrayList<Segment> nodeSegments = new ArrayList<Segment>();
            for (Vertex n : g.getNeighbors(v)) {
                Edge e = g.getEdge(v, n);
                nodeSegments.add(new Segment(e.source, e.target));
            }

            intNode.setSegments(nodeSegments);
            intersections.add(intNode);
        }

        return FindDCEL(segments, intersections);
    }

    /**
     * Finds DCEL of a subdivision using its segments and intersection points
     *
     * @param segments
     * @param intersections
     * @return
     */
    public static DCEL FindDCEL(ArrayList<Segment> segments, ArrayList<Intersection> intersections) {
        // Initialize DCEL
        DCEL dcel = new DCEL();

        int num_of_intersections = intersections.size();
        int num_of_segments = segments.size();
        ArrayList<Segment> tmp;

        // Initialize split segments
        for (int i = 0; i < num_of_segments; i++) {
            segments.get(i).initSplitSegment();
        }

        // Complete splitting operation
        for (int i = 0; i < num_of_intersections; i++) {
            tmp = intersections.get(i).getSegments();
            num_of_segments = tmp.size();
            for (int j = 0; j < num_of_segments; j++) {
                tmp.get(j).addSplit(intersections.get(i).getPoint());
            }
        }

        num_of_segments = segments.size();
        // Creation of Vertex List
        for (int i = 0; i < num_of_segments; i++) {
            int num_of_splits = segments.get(i).getSplit().size();
            for (int j = 0; j < num_of_splits; j++) {
                dcel.addVertex(segments.get(i).getSplit().get(j));
            }
        }

        // Create half edges and store their twins
        // Link half edges to their origins
        DCEL.Vertex v1, v2;
        DCEL.HalfEdge h1, h2;

        for (int i = 0; i < num_of_segments; i++) {
            int num_of_splits = segments.get(i).getSplit().size();

            for (int j = 0; j < (num_of_splits - 1); j++) {
                v1 = dcel.pointToVertex(segments.get(i).getSplit().get(j));
                v2 = dcel.pointToVertex(segments.get(i).getSplit().get(j + 1));

                h1 = new DCEL.HalfEdge(v1, dcel.getHalfEdgeList().size());
                dcel.addHalfEdge(h1);

                h2 = new DCEL.HalfEdge(v2, dcel.getHalfEdgeList().size());
                dcel.addHalfEdge(h2);

                h1.setTwin(h2);
                h2.setTwin(h1);

                v1.setHalfEdge(h1);
                v2.setHalfEdge(h2);
            }
        }

        // Stores next and prev in Half Edge List
        int num_of_halfEdges = dcel.getHalfEdgeList().size();
        ArrayList<DCEL.HalfEdge> tmpHalfEdges = new ArrayList<DCEL.HalfEdge>();
        DCEL.Vertex v3, v4, vtmp;
        DCEL.HalfEdge he1, he2, heTmp;
        double angle, oldAngle;

        for (int i = 0; i < num_of_halfEdges; i++) {
            tmpHalfEdges.clear();

            he1 = dcel.getHalfEdgeList().get(i);

            v1 = he1.getTwin().getOrigin();
            v2 = he1.getOrigin();

            he2 = null;
            vtmp = null;
            oldAngle = 0;
            angle = 0;

            for (int j = 0; j < num_of_halfEdges; j++) {

                v3 = dcel.getHalfEdgeList().get(j).getOrigin();

                if (v1.equals(v3)) {

                    heTmp = dcel.getHalfEdgeList().get(j);
                    v4 = heTmp.getTwin().getOrigin();
                    tmpHalfEdges.add(heTmp);
                    angle = v1.getAngle(v2, v4);

                    if (vtmp == null || angle >= oldAngle) {
                        vtmp = v4;
                        oldAngle = angle;
                        he2 = heTmp;
                    }
                }
            }

            he1.setNext(he2);
            he2.setPrev(he1);
        }

        // Create Faces
        ArrayList<DCEL.HalfEdge> HalfEdgesBis = (ArrayList<DCEL.HalfEdge>) dcel.getHalfEdgeList().clone();
        DCEL.Face face;

        while (HalfEdgesBis.size() > 0) {
            he1 = HalfEdgesBis.get(0);
            face = new DCEL.Face(he1.getTwin(), dcel.getFaceList().size());

            he1.setFace(face);
            face.updateInnerComponent(he1); // updating innerComponent
            dcel.addFaceList(face);

            HalfEdgesBis.removeAll(face.getInnerComponent());
        }

        int num_of_faces = dcel.getFaceList().size();
        
        System.out.println("*****************************************");
        System.out.println(num_of_faces + " FACES DETECTED.");
        for (Face f: dcel.getFaceList()){
            System.out.println(f.getInnerComponent().size());
        }
         System.out.println("*****************************************");
        /*
        DCEL.Face faceTmp;

        for (int j = 0; j < num_of_faces; j++) {
            faceTmp = dcel.getFaceList().get(j);
            faceTmp.analyseFace();
        }


        // Split inner and outer
        ArrayList<DCEL.Face> outerFace = new ArrayList<DCEL.Face>();
        num_of_faces = dcel.getFaceList().size();

        DCEL.Face f0 = new DCEL.Face(null, num_of_faces); // the infinite Face

        for (int j = 0; j < num_of_faces; j++) {
            faceTmp = dcel.getFaceList().get(j);

            if (faceTmp.isOuter()) {
                outerFace.add(faceTmp);
                dcel.getFaceList().remove(faceTmp);
                num_of_faces = dcel.getFaceList().size();
            }
        }

        // Fill the innerComponent in the Faces
        int num_of_FacesIn = dcel.getFaceList().size();
        int num_of_FacesOut = outerFace.size();
        int num_of_EdgesCrossed;

        DCEL.Face faceIn, faceOut;
        DCEL.Face faceRes;
        double distRes, distTmp, distTmp2;

        for (int i = 0; i < num_of_FacesOut; i++) {
            faceOut = outerFace.get(i);
            v1 = faceOut.getOuterHalfEdge().getOrigin();
            num_of_EdgesCrossed = 0;
            distTmp = 0;
            distRes = 0;
            faceRes = null;

            for (int j = 0; j < num_of_FacesIn; j++) {
                faceIn = dcel.getFaceList().get(j);
                h1 = faceIn.getOuterComponent();

                heTmp = h1;
                num_of_EdgesCrossed = 0;
                if (v1.crossHorizontal(heTmp)) {
                    distTmp2 = v1.horizontalDistance(heTmp);

                    if (distTmp2 >= 0) {
                        if (distTmp == 0 || distTmp2 < distTmp) {
                            distTmp = distTmp2;
                        }

                        num_of_EdgesCrossed++;
                    }
                }
                heTmp = heTmp.getNext();

                while (!(heTmp.equals(h1))) {
                    if (v1.crossHorizontal(heTmp)) {
                        distTmp2 = v1.horizontalDistance(heTmp);

                        if (distTmp2 >= 0) {
                            if (distTmp == 0 || distTmp2 < distTmp) {
                                distTmp = distTmp2;
                            }

                            num_of_EdgesCrossed++;
                        }
                    }
                    heTmp = heTmp.getNext();
                }

                // here is the trick: if there are an odd edges crossed
                // faceOut is included in faceIn
                // in order to know which one is the tinier, we just use 
                // distance between a vertex and a Half Edge
                if (num_of_EdgesCrossed % 2 == 1) {
                    if (distRes == 0) {
                        faceRes = faceIn;
                        distRes = distTmp;
                    } else {
                        if (distTmp < distRes) {
                            faceRes = faceIn;
                            distRes = distTmp;
                        }
                    }
                }
            }
            if (distRes == 0) {
                faceRes = f0;
            }

            // Link faces with holes

            num_of_halfEdges = dcel.getHalfEdgeList().size();
            for (int j = 0; j < num_of_halfEdges; j++) {
                he1 = dcel.getHalfEdgeList().get(j);
                if (he1.getFace().equals(faceOut)) {
                    he1.setFace(faceRes);
                }
            }

            // Fill the inner components
            faceRes.addInnerComponent(faceOut.getOuterComponent());
        }

        //add the infinite face to the Face List
        dcel.addFaceList(f0);

        //Re-arrange the IDs of the faces
        for (int j = 0; j < num_of_faces + 1; j++) {
            faceTmp = dcel.getFaceList().get(j);
            faceTmp.setId(j);
        }

//        //Computing inner component of each face
//        for (Face f: dcel.getFaceList()) {
//            if (!f.isOuter()) {
//                f.updateInnerComponent(f.getOuterComponent().getTwin());
//            } else {
////                f.updateInnerComponent(f.getInnerComponent().get(0));
//            }
//        }
*/
        return dcel;
    }
}

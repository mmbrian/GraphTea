/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtea.extensions.algorithms;

import graphtea.extensions.algorithms.structures.LineSegment;
import graphtea.graph.graph.GraphPoint;
import graphtea.graph.graph.Vertex;

/**
 *
 * @author ASUS
 */
public class CGUtil {

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
}

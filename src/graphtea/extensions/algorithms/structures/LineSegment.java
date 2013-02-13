/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtea.extensions.algorithms.structures;

import graphtea.extensions.algorithms.utilities.CGUtil;
import graphtea.graph.graph.GraphPoint;
import graphtea.graph.graph.Vertex;

/**
 *
 * @author Mohsen Mansouryar (mansouryar@cs.sharif.edu)
 * @author Saman Jahangiri (saman_jahangiri@math.sharif.edu)
 */
public class LineSegment implements Comparable<LineSegment> {

    public LSVertex p, v;
    private LSVertex compareNode;
    int id;
    private static int idC = 0;

    public LineSegment(Vertex p, Vertex v) {
        this.p = new LSVertex(p, this);
        this.v = new LSVertex(v, this);
        this.compareNode = (p.getLocation().y > v.getLocation().y) ? this.v : this.p;
//        isCompareNodeSwitched = false;
        this.id = idC++;
    }

    public LSVertex getCompareNode() {
        return compareNode;
    }

    public void updateCompareNode(double sweepLineY, double offset) {
        Vertex v1 = new Vertex(), v2 = new Vertex();
        v1.setLocation(new GraphPoint(this.p.getLocation().x, sweepLineY + offset));
        v2.setLocation(new GraphPoint(this.v.getLocation().x, sweepLineY + offset));
        Vertex tmp = CGUtil.doIntersect(this, new LineSegment(v1, v2));
        if (tmp != null) {
            compareNode = new LSVertex(tmp, this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LineSegment) {
            LineSegment other = (LineSegment) obj;
            if (other.p.getLabel().equals(p.getLabel())) {
                return other.v.getLabel().equals(v.getLabel());
            } else if (other.p.getLabel().equals(v.getLabel())) {
                return other.v.getLabel().equals(p.getLabel());
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return p.getLabel() + "-" + v.getLabel();
    }

    public int compareTo(LineSegment o) {
        LSVertex thisTopVertex, thisBottomVertex;
        if (this.p.getLocation().y > this.v.getLocation().y) {
            thisTopVertex = this.v;
            thisBottomVertex = this.p;
        } else {
            thisTopVertex = this.p;
            thisBottomVertex = this.v;
        }
        System.out.println("Compare node is " + o.getCompareNode().getLabel());
        return (CGUtil.isOnRight(thisBottomVertex, thisTopVertex, o.getCompareNode())) ? -1 : 1;
    }
}

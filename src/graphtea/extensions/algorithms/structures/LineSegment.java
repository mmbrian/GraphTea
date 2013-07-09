/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtea.extensions.algorithms.structures;

import graphtea.extensions.algorithms.utilities.CGUtil;
import graphtea.graph.graph.GraphPoint;

/**
 *
 * @author Mohsen Mansouryar (mansouryar@cs.sharif.edu)
 * @author Saman Jahangiri (saman_jahangiri@math.sharif.edu)
 */
public class LineSegment implements Comparable<LineSegment> {

    public LineSegment.Vertex p, v;
    private LineSegment.Vertex compareNode;
    int id;
    private static int idC = 0;
    public Object data;
    
    public LineSegment(graphtea.graph.graph.Vertex p, graphtea.graph.graph.Vertex v) {
        this.p = new LineSegment.Vertex(p, this);
        this.v = new LineSegment.Vertex(v, this);
        this.compareNode = (p.getLocation().y > v.getLocation().y) ? this.v : this.p;
//        isCompareNodeSwitched = false;
        this.id = idC++;
    }

    public LineSegment.Vertex getCompareNode() {
        return compareNode;
    }

    public void updateCompareNode(double sweepLineY, double offset) {
        graphtea.graph.graph.Vertex v1 = new graphtea.graph.graph.Vertex(), v2 = new graphtea.graph.graph.Vertex();
        v1.setLocation(new GraphPoint(this.p.getLocation().x, sweepLineY + offset));
        v2.setLocation(new GraphPoint(this.v.getLocation().x, sweepLineY + offset));
        graphtea.graph.graph.Vertex tmp = CGUtil.doIntersect(this, new LineSegment(v1, v2));
        if (tmp != null) {
            compareNode = new LineSegment.Vertex(tmp, this);
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
        LineSegment.Vertex thisTopVertex, thisBottomVertex;
        if (this.p.getLocation().y > this.v.getLocation().y) {
            thisTopVertex = this.v;
            thisBottomVertex = this.p;
        } else {
            thisTopVertex = this.p;
            thisBottomVertex = this.v;
        }
//        System.out.println("Compare node is " + o.getCompareNode().getLabel());
        return (CGUtil.isOnRight(thisBottomVertex, thisTopVertex, o.getCompareNode())) ? -1 : 1;
    }
    
    
    /**
 *
 * @author Mohsen Mansouryar (mansouryar@cs.sharif.edu)
 * @author Saman Jahangiri (saman_jahangiri@math.sharif.edu)
 */
public static class Vertex extends graphtea.graph.graph.Vertex implements Comparable<LineSegment.Vertex> {
    
    public LineSegment parent;
    public boolean isIntersectionPoint = false;
    public LineSegment fisrtSegment, secondSegment;
    
    public Vertex(graphtea.graph.graph.Vertex parent, LineSegment parentSegment) {
        super(parent);
        this.parent = parentSegment;
        isIntersectionPoint = false;
    }

    /**
     * this constructor assumes this vertex is an intersection point not an
     * endpoint
     *
     * @param parent
     */
    public Vertex(graphtea.graph.graph.Vertex parent, LineSegment firstSegment, LineSegment secondSegment) {
        super(parent);
        
        isIntersectionPoint = true;
        this.fisrtSegment = firstSegment;
        this.secondSegment = secondSegment;
        this.setLabel(firstSegment.toString() + ":" + secondSegment.toString());
        
        this.parent = null;
    }
    
    public LineSegment.Vertex getNeighbor() {
        LineSegment.Vertex v1 = parent.p,
                v2 = parent.v;
        return (v1.equals(this)) ? v2 : v1;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LineSegment.Vertex) {
            LineSegment.Vertex v = (LineSegment.Vertex) obj;
            return v.getLabel().equals(this.getLabel()) || v.getLocation().equals(this.getLocation());
        }
        return super.equals(obj);
    }
    
    public int compareTo(LineSegment.Vertex o) {
        if (this.getLocation().y < o.getLocation().y) {
            return -1;            
        } else if (this.getLocation().y == o.getLocation().y) {
            if (this.getLocation().x < o.getLocation().x) {
                return -1;
            } else if (this.getLocation().x == o.getLocation().x) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }

    @Override
    public void setMark(boolean mark) {
        super.setMark(mark);
    }

    @Override
    public String toString() {
        return this.getLabel();
    }
}

}

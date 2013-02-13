/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtea.extensions.algorithms.structures;

import graphtea.graph.graph.Vertex;
import javax.print.attribute.standard.MediaSize;

/**
 *
 * @author Mohsen Mansouryar (mansouryar@cs.sharif.edu)
 * @author Saman Jahangiri (saman_jahangiri@math.sharif.edu)
 */
public class LSVertex extends Vertex implements Comparable<LSVertex> {
    
    public LineSegment parent;
    public boolean isIntersectionPoint = false;
    public LineSegment fisrtSegment, secondSegment;
    
    public LSVertex(Vertex parent, LineSegment parentSegment) {
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
    public LSVertex(Vertex parent, LineSegment firstSegment, LineSegment secondSegment) {
        super(parent);
        
        isIntersectionPoint = true;
        this.fisrtSegment = firstSegment;
        this.secondSegment = secondSegment;
        this.setLabel(firstSegment.toString() + ":" + secondSegment.toString());
        
        this.parent = null;
    }
    
    public LSVertex getNeighbor() {
        LSVertex v1 = parent.p,
                v2 = parent.v;
        return (v1.equals(this)) ? v2 : v1;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LSVertex) {
            LSVertex v = (LSVertex) obj;
            return v.getLabel().equals(this.getLabel()) || v.getLocation().equals(this.getLocation());
        }
        return super.equals(obj);
    }
    
    public int compareTo(LSVertex o) {
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

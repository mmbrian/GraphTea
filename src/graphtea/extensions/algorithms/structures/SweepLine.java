/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtea.extensions.algorithms.structures;

import graphtea.graph.graph.GraphModel;
import graphtea.graph.graph.GraphPoint;
import graphtea.graph.graph.Vertex;
import graphtea.graph.old.GShape;

/**
 *
 * @author Mohsen
 */
public class SweepLine {

    private double sweepLineY;
    
    private final double X_OFFSET = 50,
                         Y_OFFSET = -50;
    
    private Vertex sweepLineLeft, sweepLineRight;
    
    public SweepLine(double minX, double maxX, GraphModel parent) {
        sweepLineY = Y_OFFSET;
        
        sweepLineLeft = new Vertex();
        sweepLineLeft.setLocation(new GraphPoint(minX - X_OFFSET, Y_OFFSET));
        sweepLineLeft.setLabel("L");
        sweepLineLeft.setShape(GShape.RIGHTWARDTRIANGLE);
        sweepLineLeft.setSize(new GraphPoint(35, 35));
        sweepLineLeft.setColor(2);
        parent.insertVertex(sweepLineLeft);

        sweepLineRight = new Vertex();
        sweepLineRight.setLocation(new GraphPoint(maxX + X_OFFSET, Y_OFFSET));
        sweepLineRight.setLabel("R");
        sweepLineRight.setShape(GShape.LEFTWARDTTRIANGLE);
        sweepLineRight.setSize(new GraphPoint(35, 35));
        sweepLineRight.setColor(2);
        parent.insertVertex(sweepLineRight);
        
        parent.insertEdge(sweepLineLeft, sweepLineRight);
    }
    
    public void updateY(double y) {
        sweepLineLeft.setLocation(new GraphPoint(sweepLineLeft.getLocation().getX(), y));
        sweepLineRight.setLocation(new GraphPoint(sweepLineRight.getLocation().getX(), y));
        sweepLineY = y;
    }
    
    public double getY() {
        return this.sweepLineY;
    }
    
    public void destroy(GraphModel parent) {
        parent.removeVertex(sweepLineLeft);
        parent.removeVertex(sweepLineRight);
    }
    
}

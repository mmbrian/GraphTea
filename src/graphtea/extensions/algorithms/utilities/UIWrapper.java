package graphtea.extensions.algorithms.utilities;

import graphtea.graph.graph.GraphModel;
import graphtea.graph.graph.GraphPoint;
import graphtea.graph.graph.Vertex;
import graphtea.graph.old.GShape;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 *
 * @author Mohsen Mansouryar (mansouryar@cs.sharif.edu)
 */
public class UIWrapper {

    public static final int CORNER_VERTEX_SIZE = 10;
    public static final int CORNER_VERTEX_COLOR = 19;
    public static final int TITLE_Y_OFFSET = 10;
    public static final int BUTTON_WIDTH = 100;
    public static final int BUTTON_HEIGHT = 30;
    public static final int START_BUTTON_LABEL_OFFSET = 20;
    public static final int CLEAR_BUTTON_LABEL_OFFSET = 40;
    
    
    public static final String CORNER_VERTEX_LABEL = "~";
    public static final String START_BUTTON_LABEL = "Start Algorithm";
    public static final String CLEAR_BUTTON_LABEL = "Clear";
    
    /**
     * Creates a subgraph representing a bounding box. each vertex has a "~"
     * label for identification.
     *
     * @param topX
     * @param topY
     * @param width
     * @param height
     * @return
     */
    public static GraphModel createBoundingBox(String boxTitle, Rectangle bounds) {
        GraphModel boundingBox = new GraphModel(false);
        ArrayList<Vertex> vertices = new ArrayList<Vertex>();
        for (int side = 0; side < 4; side++) {
            Vertex v = new Vertex();
            v.setLabel(CORNER_VERTEX_LABEL);
            v.setShape(GShape.RECTANGLE);
            v.setSize(new GraphPoint(CORNER_VERTEX_SIZE, CORNER_VERTEX_SIZE));
            v.setColor(CORNER_VERTEX_COLOR);
            v.setLocation(
                    new GraphPoint(
                    bounds.getX() + ((side == 1 || side == 2) ? bounds.getWidth() : 0),
                    bounds.getY() + ((side == 2 || side == 3) ? bounds.getHeight() : 0)));
            boundingBox.insertVertex(v);
            vertices.add(v);
        }
        boundingBox.insertEdge(vertices.get(0), vertices.get(1));
        boundingBox.insertEdge(vertices.get(1), vertices.get(2));
        boundingBox.insertEdge(vertices.get(2), vertices.get(3));
        boundingBox.insertEdge(vertices.get(3), vertices.get(0));

        Vertex title = new Vertex();
        title.setSize(new GraphPoint(1, 1));
        title.setColor(CORNER_VERTEX_COLOR);
        title.setLabel(boxTitle);
        title.setLocation(
                new GraphPoint(
                bounds.getX() + bounds.getWidth() / 2,
                bounds.getY() - TITLE_Y_OFFSET));
        boundingBox.insertVertex(title);

        return boundingBox;
    }

    public static Vertex createStartButton(Rectangle boundingBox) {
        Vertex start = new Vertex();
        start.setLabel(START_BUTTON_LABEL);
        start.setShape(GShape.RECTANGLE);
        start.setColor(8);
        start.setSize(new GraphPoint(BUTTON_WIDTH, BUTTON_HEIGHT));
        start.setLocation(
                new GraphPoint(
                boundingBox.getMaxX() - (BUTTON_WIDTH + CORNER_VERTEX_SIZE) / 2,
                boundingBox.getMinY() - (BUTTON_HEIGHT + CORNER_VERTEX_SIZE) / 2));
        start.setLabelLocation(new GraphPoint(START_BUTTON_LABEL_OFFSET, 0));
        return start;
    }
    
    public static Vertex createClearButton(Rectangle boundingBox) {
        Vertex clear = new Vertex();
        clear.setLabel(CLEAR_BUTTON_LABEL);
        clear.setShape(GShape.RECTANGLE);
        clear.setColor(8);
        clear.setSize(new GraphPoint(BUTTON_WIDTH, BUTTON_HEIGHT));
        clear.setLocation(
                new GraphPoint(
                boundingBox.getMaxX() - (BUTTON_WIDTH + CORNER_VERTEX_SIZE) * 1.5 - CORNER_VERTEX_SIZE ,
                boundingBox.getMinY() - (BUTTON_HEIGHT + CORNER_VERTEX_SIZE) / 2));
        clear.setLabelLocation(new GraphPoint(CLEAR_BUTTON_LABEL_OFFSET, 0));
        return clear;
    }
    
    
}

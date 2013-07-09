package graphtea.extensions.algorithms.structures;

import graphtea.graph.graph.GraphPoint;
import graphtea.graph.graph.Vertex;
import java.util.ArrayList;

/**
 * <p> <b>Segment</b> </p> <p>
 *
 * </p>
 *
 * @author Depoyant Guillaume & Ludmann Michaël8
 *
 */
public class Segment implements Key {

    private java.awt.Polygon segment;
    private float[] xpoints;
    private float[] ypoints;
    private GraphPoint upperEndpoint;
    private GraphPoint lowerEndpoint;
    private GraphPoint leftEndpoint;
    private GraphPoint rightEndpoint;
    private float slope;
    private float originOrdinate;
    public static final float MAX_SLOPE = 99999999999999.906F;
    private double value;
//    private RedBlackNode node;
    private int id;
    private boolean isVertical = false;
    private boolean isHorizontal = false;
    protected float ERROR_ALLOWED = 1.0001E-002F;
    private ArrayList<GraphPoint> split;

    public Segment() {
        segment = new java.awt.Polygon();
        xpoints = null;
        ypoints = null;
        upperEndpoint = new GraphPoint();
        lowerEndpoint = new GraphPoint();
        rightEndpoint = new GraphPoint();
        leftEndpoint = new GraphPoint();
        value = -1;
        id = -1;
        setSplit(new ArrayList<GraphPoint>());
    }

    /**
     * Constructs a segment from an edge using its nodes
     * @param v
     * @param u 
     */
    public Segment(Vertex v, Vertex u) {
        this(new float[]{(float) v.getLocation().getX(), (float) u.getLocation().getX()},
                new float[]{(float) v.getLocation().getY(), (float) u.getLocation().getY()}, 2);
    }

    public Segment(float[] xpoints, float[] ypoints, int npoints) {
        this();
        setXpoints(xpoints);
        setYpoints(ypoints);

        // Here we need to register the vertices as integer for the drawing part
        int[] xpointsInt = new int[npoints], ypointsInt = new int[npoints];
        for (int i = 0; i < npoints; i++) {
            xpointsInt[i] = Float.floatToIntBits(xpoints[i]);
            ypointsInt[i] = Float.floatToIntBits(ypoints[i]);

        }
        segment = new java.awt.Polygon(xpointsInt, ypointsInt, npoints);
        computeEndpoints();
    }

    public Segment(ArrayList<float[]> points) {
        this();
        setSegment(points);
    }

    public Segment(ArrayList<float[]> points, int id) {
        this();
        this.id = id;
        setSegment(points);
    }

    public void setSegment(float[] xpoints, float[] ypoints, int npoints) {
        segment.reset();
        setXpoints(null);
        setYpoints(null);
        for (int i = 0; i < npoints; i++) {
            segment.addPoint((int) xpoints[i], (int) ypoints[i]);
        }
        setXpoints(xpoints);
        setYpoints(ypoints);
        computeEndpoints();
    }

    public void setSegment(ArrayList<float[]> points) {
        int npoints = points.size();
        float[] xpoints = new float[npoints];
        float[] ypoints = new float[npoints];
        for (int i = 0; i < npoints; i++) {
            float[] point = points.get(i);
            if (point.length == 2) {
                xpoints[i] = point[0];
                ypoints[i] = point[1];
            }
        }
        setSegment(xpoints, ypoints, npoints);
    }

    public float[] getXpoints() {
        return this.xpoints;
    }

    public float[] getYpoints() {
        return this.ypoints;
    }

    public java.awt.Polygon getShape() {
        return segment;
    }

    public void setXpoints(float[] xpoints) {
        this.xpoints = xpoints;
    }

    public void setYpoints(float[] ypoints) {
        this.ypoints = ypoints;
    }

    public void computeEndpoints() {
        if (xpoints.length == 2 && ypoints.length == 2) {
            if ((ypoints[0] > ypoints[1]) || (ypoints[0] == ypoints[1] && xpoints[0] <= xpoints[1])) {
                upperEndpoint.setLocation(xpoints[0], ypoints[0]);
                lowerEndpoint.setLocation(xpoints[1], ypoints[1]);
            } else {
                upperEndpoint.setLocation(xpoints[1], ypoints[1]);
                lowerEndpoint.setLocation(xpoints[0], ypoints[0]);
            }
            if ((xpoints[0] > xpoints[1])) {
                rightEndpoint.setLocation(xpoints[0], ypoints[0]);
                leftEndpoint.setLocation(xpoints[1], ypoints[1]);
            } else {
                rightEndpoint.setLocation(xpoints[1], ypoints[1]);
                leftEndpoint.setLocation(xpoints[0], ypoints[0]);
            }
            value = leftEndpoint.x;

            setSlope();
            setOriginOrdinate();
        }
    }

    public GraphPoint getLowerEndpoint() {
        return this.lowerEndpoint;
    }

    public GraphPoint getUpperEndpoint() {
        return this.upperEndpoint;
    }

    public GraphPoint getLeftEndpoint() {
        return this.leftEndpoint;
    }

    public GraphPoint getRightEndpoint() {
        return this.rightEndpoint;
    }

    public void printSegment() {
        System.out.println("Segment " + id + " : upperEndPoint = " + upperEndpoint + " lowerEndPoint = " + lowerEndpoint);
    }

    public String toString() {
        return "Segment " + id + " : [{" + upperEndpoint.getX() + ", " + upperEndpoint.getY() + "} ; {" + lowerEndpoint.getX() + ", " + lowerEndpoint.getY() + "}]";
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setSlope() {
        if (xpoints[1] - xpoints[0] == 0) {
            slope = MAX_SLOPE;
            isVertical = true;
        } else {
            slope = (ypoints[1] - ypoints[0]) / (xpoints[1] - xpoints[0]);
            if (slope == 0) {
                isHorizontal = true;
                setValue(Float.MIN_VALUE);
            }
        }
    }

    public boolean isVertical() {
        return isVertical;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }

    public void setOriginOrdinate() {
        originOrdinate = ypoints[0] - ((ypoints[1] - ypoints[0]) / (xpoints[1] - xpoints[0])) * xpoints[0];
    }

    public float getSlope() {
        return slope;
    }

    public boolean isInBoundingBox(GraphPoint p) {
        boolean c1 = ((p.getX() >= xpoints[0] || p.getX() >= xpoints[1]) && (p.getY() >= ypoints[0] || p.getY() >= ypoints[1]));
        boolean c2 = ((p.getX() <= xpoints[0] || p.getX() <= xpoints[1]) && (p.getY() <= ypoints[0] || p.getY() <= ypoints[1]));
        return c1 && c2;
    }

    public boolean containsPoint(GraphPoint p) {
        if (p.equals(lowerEndpoint) || p.equals(upperEndpoint) || (isVertical() && Math.abs(p.getX() - lowerEndpoint.x) <= ERROR_ALLOWED)) {
            return true;
        }
        return (Math.abs(((p.getY() - ypoints[0]) / (p.getX() - xpoints[0]) - getSlope())) <= ERROR_ALLOWED) && isInBoundingBox(p);
    }

    // return 1 if p is on the left side of the segment (0 = on ; -1 = right)
    public int isLeft(GraphPoint.Float p) {
        double res = (lowerEndpoint.x - p.x) * (upperEndpoint.y - p.y) - (upperEndpoint.x - p.x) * (lowerEndpoint.y - p.y);
        if (res > 0) {
            return 1;
        } else {
            if (res == 0) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    public float getCurrentAbscisse(float Y) {
        return (Y - originOrdinate) / slope;
    }

    public void updateValue(float newY) {
        if (!isHorizontal()) {
            value = (newY - originOrdinate) / slope;
        } else {
            value = newY;
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof Segment) {
            return equals((Key) obj);
        } else {
            System.out.println("Bad Object equals");
            return false;
        }
    }

    @Override
    public boolean equals(Key key) {
        return this == (Segment) key;
    }

    @Override
    public String getLabel() {
        return "S" + value;
    }

//    @Override
//    public RedBlackNode getNode() {
//        return node;
//    }
    @Override
    public double getValue() {
        return value;
    }

    @Override
    public boolean largerThan(Object obj) {
        if (obj instanceof Segment) {
            return largerThan((Key) obj);
        } else {
            System.out.println("Bad Object larger than");
            return false;
        }
    }

    @Override
    public boolean largerThan(Key key) {
        double d = key.getValue();
        double d1 = getValue();
        return d1 < d;
    }

    @Override
    public boolean lessThan(Object obj) {
        if (obj instanceof Segment) {
            return lessThan((Key) obj);
        } else {
            System.out.println("Bad Object less than");
            return false;
        }
    }

    @Override
    public boolean lessThan(Key key) {
        double d = key.getValue();
        double d1 = getValue();
        return d1 > d;
    }

//    @Override
//    public void setNode(RedBlackNode redblacknode) {
//        node = redblacknode;
//    }
    @Override
    public void setValue(double d) {
        value = d;
    }

    @Override
    public void swapValue(Key key) {
        double d = value;
        setValue(key.getValue());
        key.setValue(d);
    }

    public void setSplit(ArrayList<GraphPoint> split) {
        this.split = split;
    }

    public ArrayList<GraphPoint> getSplit() {
        return split;
    }

    public void initSplitSegment() {
        this.split.clear();

        if (this.isHorizontal) {
            this.split.add(this.rightEndpoint);
            this.split.add(this.leftEndpoint);
        } else {
            this.split.add(this.lowerEndpoint);
            this.split.add(this.upperEndpoint);
        }
    }

    public void addSplit(GraphPoint p) {

        int nbPoints = this.split.size();

        for (int i = 0; i < (nbPoints - 1); i++) {
            if (split.get(i).distance(p) < split.get(i).distance(split.get(i + 1))
                    && split.get(i + 1).distance(p) < split.get(i).distance(split.get(i + 1))) {
                this.split.add(i + 1, p);
            }
        }
    }
}

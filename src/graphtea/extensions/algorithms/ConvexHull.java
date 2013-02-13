package graphtea.extensions.algorithms;

import graphtea.graph.graph.Edge;
import graphtea.graph.graph.GraphModel;
import graphtea.graph.graph.Vertex;
import graphtea.library.exceptions.InvalidEdgeException;
import graphtea.platform.core.BlackBoard;
import graphtea.plugins.algorithmanimator.core.GraphAlgorithm;
import graphtea.plugins.algorithmanimator.extension.AlgorithmExtension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;

import graphtea.extensions.algorithms.utilities.CGUtil;
/**
 * author: @author Mohsen Mansouryar (mansouryar@cs.sharif.edu)
 * author: Saman Jahangiri (saman_jahangiri@math.sharif.edu)
 */
public class ConvexHull extends GraphAlgorithm implements AlgorithmExtension {

    public ConvexHull(BlackBoard blackBoard) {
        super(blackBoard);
    }

    @Override
    public void doAlgorithm() {
        step("Computing convex hull of a set of points based on the CONVEXHULL"
                + " algorithm described in chapter one of the book Computational"
                + " Geometry, Algorithms and Applications (CGAA). Developed by"
                + " Mohsen Mansouryar (mmbrian)");

        // Getting graph model (which is currently a set of points)
        GraphModel g = graphData.getGraph();

        // Removing all edges from g
        while (g.edgeIterator().hasNext()) {
            g.removeEdge(g.edgeIterator().next());
        }

        Vertex[] points = g.getVertexArray();
        int n = points.length;

        if (n == 1) {
            step("Points on the convex hull in clockwise order are CH = " + points[0].getLabel());
            return;
        }

        // Sorting points based on x, then y
        step("Sorting input points based on their x coordinate...\n"
                + "After points are all sorted, we denote them as p_1, p_2, ..., p_" + n + " respectively.");
        Arrays.sort(points, new Comparator<Vertex>() {

            public int compare(Vertex v1, Vertex v2) {
                double v1x = v1.getLocation().getX(),
                        v1y = v1.getLocation().getY(),
                        v2x = v2.getLocation().getX(),
                        v2y = v2.getLocation().getY();

                if (v1x > v2x) {
                    return 1;
                } else if (v1x < v2x) {
                    return -1;
                } else {
                    if (v1y < v2y) {
                        return 1;
                    } else if (v1y > v2y) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        });

        step("Computing upper part of the convex hull (Lupper)");

        // Lupper shall contain a set of points all on the convex hull
        ArrayList<Vertex> Lupper = new ArrayList<Vertex>();

        Stack<Edge> edges = new Stack<Edge>();
        Stack<String> CH = new Stack<String>(); // holds labels of points on the convex hull

        // Adding p_1 & p_2 as the first possible two points
        step("Adding p_1 to Lupper");
        Lupper.add(points[0]);
        CH.add(points[0].getLabel());
        points[0].setMark(true);
        step("Adding p_2 to Lupper");
        Lupper.add(points[1]);
        CH.add(points[1].getLabel());
        points[1].setMark(true);

        Edge edge = new Edge(points[0], points[1]);
        g.insertEdge(edge);
        edges.add(edge);
        step("(p_1, p_2) might be an edge of the convex hull");

        int LUsize = Lupper.size();
        for (int i = 2; i < points.length; i++) {
            Lupper.add(points[i]);
            CH.add(points[i].getLabel());
            LUsize++;
            Lupper.get(LUsize - 3).setMark(false);
            points[i].setMark(true);

            // Adding new edge
            step("p_" + (i + 1) + " might be the next point on convex hull");
            edge = new Edge(Lupper.get(LUsize - 2), Lupper.get(LUsize - 1));
            g.insertEdge(edge);
            edges.add(edge);
            // Removing violating edges
            while (LUsize > 2 && !CGUtil.isOnRight(Lupper.get(LUsize - 3), Lupper.get(LUsize - 2), Lupper.get(LUsize - 1))) {
                step("Last three points do not make a right turn > "
                        + "the middle one cannot be on the convex hull");
                g.removeEdge(edges.pop());
                g.removeEdge(edges.pop());
                Lupper.get(LUsize - 2).setMark(false);
                Lupper.remove(LUsize-- - 2);

                String lastPlbl = CH.pop();
                CH.pop(); // Removing middle point index
                CH.add(lastPlbl);

                // Adding new edge
                step("Inserting next possible edge of the convex hull");
                Lupper.get(LUsize - 2).setMark(true);
                edge = new Edge(Lupper.get(LUsize - 2), Lupper.get(LUsize - 1));
                g.insertEdge(edge);
                edges.add(edge);
            }
        }

        // Removing marks from last two points
        Lupper.get(LUsize - 1).setMark(false);
        Lupper.get(LUsize - 2).setMark(false);

        step("Computing lower part of the convex hull (Llower)");

        ArrayList<Vertex> Llower = new ArrayList<Vertex>();

        edges = new Stack<Edge>();

        // Adding p_n & p_(n-1) as the first possible two points
        step("Adding p_" + n + " to Llower");
        Llower.add(points[n - 1]);
        if (CH.lastElement() != points[n - 1].getLabel()) {
            CH.add(points[n - 1].getLabel());
        }
        points[n - 1].setMark(true);
        step("Adding p_" + (n - 1) + " to Llower");
        Llower.add(points[n - 2]);
        CH.add(points[n - 2].getLabel());
        points[n - 2].setMark(true);

        edge = new Edge(points[n - 1], points[n - 2]);
        g.insertEdge(edge);
        edges.add(edge);
        step("(p_" + n + ", p_" + (n - 1) + ") might be a lower edge of the convex hull");

        int LLsize = Llower.size();
        for (int i = n - 3; i >= 0; i--) {
            Llower.add(points[i]);
            CH.add(points[i].getLabel());
            LLsize++;
            Llower.get(LLsize - 3).setMark(false);
            points[i].setMark(true);

            // Adding new edge
            step("p_" + (i + 1) + " might be the next point on lower part of the convex hull");
            edge = new Edge(Llower.get(LLsize - 2), Llower.get(LLsize - 1));
            g.insertEdge(edge);
            edges.add(edge);

            // Removing violating edges
            while (LLsize > 2 && !CGUtil.isOnRight(Llower.get(LLsize - 3), Llower.get(LLsize - 2), Llower.get(LLsize - 1))) {
                step("Last three points do not make a right turn > "
                        + "the middle one cannot be on the convex hull");
                edge = edges.pop();
                try {
                    g.removeEdge(edge);
                } catch (InvalidEdgeException iee) {
                }
                edge = edges.pop();
                try {
                    g.removeEdge(edge);
                } catch (InvalidEdgeException iee) {
                }

                Llower.get(LLsize - 2).setMark(false);
                Llower.remove(LLsize-- - 2);

                String lastPlbl = CH.pop();
                CH.pop(); // Removing middle point index
                CH.add(lastPlbl);

                // Adding new edge
                step("Inserting next possible edge of the convex hull");
                Llower.get(LLsize - 2).setMark(true);
                edge = new Edge(Llower.get(LLsize - 2), Llower.get(LLsize - 1));
                g.insertEdge(edge);
                edges.add(edge);
            }
        }

        // Removing marks from last two points
        Llower.get(LLsize - 1).setMark(false);
        Llower.get(LLsize - 2).setMark(false);

        step("Removing first and last point from lower part to avoid duplication.");
        Llower.remove(0);
        Llower.remove(LLsize - 1 - 1);

        step("Appending lower part to the upper part > Resulting list is the list of points on convex hull in clockwise order");
        Lupper.addAll(Llower);

        String s = "";
        while (!CH.isEmpty()) {
            s += CH.firstElement() + ", ";
            CH.removeElementAt(0);
        }
        s = s.substring(0, s.length() - 3);
        step("Points on the convex hull in clockwise order are CH = " + s);


        step("Have Fun :)");
    }

    @Override
    public String getName() {
        return "Convex Hull";
    }

    @Override
    public String getDescription() {
        return "This is just a show case for developers to see how they can make new algorithms";
    }

}

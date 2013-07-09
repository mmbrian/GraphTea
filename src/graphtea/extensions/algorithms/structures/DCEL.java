package graphtea.extensions.algorithms.structures;

import graphtea.graph.graph.GraphPoint;
import java.util.ArrayList;

public class DCEL {

    private ArrayList<Vertex> vertexList;
    private ArrayList<HalfEdge> halfEdgeList;
    private ArrayList<Face> faceList;

    public DCEL() {
        this.setVertexList(new ArrayList<Vertex>());
        this.setHalfEdgeList(new ArrayList<HalfEdge>());
        this.setFaceList(new ArrayList<Face>());
    }

    public void setVertexList(ArrayList<Vertex> vertexList) {
        this.vertexList = vertexList;
    }

    public ArrayList<Vertex> getVertexList() {
        return vertexList;
    }

    public void addVertex(GraphPoint p) {

        int nbVertex = this.vertexList.size();

        for (int i = 0; i < nbVertex; i++) {
            if (this.vertexList.get(i).getP().distance(p) == 0) {
                return;
            }
        }

        Vertex v = new Vertex(p, vertexList.size());
        vertexList.add(v);
    }

    public void addVertex(int id) {
        Vertex v = new Vertex(id);
        vertexList.add(v);
    }

    public Vertex pointToVertex(GraphPoint p) {
        int nbVertex = this.vertexList.size();

        for (int i = 0; i < nbVertex; i++) {
            if (this.vertexList.get(i).pointIsVertex(p)) {
                return this.vertexList.get(i);
            }
        }
        return this.vertexList.get(0);
    }

    public void addHalfEdge(HalfEdge halfEdge) {
        this.halfEdgeList.add(halfEdge);
    }

    public void addHalfEdge(int id) {
        HalfEdge he = new HalfEdge(id);
        this.halfEdgeList.add(he);
    }

    /**
     * Adds an edge (2 half edges) to this DCEL and updates the whole structure.
     *
     * @param u
     * @param v
     */
    public void addEdge(Vertex u, Vertex v) {
        HalfEdge he1 = new HalfEdge(u, this.getHalfEdgeList().size());
        HalfEdge he2 = new HalfEdge(v, this.getHalfEdgeList().size() + 1);
        Face face = new Face(this.getFaceList().size());

        // The face which is about to be splitted into two
        Face splittingFace = this.getReferenceFace(u, v);

        // get the previous edges for these vertices that are on
        // the reference face
        HalfEdge prev1 = this.getPreviousEdge(u, splittingFace);
        HalfEdge prev2 = this.getPreviousEdge(v, splittingFace);

        face.setOuterComponent(he1);
        splittingFace.setOuterComponent(he2);

        // setup both half edges
        he1.setFace(splittingFace);
        he1.setNext(prev2.next);
        he1.setPrev(prev1);
        he1.setOrigin(u);
        he1.setTwin(he2);

        he2.setFace(face);
        he2.setNext(prev1.next);
        he2.setPrev(prev2);
        he2.setOrigin(v);
        he2.setTwin(he1);

        // set the previous edge's next pointers to the new half edges
        prev1.next = he1;
        prev2.next = he2;

        // update both faces' inner components
        face.updateInnerComponent(he2);
        splittingFace.updateInnerComponent(he1);

        // add the new edges to the list
        this.addHalfEdge(he1);
        this.addHalfEdge(he2);

        // add the new face to the list
        this.addFaceList(face);
    }

    /**
     * Walks around the given face and finds the previous edge for the given
     * vertex. <p> This method assumes that the given vertex will be on the
     * given face.
     *
     * @param vertex the vertex to find the previous edge for
     * @param face the face the edge should lie on
     * @return {@link HalfEdge} the previous edge
     */
    private HalfEdge getPreviousEdge(Vertex vertex, Face face) {
        // find the vertex on the given face and return the
        // edge that points to it
        HalfEdge twin = vertex.getHalfEdge().twin;
        HalfEdge edge = vertex.getHalfEdge().twin.next.twin;
        // look at all the edges that have their
        // destination as this vertex
        while (!edge.equals(twin)) {
            // we can't use the getPrevious method on the leaving
            // edge since this doesn't give us the right previous edge
            // in all cases.  The real criteria is to find the edge that
            // has this vertex as the destination and has the same face
            // as the given face
            if (edge.face.equals(face)) {
                return edge;
            }
            edge = edge.next.twin;
        }
        // if we get here then its the last edge
        return edge;
    }

    public void setHalfEdgeList(ArrayList<HalfEdge> halfEdgeList) {
        this.halfEdgeList = halfEdgeList;
    }

    public ArrayList<HalfEdge> getHalfEdgeList() {
        return halfEdgeList;
    }

    public void addFaceList(Face face) {
        this.faceList.add(face);
    }

    public void addFaceList(int id) {
        Face f = new Face(id);

        if (id == 0) {
            f.setIsOuter(true);
        }

        this.faceList.add(f);
    }

    public void setFaceList(ArrayList<Face> faceList) {
        this.faceList = faceList;
    }

    public ArrayList<Face> getFaceList() {
        return faceList;
    }

    /**
     * Finds the face that both vertices are on. <p> If the given vertices are
     * connected then the first common face is returned. <p> If the given
     * vertices do not have a common face the first vertex's leaving edge's face
     * is returned.
     *
     * @param v1 the first vertex
     * @param v2 the second vertex
     * @return {@link Face} the face on which both vertices lie
     */
    protected Face getReferenceFace(Vertex v1, Vertex v2) {
        // find the face that both vertices are on

        // if the leaving edge faces are already the same then just return
        if (v1.getHalfEdge().getFace().equals(v2.getHalfEdge().getFace())) {
            return v1.getHalfEdge().getFace();
        }

        // loop over all the edges whose destination is the first vertex (constant time)
        HalfEdge e1 = v1.getHalfEdge().twin.next.twin;
        while (e1 != v1.getHalfEdge().twin) {
            // loop over all the edges whose destination is the second vertex (constant time)
            HalfEdge e2 = v2.getHalfEdge().twin.next.twin;
            while (e2 != v2.getHalfEdge().twin) {
                // if we find a common face, that must be the reference face
                if (e1.face == e2.face) {
                    return e1.face;
                }
                e2 = e2.next.twin;
            }
            e1 = e1.next.twin;
        }

        // if we don't find a common face then return v1.leaving.face
        return v1.getHalfEdge().face;
    }

    public void printDCEL() {
        int nbFaces = this.getFaceList().size();
        int nbHalfEdges = this.getHalfEdgeList().size();
        int nbVertex = this.getVertexList().size();

        Face faceTmp;
        HalfEdge heTmp;
        Vertex vTmp;

        //Print Vertex List
        System.out.println("\nVERTEX");

        for (int j = 0; j < nbVertex; j++) {
            vTmp = this.getVertexList().get(j);
            System.out.println("\nid: " + vTmp.getId());
            System.out.println("coordinates: " + vTmp.getP().getX() + " " + vTmp.getP().getY());
            System.out.println("incidentEdge: " + vTmp.getHalfEdge().getId());
        }

        //Print HalfEdge List
        System.out.println("\nHALF EDGES");

        for (int j = 0; j < nbHalfEdges; j++) {
            heTmp = this.getHalfEdgeList().get(j);
            System.out.println("\nid: " + heTmp.getId());
            System.out.println("origin: " + heTmp.getOrigin().getId());
            System.out.println("twin: " + heTmp.getTwin().getId());
            System.out.println("incidentFace: " + heTmp.getFace().getId());
            System.out.println("next: " + heTmp.getNext().getId());
            System.out.println("prev: " + heTmp.getPrev().getId());
        }

        //Print FaceList
        System.out.println("\nFACES");


        for (int j = 0; j < nbFaces; j++) {
            faceTmp = this.getFaceList().get(j);

            System.out.println("\nid:" + faceTmp.getId());
            if (faceTmp.getOuterComponent() == null) {
                System.out.println("outerComponent: none");
            } else {
                System.out.println("outerComponent: " + faceTmp.getOuterComponent().getId());
            }

            ArrayList<HalfEdge> HalfEdges2 = faceTmp.getInnerComponent();
            int nbHE = HalfEdges2.size();

            if (nbHE == 0) {
                System.out.println("innerComponent: none");
            }

            for (int i = 0; i < nbHE; i++) {
                System.out.println("innerComponent: " + faceTmp.getInnerComponent().get(i).getId());
            }
        }
    }

    public void colorDCEL(ArrayList<Face> faceList) {

        ArrayList<Face> newFaces = new ArrayList<Face>();
        newFaces = (ArrayList<Face>) faceList.clone();

        int nbFaces, index;
        nbFaces = newFaces.size();
        newFaces.remove(nbFaces - 1);

        Face faceTmp;

        while (newFaces.size() > 0) {

            faceTmp = newFaces.get(0);
            index = 0;
            nbFaces = newFaces.size();

            for (int j = 0; j < nbFaces; j++) {
                if (newFaces.get(j).getOuterHalfEdge().getOrigin().getP().getX() < faceTmp.getOuterHalfEdge().getOrigin().getP().getX()) {
                    faceTmp = newFaces.get(j);
                    index = j;
                }
            }

            colorDCEL(faceTmp);
            newFaces.remove(index);
        }

    }

    public void colorDCEL(Face face) {

        GraphPoint pTmp, pTmp2;
        HalfEdge heTmp;
        HalfEdge h0;
        ArrayList<HalfEdge> innerComp;
        ArrayList<int[]> points = new ArrayList<int[]>();

        h0 = face.getOuterComponent();
        heTmp = h0;

        // we add the main points of the polygon
        do {
            pTmp = heTmp.getOrigin().getP();
            int[] point = new int[]{(int) pTmp.getX(), (int) pTmp.getY()};
            points.add(point);
            heTmp = heTmp.getNext();
        } while (!(h0.equals(heTmp)));

        // and from here we "substract" the innerComponents
        pTmp2 = heTmp.getOrigin().getP();

        innerComp = face.getInnerComponent();

        for (int i = 0; i < innerComp.size(); i++) {
            int[] point = new int[]{(int) pTmp2.getX(), (int) pTmp2.getY()};
            points.add(point);

            heTmp = h0 = innerComp.get(i);

            do {
                pTmp = heTmp.getOrigin().getP();
                int[] pointIn = new int[]{(int) pTmp.getX(), (int) pTmp.getY()};
                points.add(pointIn);
                heTmp = heTmp.getNext();
            } while (!(h0.equals(heTmp)));

            pTmp = heTmp.getOrigin().getP();
            int[] pointIn = new int[]{(int) pTmp.getX(), (int) pTmp.getY()};
            points.add(pointIn);
        }

    }

    public static class Vertex implements Comparable<DCEL.Vertex> {

        private GraphPoint p;
        private int id;
        private HalfEdge halfEdge;
        //private Edges edges;
        public Object data = null;

        public Vertex(GraphPoint p, int id) {
            this.setP(p);
            this.setId(id);
        }

        public Vertex(int id) {
            this.setId(id);
        }

        public void setP(GraphPoint p) {
            this.p = p;
        }

        public GraphPoint getP() {
            return p;
        }

        public boolean pointIsVertex(GraphPoint p) {
            return (p.distance(this.p) == 0);
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setHalfEdge(HalfEdge halfEdge) {
            this.halfEdge = halfEdge;
        }

        public HalfEdge getHalfEdge() {
            return halfEdge;
        }

        public double getAngle(Vertex v1, Vertex v2) {
            double x, y, x1, x2, y1, y2;

            x = this.p.getX();
            y = this.p.getY();

            x1 = v1.getP().getX();
            y1 = v1.getP().getY();

            x2 = v2.getP().getX();
            y2 = v2.getP().getY();
            /*
             double a, b, c, cos;
		
             a = Math.sqrt((x1-x)*(x1-x)+(y1-y)*(y1-y));
             b = Math.sqrt((x2-x)*(x2-x)+(y2-y)*(y2-y));
             c = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
		
             cos = (a*a+b*b-c*c)/(2*a*b);
		
             return Math.acos(cos);
             */
            double theta1, theta2, theta;

            theta1 = Math.atan2(x - x1, y - y1);
            theta2 = Math.atan2(x - x2, y - y2);
            theta = theta2 - theta1;

            while (theta < 0) {
                theta = theta + 2 * 3.141592646952213;
            }
            while (theta > 2 * 3.141592646952213) {
                theta = theta - 2 * 3.141592646952213;
            }

            return theta;
        }

        public boolean crossHorizontal(HalfEdge heTmp) {

            GraphPoint p1, p2, p;
            p1 = heTmp.getOrigin().getP();
            p2 = heTmp.getTwin().getOrigin().getP();
            p = this.getP();

            boolean isBetween = (p1.getY() > p.getY()) && (p.getY() > p2.getY()) || (p1.getY() < p.getY()) && (p.getY() < p2.getY());
            boolean isAtLeft = (p.getX() > p1.getX()) || (p.getX() > p2.getX());

            return (isBetween && isAtLeft);
        }

        public double horizontalDistance(HalfEdge heTmp) {
            GraphPoint p1, p2, p;
            double scale, newX;

            p = this.getP();
            p1 = heTmp.getOrigin().getP();
            p2 = heTmp.getTwin().getOrigin().getP();

            scale = (p.getY() - p1.getY()) / (p2.getY() - p1.getY());
            newX = scale * (p2.getX() - p1.getX()) + p1.getX();

            return p.getX() - newX;
        }

        public int compareTo(Vertex o) {
            if (this.p.getY() < o.p.getY()) {
                return -1;
            } else if (this.p.getY() == o.p.getY()) {
                if (this.p.getX() < o.p.getX()) {
                    return -1;
                } else if (this.p.getX() == o.p.getX()) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                return 1;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj instanceof DCEL.Vertex) {
                return ((DCEL.Vertex)obj).getP().equals(this.getP());
            } else {
                return false;
            }
        }
        
    }

    public static class HalfEdge {

        private Vertex origin;
        private HalfEdge next;
        private HalfEdge prev;
        private HalfEdge twin;
        private Face face;
        private int id;
        public Object data;

        public HalfEdge(Vertex v, int id) {
            this.origin = v;
            this.id = id;
        }

        public HalfEdge(int id) {
            this.id = id;
        }

        public void setOrigin(Vertex origin) {
            this.origin = origin;
        }

        public Vertex getOrigin() {
            return origin;
        }

        public void setNext(HalfEdge next) {
            this.next = next;
        }

        public HalfEdge getNext() {
            return next;
        }

        public void setPrev(HalfEdge prev) {
            this.prev = prev;
        }

        public HalfEdge getPrev() {
            return prev;
        }

        public void setTwin(HalfEdge twin) {
            this.twin = twin;
        }

        public HalfEdge getTwin() {
            return twin;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setFace(Face face) {
            this.face = face;
        }

        public Face getFace() {
            return face;
        }

        @Override
        public boolean equals(Object obj) {
            HalfEdge he = (HalfEdge) obj;
            return this.id == he.getId();
        }
    }

    public static class Face {

        private int id;
        private HalfEdge outerComponent;
        private ArrayList<HalfEdge> innerComponent;
        private boolean isOuter;
        private Face outerFace;
        private HalfEdge outerHalfEdge;

        public Face(HalfEdge outer, int id) {
            this(id);
            this.setOuterComponent(outer);
            this.setInnerComponent(new ArrayList<HalfEdge>());
        }

        public Face(int id) {
            this.setId(id);
            this.setIsOuter(false);
            this.setInnerComponent(new ArrayList<HalfEdge>());
            this.setOuterComponent(null);
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setOuterComponent(HalfEdge outerComponent) {
            this.outerComponent = outerComponent;
        }

        public HalfEdge getOuterComponent() {
            return outerComponent;
        }

        public void setInnerComponent(ArrayList<HalfEdge> innerComponent) {
            this.innerComponent = innerComponent;
        }

        public void updateInnerComponent(HalfEdge someInnerHalfEdge) {
            this.innerComponent = new ArrayList<HalfEdge>();
            this.addInnerComponent(someInnerHalfEdge);

            HalfEdge curr = someInnerHalfEdge.next;
            while (!curr.equals(someInnerHalfEdge)) {
                this.addInnerComponent(curr);
                curr.setFace(this);
                curr = curr.next;
            }
        }

        public ArrayList<HalfEdge> getInnerComponent() {
            return innerComponent;
        }

        public void addInnerComponent(HalfEdge he) {
            this.innerComponent.add(he);
        }

        public void setIsOuter(boolean isOuter) {
            this.isOuter = isOuter;
        }

        public boolean isOuter() {
            return isOuter;
        }

        public void setOuterFace(Face outerFace) {
            this.outerFace = outerFace;
        }

        public Face getOuterFace() {
            return outerFace;
        }

        public void setOuterHalfEdge(HalfEdge outerHalfEdge) {
            this.outerHalfEdge = outerHalfEdge;
        }

        public HalfEdge getOuterHalfEdge() {
            return outerHalfEdge;
        }

        public void analyseFace() {
            Vertex v;
            HalfEdge he, heTmp;
            double angle;

            heTmp = this.outerComponent.getNext();
            he = this.outerComponent;
            v = he.getOrigin();

            while (!(heTmp.equals(this.outerComponent))) {
                if (v.getP().getX() > heTmp.getOrigin().getP().getX()) {
                    he = heTmp;
                    v = he.getOrigin();
                }
                heTmp = heTmp.getNext();
            }

            angle = v.getAngle(he.getNext().getOrigin(), he.getPrev().getOrigin());

            if (angle == 0 || angle > 3.141592646952213) {
                this.setIsOuter(true);
            }
            this.setOuterHalfEdge(he);

        }

        @Override
        public boolean equals(Object obj) {
            Face f = (Face) obj;
            return this.getId() == f.getId();
        }
    }
}

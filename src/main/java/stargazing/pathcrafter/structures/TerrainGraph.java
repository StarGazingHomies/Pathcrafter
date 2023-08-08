package stargazing.pathcrafter.structures;

import stargazing.pathcrafter.Constants;
import stargazing.pathcrafter.Pathcrafter;

import java.util.ArrayList;

import static stargazing.pathcrafter.Constants.ELEVATE_JUMP_TICKS;

public class TerrainGraph {


    public static class Edge {
        public final int to;
        public final double weight;
        //public ArrayList<EdgeAction> actions;

        public enum EdgeActionType {
            WALK,
            JUMP,
            BEGIN,
            END
        }

        Edge(int t, double w) {to = t; weight = w;}
    }

    public ArrayList<Vertex> vertices = new ArrayList<>();
    public ArrayList<ArrayList<Edge>> edges = new ArrayList<>();

    boolean initialized = false;

    TerrainGraph() {}

    /**
     * Regular ol' euclidean distance
     * @param v1 Vertex 1
     * @param v2 Vertex 2
     * @return Euclidean distance between v1 and v2
     */
    static double euclideanDist(Vertex v1, Vertex v2) {
        return Math.pow((v1.x - v2.x) * (v1.x - v2.x) +
                (v1.y - v2.y) * (v1.y - v2.y) +
                (v1.z - v2.z) * (v1.z - v2.z) , 0.5);
    }

    /**
     * Euclidean distance ignoring the y coordinate, since jumping doesn't slow you down in Minecraft
     * @param v1 Vertex 1
     * @param v2 Vertex 2
     * @return Euclidean distance between v1 and v2 on the xz plane.
     */
    static double flatEuclideanDist(Vertex v1, Vertex v2) {
        return Math.pow((v1.x - v2.x) * (v1.x - v2.x) +
                (v1.z - v2.z) * (v1.z - v2.z) , 0.5);
    }

    /**
     * Simple heuristic function based on sprint jumping.
     * @param v Current vertex
     * @param dest Destination
     * @return A double representing the estimated amount of ticks required
     */
    public double heuristic(Vertex v, Vertex dest) {
        // Sprint jumping distance
        double xz_result = flatEuclideanDist(v, dest) / Constants.SPRINT_JUMP_SPEED;
        // Minimum amount of blocks needed to jump up. Very crude estimation.
        // Note that this heuristic isn't at all accurate when partial blocks or blipping are involved.
        double y_result = v.y < dest.y ? (dest.y - v.y) * ELEVATE_JUMP_TICKS : 0;
        return Math.max(xz_result, y_result);
    }

    public double heuristic(int v, int dest) {
        return heuristic(getVertex(v), getVertex(dest));
    }

    public Vertex getVertex(int i) {return vertices.get(i);}

    public void initEdgeList() {
        for (int i=0; i<vertices.size(); i++) edges.add(new ArrayList<>());
    }

    public void addEdge(int from, int to, EdgeInfo info) {
        if (!initialized) {
            initEdgeList();
        }
        Edge e = info.toEdge(to);
        edges.get(from).add(e);
    }

    public Edge getEdge(int from, int to) {
        for (Edge e:edges.get(from)) {
            if (e.to == to) return e;
        }
        return null;
    }

    public ArrayList<Terrain.PathAction> interpretEdge(int from, int to, EdgeInfo e) {
        Vertex fromVertex = getVertex(from);
        Vertex toVertex = getVertex(to);
        ArrayList<Terrain.PathAction> r = new ArrayList<>();
        for (EdgeAction action : e.actions) {
            double[] coordinatesXZ = interpolate(fromVertex, toVertex, action.dist);
            Pathcrafter.LOGGER.info(String.format("Perform action %s at (%.2f, %.2f, %.2f)",
                    action.action, coordinatesXZ[0], action.y, coordinatesXZ[1]));
            r.add(new Terrain.PathAction(coordinatesXZ[0], action.y, coordinatesXZ[1], action.action));
        }
        return r;
    }

    public double[] interpolate(Vertex v1, Vertex v2, double d) {
        double x1 = v1.x, z1 = v1.z, x2 = v2.x, z2 = v2.z;
        double totDist = flatEuclideanDist(v1,v2);
        double fraction = d / totDist;
        return new double[]{x1 + (x2 - x1) * fraction, z1 + (z2 - z1) * fraction};
    }

    public static class EdgeAction {
        // Describes one action in the edge
        final Edge.EdgeActionType action;
        final double y;
        final double dist;
        final int jumpTicks;
        EdgeAction(Edge.EdgeActionType edgeActionType, double y, double dist) {
            this.action = edgeActionType;
            this.y = y;
            this.dist = dist;
            this.jumpTicks = 0;
        }
        EdgeAction(Edge.EdgeActionType edgeActionType, double y, double dist, int jumpTicks) {
            this.action = edgeActionType;
            this.y = y;
            this.dist = dist;
            this.jumpTicks = jumpTicks;
        }

        public String toString() {
            return String.format("%s at %f", action, dist);
        }
    }

    public static class EdgeInfo {
        public final double weight;
        public final ArrayList<EdgeAction> actions;

        public EdgeInfo(double weight, ArrayList<EdgeAction> actions) {
            this.weight = weight;
            this.actions = actions;
        }

        public Edge toEdge(int to) {
            return new Edge(to, weight);
        }
    }
}

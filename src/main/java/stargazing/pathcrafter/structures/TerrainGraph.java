package stargazing.pathcrafter.structures;

import stargazing.pathcrafter.Constants;

import java.util.ArrayList;

import static stargazing.pathcrafter.Constants.ELEVATE_JUMP_TICKS;

public class TerrainGraph {

    public static class Edge {
        public final int to;
        public final double weight;

        public enum EdgeActionType {
            WALK,
            JUMP
        }

        public static class EdgeAction {
            // Describes one action in the edge
            EdgeActionType action;
            double dist;
            EdgeAction(EdgeActionType edgeActionType, double dist) {
                this.action = edgeActionType;
                this.dist = dist;
            }

            public String toString() {
                return String.format("%s at %f", action, dist);
            }
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

    public void addEdge(int from, int to, double w) {
        if (!initialized) {
            initEdgeList();
        }
        Edge e = new Edge(to, w);
        edges.get(from).add(e);
    }
}

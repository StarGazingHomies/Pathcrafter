package stargazing.pathcrafter.structures;

import java.util.ArrayList;

public class TerrainGraph {

    public static class Vertex {
        double x, y, z;

    }

    public static class Edge {
        int to;
        double weight;
    }

    ArrayList<Vertex> vertices;
    ArrayList<ArrayList<Edge>> edges;

    static double euclideanDist(Vertex v1, Vertex v2) {
        return Math.pow((v1.x - v2.x) * (v1.x - v2.x) +
                (v1.y - v2.y) * (v1.y - v2.y) +
                (v1.z - v2.z) * (v1.z - v2.z) , 0.5);
    }

    public double heuristic(int v, int dest) {
        return euclideanDist(vertices.get(v), vertices.get(dest));
    }

    public void findPath() {
    }
}

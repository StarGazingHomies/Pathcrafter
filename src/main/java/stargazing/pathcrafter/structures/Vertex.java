package stargazing.pathcrafter.structures;

public class Vertex {
    double x, y, z;

    Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String toString() {
        return String.format("Vertex(%f, %f, %f)", x, y, z);
    }
}
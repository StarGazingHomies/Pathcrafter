package stargazing.pathcrafter.structures;

/**
 * 3-element vector, for graph vertices
 */
public class Vertex {
    double x, y, z;
    int columnX, columnZ;

    Vertex(double x, double y, double z, int columnX, int columnZ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.columnX = columnX;
        this.columnZ = columnZ;
    }

    public String toString() {
        return String.format("Vertex(%f, %f, %f)", x, y, z);
    }
}
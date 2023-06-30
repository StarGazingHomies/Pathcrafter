package stargazing.pathcrafter.structures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * Manager for all edges in terrain.
 * May be used for potential performance later. It is not the most intuitive solution.
 */
public class TerrainBounds {

    public static class Bound {
        Direction d;
        double position; // position for this bound, in direction of axis
        double range1, range2; // range for this bound, in perpendicular direction
        int columnX, columnZ;

        Bound(Direction d, double position, double range1, double range2, int columnX, int columnZ) {
            this.d = d;
            this.position = position;
            this.range1 = range1;
            this.range2 = range2;
            this.columnX = columnX;
            this.columnZ = columnZ;
        }

        public boolean canCombine(Bound other) {
            return this.d == other.d &&
                    this.position == other.position &&
                    // Yes, only combine if it's the exact same BlockColumn
                    this.columnX == other.columnX &&
                    this.columnZ == other.columnZ &&
                    // One of the ranges could add onto the other
                    (this.range1 == other.range2 || this.range2 == other.range1);
        }

        public Bound combine(Bound other) {
            if (!canCombine(other)) return null;
            if (this.range1 == other.range2) {
                return new Bound(this.d, this.position, other.range1, this.range2, this.columnX, this.columnZ);
            }
            else {
                return new Bound(this.d, this.position, this.range1, other.range2, this.columnX, this.columnZ);
            }
        }
    }

    static class BoundComparator implements Comparator<Bound> {
        @Override
        public int compare(Bound o1, Bound o2) {
            // First compare direction
            int dirResult = o1.d.compareTo(o2.d);
            if (dirResult != 0) return dirResult;
            // Then sort by position by the perpendicular axis
            int posResult = Double.compare(o1.position, o2.position);
            if (posResult != 0) return posResult;
            // Finally look at range start
            return Double.compare(o1.range1, o2.range2);
        }
    }

    TreeSet<Bound> bounds;

    // Using ceil and floor functions are useful.
    public TerrainBounds() {
        bounds = new TreeSet<>(new BoundComparator());
    }

    public void addBound(Bound b) {
        // Automatic combining of bounds
        // Note that splitting by direction is not necessary since it's taken care of by sorting
        Bound lower = bounds.lower(b);
        if (lower != null && b.canCombine(lower)) {
            Bound combined = lower.combine(b);
            bounds.remove(lower);
            bounds.add(combined);
            return;
        }
        Bound higher = bounds.higher(b);
        if (higher != null && b.canCombine(higher)) {
            Bound combined = higher.combine(b);
            bounds.remove(higher);
            bounds.add(combined);
            return;
        }
        bounds.add(b);
    }

}

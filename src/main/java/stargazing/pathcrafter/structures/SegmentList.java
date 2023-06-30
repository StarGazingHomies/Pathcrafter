package stargazing.pathcrafter.structures;

import org.jetbrains.annotations.NotNull;
import stargazing.pathcrafter.Pathcrafter;

import java.util.Comparator;
import java.util.Objects;
import java.util.TreeSet;

import static stargazing.pathcrafter.Constants.*;
import static stargazing.pathcrafter.util.PlayerSpeed.jumpTicksToFallTo;

public class SegmentList {
    public static class Segment {
        public final double start, end;
        public double val;
        public Segment(double s, double e) {
            start = s;
            end = e;
            val = -1;
        }

        public String toString() {
            return String.format("Segment(%f, %f) -> %f", start, end, val);
        }

        public void updateVal(double newVal) {
            if (val == -1) val = newVal;
            else val = Math.min(val, newVal);
        }
    }

    public static class SegmentComparator implements Comparator<Segment> {

        @Override
        public int compare(Segment o1, Segment o2) {
            return Double.compare(o1.start, o2.start);
        }
    }

    public final double time;
    public final TreeSet<Segment> segments;

    SegmentList(double time) {
        this.time = time;
        segments = new TreeSet<>(new SegmentComparator());
    }

    SegmentList(@NotNull SegmentList copy) {
        segments = (TreeSet<Segment>) copy.segments.clone();
        time = copy.time;
    }

    public void addSegment(Segment s) {
        if (SEGMENT_LIST_DEBUG_INFO) Pathcrafter.LOGGER.info("Adding segment" + s);
        Segment lower = segments.floor(s);
        if (lower != null && lower.end >= s.start) {
            segments.remove(lower);
            s = new Segment(lower.start, s.end);
        }
        Segment higher = segments.ceiling(s);
        if (higher != null && s.end >= higher.start) {
            segments.remove(higher);
            s = new Segment(s.start, higher.end);
        }
        segments.add(s);
        if (SEGMENT_LIST_DEBUG_INFO) debug_print();
    }

    public void addColumn(BlockColumn column) {
        assert (column.surfaces.size() % 2 == 0);
        for (int i=0; i<column.surfaces.size(); i+=2) {
            addSegment(new Segment(column.surfaces.get(i).y, column.surfaces.get(i+1).y));
        }
    }

    public boolean contains(double y) {
        Segment floor = segments.floor(new Segment(y, y));
        return floor == null || y <= floor.end;
    }

    public double floor(double y) {
        Segment floor = segments.floor(new Segment(y, y));
        if (floor == null) {
            Pathcrafter.LOGGER.warn("Encountered null floor when looking for floor of " + y + "!");
            Pathcrafter.LOGGER.warn("SegmentList at time " + time);
            for (Segment s : segments) {
                Pathcrafter.LOGGER.warn("> " + s);
            }
            return MIN_HEIGHT;
        }
        return floor.end;
    }

    public void mark(double minHeight, double maxHeight, double initialVal, double initialY) {
        // Look at endpoints!
        // Note: This is still incorrect, as we need to consider the "slack" from sprinting to the end of the block.
        // You don't have to sprint jump at the end, can do so at the beginning instead,
        // if it's guaranteed to land at the same tick anyway
        // But for now, it's good enough.
        for (Segment s = segments.floor(new Segment(minHeight, minHeight));
             s != null && s.end <= maxHeight;
             s = segments.higher(s)) {
            double newVal = initialVal + jumpTicksToFallTo(s.end - initialY);
            s.updateVal(newVal);
            if (SEGMENT_LIST_DEBUG_INFO)
                Pathcrafter.LOGGER.info("Marking segment " + s + " as value " + newVal);
        }
    }

    public void debug_print() {
        if (!SEGMENT_LIST_ALLOW_INFO_CALL) return;
        Pathcrafter.LOGGER.info("SegmentList at time " + time);
        for (Segment s : segments) {
            Pathcrafter.LOGGER.info("> " + s);
        }
    }
}

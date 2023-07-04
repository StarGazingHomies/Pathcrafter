package stargazing.pathcrafter.structures;

import org.jetbrains.annotations.NotNull;
import stargazing.pathcrafter.Pathcrafter;
import stargazing.pathcrafter.util.PlayerSpeed;

import java.util.Comparator;
import java.util.Objects;
import java.util.TreeSet;

import static stargazing.pathcrafter.Constants.*;
import static stargazing.pathcrafter.util.PlayerSpeed.jumpTicksToFallTo;
import static stargazing.pathcrafter.config.DebugToggles.SEGMENT_LIST_DEBUG_INFO;
import static stargazing.pathcrafter.config.DebugToggles.SEGMENT_LIST_ALLOW_INFO_CALL;

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
        // Correct if and only if each segment intersects at most 2 other segs
        // but like can't be bothered to do the correct thing for now.
        // it's also very rare for such a thing to matter in the wild (intuition, maybe not true.)
        if (SEGMENT_LIST_DEBUG_INFO.enabled()) Pathcrafter.LOGGER.info("Adding segment" + s);
        Segment lower = segments.floor(s);
        if (lower != null && lower.end >= s.start) {
            segments.remove(lower);
            s = new Segment(lower.start, Math.max(s.end, lower.end));
        }
        Segment higher = segments.ceiling(s);
        if (higher != null && s.end >= higher.start) {
            segments.remove(higher);
            s = new Segment(Math.min(higher.start, s.start), higher.end);
        }
        segments.add(s);
        if (SEGMENT_LIST_DEBUG_INFO.enabled()) debug_print();
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
            if (SEGMENT_LIST_DEBUG_INFO.enabled()) {
                Pathcrafter.LOGGER.warn("Encountered null floor when looking for floor of " + y + "!");
                Pathcrafter.LOGGER.warn("SegmentList at time " + time);
                for (Segment s : segments) {
                    Pathcrafter.LOGGER.warn("> " + s);
                }
            }
            return MIN_HEIGHT;
        }
        return floor.end;
    }

    public void mark(double minHeight, double maxHeight, double initialVal, double initialY, int curTick,
                     double nextSegDist, double maxSlack) {
        // Keeping it here for now. Up next, jump segment
        for (Segment s = segments.floor(new Segment(minHeight, minHeight));
             s != null && s.end <= maxHeight;
             s = segments.higher(s)) {

            int landingTick = jumpTicksToFallTo(s.end - initialY);
            double maxDist = PlayerSpeed.flatJumpDistances.get(landingTick).deltaX;
            double slack = Math.max(Math.min(maxDist - nextSegDist, maxSlack), 0) / SPRINT_SPEED;

            double newVal = initialVal + landingTick - slack;
            s.updateVal(newVal);
            if (SEGMENT_LIST_DEBUG_INFO.enabled()) {
                Pathcrafter.LOGGER.info(String.format("Args | minH %f | maxH %f | initVal %f | initY %f | curTick %d | maxDeltaDist %f | maxSlack %f",
                        minHeight, maxHeight, initialVal, initialY, curTick, nextSegDist, maxSlack));
                Pathcrafter.LOGGER.info(String.format("Landing tick %d | maxSlackDist %f | slack %f",
                        landingTick, nextSegDist - maxDist, slack));
                Pathcrafter.LOGGER.info(String.format("Marking segment %s as value %f (%f + %d - %f)",
                        s, newVal, initialVal, landingTick, slack));
            }
        }
    }

    public void debug_print() {
        if (!SEGMENT_LIST_ALLOW_INFO_CALL.enabled()) return;
        Pathcrafter.LOGGER.info("SegmentList at time " + time);
        for (Segment s : segments) {
            Pathcrafter.LOGGER.info("> " + s);
        }
    }
}

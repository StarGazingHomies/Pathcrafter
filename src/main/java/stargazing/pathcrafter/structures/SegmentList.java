package stargazing.pathcrafter.structures;

import org.jetbrains.annotations.NotNull;
import stargazing.pathcrafter.Pathcrafter;

import java.util.TreeSet;

import static stargazing.pathcrafter.Constants.*;
import static stargazing.pathcrafter.config.DebugToggles.SEGMENT_LIST_DEBUG_INFO;
import static stargazing.pathcrafter.config.DebugToggles.SEGMENT_LIST_ALLOW_INFO_CALL;

public class SegmentList {
    public static class Segment implements Comparable<Segment> {
        public final double start, end;
        public double val;
        public TerrainGraph.EdgeAction action;
        public Segment from;
        public Segment(double s, double e) {
            start = s;
            end = e;
            val = -1;
        }

        public String toString() {
            return String.format("Segment(%f, %f) -> %f [LastAction: %s]", start, end, val, action);
        }

        public void updateVal(double newVal, TerrainGraph.EdgeAction newAction, Segment from) {
            if (val == -1 || newVal < val) {
                //Pathcrafter.LOGGER.info(String.format("Updating value of segment %s to %f", this, newVal));
                val = newVal;
                action = newAction;
                this.from = from;
            }
        }

        @Override
        public int compareTo(@NotNull SegmentList.Segment o) {
            return Double.compare(start, o.start);
        }
    }

    public final double startDist;
    public final double endDist;
    public final TreeSet<Segment> segments;

    SegmentList(double startDist, double endDist) {
        this.startDist = startDist;
        this.endDist = endDist;
        segments = new TreeSet<>();
    }

    SegmentList(@NotNull SegmentList copy) {
        segments = (TreeSet<Segment>) copy.segments.clone();
        startDist = copy.startDist;
        endDist = copy.endDist;
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
                Pathcrafter.LOGGER.warn("SegmentList at time " + startDist);
                for (Segment s : segments) {
                    Pathcrafter.LOGGER.warn("> " + s);
                }
            }
            return MIN_HEIGHT;
        }
        return floor.end;
    }

    /**
     *
     * @param posY The current Y position of the jump
     * @param lastY The last Y position of the jump
     * @param start The start of the jump's range
     * @param end The end of the jump's range
     * @param curVal The number of ticks to get to this point in the jump
     * @param segEndDist The latest point at which the jump can be initiated
     * @param source The source of the jump
     * @return Whether the segment is intersected.
     */
    public boolean mark(double posY, double lastY, double start, double end,
                        double curVal, double maxSlack, double segEndDist, Segment source, int jumpTick) {
        double minY = Math.min(posY, lastY), maxY = Math.max(posY, lastY);
        Segment s = segments.floor(new Segment(maxY, maxY));
        if (SEGMENT_LIST_DEBUG_INFO.enabled())
            Pathcrafter.LOGGER.info(String.format("Potential jump collisions segment: %s", s));
        // No intersection yet.
        if (s == null) return false;
        if (s.end < minY) return false;
        if (s.end > maxY) return true;

        // See which part intersects
        // Start is currently not used :3
        double newVal = curVal - Math.min(maxSlack, end - startDist) / SPRINT_SPEED;

        // Update value
        if (SEGMENT_LIST_DEBUG_INFO.enabled())
            Pathcrafter.LOGGER.info(String.format("Updating value to %f (= %f - Math.min(%f, %f - %f) / %f)",
                    newVal, curVal, maxSlack, end, startDist, SPRINT_SPEED));
        s.updateVal(newVal,
                new TerrainGraph.EdgeAction(TerrainGraph.Edge.EdgeActionType.JUMP, source.end, segEndDist, jumpTick),
                source);

        return true;
    }

    public void debug_print() {
        if (!SEGMENT_LIST_ALLOW_INFO_CALL.enabled()) return;
        Pathcrafter.LOGGER.info("SegmentList at time " + startDist + " to " + endDist);
        for (Segment s : segments) {
            Pathcrafter.LOGGER.info("> " + s);
        }
    }
}

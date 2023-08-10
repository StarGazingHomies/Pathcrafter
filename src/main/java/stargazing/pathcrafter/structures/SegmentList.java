package stargazing.pathcrafter.structures;

import org.jetbrains.annotations.NotNull;
import stargazing.pathcrafter.Pathcrafter;

import java.util.TreeSet;

import static stargazing.pathcrafter.Constants.*;
import static stargazing.pathcrafter.config.DebugToggles.SEGMENT_LIST_DEBUG_INFO;
import static stargazing.pathcrafter.config.DebugToggles.SEGMENT_LIST_ALLOW_INFO_CALL;

public class SegmentList {
    public class Segment implements Comparable<Segment> {
        public final double start, end; // These are vertical.
        public double val;
        public double jumpStart, jumpEnd; // These are horizontal. I should come up with better names.
        public double jumpMomentum;
        public TerrainGraph.EdgeAction action;
        public Segment from;
        public Segment(double s, double e) {
            start = s;
            end = e;
            val = -1;
            jumpStart = startDist;
            jumpEnd = endDist;
            jumpMomentum = 0;
        }

        public String toString() {
            return String.format("Segment(%f, %f) -> %f (landing [%f, %f] | m: %f) [LastAction: %s]",
                    start, end, val, jumpStart, jumpEnd, jumpMomentum, action);
        }

        /**
         * Updates the value for a segment, used for starting segment & walking. No jump information.
         * @param newVal The new time it takes to get to the segment
         * @param newAction The last action to get to the segment
         * @param from The origin of the action
         */
        public void updateVal(double newVal, TerrainGraph.EdgeAction newAction, Segment from) {
            if (val == -1 || newVal < val) {
                //Pathcrafter.LOGGER.info(String.format("Updating value of segment %s to %f", this, newVal));
                val = newVal;
                action = newAction;
                this.from = from;
                this.jumpStart = startDist;
                this.jumpEnd = endDist;
                this.jumpMomentum = 0;
            }
        }

        /**
         * Updates the value for a segment, used for starting segment & walking. Has jump information.
         * @param newVal The new time it takes to get to the segment
         * @param newAction The last action to get to the segment
         * @param from The origin of the action
         * @param jumpStart The start of the area possible to land from a jump
         * @param jumpEnd The end of the area possible to land from a jump
         * @param jumpMomentum The momentum at the end of a jump
         */
        public void updateVal(double newVal, TerrainGraph.EdgeAction newAction, Segment from, double jumpStart, double jumpEnd, double jumpMomentum) {
            if (val == -1 || newVal < val) {
                //Pathcrafter.LOGGER.info(String.format("Updating value of segment %s to %f", this, newVal));
                val = newVal;
                action = newAction;
                this.from = from;
                this.jumpStart = jumpStart;
                this.jumpEnd = jumpEnd;
                this.jumpMomentum = jumpMomentum;
            }
        }

        @Override
        public int compareTo(@NotNull SegmentList.Segment o) {
            return Double.compare(start, o.start);
        }
    }

    public final double startDist;
    public final double endDist; // These are horizontal.
    public final TreeSet<Segment> segments; // This is vertical.

    SegmentList(double startDist, double endDist) {
        this.startDist = startDist;
        this.endDist = endDist;
        segments = new TreeSet<>();
    }

    SegmentList(@NotNull SegmentList copy) {
        segments = (TreeSet<Segment>) copy.segments.clone(); // What else am I supposed to do here, IntelliJ?
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

    public Segment floor(double y) {
        return segments.floor(new Segment(y, y));
    }

    /**
     *
     * @param posY The current Y position of the jump
     * @param lastY The last Y position of the jump
     * @param curRange The current jump range to intersect
     * @param curVal The number of ticks to get to this point in the jump
     * @param segEndDist The latest point at which the jump can be initiated
     * @param source The source of the jump
     * @return Whether the segment is intersected.
     */
    public boolean mark(double posY, double lastY, JumpRanges.JumpRange curRange,
                        double curVal, double maxSlack, double segEndDist, Segment source, int jumpTick) {
        double minY = Math.min(posY, lastY), maxY = Math.max(posY, lastY);
        Segment s = segments.floor(new Segment(maxY, maxY));
        if (SEGMENT_LIST_DEBUG_INFO.enabled())
            Pathcrafter.LOGGER.info(String.format("Potential jump collisions segment: %s", s));
        // No intersection yet.
        if (s == null) return false;
        if (s.end < minY) return false;
        if (s.end > maxY) return true;

        double[] nextTick = curRange.peekNextTick();
        double start = nextTick[0];
        double end = nextTick[1];
        // Very rudimentary momentum handling - just saving it alongside.
        // Not correct, but close enough.
        double velEnd = nextTick[3];

        // See which part intersects
        // Start is currently not used :3
        double newVal = curVal - Math.min(maxSlack, end - startDist) / SPRINT_SPEED;

        // Update value
        if (SEGMENT_LIST_DEBUG_INFO.enabled())
            Pathcrafter.LOGGER.info(String.format("Updating value to %f (= %f - Math.min(%f, %f - %f) / %f)",
                    newVal, curVal, maxSlack, end, startDist, SPRINT_SPEED));
        s.updateVal(newVal,
                new TerrainGraph.EdgeAction(TerrainGraph.Edge.EdgeActionType.JUMP, source.end, segEndDist, jumpTick),
                source,
                Math.max(start, this.startDist),
                Math.min(end, this.endDist),
                velEnd);

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

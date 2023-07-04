package stargazing.pathcrafter.structures;

import org.jetbrains.annotations.NotNull;

import java.util.TreeSet;

public class JumpSegment {
    // Not used for jumping v0.5, prep for jumping v1

    // At some point in the future ("Advanced Jumping"), this needs to become a 2d shape,
    // a circle, with rectangular cutouts for whenever a collision happens.
    // but for now, 1d is enough.
    // Also, currently there's no idea of (if headhitting) multiple jumps within the span of 1 block
    // although this is only really applicable for trap door headhitters (2gt / jump)
    // This will also present some inaccuracies compared to pure sprint jumping.

    // Note to self: In the future, keep track of lastLanding data per segment,
    // Keep lastLanding data if sprinting and y-level is exactly equal,
    // and then do an appropriate offset when taking the next jump
    // to mitigate these inaccuracies
    int tick = 0;
    public static class Range implements Comparable<Range> {
        double start, end;
        int fallTick;

        public Range(double s, double e) {
            start = s; end = e;
        }

        public int remove(double s, double e) {
            if (s <= start && end <= e) return -1; // Completely contain
            if (e <= start || end <= s) return 0;  // Irrelevant
            if (start < s && e < end) return 1;  // Split
            if (s < start) {
                start = e;
            }
            else {
                end = s;
            }
            return 0;
        }

        @Override
        public int compareTo(@NotNull JumpSegment.Range o) {
            return Double.compare(start, o.start);
        }
    }
    private final TreeSet<Range> ranges = new TreeSet<>();
    public JumpSegment(double start, double end) {
        ranges.add(new Range(start, end));
    }

    public void collide(double start, double end) {
        // Filter out irrelevant segments
        if (start > getEnd() || end < getStart()) return;

        // Go through all impacted segments
        for (Range cur = ranges.floor(new Range(start, start));
        cur != null && cur.start <= end;
        cur = ranges.higher(cur)) {
            int result = cur.remove(start, end);
            if (result == -1) {
                ranges.remove(cur);
            }
            if (result == 1) {
                ranges.remove(cur);
                ranges.add(new Range(cur.start, start));
                ranges.add(new Range(end, cur.end));
            }
        }
    }

    public double getStart() {
        return ranges.first().start;
    }
    public double getEnd() {
        return ranges.last().end;
    }
}

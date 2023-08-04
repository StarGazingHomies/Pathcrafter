package stargazing.pathcrafter.structures;

import org.jetbrains.annotations.NotNull;
import stargazing.pathcrafter.Pathcrafter;
import stargazing.pathcrafter.util.PlayerSpeed;

import java.util.TreeSet;

import static stargazing.pathcrafter.Constants.*;

public class JumpRanges {
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
    double velY = 0, posY = 0, lastY = 0;

    public static class JumpRange implements Comparable<JumpRange> {
        double start, end, velStart = 0, velEnd = 0;
        int tick = 0;

        public JumpRange(double s, double e) {
            start = s; end = e;
        }
        public JumpRange(double s, double e, double sVel, double eVel, int t) {
            start = s; end = e; velStart = sVel; velEnd = eVel; tick = t;
        }

        public String toString() {
            return String.format("JumpRange from (%.3f | %.3f) to (%.3f | %.3f) at internal tick %d",
                    start, velStart, end, velEnd, tick);
        }

        public void advanceTick() {
            if (tick == 0) {
                velStart = velStart * HORIZONTAL_DRAG +
                        HORIZONTAL_ACCELERATION_GROUND * SPRINT_FACTOR * STRAIGHT_FACTOR +
                        SPRINT_JUMP_MODIFIER;
                velEnd = velEnd * HORIZONTAL_DRAG +
                        HORIZONTAL_ACCELERATION_GROUND * SPRINT_FACTOR * STRAIGHT_FACTOR +
                        SPRINT_JUMP_MODIFIER;
            }
            else if  (tick == 1) {
                velStart = velStart * HORIZONTAL_DRAG * HORIZONTAL_DRAG_GROUND_MULTIPLIER + HORIZONTAL_ACCELERATION_AIR * SPRINT_FACTOR;
                velEnd = velEnd * HORIZONTAL_DRAG * HORIZONTAL_DRAG_GROUND_MULTIPLIER + HORIZONTAL_ACCELERATION_AIR * SPRINT_FACTOR;
            } else {
                velStart = velStart * HORIZONTAL_DRAG + HORIZONTAL_ACCELERATION_AIR * SPRINT_FACTOR;
                velEnd = velEnd * HORIZONTAL_DRAG + HORIZONTAL_ACCELERATION_AIR * SPRINT_FACTOR;
            }
            //start += velStart;
            end += velEnd;
            tick++;
        }

        public int remove(double s, double e) {
            if (s <= start && end <= e) return -1; // Completely contain (# of ranges -= 1)
            if (e <= start || end <= s) return 0;  // Irrelevant (# of ranges stay the same)
            if (start < s && e < end) return 1;  // Split (# of ranges += 1)
            if (s <= start) {
                start = e;
            }
            else {
                end = s;
            }
            return 0;
        }

        @Override
        public int compareTo(@NotNull JumpRanges.JumpRange o) {
            return Double.compare(start, o.start);
        }
    }
    public final TreeSet<JumpRange> ranges = new TreeSet<>();
    public JumpRanges(double start, double end, double y) {
        this.posY = y;
        this.lastY = y;
        ranges.add(new JumpRange(start, end));
    }

    public void advanceTickY() {
        // Should be called before X, since Minecraft does Y movement, then X movement.
        if (tick == 0) {
            velY += 0.42;
        }
        else {
            velY = (velY - GRAVITY_MODIFIER) * VERTICAL_DRAG;
        }
        lastY = posY;
        posY += velY;
        tick++;
    }

    public void advanceTickX() {
        for (JumpRange r : ranges) {
            r.advanceTick();
        }
    }

    public void collide(double start, double end) {
        // Head: immediately start going down.
        // Split into multiple segments if needed
        // since jumpSegment operates on a single y-level / tier.

        // Front / back: modify endpoints
        // Feet: land, and modify endpoints
        // System.out.printf("JumpRanges: Start: %f | End: %f\n", getStart(), getEnd());

        // Filter out irrelevant segments
        if (start > getEnd() || end < getStart()) return;

        //System.out.print("Going into loop.\n");
        double maxStart = Math.max(start, getStart());
        // Go through all impacted segments
        for (JumpRange cur = ranges.floor(new JumpRange(maxStart, maxStart));
             cur != null && cur.start <= end;
             cur = ranges.higher(cur)) {
            //System.out.printf("Checking: %s\n", cur);
            int result = cur.remove(start, end);
            //System.out.printf("Result: %d\n", result);
            //System.out.printf("New region: %s\n", cur);
            if (result == -1) {
                ranges.remove(cur);
            }
            if (result == 1) {
                ranges.remove(cur);
                ranges.add(new JumpRange(cur.start, start, cur.velStart, 0, cur.tick));
                ranges.add(new JumpRange(end, cur.end, 0, cur.velEnd, cur.tick));
            }
        }
    }


    public double getStart() {
        return ranges.first().start;
    }
    public double getEnd() {
        return ranges.last().end;
    }
    public String toString() {
        return String.format("JumpRange at (y=%.3f | vY=%.3f) with %d ranges", posY, velY, ranges.size());
    }

    public void debugPrint() {
        System.out.printf("%s\n", this);
        for (JumpRange range : ranges) {
            System.out.printf("- %s\n", range);
        }
    }

    public void debugLog() {
        Pathcrafter.LOGGER.info(String.format("%s", this));
        for (JumpRange range : ranges) {
            Pathcrafter.LOGGER.info(String.format("- %s", range));
        }
    }

    public static void main(String[] args) {
        // Again, a main class for testing purposes. Will be removed once I'm sure the code works correctly
        // (and then I will have to rewrite it again to validate functionality...)
        PlayerSpeed.main(args);
        JumpRanges seg = new JumpRanges(0, 0, 0);
        for (int i=0; i<17; i++) {
            seg.advanceTickY();
            seg.debugPrint();
        }
        System.out.println("-------------------------------------------");
        seg = new JumpRanges(0, 3, 0);
        seg.collide(1, 2);
        seg.debugPrint();
        System.out.println("-------------------------------------------");
        seg = new JumpRanges(0, 3, 0);
        seg.collide(1, 4);
        seg.debugPrint();
        System.out.println("-------------------------------------------");
        seg = new JumpRanges(0, 3, 0);
        seg.collide(-1, 2);
        seg.debugPrint();
        System.out.println("-------------------------------------------");
        seg = new JumpRanges(0, 3, 0);
        seg.collide(-1, 5);
        seg.debugPrint();
        System.out.println("-------------------------------------------");
    }
}

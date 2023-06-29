/**
 * Utility functions related to the player's speed and momentum
 * @author StarGazingHomies
 */

package stargazing.pathcrafter.util;

import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;

import static stargazing.pathcrafter.Constants.*;

public class PlayerSpeed {

    public static class JumpData {
        public int ticksElapsed;
        public double minY, maxY, deltaX;

        JumpData(int tick, double minY, double maxY, double deltaX) {
            this.ticksElapsed = tick;
            this.minY = minY;
            this.maxY = maxY;
            this.deltaX = deltaX;
        }
    }

    public static final int JUMP_DATA_START_TICK = 7;
    public static final ArrayList<JumpData> flatJumpDistances = new ArrayList<>();

    public static void initializeFreeFallData() {

    }

    /*
     For ticksToFallTo(double y).
     This one uses something kinda different.
     The base y velocity formula is:
     velY_(i+1) = (velY_i - GRAVITY_MODIFIER) * VERTICAL_DRAG;
     However, we can simplify into a geometric sequence + a constant
     (velY_(i+1) + k) = VERTICAL_DRAG * (velY_i + k) for some k.
     This k is equal to VERTICAL_DRAG * GRAVITY_MODIFIER / (1 - VERTICAL_DRAG)
     Let y'_i = y_i + k
    */
    static final double k = VERTICAL_DRAG * GRAVITY_MODIFIER / (1 - VERTICAL_DRAG);

    public static int ticksToFallTo(double y) {
        // Since it's free fall, initial y' is just k.
        // double y'_0 = k; (not necessary, we'll just use k directly)

        // pos_j = sum( i from 1 to j : y_i )
        // therefore pos_j = sum( i from 1 to j : y'_i ) - j * k;
        // Using geometric seq. sum, pos_j = kd(1-d^j) / (1-d) - j * k
        // Now we're solving for the first j such that pos_j < y
        // Could binary search for a log(n) solution... but could we just use newton's method a few times?



        // OR, since I'm too lazy, just...

        double pos = 0, vy = 0;
        int t = 0;
        while (y < pos) {
            vy = (vy - GRAVITY_MODIFIER) * VERTICAL_DRAG;
            pos += vy;
            t++;
        }
        return t;
    }

    public static void initializeJumpData(double momentumX, boolean Strafe45) {
        // Initial state
        // Assume 0 momentum for now.
        int tick = 0;
        double posX = 0.0, posY = 0.0;
        double velX = momentumX, velY = 0.0, lastY=0.0;

        // Jump (tick 1)
        tick = 1;
        velY += 0.42;
        posY += velY;
        velX = velX * HORIZONTAL_DRAG +
                HORIZONTAL_ACCELERATION_GROUND * SPRINT_FACTOR * (Strafe45?1.0:STRAIGHT_FACTOR) +
                SPRINT_JUMP_MODIFIER;
        posX += velX;
        flatJumpDistances.add(
                new JumpData(
                        tick, posY, lastY, posX
                )
        );

        tick = 2;
        for (;posY>-320;tick++) {
            velY = (velY - GRAVITY_MODIFIER) * VERTICAL_DRAG;
            lastY = posY;
            posY += velY;

            if (tick == 2) {
                velX = velX * HORIZONTAL_DRAG * HORIZONTAL_DRAG_GROUND_MULTIPLIER + HORIZONTAL_ACCELERATION_AIR * SPRINT_FACTOR;
            } else {
                velX = velX * HORIZONTAL_DRAG + HORIZONTAL_ACCELERATION_AIR * SPRINT_FACTOR;
            }
            posX += velX;

            //if (tick >= 7) {
                flatJumpDistances.add(
                        new JumpData(
                            tick, posY, lastY, posX
                        )
                );
            //}
        }
    }

    static {
        // initializeJumpData(FLAT_MOMENTUM, true);
        initializeJumpData(0.0, false);
    }

    public static void main(String[] args) {
        // Testing function
        for (JumpData jd:flatJumpDistances) {
            System.out.printf("Tick %d | Y: [%f, %f) | maxX: %f\n", jd.ticksElapsed, jd.minY, jd.maxY, jd.deltaX);
        }
    }
}

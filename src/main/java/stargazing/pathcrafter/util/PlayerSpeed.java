/**
 * Utility functions related to the player's speed and momentum
 */

package stargazing.pathcrafter.util;

import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;

public class PlayerSpeed {
    public static final double SPRINT_SPEED = 5.612 / 20.0;
    public static final double WALK_SPEED = 4.317 / 20.0;
    // Handy shortcut for flat ground sprint jumping
    public static final double SPRINT_JUMP_SPEED = 7.127 / 20.0;

    // It takes 12gt to land at the same y-level after jumping.
    public static final int FLAT_JUMP_TICKS = 12;
    public static final double FLAT_MOMENTUM = 0.3176;

    public static final double GRAVITY_MODIFIER = 0.08;
    public static final double VERTICAL_DRAG = 0.98, HORIZONTAL_DRAG = 0.91;
    public static final double HORIZONTAL_DRAG_GROUND_MULTIPLIER = 0.6; // Air --> 1.0
    public static final double HORIZONTAL_ACCELERATION_AIR = 0.02, HORIZONTAL_ACCELERATION_GROUND = 0.1;
    public static final double SPRINT_FACTOR = 1.3;
    public static final double STRAIGHT_FACTOR = 0.98; // 45-Strafe --> 1.0
    public static final double SPRINT_JUMP_MODIFIER = 0.2;

    public static class JumpData {
        int ticksElapsed;
        double minY, maxY, deltaX;

        JumpData(int tick, double minY, double maxY, double deltaX) {
            this.ticksElapsed = tick;
            this.minY = minY;
            this.maxY = maxY;
            this.deltaX = deltaX;
        }
    }

    public static final int JUMP_DATA_START_TICK = 7;
    public static final ArrayList<JumpData> flatJumpDistances = new ArrayList<>();

    public static void initializeJumpData(double momentumX, boolean Strafe45) {
        // Initial state
        // Assume 0 momentum for now.
        int tick = 0;
        double posX = 0.0, posY = 0.0;
        double velX = momentumX, velY = 0.0, lastY;

        // Jump (tick 1)
        tick = 1;
        velY += 0.42;
        posY += velY;
        velX = velX * HORIZONTAL_DRAG +
                HORIZONTAL_ACCELERATION_GROUND * SPRINT_FACTOR * (Strafe45?1.0:STRAIGHT_FACTOR) +
                SPRINT_JUMP_MODIFIER;
        posX += velX;

        // Start storing from tick 7 as the player starts falling down.
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

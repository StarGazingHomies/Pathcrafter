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

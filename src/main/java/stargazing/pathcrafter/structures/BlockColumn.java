package stargazing.pathcrafter.structures;

import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.NotNull;
import stargazing.pathcrafter.Pathcrafter;

import java.util.ArrayList;
import java.util.stream.Stream;

import static stargazing.pathcrafter.Constants.*;

/**
 * Stores the relevant horizontal surfaces for a column of blocks.
 */
public class BlockColumn {

    public static class HorizontalSurface {
        public double x1, x2, z1, z2, y;
        // True if the surface is "top", false, if the surface is "bottom"
        public boolean type;

        HorizontalSurface(double x1, double x2, double z1, double z2, double y, boolean type) {
            this.x1 = x1;
            this.x2 = x2;
            this.y = y;
            this.z1 = z1;
            this.z2 = z2;
            this.type = type;
        }

        boolean hasSameDimensions(HorizontalSurface other) {
            return this.x1 == other.x1 && this.z1 == other.z1 &&
                    this.x2 == other.x2 && this.z2 == other.z2;
        }
    }

    public final int blockX, blockZ;

    public final ArrayList<HorizontalSurface> surfaces = new ArrayList<>();

    private int lastYOffset = MIN_HEIGHT - 1;

    public BlockColumn(int x, int z) {
        blockX = x;
        blockZ = z;
    }

    public String toString() {
        return String.format("BlockColumn(%d, %d)", blockX, blockZ);
    }

    /**
     * Adds a block to the BlockColumn. Does automatic modification of other affected layers.
     * @param shape The shape of the block's collision box
     * @param yOffset The y position of the given block
     */
    public void addBlock(@NotNull VoxelShape shape, int yOffset) {
        assert yOffset > lastYOffset;    // The column should be assembled in a bottom to top fashion.
        lastYOffset = yOffset;

        if (shape.isEmpty()) return;     // If the shape is empty, nothing needs to be done.

        // Get the bounding box
        double minX = shape.getMin(Direction.Axis.X);
        double maxX = shape.getMax(Direction.Axis.X);
        double minY = shape.getMin(Direction.Axis.Y) + yOffset - PLAYER_HEIGHT;
        double maxY = shape.getMax(Direction.Axis.Y) + yOffset;
        double minZ = shape.getMin(Direction.Axis.Z);
        double maxZ = shape.getMax(Direction.Axis.Z);

        // Very crude "does the player fit" checking, only usable for solid blocks.
        int topSurfaceIndex = surfaces.size() - 1;
        HorizontalSurface newTopSurface = new HorizontalSurface(minX, maxX, minZ, maxZ, maxY, true);
        if (!surfaces.isEmpty() &&
                surfaces.get(topSurfaceIndex).y > minY &&
                surfaces.get(topSurfaceIndex).hasSameDimensions(newTopSurface)) {
            surfaces.remove(topSurfaceIndex);
            surfaces.add(newTopSurface);
        }
        else {
            surfaces.add(new HorizontalSurface(minX, maxX, minZ, maxZ, minY, false));
            surfaces.add(newTopSurface);
        }
    }

    public void debug_logSurfaces() {
        Pathcrafter.LOGGER.info(String.format("Block colum at x: %d, z: %d:", blockX, blockZ));
        for (HorizontalSurface surface : surfaces) {
            Pathcrafter.LOGGER.info(String.format("Surface at y: %f | x [%f - %f] | z [%f - %f]",
                    surface.y, surface.x1, surface.x2, surface.z1, surface.z2));
        }
    }

    /**
     * Get the y-coordinate of the surface.
     * @param i The index of the surface.
     * @return The y-coordinate of the surface, or MAX_HEIGHT * 2 if index out of range.
     */
    public double getY(int i) {
        return i >= surfaces.size() ? MAX_HEIGHT * 2 :surfaces.get(i).y;
    }

    /**
     * Get the openness of the column at a given index
     * @param i The index to get openness at
     * @return False if it's air, true if it is solid
     */
    public boolean getState(int i) {
        return i < surfaces.size() && surfaces.get(i).type;
    }

    public double getNextStepY(int i) {
        if (!getState(i)) return getY(i);
        else return getY(i+1);
    }
}
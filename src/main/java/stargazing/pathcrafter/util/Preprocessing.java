package stargazing.pathcrafter.util;

import net.minecraft.state.property.Property;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.NotNull;
import stargazing.pathcrafter.Pathcrafter;

import java.util.ArrayList;

import static stargazing.pathcrafter.Constants.*;

public class Preprocessing {
    // Generates vertices and edges from terrain

    public static class HorizontalSurface {
        public double minX, maxX, minZ, maxZ, y;

        HorizontalSurface(double minX, double maxX, double minZ, double maxZ, double y) {
            this.minX = minX;
            this.maxX = maxX;
            this.y = y;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }
    }

    /**
     * Stores the relevant horizontal surfaces for a column of blocks.
     */
    public static class BlockColumn {

        private final int blockX, blockZ;

        private final ArrayList<HorizontalSurface> topSurfaces = new ArrayList<>();
        // Only used for [valid jump] checks until head hitting
        private final ArrayList<HorizontalSurface> bottomSurfaces = new ArrayList<>();

        private int lastYOffset = MIN_HEIGHT - 1;

        BlockColumn(int x, int z) {
            blockX = x;
            blockZ = z;
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
            double minY = shape.getMin(Direction.Axis.Y) + yOffset;
            double maxY = shape.getMax(Direction.Axis.Y) + yOffset;
            double minZ = shape.getMin(Direction.Axis.Z);
            double maxZ = shape.getMax(Direction.Axis.Z);

            // Very crude "does the player fit" checking, only usable for solid blocks.
            int topSurfaceIndex = topSurfaces.size() - 1;
            if (!topSurfaces.isEmpty() && topSurfaces.get(topSurfaceIndex).y + PLAYER_HEIGHT > minY) {
                topSurfaces.remove(topSurfaceIndex);
                topSurfaces.add(new HorizontalSurface(minX, maxX, minZ, maxZ, maxY));
            }
            else {
                topSurfaces.add(new HorizontalSurface(minX, maxX, minZ, maxZ, maxY));
                bottomSurfaces.add(new HorizontalSurface(minX, maxX, minZ, maxZ, minY));
            }
        }

        public void getTopSurfaceAt(double yLevel) {
            // Binary search because the list is sorted
            int minIndex = 0, maxIndex = topSurfaces.size();
        }

        public void debug_logSurfaces() {
            assert topSurfaces.size() == bottomSurfaces.size();
            Pathcrafter.LOGGER.info(String.format("Block colum at x: %d, z: %d:", blockX, blockZ));
            for (int i=0; i<topSurfaces.size(); i++) {
                Pathcrafter.LOGGER.info(String.format("Bottom: %f | Top: %f",
                        bottomSurfaces.get(i).y, topSurfaces.get(i).y));
            }
        }

    }

    /**
     * Convert a chunk into stacks of top and bottom surfaces
     * @param c Chunk
     * @param client Minecraft Client
     */
    public static void processChunk(Chunk c, MinecraftClient client) {
        assert client.world != null;
        // Initialize the columns
        BlockColumn[][] columns = new BlockColumn[CHUNK_SIZE][CHUNK_SIZE];
        for (int x=0; x<CHUNK_SIZE; x++) {
            for (int z=0; z<CHUNK_SIZE; z++) {
                columns[x][z] = new BlockColumn(x, z);
            }
        }

        for (ChunkSection cs : c.getSectionArray()) {
            int subchunkY = cs.getYOffset();
            Pathcrafter.LOGGER.info(String.format("Chunk Y offset: %d", subchunkY));
            for (int i=0; i<CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE; i++) {
                // Unpack x, y, z for sub chunk
                int x = i%CHUNK_SIZE;
                int z = (i/CHUNK_SIZE)%CHUNK_SIZE;
                int y = i/CHUNK_SIZE/CHUNK_SIZE;
                BlockState bs = cs.getBlockState(x, y, z);
                // Ignore air blocks
                if (bs.getMaterial() == Material.AIR) {
                    continue;
                }

                // Get the shape of the block
                VoxelShape collisionShape = bs.getCollisionShape(client.world, new BlockPos(x, y+subchunkY, z));

                // Debugging
                String stringRepresentation =
                        String.format(
                                "x: %d | y: %d | z: %d | %s | Bounds: %s",
                                x, y+subchunkY, z,
                                bs.getBlock().getName().toString(),
                                collisionShape.toString());
                Pathcrafter.LOGGER.info(stringRepresentation);

                columns[x][z].addBlock(collisionShape, y+subchunkY);
            }
        }

        // Debugging
        for (int x=0; x<CHUNK_SIZE; x++) {
            for (int z=0; z<CHUNK_SIZE; z++) {
                columns[x][z].debug_logSurfaces();
            }
        }
    }

}

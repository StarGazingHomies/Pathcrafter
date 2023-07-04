package stargazing.pathcrafter.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.NotNull;
import stargazing.pathcrafter.Pathcrafter;
import stargazing.pathcrafter.structures.BlockColumn;

import static stargazing.pathcrafter.Constants.*;
import static stargazing.pathcrafter.config.DebugToggles.BLOCK_COLUMN_DEBUG_INFO;

public class Preprocessing {

    /**
     * Convert a chunk into stacks of top and bottom surfaces
     * @param c Chunk
     * @param client Minecraft Client
     */
    public static BlockColumn[][] processChunk(Chunk c, MinecraftClient client) {
        assert client.world != null;
        assert c != null;
        // Initialize the columns
        ChunkPos p = c.getPos();
        BlockColumn[][] columns = new BlockColumn[CHUNK_SIZE][CHUNK_SIZE];
        for (int x=0; x<CHUNK_SIZE; x++) {
            for (int z=0; z<CHUNK_SIZE; z++) {
                columns[x][z] = new BlockColumn(x + p.x * CHUNK_SIZE, z + p.z * CHUNK_SIZE);
            }
        }

        for (ChunkSection cs : c.getSectionArray()) {
            int subchunkY = cs.getYOffset();
            // Debug info
            if (BLOCK_COLUMN_DEBUG_INFO.enabled())
                Pathcrafter.LOGGER.info(String.format("Chunk Y offset: %d", subchunkY));

            for (int i=0; i<CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE; i++) {
                // Unpack x, y, z for sub chunk
                int x = i%CHUNK_SIZE;
                int z = (i/CHUNK_SIZE)%CHUNK_SIZE;
                int y = i/CHUNK_SIZE/CHUNK_SIZE;
                BlockState bs = cs.getBlockState(x, y, z);
                // Ignore water and lava blocks for now
                if (bs.getMaterial() == Material.WATER || bs.getMaterial() == Material.LAVA) {
                    continue;
                }

                // Get the shape of the block
                VoxelShape collisionShape = bs.getCollisionShape(client.world, new BlockPos(x, y+subchunkY, z));

                // Ignore blocks without collision
                if (collisionShape.isEmpty()) {
                    continue;
                }

                columns[x][z].addBlock(collisionShape, y+subchunkY);
            }
        }

        // Debugging
        if (BLOCK_COLUMN_DEBUG_INFO.enabled()) {
            for (int x=0; x<CHUNK_SIZE; x++) {
                for (int z=0; z<CHUNK_SIZE; z++) {
                    columns[x][z].debug_logSurfaces();
                }
            }
        }
        return columns;
    }

}

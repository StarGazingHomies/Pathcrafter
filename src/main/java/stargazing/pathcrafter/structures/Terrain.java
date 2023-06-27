package stargazing.pathcrafter.structures;

import net.minecraft.world.chunk.Chunk;
import stargazing.pathcrafter.Pathcrafter;
import stargazing.pathcrafter.util.Preprocessing;
import stargazing.pathcrafter.util.World;

import java.nio.file.Path;
import java.util.ArrayList;

import static stargazing.pathcrafter.Constants.*;

public class Terrain {
    int minX, minZ, maxX, maxZ;
    // Note that y ranges aren't important, we are processing all the blocks in these chunks anyway

    BlockColumn[][] columns;
    TerrainGraph graph = new TerrainGraph();

    public Terrain(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        // Establish ranges
        minX = Math.min(startX, endX) - MAX_SEARCH_RANGE;
        minZ = Math.min(startZ, endZ) - MAX_SEARCH_RANGE;
        maxX = Math.max(startX, endX) + MAX_SEARCH_RANGE;
        maxZ = Math.max(startZ, endZ) + MAX_SEARCH_RANGE;
        genColumns();
    }

    public BlockColumn getColumn(int x, int z) {
        return columns[x - minX][z - minZ];
    }

    /**
     * Loads chunks and generates columns for the search area
     */
    public void genColumns() {
        int chunkMinX = Math.floorDiv(minX, CHUNK_SIZE); // Gah, I hate java division w/ negatives sometimes
        int chunkMinZ = Math.floorDiv(minZ, CHUNK_SIZE);
        int chunkMaxX = Math.floorDiv(maxX, CHUNK_SIZE) + 1;
        int chunkMaxZ = Math.floorDiv(maxZ, CHUNK_SIZE) + 1;

        columns = new BlockColumn[maxX - minX + 1][maxZ - minZ + 1];

        long startTime = System.nanoTime();
        for (int chunkX = chunkMinX; chunkX < chunkMaxX; chunkX++) {
            for (int chunkZ = chunkMinZ; chunkZ < chunkMaxZ; chunkZ++) {
                Chunk c = World.getChunk(chunkX,chunkZ);
                BlockColumn[][] processedChunk = Preprocessing.processChunk(c, World.getClient());

                // Copy data
                for (int x=0; x<CHUNK_SIZE; x++) {
                    // Absolute X position
                    int absX = x + chunkX * 16;
                    // Check if within bounds
                    if (absX < minX) continue;
                    if (absX > maxX) break;
                    for (int z=0; z<CHUNK_SIZE; z++) {
                        int absZ = z + chunkZ * 16;
                        if (absZ < minZ) continue;
                        if (absZ > maxZ) break;
                        columns[absX - minX][absZ - minZ] = processedChunk[x][z];
                        if (TERRAIN_DEBUG_INFO)
                            Pathcrafter.LOGGER.info(
                                    String.format("Finished generating column at (%d, %d)", absX, absZ));
                    }
                }
            }
        }
        if (TERRAIN_DEBUG_INFO) {
            long endTime = System.nanoTime();
            Pathcrafter.LOGGER.info(
                    String.format("Chunk data generation took %f seconds", (endTime - startTime) / 1.0e9));
        }
    }

    /**
     * Computes all relevant vertices for
     */
    public void findVertices() {
        long startTime = System.nanoTime();

        // Start and end vertices


        for (int x = minX; x < maxX - 1; x++) {
            for (int z = minZ; z < maxZ - 1; z++) {
                Pathcrafter.LOGGER.info(String.format("%d, %d", x, z));
                findVerticesAt(x, z);
            }
        }


        if (TERRAIN_DEBUG_INFO) {
            long endTime = System.nanoTime();
            Pathcrafter.LOGGER.info(
                    String.format("Vertex generation took %f seconds total.", (endTime - startTime) / 1.0e9));
            Pathcrafter.LOGGER.info(String.format("Vertex count: %d", graph.vertices.size()));
        }
    }

    public void findVerticesAt(int x, int z) {
        long startTime = System.nanoTime();
        // Look for vertices close to the vertex of original blocks.
        // i.e. the corners of blocks
        // For now, this simple algo of a shitton of if/else & switch statements will do.

        // To merge 4 sorted lists together... probably no need to use minheap, but is always an option.

        BlockColumn column0 = getColumn(x, z);
        BlockColumn column1 = getColumn(x, z+1);
        BlockColumn column2 = getColumn(x+1, z);
        BlockColumn column3 = getColumn(x+1, z+1);

        int i0=0, i1=0, i2=0, i3=0;
        while (i0 < column0.surfaces.size() ||
                i1 < column1.surfaces.size() ||
                i2 < column2.surfaces.size() ||
                i3 < column3.surfaces.size()) {
            double y0 = column0.getY(i0);
            double y1 = column1.getY(i1);
            double y2 = column2.getY(i2);
            double y3 = column3.getY(i3);
            double curY = Math.min(Math.min(y0, y1), Math.min(y2, y3));
            if (TERRAIN_DEBUG_INFO)
                Pathcrafter.LOGGER.info(String.format("Currently at y level %f", curY));

            // y coordinates match and it is a top surface
            boolean state0 = column0.getState(i0);
            boolean state1 = column1.getState(i1);
            boolean state2 = column2.getState(i2);
            boolean state3 = column3.getState(i3);
            boolean match0 = y0 == curY;
            boolean match1 = y1 == curY;
            boolean match2 = y2 == curY;
            boolean match3 = y3 == curY;
            boolean valid0 = match0 && state0;
            boolean valid1 = match1 && state1;
            boolean valid2 = match2 && state2;
            boolean valid3 = match3 && state3;
            int matchState = (valid0 ? 1 : 0) + (valid1 ? 2 : 0) + (valid2 ? 4 : 0) + (valid3 ? 8 : 0);

            if (match0) i0++;
            if (match1) i1++;
            if (match2) i2++;
            if (match3) i3++;

            // Big switch statement
            switch (matchState) {
                // 1 vertex
                case 1:
                    if (state1 && state2) {
                        graph.vertices.add(createVertex(x, curY, z, 0));
                    }
                    else if (state1) {
                        graph.vertices.add(createVertex(x, curY, z, 2));
                    }
                    else if (state2) {
                        graph.vertices.add(createVertex(x, curY, z, 1));
                    }
                    else if (state3) {
                        graph.vertices.add(createVertex(x, curY, z, 0));
                        graph.vertices.add(createVertex(x, curY, z, 1));
                        graph.vertices.add(createVertex(x, curY, z, 2));
                    }
                    else {
                        graph.vertices.add(createVertex(x, curY, z, 3));
                    }
                    break;
                case 2:
                    if (state0 && state3) {
                        graph.vertices.add(createVertex(x, curY, z, 1));
                    }
                    else if (state0) {
                        graph.vertices.add(createVertex(x, curY, z, 3));
                    }
                    else if (state3) {
                        graph.vertices.add(createVertex(x, curY, z, 0));
                    }
                    else if (state2) {
                        graph.vertices.add(createVertex(x, curY, z, 1));
                        graph.vertices.add(createVertex(x, curY, z, 3));
                        graph.vertices.add(createVertex(x, curY, z, 0));
                    }
                    else {
                        graph.vertices.add(createVertex(x, curY, z, 2));
                    }
                    break;
                case 4:
                    if (state0 && state3) {
                        graph.vertices.add(createVertex(x, curY, z, 2));
                    }
                    else if (state0) {
                        graph.vertices.add(createVertex(x, curY, z, 3));
                    }
                    else if (state3) {
                        graph.vertices.add(createVertex(x, curY, z, 0));
                    }
                    else if (state1) {
                        graph.vertices.add(createVertex(x, curY, z, 1));
                        graph.vertices.add(createVertex(x, curY, z, 3));
                        graph.vertices.add(createVertex(x, curY, z, 0));
                    }
                    else {
                        graph.vertices.add(createVertex(x, curY, z, 1));
                    }
                    break;
                case 8:
                    if (state1 && state2) {
                        graph.vertices.add(createVertex(x, curY, z, 3));
                    }
                    else if (state1) {
                        graph.vertices.add(createVertex(x, curY, z, 2));
                    }
                    else if (state2) {
                        graph.vertices.add(createVertex(x, curY, z, 1));
                    }
                    else if (state0) {
                        graph.vertices.add(createVertex(x, curY, z, 0));
                        graph.vertices.add(createVertex(x, curY, z, 1));
                        graph.vertices.add(createVertex(x, curY, z, 2));
                    }
                    else {
                        graph.vertices.add(createVertex(x, curY, z, 0));
                    }
                    break;

                // 3 vertex
                case 7:
                    if (state3) {
                        graph.vertices.add(createVertex(x, curY, z, 0));
                    }
                    else {
                        graph.vertices.add(createVertex(x, curY, z, 3));
                    }
                    break;
                case 11:
                    if (state2) {
                        graph.vertices.add(createVertex(x, curY, z, 1));
                    }
                    else {
                        graph.vertices.add(createVertex(x, curY, z, 2));
                    }
                    break;
                case 13:
                    if (state1) {
                        graph.vertices.add(createVertex(x, curY, z, 2));
                    }
                    else {
                        graph.vertices.add(createVertex(x, curY, z, 1));
                    }
                    break;
                case 14:
                    if (state0) {
                        graph.vertices.add(createVertex(x, curY, z, 3));
                    }
                    else {
                        graph.vertices.add(createVertex(x, curY, z, 0));
                    }
                    break;

                // 2 vertex diagonal
                case 6:
                    if (state0 && state3) {
                        graph.vertices.add(createVertex(x, curY, z, 1));
                        graph.vertices.add(createVertex(x, curY, z, 2));
                    }
                    else if (state0) {
                        graph.vertices.add(createVertex(x, curY, z, 3));
                    }
                    else if (state3) {
                        graph.vertices.add(createVertex(x, curY, z, 0));
                    }
                    else {
                        graph.vertices.add(createVertex(x, curY, z, 3));
                        graph.vertices.add(createVertex(x, curY, z, 0));
                    }
                    break;
                case 9:
                    if (state1 && state2) {
                        graph.vertices.add(createVertex(x, curY, z, 0));
                        graph.vertices.add(createVertex(x, curY, z, 3));
                    }
                    else if (state1) {
                        graph.vertices.add(createVertex(x, curY, z, 2));
                    }
                    else if (state2) {
                        graph.vertices.add(createVertex(x, curY, z, 1));
                    }
                    else {
                        graph.vertices.add(createVertex(x, curY, z, 2));
                        graph.vertices.add(createVertex(x, curY, z, 1));
                    }
                    break;

                // These cases don't generate any vertices
                case 0:
                case 3:
                case 5:
                case 10:
                case 12:
                case 15:
                    break;
            }
        }
        if (TERRAIN_DEBUG_INFO) {
            long endTime = System.nanoTime();
            Pathcrafter.LOGGER.info(
                    String.format("Vertex generation of (%d, %d) took %f seconds",
                            x, z, (endTime - startTime) / 1.0e9));
        }
    }

    public static Vertex createVertex(double x, double y, double z, int type) {
        // four solid-block-creatable vertices
        // 0 1
        // 2 3 -> +z
        // |
        // v +x
        double xCoord = x + 1 + ((type & 2) > 0 ? 1 : -1) * PLAYER_HALF_WIDTH;
        double zCoord = z + 1 + ((type & 1) > 0 ? 1 : -1) * PLAYER_HALF_WIDTH;
        return new Vertex(xCoord, y, zCoord);
    }

}

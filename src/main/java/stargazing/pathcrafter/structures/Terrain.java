package stargazing.pathcrafter.structures;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import stargazing.pathcrafter.Pathcrafter;
import stargazing.pathcrafter.util.PlayerSpeed;
import stargazing.pathcrafter.util.Preprocessing;
import stargazing.pathcrafter.util.World;

import javax.swing.text.Segment;
import java.util.*;

import static stargazing.pathcrafter.Constants.*;
import static stargazing.pathcrafter.util.PlayerSpeed.ticksToFallTo;

public class Terrain {
    public final int minX, minZ, maxX, maxZ;
    // Note that y ranges aren't important, we are processing all the blocks in these chunks anyway

    BlockColumn[][] columns;
    TerrainGraph graph = new TerrainGraph();
    TerrainBounds bounds = new TerrainBounds();

    public Terrain(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        // Establish ranges
        minX = Math.min(startX, endX) - MAX_SEARCH_RANGE;
        minZ = Math.min(startZ, endZ) - MAX_SEARCH_RANGE;
        maxX = Math.max(startX, endX) + MAX_SEARCH_RANGE;
        maxZ = Math.max(startZ, endZ) + MAX_SEARCH_RANGE;

        // Start and end vertices
        graph.vertices.add(new Vertex(startX, startY, startZ, (int)startX, (int)startZ));
        graph.vertices.add(new Vertex(endX, endY, endZ, (int)endX, (int)endZ));

        genColumns();
    }

    public BlockColumn getColumn(int x, int z) {
        if (minX <= x && x <= maxX && minZ <= z && z <= maxZ) {
            return columns[x - minX][z - minZ];
        }
        else {
            return null;
        }
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
     * Computes all relevant vertices for the algorithm.
     */
    public void findVertices() {
        long startTime = System.nanoTime();

        for (int x = minX; x < maxX - 1; x++) {
            for (int z = minZ; z < maxZ - 1; z++) {
                //Pathcrafter.LOGGER.info(String.format("%d, %d", x, z));
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

    /**
     * Simple vertex generation algorithm
     * @param x x coordinate of intersection - 1
     * @param z z coordinate of intersection - 1
     */
    public void findVerticesAt(int x, int z) {
        // Look for vertices close to the vertex of original blocks.
        // i.e. the corners of blocks
        // For now, this simple algo of a shitton of if/else & switch statements will do.

        // To merge 4 sorted lists together... probably no need to use minheap, but is always an option.

        BlockColumn column0 = getColumn(x, z);
        BlockColumn column1 = getColumn(x, z+1);
        BlockColumn column2 = getColumn(x+1, z);
        BlockColumn column3 = getColumn(x+1, z+1);

        if (TERRAIN_VERTEX_DEBUG_INFO) {
            column0.debug_logSurfaces();
            column1.debug_logSurfaces();
            column2.debug_logSurfaces();
            column3.debug_logSurfaces();
        }

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

            if (TERRAIN_VERTEX_DEBUG_INFO) {
                Pathcrafter.LOGGER.info(String.format("Y: %s -> State: %s", curY, matchState));
                Pathcrafter.LOGGER.info(String.format("Match status: %b, %b, %b, %b", match0, match1, match2, match3));
                Pathcrafter.LOGGER.info(String.format("Valid status: %b, %b, %b, %b", valid0, valid1, valid2, valid3));
            }

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
    }

    /**
     * Creates a vertex at the given coordinates with an offset
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param type Only the last 2 bits are read. 2nd last bit denotes x-offset, last bit denotes z-offset
     * @return A new vertex object
     */
    @Contract("_, _, _, _ -> new")
    public static @NotNull Vertex createVertex(double x, double y, double z, int type) {
        // Useable even with non-full block vertices, because the player-width offset is important
        // Although the columnX and columnZ args may change if there's hitboxes that extend past the block
        // 0 1
        // 2 3 -> +z
        // |
        // v +x
        double xCoord = x + 1 + ((type & 2) > 0 ? 1 : -1) * PLAYER_HALF_WIDTH;
        double zCoord = z + 1 + ((type & 1) > 0 ? 1 : -1) * PLAYER_HALF_WIDTH;
        return new Vertex(xCoord, y, zCoord, (int)x, (int)z);
    }

    public void findEdge(int s, int e) {
        findEdge(graph.getVertex(s), graph.getVertex(e));
    }

    public static class ColumnEvent implements Comparable<ColumnEvent> {
        double time;
        ArrayList<BlockColumn> columnAdd, columnRemove;
        ColumnEvent(double t) {
            time = t;
            columnAdd = new ArrayList<>();
            columnRemove = new ArrayList<>();
        }

        void addColumn(BlockColumn bc) {
            columnAdd.add(bc);
        }

        void removeColumn(BlockColumn bc) {
            columnRemove.add(bc);
        }

        void debug_print() {
            Pathcrafter.LOGGER.info(String.format("Time %f", this.time));
            for (BlockColumn column : this.columnAdd) {
                Pathcrafter.LOGGER.info(String.format("Add column %s", column));
            }
            for (BlockColumn column : this.columnRemove) {
                Pathcrafter.LOGGER.info(String.format("Remove column %s", column));
            }
        }

        @Override
        public int compareTo(@NotNull ColumnEvent o) {
            return Double.compare(this.time, o.time);
        }

        public boolean equals(@NotNull ColumnEvent o) {
            return this.time == o.time;
        }
    }

    public void findAllEdgesFrom(int i) {
        long startTime = System.nanoTime();
        Vertex s = graph.getVertex(i);

        // Debug counters
        int success_hasPath = 0;
        int success_noPath = 0;
        int exception = 0;
        int unloaded = 0;

        for (int j=0; j<graph.vertices.size(); j++) {
            if (j == i) continue;
            double edgeResult = -1;
            try {
                edgeResult = findEdge(s, graph.getVertex(j));
            }
            catch (java.lang.NullPointerException e) {
                exception++;
                e.printStackTrace();
                Pathcrafter.LOGGER.info(String.format("Error finding path from %d to %d\n", i, j));
                continue;
            }
            // counters
            if (edgeResult == -1) success_noPath++;
            if (edgeResult == -2) unloaded++;
            // ignore unreachable
            if (edgeResult < 0) continue;

            // Debug
            if (graph.getVertex(j).y > 15) {
                // Strange occurence. has to debug
                Pathcrafter.LOGGER.info(String.format("Edge from %d (%s) to %d (%s)-> %f",
                        i, graph.getVertex(i), j, graph.getVertex(j), edgeResult));
            }
            if (TERRAIN_EDGE_LIST_EDGES)
                Pathcrafter.LOGGER.info(String.format("Edge from %d (%s) to %d (%s)-> %f",
                        i, graph.getVertex(i), j, graph.getVertex(j), edgeResult));

            graph.addEdge(i, j, edgeResult);
            success_hasPath++;
        }
        if (TERRAIN_EDGE_GENERATOR_DEBUG_INFO) {
            long endTime = System.nanoTime();
            Pathcrafter.LOGGER.info(
                    String.format("Edge generation from %s (%d) took %f seconds",
                            graph.getVertex(i), i, (endTime - startTime) / 1.0e9));
            Pathcrafter.LOGGER.info(
                    String.format("%d success and has path, %d success with no path", success_hasPath, success_noPath)
            );
            Pathcrafter.LOGGER.info(
                    String.format("%d cross unloaded chunks, %d resulted in an exception", unloaded, exception)
            );
        }
    }

    /**
     * Find the edge weight between two vertices
     * @param start Starting vertex
     * @param end Destination vertex
     * @return The edge travel time if path is available, otherwise -1.
     */
    public double findEdge(Vertex start, Vertex end) {
        // Convert this into 2d, and then... use dp?
        if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO)
            Pathcrafter.LOGGER.info(String.format("Finding path from %s to %s", start, end));

        // If the vertices are the same, return.
        if (start.equals(end)) {
            if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO)
                Pathcrafter.LOGGER.info(String.format("%s and %s are the same vertex!", start, end));
            return -1;
        }

        // x and z directions
        int x_dir = end.x > start.x ? 1 : -1;
        int z_dir = end.z > start.z ? 1 : -1;

        // Distance
        double dist = Math.pow((end.x - start.x) * (end.x - start.x) + (end.z - start.z) * (end.z - start.z), 0.5f);
        Pathcrafter.LOGGER.info(String.format("XZ-distance: %f", dist));

        // Find all relevant columns
        Set<BlockColumn> relevantColumns = new HashSet<BlockColumn>();
        Set<BlockColumn> centralColumns = getColumns(
                start.x - PLAYER_HALF_WIDTH_PADDED * x_dir,
                start.z - PLAYER_HALF_WIDTH_PADDED * z_dir,
                end.x   + PLAYER_HALF_WIDTH_PADDED * x_dir,
                end.z   + PLAYER_HALF_WIDTH_PADDED * z_dir);
        Set<BlockColumn> upperColumns = getColumns(
                start.x - PLAYER_HALF_WIDTH_PADDED * x_dir,
                start.z + PLAYER_HALF_WIDTH_PADDED * z_dir,
                end.x   - PLAYER_HALF_WIDTH_PADDED * x_dir,
                end.z   + PLAYER_HALF_WIDTH_PADDED * z_dir);
        Set<BlockColumn> lowerColumns = getColumns(
                start.x + PLAYER_HALF_WIDTH_PADDED * x_dir,
                start.z - PLAYER_HALF_WIDTH_PADDED * z_dir,
                end.x   + PLAYER_HALF_WIDTH_PADDED * x_dir,
                end.z   - PLAYER_HALF_WIDTH_PADDED * z_dir);
        if (centralColumns == null || upperColumns == null || lowerColumns == null) {
            // Congrats, you hit the edge of the loaded area!
            // The weights can't be calculated, so... have a -1!
            return -2;
        }
        relevantColumns.addAll(centralColumns);
        relevantColumns.addAll(upperColumns);
        relevantColumns.addAll(lowerColumns);

        // Generate whatever shit the dp will run on
        TreeSet<ColumnEvent> columnEvents = new TreeSet<>();
        for (BlockColumn c : relevantColumns) {
            double[] times = getColumnTimes(start.x, start.z, end.x, end.z, c);

            // Ignore columns that have 0 impact
            if (times[0] == times[1]) {
                continue;
            }

            if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO)
                Pathcrafter.LOGGER.info(String.format("Column %s times: %f, %f", c, times[0], times[1]));

            ColumnEvent addEvent = new ColumnEvent(times[0]);
            ColumnEvent addDup = columnEvents.floor(addEvent);
            //if (addDup != null) addDup.debug_print();
            if (addDup != null && addDup.time == times[0]) {
                //Pathcrafter.LOGGER.info("Addition duplicate found.");
                addDup.addColumn(c);
            }
            else {
                //Pathcrafter.LOGGER.info("No duplicate, adding add event.");
                addEvent.addColumn(c);
                columnEvents.add(addEvent);
            }

            ColumnEvent removeEvent = new ColumnEvent(times[1]);
            ColumnEvent removeDup = columnEvents.floor(removeEvent);
            //if (removeDup != null) removeDup.debug_print();
            if (removeDup != null && removeDup.time == times[1]) {
                //Pathcrafter.LOGGER.info("Removal duplicate found.");
                removeDup.removeColumn(c);
            }
            else {
                //Pathcrafter.LOGGER.info("No duplicate, adding removal event.");
                removeEvent.removeColumn(c);
                columnEvents.add(removeEvent);
            }

            /*
            Pathcrafter.LOGGER.info("Current events:");
            for (ColumnEvent ce : columnEvents) {
                ce.debug_print();
            }
            Pathcrafter.LOGGER.info(new String(new char[50]).replace('\0', '-'));
            */
        }

        Set<BlockColumn> currentColumns = new HashSet<>();
        ArrayList<SegmentList> segments = new ArrayList<>();
        int startIndex = -1;

        for (ColumnEvent ce : columnEvents) {

            //Pathcrafter.LOGGER.info(String.format("Time %f", ce.time));
            for (BlockColumn column : ce.columnAdd) {
                currentColumns.add(column);
                //Pathcrafter.LOGGER.info(String.format("Add column %s", column));
            }
            for (BlockColumn column : ce.columnRemove) {
                currentColumns.remove(column);
                //Pathcrafter.LOGGER.info(String.format("Remove column %s", column));
            }

            if (ce.time < 0) startIndex++;

            SegmentList s = new SegmentList(ce.time);
            for (BlockColumn c : currentColumns) {
                s.addColumn(c);
            }
            segments.add(s);

            if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) s.debug_print();

            // Anything else doesn't impact it anymore.
            if (ce.time >= 1) break;
        }

        // Now do the weird ass dp
        SegmentList.Segment startingSegment = segments.get(startIndex).segments.floor(new SegmentList.Segment(start.y, start.y));
        if (startingSegment == null) {
            //Pathcrafter.LOGGER.warn("Invalid starting segment!");
            return -1;
        }
        startingSegment.val = 0;

        // The end segment
        SegmentList.Segment endSegment =
                segments.get(segments.size() - 1).segments.floor(new SegmentList.Segment(start.y, start.y));
        double endOffset = 0d;
        if (endSegment == null) {
            return -1;
        }
        if (endSegment.end != end.y) {
            // The end segment can be ambiguous if it's exactly at the endpoint.
            // So we attempt to let the segment before 1.0 be the end
            if (segments.get(segments.size() - 1).time != 1d) {
                if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO)
                    Pathcrafter.LOGGER.info("Invalid end segment! No path found.");
                return -1;
            }
            if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO)
                Pathcrafter.LOGGER.info("Invalid end segment! Trying previous...");

            endSegment = segments.get(segments.size() - 2).segments.floor(new SegmentList.Segment(start.y, start.y));
            endOffset = (segments.get(segments.size() - 1).time - segments.get(segments.size() - 2).time)
                    * dist / SPRINT_SPEED;

            if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO)
                Pathcrafter.LOGGER.info(String.format("New segment: %s (offset: %f)", endSegment.toString(), endOffset));

            if (endSegment == null || endSegment.end != end.y) {
                if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) Pathcrafter.LOGGER.info("Invalid end segment!");
                return -1;
            }
        }

        Pathcrafter.LOGGER.info("test");

        if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) {
            Pathcrafter.LOGGER.info("----------------------------------------------------------------------");
            Pathcrafter.LOGGER.info("-------------------------------DP START-------------------------------");
            Pathcrafter.LOGGER.info("----------------------------------------------------------------------");
            Pathcrafter.LOGGER.info(String.format("Starting index: %d", startIndex));
        }

        for (int i=startIndex; i < segments.size() - 1; i++) {
            // This DP transition is not accurate - not taking into account jumping early to avoid headhitting
            // And just directly saying "if there's a hole u can jump down"
            // but will do for now
            // Also I should rename these variables soon, it's confusing af right now

            SegmentList curSegment = segments.get(i);
            // We start at 0, not a negative number.
            double curTime = Math.max(0, curSegment.time);
            // i+1 here since you can choose to jump off the end of a block
            double segmentCurDist = segments.get(i+1).time * dist;

            if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) {
                Pathcrafter.LOGGER.info("----------------------------------------------------------------------");
                Pathcrafter.LOGGER.info(String.format("Looking at segment list %d at time %f", i, curTime));
            }


            for (SegmentList.Segment s : curSegment.segments) {
                // Ignore unreachable segments
                if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) Pathcrafter.LOGGER.info(String.format("Current segment: %s", s));
                if (s.val == -1) continue;

                // Try sprinting forwards
                SegmentList nextSegment = segments.get(i+1);
                // Current time + time it takes to sprint to the edge of the block
                SegmentList.Segment sprintingToSegment = nextSegment.segments.floor(new SegmentList.Segment(s.end, s.end));
                double curCost = s.val + (nextSegment.time - curTime) * dist / SPRINT_SPEED;
                // If there is somewhere to sprint to
                if (sprintingToSegment != null && sprintingToSegment.end <= s.end) {
                    // time to edge + ticks to fall, if any
                    double sprintCost = curCost + ticksToFallTo(sprintingToSegment.end - s.end);
                    sprintingToSegment.updateVal(sprintCost);
                    if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO)
                        Pathcrafter.LOGGER.info("Sprinting to segment " + sprintingToSegment + " requires tick " + sprintCost);
                }

                // Try jumping forwards
                int curTick = 0;
                double curDist = 0, curHeight = 0, baseHeight = s.end;
                boolean blocked = false;
                double minHeight = baseHeight;

                for (int j=i; j<segments.size(); j++) {
                    if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) Pathcrafter.LOGGER.info("Looking at segment " + j);
                    SegmentList jumpCurSegment = segments.get(j);
                    if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) jumpCurSegment.debug_print();
                    // How long, in meters, this segment lasts
                    double maxDeltaDist = jumpCurSegment.time * dist - segmentCurDist;
                    if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) Pathcrafter.LOGGER.info("Max delta dist: " + maxDeltaDist);

                    // Mark all available jumps
                    // 'Tis the ascent of a jump
                    if (curTick < 5) curHeight = PlayerSpeed.flatJumpDistances.get(5).minY;

                    minHeight = Math.min(minHeight, jumpCurSegment.floor(minHeight));
                    // If ur collided with ground, then it's over
                    if (minHeight > curHeight + baseHeight) break;

                    // Minimum reachable height, through ground holes.
                    // Again, not technically correct, just an approximation
                    if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) Pathcrafter.LOGGER.info("This segment's minimum height: " + minHeight);
                    if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) Pathcrafter.LOGGER.info("This segment's maximum height: " + curHeight + baseHeight);

                    // Reminder to correct the cost (based on landing time) later
                    jumpCurSegment.mark(minHeight, curHeight + baseHeight, curCost, baseHeight);

                    while (curTick <= MAX_SEARCH_JUMP_TICK) {
                        // Just check if a jump is doable right now
                        curDist = PlayerSpeed.flatJumpDistances.get(curTick).deltaX;
                        curHeight = PlayerSpeed.flatJumpDistances.get(curTick).minY;
                        // First the player Y is changed, then XZ, when collision checking
                        if (jumpCurSegment.contains(curHeight + baseHeight)) {
                            if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) Pathcrafter.LOGGER.info("Blocked!");
                            blocked = true;
                            break;
                        }
                        // Go to next segment if the distance is too far
                        if (curDist > maxDeltaDist) break;

                        curTick++;
                        if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) Pathcrafter.LOGGER.info("Advancing to tick " + curTick + "!");
                    }

                    if (curTick > MAX_SEARCH_JUMP_TICK) break;
                    if (blocked) break;
                    if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) Pathcrafter.LOGGER.info("Advancing to next segment!");
                }
            }
            if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) {
                Pathcrafter.LOGGER.info("----------------------------------------------------------------------");
                Pathcrafter.LOGGER.info("Current status:");
                for (int j=startIndex; j < segments.size(); j++) {
                    segments.get(j).debug_print();
                }
            }
        }

        if (TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO) {
            Pathcrafter.LOGGER.info("----------------------------------------------------------------------");
            Pathcrafter.LOGGER.info(String.format("Final result: %f", endSegment.val + endOffset));
        }
        // Note to self: Still need to add a final bit of sprinting distance.
        return endSegment.val + endOffset;
    }

    /**
     * Gets the blockcolumns that intersect a segment from (startX, startZ) to (endX, endZ)
     * @param startX Start x coordinate
     * @param startZ Start z coordinate
     * @param endX End x coordinate
     * @param endZ End z coordinate
     * @return The columns intersected by the segment
     */
    public Set<BlockColumn> getColumns(double startX, double startZ, double endX, double endZ) {
        // Is there a good line algorithm for fractional coordinates?
        Set<BlockColumn> columns = new HashSet<>();
        int xDir = endX > startX ? 1 : -1;
        int zDir = endZ > startZ ? 1 : -1;

        int curX = (int)Math.floor(startX);
        int curZ = (int)Math.floor(startZ);
        int endIntX = (int)Math.floor(endX);
        int endIntZ = (int)Math.floor(endZ);

        double nextXUpdate = (curX + ((xDir + 1) >> 1) - startX) / (endX - startX);
        double nextZUpdate = (curZ + ((zDir + 1) >> 1) - startZ) / (endZ - startZ);
        while (curX != endIntX || curZ != endIntZ) {
            BlockColumn curColumn = getColumn(curX, curZ);
            if (TERRAIN_COLUMNS_DEBUG_INFO) Pathcrafter.LOGGER.info(String.format("Adding column %s!", curColumn));
            // Out of bounds!
            if (curColumn == null) {
                return null;
            }
            columns.add(curColumn);
            if (nextXUpdate < nextZUpdate) {
                curX += xDir;
                nextXUpdate += xDir / (endX - startX);
            }
            else {
                curZ += zDir;
                nextZUpdate += zDir / (endZ - startZ);
            }
        }
        columns.add(getColumn(curX, curZ));

        return columns;
    }

    public double[] getColumnTimes(double startX, double startZ, double endX, double endZ, @NotNull BlockColumn column) {
        // W/ solid blocks assumption, we can assume hitbox of -0.3 ~ 1.3.
        double x_t1 = (column.blockX     - PLAYER_HALF_WIDTH - startX) / (endX - startX);
        double x_t2 = (column.blockX + 1 + PLAYER_HALF_WIDTH - startX) / (endX - startX);
        if (x_t2 < x_t1) {
            double temp = x_t1;
            x_t1 = x_t2;
            x_t2 = temp;
        }
        double z_t1 = (column.blockZ     - PLAYER_HALF_WIDTH - startZ) / (endZ - startZ);
        double z_t2 = (column.blockZ + 1 + PLAYER_HALF_WIDTH - startZ) / (endZ - startZ);
        if (z_t2 < z_t1) {
            double temp = z_t1;
            z_t1 = z_t2;
            z_t2 = temp;
        }
        return new double[]{Math.max(z_t1, x_t1), Math.min(z_t2, x_t2)};
    }

    public TerrainGraph getGraph() {return this.graph;}


    public double getResult() {

        int vertexCount = graph.vertices.size();
        boolean[] genEdges = new boolean[vertexCount];
        double[] dist = new double[vertexCount];
        double[] bestDist = new double[vertexCount];
        int[] last = new int[vertexCount];
        Arrays.fill(dist, Double.MAX_VALUE);
        Arrays.fill(bestDist, Double.MAX_VALUE);
        PriorityQueue<Integer> q = new PriorityQueue<>(Comparator.comparingDouble((Integer o) -> bestDist[o]));
        q.add(0);
        dist[0] = 0;

        while (!q.isEmpty()) {
            int front = q.remove();
            Pathcrafter.LOGGER.info(
                    String.format("Getting paths from %s (%d) | dist: %f | dist + heuristic: %f",
                            graph.getVertex(front), front, dist[front], bestDist[front]));
            if (front == 1) {
                // End!
                int cur = 1;
                while (cur != 0) {
                    cur = last[cur];
                    Pathcrafter.LOGGER.info("From: " + graph.getVertex(cur) + "(" + cur + ")");
                }
                return dist[1];
            }

            if (!genEdges[front]) {
                findAllEdgesFrom(front);
                genEdges[front] = true;
            }

            for (TerrainGraph.Edge e : graph.edges.get(front)) {
                double result = dist[front] + e.weight;
                //Pathcrafter.LOGGER.info("To: " + e.to + ", Result: " + result);
                if (result < dist[e.to]) {
                    dist[e.to] = result;
                    last[e.to] = front;
                    bestDist[e.to] = result + graph.heuristic(e.to, 1);
                    q.add(e.to);
                }
            }
        }

        // No path
        return -1;
    }

}

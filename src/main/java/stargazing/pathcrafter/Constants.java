package stargazing.pathcrafter;

public class Constants {

    // Debug printing toggle
    public static final boolean BLOCK_COLUMN_DEBUG_INFO = false;
    public static final boolean TERRAIN_DEBUG_INFO = true;
    public static final boolean TERRAIN_EDGE_GENERATOR_DEBUG_INFO = true;
    public static final boolean TERRAIN_EDGE_LIST_EDGES = false;
    public static final boolean TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO = false;
    public static final boolean TERRAIN_COLUMNS_DEBUG_INFO = false;
    public static final boolean SEGMENT_LIST_DEBUG_INFO = false;
    public static final boolean SEGMENT_LIST_ALLOW_INFO_CALL = false;

    // Player related constants
    public static final double PLAYER_HEIGHT = 1.8;
    public static final double PLAYER_WIDTH_MARGIN = 0.002;
    public static final double PLAYER_WIDTH = 0.6;
    public static final double PLAYER_HALF_WIDTH = 0.3; // Arguably more useful
    public static final double PLAYER_HALF_WIDTH_PADDED = 0.32;

    // Chunk-related constants
    public static final int CHUNK_SIZE = 16;
    public static final int MIN_HEIGHT = -64;
    public static final int MAX_HEIGHT = 319;
    public static final int TOT_HEIGHT = 384;

    // Player speed constants
    public static final double SPRINT_SPEED = 5.612 / 20.0;
    public static final double WALK_SPEED = 4.317 / 20.0;
    // Handy shortcut for flat ground sprint jumping
    public static final double SPRINT_JUMP_SPEED = 7.127 / 20.0;

    // It takes 12gt to land at the same y-level after jumping.
    public static final int FLAT_JUMP_TICKS = 12;
    public static final int ELEVATE_JUMP_TICKS = 8;
    public static final double FLAT_MAX_MOMENTUM = 0.3176;

    public static final double GRAVITY_MODIFIER = 0.08;
    public static final double VERTICAL_DRAG = 0.98, HORIZONTAL_DRAG = 0.91;
    public static final double HORIZONTAL_DRAG_GROUND_MULTIPLIER = 0.6; // Air --> 1.0
    public static final double HORIZONTAL_ACCELERATION_AIR = 0.02, HORIZONTAL_ACCELERATION_GROUND = 0.1;
    public static final double SPRINT_FACTOR = 1.3;
    public static final double STRAIGHT_FACTOR = 0.98; // 45-Strafe --> 1.0
    public static final double SPRINT_JUMP_MODIFIER = 0.2;

    // Options, will implement something later
    public static final int MAX_SEARCH_RANGE = 20;
    public static final double SEARCH_RANGE_FACTOR = 0.7;
    public static final int MAX_SEARCH_JUMP_TICK = 17;

}

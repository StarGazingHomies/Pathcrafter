package stargazing.pathcrafter.config;

public class DebugToggles {

    public static class BooleanSetting {
        public String name;
        public boolean b;
        public BooleanSetting(boolean b, String name) {
            this.b = b;
            this.name = name;
        }
        public boolean enabled() {
            return b;
        }
        public void flip() {
            b = !b;
        }
        public void set(boolean b) {
            this.b = b;
        }
    }

    // Debug printing toggles
    public static BooleanSetting BLOCK_COLUMN_DEBUG_INFO =
            new BooleanSetting(false, "block column debug info");
    public static BooleanSetting TERRAIN_DEBUG_INFO =
            new BooleanSetting(true, "terrain debug info");
    public static BooleanSetting TERRAIN_VERTEX_DEBUG_INFO =
            new BooleanSetting(false, "terrain vertex debug info");
    public static BooleanSetting TERRAIN_EDGE_GENERATOR_DEBUG_INFO =
            new BooleanSetting(false, "terrain edge generator debug info");
    public static BooleanSetting TERRAIN_EDGE_LIST_EDGES =
            new BooleanSetting(false, "terrain list edges");
    public static BooleanSetting TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO =
            new BooleanSetting(false, "terrain individual edge debug info"); //
    public static BooleanSetting TERRAIN_COLUMNS_DEBUG_INFO =
            new BooleanSetting(false, "terrain columns debug info");
    public static BooleanSetting SEGMENT_LIST_DEBUG_INFO =
            new BooleanSetting(false, "segment list debug info");
    public static BooleanSetting SEGMENT_LIST_ALLOW_INFO_CALL =
            new BooleanSetting(false, "segment list info calls"); //
}

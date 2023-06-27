package stargazing.pathcrafter.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.chunk.Chunk;

public class World {
    public static MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }
    public static ClientWorld getWorld() {
        return getClient().world;
    }

    public static Chunk getChunk(int chunkX, int chunkZ) {
        return getWorld().getChunk(chunkX, chunkZ);
    }
}

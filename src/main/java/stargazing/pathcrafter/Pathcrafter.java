package stargazing.pathcrafter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stargazing.pathcrafter.util.Preprocessing;
import stargazing.pathcrafter.util.World;

import static stargazing.pathcrafter.util.Preprocessing.processChunk;

public class Pathcrafter implements ClientModInitializer {

    private static KeyBinding keyBinding;
    public static final Logger LOGGER = LoggerFactory.getLogger("Pathcrafter");

    private boolean pressed = false;
    @Override
    public void onInitializeClient() {
        LOGGER.info("Pathcrafter loaded!");
        //HudRenderCallback.EVENT.register(new OverlayRenderer());

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pathcrafter.debug",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.pathcrafter"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding.wasPressed()) {
                if (pressed) {
                    return;
                }
                pressed = true;
                assert client.player != null;
                Chunk c = World.getChunk(0,0);
                Preprocessing.processChunk(c, client);
                client.player.sendMessage(Text.literal("Debug Button Pressed"), false);
            }
            else {
                pressed = false;
            }
        });
    }
}

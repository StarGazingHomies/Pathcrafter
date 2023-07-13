package stargazing.pathcrafter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stargazing.pathcrafter.command.CommandRegistrar;
import stargazing.pathcrafter.overlay.OverlayRenderer;
import stargazing.pathcrafter.structures.Terrain;

public class Pathcrafter implements ClientModInitializer {

    private static KeyBinding debugActionKeyBinding, debugRenderKeyBinding;
    public static final Logger LOGGER = LoggerFactory.getLogger("Pathcrafter");

    public static Terrain terrain;

    private boolean debugActionPressed = false, debugRenderPressed = false;
    public static boolean debugRenderToggle = true;
    @Override
    public void onInitializeClient() {
        LOGGER.info("Pathcrafter loaded!");
        WorldRenderEvents.END.register(new OverlayRenderer());

        CommandRegistrar.register();

        debugActionKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pathcrafter.debugAction",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.pathcrafter"
        ));

        debugRenderKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pathcrafter.debugRender",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.pathcrafter"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (debugActionKeyBinding.wasPressed()) {
                if (debugActionPressed) {
                    return;
                }
                debugActionPressed = true;
                assert client.player != null;
                client.player.sendMessage(Text.literal("Debug Button Pressed"), false);

                //terrain = new Terrain(1, 0,1, 15, 0,15);
                //terrain.findVerticesAt(4, 4);
                terrain = new Terrain(1, 0,1, 15, 0,15);
                terrain.findVertices();
                //terrain.findAllEdgesFrom(0);
                //terrain.getResult();
            }
            else {
                debugActionPressed = false;
            }

            if (debugRenderKeyBinding.wasPressed()) {
                if (debugRenderPressed) {
                    return;
                }
                debugRenderPressed = true;
                debugRenderToggle = !debugRenderToggle;
            }
            else {
                debugRenderPressed = false;
            }
        });
    }
}

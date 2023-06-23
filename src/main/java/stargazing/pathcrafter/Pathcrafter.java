package stargazing.pathcrafter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stargazing.pathcrafter.overlay.OverlayRenderer;

public class Pathcrafter implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Pathcrafter");
    @Override
    public void onInitializeClient() {
        LOGGER.info("Hello World!");
        //HudRenderCallback.EVENT.register(new OverlayRenderer());
    }
}

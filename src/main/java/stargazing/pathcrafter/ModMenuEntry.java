package stargazing.pathcrafter;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import stargazing.pathcrafter.gui.DebugScreen;

public class ModMenuEntry implements ModMenuApi{

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return DebugScreen::new;
    }
}

package stargazing.pathcrafter.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class DebugScreen extends Screen {
    private final Screen parent;

    public DebugScreen(Screen parent) {
        super(Text.literal("Debug screen"));
        this.parent = parent;
    }

    public ButtonWidget button1;

    @Override
    protected void init() {
        button1 = ButtonWidget.builder(Text.literal("Button 1"), button -> System.out.println("You clicked button1!"))
            .dimensions(width / 2 - 205, 20, 200, 20)
            .tooltip(Tooltip.of(Text.literal("Tooltip of button1")))
            .build();

        addDrawableChild(button1);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}

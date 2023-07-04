package stargazing.pathcrafter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandException;
import net.minecraft.text.Text;
import stargazing.pathcrafter.Pathcrafter;
import stargazing.pathcrafter.config.DebugToggles;

public class Debug {
    public static int boop(CommandContext<FabricClientCommandSource> context) {
        // The command to make sure everything works as is
        // also for the mod to make cute noises.
        int rand = (int)(Math.random() * 3);
        if (rand == 0)
            context.getSource().sendFeedback(Text.literal("Squeak!"));
        else if (rand == 1)
            context.getSource().sendFeedback(Text.literal("Meep!"));
        else
            context.getSource().sendFeedback(Text.literal("Eep!"));
        return 1;
    }

    public static int findEdges(CommandContext<FabricClientCommandSource> context){
        int v1 = IntegerArgumentType.getInteger(context, "vertex");
        if (Pathcrafter.terrain == null) {
            throw new CommandException(Text.literal("Terrain graph not generated!"));
        }
        if (!(0 <= v1 && v1 < Pathcrafter.terrain.getGraph().vertices.size())) {
            throw new CommandException(Text.literal("Vertex index out of range!"));
        }
        Pathcrafter.terrain.findAllEdgesFrom(v1);
        return 1;
    }

    public static int findEdge(CommandContext<FabricClientCommandSource> context) {
        int v1 = IntegerArgumentType.getInteger(context, "vertex 1");
        int v2 = IntegerArgumentType.getInteger(context, "vertex 2");
        if (Pathcrafter.terrain == null) {
            throw new CommandException(Text.literal("Terrain graph not generated!"));
        }
        if (!(0 <= v1 && v1 < Pathcrafter.terrain.getGraph().vertices.size() &&
                0 <= v2 && v2 < Pathcrafter.terrain.getGraph().vertices.size())) {
            throw new CommandException(Text.literal("Vertex index out of range!"));
        }
        double result = Pathcrafter.terrain.findEdge(v1, v2);
        context.getSource().sendFeedback(Text.literal(String.format("Operation completed with result %f", result)));
        return 1;
    }

    public static int listSettings(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("Settings are not implemented yet!"));
        return 1;
    }

    public static class DebugToggle implements Command<FabricClientCommandSource> {
        DebugToggles.BooleanSetting s;
        public DebugToggle(DebugToggles.BooleanSetting s) {
            this.s = s;
        }

        @Override
        public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
            s.flip();
            if (s.enabled()) {
                context.getSource().sendFeedback(Text.literal("Setting "+s.name+" set to true."));
            }
            else {
                context.getSource().sendFeedback(Text.literal("Setting "+s.name+" set to false."));
            }
            return 1;
        }
    }

}

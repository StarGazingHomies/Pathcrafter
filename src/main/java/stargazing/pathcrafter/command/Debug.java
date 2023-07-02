package stargazing.pathcrafter.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import stargazing.pathcrafter.Pathcrafter;

public class Debug {
    public static int boop(CommandContext<FabricClientCommandSource> context) {
        // The command to make sure everything works as is
        context.getSource().sendError(Text.literal("Squeak!"));
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
        Pathcrafter.terrain.findEdge(v1, v2);
        return 1;
    }

}

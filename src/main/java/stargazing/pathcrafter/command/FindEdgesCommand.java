package stargazing.pathcrafter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandException;
import net.minecraft.text.Text;
import stargazing.pathcrafter.Pathcrafter;
import stargazing.pathcrafter.structures.TerrainGraph;

public class FindEdgesCommand {
    public static int findEdges(CommandContext<FabricClientCommandSource> context){
        int v1 = IntegerArgumentType.getInteger(context, "vertex");
        if (Pathcrafter.terrain == null) {
            throw new CommandException(Text.literal("Terrain graph not generated!"));
        }
        if (!(0 <= v1 && v1 < Pathcrafter.terrain.getGraph().vertices.size())) {
            throw new CommandException(Text.literal("Vertex index out of range!"));
        }
        Pathcrafter.terrain.findAllEdgesFrom(v1);
        context.getSource().sendFeedback(Text.literal("Operation completed."));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> pathcrafterNode) {
        LiteralCommandNode<FabricClientCommandSource> findEdgesNode = ClientCommandManager
                .literal("findEdges")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> findEdgesVertex = ClientCommandManager
                .argument("vertex", IntegerArgumentType.integer())
                .build();
        findEdgesNode.addChild(findEdgesVertex);
        pathcrafterNode.addChild(findEdgesNode);
    }
}

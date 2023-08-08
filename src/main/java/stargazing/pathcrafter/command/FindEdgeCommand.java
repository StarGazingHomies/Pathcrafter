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

public class FindEdgeCommand {
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
        TerrainGraph.EdgeInfo result = Pathcrafter.terrain.findEdge(v1, v2);
        context.getSource().sendFeedback(Text.literal(String.format("Operation completed with result %f", result.weight)));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> pathcrafterNode) {
        LiteralCommandNode<FabricClientCommandSource> findEdgeNode = ClientCommandManager
                .literal("findEdge")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> findEdgeVertex1 = ClientCommandManager
                .argument("vertex 1", IntegerArgumentType.integer())
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> findEdgeVertex2 = ClientCommandManager
                .argument("vertex 2", IntegerArgumentType.integer())
                .executes(FindEdgeCommand::findEdge)
                .build();

        findEdgeNode.addChild(findEdgeVertex1);
        findEdgeVertex1.addChild(findEdgeVertex2);
        pathcrafterNode.addChild(findEdgeNode);
    }
}

package stargazing.pathcrafter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandException;
import net.minecraft.text.Text;
import stargazing.pathcrafter.Pathcrafter;
import stargazing.pathcrafter.structures.Terrain;

public class GenVerticesCommand {
    public static int genVertices(CommandContext<FabricClientCommandSource> context) {
        if (Pathcrafter.terrain == null) {
            throw new CommandException(Text.literal("Terrain graph not generated!"));
        }
        if (Pathcrafter.terrain.getGraph().vertices.size() != 0) {
            context.getSource().sendFeedback(Text.literal(String.format(
                    "Clearing %d existing vertices", Pathcrafter.terrain.getGraph().vertices.size()
            )));
            Pathcrafter.terrain.getGraph().vertices.clear();
        }
        Pathcrafter.terrain.findVertices();
        context.getSource().sendFeedback(Text.literal(
                String.format("%d vertices generated.", Pathcrafter.terrain.getGraph().vertices.size())));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> pathcrafterNode) {
        LiteralCommandNode<FabricClientCommandSource> genVerticesNode = ClientCommandManager
                .literal("genVertices")
                .executes(GenVerticesCommand::genVertices)
                .build();

        pathcrafterNode.addChild(genVerticesNode);
    }
}

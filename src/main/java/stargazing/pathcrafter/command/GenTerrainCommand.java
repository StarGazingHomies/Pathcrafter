package stargazing.pathcrafter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import stargazing.pathcrafter.Pathcrafter;
import stargazing.pathcrafter.structures.Terrain;

public class GenTerrainCommand {
    public static int genTerrain(CommandContext<FabricClientCommandSource> context) {
        double srcX = DoubleArgumentType.getDouble(context, "sourceX");
        double srcY = DoubleArgumentType.getDouble(context, "sourceY");
        double srcZ = DoubleArgumentType.getDouble(context, "sourceZ");
        double dstX = DoubleArgumentType.getDouble(context, "destinationX");
        double dstY = DoubleArgumentType.getDouble(context, "destinationY");
        double dstZ = DoubleArgumentType.getDouble(context, "destinationZ");
        Pathcrafter.terrain = new Terrain(srcX, srcY, srcZ, dstX, dstY, dstZ);
        return Command.SINGLE_SUCCESS;
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> pathcrafterNode) {
        LiteralCommandNode<FabricClientCommandSource> genTerrainNode = ClientCommandManager
                .literal("genTerrain")
                .build();

        // Ugh, no blockPos for client side commands... woohoo
        ArgumentCommandNode<FabricClientCommandSource, Double> sourceXNode = ClientCommandManager
                .argument("sourceX", DoubleArgumentType.doubleArg())
                .build();
        ArgumentCommandNode<FabricClientCommandSource, Double> sourceYNode = ClientCommandManager
                .argument("sourceY", DoubleArgumentType.doubleArg())
                .build();
        ArgumentCommandNode<FabricClientCommandSource, Double> sourceZNode = ClientCommandManager
                .argument("sourceZ", DoubleArgumentType.doubleArg())
                .build();
        ArgumentCommandNode<FabricClientCommandSource, Double> destinationXNode = ClientCommandManager
                .argument("destinationX", DoubleArgumentType.doubleArg())
                .build();
        ArgumentCommandNode<FabricClientCommandSource, Double> destinationYNode = ClientCommandManager
                .argument("destinationY", DoubleArgumentType.doubleArg())
                .build();
        ArgumentCommandNode<FabricClientCommandSource, Double> destinationZNode = ClientCommandManager
                .argument("destinationZ", DoubleArgumentType.doubleArg())
                .executes(GenTerrainCommand::genTerrain)
                .build();

        genTerrainNode.addChild(sourceXNode);
        sourceXNode.addChild(sourceYNode);
        sourceYNode.addChild(sourceZNode);
        sourceZNode.addChild(destinationXNode);
        destinationXNode.addChild(destinationYNode);
        destinationYNode.addChild(destinationZNode);

        pathcrafterNode.addChild(genTerrainNode);
    }
}

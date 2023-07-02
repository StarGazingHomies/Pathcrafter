package stargazing.pathcrafter.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class Command {

    public static void register() {
        com.mojang.brigadier.Command<FabricClientCommandSource> command = context -> {
            context.getSource().sendError(Text.literal("Called /foo with no arguments"));
            return 1;
        };
        ClientCommandRegistrationCallback c =
                (dispatcher, registryAccess) -> {
                    LiteralCommandNode<FabricClientCommandSource> pathNode = ClientCommandManager
                            .literal("pathcrafter")
                            .build();


                    LiteralCommandNode<FabricClientCommandSource> boopNode = ClientCommandManager
                            .literal("boop")
                            .executes(Debug::boop)
                            .build();

                    // Find edge
                    LiteralCommandNode<FabricClientCommandSource> findEdgeNode = ClientCommandManager
                            .literal("findEdge")
                            .build();

                    ArgumentCommandNode<FabricClientCommandSource, Integer> findEdgeVertex1 = ClientCommandManager
                            .argument("vertex 1", IntegerArgumentType.integer())
                            .build();

                    ArgumentCommandNode<FabricClientCommandSource, Integer> findEdgeVertex2 = ClientCommandManager
                            .argument("vertex 2", IntegerArgumentType.integer())
                            .executes(Debug::findEdge)
                            .build();

                    findEdgeNode.addChild(findEdgeVertex1);
                    findEdgeVertex1.addChild(findEdgeVertex2);

                    dispatcher.getRoot().addChild(pathNode);
                    pathNode.addChild(boopNode);
                    pathNode.addChild(findEdgeNode);
                };

        ClientCommandRegistrationCallback.EVENT.register(c);

    }
}

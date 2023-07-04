package stargazing.pathcrafter.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import stargazing.pathcrafter.config.DebugToggles;

public class CommandRegistrar {

    public static void register() {
        ClientCommandRegistrationCallback c =
                (dispatcher, registryAccess) -> {
                    // Main node
                    // /pathcrafter
                    LiteralCommandNode<FabricClientCommandSource> pathcrafterNode = ClientCommandManager
                            .literal("pathcrafter")
                            .build();
                    dispatcher.getRoot().addChild(pathcrafterNode);

                    // Boop (Test command)
                    // /pathcrafter boop
                    LiteralCommandNode<FabricClientCommandSource> boopNode = ClientCommandManager
                            .literal("boop")
                            .executes(Debug::boop)
                            .build();
                    pathcrafterNode.addChild(boopNode);

                    // Find edge
                    // /pathcrafter findEdge <v1> <v2>
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
                    pathcrafterNode.addChild(findEdgeNode);

                    // Find edges
                    // /pathcrafter findEdges <v>
                    LiteralCommandNode<FabricClientCommandSource> findEdgesNode = ClientCommandManager
                            .literal("findEdges")
                            .build();

                    ArgumentCommandNode<FabricClientCommandSource, Integer> findEdgesVertex = ClientCommandManager
                            .argument("vertex", IntegerArgumentType.integer())
                            .build();
                    findEdgesNode.addChild(findEdgesVertex);
                    pathcrafterNode.addChild(findEdgesNode);

                    // Config
                    // /pathcrafter config <name>
                    LiteralCommandNode<FabricClientCommandSource> configNode = ClientCommandManager
                            .literal("config")
                            .build();
                    pathcrafterNode.addChild(configNode);
                    configNode.addChild(
                            ClientCommandManager
                                    .literal("BLOCK_COLUMN_DEBUG_INFO")
                                    .executes(new Debug.DebugToggle(DebugToggles.BLOCK_COLUMN_DEBUG_INFO))
                                    .build());
                    configNode.addChild(
                            ClientCommandManager
                                    .literal("TERRAIN_DEBUG_INFO")
                                    .executes(new Debug.DebugToggle(DebugToggles.TERRAIN_DEBUG_INFO))
                                    .build());
                    configNode.addChild(
                            ClientCommandManager
                                    .literal("TERRAIN_VERTEX_DEBUG_INFO")
                                    .executes(new Debug.DebugToggle(DebugToggles.TERRAIN_VERTEX_DEBUG_INFO))
                                    .build());
                    configNode.addChild(
                            ClientCommandManager
                                    .literal("TERRAIN_EDGE_GENERATOR_DEBUG_INFO")
                                    .executes(new Debug.DebugToggle(DebugToggles.TERRAIN_EDGE_GENERATOR_DEBUG_INFO))
                                    .build());
                    configNode.addChild(
                            ClientCommandManager
                                    .literal("TERRAIN_EDGE_LIST_EDGES")
                                    .executes(new Debug.DebugToggle(DebugToggles.TERRAIN_EDGE_LIST_EDGES))
                                    .build());
                    configNode.addChild(
                            ClientCommandManager
                                    .literal("TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO")
                                    .executes(new Debug.DebugToggle(DebugToggles.TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO))
                                    .build());
                    configNode.addChild(
                            ClientCommandManager
                                    .literal("TERRAIN_COLUMNS_DEBUG_INFO")
                                    .executes(new Debug.DebugToggle(DebugToggles.TERRAIN_COLUMNS_DEBUG_INFO))
                                    .build());
                    configNode.addChild(
                            ClientCommandManager
                                    .literal("SEGMENT_LIST_DEBUG_INFO")
                                    .executes(new Debug.DebugToggle(DebugToggles.SEGMENT_LIST_DEBUG_INFO))
                                    .build());
                    configNode.addChild(
                            ClientCommandManager
                                    .literal("SEGMENT_LIST_ALLOW_INFO_CALL")
                                    .executes(new Debug.DebugToggle(DebugToggles.SEGMENT_LIST_ALLOW_INFO_CALL))
                                    .build());
                };

        ClientCommandRegistrationCallback.EVENT.register(c);

    }
}

package stargazing.pathcrafter.command;

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

                    BoopCommand.register(pathcrafterNode);
                    FindEdgeCommand.register(pathcrafterNode);
                    FindEdgesCommand.register(pathcrafterNode);
                    ConfigCommand.register(pathcrafterNode);
                    GenTerrainCommand.register(pathcrafterNode);
                    GenVerticesCommand.register(pathcrafterNode);
                    FindPathCommand.register(pathcrafterNode);
                };

        ClientCommandRegistrationCallback.EVENT.register(c);
    }
}

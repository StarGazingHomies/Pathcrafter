package stargazing.pathcrafter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class BoopCommand {
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
        return Command.SINGLE_SUCCESS;
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> pathcrafterNode) {
        // Boop (Test command)
        // /pathcrafter boop
        LiteralCommandNode<FabricClientCommandSource> boopNode = ClientCommandManager
                .literal("boop")
                .executes(BoopCommand::boop)
                .build();
        pathcrafterNode.addChild(boopNode);
    }
}

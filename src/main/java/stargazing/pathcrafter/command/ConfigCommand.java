package stargazing.pathcrafter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import stargazing.pathcrafter.config.DebugToggles;

public class ConfigCommand {

    public static int listSettings(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("Settings are not implemented yet!"));
        return 1;
    }

    public static class ToggleBooleanSetting implements Command<FabricClientCommandSource> {
        DebugToggles.BooleanSetting s;
        public ToggleBooleanSetting(DebugToggles.BooleanSetting s) {
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
            return Command.SINGLE_SUCCESS;
        }

    }


    public static class SetBooleanSetting implements Command<FabricClientCommandSource> {
        DebugToggles.BooleanSetting s;
        public SetBooleanSetting(DebugToggles.BooleanSetting s) {
            this.s = s;
        }

        @Override
        public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
            boolean value = BoolArgumentType.getBool(context, "value");
            s.set(value);
            if (s.enabled()) {
                context.getSource().sendFeedback(Text.literal("Setting "+s.name+" set to true."));
            }
            else {
                context.getSource().sendFeedback(Text.literal("Setting "+s.name+" set to false."));
            }
            return Command.SINGLE_SUCCESS;
        }
    }

    public static void registerBoolean(
            LiteralCommandNode<FabricClientCommandSource> configNode,
            DebugToggles.BooleanSetting setting,
            String settingName
    ) {
        LiteralCommandNode<FabricClientCommandSource> settingNode = ClientCommandManager
                .literal(settingName)
                .executes(new ToggleBooleanSetting(setting))
                .build();
        settingNode.addChild(ClientCommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(new SetBooleanSetting(setting))
                .build()
        );
        configNode.addChild(settingNode);
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> pathcrafterNode) {
        LiteralCommandNode<FabricClientCommandSource> configNode = ClientCommandManager
                .literal("config")
                .build();

        pathcrafterNode.addChild(configNode);
        registerBoolean(configNode, DebugToggles.BLOCK_COLUMN_DEBUG_INFO, "BLOCK_COLUMN_DEBUG_INFO");
        registerBoolean(configNode, DebugToggles.TERRAIN_DEBUG_INFO, "TERRAIN_DEBUG_INFO");
        registerBoolean(configNode, DebugToggles.TERRAIN_VERTEX_DEBUG_INFO, "TERRAIN_VERTEX_DEBUG_INFO");
        registerBoolean(configNode, DebugToggles.TERRAIN_EDGE_GENERATOR_DEBUG_INFO, "TERRAIN_EDGE_GENERATOR_DEBUG_INFO");
        registerBoolean(configNode, DebugToggles.TERRAIN_EDGE_LIST_EDGES, "TERRAIN_EDGE_LIST_EDGES");
        registerBoolean(configNode, DebugToggles.TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO, "TERRAIN_INDIVIDUAL_EDGE_DEBUG_INFO");
        registerBoolean(configNode, DebugToggles.TERRAIN_COLUMNS_DEBUG_INFO, "TERRAIN_COLUMNS_DEBUG_INFO");
        registerBoolean(configNode, DebugToggles.SEGMENT_LIST_DEBUG_INFO, "SEGMENT_LIST_DEBUG_INFO");
        registerBoolean(configNode, DebugToggles.SEGMENT_LIST_ALLOW_INFO_CALL, "SEGMENT_LIST_ALLOW_INFO_CALL");
    }
}

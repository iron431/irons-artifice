package io.redspace.irons_artifice.command;

import io.redspace.irons_artifice.item.GunplayManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public final class DebugCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("irons_artifice")
                .then(Commands.literal("debug")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("shoot")
                                .executes(DebugCommands::shoot))));
    }

    private static int shoot(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getLevel();

        Vec3 origin = source.getPosition();
        Vec2 rotation = source.getRotation();
        Vec3 direction = Vec3.directionFromRotation(rotation.x, rotation.y);

        if (!GunplayManager.debugFire(level, player, origin, direction)) {
            source.sendFailure(Component.literal(player.getName().getString() + " isn't holding a gun."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(String.format(
                "Fired debug shot from (%.2f, %.2f, %.2f) towards (%.2f, %.2f, %.2f)",
                origin.x, origin.y, origin.z, direction.x, direction.y, direction.z)), true);
        return 1;
    }
}

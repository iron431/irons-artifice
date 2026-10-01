package io.redspace.irons_artifice.command;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.GunplayManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
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
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("shoot")
                                .executes(DebugCommands::shoot))
                        .then(Commands.literal("give_attribute")
                                .then(Commands.argument("item", ItemArgument.item(event.getBuildContext()))
                                        .then(Commands.argument("attribute", ResourceArgument.resource(event.getBuildContext(), Registries.ATTRIBUTE))
                                                .then(Commands.argument("add", DoubleArgumentType.doubleArg())
                                                        .then(Commands.argument("m_total", DoubleArgumentType.doubleArg())
                                                                .then(Commands.argument("m_base", DoubleArgumentType.doubleArg())
                                                                        .executes(DebugCommands::giveAttribute)))))))));
    }

    private static int giveAttribute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = ItemArgument.getItem(context, "item").createItemStack(1);
        Holder<Attribute> attribute = ResourceArgument.getAttribute(context, "attribute");

        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        modifiers = withDebugModifier(modifiers, attribute, DoubleArgumentType.getDouble(context, "add"), AttributeModifier.Operation.ADD_VALUE);
        modifiers = withDebugModifier(modifiers, attribute, DoubleArgumentType.getDouble(context, "m_total"), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifiers = withDebugModifier(modifiers, attribute, DoubleArgumentType.getDouble(context, "m_base"), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);

        Component name = stack.getDisplayName();
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        context.getSource().sendSuccess(() -> Component.literal("Gave ").append(name).append(" to " + player.getName().getString()), true);
        return 1;
    }

    private static ItemAttributeModifiers withDebugModifier(ItemAttributeModifiers modifiers, Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
        if (amount == 0) {
            return modifiers;
        }
        AttributeModifier modifier = new AttributeModifier(IronsArtifice.id("debug/" + operation.getSerializedName()), amount, operation);
        return modifiers.withModifierAdded(attribute, modifier, EquipmentSlotGroup.ANY);
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

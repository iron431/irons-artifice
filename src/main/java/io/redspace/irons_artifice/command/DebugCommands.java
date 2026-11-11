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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@Mod.EventBusSubscriber
public final class DebugCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("irons_artifice")
                .then(Commands.literal("debug")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
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
        ItemStack stack = ItemArgument.getItem(context, "item").createItemStack(1, false);
        Attribute attribute = ResourceArgument.getAttribute(context, "attribute").value();

        retainDefaultModifiers(stack);
        addDebugModifier(stack, attribute, DoubleArgumentType.getDouble(context, "add"), AttributeModifier.Operation.ADDITION);
        addDebugModifier(stack, attribute, DoubleArgumentType.getDouble(context, "m_total"), AttributeModifier.Operation.MULTIPLY_TOTAL);
        addDebugModifier(stack, attribute, DoubleArgumentType.getDouble(context, "m_base"), AttributeModifier.Operation.MULTIPLY_BASE);

        Component name = stack.getDisplayName();
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        context.getSource().sendSuccess(() -> Component.literal("Gave ").append(name).append(" to " + player.getName().getString()), true);
        return 1;
    }

    /**
     * Stack modifiers replace the item's defaults once present, so the defaults are copied onto the stack first.
     */
    private static void retainDefaultModifiers(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("AttributeModifiers", Tag.TAG_LIST)) {
            return;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            stack.getItem().getAttributeModifiers(slot, stack).forEach((attribute, modifier) -> stack.addAttributeModifier(attribute, modifier, slot));
        }
    }

    private static void addDebugModifier(ItemStack stack, Attribute attribute, double amount, AttributeModifier.Operation operation) {
        if (amount == 0) {
            return;
        }
        String name = IronsArtifice.id("debug/" + operation.name().toLowerCase(Locale.ROOT)).toString();
        UUID id = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
        String attributeName = BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString();
        stack.getOrCreateTag().getList("AttributeModifiers", Tag.TAG_COMPOUND).removeIf(tag -> tag instanceof CompoundTag entry
                && entry.getString("AttributeName").equals(attributeName)
                && entry.hasUUID("UUID") && entry.getUUID("UUID").equals(id));
        stack.addAttributeModifier(attribute, new AttributeModifier(id, name, amount, operation), null);
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

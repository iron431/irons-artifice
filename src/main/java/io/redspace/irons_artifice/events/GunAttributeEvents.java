package io.redspace.irons_artifice.events;

import io.redspace.irons_artifice.attribute.GunStat;
import io.redspace.irons_artifice.gun.GunStatResolver;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import java.util.function.BiConsumer;

/**
 * Makes a gun's main hand attribute modifiers describe the whole gun: its own stats plus every installed modifier item.
 */
@EventBusSubscriber
public final class GunAttributeEvents {

    /**
     * Posted every time a stack's attribute modifiers are queried, on both sides, including once per slot group for every tooltip frame
     */
    @SubscribeEvent
    public static void gatherGunStats(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof GunItem gunItem)) {
            return;
        }
        ItemAttributeModifiers baseStats = gunItem.getGun().baseStats();
        // a stack whose attribute modifiers were overridden would otherwise lose stats nothing can work without, like muzzle velocity.
        // An untouched stack still carries the very instance the item was given, which is nearly every stack, nearly every call
        if (event.getDefaultModifiers() != baseStats) {
            for (ItemAttributeModifiers.Entry base : baseStats.modifiers()) {
                if (!hasModifier(event, base.attribute(), base.modifier())) {
                    event.addModifier(base.attribute(), base.modifier(), base.slot());
                }
            }
        }
        forEachInstalledModifier(stack, (attribute, modifier) -> {
            if (modifier.is(GunStat.BASE_ID)) {
                event.replaceModifier(attribute, modifier, EquipmentSlotGroup.MAINHAND);
            } else {
                event.addModifier(attribute, modifier, EquipmentSlotGroup.MAINHAND);
            }
        });
    }

    /**
     * Guns describe their stats themselves, fully resolved. The raw modifiers behind them would only repeat that, piecemeal.
     * What an installed modifier does to its holder rather than to the gun is not part of that description, so it stays visible.
     */
    @SubscribeEvent
    public static void hideGunStatLines(GatherSkippedAttributeTooltipsEvent event) {
        ItemStack stack = event.getStack();
        if (!(stack.getItem() instanceof GunItem)) {
            return;
        }
        event.skipId(GunStat.BASE_ID);
        forEachInstalledModifier(stack, (attribute, modifier) -> {
            if (attribute.value() instanceof GunStat) {
                event.skipId(modifier.id());
            }
        });
    }

    /**
     * What the modifier items installed in the gun contribute, carrying the ids they have while installed.
     * <p>
     * Gun stats change the gun. An entry on any other attribute is simply passed on to whoever holds the gun.
     */
    public static void forEachInstalledModifier(ItemStack gun, BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        int[] ordinal = {0};
        GunContainer.forEachInstalled(gun, installed -> {
            ItemAttributeModifiers stats = installed.get(DataComponentRegistry.GUN_MODIFIER_STATS.get());
            if (stats != null) {
                for (ItemAttributeModifiers.Entry entry : stats.modifiers()) {
                    consumer.accept(entry.attribute(), whileInstalled(entry, ordinal[0]));
                }
            }
            ordinal[0]++;
        });
    }

    private static AttributeModifier whileInstalled(ItemAttributeModifiers.Entry entry, int ordinal) {
        AttributeModifier modifier = entry.modifier();
        if (!(entry.attribute().value() instanceof GunStat)) {
            return new AttributeModifier(GunStatResolver.heldId(modifier.id(), ordinal), modifier.amount(), modifier.operation());
        }
        if (modifier.is(GunStat.BASE_ID)) {
            // keeps the id so it replaces the gun's own base instead of stacking with it
            return modifier;
        }
        return new AttributeModifier(GunStatResolver.installedId(modifier.id(), ordinal), modifier.amount(), modifier.operation());
    }

    private static boolean hasModifier(ItemAttributeModifierEvent event, Holder<Attribute> attribute, AttributeModifier modifier) {
        // reading the list does not force the event to copy it, unlike asking it to add something that is already there
        for (ItemAttributeModifiers.Entry entry : event.getModifiers()) {
            if (entry.matches(attribute, modifier.id())) {
                return true;
            }
        }
        return false;
    }
}

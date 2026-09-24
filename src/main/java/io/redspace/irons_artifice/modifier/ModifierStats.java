package io.redspace.irons_artifice.modifier;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.attribute.GunStat;
import io.redspace.irons_artifice.events.AmmoRefundEvents;
import io.redspace.irons_artifice.registry.AttributeRegistry;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemAttributeModifiers.Display;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Stat changes of the built-in modifier items, stored on the item as {@link DataComponentRegistry#GUN_MODIFIER_STATS}.
 * <p>
 * Call these from the item factory, not from a static initializer: attribute holders are only bound once attributes are registered.
 */
public final class ModifierStats {

    public static Builder builder(String modifierItemName) {
        return new Builder(IronsArtifice.id(modifierItemName));
    }

    /**
     * Tooltip line shown in place of the generated one, for changes that read better as a sentence
     */
    public static Display describedAs(MutableComponent text) {
        return Display.override(text.withStyle(ChatFormatting.AQUA));
    }

    public static ItemAttributeModifiers incendiaryTip() {
        return builder("incendiary_tip_modifier")
                .multiply(AttributeRegistry.GUN_DAMAGE, 0.10)
                .build();
    }

    public static ItemAttributeModifiers spiralTip() {
        return builder("spiral_tip_modifier")
                .add(AttributeRegistry.UNDERWATER_DRAG, 1, describedAs(Component.translatable("irons_artifice.modifier.spiral_tip")))
                .build();
    }

    public static ItemAttributeModifiers scattershot() {
        return builder("scattershot_modifier")
                .add(AttributeRegistry.PROJECTILE_COUNT, 3)
                .multiply(AttributeRegistry.GUN_DAMAGE, 0.25)
                .add(AttributeRegistry.BULLET_SPREAD, 3)
                .build();
    }

    public static ItemAttributeModifiers breachingShell() {
        return builder("breaching_shell_modifier")
                .enable(AttributeRegistry.BREAKS_BLOCKS)
                .multiply(AttributeRegistry.BLOCK_DAMAGE, 0.5)
                .build();
    }

    public static ItemAttributeModifiers windChamber() {
        return builder("wind_chamber_modifier")
                .multiply(AttributeRegistry.CHARACTER_BLOWBACK, 0.5)
                .multiply(AttributeRegistry.IN_AIR_PENALTY, -0.5)
                .build();
    }

    public static ItemAttributeModifiers overchargedPowder() {
        return builder("overcharged_powder_modifier")
                .multiply(AttributeRegistry.BULLET_SPEED, 0.25)
                .multiply(AttributeRegistry.CAMERA_RECOIL, 0.20)
                .multiply(AttributeRegistry.GUN_DAMAGE, 0.15)
                .build();
    }

    public static ItemAttributeModifiers antigravityPowder() {
        return builder("antigravity_powder_modifier")
                .multiply(AttributeRegistry.BULLET_GRAVITY, -1, describedAs(Component.translatable("irons_artifice.modifier.antigravity")))
                .build();
    }

    public static ItemAttributeModifiers seekingPowder() {
        return builder("seeking_powder_modifier")
                .add(AttributeRegistry.SEEKING, 0.10)
                .multiply(AttributeRegistry.BULLET_GRAVITY, -0.25)
                .setBase(AttributeRegistry.BULLET_SPEED, 3)
                .build();
    }

    public static ItemAttributeModifiers enchantedBullet() {
        return builder("enchanted_bullet_modifier")
                .multiply(AttributeRegistry.AMMO_CONSUME_CHANCE, -AmmoRefundEvents.INFINITY_CHANCE)
                .build();
    }

    public static ItemAttributeModifiers trickBullet() {
        return builder("trick_bullet_modifier")
                .add(AttributeRegistry.RICOCHET, 3)
                .build();
    }

    public static ItemAttributeModifiers steelCore() {
        return builder("steel_core_modifier")
                .add(AttributeRegistry.PIERCING, 2)
                .build();
    }

    public static ItemAttributeModifiers leadCore() {
        return builder("lead_core_modifier")
                .multiply(AttributeRegistry.BULLET_SPEED, -0.10)
                .add(AttributeRegistry.BULLET_KNOCKBACK, 0.5)
                .build();
    }

    public static ItemAttributeModifiers bloodlettingTip() {
        return builder("bloodletting_tip_modifier")
                .add(AttributeRegistry.LEECH, 1)
                .build();
    }

    public static ItemAttributeModifiers hairTrigger() {
        return builder("hair_trigger_modifier")
                .multiply(AttributeRegistry.FIRE_RATE, 0.5)
                .build();
    }

    public static ItemAttributeModifiers gasVent() {
        return builder("gas_vent_modifier")
                .multiply(AttributeRegistry.CAMERA_RECOIL, -0.5)
                .build();
    }

    public static ItemAttributeModifiers gunOil() {
        return builder("gun_oil_modifier")
                .multiply(AttributeRegistry.RELOAD_SPEED, 0.25)
                .build();
    }

    public static ItemAttributeModifiers bufferSpring() {
        return builder("buffer_spring_modifier")
                .multiply(AttributeRegistry.CHARACTER_BLOWBACK, -1, describedAs(Component.translatable("irons_artifice.modifier.negate",
                        Component.translatable("irons_artifice.component_type.character_blowback"))))
                .build();
    }

    public static ItemAttributeModifiers mechanicalRepeater() {
        return builder("mechanical_repeater_modifier")
                .enable(AttributeRegistry.AUTO_FIRE)
                .build();
    }

    public static ItemAttributeModifiers mechanicalAccelerator() {
        return builder("mechanical_accelerator_modifier")
                .add(AttributeRegistry.ACCELERATING, 1, Display.hidden())
                .build();
    }

    public static ItemAttributeModifiers scopeAttachment() {
        return builder("scope_attachment_modifier")
                .add(AttributeRegistry.BULLET_SPREAD, -1)
                .build();
    }

    /**
     * Not currently given to any item
     */
    public static ItemAttributeModifiers heavyBolt() {
        return builder("heavy_bolt_modifier")
                .multiply(AttributeRegistry.FIRE_RATE, -0.25)
                .multiply(AttributeRegistry.RELOAD_SPEED, -0.25)
                .multiply(AttributeRegistry.GUN_DAMAGE, 0.25)
                .build();
    }

    public static final class Builder {
        private final Identifier id;
        private final ItemAttributeModifiers.Builder entries = ItemAttributeModifiers.builder();
        private final Set<Map.Entry<Holder<Attribute>, Identifier>> used = new HashSet<>();

        private Builder(Identifier id) {
            this.id = id;
        }

        /**
         * Flat change
         */
        public Builder add(Holder<Attribute> stat, double amount) {
            return add(stat, amount, Display.attributeModifiers());
        }

        public Builder add(Holder<Attribute> stat, double amount, Display display) {
            return entry(stat, new AttributeModifier(id, amount, Operation.ADD_VALUE), display);
        }

        /**
         * Multiplies the final value by {@code 1 + amount}
         */
        public Builder multiply(Holder<Attribute> stat, double amount) {
            return multiply(stat, amount, Display.attributeModifiers());
        }

        public Builder multiply(Holder<Attribute> stat, double amount, Display display) {
            return entry(stat, new AttributeModifier(id, amount, Operation.ADD_MULTIPLIED_TOTAL), display);
        }

        /**
         * Turns a toggle stat on
         */
        public Builder enable(Holder<Attribute> toggle) {
            return add(toggle, 1);
        }

        /**
         * Replaces the gun's own base for the stat, keeping every other modifier. Data cannot know who will fire the gun, so {@code base}
         * assumes a shooter whose own base for the stat is the attribute's default, which it is unless something deliberately changed it.
         */
        public Builder setBase(Holder<Attribute> stat, double base) {
            return setBase(stat, base, Display.attributeModifiers());
        }

        public Builder setBase(Holder<Attribute> stat, double base, Display display) {
            return entry(stat, new AttributeModifier(GunStat.BASE_ID, base - stat.value().getDefaultValue(), Operation.ADD_VALUE), display);
        }

        private Builder entry(Holder<Attribute> stat, AttributeModifier modifier, Display display) {
            // modifiers are unique per attribute and id, so a second entry would silently replace the first
            if (!used.add(Map.entry(stat, modifier.id()))) {
                throw new IllegalArgumentException(modifier.id() + " already changes " + stat.getRegisteredName());
            }
            // vanilla compares attribute holders by identity in places, so store the registry's own holder
            entries.add(stat.getDelegate(), modifier, EquipmentSlotGroup.ANY, display);
            return this;
        }

        public ItemAttributeModifiers build() {
            return entries.build();
        }
    }
}

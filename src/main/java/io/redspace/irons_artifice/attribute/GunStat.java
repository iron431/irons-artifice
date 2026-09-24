package io.redspace.irons_artifice.attribute;

import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;

/**
 * Marks an {@link Attribute} as a gun stat. Every registered attribute implementing this is attached to every living entity, synced to
 * clients and takes part in shot resolution, including ones added by addons. A number that no entity should carry, like the offset of a
 * suppressor's muzzle, is not a stat but an item component on the gun.
 */
public interface GunStat {
    /**
     * Id of the modifier a gun uses to supply its own base for a stat, like {@code minecraft:base_attack_damage} on a sword.
     * A gun modifier entry carrying this id replaces the gun's base instead of stacking with it.
     * <p>
     * Reserved for the gun in the main hand, together with the {@code <id>/installed_<n>} and {@code <id>/held_<n>} ids its installed
     * modifiers surface under. Resolving a shot removes these from the shooter's stats before applying the fired gun, so anything else
     * using them on a gun stat is dropped from every shot.
     */
    Identifier BASE_ID = IronsArtifice.id("base_gun_stat");

    /**
     * {@link Attribute} does not expose its own sentiment
     */
    Attribute.Sentiment sentiment();

    /**
     * Lang key of the short stat name shown on gun modifier tooltips
     */
    String shortNameKey();
}

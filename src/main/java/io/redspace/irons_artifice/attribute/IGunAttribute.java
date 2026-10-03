package io.redspace.irons_artifice.attribute;

import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

/**
 * An {@link Attribute} linked to a shot component. Applied to every shot composed by an entity that carries it.
 */
public interface IGunAttribute {
    /**
     * @param instance the shooter's instance of this attribute
     */
    void applyTo(AttributeInstance instance, ShotProfile profile);
}

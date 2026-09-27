package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import io.redspace.ironslib.registry.IronsLibRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.item.component.UseEffects;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class BayonetAttachmentModifier implements GunModifier {
    /**
     * The {@code KineticAnimation} the client registers for bayonet charges.
     */
    public static final Identifier BAYONET_ANIMATION = IronsArtifice.id("bayonet");

    @Override
    public void apply(ShotComponentMap components) {
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.bayonet").withStyle(ChatFormatting.AQUA));
    }

    @Override
    public Optional<DataComponentPatch> getPatch() {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        AttackRange defaultSpearAttackRange = new AttackRange(2.0F, 4.5F, 2.0F, 6.5F, 0.125F, 0.5F);
        builder.set(DataComponents.ATTACK_RANGE, defaultSpearAttackRange);
        builder.set(DataComponents.KINETIC_WEAPON,
                createVanillaSpear(
                        1f,
                        0.5f,
                        2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F
                ));
        builder.set(DataComponents.USE_EFFECTS, new UseEffects(true, false, 1));
        builder.set(DataComponentRegistry.ATTACHMENT.get(), new AttachmentMap(Map.of(
                GunBones.SOCKET_BAYONET, IronsArtifice.id("iron_bayonet")
        )));
        builder.set(IronsLibRegistries.ComponentRegistry.KINETIC_ANIMATION.get(), BAYONET_ANIMATION);
        return Optional.of(builder.build());
    }

    public KineticWeapon createVanillaSpear(
            float damageMultiplier,
            float delay,
            float dismountTime,
            float dismountThreshold,
            float knockbackTime,
            float knockbackThreshold,
            float damageTime,
            float damageThreshold
    ) {
        return new KineticWeapon(
                10,
                (int) (delay * 20.0F),
                KineticWeapon.Condition.ofAttackerSpeed((int) (dismountTime * 20.0F), dismountThreshold),
                KineticWeapon.Condition.ofAttackerSpeed((int) (knockbackTime * 20.0F), knockbackThreshold),
                KineticWeapon.Condition.ofRelativeSpeed((int) (damageTime * 20.0F), damageThreshold),
                0.38F,
                damageMultiplier,
                Optional.of(SoundEvents.SPEAR_USE),
                Optional.of(SoundEvents.SPEAR_HIT)
        );
    }
}

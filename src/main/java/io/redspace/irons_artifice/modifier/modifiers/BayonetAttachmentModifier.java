package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.item.kinetic.AttackRange;
import io.redspace.irons_artifice.item.kinetic.KineticWeapon;
import io.redspace.irons_artifice.item.kinetic.UseEffects;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import io.redspace.irons_artifice.registry.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class BayonetAttachmentModifier implements GunModifier {

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
        builder.set(DataComponentRegistry.ATTACK_RANGE.get(), defaultSpearAttackRange);
        builder.set(DataComponentRegistry.KINETIC_WEAPON.get(),
                createVanillaSpear(
                        1f,
                        0.5f,
                        2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F
                ));
        builder.set(DataComponentRegistry.USE_EFFECTS.get(), new UseEffects(true, false, 1));
        builder.set(DataComponentRegistry.ATTACHMENT.get(), new AttachmentMap(Map.of(
                GunBones.SOCKET_BAYONET, IronsArtifice.id("iron_bayonet")
        )));
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
                Optional.<Holder<SoundEvent>>of(SoundRegistry.BAYONET_USE),
                Optional.<Holder<SoundEvent>>of(SoundRegistry.BAYONET_HIT)
        );
    }
}

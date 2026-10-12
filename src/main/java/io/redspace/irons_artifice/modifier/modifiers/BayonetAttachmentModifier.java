package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.registry.SoundRegistry;
import io.redspace.irons_artifice.utils.ModifierPatchHandler;
import io.redspace.ironslib.kinetic_weapon.AttackRange;
import io.redspace.ironslib.kinetic_weapon.KineticWeapon;
import io.redspace.ironslib.kinetic_weapon.UseEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class BayonetAttachmentModifier implements GunModifier {

    /** The {@code KineticAnimation} the client registers for bayonet charges. */
    public static final ResourceLocation BAYONET_ANIMATION = IronsArtifice.id("bayonet");

    @Override
    public void apply(ShotComponentMap components) {
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.bayonet").withStyle(ChatFormatting.AQUA));
    }

    @Override
    public Optional<CompoundTag> getPatch() {
        CompoundTag patch = new CompoundTag();
        AttackRange defaultSpearAttackRange = new AttackRange(2.0F, 4.5F, 2.0F, 6.5F, 0.125F, 0.5F);
        ModifierPatchHandler.set(patch, AttackRange.TAG_KEY, AttackRange.CODEC, defaultSpearAttackRange);
        ModifierPatchHandler.set(patch, KineticWeapon.TAG_KEY, KineticWeapon.CODEC,
                createVanillaSpear(
                        1f,
                        0.5f,
                        2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F
                ).withAnimation(BAYONET_ANIMATION));
        ModifierPatchHandler.set(patch, UseEffects.TAG_KEY, UseEffects.CODEC, new UseEffects(true, 1));
        ModifierPatchHandler.set(patch, AttachmentMap.TAG_KEY, AttachmentMap.CODEC, new AttachmentMap(Map.of(
                GunBones.SOCKET_BAYONET, IronsArtifice.id("iron_bayonet")
        )));
        return Optional.of(patch);
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
                SoundRegistry.BAYONET_USE.getHolder(),
                SoundRegistry.BAYONET_HIT.getHolder()
        );
    }
}

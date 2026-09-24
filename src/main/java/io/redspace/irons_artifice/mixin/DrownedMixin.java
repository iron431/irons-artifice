package io.redspace.irons_artifice.mixin;

import io.redspace.irons_artifice.entity.IGunslingerMob;
import io.redspace.irons_artifice.entity.ai.DrownedRangedGunAttackGoal;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.registry.AttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Drowned.class)
public abstract class DrownedMixin extends Monster implements IGunslingerMob {
    protected DrownedMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "addBehaviourGoals", at = @At("HEAD"))
    private void irons_artifice$addGunGoal(CallbackInfo ci) {
        this.goalSelector.addGoal(0, new DrownedRangedGunAttackGoal((Drowned) (Object) this, 14, 10, 30, 0, 0));
    }

    @Override
    public void customizeMobShot(@NotNull Mob mob, @NotNull ShotProfile shotProfile) {
        IGunslingerMob.super.customizeMobShot(mob, shotProfile);
        // tricorne's +25% destroying plebs since '26
        shotProfile.addModifier(AttributeRegistry.GUN_DAMAGE, DrownedRangedGunAttackGoal.NERF_MODIFIER, -0.50, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
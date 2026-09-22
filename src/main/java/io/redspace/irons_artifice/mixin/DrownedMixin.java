package io.redspace.irons_artifice.mixin;

import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.entity.IGunslingerMob;
import io.redspace.irons_artifice.entity.ai.DrownedRangedGunAttackGoal;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Drowned;
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
        shotProfile.modifyValue(ShotComponents.DAMAGE, new ValueModifier(-0.50, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL));
    }
}
package io.redspace.irons_artifice.mixin;

import io.redspace.irons_artifice.entity.ai.RangedGunAttackGoal;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin extends Monster implements RangedAttackMob {
    @Shadow
    @Final
    private RangedBowAttackGoal<AbstractSkeleton> bowGoal;

    @Shadow
    @Final
    private MeleeAttackGoal meleeGoal;

    protected AbstractSkeletonMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Unique
    private RangedGunAttackGoal<AbstractSkeleton> irons_artifice$gunGoal;

    @Inject(method = "reassessWeaponGoal", at = @At("HEAD"), cancellable = true)
    private void irons_artifice$assessGunGoal(CallbackInfo ci) {
        if (irons_artifice$gunGoal == null) {
            irons_artifice$gunGoal = new RangedGunAttackGoal<>((AbstractSkeleton) (Object) this, 32, 20, 40, 60, 100);
        }
        this.goalSelector.removeGoal(irons_artifice$gunGoal);
        if (this.getMainHandItem().getItem() instanceof GunItem) {
            this.goalSelector.removeGoal(bowGoal);
            this.goalSelector.removeGoal(meleeGoal);
            this.goalSelector.addGoal(4, irons_artifice$gunGoal);
            ci.cancel();
        }
    }
}

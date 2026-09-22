package io.redspace.irons_artifice.mixin;

import io.redspace.irons_artifice.config.ServerConfig;
import io.redspace.irons_artifice.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PatrolSpawner.class)
public class IllagerPatrolMixin {
    @Inject(method = "spawnPatrolMember", at = @At("HEAD"), cancellable = true)
    private void irons_artifice$swapPatrolLeader(ServerLevel level, BlockPos pos, RandomSource random, boolean isLeader, CallbackInfoReturnable<Boolean> cir) {
        if (isLeader && level.getRandom().nextFloat() < ServerConfig.ILLIFICER_REPLACE_PATROL_LEADER_CHANCE.get()) {
            BlockState state = level.getBlockState(pos);
            if (!NaturalSpawner.isValidEmptySpawnBlock(level, pos, state, state.getFluidState(), EntityRegistry.ILLIFICER.get())) {
                return;
            }
            if (!PatrollingMonster.checkPatrollingMonsterSpawnRules(EntityRegistry.ILLIFICER.get(), level, MobSpawnType.PATROL, pos, random)) {
                return;
            }
            PatrollingMonster mob = EntityRegistry.ILLIFICER.get().create(level, MobSpawnType.PATROL);
            if (mob != null) {
                mob.setPatrolLeader(true);
                mob.findPatrolTarget();

                mob.setPos(pos.getX(), pos.getY(), pos.getZ());
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);
                level.addFreshEntityWithPassengers(mob);
                cir.setReturnValue(true);
            }
        }
    }
}

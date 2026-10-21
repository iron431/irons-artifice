package io.redspace.irons_artifice.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface MobAccessor {
    @Accessor("lootTable")
    void irons_artifice$setLootTable(@Nullable ResourceLocation lootTable);
}

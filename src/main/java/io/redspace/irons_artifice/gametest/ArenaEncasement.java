package io.redspace.irons_artifice.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/**
 * Barrier shell around a test structure: walls on the X and Z perimeter and a ceiling, since no test asks for sky
 * access. Without it a knocked-back mob leaves the floor and drops out of the next shot's line.
 */
public final class ArenaEncasement {
    private ArenaEncasement() {
    }

    /** Call before spawning anything; the walls and ceiling sit one block outside the structure bounds. */
    public static void encase(GameTestHelper helper) {
        encase(TestFixtures.arenaBounds(helper), helper.getLevel());
    }

    private static void encase(AABB bounds, ServerLevel level) {
        BlockPos min = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ).offset(-1, 0, -1);
        BlockPos max = BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ);
        BlockPos.betweenClosedStream(min, max).forEach(pos -> {
            boolean wall = pos.getX() == min.getX() || pos.getX() == max.getX()
                    || pos.getZ() == min.getZ() || pos.getZ() == max.getZ();
            boolean ceiling = pos.getY() == max.getY();
            if (wall || ceiling) {
                level.setBlockAndUpdate(pos, Blocks.BARRIER.defaultBlockState());
            }
        });
    }
}

package io.redspace.irons_artifice.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ServerConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue ILLIFICER_REPLACE_PATROL_LEADER_CHANCE;
    public static final ForgeConfigSpec.DoubleValue DROWNED_PIRATE_CURSE_CHANCE;

    static {
        ILLIFICER_REPLACE_PATROL_LEADER_CHANCE = BUILDER
                .push("illificer")
                .comment("Chance (0-1) for an Illificer to replace an Patrol Leader on natural patrol")
                .defineInRange("replacePatrolLeader", 0.50, 0.0, 1.0);
        BUILDER.pop();
        DROWNED_PIRATE_CURSE_CHANCE = BUILDER
                .push("drowned-pirates")
                .comment("Chance (0-1) for Drowned Pirates to ambush various ocean chests")
                .defineInRange("drownedPirateAmbush", 0.25, 0.0, 1.0);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

}

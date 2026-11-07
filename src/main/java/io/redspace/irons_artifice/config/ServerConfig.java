package io.redspace.irons_artifice.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue ILLIFICER_REPLACE_PATROL_LEADER_CHANCE;
    public static final ModConfigSpec.DoubleValue DROWNED_PIRATE_CURSE_CHANCE;
    public static final ModConfigSpec.BooleanValue GUN_OVERRIDES_ENABLED;
    public static final ModConfigSpec.ConfigValue<List<? extends UnmodifiableConfig>> GUN_OVERRIDES;

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
        GUN_OVERRIDES_ENABLED = BUILDER
                .comment(
                        "Per-gun stat overrides, applied to the gun's base stats before modifiers.",
                        "Each [[guns.overrides]] table names one gun by item id, then sets components by id.",
                        "A number replaces the component's base value; true or false sets a flag.",
                        "Only components already in the gun's base stats can be changed. Common ones:",
                        "  \"irons_artifice:damage\", \"irons_artifice:spread\", \"irons_artifice:fire_delay\", \"irons_artifice:projectile_count\", \"irons_artifice:bullet_speed\", \"irons_artifice:knockback\", \"irons_artifice:character_blowback\"",
                        "Example:",
                        "  [[guns.overrides]]",
                        "  item = \"irons_artifice:musket\"",
                        "  \"irons_artifice:damage\" = 24.0",
                        "  \"irons_artifice:spread\" = 0.25")
                .push("guns")
                .comment("Whether the overrides below apply")
                .define("enabled", true);
        GUN_OVERRIDES = BUILDER.defineListAllowEmpty("overrides", GunConfig::stubPerGun, Config::inMemory, GunConfig::isGunEntry);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

}

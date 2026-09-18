package io.redspace.irons_artifice.config;

import net.minecraft.util.FastColor;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER.push("Ammo-Counter")
            .comment("Whether the ammo indicator HUD is shown when holding a gun")
            .define("enabled", true);

    public static final ModConfigSpec.ConfigValue<Anchor> ANCHOR = BUILDER
            .comment("Ammo Counter Screen Anchor")
            .defineEnum("anchor", Anchor.BOTTOM_RIGHT);

    public static final ModConfigSpec.IntValue OFFSET_X = BUILDER
            .comment("Horizontal offset from the anchor edge (pixels)")
            .defineInRange("offsetX", 24, 0, 512);

    public static final ModConfigSpec.IntValue OFFSET_Y = BUILDER
            .comment("Vertical offset from the anchor edge (pixels)")
            .defineInRange("offsetY", 24, 0, 512);

    public static final ModConfigSpec.BooleanValue SHOW_ICON = BUILDER
            .comment("Show the bullet icon beside the ammo text")
            .define("showIcon", true);

    public static final ModConfigSpec.BooleanValue SHOW_MAGAZINE = BUILDER
            .comment("Show magazine capacity beside ammo count")
            .define("showMagazine", true);

    public static final ModConfigSpec.BooleanValue SHOW_RESERVE = BUILDER
            .comment("Show reserve ammo count from inventory")
            .define("showReserve", true);

    public static final ModConfigSpec.DoubleValue LOADED_SCALE = BUILDER
            .comment("Scale of the loaded ammo digits")
            .defineInRange("loadedScale", 2.0, 0.25, 8.0);

    public static final ModConfigSpec.IntValue ICON_SIZE = BUILDER
            .comment("Bullet icon size in pixels")
            .defineInRange("iconSize", 16, 4, 64);

    public static final ModConfigSpec.BooleanValue TEXT_SHADOW = BUILDER
            .comment("Whether to draw drop shadow on ammo text")
            .define("textShadow", true);

    public static final ModConfigSpec.ConfigValue<String> COLOR_FULL = BUILDER
            .comment("Text color at full magazine")
            .define("colorFull", "#FFFFFF");

    public static final ModConfigSpec.ConfigValue<String> COLOR_LOW = BUILDER
            .comment("Text color at low ammo")
            .define("colorLow", "#FFAA00");

    public static final ModConfigSpec.ConfigValue<String> COLOR_EMPTY = BUILDER
            .comment("Text color when magazine is empty")
            .define("colorEmpty", "#FF5555");

    public static final ModConfigSpec.ConfigValue<String> COLOR_RESERVE = BUILDER
            .comment("Reserve ammo text color")
            .define("colorReserve", "#CCCCCC");

    public static final ModConfigSpec.ConfigValue<String> COLOR_ICON = BUILDER
            .comment("Bullet icon tint")
            .define("colorIcon", "#FFFFFF");

    public static final ModConfigSpec.BooleanValue FLASH_ENABLED = BUILDER
            .comment("Play a ghostly zero flash when the magazine empties")
            .define("flashEnabled", true);

    public static final ModConfigSpec.IntValue FLASH_DURATION_TICKS = BUILDER
            .comment("Empty-magazine flash duration in ticks")
            .defineInRange("flashDurationTicks", 30, 1, 100);

    public static final ModConfigSpec.DoubleValue FLASH_END_SCALE = BUILDER
            .comment("Flash scale multiplier")
            .defineInRange("flashScale", 6, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue FLASH_TRAVEL = BUILDER
            .comment("Percent of how far the flash drifts toward screen center (1.0 = reach center)")
            .defineInRange("flashTravel", 0.4, 0.0, 1.0);

    public static final ModConfigSpec.ConfigValue<String> COLOR_FLASH = BUILDER
            .comment("Empty-magazine flash color (hex, e.g. #FF5555)")
            .define("colorFlash", "#FF5555");

    public static final ModConfigSpec SPEC = BUILDER.pop().build();

    public enum Anchor {
        BOTTOM_RIGHT,
        BOTTOM_LEFT,
        TOP_RIGHT,
        TOP_LEFT,
        HOTBAR;

        public boolean isRight() {
            return this == BOTTOM_RIGHT || this == TOP_RIGHT;
        }

        public boolean isLeft() {
            return this == BOTTOM_LEFT || this == TOP_LEFT;
        }

        public boolean isCenter() {
            return this == HOTBAR;
        }
    }

    public static int parseHexColor(String hex, int fallback) {
        if (hex == null || hex.isBlank()) {
            return fallback;
        }
        hex = hex.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        } else if (hex.startsWith("0x")) {
            hex = hex.substring(2);
        }
        try {
            int rgb = Integer.parseInt(hex, 16);
            return FastColor.ARGB32.color(255, (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        } catch (NumberFormatException ignored) {
        }
        return fallback;
    }

    public static int colorFull() {
        return parseHexColor(COLOR_FULL.get(), 0xFFFFFF);
    }

    public static int colorLow() {
        return parseHexColor(COLOR_LOW.get(), 0xFFAA00);
    }

    public static int colorEmpty() {
        return parseHexColor(COLOR_EMPTY.get(), 0xFF5555);
    }

    public static int colorReserve() {
        return parseHexColor(COLOR_RESERVE.get(), 0xCCCCCC);
    }

    public static int colorIcon() {
        return parseHexColor(COLOR_ICON.get(), 0xFFFFFF);
    }

    public static int colorFlash() {
        return parseHexColor(COLOR_FLASH.get(), 0xFF5555);
    }
}

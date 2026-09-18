package io.redspace.irons_artifice.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class Keybinds {

    public static final KeyMapping OPEN_MODIFIER_MENU = new KeyMapping(
            "key.irons_artifice.open_modifier_menu",
            GLFW.GLFW_KEY_G,
            KeyMapping.CATEGORY_GAMEPLAY
    );

    public static final KeyMapping RELOAD = new KeyMapping(
            "key.irons_artifice.reload",
            GLFW.GLFW_KEY_R,
            KeyMapping.CATEGORY_GAMEPLAY
    );

    private Keybinds() {}
}

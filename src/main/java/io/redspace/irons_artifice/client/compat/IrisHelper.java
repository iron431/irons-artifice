package io.redspace.irons_artifice.client.compat;

import io.redspace.irons_artifice.IronsArtifice;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class IrisHelper {
    private static @Nullable Object irisApi;
    private static @Nullable Method isRenderingShadowPass;

    static {
        // lazy way to use reflection instead of a dedicated compat handler
        // technically safer because it doesn't brick on api change
        if (ModList.get().isLoaded("iris")) {
            try {
                Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                irisApi = apiClass.getMethod("getInstance").invoke(null);
                isRenderingShadowPass = apiClass.getMethod("isRenderingShadowPass");
            } catch (ReflectiveOperationException e) {
                IronsArtifice.LOGGER.warn("Iris is loaded but its API could not be found, shadow pass detection is off", e);
            }
        }
    }

    private IrisHelper() {
    }

    public static boolean isRenderingShadowPass() {
        if (isRenderingShadowPass == null) {
            return false;
        }
        try {
            return (boolean) isRenderingShadowPass.invoke(irisApi);
        } catch (ReflectiveOperationException | ClassCastException e) {
            isRenderingShadowPass = null;
            IronsArtifice.LOGGER.warn("Iris shadow pass check failed, turning it off", e);
            return false;
        }
    }
}

package io.redspace.irons_artifice.client.compat;

import io.redspace.irons_artifice.IronsArtifice;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class IrisHelper {
    private static @Nullable Object irisApi;
    private static @Nullable Method isRenderingShadowPass;

    // 1.20.1 iris vs oculus api
    private static final String[] API_CLASSES = {"net.irisshaders.iris.api.v0.IrisApi", "net.coderbot.iris.api.v0.IrisApi"};

    static {
        // lazy way to use reflection instead of a dedicated compat handler
        // technically safer because it doesn't brick on api change
        if (ModList.get().isLoaded("oculus") || ModList.get().isLoaded("iris")) {
            try {
                Class<?> apiClass = findApiClass();
                irisApi = apiClass.getMethod("getInstance").invoke(null);
                isRenderingShadowPass = apiClass.getMethod("isRenderingShadowPass");
            } catch (ReflectiveOperationException e) {
                IronsArtifice.LOGGER.warn("Oculus/Iris is loaded but its API could not be found, shadow pass detection is off", e);
            }
        }
    }

    private static Class<?> findApiClass() throws ClassNotFoundException {
        ClassNotFoundException missing = null;
        for (String name : API_CLASSES) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                missing = e;
            }
        }
        throw missing;
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

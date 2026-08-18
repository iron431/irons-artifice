package io.redspace.irons_artifice.data;

import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public record MuzzleFlashSettings(
        Set<MuzzleFlashType> types,
        float muzzleDistanceScalar,
        List<Vector3f> tints
) implements Copyable<MuzzleFlashSettings> {
    public static final Vector3f WHITE = new Vector3f(1f, 1f, 1f);
    public static final Vector3f UNTINTED = new Vector3f(-1f, -1f, -1f);
    public static final Supplier<MuzzleFlashSettings> DEFAULT = ()->of(1.5f, MuzzleFlashType.TRIANGLE, MuzzleFlashType.SMALL_STAR);

    public static MuzzleFlashSettings of(float muzzleDistanceScalar, MuzzleFlashType... types) {
        if (types.length == 0) {
            throw new IllegalArgumentException("Nonzero type count required");
        }
        return new MuzzleFlashSettings(EnumSet.copyOf(List.of(types)), muzzleDistanceScalar, new ArrayList<>());
    }

//    /** Replaces the tint list with a single tint. */
//    public MuzzleFlashSettings withTint(Vector3f tint) {
//        return new MuzzleFlashSettings(types, muzzleDistanceScalar, List.of(new Vector3f(tint)));
//    }

    public void addTint(Vector3f tint) {
        tints.add(tint);
    }

    public void addTint(int tint) {
        addTint(ARGB.vector3fFromRGB24(tint));
    }

    public Vector3f pickTint(RandomSource random) {
        if (tints.isEmpty()) {
            return UNTINTED;
        }
        return tints.get(random.nextInt(tints.size()));
    }

    public MuzzleFlashType pick(RandomSource random) {
        if (types.isEmpty()) {
            throw new IllegalStateException("MuzzleFlashSettings has no types to pick from");
        }
        return types.stream().skip(random.nextInt(types.size())).findFirst().orElseThrow();
    }

    @Override
    public MuzzleFlashSettings copy() {
        return new MuzzleFlashSettings(
                types.isEmpty() ? EnumSet.noneOf(MuzzleFlashType.class) : EnumSet.copyOf(types),
                muzzleDistanceScalar,
                new ArrayList<>(tints)
        );
    }
}

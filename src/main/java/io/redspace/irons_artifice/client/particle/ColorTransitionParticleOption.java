package io.redspace.irons_artifice.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.irons_artifice.registry.ParticleRegistry;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector3f;

public class ColorTransitionParticleOption implements ParticleOptions {
    public static MapCodec<ColorTransitionParticleOption> codec(ParticleType<ColorTransitionParticleOption> type) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.INT.fieldOf("from_color").forGetter(o -> o.fromColor),
                Codec.INT.fieldOf("to_color").forGetter(o -> o.toColor),
                Codec.FLOAT.fieldOf("from_intensity").forGetter(o -> o.fromIntensity),
                Codec.FLOAT.fieldOf("to_intensity").forGetter(o -> o.toIntensity),
                Codec.FLOAT.fieldOf("from_alpha").forGetter(o -> o.fromAlpha),
                Codec.FLOAT.fieldOf("to_alpha").forGetter(o -> o.toAlpha),
                Codec.FLOAT.fieldOf("from_scale").forGetter(o -> o.fromScale),
                Codec.FLOAT.fieldOf("to_scale").forGetter(o -> o.toScale),
                Codec.FLOAT.fieldOf("offset").forGetter(o -> o.offset)
        ).apply(builder, (fromColor, toColor, fromIntensity, toIntensity, fromAlpha, toAlpha, fromScale, toScale, offset) ->
                new ColorTransitionParticleOption(type, fromColor, toColor, fromIntensity, toIntensity, fromAlpha, toAlpha, fromScale, toScale, offset)));
    }

    public static StreamCodec<? super RegistryFriendlyByteBuf, ColorTransitionParticleOption> streamCodec(ParticleType<ColorTransitionParticleOption> type) {
        // 9 fields exceeds StreamCodec.composite's 6-field limit, so encode/decode manually.
        return StreamCodec.of(
                (buf, value) -> {
                    buf.writeInt(value.fromColor);
                    buf.writeInt(value.toColor);
                    buf.writeFloat(value.fromIntensity);
                    buf.writeFloat(value.toIntensity);
                    buf.writeFloat(value.fromAlpha);
                    buf.writeFloat(value.toAlpha);
                    buf.writeFloat(value.fromScale);
                    buf.writeFloat(value.toScale);
                    buf.writeFloat(value.offset);
                },
                buf -> new ColorTransitionParticleOption(
                        type,
                        buf.readInt(), buf.readInt(),
                        buf.readFloat(), buf.readFloat(),
                        buf.readFloat(), buf.readFloat(),
                        buf.readFloat(), buf.readFloat(),
                        buf.readFloat()
                )
        );
    }

    private final ParticleType<ColorTransitionParticleOption> type;
    private final int fromColor, toColor;
    private final float fromIntensity, toIntensity;
    private final float fromAlpha, toAlpha;
    private final float fromScale, toScale;
    private final float offset;

    public ColorTransitionParticleOption(ParticleType<ColorTransitionParticleOption> type, int fromColor, int toColor, float scale) {
        this(type, fromColor, toColor, 1.0F, 1.0F, 1.0F, 1.0F, scale, scale, 0.0F);
    }

    public ColorTransitionParticleOption(ParticleType<ColorTransitionParticleOption> type, int fromColor, int toColor,
                                          float fromIntensity, float toIntensity,
                                          float fromAlpha, float toAlpha,
                                          float fromScale, float toScale,
                                          float offset) {
        this.type = type;
        this.fromColor = fromColor;
        this.toColor = toColor;
        this.fromIntensity = fromIntensity;
        this.toIntensity = toIntensity;
        this.fromAlpha = fromAlpha;
        this.toAlpha = toAlpha;
        this.fromScale = fromScale;
        this.toScale = toScale;
        this.offset = offset;
    }

    public static ColorTransitionParticleOption bulletTrail(int fromColor, int toColor){
        return new ColorTransitionParticleOption(ParticleRegistry.BULLET_TRAIL.get(), fromColor, toColor,
                1f, 0f, 1f, 1f, 0.5f, 0f, 0
        );
    }

    public Vector3f getFromColor() {
        return Utils.vector3fFromRGB24(this.fromColor);
    }

    public Vector3f getToColor() {
        return Utils.vector3fFromRGB24(this.toColor);
    }
    public int getFromColorPacked() {
        return fromColor;
    }

    public int getToColorPacked() {
        return toColor;
    }

    public float getFromIntensity() {
        return this.fromIntensity;
    }

    public float getToIntensity() {
        return this.toIntensity;
    }

    public float getFromAlpha() {
        return this.fromAlpha;
    }

    public float getToAlpha() {
        return this.toAlpha;
    }

    public float getFromScale() {
        return this.fromScale;
    }

    public float getToScale() {
        return this.toScale;
    }

    public float getOffset() {
        return this.offset;
    }

    @Override
    public ParticleType<ColorTransitionParticleOption> getType() {
        return this.type;
    }
}

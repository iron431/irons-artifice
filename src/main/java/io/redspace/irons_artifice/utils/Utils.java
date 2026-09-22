package io.redspace.irons_artifice.utils;

import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.ValueModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;

public class Utils {
    public static final DecimalFormat DECIMAL_FORMAT = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT;

    public static Vec3 reflect(Vec3 direction, Vec3 normal) {
        return direction.subtract(normal.scale(2 * normal.dot(direction)));
    }

    public static Component formatValueModifierDescription(ValueModifier valueModifier, ComponentType<?> componentType) {
        return formatValueModifierDescription(valueModifier, Component.translatable(String.format("%s.component_type.%s", componentType.getName().getNamespace(), componentType.getName().getPath())));
    }

    public static Component getComponentTranslate(ComponentType<?> componentType) {
        return Component.translatable(String.format("%s.component_type.%s", componentType.getName().getNamespace(), componentType.getName().getPath()));
    }

    public static Component formatValueModifierDescription(ValueModifier valueModifier, Component valueName) {
        double value = valueModifier.amount();
        String identifier = value < 0 ? "minus" : "plus";
        if (valueModifier.operation() != ValueModifier.Operation.ADD) {
            identifier += "_percent";
            value = (1 + value) * 100;
        } else {
            value = Math.abs(value);
        }
        int color;
        if (valueModifier.type() == ValueModifier.Type.NEUTRAL) {
            color = ChatFormatting.YELLOW.getColor();
        } else {
            color = valueModifier.type() == ValueModifier.Type.BENEFICIAL ^ valueModifier.amount() < 0 ?
                    ChatFormatting.GREEN.getColor() : ChatFormatting.RED.getColor();
        }
        return Component.translatable(String.format("irons_artifice.value_modifier.%s", identifier), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value), valueName).withColor(color);
    }

    /**
     * @return Uniformly distributed unit vector pointing within a cone of full range: 2 * degreeRadius
     */
    public static Vec3 directionWithinCone(Vec3 axis, float degreeRadius, RandomSource random) {
        // get orthonormal basis
        Vec3 n = axis.normalize();
        Vec3 vec3 = Math.abs(n.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 u = n.cross(vec3).normalize();
        Vec3 v = n.cross(u).normalize();

        // use dot product manipulation to limit range via cosine of the half angle
        float cosHalf = Mth.cos(degreeRadius * ((float) Math.PI / 180));
        // uniformly distribute distance from axis (1 == perfectly aligned, as per dot product)
        float z = Mth.lerp(random.nextFloat(), cosHalf, 1f);
        // trig identity to get sine from sin^2
        float r = Mth.sqrt(1f - z * z);
        float phi = random.nextFloat() * ((float) (Math.PI * 2));
        float cosPhi = Mth.cos(phi);
        float sinPhi = Mth.sin(phi);
        // apply random directionality
        return n.scale(z).add(u.scale(r * cosPhi)).add(v.scale(r * sinPhi)).normalize();
    }

    public static void spawnParticles(Level level, ParticleOptions particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, boolean force) {
        level.getServer().getPlayerList().getPlayers().forEach(player -> ((ServerLevel) level).sendParticles(player, particle, force, force, x, y, z, count, deltaX, deltaY, deltaZ, speed));
    }

    public static Vec3 randomVec3(double scale) {
        return new Vec3(
                (Math.random() - 0.5) * 2 * scale,
                (Math.random() - 0.5) * 2 * scale,
                (Math.random() - 0.5) * 2 * scale
        );
    }

    public static float triangleInterpolate(float x, float start, float peak, float end) {
        if (x <= start || x >= end) {
            return 0.0f;
        }
        if (x <= peak) {
            return (x - start) / (peak - start);
        } else {
            return (end - x) / (end - peak);
        }
    }

    public static double mapClamped(
            double value,
            double inputMin,
            double inputMax,
            double outputMin,
            double outputMax
    ) {
        return Mth.lerp(
                Mth.clamp((value - inputMin) / (inputMax - inputMin), 0, 1),
                outputMin,
                outputMax
        );
    }

    public static boolean canHarm(@Nullable Entity attacker, @Nullable Entity target) {
        if (attacker == null || target == null) {
            return true;
        }
        if (attacker == target) {
            return false;
        }
        if (!target.isAlive()) {
            return false;
        }
        if (target.isAlliedTo(attacker) || attacker.isAlliedTo(target)) {
            return false;
        }
        if (target instanceof OwnableEntity ownableEntity && !canHarm(attacker, ownableEntity.getOwner())) {
            // warning: recursion. ownership loops can overflow.
            return false;
        }
        if (target instanceof Player player && attacker instanceof Player player1 && !player1.canHarmPlayer(player)) {
            return false;
        }
        return true;
    }

    public static boolean hasLineOfSight(Entity a, Entity b) {
        return a.level().clip(new ClipContext(a.getBoundingBox().getCenter(), b.getBoundingBox().getCenter(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).getType() == HitResult.Type.MISS;
    }
}

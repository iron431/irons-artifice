package io.redspace.irons_artifice.gun;

import io.redspace.irons_artifice.attribute.GunStat;
import io.redspace.irons_artifice.mixin.LivingEntityAccessor;
import io.redspace.irons_artifice.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the gun stats of a single shot.
 * <p>
 * A gun's modifiers live in its main hand attribute modifiers, so the game applies them to whoever holds it, like a sword. That application is
 * server-only and happens on the holder's next tick, which is too late and too one-sided for shots: the client predicts them, and a gun can be
 * fired the moment it is drawn. So a shot never reads the shooter's attributes directly. It works on a private copy that holds everything the
 * shooter has <i>except</i> what their main hand contributes, plus what the fired gun contributes, read straight from the stack.
 */
public final class GunStatResolver {
    private static final String INSTALLED_SUFFIX = "/installed_";
    private static final String HELD_SUFFIX = "/held_";
    private static final Map<Identifier, Identifier[]> INSTALLED_IDS = new ConcurrentHashMap<>();
    private static final Map<Identifier, Identifier[]> HELD_IDS = new ConcurrentHashMap<>();

    private static volatile @Nullable Registered registered;

    /**
     * @param stats    every registered gun stat
     * @param defaults each of them at its default value
     */
    private record Registered(List<Holder<Attribute>> stats, AttributeSupplier defaults) {
    }

    private static Registered registered() {
        Registered result = registered;
        if (result == null) {
            synchronized (GunStatResolver.class) {
                result = registered;
                if (result == null) {
                    List<Holder<Attribute>> stats = AttributeRegistry.gunStats();
                    AttributeSupplier.Builder builder = AttributeSupplier.builder();
                    stats.forEach(builder::add);
                    AttributeSupplier defaults = builder.build();
                    // default instances cache their value on first read, and both logical sides read this supplier
                    stats.forEach(defaults::getValue);
                    result = new Registered(stats, defaults);
                    registered = result;
                }
            }
        }
        return result;
    }

    public static AttributeMap newStats() {
        return new AttributeMap(registered().defaults());
    }

    public static AttributeMap copyOf(AttributeMap stats) {
        AttributeMap copy = newStats();
        copy.assignAllValues(stats);
        return copy;
    }

    public static AttributeMap resolve(@Nullable LivingEntity shooter, ItemStack gun) {
        if (shooter == null) {
            return resolve(null, ItemStack.EMPTY, gun);
        }
        return resolve(shooter.getAttributes(), appliedMainHand(shooter), gun);
    }

    /**
     * @param shooter         attributes of whoever fires, or null to resolve the gun on its own
     * @param appliedMainHand the main hand stack whose modifiers {@code shooter} currently carries
     * @param gun             the gun being fired
     */
    public static AttributeMap resolve(@Nullable AttributeMap shooter, ItemStack appliedMainHand, ItemStack gun) {
        AttributeMap stats = newStats();
        if (shooter != null) {
            copyShooter(shooter, stats);
            stripMainHand(appliedMainHand, gun, stats);
        }
        gun.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.value() instanceof GunStat) {
                setModifier(stats, attribute, modifier);
            }
        });
        return stats;
    }

    /**
     * Adds the modifier, replacing any existing one with the same id
     */
    public static void setModifier(AttributeMap stats, Holder<Attribute> stat, AttributeModifier modifier) {
        AttributeInstance instance = stats.getInstance(stat);
        if (instance != null) {
            // not addOrUpdateTransientModifier: it leaves the old entry behind when the operation differs
            instance.removeModifier(modifier.id());
            instance.addTransientModifier(modifier);
        }
    }

    private static void copyShooter(AttributeMap shooter, AttributeMap stats) {
        // only touches stats the shooter has already instantiated, and instantiates nothing on the shooter
        stats.assignAllValues(shooter);
        for (Holder<Attribute> stat : registered().stats()) {
            // every living entity carries every gun stat, but the map need not belong to one
            if (shooter.hasAttribute(stat)) {
                // an entity type may define its own base without ever instantiating the attribute
                double base = shooter.getBaseValue(stat);
                if (base != stats.getBaseValue(stat)) {
                    AttributeInstance instance = stats.getInstance(stat);
                    if (instance != null) {
                        instance.setBaseValue(base);
                    }
                }
            }
        }
    }

    private static void stripMainHand(ItemStack appliedMainHand, ItemStack gun, AttributeMap stats) {
        // when the fired gun is what the shooter has applied, everything it contributed is about to be replaced id for id anyway.
        // The server's record is a copy, so identity alone would only ever recognise the client's
        if (!appliedMainHand.isEmpty() && appliedMainHand != gun && !ItemStack.matches(appliedMainHand, gun)) {
            appliedMainHand.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
                if (stats.hasModifier(attribute, modifier.id())) {
                    AttributeInstance instance = stats.getInstance(attribute);
                    if (instance != null) {
                        instance.removeModifier(modifier.id());
                    }
                }
            });
        }
        // the client cannot know which stack the server last applied, but anything a gun contributes is recognisable by id.
        // Every instance of this private map is in its update set: instances mark themselves dirty when created, and nothing ever clears the set
        for (AttributeInstance instance : new ArrayList<>(stats.getAttributesToUpdate())) {
            for (AttributeModifier modifier : instance.getModifiers()) {
                if (isGunDerived(modifier.id())) {
                    instance.removeModifier(modifier.id());
                }
            }
        }
    }

    /**
     * The main hand stack whose modifiers the entity currently carries. Equipment changes are applied on the entity's next tick and only on the
     * server, so right after a swap this is still the previous item. The client can only assume its current main hand.
     * <p>
     * On the server this is the game's own record of the applied stack. Do not modify it.
     */
    public static ItemStack appliedMainHand(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return entity.getMainHandItem();
        }
        return ((LivingEntityAccessor) entity).getLastEquipmentItems().getOrDefault(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    /**
     * Id a modifier item's gun stat entry gets once installed. The same item may be installed more than once, and modifiers are unique per id,
     * so each occurrence needs its own.
     * <p>
     * Ids of this form, and {@link GunStat#BASE_ID}, are reserved for the gun in the main hand: on a gun stat they are dropped from the shooter
     * before a shot is resolved, whatever put them there.
     *
     * @param ordinal position among the gun's installed modifiers
     */
    public static Identifier installedId(Identifier entryId, int ordinal) {
        return suffixedId(INSTALLED_IDS, INSTALLED_SUFFIX, entryId, ordinal);
    }

    /**
     * Id for an installed modifier's entry on an attribute that is not a gun stat, which is passed on to whoever holds the gun
     */
    public static Identifier heldId(Identifier entryId, int ordinal) {
        return suffixedId(HELD_IDS, HELD_SUFFIX, entryId, ordinal);
    }

    private static Identifier suffixedId(Map<Identifier, Identifier[]> interned, String suffix, Identifier entryId, int ordinal) {
        // resolved far too often to build, and validate, a new id every time
        Identifier[] ids = interned.get(entryId);
        if (ids == null || ordinal >= ids.length) {
            ids = interned.compute(entryId, (id, existing) -> {
                int have = existing == null ? 0 : existing.length;
                if (ordinal < have) {
                    return existing;
                }
                Identifier[] grown = new Identifier[Math.max(8, ordinal + 1)];
                for (int i = 0; i < grown.length; i++) {
                    grown[i] = i < have ? existing[i] : id.withSuffix(suffix + i);
                }
                return grown;
            });
        }
        return ids[ordinal];
    }

    public static boolean isGunDerived(Identifier id) {
        return id.equals(GunStat.BASE_ID) || isInstalledId(id);
    }

    public static boolean isInstalledId(Identifier id) {
        String path = id.getPath();
        int index = path.lastIndexOf(INSTALLED_SUFFIX);
        if (index < 0 || index + INSTALLED_SUFFIX.length() == path.length()) {
            return false;
        }
        for (int i = index + INSTALLED_SUFFIX.length(); i < path.length(); i++) {
            if (!Character.isDigit(path.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}

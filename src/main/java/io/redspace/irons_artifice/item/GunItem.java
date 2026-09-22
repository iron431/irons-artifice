package io.redspace.irons_artifice.item;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.api.GunAnimations;
import io.redspace.irons_artifice.data.HandOccupancy;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.GunProfile;
import io.redspace.irons_artifice.gun.GunState;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.animation_adjuster.AnimationAdjuster;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class GunItem extends BaseGeoItem {
    public static final DataTicket<MagazineContents> MAGAZINE_ANIMATION_TICKET = new DataTicket<>(IronsArtifice.id("magazine_state").toString(), MagazineContents.class);
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final DataTicket<List<AnimationAdjuster>> ANIMATION_ADJUSTERS_TICKET = new DataTicket<>(IronsArtifice.id("animation_adjusters").toString(), (Class) List.class);
    public static final DataTicket<AttachmentMap> ATTACHMENTS = new DataTicket<>(IronsArtifice.id("attachments").toString(), AttachmentMap.class);
    public static final DataTicket<Double> RELOAD_PROGRESS_SECONDS_TICKET = new DataTicket<>(IronsArtifice.id("reload_progress_seconds").toString(), Double.class);
    public static final DataTicket<Float> RELOAD_PERCENT_TICKET = new DataTicket<>(IronsArtifice.id("reload_percent").toString(), Float.class);
    public static final DataTicket<Float> MUZZLE_OFFSET_TICKET = new DataTicket<>(IronsArtifice.id("muzzle_offset").toString(), Float.class);
    public static final DataTicket<HandOccupancy> HAND_OCCUPANCY_TICKET = new DataTicket<>(IronsArtifice.id("hand_occupancy").toString(), HandOccupancy.class);
    public static final DataTicket<Integer> ITEM_OWNER_ID_TICKET = new DataTicket<>(IronsArtifice.id("item_owner_id").toString(), Integer.class);
    public static final String TRIGGERED_ANIMATION_CONTROLLER = GunAnimations.CONTROLLER_ACTIONS;
    public static final String IDLE_ANIMATION_CONTROLLER = GunAnimations.CONTROLLER_IDLE;

    private final GunProfile gunProfile;

    public GunItem(Properties properties, GunProfile gunProfile) {
        super(properties
                .stacksTo(1)
                .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                .component(DataComponentRegistry.MAGAZINE.get(), new MagazineContents(gunProfile.magazineCapacity()))
        );
        this.gunProfile = gunProfile;
    }

    public static final int SCOPE_USE_DURATION = 1200;

    public static boolean hasGunSpyglass(ItemStack stack) {
        return stack.has(DataComponentRegistry.GUN_SPYGLASS.get());
    }

    public static boolean isScoping(LivingEntity entity) {
        return entity.isUsingItem() && hasGunSpyglass(entity.getUseItem());
    }

    public static boolean isChargingBayonet(Entity entity) {
        return entity instanceof LivingEntity living && living.isUsingItem() && living.getUseItem().has(DataComponentRegistry.BAYONET.get());
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (GunItem.isReloading(stack)) {
            return InteractionResult.FAIL;
        }
        if (hasGunSpyglass(stack)) {
            player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        if (stack.has(DataComponentRegistry.BAYONET.get())) {
            player.playSound(SoundEvents.TRIDENT_THROW, 0.4F, 1.2F);
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        return super.use(level, player, hand);
    }

    @Override
    public @NonNull UseAnim getUseAnimation(@NonNull ItemStack stack) {
        if (hasGunSpyglass(stack)) {
            return UseAnim.SPYGLASS;
        }
        if (stack.has(DataComponentRegistry.BAYONET.get())) {
            return UseAnim.SPEAR;
        }
        return super.getUseAnimation(stack);
    }

    @Override
    public int getUseDuration(@NonNull ItemStack stack, @NonNull LivingEntity user) {
        if (hasGunSpyglass(stack)) {
            return SCOPE_USE_DURATION;
        }
        if (stack.has(DataComponentRegistry.BAYONET.get())) {
            return 72000;
        }
        return super.getUseDuration(stack, user);
    }

    @Override
    public @NonNull ItemStack finishUsingItem(@NonNull ItemStack stack, @NonNull Level level, @NonNull LivingEntity entity) {
        if (hasGunSpyglass(stack)) {
            entity.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
            return stack;
        }
        if (stack.has(DataComponentRegistry.BAYONET.get())) {
            return stack;
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public boolean releaseUsing(@NonNull ItemStack stack, @NonNull Level level, @NonNull LivingEntity entity, int remainingTime) {
        if (hasGunSpyglass(stack)) {
            entity.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
            return true;
        }
        if (stack.has(DataComponentRegistry.BAYONET.get())) {
            return true;
        }
        return super.releaseUsing(stack, level, entity, remainingTime);
    }

    public GunProfile getGun() {
        return gunProfile;
    }

    public int magazineCapacity() {
        return gunProfile.magazineCapacity();
    }

    public static @Nullable HandOccupancy currentOccupancy(LivingEntity entity, InteractionHand hand) {
        return currentOccupancy(entity, entity.getItemInHand(hand));
    }

    public static @Nullable HandOccupancy currentOccupancy(LivingEntity entity, ItemStack stack) {
        if (!(stack.getItem() instanceof GunItem gun)) {
            return null;
        }
        HandOccupancy occupancy;
        if (isReloading(stack)) {
            occupancy = gun.getGun().occupancyFor(GunState.RELOAD);
        } else if (FireDelayState.isActive(entity, stack)) {
            occupancy = gun.getGun().occupancyFor(GunState.FIRE);
        } else {
            occupancy = gun.getGun().defaultOccupancy();
        }
        if (occupancy == HandOccupancy.BOTH && stack == entity.getOffhandItem() && !entity.getMainHandItem().isEmpty()) {
            return HandOccupancy.MAINHAND;
        }
        return occupancy;
    }

    public static boolean isOffhandItemUseBlocked(LivingEntity entity) {
        return currentOccupancy(entity, InteractionHand.MAIN_HAND) == HandOccupancy.BOTH;
    }

    public static MagazineContents getMagazine(ItemStack stack) {
        MagazineContents magazine = MagazineContents.get(stack);
        return magazine != null ? magazine : MagazineContents.EMPTY;
    }

    public static void setMagazine(ItemStack stack, MagazineContents magazine) {
        MagazineContents.set(stack, magazine);
    }

    public static boolean isReloading(ItemStack stack) {
        return ReloadState.has(stack);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(@NonNull ItemStack itemStack, @NonNull TooltipContext context, @NonNull List<Component> tooltip, @NonNull TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, tooltip, tooltipFlag);
        Consumer<Component> statBuilder = (component) -> tooltip.add(Component.literal(" ").append(component).withStyle(ChatFormatting.DARK_GREEN));
        Function<String, Component> highlightText = s -> Component.literal(s).withStyle(ChatFormatting.GREEN);
        ShotProfile shotProfile = GunplayManager.compose(context.player(), this.gunProfile, itemStack);
        String damage = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(shotProfile.value(ShotComponents.DAMAGE));
        int bulletCount = (int) shotProfile.value(ShotComponents.PROJECTILE_COUNT);
        int bulletSpeedPercent = (int) (100 * shotProfile.value(ShotComponents.BULLET_SPEED) / Bullet.BASE_SPEED);
        String fireRate = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(20.0 / shotProfile.fireDelayTicks());
        String reloadTime = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(gunProfile.reloadTimeTicks() / 20f / shotProfile.value(ShotComponents.RELOAD_SPEED_MULTIPLIER));
        if (bulletCount > 1) {
            statBuilder.accept(Component.translatable("irons_artifice.tooltip.damage_per_bullet", highlightText.apply(damage), Component.literal(String.valueOf(bulletCount)).withStyle(ChatFormatting.YELLOW)));
            statBuilder.accept(Component.translatable("irons_artifice.tooltip.bullet_count", bulletCount).withStyle(ChatFormatting.YELLOW));
        } else {
            statBuilder.accept(Component.translatable("irons_artifice.tooltip.damage", highlightText.apply(damage)));
        }
        if (bulletSpeedPercent != 100 || Bullet.BASE_SPEED != shotProfile.peek(ShotComponents.BULLET_SPEED).base()) {
            statBuilder.accept(Component.translatable("irons_artifice.tooltip.bullet_speed_percent", highlightText.apply(bulletSpeedPercent + "%")));
        }
        if (gunProfile.magazineCapacity() > 1) {
            // hide fire rate on single shot guns
            statBuilder.accept(Component.translatable("irons_artifice.tooltip.fire_rate", highlightText.apply(fireRate)));
        }
        statBuilder.accept(Component.translatable("irons_artifice.tooltip.reload_time", highlightText.apply(reloadTime + "s")));
        statBuilder.accept(Component.translatable("irons_artifice.tooltip.ammo_capacity", highlightText.apply("" + gunProfile.magazineCapacity())));
        tooltip.add(Component.translatable("irons_artifice.tooltip.modifier_count",
                        gunProfile.modifierSlots()
                ).withStyle(ChatFormatting.GOLD)
                .append(" ").append(Component.translatable("irons_artifice.tooltip.keybind_hint",
                                Component.keybind("key.irons_artifice.open_modifier_menu")
                                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withItalic(false)))
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(true))));
        GunContainer container = new GunContainer(itemStack);
        for (var item : container.getItems()) {
            if (!item.isEmpty()) {
                tooltip.add(Component.literal(" * ").withStyle(ChatFormatting.DARK_GRAY).append(item.getHoverName().copy().withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    public static void playReloadFeedback(Level level, Player player, ReloadResult result) {
        switch (result) {
            case NO_AMMO -> level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.6F, 1.0F);
            case ALREADY_FULL -> level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.NOTE_BLOCK_DIDGERIDOO, SoundSource.PLAYERS, 0.6F, 1.0F);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (isReloading(stack)) {
            return (int) (ReloadState.get(stack).percent(0) * 13);
        } else {
            int count = getMagazine(stack).count();
            return Mth.clamp(Math.round(count * 13.0F / magazineCapacity()), 0, 13);
        }
    }

    @Override
    public int getBarColor(ItemStack stack) {
        if (isReloading(stack)) {
            return 0xAAAAAA;
        } else {
            // hell yeah
            return 0xFFAA00;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        super.registerControllers(controllers);
        controllers.add(new AnimationController<>(this, IDLE_ANIMATION_CONTROLLER, this::gunIdleHandler));
        controllers.add(new OffsetableAnimationController<>(this, GunAnimations.CONTROLLER_ACTIONS, test -> PlayState.STOP)
                .triggerableAnim(GunAnimations.FIRE, RawAnimation.begin().thenPlay(GunAnimations.FIRE))
                .triggerableAnim(GunAnimations.RELOAD, RawAnimation.begin().thenPlay(GunAnimations.RELOAD))
                .triggerableAnim(GunAnimations.EQUIP, RawAnimation.begin().thenPlay(GunAnimations.EQUIP))
        );
    }

    private PlayState gunIdleHandler(AnimationState<GunItem> animationState) {
        animationState.setAnimation(RawAnimation.begin().thenPlayAndHold(GunAnimations.IDLE));
        return PlayState.CONTINUE;
    }

    public void configureActionTimeline(long instanceId, double offsetSeconds, double skipAtSeconds, double skipToSeconds) {
        AnimationController<?> controller = getAnimatableInstanceCache().getManagerForId(instanceId)
                .getAnimationControllers().get(TRIGGERED_ANIMATION_CONTROLLER);
        if (controller instanceof OffsetableAnimationController<?> skippable) {
            skippable.configureTimeline(offsetSeconds, skipAtSeconds, skipToSeconds);
        }
    }

    private static class OffsetableAnimationController<T extends GeoAnimatable> extends AnimationController<T> {
        private double offsetTicks;
        private double skipAtTicks;
        private double skipToTicks;
        private boolean skipped;

        public OffsetableAnimationController(T animatable, String name, AnimationController.AnimationStateHandler<T> stateHandler) {
            super(animatable, name, 0, stateHandler);
        }

        public void configureTimeline(double offsetSeconds, double skipAtSeconds, double skipToSeconds) {
            this.offsetTicks = offsetSeconds * 20;
            this.skipAtTicks = skipAtSeconds * 20;
            this.skipToTicks = skipToSeconds * 20;
            this.skipped = false;
        }

        @Override
        protected double adjustTick(double tick) {
            double adjusted = super.adjustTick(tick);
            if (offsetTicks > 0) {
                adjusted += offsetTicks;
            }
            if (!skipped && skipToTicks > skipAtTicks && adjusted >= skipAtTicks && adjusted < skipToTicks) {
                adjusted = skipToTicks;
                skipped = true;
            }
            return adjusted;
        }
    }
}

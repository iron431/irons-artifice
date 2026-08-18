package io.redspace.irons_artifice.item;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.data.HandOccupancy;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.GunProfile;
import io.redspace.irons_artifice.gun.ShotProfile;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class GunItem extends BaseGeoItem {
    public static final DataTicket<MagazineContents> MAGAZINE_ANIMATION_TICKET = DataTicket.create(IronsArtifice.id("magazine_state").toString(), MagazineContents.class);
    public static final DataTicket<AnimationAdjuster> ANIMATION_ADJUSTER_TICKET = DataTicket.create(IronsArtifice.id("animation_adjuster").toString(), AnimationAdjuster.class);
    public static final DataTicket<AttachmentMap> ATTACHMENTS = DataTicket.create(IronsArtifice.id("attachments").toString(), AttachmentMap.class);
    public static final DataTicket<Double> RELOAD_PROGRESS_SECONDS_TICKET = DataTicket.create(IronsArtifice.id("reload_progress_seconds").toString(), Double.class);
    public static final DataTicket<HandOccupancy> HAND_OCCUPANCY_TICKET = DataTicket.create(IronsArtifice.id("hand_occupancy").toString(), HandOccupancy.class);
    public static final String TRIGGERED_ANIMATION_CONTROLLER = "Actions";
    public static final String IDLE_ANIMATION_CONTROLLER = "gun_animation_controller";

    private final GunProfile gunProfile;

    public GunItem(Properties properties, GunProfile gunProfile) {
        super(properties
                .stacksTo(1)
                .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                .component(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.CONTAINER, true))
                .component(DataComponentRegistry.MAGAZINE, new MagazineContents(gunProfile.magazineCapacity()))
        );
        this.gunProfile = gunProfile;
    }

    public static final int SCOPE_USE_DURATION = 1200;

    public static boolean hasGunSpyglass(ItemInstance stack) {
        return stack.has(DataComponentRegistry.GUN_SPYGLASS);
    }

    public static boolean isScoping(LivingEntity entity) {
        return entity.isUsingItem() && hasGunSpyglass(entity.getUseItem());
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!hasGunSpyglass(stack)) {
            return super.use(level, player, hand);
        }
        player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public int getUseDuration(@NonNull ItemStack stack, @NonNull LivingEntity user) {
        return hasGunSpyglass(stack) ? SCOPE_USE_DURATION : super.getUseDuration(stack, user);
    }

    @Override
    public @NonNull ItemStack finishUsingItem(@NonNull ItemStack stack, @NonNull Level level, @NonNull LivingEntity entity) {
        if (hasGunSpyglass(stack)) {
            entity.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
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
        return super.releaseUsing(stack, level, entity, remainingTime);
    }

    public GunProfile getGun() {
        return gunProfile;
    }

    public int magazineCapacity() {
        return gunProfile.magazineCapacity();
    }

    public HandOccupancy occupancyForCurrentAnimation(ItemStack stack) {
        return gunProfile.occupancyForAnimation(currentAnimation(stack));
    }

    public static @Nullable HandOccupancy currentOccupancy(ItemStack stack) {
        if (!(stack.getItem() instanceof GunItem gun)) {
            return null;
        }
        return gun.occupancyForCurrentAnimation(stack);
    }

    public static @Nullable HandOccupancy currentOccupancy(LivingEntity entity, InteractionHand hand) {
        return currentOccupancy(entity, entity.getItemInHand(hand));
    }

    public static @Nullable HandOccupancy currentOccupancy(LivingEntity entity, ItemStack stack) {
        HandOccupancy occupancy = currentOccupancy(stack);
        if (occupancy == HandOccupancy.BOTH && stack == entity.getOffhandItem() && !entity.getMainHandItem().isEmpty()) {
            return HandOccupancy.MAINHAND;
        }
        return occupancy;
    }

    private String currentAnimation(ItemStack stack) {
        var controller = getAnimatableInstanceCache().getManagerForId(GeoItem.getId(stack))
                .getAnimationControllers().get(TRIGGERED_ANIMATION_CONTROLLER);
        if (controller != null) {
            if (controller.isTriggeredAnimation("reload")) {
                return "reload";
            }
            if (controller.isTriggeredAnimation("fire")) {
                return "fire";
            }
            if (controller.isTriggeredAnimation("equip")) {
                return "equip";
            }
        }
        return "idle";
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
    public void appendHoverText(@NonNull ItemStack itemStack, @NonNull TooltipContext context, @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder, @NonNull TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
        Consumer<Component> statBuilder = (component) -> builder.accept(Component.literal(" ").append(component).withStyle(ChatFormatting.DARK_GREEN));
        Function<String, Component> highlightText = s -> Component.literal(s).withStyle(ChatFormatting.GREEN);
        ShotProfile shotProfile = GunplayManager.compose(context.player(), this.gunProfile, itemStack);
        String damage = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(shotProfile.value(ShotComponents.DAMAGE));
        int bulletCount = (int) shotProfile.value(ShotComponents.PROJECTILE_COUNT);
        int bulletSpeedPercent = (int) (100 * shotProfile.value(ShotComponents.BULLET_SPEED) / Bullet.BASE_SPEED);
        String fireRate = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(20 / shotProfile.fireDelayTicks());
        String reloadTime = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(gunProfile.reloadTimeTicks() / 20f / shotProfile.value(ShotComponents.RELOAD_SPEED_MULTIPLIER));
        if (bulletCount > 1) {
            statBuilder.accept(Component.translatable("irons_artifice.tooltip.damage_per_bullet", highlightText.apply(damage), Component.literal(String.valueOf(bulletCount)).withStyle(ChatFormatting.YELLOW)));
            statBuilder.accept(Component.translatable("irons_artifice.tooltip.bullet_count", bulletCount).withStyle(ChatFormatting.YELLOW));
        } else {
            statBuilder.accept(Component.translatable("irons_artifice.tooltip.damage", highlightText.apply(damage)));
        }
        if (bulletSpeedPercent != 100 || Bullet.BASE_SPEED != shotProfile.get(ShotComponents.BULLET_SPEED).base()) {
            statBuilder.accept(Component.translatable("irons_artifice.tooltip.bullet_speed_percent", highlightText.apply(bulletSpeedPercent + "%")));
        }
        if (gunProfile.magazineCapacity() > 1) {
            // hide fire rate on single shot guns
            statBuilder.accept(Component.translatable("irons_artifice.tooltip.fire_rate", highlightText.apply(fireRate)));
        }
        statBuilder.accept(Component.translatable("irons_artifice.tooltip.reload_time", highlightText.apply(reloadTime + "s")));
        statBuilder.accept(Component.translatable("irons_artifice.tooltip.ammo_capacity", highlightText.apply("" + gunProfile.magazineCapacity())));
        builder.accept(Component.translatable("irons_artifice.tooltip.modifier_count",
                        gunProfile.modifierSlots()
                ).withStyle(ChatFormatting.GOLD)
                .append(" ").append(Component.translatable("irons_artifice.tooltip.keybind_hint",
                        Component.keybind("key.irons_artifice.open_modifier_menu")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withItalic(false)))
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(true))));
        GunContainer container = new GunContainer(itemStack);
        for (var item : container.getItems()) {
            if (!item.isEmpty()) {
                builder.accept(Component.literal(" * ").withStyle(ChatFormatting.DARK_GRAY).append(item.getHoverName().copy().withStyle(ChatFormatting.GRAY)));
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
    public void registerControllers(AnimatableManager.@NonNull ControllerRegistrar controllers) {
        super.registerControllers(controllers);
        controllers.add(new AnimationController<>(IDLE_ANIMATION_CONTROLLER, this::gunIdleHandler));
        controllers.add(new OffsetableAnimationController<>("Actions", test -> PlayState.STOP)
                .triggerableAnim("fire", RawAnimation.begin().thenPlay("fire"))
                .triggerableAnim("reload", RawAnimation.begin().thenPlay("reload"))
                .triggerableAnim("equip", RawAnimation.begin().thenPlay("equip"))
        );
    }

    private PlayState gunIdleHandler(AnimationTest<GunItem> animationTest) {
        animationTest.setAnimation(RawAnimation.begin().thenPlayAndHold("idle"));
        return PlayState.CONTINUE;
    }

    public void configureActionTimelineSkip(long instanceId, double skipAtSeconds, double skipToSeconds) {
        AnimationController<?> controller = getAnimatableInstanceCache().getManagerForId(instanceId)
                .getAnimationControllers().get(TRIGGERED_ANIMATION_CONTROLLER);
        if (controller instanceof OffsetableAnimationController<?> skippable) {
            skippable.setTimelineSkip(skipAtSeconds, skipToSeconds);
        }
    }

    private static class OffsetableAnimationController<T extends GeoAnimatable> extends AnimationController<T> {
        private double skipAtSeconds;
        private double skipToSeconds;
        private boolean skipped;

        public OffsetableAnimationController(String name, AnimationStateHandler<T> stateHandler) {
            super(name, stateHandler);
        }

        public void setTimelineSkip(double skipAtSeconds, double skipToSeconds) {
            this.skipAtSeconds = skipAtSeconds;
            this.skipToSeconds = skipToSeconds;
            this.skipped = false;
        }

        private boolean applyTimelineSkip() {
            if (skipped || skipToSeconds <= skipAtSeconds || timelineTime < skipAtSeconds || timelineTime >= skipToSeconds) {
                return false;
            }
            timelineTime = skipToSeconds;
            skipped = true;
            return true;
        }

        @Override
        protected void initializeNewAnimation(T animatable, GeoRenderState renderState, GeoModel<T> geoModel, double prevAnimSpeed, int prevTransitionTicks) {
            double offset = timelineTime;
            super.initializeNewAnimation(animatable, renderState, geoModel, prevAnimSpeed, prevTransitionTicks);
            if (offset > 0) {
                timelineTime = offset;
            }
            boolean skippedNow = applyTimelineSkip();
            if (this.timeline != null && (offset > 0 || skippedNow)) {
                this.animationPoint = this.timeline.createAnimationPoint(this.timelineTime, this.animationPoint, this.easingOverride);
            }
        }

        @Override
        protected void progressExistingAnimation(T animatable, GeoRenderState renderState, double prevTimelineTime, double timeAdvanced) {
            if (applyTimelineSkip()) {
                prevTimelineTime = timelineTime;
            }
            super.progressExistingAnimation(animatable, renderState, prevTimelineTime, timeAdvanced);
        }

    }
}

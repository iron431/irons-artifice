package io.redspace.irons_artifice.item;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
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
import io.redspace.irons_artifice.item.kinetic.KineticWeapon;
import io.redspace.irons_artifice.item.kinetic.KineticWeaponHandler;
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
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class GunItem extends BaseGeoItem {
    // Only the values the render-time animation adjusters read travel on a DataTicket; the attachments, the holder
    // and its hand occupancy are read straight off the rendered stack, which is all the item renderer is handed.
    public static final DataTicket<MagazineContents> MAGAZINE_ANIMATION_TICKET = new DataTicket<>(IronsArtifice.id("magazine_state").toString(), MagazineContents.class);
    public static final DataTicket<Double> RELOAD_PROGRESS_SECONDS_TICKET = new DataTicket<>(IronsArtifice.id("reload_progress_seconds").toString(), Double.class);
    public static final DataTicket<Float> RELOAD_PERCENT_TICKET = new DataTicket<>(IronsArtifice.id("reload_percent").toString(), Float.class);
    public static final DataTicket<Float> MUZZLE_OFFSET_TICKET = new DataTicket<>(IronsArtifice.id("muzzle_offset").toString(), Float.class);
    public static final String TRIGGERED_ANIMATION_CONTROLLER = GunAnimations.CONTROLLER_ACTIONS;
    public static final String IDLE_ANIMATION_CONTROLLER = GunAnimations.CONTROLLER_IDLE;

    private final GunProfile gunProfile;

    public GunItem(Properties properties, GunProfile gunProfile) {
        super(properties
                .stacksTo(1)
                .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                .component(DataComponentRegistry.MAGAZINE, new MagazineContents(gunProfile.magazineCapacity()))
        );
        this.gunProfile = gunProfile;
    }

    public static final int SCOPE_USE_DURATION = 1200;

    /**
     * The same 72000 ticks vanilla's {@code Item.getUseDuration} returns for a kinetic stack once the
     * component is vanilla's own. The charge ends on its conditions running out or on letting go, never
     * on the duration elapsing -- and {@link KineticWeaponHandler} counts ticks up from it, so the value
     * is load-bearing beyond keeping the use alive.
     */
    public static final int KINETIC_USE_DURATION = 72000;

    public static boolean hasGunSpyglass(ItemStack stack) {
        return stack.has(DataComponentRegistry.GUN_SPYGLASS);
    }

    public static boolean isScoping(LivingEntity entity) {
        return entity.isUsingItem() && hasGunSpyglass(entity.getUseItem());
    }

    public static boolean isChargingBayonet(Entity entity) {
        return entity instanceof LivingEntity living && living.isUsingItem() && KineticWeapon.has(living.getUseItem());
    }

    /**
     * Whether this stack's arm pose is the mod's own rather than vanilla's. A charging kinetic stack
     * declares {@link UseAnim#SPEAR}, which 1.21.1 turns into {@code ArmPose.THROW_SPEAR} ahead of
     * NeoForge's item extension; {@code PlayerRendererMixin} asks this before hoisting the extension.
     */
    public static boolean posedAsKineticWeapon(ItemStack stack) {
        return stack.getItem() instanceof GunItem && KineticWeapon.has(stack);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (GunItem.isReloading(stack)) {
            return InteractionResultHolder.fail(stack);
        }
        if (hasGunSpyglass(stack)) {
            player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        // Vanilla starts the charge from Item#use once the component is its own; here the item does it,
        // and only once the gun has finished cycling its action.
        KineticWeapon kineticWeapon = KineticWeapon.get(stack);
        if (kineticWeapon != null && !FireDelayState.isActive(player, stack)) {
            player.startUsingItem(hand);
            // Server-authoritative, with a null excluded listener so the charging player hears it too:
            // Level.playSound's Player argument is the listener to EXCLUDE, and ServerLevel skips it
            // where ClientLevel plays only for it. Item.use runs on both sides for a right click, so the
            // guard is what stops the sound doubling.
            if (!level.isClientSide()) {
                kineticWeapon.sound().ifPresent(sound -> level.playSound(
                        null, player.getX(), player.getY(), player.getZ(),
                        sound, player.getSoundSource(), 1.0F, 1.0F));
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity user) {
        if (hasGunSpyglass(stack)) {
            return SCOPE_USE_DURATION;
        }
        return KineticWeapon.has(stack) ? KINETIC_USE_DURATION : super.getUseDuration(stack, user);
    }

    /**
     * {@link UseAnim#SPEAR} is what vanilla answers for a kinetic stack, so keeping the value keeps the
     * first-person timing. {@code ItemInHandRendererMixin} replaces the trident pose 1.21.1 would
     * otherwise draw for it, and {@code PlayerRendererMixin} the third-person one.
     */
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return KineticWeapon.has(stack) ? UseAnim.SPEAR : super.getUseAnimation(stack);
    }

    /**
     * Vanilla intercepts this a level higher, in {@code ItemStack.onUseTick}; at 1.21.1 that method only
     * forwards here, so the interception happens here. The return matters: falling through to super
     * would run the item's ordinary use tick on top of the charge.
     */
    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide() && KineticWeapon.has(stack)) {
            KineticWeaponHandler.tickCharge(stack, entity, remainingUseDuration,
                    entity.getUsedItemHand() == InteractionHand.OFF_HAND
                            ? EquipmentSlot.OFFHAND
                            : EquipmentSlot.MAINHAND);
            return;
        }
        super.onUseTick(level, entity, stack, remainingUseDuration);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (hasGunSpyglass(stack)) {
            entity.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
            return stack;
        }
        if (KineticWeapon.has(stack)) {
            KineticWeaponHandler.endCharge(entity);
            return stack;
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int remainingTime) {
        if (hasGunSpyglass(stack)) {
            entity.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
            return;
        }
        if (KineticWeapon.has(stack)) {
            KineticWeaponHandler.endCharge(entity);
            return;
        }
        super.releaseUsing(stack, level, entity, remainingTime);
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
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, tooltipComponents, tooltipFlag);
        Consumer<Component> builder = tooltipComponents::add;
        Consumer<Component> statBuilder = (component) -> builder.accept(Component.literal(" ").append(component).withStyle(ChatFormatting.DARK_GREEN));
        Function<String, Component> highlightText = s -> Component.literal(s).withStyle(ChatFormatting.GREEN);
        // Item.TooltipContext carries no holder at this version, so the stat lines are composed off the
        // stack alone -- the modifiers still apply, the holder-scoped ComposeShotEvent does not.
        ShotProfile shotProfile = GunplayManager.compose(null, this.gunProfile, itemStack);
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
    public void registerControllers(AnimatableManager.@NotNull ControllerRegistrar controllers) {
        super.registerControllers(controllers);
        controllers.add(new AnimationController<>(this, IDLE_ANIMATION_CONTROLLER, this::gunIdleHandler));
        controllers.add(new OffsetableAnimationController<>(this, GunAnimations.CONTROLLER_ACTIONS, state -> PlayState.STOP)
                .triggerableAnim(GunAnimations.FIRE, RawAnimation.begin().thenPlay(GunAnimations.FIRE))
                .triggerableAnim(GunAnimations.RELOAD, RawAnimation.begin().thenPlay(GunAnimations.RELOAD))
                .triggerableAnim(GunAnimations.EQUIP, RawAnimation.begin().thenPlay(GunAnimations.EQUIP))
        );
    }

    private PlayState gunIdleHandler(AnimationState<GunItem> animationState) {
        animationState.setAnimation(RawAnimation.begin().thenPlayAndHold(GunAnimations.IDLE));
        return PlayState.CONTINUE;
    }

    public void configureActionTimelineSkip(long instanceId, double skipAtSeconds, double skipToSeconds) {
        AnimationController<?> controller = getAnimatableInstanceCache().getManagerForId(instanceId)
                .getAnimationControllers().get(TRIGGERED_ANIMATION_CONTROLLER);
        if (controller instanceof OffsetableAnimationController<?> skippable) {
            skippable.setTimelineSkip(skipAtSeconds, skipToSeconds);
        }
    }

    /**
     * The controller has no timeline of its own; it only exposes a tick that restarts whenever an animation does.
     * This subclass shifts that tick so an action animation can be joined part-way through, and so a stretch of it
     * can be skipped over, and it surfaces the resulting position for the render-time animation adjusters.
     */
    public static class OffsetableAnimationController<T extends GeoAnimatable> extends AnimationController<T> {
        private static final double TICKS_PER_SECOND = 20;

        private double offsetTicks;
        private double skipAtTicks;
        private double skipToTicks;
        private double skipShiftTicks;
        private boolean skipped;
        private double currentAnimationTicks;

        public OffsetableAnimationController(T animatable, String name, AnimationStateHandler<T> stateHandler) {
            super(animatable, name, stateHandler);
        }

        /**
         * Join the animation this many seconds in. Applies to whatever animation is playing or triggered next.
         */
        public void setTimelineTime(double offsetSeconds) {
            this.offsetTicks = Math.max(offsetSeconds, 0) * TICKS_PER_SECOND;
        }

        public void setTimelineSkip(double skipAtSeconds, double skipToSeconds) {
            this.skipAtTicks = skipAtSeconds * TICKS_PER_SECOND;
            this.skipToTicks = skipToSeconds * TICKS_PER_SECOND;
            this.skipShiftTicks = 0;
            this.skipped = false;
        }

        /**
         * Seconds into the animation currently playing, or zero when the controller is stopped.
         */
        public double getCurrentAnimationTime() {
            return getAnimationState() == State.STOPPED ? 0 : this.currentAnimationTicks / TICKS_PER_SECOND;
        }

        /**
         * Whether the named triggerable animation is the one currently loaded on this controller.
         */
        public boolean isTriggeredAnimation(String animName) {
            RawAnimation animation = this.triggerableAnimations.get(animName);
            return animation != null && animation.equals(this.currentRawAnimation);
        }

        @Override
        public boolean stopTriggeredAnimation() {
            return super.stopTriggeredAnimation();
        }

        /**
         * Stop the controller and drop everything the offset and skip state was carrying.
         */
        public void reset() {
            stop();
            forceAnimationReset();
            this.offsetTicks = 0;
            this.skipAtTicks = 0;
            this.skipToTicks = 0;
            this.skipShiftTicks = 0;
            this.skipped = false;
            this.currentAnimationTicks = 0;
        }

        @Override
        protected double adjustTick(double tick) {
            double adjusted = super.adjustTick(tick);
            if (getAnimationState() != State.RUNNING) {
                // Transitions poll the animation queue off a zeroed tick; shifting it there would stall them.
                return adjusted;
            }
            adjusted += this.offsetTicks;
            if (!this.skipped && this.skipToTicks > this.skipAtTicks
                    && adjusted >= this.skipAtTicks && adjusted < this.skipToTicks) {
                this.skipShiftTicks = this.skipToTicks - adjusted;
                this.skipped = true;
            }
            adjusted += this.skipShiftTicks;
            this.currentAnimationTicks = adjusted;

            return adjusted;
        }
    }
}

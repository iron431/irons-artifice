package io.redspace.irons_artifice.events;

import software.bernie.geckolib.animatable.GeoItem;
import io.redspace.irons_artifice.api.GunAnimations;
import io.redspace.irons_artifice.config.ServerConfig;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.entity.DrownedPirateHelper;
import io.redspace.irons_artifice.item.BulletContainerContents;
import io.redspace.irons_artifice.item.BulletContainerItem;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.PendingShot;
import io.redspace.irons_artifice.item.ReloadState;
import io.redspace.irons_artifice.mixin.ItemEntityAccessor;
import io.redspace.irons_artifice.mixin.RandomizableContainerBlockEntityAccessor;
import io.redspace.irons_artifice.network.PayloadRegistry;
import io.redspace.irons_artifice.network.packets.ClientboundEquipSoundPacket;
import io.redspace.irons_artifice.network.packets.ClientboundGunAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class ServerEvents {


    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        //    @SubscribeEvent
//    public static void onDamage(LivingIncomingDamageEvent event) {
//        // fixme: https://github.com/neoforged/NeoForge/issues/3348
//        if (event.getSource().getDirectEntity() instanceof Bullet) {
//            event.getContainer().setPostAttackInvulnerabilityTicks(0);
//        }
//    }
        if (event.getSource().getDirectEntity() instanceof Bullet) {
            event.getEntity().invulnerableTime = 0;
        }
    }

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        // fixme: mainhand only
        ItemStack itemStack = living.getMainHandItem();
        if (!(itemStack.getItem() instanceof GunItem gunItem)) {
            return;
        }
        var level = living.level();
        if (FireDelayState.get(living).duration() > 0) {
            boolean finished = FireDelayState.tick(living, gunItem, level);
            if (finished && !level.isClientSide()) {
                GunplayManager.flushPendingShot(living);
            }
        }
        // let reload ticking (and sfx handling) be server authoritative
        if (GunItem.isReloading(itemStack)) {
            ReloadState finished = ReloadState.tickReload(itemStack, gunItem, living);
            if (finished != null && !level.isClientSide()) {
                ReloadResult result = GunplayManager.attemptFinishReload(living, itemStack, finished.roundsToLoad());
                if (living instanceof Player player) {
                    GunItem.playReloadFeedback(level, player, result);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBulletPickup(EntityItemPickupEvent event) {
        ItemEntity itemEntity = event.getItem();
        ItemStack bullets = itemEntity.getItem();
        Player player = event.getEntity();
        UUID target = ((ItemEntityAccessor) itemEntity).getTarget();
        boolean claimable = target == null || target.equals(player.getUUID());
        if (itemEntity.hasPickUpDelay() || !claimable || !BulletContainerContents.accepts(bullets)) {
            return;
        }
        ItemStack original = bullets.copy();
        int moved = BulletContainerItem.storeInPouches(player.getInventory(), bullets);
        if (moved <= 0) {
            return;
        }
        player.awardStat(Stats.ITEM_PICKED_UP.get(original.getItem()), moved);
        if (bullets.isEmpty()) {
            event.setCanceled(true);
            ForgeEventFactory.firePlayerItemPickupEvent(player, itemEntity, original);
            player.take(itemEntity, moved);
            itemEntity.discard();
            player.onItemPickup(itemEntity);
        }
    }

    @SubscribeEvent
    public static void onOffhandItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.OFF_HAND) {
            return;
        }
        if (GunItem.isOffhandItemUseBlocked(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        var entity = event.getEntity();
        if (event.getSlot().equals(EquipmentSlot.MAINHAND)) {
            PendingShot.clear(entity);
        }
        var equippedStack = event.getTo();
        var fromStack = event.getFrom();
        if (entity.level() instanceof ServerLevel serverLevel &&
                event.getSlot().equals(EquipmentSlot.MAINHAND) && //fixme: hardcoded mainhand
                !equippedStack.isEmpty() && equippedStack.getItem() instanceof GunItem gunItem &&
                (gunItem != fromStack.getItem() || GeoItem.getId(equippedStack) != GeoItem.getId(fromStack))) {
            if (GunItem.isReloading(equippedStack)) {
                GunplayManager.playReloadAnimation(entity, equippedStack);
            } else {
                performEquipEffects(serverLevel, gunItem, entity, equippedStack);
            }
        }
    }

    private static void performEquipEffects(ServerLevel serverLevel, GunItem gunItem, LivingEntity entity, ItemStack equippedStack) {
        ClientboundGunAnimationPacket packet = new ClientboundGunAnimationPacket(entity.getId(), GeoItem.getOrAssignId(equippedStack, serverLevel),
                equippedStack == entity.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                GunAnimations.EQUIP, 1.0, 0);
        PayloadRegistry.sendToPlayersTrackingEntityAndSelf(entity, packet);
        if (gunItem.getGun().equipSound() != null && entity instanceof ServerPlayer serverPlayer) {
            PayloadRegistry.sendToPlayer(serverPlayer, new ClientboundEquipSoundPacket(SoundSource.PLAYERS, gunItem));
        }
    }

    @SubscribeEvent
    public static void onBlockUsed(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (event.getEntity().isCreative() || event.getEntity().isSpectator()) {
            return;
        }
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof RandomizableContainerBlockEntity randomizableContainerBlockEntity)) {
            return;
        }
        ResourceLocation lootTable = ((RandomizableContainerBlockEntityAccessor) randomizableContainerBlockEntity).getLootTable();
        if (lootTable == null) {
            return;
        }
//        boolean isCursed = level.getServer().reloadableRegistries().lookup().lookupOrThrow(Registries.LOOT_TABLE).get(lootTableKey).map(table -> table.is(IronsArtificeTags.CURSED_BY_PIRATES)).orElse(false);
        // fixme: appears loot table dont have tagging
        boolean isCursed = lootTable.equals(new ResourceLocation("chests/buried_treasure")) ||
                lootTable.equals(new ResourceLocation("chests/shipwreck_treasure")) ||
                lootTable.equals(new ResourceLocation("chests/underwater_ruin_big"));
        if (!isCursed) {
            return;
        }
        if (level.getRandom().nextFloat() > ServerConfig.DROWNED_PIRATE_CURSE_CHANCE.get()) {
            return;
        }
        DrownedPirateHelper.trySpawnPirates(level, event.getEntity(), event.getPos().getCenter());
    }
}

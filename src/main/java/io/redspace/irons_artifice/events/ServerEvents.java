package io.redspace.irons_artifice.events;

import com.geckolib.animatable.GeoItem;
import io.redspace.irons_artifice.config.ServerConfig;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.entity.DrownedPirateHelper;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.ReloadState;
import io.redspace.irons_artifice.network.packets.ClientboundEquipSoundPacket;
import io.redspace.irons_artifice.network.packets.ClientboundGunAnimationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class ServerEvents {


    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
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
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        // fixme: mainhand only
        ItemStack itemStack = living.getMainHandItem();
        if (!(itemStack.getItem() instanceof GunItem gunItem)) {
            return;
        }
        var level = living.level();
        if (FireDelayState.isActive(itemStack)) {
            FireDelayState.tick(itemStack, gunItem, level, living);
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
    public static void onMobEffectApplication(MobEffectEvent.Applicable event) {
        if (event.getEffectSource() instanceof AreaEffectCloud areaEffectCloud &&
                areaEffectCloud.getOwner() == event.getEntity() &&
                areaEffectCloud.getPersistentData().getBooleanOr("irons_artifice:venom_cloud", false)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
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
                "equip", 1.0, 0);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
        if (gunItem.getGun().equipSound() != null && entity instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new ClientboundEquipSoundPacket(SoundSource.PLAYERS, gunItem));
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
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof RandomizableContainerBlockEntity randomizableContainerBlockEntity) || randomizableContainerBlockEntity.getLootTable() == null) {
            return;
        }
        ResourceKey<LootTable> lootTableKey = randomizableContainerBlockEntity.getLootTable();
//        boolean isCursed = level.getServer().reloadableRegistries().lookup().lookupOrThrow(Registries.LOOT_TABLE).get(lootTableKey).map(table -> table.is(IronsArtificeTags.CURSED_BY_PIRATES)).orElse(false);
        // fixme: appears loot table dont have tagging
        boolean isCursed = lootTableKey.identifier().equals(Identifier.withDefaultNamespace("chests/buried_treasure")) ||
                lootTableKey.identifier().equals(Identifier.withDefaultNamespace("chests/shipwreck_treasure")) ||
                lootTableKey.identifier().equals(Identifier.withDefaultNamespace("chests/underwater_ruin_big"));
        if (!isCursed) {
            return;
        }
        if (level.getRandom().nextFloat() > ServerConfig.DROWNED_PIRATE_CURSE_CHANCE.get()) {
            return;
        }
        DrownedPirateHelper.trySpawnPirates(level, event.getEntity(), event.getPos().getCenter());
    }
}

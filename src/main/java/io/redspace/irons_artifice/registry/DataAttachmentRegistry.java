package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.advancement.ShotCombatTracker;
import io.redspace.irons_artifice.data.LastHitTarget;
import io.redspace.irons_artifice.data.RecentShots;
import io.redspace.irons_artifice.data.RecoilState;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.PendingShot;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.Internal
public final class DataAttachmentRegistry {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, IronsArtifice.MODID);

    public static final Supplier<AttachmentType<FireDelayState>> FIRE_DELAY =
            ATTACHMENT_TYPES.register("fire_delay",
                    () -> AttachmentType.builder(() -> FireDelayState.NONE).build());

    public static final Supplier<AttachmentType<PendingShot>> PENDING_SHOT =
            ATTACHMENT_TYPES.register("pending_shot",
                    () -> AttachmentType.builder(() -> PendingShot.NONE).build());

    public static final Supplier<AttachmentType<RecoilState>> RECOIL =
            ATTACHMENT_TYPES.register("recoil",
                    () -> AttachmentType.builder(() -> RecoilState.NONE).build());

    public static final Supplier<AttachmentType<LastHitTarget>> LAST_HIT_TARGET =
            ATTACHMENT_TYPES.register("last_hit_target",
                    () -> AttachmentType.builder(() -> LastHitTarget.NONE).build());

    public static final Supplier<AttachmentType<RecentShots>> RECENT_SHOTS =
            ATTACHMENT_TYPES.register("recent_shots",
                    () -> AttachmentType.builder(() -> RecentShots.NONE).build());

    public static final Supplier<AttachmentType<ShotCombatTracker>> SHOT_COMBAT =
            ATTACHMENT_TYPES.register("shot_combat",
                    () -> AttachmentType.builder(ShotCombatTracker::new).build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}

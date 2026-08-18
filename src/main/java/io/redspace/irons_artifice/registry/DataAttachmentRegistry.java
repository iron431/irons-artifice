package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.data.LastHitTarget;
import io.redspace.irons_artifice.data.RecentShots;
import io.redspace.irons_artifice.data.RecoilState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class DataAttachmentRegistry {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, IronsArtifice.MODID);

    public static final Supplier<AttachmentType<RecoilState>> RECOIL =
            ATTACHMENT_TYPES.register("recoil",
                    () -> AttachmentType.builder(() -> RecoilState.NONE).build());

    public static final Supplier<AttachmentType<LastHitTarget>> LAST_HIT_TARGET =
            ATTACHMENT_TYPES.register("last_hit_target",
                    () -> AttachmentType.builder(() -> LastHitTarget.NONE).build());

    public static final Supplier<AttachmentType<RecentShots>> RECENT_SHOTS =
            ATTACHMENT_TYPES.register("recent_shots",
                    () -> AttachmentType.builder(() -> RecentShots.NONE).build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}

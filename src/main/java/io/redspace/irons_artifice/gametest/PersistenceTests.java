package io.redspace.irons_artifice.gametest;

import io.netty.buffer.Unpooled;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.item.ReloadState;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class PersistenceTests {

    private static ItemStack loadedStack() {
        ItemStack stack = new ItemStack(ItemRegistry.MUSKET.get());
        GunItem.setMagazine(stack, new MagazineContents(3));
        ReloadState.set(stack, new ReloadState(4.0, 20.0, 1.5, 2, 0.0, 0.0));
        stack.set(DataComponentRegistry.MODIFIER_PATCH.get(), DataComponentPatch.builder()
                .set(DataComponentRegistry.GUN_SPYGLASS.get(), Unit.INSTANCE)
                .build());
        stack.set(DataComponentRegistry.GUN_SPYGLASS.get(), Unit.INSTANCE);
        stack.set(DataComponentRegistry.ATTACHMENT.get(),
                new AttachmentMap(Map.of("scope", IronsArtifice.id("spyglass"))));
        return stack;
    }

    /**
     * Compares every restored component's content against {@code original}, not just its presence:
     * a codec that round-trips the right component with the wrong value passes a presence check.
     */
    private static void assertLoadedValues(GameTestHelper helper, ItemStack original, ItemStack restored,
                                            String stage) {
        helper.assertValueEqual(GunItem.getMagazine(restored).count(), GunItem.getMagazine(original).count(),
                "magazine count after " + stage);

        ReloadState originalReload = ReloadState.get(original);
        ReloadState restoredReload = ReloadState.get(restored);
        helper.assertTrue(restoredReload != null, "reload state present after " + stage);
        // ReloadState.equals() omits progress, a live tick value kept out of equals/hashCode so a
        // ticking reload does not spam the network -- so record equality alone would accept a codec
        // that drops or mangles it.
        helper.assertValueEqual(restoredReload, originalReload, "reload state content after " + stage);
        helper.assertValueEqual(restoredReload.progress(), originalReload.progress(),
                "reload state progress after " + stage);

        DataComponentPatch originalPatch = original.get(DataComponentRegistry.MODIFIER_PATCH.get());
        DataComponentPatch restoredPatch = restored.get(DataComponentRegistry.MODIFIER_PATCH.get());
        helper.assertTrue(restoredPatch != null && !restoredPatch.isEmpty(),
                "modifier patch present after " + stage);
        helper.assertTrue(restoredPatch.equals(originalPatch), "modifier patch content after " + stage);

        helper.assertTrue(restored.has(DataComponentRegistry.GUN_SPYGLASS.get()),
                "gun spyglass present after " + stage);

        AttachmentMap originalAttachments = original.get(DataComponentRegistry.ATTACHMENT.get());
        AttachmentMap restoredAttachments = restored.get(DataComponentRegistry.ATTACHMENT.get());
        helper.assertTrue(restoredAttachments != null && !restoredAttachments.isEmpty(),
                "attachment map present after " + stage);
        // AttachmentMap's constructor normalizes through Map.copyOf, but its record equals
        // compares by content, so a copyOf result and a HashMap still compare equal.
        helper.assertValueEqual(restoredAttachments, originalAttachments, "attachment map content after " + stage);
    }

    static void componentsSurviveSave(GameTestHelper helper) {
        RegistryAccess access = helper.getLevel().registryAccess();
        RegistryOps<Tag> ops = access.createSerializationContext(NbtOps.INSTANCE);

        ItemStack original = loadedStack();
        Tag saved = ItemStack.CODEC.encodeStart(ops, original).getOrThrow();
        ItemStack restored = ItemStack.CODEC.parse(ops, saved).getOrThrow();

        assertLoadedValues(helper, original, restored, "save round trip");
        helper.succeed();
    }

    static void componentsSurviveNetwork(GameTestHelper helper) {
        RegistryFriendlyByteBuf buffer =
                new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());

        ItemStack original = loadedStack();
        ItemStack.STREAM_CODEC.encode(buffer, original);
        ItemStack restored = ItemStack.STREAM_CODEC.decode(buffer);

        assertLoadedValues(helper, original, restored, "network round trip");
        helper.succeed();
    }
}

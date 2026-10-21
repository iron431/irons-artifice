package io.redspace.irons_artifice.gametest;

import io.netty.buffer.Unpooled;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.item.ReloadState;
import io.redspace.irons_artifice.registry.ItemRegistry;
import io.redspace.irons_artifice.utils.ModifierPatchHandler;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class PersistenceTests {

    private static ItemStack loadedStack() {
        ItemStack stack = new ItemStack(ItemRegistry.MUSKET.get());
        GunItem.setMagazine(stack, new MagazineContents(3));
        ReloadState.set(stack, new ReloadState(4.0, 20.0, 1.5, 2, 0.0, 0.0));
        CompoundTag patch = new CompoundTag();
        patch.putBoolean(GunItem.GUN_SPYGLASS_TAG_KEY, true);
        stack.getOrCreateTag().put(ModifierPatchHandler.TAG_KEY, patch);
        stack.getOrCreateTag().putBoolean(GunItem.GUN_SPYGLASS_TAG_KEY, true);
        AttachmentMap.set(stack, new AttachmentMap(Map.of("scope", IronsArtifice.id("spyglass"))));
        return stack;
    }

    private static void assertLoadedValues(GameTestHelper helper, ItemStack original, ItemStack restored,
                                            String stage) {
        TestFixtures.assertValueEqual(GunItem.getMagazine(restored).count(), GunItem.getMagazine(original).count(),
                "magazine count after " + stage);

        ReloadState originalReload = ReloadState.get(original);
        ReloadState restoredReload = ReloadState.get(restored);
        helper.assertTrue(restoredReload != null, "reload state present after " + stage);
        TestFixtures.assertValueEqual(restoredReload, originalReload, "reload state content after " + stage);

        CompoundTag originalPatch = original.getTagElement(ModifierPatchHandler.TAG_KEY);
        CompoundTag restoredPatch = restored.getTagElement(ModifierPatchHandler.TAG_KEY);
        helper.assertTrue(restoredPatch != null && !restoredPatch.isEmpty(),
                "modifier patch present after " + stage);
        helper.assertTrue(restoredPatch.equals(originalPatch), "modifier patch content after " + stage);

        helper.assertTrue(GunItem.hasGunSpyglass(restored),
                "gun spyglass present after " + stage);

        AttachmentMap originalAttachments = AttachmentMap.of(original);
        AttachmentMap restoredAttachments = AttachmentMap.of(restored);
        helper.assertTrue(!restoredAttachments.isEmpty(),
                "attachment map present after " + stage);
        TestFixtures.assertValueEqual(restoredAttachments, originalAttachments, "attachment map content after " + stage);
    }

    static void componentsSurviveSave(GameTestHelper helper) {
        ItemStack original = loadedStack();
        CompoundTag saved = original.save(new CompoundTag());
        ItemStack restored = ItemStack.of(saved);

        assertLoadedValues(helper, original, restored, "save round trip");
        helper.succeed();
    }

    static void componentsSurviveNetwork(GameTestHelper helper) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        ItemStack original = loadedStack();
        buffer.writeItem(original);
        ItemStack restored = buffer.readItem();

        assertLoadedValues(helper, original, restored, "network round trip");
        helper.succeed();
    }
}

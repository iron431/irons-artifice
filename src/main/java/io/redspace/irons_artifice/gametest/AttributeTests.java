package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.modifiers.LeechModifier;
import io.redspace.irons_artifice.registry.IronsArtificeAttributes;
import io.redspace.irons_artifice.registry.IronsArtificeItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import static io.redspace.irons_artifice.data.ValueModifier.Operation.ADD;
import static io.redspace.irons_artifice.data.ValueModifier.Operation.MULTIPLY_BASE;
import static io.redspace.irons_artifice.data.ValueModifier.Operation.MULTIPLY_TOTAL;

public final class AttributeTests {

    /** Attribute modifiers become value modifiers one to one, so both must resolve the same stack the same way. */
    static void valueMatchesVanillaAttributeMath(GameTestHelper helper) {
        double base = 7;
        Value value = Value.of(base);
        AttributeInstance vanilla = new AttributeInstance(Holder.direct(new RangedAttribute("test", base, -1e6, 1e6)), changed -> {
        });
        addToBoth(value, vanilla, "add_a", 2, ADD, AttributeModifier.Operation.ADD_VALUE);
        addToBoth(value, vanilla, "add_b", -1, ADD, AttributeModifier.Operation.ADD_VALUE);
        addToBoth(value, vanilla, "base_a", 0.5, MULTIPLY_BASE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        addToBoth(value, vanilla, "base_b", -0.2, MULTIPLY_BASE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        addToBoth(value, vanilla, "total_a", 0.25, MULTIPLY_TOTAL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addToBoth(value, vanilla, "total_b", -0.1, MULTIPLY_TOTAL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        TestFixtures.assertClose(helper, value.compute(), vanilla.getValue(), "value");
        helper.succeed();
    }

    /** {@link LeechModifier} reads the leech component while the shot composes, so it only sees a leech attribute applied before {@code ComposeShotEvent}. */
    static void shooterAttributesApplyBeforeComposeListeners(GameTestHelper helper) {
        LivingEntity shooter = TestFixtures.firingShooter(helper, new BlockPos(1, 1, 1),
                TestFixtures.gunWith(IronsArtificeItems.MUSKET.get(), 0));
        double baseline = TestFixtures.composedProfile(helper, shooter).value(ShotComponents.DAMAGE);

        shooter.getAttribute(IronsArtificeAttributes.LEECH).addTransientModifier(
                new AttributeModifier(IronsArtifice.id("test"), 1, AttributeModifier.Operation.ADD_VALUE));

        TestFixtures.assertClose(helper, TestFixtures.composedProfile(helper, shooter).value(ShotComponents.DAMAGE),
                baseline * (1 + LeechModifier.DAMAGE_BONUS), "damage");
        helper.succeed();
    }

    private static void addToBoth(Value value, AttributeInstance vanilla, String id, double amount,
                                  ValueModifier.Operation operation, AttributeModifier.Operation vanillaOperation) {
        value.addModifier(new ValueModifier(amount, operation));
        vanilla.addTransientModifier(new AttributeModifier(IronsArtifice.id(id), amount, vanillaOperation));
    }
}

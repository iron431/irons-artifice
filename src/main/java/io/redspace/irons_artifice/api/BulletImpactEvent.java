package io.redspace.irons_artifice.api;

import io.redspace.irons_artifice.entity.Bullet;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import org.jspecify.annotations.Nullable;

public class BulletImpactEvent extends ProjectileImpactEvent {
    private final Bullet bullet;
    private final Bullet.HitState defaultHitState;
    private Bullet.@Nullable HitState hitState;

    public BulletImpactEvent(Bullet bullet, HitResult ray, Bullet.HitState defaultHitState) {
        super(bullet, ray);
        this.bullet = bullet;
        this.defaultHitState = defaultHitState;
    }

    public Bullet getBullet() {
        return bullet;
    }

    /**
     * Unless a listener sets one, a cancelled impact keeps flying and an uncancelled one uses the bullet's default.
     */
    public Bullet.HitState getHitState() {
        if (hitState != null) {
            return hitState;
        }
        return isCanceled() ? Bullet.HitState.CONTINUE : defaultHitState;
    }

    public void setHitState(Bullet.HitState hitState) {
        this.hitState = hitState;
    }
}

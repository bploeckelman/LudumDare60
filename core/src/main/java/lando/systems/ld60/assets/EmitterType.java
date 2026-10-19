package lando.systems.ld60.assets;

import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.effects.*;

public enum EmitterType {
    TEST (new TestEffect())
    , CLONE(new CloneEffect())
    , DUST (new FootStepDustCloudEffect())
    , BLOOD(new BloodSplatterEffect())
    , BLOOD_SPRAY(new BloodSprayEffect())
    , HEALTH_BAR_DAMAGE(new HealthBarParticleEffect())
    , SPAWN_CLOUD(new SpawnCloudEffect())
    , HEART_PICKUP(new HeartPickupEffect())
    , PUNCH(new PunchEffect())
    , KICK(new KickEffect())
    , THROW_TRAIL(new ThrowTrailEffect())
    ;

    public final ParticleEffect effect;

    EmitterType(ParticleEffect effect) {
        this.effect = effect;
    }
}

package lando.systems.ld60.assets;

import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.effects.*;
<<<<<<< HEAD

=======
>>>>>>> 1438ec1 (heart pickup)

public enum EmitterType {
    TEST (new TestEffect())
    , CLONE(new CloneEffect())
    , DUST (new FootStepDustCloudEffect())
    , BLOOD(new BloodSplatterEffect())
    , BLOOD_SPRAY(new BloodSprayEffect())
    , HEALTH_BAR_DAMAGE(new HealthBarParticleEffect())
    , SPAWN_CLOUD(new SpawnCloudEffect())
    , HEART_PICKUP(new HeartPickupEffect())
    ;

    public final ParticleEffect effect;

    EmitterType(ParticleEffect effect) {
        this.effect = effect;
    }
}

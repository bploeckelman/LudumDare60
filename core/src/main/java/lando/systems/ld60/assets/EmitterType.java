package lando.systems.ld60.assets;

import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.effects.BloodSplatterEffect;
import lando.systems.ld60.particles.effects.CloneEffect;
import lando.systems.ld60.particles.effects.FootStepDustCloudEffect;
import lando.systems.ld60.particles.effects.HealthBarParticleEffect;
import lando.systems.ld60.particles.effects.TestEffect;

public enum EmitterType {
    TEST (new TestEffect())
    , CLONE(new CloneEffect())
    , DUST (new FootStepDustCloudEffect())
    , BLOOD(new BloodSplatterEffect())
    , HEALTH_BAR_DAMAGE(new HealthBarParticleEffect())
    ;

    public final ParticleEffect effect;

    EmitterType(ParticleEffect effect) {
        this.effect = effect;
    }
}

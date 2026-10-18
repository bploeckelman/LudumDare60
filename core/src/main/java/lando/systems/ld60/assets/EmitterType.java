package lando.systems.ld60.assets;

import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.effects.BloodSplatterEffect;
import lando.systems.ld60.particles.effects.CloneEffect;
import lando.systems.ld60.particles.effects.FootStepDustCloudEffect;
import lando.systems.ld60.particles.effects.TestEffect;

public enum EmitterType {
    TEST (new TestEffect())
    , CLONE(new CloneEffect())
    , DUST (new FootStepDustCloudEffect())
    , BLOOD(new BloodSplatterEffect())
    ;

    public final ParticleEffect effect;

    EmitterType(ParticleEffect effect) {
        this.effect = effect;
    }
}

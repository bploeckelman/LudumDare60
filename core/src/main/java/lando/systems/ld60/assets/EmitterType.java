package lando.systems.ld60.assets;

import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.effects.CloneEffect;
import lando.systems.ld60.particles.effects.TestEffect;

public enum EmitterType {
    TEST (new TestEffect())
    , CLONE(new CloneEffect())
    ;

    public final ParticleEffect effect;

    EmitterType(ParticleEffect effect) {
        this.effect = effect;
    }
}

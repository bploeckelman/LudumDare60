package lando.systems.ld60.particles;

public interface ParticleEffectParams {
    default int getOrder() {
        return 0;
    }
    boolean isComplete();
}

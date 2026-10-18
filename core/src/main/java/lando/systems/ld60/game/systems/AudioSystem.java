package lando.systems.ld60.game.systems;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld60.assets.MusicType;
import lando.systems.ld60.assets.SoundType;
import lando.systems.ld60.assets.SoundType.SoundGroup;
import lando.systems.ld60.assets.SoundType.WeightedSound;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.events.Signal;
import lando.systems.ld60.events.SignalListener;

public class AudioSystem implements SignalListener<AudioEvent> {

    public static final float DEFAULT_VOLUME = 0.5f;
    public static Music currentMusic = null;


    public AudioSystem() {
        AudioEvent.signal.register(this);
    }

    @Override
    public void receive(Signal<AudioEvent> signal, AudioEvent event) {
        switch (event) {
            case AudioEvent.PlaySound ps -> {
                playSoundLogic(ps.soundType, ps.volume, ps.pan);
            }
            case AudioEvent.PlaySoundGroup psg -> {
                SoundType pickedSound = getRandomFromGroup(psg.group);
                playSoundLogic(pickedSound, psg.volume, 0f);
            }
            case AudioEvent.PlayMusic pm -> {
                var music = pm.musicType.get();
                music.setVolume(pm.volume);
                music.setLooping(true);
                music.play();
                this.currentMusic = music;
            }
            case AudioEvent.StopMusic sm -> {
                if (sm.musicType != null) {
                    var music = sm.musicType.get();
                    music.stop();
                } else {
                    for (var type : MusicType.values()) {
                        type.get().stop();
                    }
                }
                this.currentMusic = null;
            }
            default -> throw new IllegalStateException("Unexpected event: " + event);
        }
    }

    private void playSoundLogic(SoundType soundType, float volume, float pan) {
        // Automatically calls default get() from AssetType interface
        soundType.get().play(volume, soundType.pitch, pan);
    }

    private SoundType getRandomFromGroup(SoundGroup group) {
        float randomVal = MathUtils.random(group.totalWeight);
        float currentWeight = 0f;

        for (WeightedSound ws : group.sounds) {
            currentWeight += ws.weight;
            if (randomVal <= currentWeight) {
                return ws.sound;
            }
        }

        return group.sounds[group.sounds.length - 1].sound;
    }

    public void dispose() {
        AudioEvent.signal.remove(this);
    }
}

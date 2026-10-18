package lando.systems.ld60.game.systems;

import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld60.Main;
import lando.systems.ld60.assets.MusicType;
import lando.systems.ld60.assets.SoundType;
import lando.systems.ld60.assets.SoundType.SoundGroup;
import lando.systems.ld60.assets.SoundType.WeightedSound;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.events.Signal;
import lando.systems.ld60.events.SignalListener;

public class AudioSystem implements SignalListener<AudioEvent> {

    public static MutableFloat musicVolume;
    public static MutableFloat soundVolume;

    public static final float DEFAULT_VOLUME = 0.5f;
    public static Music currentMusic = null;

    private static final Preferences prefs = Main.game.prefs;


    public AudioSystem() {
        musicVolume = new MutableFloat(prefs.getFloat("musicVolume", DEFAULT_VOLUME));
        soundVolume = new MutableFloat(prefs.getFloat("soundVolume", DEFAULT_VOLUME));
        prefs.putFloat("soundVolume", soundVolume.floatValue());
        prefs.putFloat("musicVolume", musicVolume.floatValue());
        prefs.flush();
        AudioEvent.signal.register(this);
    }

    public static void setSoundVolume(float soundVolume) {
        prefs.putFloat("soundVolume", soundVolume);
        prefs.flush();
        AudioSystem.soundVolume.setValue(soundVolume);
    }
    public static void setMusicVolume(float musicVolume) {
        prefs.putFloat("musicVolume", musicVolume);
        prefs.flush();
        if (AudioSystem.currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
        AudioSystem.musicVolume.setValue(musicVolume);
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
                music.setVolume(pm.volume * musicVolume.floatValue());
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
        soundType.get().play(volume * soundVolume.floatValue(), soundType.pitch, pan);
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

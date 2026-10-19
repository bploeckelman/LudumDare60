package lando.systems.ld60.events;

import lando.systems.ld60.assets.MusicType;
import lando.systems.ld60.assets.SoundType;

public abstract class AudioEvent implements SignalEvent {

    public static final Signal<AudioEvent> signal = new Signal<>();

    public static void playSound(SoundType sound)               { signal.dispatch(new PlaySound(sound)); }
    public static void playSound(SoundType sound, float volume) { signal.dispatch(new PlaySound(sound, volume)); }
    public static void playSound(SoundType sound, float volume, float pan) { signal.dispatch(new PlaySound(sound, volume, pan)); }

    public static void playSound(SoundType.SoundGroup group)               { signal.dispatch(new PlaySoundGroup(group)); }
    public static void playSound(SoundType.SoundGroup group, float volume) { signal.dispatch(new PlaySoundGroup(group, volume)); }

    public static void playMusic(MusicType music)               { AudioEvent.stopAllMusic(); signal.dispatch(new PlayMusic(music)); }
    public static void playMusic(MusicType music, float volume) { AudioEvent.stopAllMusic(); signal.dispatch(new PlayMusic(music, volume)); }
    public static void stopMusic(MusicType music)               { signal.dispatch(new StopMusic(music)); }
    public static void stopAllMusic()                           { signal.dispatch(new StopMusic()); }

    // --- Specific Event Payloads ---

    public static final class PlaySound extends AudioEvent {
        public final SoundType soundType;
        public final float volume;
        public final float pan;

        private PlaySound(SoundType soundType) { this(soundType, 1f, 0f); }
        private PlaySound(SoundType soundType, float volume) { this(soundType, volume, 0f); }
        private PlaySound(SoundType soundType, float volume, float pan) {
            this.soundType = soundType;
            this.volume = volume;
            this.pan = pan;
        }
    }

    public static final class PlaySoundGroup extends AudioEvent {
        public final SoundType.SoundGroup group;
        public final float volume;

        private PlaySoundGroup(SoundType.SoundGroup group) { this(group, 1f); }
        private PlaySoundGroup(SoundType.SoundGroup group, float volume) {
            this.group = group;
            this.volume = volume;
        }
    }

    public static final class PlayMusic extends AudioEvent {
        public final MusicType musicType;
        public final float volume;
        private PlayMusic(MusicType musicType) { this(musicType, 1f); }
        private PlayMusic(MusicType musicType, float volume) {
            this.musicType = musicType;
            this.volume = volume;
        }
    }

    public static final class StopMusic extends AudioEvent {
        public final MusicType musicType;
        private StopMusic() { this(null); }
        private StopMusic(MusicType musicType) { this.musicType = musicType; }
    }
}

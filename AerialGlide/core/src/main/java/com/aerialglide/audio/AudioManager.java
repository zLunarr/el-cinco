package com.aerialglide.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {
    private static final String PREFS = "aerial_glide_audio";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_VOLUME = "volume";

    private final Preferences prefs;
    private boolean enabled;
    private float volume;
    private Music currentMusic;

    public AudioManager() {
        prefs = Gdx.app.getPreferences(PREFS);
        enabled = prefs.getBoolean(KEY_ENABLED, true);
        volume = prefs.getFloat(KEY_VOLUME, 0.7f);
    }

    public void playMusicLoop(String path) {
        if (!enabled) return;
        stopMusic();
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
        currentMusic.setLooping(true);
        currentMusic.setVolume(volume);
        currentMusic.play();
    }

    public void playSound(String path) {
        if (!enabled) return;
        Sound s = Gdx.audio.newSound(Gdx.files.internal(path));
        s.play(volume);
        s.dispose();
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) stopMusic();
        save();
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null) currentMusic.setVolume(this.volume);
        save();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public float getVolume() {
        return volume;
    }

    private void save() {
        prefs.putBoolean(KEY_ENABLED, enabled);
        prefs.putFloat(KEY_VOLUME, volume);
        prefs.flush();
    }

    public void dispose() {
        stopMusic();
    }
}

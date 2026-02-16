package com.aerialglide.persistence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class ScoreRepository {
    private static final String PREFS = "aerial_glide_scores";
    private static final String HIGH_SCORE_KEY = "offline_high_score";
    private final Preferences prefs;

    public ScoreRepository() {
        prefs = Gdx.app.getPreferences(PREFS);
    }

    public int getOfflineHighScore() {
        return prefs.getInteger(HIGH_SCORE_KEY, 0);
    }

    public void saveOfflineHighScore(int score) {
        prefs.putInteger(HIGH_SCORE_KEY, Math.max(score, 0));
        prefs.flush();
    }
}

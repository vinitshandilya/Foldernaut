package com.vinit.foldernaut;

import android.content.Context;
import android.media.MediaPlayer;

public class ClickSound {

    int soundResource;
    Context ctx;

    public ClickSound(Context ctx, int soundResource) {
        this.ctx = ctx;
        this.soundResource = soundResource;
    }

    public void play() {
        MediaPlayer mp = MediaPlayer.create(ctx, soundResource);
        try {
            if (mp.isPlaying()) {
                mp.stop();
                mp.release();
                mp = MediaPlayer.create(ctx, soundResource);
            } mp.start();
        } catch(Exception e) { e.printStackTrace(); }
    }
}

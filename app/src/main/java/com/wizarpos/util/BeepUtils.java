package com.wizarpos.util;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;

public class BeepUtils {
    private static final float BEEP_VOLUME = 0.40f;
    private static Handler handler = new Handler(Looper.getMainLooper());
    /**
     * 播放声音
     */
    public static void playAudio(Context context) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // 保存当前音效音量
        final int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);

// 临时设置音量为最大或适当增大
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, maxVolume, 0);

// 播放系统音效
        audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);

// 延迟后恢复音量
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, currentVolume, 0);
//            }
//        }, 300);
    }
}

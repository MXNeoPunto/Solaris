package solaris.gt;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundHelper {
    private SoundPool soundPool;
    private int clickSoundId;
    private boolean loaded = false;

    public SoundHelper(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((soundPool1, sampleId, status) -> {
            if (status == 0) {
                loaded = true;
            }
        });

        clickSoundId = soundPool.load(context, R.raw.click_sound, 1);
    }

    public void playClick() {
        if (loaded) {
            soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f);
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}

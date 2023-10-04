package Services;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.musicplayer.myproject.Activities.MainActivity;
import com.google.android.exoplayer2.MediaItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Utilities.HelperMethods;


public class WidgetService extends IntentService {

    public static final String SHUFFLE = "WIDGET SHUFFLE BUTTON";
    public static final String SKIP_PREVIOUS = "WIDGET SKIP PREVIOUS";
    public static final String PLAY_PAUSE = "WIDGET PLAY PAUSE";
    public static final String SKIP_NEXT = "WIDGET SKIP NEXT";
    public static final String PLAY_LIST = "WIDGET PLAY LIST";
    public static final String SONG_POSITION_INDEX = "SONG POSITION INDEX";
    MainActivity mainActivity = new MainActivity();
    List<MediaItem> mediaItems;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WidgetService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        handleButtonsClicks(Objects.requireNonNull(intent));
    }


    private void handleButtonsClicks(Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case SHUFFLE:
                break;
            case SKIP_PREVIOUS:
                setSkipPrevious();
                break;
            case PLAY_PAUSE:
                setPlayPause();
                break;
            case SKIP_NEXT:
                setSkipNext();
                break;
            case PLAY_LIST:

        }

    }

    private List<MediaItem> getMediaItems() {
        mediaItems = new ArrayList<>();
        mainActivity.initializeMusicPlayer(getApplicationContext());
        return HelperMethods.convertSongsToMediaItems(mainActivity.fetchSongs());
    }

    private void setPlayPause() {
        mediaItems = getMediaItems();
        mainActivity.startOrContinuePlaying(mediaItems, mainActivity.restoreLastPlayedSongPosition());
    }

    private void setSkipPrevious() {
        if (mainActivity.exoPlayer.isPlaying()) {
            mainActivity.exoPlayer.seekToPrevious();
            mainActivity.storeLastPlayedSongPosition(mainActivity.exoPlayer.getCurrentMediaItemIndex());
        } else {
            mediaItems = getMediaItems();
            if (mainActivity.restoreLastPlayedSongPosition() == 0) {
                mainActivity.startOrContinuePlaying(mediaItems, mainActivity.restoreLastPlayedSongPosition());
            } else {
                mainActivity.startOrContinuePlaying(mediaItems, mainActivity.restoreLastPlayedSongPosition() - 1);
            }
        }
    }


    private void setSkipNext() {
        if (mainActivity.exoPlayer.isPlaying()) {
            mainActivity.exoPlayer.seekToNext();
            mainActivity.storeLastPlayedSongPosition(mainActivity.exoPlayer.getCurrentMediaItemIndex());
        } else {
            mediaItems = getMediaItems();
            if (mainActivity.restoreLastPlayedSongPosition() == 0) {
                mainActivity.startOrContinuePlaying(mediaItems, mainActivity.restoreLastPlayedSongPosition());
            } else {
                mainActivity.startOrContinuePlaying(mediaItems, mainActivity.restoreLastPlayedSongPosition() - 1);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainActivity.storeLastPlayedSongPosition(mainActivity.exoPlayer.getCurrentMediaItemIndex());
        if (mainActivity.exoPlayer.isPlaying()) {
            mainActivity.exoPlayer.stop();
        }
        mainActivity.exoPlayer.release();
        mainActivity = null;
    }
}

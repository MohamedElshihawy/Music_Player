package Utilities;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import java.util.ArrayList;
import java.util.List;

import Data.Song;

public class HelperMethods {
    public static String getDuration(int songDuration) {

        long hours = songDuration / (1000 * 60 * 60);

        int minutes = (int) (songDuration % (1000 * 60 * 60) / (1000 * 60));

        int seconds = (int) (songDuration % (1000 * 60 * 60) % (1000 * 60 * 60) / (1000));
        String time;
        if (hours >= 1) {
            time = hours + ":" + minutes + ":" + seconds;
        } else if (minutes >= 1) {
            time = minutes + ":" + seconds;

        } else {
            time = 0 + ":" + seconds;
        }

        return time;
    }



    public static List<MediaItem> convertSongsToMediaItems(List<Song> songs) {
        List<MediaItem> mediaItems = new ArrayList<>();
        MediaItem mediaItem;
        if (songs.size() != 0) {
            for (Song song : songs) {
                mediaItem = new MediaItem.Builder()
                        .setUri(song.getSongUri())
                        .setMediaMetadata(getMediaMetaData(song))
                        .build();
                mediaItems.add(mediaItem);
            }
        }
        return mediaItems;
    }

    private static MediaMetadata getMediaMetaData(Song song) {
        return new MediaMetadata.Builder().setTitle(song.getSongTitle())
                .setArtworkUri(song.getSongAlbumUri())
                .setArtist(song.getSongArtistName())
                .build();
    }

}

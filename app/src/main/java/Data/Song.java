package Data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Song implements Parcelable ,Serializable {

    private String songTitle;
    private long songId;
    private Uri songUri;
    private Uri songAlbumUri;
    private String songDuration;
    private String songArtistName;
    private boolean isFavourite;

    public Song(String songTitle, long songId, Uri songUri, Uri songAlbumUri, String songDuration, String songArtistName) {
        this.songTitle = songTitle;
        this.songId = songId;
        this.songUri = songUri;
        this.songAlbumUri = songAlbumUri;
        this.songDuration = songDuration;
        this.songArtistName = songArtistName;
    }

    protected Song(Parcel in) {
        songTitle = in.readString();
        songId = in.readLong();
        songUri = in.readParcelable(Uri.class.getClassLoader());
        songAlbumUri = in.readParcelable(Uri.class.getClassLoader());
        songDuration = in.readString();
        songArtistName = in.readString();
        isFavourite = in.readByte() != 0;
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public Uri getSongUri() {
        return songUri;
    }

    public void setSongUri(Uri songUri) {
        this.songUri = songUri;
    }

    public Uri getSongAlbumUri() {
        return songAlbumUri;
    }

    public void setSongAlbumUri(Uri songAlbumUri) {
        this.songAlbumUri = songAlbumUri;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(String songDuration) {
        this.songDuration = songDuration;
    }

    public String getSongArtistName() {
        return songArtistName;
    }

    public void setSongArtistName(String songArtistName) {
        this.songArtistName = songArtistName;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(songTitle);
        dest.writeLong(songId);
        dest.writeParcelable(songUri, flags);
        dest.writeParcelable(songAlbumUri, flags);
        dest.writeString(songDuration);
        dest.writeString(songArtistName);
        dest.writeByte((byte) (isFavourite ? 1 : 0));
    }
}

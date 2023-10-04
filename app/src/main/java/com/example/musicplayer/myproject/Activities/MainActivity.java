package com.example.musicplayer.myproject.Activities;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chibde.visualizer.BarVisualizer;
import com.example.musicplayer.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Data.Song;
import Services.WidgetService;
import Utilities.HelperMethods;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    final String storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    final String micPermission = Manifest.permission.RECORD_AUDIO;
    public ExoPlayer exoPlayer;
    Toolbar mToolbar;
    ActivityResultLauncher<String> storageResultLauncher;
    ActivityResultLauncher<String> micResultLauncher;
    RecyclerView mRecyclerView;
    SongAdapter mSongAdapter;
    RecyclerView.LayoutManager layoutManager;
    List<Song> allSongs, allSongsBackup;
    SongAdapter.OnItemClick onItemClick;
    ImageView blurBackground, playerBackBtn;
    ConstraintLayout miniPlayerContainer;
    TextView playerSongName;
    BarVisualizer playerVisualizer;
    ImageButton playerShuffleRepeatBtn, playerSkipPreviousBtn,
            playerPlayPauseBtn, playerSkipNextBtn, playerPlayListBtn,
            miniPlayerSkipPreviousBtn, miniPlayerSkipNextBtn, miniPlayerPlayPause;
    SeekBar playerProgressBar;
    View miniPlayer, mainPlayer;
    TextView playerProgressDuration, playerFullDuration, playerSongTitle, miniPlayerSongTitle, miniPlayerArtistName;

    ImageView playerSongCover, miniPlayerSongCover;
    int songPosition;
    boolean activateVisualiser = false;
    boolean firstTimeToOPenApp = true;
    int repeatMode = 1;
    private List<MediaItem> mediaItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        allSongsBackup = new ArrayList<>();
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));


        exoPlayer = initializeMusicPlayer(this);

        songPosition = restoreLastPlayedSongPosition();

        storageResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                List<Song> list = new ArrayList<>();

                list = fetchSongs();
                showAllSongs(list);
                allSongsBackup.addAll(list);


            } else {
                replyToUserResponse();
            }
        });
        storageResultLauncher.launch(storagePermission);

        micResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                activateVisualiser = true;
            } else {
                ResponseToMicPer();
            }
        });
        micResultLauncher.launch(micPermission);


        playerControls();


    }


    private void ResponseToMicPer() {
        if (ContextCompat.checkSelfPermission(this, micPermission) == PackageManager.PERMISSION_GRANTED) {
            activateVisualiser = true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(micPermission)) {
                new AlertDialog.Builder(this).setTitle("Permission Required")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                storageResultLauncher.launch(storagePermission);
                            }
                        })
                        .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }).show();
            }
        } else {
            Toast.makeText(this, " Permission denied ", Toast.LENGTH_SHORT).show();

        }
    }

    public List<Song> fetchSongs() {
        List<Song> songs = new ArrayList<>();
        Uri mediaStoreUri;
        allSongs = new ArrayList<>();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaStoreUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DISPLAY_NAME
        };

        String order = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = getContentResolver().query(mediaStoreUri, projection, null, null, order)) {
            int id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int artist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int duration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int albumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int songName = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);

            while (cursor.moveToNext()) {
                long mId = cursor.getLong(id);
                String mArtist = cursor.getString(artist);
                int mDuration = cursor.getInt(duration);
                long mAlbumId = cursor.getLong(albumId);
                String mSongName = cursor.getString(songName);

                Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mId);


                Uri albumCoverUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                        , mAlbumId);

                mSongName = mSongName.substring(0, mSongName.lastIndexOf("."));

                songs.add(new Song(mSongName, mId, songUri, albumCoverUri, HelperMethods.getDuration(mDuration), mArtist));
            }

            return songs;
        }


    }

    private void showAllSongs(List<Song> songs) {
        if (songs.size() == 0) {
            Toast.makeText(this, "No Songs Found In Your Device", Toast.LENGTH_SHORT).show();
        } else {
            allSongs.clear();
            allSongs.addAll(songs);

            String appBarTitle = getResources().getString(R.string.app_name) + " " + songs.size() + " Song Found";

            Objects.requireNonNull(getSupportActionBar()).setTitle(appBarTitle);

            mSongAdapter = new SongAdapter(allSongs, this, onItemClick);

            layoutManager = new LinearLayoutManager(this);

            mRecyclerView.setLayoutManager(layoutManager);

            ScaleInAnimationAdapter inAnimationAdapter = new ScaleInAnimationAdapter(mSongAdapter);
            inAnimationAdapter.setDuration(500);
            inAnimationAdapter.setFirstOnly(false);
            inAnimationAdapter.setInterpolator(new OvershootInterpolator());
            mRecyclerView.setAdapter(inAnimationAdapter);
            mSongAdapter.setOnItemClick(new SongAdapter.OnItemClick() {
                @Override
                public void OnClick(int Position) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), micPermission) == PackageManager.PERMISSION_GRANTED) {
                        songPosition = Position;
                        mediaItems = HelperMethods.convertSongsToMediaItems(allSongs);

                        if (firstTimeToOPenApp) {
                            activateVisualiser();
                            firstTimeToOPenApp = false;
                        }
                        startOrContinuePlaying(mediaItems, songPosition);
                        mRecyclerView.setVisibility(View.GONE);
                        showMainPlayer();

                    } else {
                        micResultLauncher.launch(micPermission);
                    }

                }
            });

        }
    }

    private void showMainPlayer() {
        mainPlayer.setVisibility(View.VISIBLE);
        miniPlayer.setVisibility(View.VISIBLE);
    }

    private void replyToUserResponse() {

        if (ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED) {

            showAllSongs(fetchSongs());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(storagePermission)) {
                new AlertDialog.Builder(this).setTitle("Permission Required")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                storageResultLauncher.launch(storagePermission);
                            }
                        })
                        .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }).show();
            }
        } else {
            Toast.makeText(this, " Permission denied ", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem menuItem = menu.findItem(R.id.search_song);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchForSong(searchView);

        return super.onCreateOptionsMenu(menu);
    }

    private void searchForSong(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "onQueryTextChange: " + newText);
                getSongByName(newText.toLowerCase());
                return true;
            }
        });

    }

    private void getSongByName(String newText) {
        List<Song> filteredSongs = new ArrayList<>();
        Log.i(TAG, "getSongByName: name = " + newText + "songs  = " + allSongs.size() + "  backup" + allSongsBackup.size());
        if (allSongs.size() > 0) {
            for (Song song : allSongs) {
                if (song.getSongTitle().toLowerCase().contains(newText)) {
                    filteredSongs.add(song);
                }
            }
        } else {
            Log.i(TAG, "getSongByName: arrived");
            allSongs.addAll(allSongsBackup);
            Log.i(TAG, "getSongByName: size" + allSongs.size());
        }
        if (mSongAdapter != null) {
            mSongAdapter.setFilteredSongs(filteredSongs);
        }


    }

    public ExoPlayer initializeMusicPlayer(Context context) {
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(context).build();
        } else {
            return exoPlayer;
        }
        return exoPlayer;
    }


    public void activateVisualiser() {
        if (activateVisualiser) {
            playerVisualizer.setDensity(20);
            playerVisualizer.setColor(androidx.cardview.R.color.cardview_light_background);
            playerVisualizer.setPlayer(exoPlayer.getAudioSessionId());

        }
    }


    public void startOrContinuePlaying(List<MediaItem> mediaItems, int songPosition) {
        if (exoPlayer != null) {
            if (!exoPlayer.isPlaying()) {
                exoPlayer.setMediaItems(mediaItems, songPosition, 0);
            } else {
                exoPlayer.pause();
                exoPlayer.stop();
                exoPlayer.seekTo(songPosition, 0);
            }
            exoPlayer.prepare();
            exoPlayer.play();
        }
    }

    private void initializeViews() {
        mainPlayer = findViewById(R.id.player_view);
        miniPlayer = findViewById(R.id.mini_player);
        playerBackBtn = findViewById(R.id.player_back_btn);
        playerShuffleRepeatBtn = findViewById(R.id.player_shuffle_repeat);
        playerPlayPauseBtn = findViewById(R.id.player_play);
        playerSkipNextBtn = findViewById(R.id.player_skip_next);
        playerSkipPreviousBtn = findViewById(R.id.player_skip_previous);
        playerPlayListBtn = findViewById(R.id.player_play_list);
        playerSongName = findViewById(R.id.player_song_title);
        playerProgressDuration = findViewById(R.id.player_st_duration);
        playerFullDuration = findViewById(R.id.player_song_duration);
        playerSongCover = findViewById(R.id.player_song_cover);
        playerProgressBar = findViewById(R.id.seekBar);
        playerVisualizer = findViewById(R.id.barVisualizer);


        miniPlayerArtistName = findViewById(R.id.mini_artist_name);
        miniPlayerPlayPause = findViewById(R.id.mini_play);
        miniPlayerSkipNextBtn = findViewById(R.id.mini_skip_next);
        miniPlayerSongCover = findViewById(R.id.mini_song_cover);
        miniPlayerSongTitle = findViewById(R.id.mini_song_title);
        miniPlayerSkipPreviousBtn = findViewById(R.id.mini_skip_previous);


        mRecyclerView = findViewById(R.id.songs_recyclerview);
        mToolbar = findViewById(R.id.tool_bar);

        blurBackground = findViewById(R.id.blur_image);
    }


    public void playerControls() {
        playerBackBtn.setOnClickListener(v -> exitMainPlayer());
        playerPlayListBtn.setOnClickListener(v -> mainPlayer.setVisibility(View.GONE));
        miniPlayer.setOnClickListener(v -> transitionToPlayer());

        exoPlayer.addListener(new Player.Listener() {

            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);

                assert mediaItem != null;
                playerSongName.setText(mediaItem.mediaMetadata.title);
                miniPlayerSongTitle.setText(mediaItem.mediaMetadata.title);
                miniPlayerSongTitle.setSelected(true);
                miniPlayerArtistName.setText(mediaItem.mediaMetadata.artist);
                playerSongCover.setImageURI(mediaItem.mediaMetadata.artworkUri);
                miniPlayerSongCover.setImageURI(mediaItem.mediaMetadata.artworkUri);
                playerSongName.setSelected(true);
                miniPlayerSongTitle.setSelected(true);
                playerFullDuration.setText(HelperMethods.getDuration((int) exoPlayer.getDuration()));
                playerProgressBar.setMax((int) exoPlayer.getDuration());
                playerProgressBar.setProgress((int) exoPlayer.getCurrentPosition());
                playerProgressDuration.setText(HelperMethods.getDuration((int) exoPlayer.getCurrentPosition()));
                playerPlayPauseBtn.setImageResource(R.drawable.pause);
                miniPlayerPlayPause.setImageResource(R.drawable.pause);
                updateBlurBackground();
                showSongCover();
                updatePlayerProgress();
                activateVisualiser();
                playerSongCover.setAnimation(rotateImageAnimation());
                miniPlayerSongCover.setAnimation(rotateImageAnimation());

                if (!exoPlayer.isPlaying()) {
                    exoPlayer.play();
                }


            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);

                if (playbackState == ExoPlayer.STATE_READY) {
                    playerSongName.setText(Objects.requireNonNull(exoPlayer.getCurrentMediaItem()).mediaMetadata.title);
                    miniPlayerSongTitle.setText(Objects.requireNonNull(exoPlayer.getCurrentMediaItem()).mediaMetadata.title);
                    miniPlayerSongTitle.setSelected(true);
                    miniPlayerArtistName.setText(exoPlayer.getCurrentMediaItem().mediaMetadata.artist);
                    playerProgressDuration.setText(HelperMethods.getDuration((int) exoPlayer.getCurrentPosition()));
                    playerFullDuration.setText(HelperMethods.getDuration((int) exoPlayer.getDuration()));
                    playerProgressBar.setMax((int) exoPlayer.getDuration());
                    playerProgressBar.setProgress((int) exoPlayer.getCurrentPosition());
                    playerPlayPauseBtn.setImageResource(R.drawable.pause);
                    miniPlayerPlayPause.setImageResource(R.drawable.pause);


                    showSongCover();
                    updatePlayerProgress();
                    activateVisualiser();
                    playerSongCover.setAnimation(rotateImageAnimation());
                    miniPlayerSongCover.setAnimation(rotateImageAnimation());

                } else {
                    playerPlayPauseBtn.setImageResource(R.drawable.play);
                    miniPlayerPlayPause.setImageResource(R.drawable.play);
                }
            }
        });

        playerSkipNextBtn.setOnClickListener(view -> skipToNextSong());
        miniPlayerSkipNextBtn.setOnClickListener(view -> skipToNextSong());
        playerSkipPreviousBtn.setOnClickListener(view -> skipToPreviousSong());
        miniPlayerSkipPreviousBtn.setOnClickListener(view -> skipToPreviousSong());

        playerPlayPauseBtn.setOnClickListener(view -> playPausePlayer());
        miniPlayerPlayPause.setOnClickListener(view -> playPausePlayer());

        playerProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int barProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                barProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (exoPlayer.getPlaybackState() == ExoPlayer.STATE_READY) {
                    seekBar.setProgress(barProgress);
                    playerProgressDuration.setText(HelperMethods.getDuration(barProgress));
                    exoPlayer.seekTo(barProgress);
                }

            }
        });

        playerShuffleRepeatBtn.setOnClickListener(view -> shuffleOrRepeatSongs());


    }

    private void exitMainPlayer() {
        mainPlayer.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void updateBlurBackground() {
        BitmapDrawable drawable = (BitmapDrawable) playerSongCover.getDrawable();
        if (drawable == null) {
            drawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.ic_music);
        }
        assert drawable != null;
        Bitmap bitmap = drawable.getBitmap();

        Glide.with(this)
                .load(bitmap)
                .apply(bitmapTransform(new BlurTransformation(25, 3)))
                .into(blurBackground);
    }

    private void shuffleOrRepeatSongs() {
        switch (repeatMode) {
            case 1:
                exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
                playerShuffleRepeatBtn.setImageResource(R.drawable.repeat_all);
                exoPlayer.setShuffleModeEnabled(false);
                repeatMode = 2;
                break;
            case 2:
                exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                playerShuffleRepeatBtn.setImageResource(R.drawable.repeat_one);
                repeatMode = 3;
                break;
            case 3:
                exoPlayer.setShuffleModeEnabled(true);
                playerShuffleRepeatBtn.setImageResource(R.drawable.shuffle);
                repeatMode = 1;
                break;

        }
    }

    private void playPausePlayer() {
        if (exoPlayer.isPlaying()) {
            exoPlayer.pause();
            playerPlayPauseBtn.setImageResource(R.drawable.play);
            miniPlayerPlayPause.setImageResource(R.drawable.play);
            playerSongCover.clearAnimation();
            miniPlayerSongCover.clearAnimation();
        } else {
            exoPlayer.play();
            playerPlayPauseBtn.setImageResource(R.drawable.pause);
            miniPlayerPlayPause.setImageResource(R.drawable.pause);
            playerSongCover.startAnimation(rotateImageAnimation());
            miniPlayerSongCover.startAnimation(rotateImageAnimation());

        }
    }

    private void skipToNextSong() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNext();
        }
    }

    private void skipToPreviousSong() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToPrevious();
        }
    }

    private Animation rotateImageAnimation() {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF
                , .5f, Animation.RELATIVE_TO_SELF, .5f);
        rotateAnimation.setDuration(10000);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        return rotateAnimation;
    }

    private void updatePlayerProgress() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (exoPlayer.isPlaying()) {
                    playerProgressDuration.setText(HelperMethods.getDuration((int) exoPlayer.getCurrentPosition()));
                    playerProgressBar.setProgress((int) exoPlayer.getCurrentPosition());
                }
                // calling this recursive method all the time
                updatePlayerProgress();
            }
        }, 1000);
    }

    private void showSongCover() {
        playerSongCover.setImageURI(Objects.requireNonNull(exoPlayer.getCurrentMediaItem()).mediaMetadata.artworkUri);
        miniPlayerSongCover.setImageURI(Objects.requireNonNull(exoPlayer.getCurrentMediaItem()).mediaMetadata.artworkUri);

        if (playerSongCover.getDrawable() == null) {
            playerSongCover.setImageResource(R.drawable.ic_music);
            miniPlayerSongCover.setImageResource(R.drawable.ic_music);
        }
    }

    private void transitionToPlayer() {
        miniPlayer.setVisibility(View.GONE);
        mainPlayer.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }


    public void storeLastPlayedSongPosition(int Position) {
        SharedPreferences preferences = this.getSharedPreferences("application", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(WidgetService.SONG_POSITION_INDEX, Position);
        editor.apply();
    }

    public int restoreLastPlayedSongPosition() {
        SharedPreferences preferences = this.getSharedPreferences("application", Context.MODE_PRIVATE);
        return preferences.getInt(WidgetService.SONG_POSITION_INDEX, 0);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer.isPlaying()) {
            storeLastPlayedSongPosition(exoPlayer.getCurrentMediaItemIndex());
            exoPlayer.stop();
        }
        exoPlayer.release();
    }


    @Override
    public void onBackPressed() {
        if (mainPlayer.getVisibility() == View.VISIBLE) {
            mainPlayer.setVisibility(View.GONE);
            miniPlayer.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
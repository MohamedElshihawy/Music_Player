package com.example.musicplayer.myproject.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;

import java.util.List;

import Data.Song;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.mViewHolder> {
    List<Song> songs;
    Context context;
    OnItemClick onItemClick;


    public SongAdapter(List<Song> songs, Context context, OnItemClick onItemClick) {
        this.songs = songs;
        this.context = context;
        this.onItemClick = onItemClick;

    }

    @NonNull
    @Override
    public mViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new mViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull mViewHolder holder, int position) {
        Song CurrentSong = songs.get(position);

        holder.songName.setText(CurrentSong.getSongTitle());
        if (CurrentSong.isFavourite()) {
            holder.isFavourite.setImageResource(R.drawable.ic_favorite);
        } else {
            holder.isFavourite.setImageResource(R.drawable.ic_favorite_border);
        }
        holder.songDuration.setText(CurrentSong.getSongDuration());

        holder.artistName.setText(CurrentSong.getSongArtistName());
        Uri coverUri = CurrentSong.getSongAlbumUri();

        if (coverUri != null) {
            holder.songCover.setImageURI(coverUri);

            if (holder.songCover.getDrawable() == null) {
                holder.songCover.setImageResource(R.drawable.ic_music);
            }

        }


    }


    @Override
    public int getItemCount() {
        return songs.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFilteredSongs(List<Song> filteredSongs) {
        songs.clear();
        songs.addAll(filteredSongs);
        notifyDataSetChanged();
    }



    public interface OnItemClick {
        void OnClick(int Position );
    }

    public class mViewHolder extends RecyclerView.ViewHolder {

        ImageView songCover, isFavourite;
        TextView songName, artistName, songDuration;
        View root;

        public mViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView;
            songCover = root.findViewById(R.id.song_cover);
            isFavourite = root.findViewById(R.id.favourite_song);
            songName = root.findViewById(R.id.song_name);
            songDuration = root.findViewById(R.id.song_duration);
            artistName = root.findViewById(R.id.artist_name);
            songName.setSelected(true);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getLayoutPosition();
                    if (onItemClick != null && position != RecyclerView.NO_POSITION) {
                        onItemClick.OnClick(position);
                    }

                }
            });
        }
    }
    public void setOnItemClick(OnItemClick onItemClick ){
        this.onItemClick = onItemClick;
    }


}

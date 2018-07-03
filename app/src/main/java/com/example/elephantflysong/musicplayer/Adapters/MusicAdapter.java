package com.example.elephantflysong.musicplayer.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.elephantflysong.musicplayer.Music.Music;
import com.example.elephantflysong.musicplayer.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by ElephantFlySong on 2018/6/14.
 */

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private ArrayList<Music> list;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public MusicAdapter(ArrayList<Music> list){
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_musics,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.setMusic(list.get(position));
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(position);
            }
        });
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListener.onItemLongClickListener(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public void setOnItemLongClickListern(OnItemLongClickListener listern){
        this.longClickListener = listern;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView image_cover;
        private TextView text_name;
        private TextView text_artist;
        public View view;

        public ViewHolder(View view){
            super(view);
            this.view = view;
            image_cover = view.findViewById(R.id.item_musics_image_cover);
            text_name = view.findViewById(R.id.item_musics_text_munsicName);
            text_artist = view.findViewById(R.id.item_musics_text_artist);
        }

        public void setMusic(Music music){
            image_cover.setBackgroundResource(R.drawable.music);
            text_name.setText(music.getName());
            text_artist.setText(music.getArtist());
        }
    }

    public void resetData(ArrayList<Music> data){
        this.list = data;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener{
        void onItemLongClickListener(int position);
    }
}

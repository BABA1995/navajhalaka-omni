package com.lib.videoplayer.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;


import com.lib.utility.util.CustomIntent;
import com.lib.videoplayer.R;
import com.lib.videoplayer.VideoApplication;
import com.lib.videoplayer.object.Movie;
import com.lib.videoplayer.object.MoviesList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aarokiax on 2/20/2017.
 */

public class MovieListRVAdapter extends BaseAdapter {

    private static Context mContext;
    LayoutInflater lInflater;

    private List<Movie> mDataList;
    private TextView text;
    private ImageView movieIcon;
    private MovieSelectCallback mMovieSelectcallback;
    private VideoThumbLoader mVideoThumbLoader = new VideoThumbLoader();

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;


    public MovieListRVAdapter(Context context,List<Movie>data){
        mContext=context;
        mDataList=data;
        this.mMovieSelectcallback = ((MovieSelectCallback) context);
        lInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = lInflater.inflate(R.layout.movie_item, parent, false);
        }
        Movie movie=getReport(position);

        text = (TextView)convertView.findViewById(R.id.movie_item_text);
        movieIcon = (ImageView)convertView.findViewById(R.id.movieicon);

        if(movie.getMovieName()!=null||!movie.getMovieName().equals("null")){
            text.setText(movie.getMovieName());
            text.setGravity(Gravity.CENTER);
            text.setSelected(true);
        }else {
            text.setText("");
        }

        movieIcon.setTag(mDataList.get(position).getMoviePath());
        mVideoThumbLoader.showThumbByAsynctack(mDataList.get(position).getMoviePath(),movieIcon);
        Bitmap bp= ThumbnailUtils.createVideoThumbnail(mDataList.get(position).getMoviePath(), MediaStore.Images.Thumbnails.MINI_KIND);
        movieIcon.setImageBitmap(bp);
        convertView.setTag(position);

        movieIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                VideoData.updateMovieSelection(mDataList.get(position).getMovieId());
                LocalBroadcastManager.getInstance(VideoApplication.getVideoContext()).sendBroadcast(new Intent(CustomIntent.ACTION_MOVIE_SELECTION_CHANGED));
                mMovieSelectcallback.onMovieSelected();
            }
        });
        return convertView;
    }

    Movie getReport(int position){
        return ((Movie)getItem(position));
    }

    public static interface MovieSelectCallback {
        void onMovieSelected();
    }
}

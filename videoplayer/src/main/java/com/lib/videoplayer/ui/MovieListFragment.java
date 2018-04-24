package com.lib.videoplayer.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lib.videoplayer.R;
import com.lib.videoplayer.object.Movie;
import com.lib.videoplayer.object.MoviesList;
import com.lib.videoplayer.util.MovieListRVAdapter;
import com.lib.videoplayer.util.MyGridView;
import com.lib.videoplayer.util.VideoData;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MovieListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieListFragment extends Fragment {
    private static final String TAG = MovieListFragment.class.getSimpleName();
    private View mRootView;
    private MovieListRVAdapter hindiadapter, englishadapter, kannadaadapter, tamiladapter;
    private GridView hindigv, englishgv, kannadagv, tamilgv;
    private List<MoviesList> movieList;
    private List<Movie>movies;
    private TextView emptyView, tvhindi, tvenglish, tvkannada, tvtamil;
    private LinearLayout llhindi, llenglish, llkannada, lltamil;
    private ProgressBar mProgressBar;



    public MovieListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoadingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MovieListFragment newInstance() {
        MovieListFragment fragment = new MovieListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieList = new ArrayList<MoviesList>();
        new LoadDataAsync().execute();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.movies_lang_item, container, false);


        emptyView = (TextView) mRootView.findViewById(R.id.empty_view);
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.loading);

        hindigv = (MyGridView) mRootView.findViewById(R.id.hindi_movie_list);
        englishgv = (MyGridView) mRootView.findViewById(R.id.english_movie_list);
        kannadagv = (MyGridView) mRootView.findViewById(R.id.kannada_movie_list);
        tamilgv = (MyGridView) mRootView.findViewById(R.id.tamil_movie_list);

        tvhindi = (TextView) mRootView.findViewById(R.id.tvhindi);
        tvenglish = (TextView) mRootView.findViewById(R.id.tvenglish);
        tvkannada = (TextView) mRootView.findViewById(R.id.tvkannada);
        tvtamil = (TextView) mRootView.findViewById(R.id.tvtamil);

        llhindi = (LinearLayout) mRootView.findViewById(R.id.llhindi);
        llenglish = (LinearLayout) mRootView.findViewById(R.id.llenglish);
        llkannada = (LinearLayout) mRootView.findViewById(R.id.llkanada);
        lltamil = (LinearLayout) mRootView.findViewById(R.id.lltamil);

        tvhindi.setVisibility(View.INVISIBLE);
        tvenglish.setVisibility(View.INVISIBLE);
        tvkannada.setVisibility(View.INVISIBLE);
        tvtamil.setVisibility(View.INVISIBLE);


        return mRootView;
    }


    private class LoadDataAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            movieList = VideoData.getMoviesList();
            for (int i = 0; i < movieList.size(); i++) {
                if (movieList.get(i).getLanguage().equals("Hindi") || movieList.get(i).getLanguage().equals("HINDI") || movieList.get(i).getLanguage().equals("hindi")) {
                    hindiadapter = new MovieListRVAdapter(getActivity(), movieList.get(i).getMovies());
                }
                if (movieList.get(i).getLanguage().equals("English") || movieList.get(i).getLanguage().equals("ENGLISH") || movieList.get(i).getLanguage().equals("english")) {
                    englishadapter = new MovieListRVAdapter(getActivity(), movieList.get(i).getMovies());
                }
                if (movieList.get(i).getLanguage().equals("Kanada") || movieList.get(i).getLanguage().equals("KANADA") || movieList.get(i).getLanguage().equals("kanada")) {
                    kannadaadapter = new MovieListRVAdapter(getActivity(), movieList.get(i).getMovies());
                }
                if (movieList.get(i).getLanguage().equals("Tamil") || movieList.get(i).getLanguage().equals("TAMIL") || movieList.get(i).getLanguage().equals("tamil")) {
                    tamiladapter = new MovieListRVAdapter(getActivity(), movieList.get(i).getMovies());
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mProgressBar.setVisibility(View.GONE);

            if (movieList.size() == 0) {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setGravity(Gravity.CENTER);
            }

            if (hindiadapter==null||hindiadapter.equals("null")||hindiadapter.isEmpty()) {
                ((ViewGroup) llhindi.getParent()).removeView(llhindi);
            } else {
                tvhindi.setVisibility(View.VISIBLE);
                hindigv.setAdapter(hindiadapter);
            }

            if (englishadapter==null||englishadapter.equals("null")||englishadapter.isEmpty()) {
                ((ViewGroup) llenglish.getParent()).removeView(llenglish);
            } else {
                tvenglish.setVisibility(View.VISIBLE);
                englishgv.setAdapter(englishadapter);

            }

            if (kannadaadapter==null||kannadaadapter.equals("null")||kannadaadapter.isEmpty()) {
                ((ViewGroup) llkannada.getParent()).removeView(llkannada);

            } else {
                tvkannada.setVisibility(View.VISIBLE);
                kannadagv.setAdapter(kannadaadapter);
            }

            if (tamiladapter==null||tamiladapter.equals("null")||tamiladapter.isEmpty()) {
                ((ViewGroup) lltamil.getParent()).removeView(lltamil);
            } else {
                tvtamil.setVisibility(View.VISIBLE);
                tamilgv.setAdapter(tamiladapter);

            }
        }
    }
}

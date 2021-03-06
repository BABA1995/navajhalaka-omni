package com.app.navajhalaka.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.app.navajhalaka.R;
import com.app.navajhalaka.app.BioscopeApp;
import com.app.navajhalaka.database.BusProvider;
import com.app.navajhalaka.model.BusDetails;
import com.app.navajhalaka.util.BusUtil;
import com.app.navajhalaka.util.ImagePagerAdapter;
import com.lib.location.ui.TopBannerFragment;
import com.lib.route.RouteApplication;
import com.lib.route.objects.Route;
import com.lib.route.util.RouteUtil;
import com.lib.utility.util.CustomIntent;
import com.lib.utility.util.Logger;
import com.lib.videoplayer.ui.VideoActivity;
import com.lib.videoplayer.util.StateMachine;
import com.lib.videoplayer.util.VideoData;

import static com.app.navajhalaka.database.BusProvider.CONTENT_URI_BUS_DETAIL_TABLE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
// to commit
public class HomeFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final long WAITING_TIME = 30 * 1000;//30 secs
    private ImageButton mPlayBottom;
    private View mRootView;
    private Handler mHandler;
    private StartVideoRunnable mRunnable;
    private ViewPager mViewPager;
    private ImagePagerAdapter mImagePagerAdapter;
    private BroadcastReceiver mReceiver;
    String comp;

    private TopBannerFragment topBannerFragment=new TopBannerFragment();

    private Handler mSlideHandler;
    public static final int DELAY = 10 * 1000;// 5 seconds
    private int page = 0;

    Runnable mSlideRunnable = new Runnable() {
        public void run() {
            if (mImagePagerAdapter.getCount() - 1 == page) {
                page = 0;
            } else {
                page++;
            }
            Log.d(TAG, "Page :: " + page);
            mViewPager.setCurrentItem(page, true);
            mSlideHandler.postDelayed(this, DELAY);
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.landing_fragment_layout, container, false);

        comp=getCompanyName(getContext());
        initView();
        topBannerFragment.setExtVisibility(comp);
        return mRootView;
    }

    public static String getCompanyName(Context context) {
        String Company = "";
        Cursor lCursor = null;
        try {
            lCursor = context.getContentResolver().query(CONTENT_URI_BUS_DETAIL_TABLE, null, null, null, null);
            while (null != lCursor && lCursor.moveToNext()) {
                Company = lCursor.getString(lCursor.getColumnIndex(BusProvider.COLUMNS.COMPANY_NAME));
                break;
            }
        } catch (Exception e) {

        } finally {
            if (null != lCursor && !lCursor.isClosed()) {
                lCursor.close();
            }
        }
        return Company;
    }


    private void initView() {
        Route defaultRoute = RouteUtil.getCurrentRoute(RouteApplication.getRouteContext());
        String routeId = null;
        if (null != defaultRoute) {
            routeId = defaultRoute.getmRouteId();
        }
        mImagePagerAdapter = new ImagePagerAdapter(getActivity(), RouteUtil.getImagesForRoute(routeId));
        mViewPager = (ViewPager) mRootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mImagePagerAdapter);
        mPlayBottom = (ImageButton) mRootView.findViewById(R.id.play);
        mPlayBottom.setOnClickListener(this);
        mHandler = new Handler();
        mSlideHandler = new Handler();
        mRunnable = new StartVideoRunnable();
        mReceiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CustomIntent.ACTION_ROUTE_IMAGE_DOWNLOAD_COMPLETE);
        intentFilter.addAction(CustomIntent.ACTION_ROUTE_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, intentFilter);
        if (null != getActivity() && null != getActivity().getWindow()) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPlayBottom.requestFocus();
        mSlideHandler.postDelayed(mSlideRunnable, DELAY);
        StartVideoTimer();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play:


                Intent lIntent = new Intent(getActivity(), VideoActivity.class);
                Bundle lBundle = new Bundle();

                if (TopBannerFragment.getDefaults("KEY_SELECTEDVIDEO", getContext()) == null) {
                    Logger.info("NullLoopEntered :", "insideIfLoop");
                    lBundle.putInt(CustomIntent.EXTRAS.VIDEO_STATE, StateMachine.VIDEO_STATE.MOVIE_AND_ADV);
                    lIntent.putExtra(CustomIntent.EXTRAS.VIDEO_STATE, lBundle);
                    startActivity(lIntent);
                    break;
                } else {
                    String videoPath = TopBannerFragment.getDefaults("KEY_SELECTEDVIDEO", getContext());
                    Logger.info("VideoPath", "InsideMainElse");
                    if (videoPath == "0") {
                        lBundle.putInt(CustomIntent.EXTRAS.VIDEO_STATE, StateMachine.VIDEO_STATE.MOVIE_AND_ADV);
                        lIntent.putExtra(CustomIntent.EXTRAS.VIDEO_STATE, lBundle);
                        startActivity(lIntent);
                        break;
                    } else {
                        Log.d("VideoPath", "InsideElseLoop");
                        lIntent.putExtra("VIDEO_PATH", videoPath);
                        lBundle.putInt(CustomIntent.EXTRAS.VIDEO_STATE, StateMachine.VIDEO_STATE.EXERNAL_HARD_DISK);
                        lIntent.putExtra(CustomIntent.EXTRAS.VIDEO_STATE, lBundle);
                        startActivity(lIntent);
                        break;

                    }
                }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
        mSlideHandler.removeCallbacks(mSlideRunnable);
    }


    public void StartVideoTimer() {
        mHandler.removeCallbacks(mSlideRunnable);
        mHandler.postDelayed(mRunnable, WAITING_TIME);
    }

    private class StartVideoRunnable implements Runnable {

        @Override
        public void run() {
            if (VideoData.isLandingVideoExist(BioscopeApp.getContext())) {
                Intent lIntent = new Intent(getActivity(), VideoActivity.class);
                Bundle lBundle = new Bundle();
                lBundle.putInt(CustomIntent.EXTRAS.VIDEO_STATE, StateMachine.VIDEO_STATE.ONLY_ADV);
                lIntent.putExtra(CustomIntent.EXTRAS.VIDEO_STATE, lBundle);
                startActivity(lIntent);
            } else {
                StartVideoTimer();
            }
        }
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent) {
                switch (intent.getAction()) {
                    case CustomIntent.ACTION_ROUTE_CHANGED:
                        Route route = RouteUtil.getCurrentRoute(RouteApplication.getRouteContext());
                        if (null != route) {
                            mImagePagerAdapter = new ImagePagerAdapter(getActivity(), RouteUtil.getImagesForRoute(route.getmRouteId()));
                            mViewPager.setAdapter(mImagePagerAdapter);
                        }
                        break;
                    case CustomIntent.ACTION_ROUTE_IMAGE_DOWNLOAD_COMPLETE:
                        String newRoute = intent.getStringExtra(CustomIntent.EXTRAS.ROUTE_ID);
                        Route defaultRoute = RouteUtil.getCurrentRoute(RouteApplication.getRouteContext());
                        Log.d(TAG, "ACTION_ROUTE_IMAGE_DOWNLOAD_COMPLETE :: newRoute " + newRoute + " defaultRouteId " + defaultRoute);
                        if (null != defaultRoute && null != newRoute && newRoute.equals(defaultRoute.getmRouteId())) {
                            mImagePagerAdapter = new ImagePagerAdapter(getActivity(), RouteUtil.getImagesForRoute(defaultRoute.getmRouteId()));
                            mViewPager.setAdapter(mImagePagerAdapter);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

}
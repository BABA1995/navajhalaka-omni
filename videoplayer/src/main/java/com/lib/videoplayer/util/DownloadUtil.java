package com.lib.videoplayer.util;


import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lib.location.util.NetworkUtil;
import com.lib.utility.util.CustomIntent;
import com.lib.utility.util.ExternalStorage;
import com.lib.utility.util.Logger;
import com.lib.videoplayer.VideoApplication;
import com.lib.videoplayer.database.VideoProvider;
import com.lib.videoplayer.object.Data;
import com.lib.videoplayer.object.DownloadData;

import java.io.File;

public class DownloadUtil {
    private static final String TAG = DownloadUtil.class.getSimpleName();
    private static final boolean DEBUG = true;

    /*
     * Method to start download
     * @param context
     * @param downloadUri
     * @param name
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static long beginDownload(Context context, String downloadUri, String dir, String name,Data data) {
        long downloadId = -1;
        if (NetworkUtil.isInternetAvailable(context)) {
            String status = VideoProvider.DOWNLOAD_STATUS.DOWNLOADING;
            File ext = ExternalStorage.getPath(context);
            File root = new File(ext + File.separator);
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri DownloadUri = Uri.parse(downloadUri);
            DownloadManager.Request request = new DownloadManager.Request(DownloadUri);
            request.setNotificationVisibility(1);
            FileUtil.createFolderIfRequired(root + dir);
            Uri path = Uri.withAppendedPath(Uri.fromFile(root), dir + name);
            Log.d(TAG, "--<SD CARD :: TEST > beginDownload :: path " + path);
            request.setDestinationUri(path);
            data.setPath(""+path);
            String absolutePath = data.getPath().substring(7);
            data.setPath(absolutePath);
            boolean fileExists = hasDownloadedFile(absolutePath);
            if (fileExists){
                status = VideoProvider.DOWNLOAD_STATUS.DOWNLOADED;
            }
            data.setDownloadStatus(status);
            //Enqueue a new download and same the referenceId
            downloadId = downloadManager.enqueue(request);
            if (status.equals(VideoProvider.DOWNLOAD_STATUS.DOWNLOADED)){
                downloadManager.remove(downloadId);
                Message lMessage = new Message();
                lMessage.what = VideoTaskHandler.TASK.HANDLE_DOWNLOADED_VIDEO;
                Bundle lBundle = new Bundle();
                lBundle.putLong(VideoTaskHandler.KEY.DOWNLOAD_ID, downloadId);
                lMessage.setData(lBundle);
                VideoTaskHandler.getInstance(context).sendMessage(lMessage);
            }
            else {
                LocalBroadcastManager.getInstance(VideoApplication.getVideoContext()).sendBroadcast(new Intent(CustomIntent.ACTION_DOWNLOAD_STARTED));

            }
        }
        Log.d(TAG, "beginDownload :: downloadId " + downloadId);
        return downloadId;
    }

    private static boolean hasDownloadedFile(String absolutePath) {
        if (Environment.isExternalStorageEmulated()) {
            File dir = new File(absolutePath);
            return dir.exists();
        }
        return false;
    }

    /*
     * Method to check the loaded file belongs to our application or not
     * @param context
     * @param downloadId
     * @return
     */
    public static boolean isValid(Context context, long downloadId) {
        String selection = VideoProvider.VIDEO_COLUMNS.DOWNLOADING_ID + " = ?";
        String[] selectionArg = new String[]{"" + downloadId};
        boolean lValue = false;
        Cursor lCursor = null;
        try {
            lCursor = context.getContentResolver().query(VideoProvider.CONTENT_URI_VIDEO_TABLE, null, selection, selectionArg, null);
            if (null != lCursor && lCursor.getCount() > 0) {
                lValue = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception isValid() ", e);
            lValue = false;
        } finally {
            if (null != lCursor && !lCursor.isClosed()) {
                lCursor.close();
            }
        }
        if (DEBUG) Log.d(TAG, "isValid() " + lValue);
        return lValue;
    }

    /*
     * Method to get the downloded file's path
     * @param context
     * @param downloadId
     * @return
     */
    public static DownloadData getDownloadedFileData(Context context, long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query myDownloadQuery = new DownloadManager.Query();
        myDownloadQuery.setFilterById(downloadId);
        Cursor cursor = null;
        DownloadData data = null;
        try {
            cursor = downloadManager.query(myDownloadQuery);
            if (null != cursor) {
                while (cursor.moveToNext()) {
                    String downloadedPath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    Logger.debug(TAG, "getDownloadedFileData downloadStatus " + downloadStatus + " downloadedPath " + downloadedPath);
                    data = new DownloadData(downloadedPath, downloadStatus);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception getDownloadedFileData ", e);
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return data;
    }

    /*
     * method to create uri for different type of video file
     * @param videoType
     * @return
     */
    public static String getDestinationDir(String videoType) {
        //TODO:
        switch (videoType) {
            case VideoProvider.VIDEO_TYPE.COMPANY_VIDEO:
            case VideoProvider.VIDEO_TYPE.COMPANY_AD: {
                return "/movie_bioscope/company/";
            }
            case VideoProvider.VIDEO_TYPE.SAFETY_VIDEO:
                return "/movie_bioscope/safety/";
            case VideoProvider.VIDEO_TYPE.MOVIE:
                return "/movie_bioscope/movie/";
            case VideoProvider.VIDEO_TYPE.ADV:
                return "/movie_bioscope/adv/";
            case VideoProvider.VIDEO_TYPE.BREAKING_VIDEO:
                return "/movie_bioscope/breaking_video/";
            case VideoProvider.VIDEO_TYPE.BREAKING_NEWS:
                return "/movie_bioscope/breaking_news/";
            case VideoProvider.VIDEO_TYPE.TRAILER:
            case VideoProvider.VIDEO_TYPE.COMEDY_SHOW:
            case VideoProvider.VIDEO_TYPE.SERIAL:
            case VideoProvider.VIDEO_TYPE.DEVOTIONAL:
            case VideoProvider.VIDEO_TYPE.SPORTS:
            case VideoProvider.VIDEO_TYPE.VIDEO_SONG:
                return "/movie_bioscope/landing_video/";
            case VideoProvider.VIDEO_TYPE.TICKER:
                return "/movie_bioscope/ticker/";
            default:
                return "/movie_bioscope/";

        }
    }
}

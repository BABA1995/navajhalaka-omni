 package com.lib.videoplayer.util;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.lib.utility.util.ExternalStorage;

import com.lib.utility.util.CustomIntent;
import com.lib.utility.util.Logger;
import com.lib.videoplayer.database.VideoProvider;
import com.lib.videoplayer.object.AdsSlotsData;
import com.lib.videoplayer.object.Asset;
import com.lib.videoplayer.object.Data;
import com.lib.videoplayer.object.DownloadData;
import com.lib.videoplayer.object.PushData;
import com.lib.videoplayer.object.SequenceCloudData;
import com.lib.videoplayer.object.SlotData;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class VideoTaskHandler extends Handler {
    private static final String TAG = VideoTaskHandler.class.getSimpleName();
    private static Context sContext;
    public String dFilePath;

    public interface KEY {
        String ROW_ID = "row_id";
        String DOWNLOAD_ID = "download_id";
    }

    public interface CLOUD_JSON {
        String DATA = "data";
        String ASSETS = "assets";
        String ACTION = "action";
    }

    public interface JSON_ACTION {
        String DOWNLOAD = "DOWNLOAD";
        String UPDATE = "UPDATE";
        String REFRESH = "REFRESH";
        String DELETE = "DELETE";
        String SEQUENCE = "SEQUENCE";
        String ADS_SLOTS_CONFIG = "SLOTS";
    }

    public interface TASK {
        int HANDLE_VIDEO_DATA = 1;
        int HANDLE_DOWNLOADED_VIDEO = 2;
        int BACK_GROUND_BREAKING_NEWS_SEARCH = 3;
        int HANDLE_ADS_SLOT_CONFIG = 4;
    }

    private static VideoTaskHandler sInstance;


    private VideoTaskHandler(Looper looper) {
        super(looper);
    }

    public static VideoTaskHandler getInstance(Context aContext) {
        sContext = aContext;
        if (null == sInstance) {
            synchronized (VideoTaskHandler.class) {
                if (null == sInstance) {
                    HandlerThread lThread = new HandlerThread(TAG);
                    lThread.start();
                    sInstance = new VideoTaskHandler(lThread.getLooper());
                }
            }
        }
        return sInstance;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Bundle lBundle = msg.getData();
        switch (msg.what) {
            case TASK.HANDLE_VIDEO_DATA:
                if (null != lBundle) {
                    String rowId = (String) lBundle.get(KEY.ROW_ID);
                    PushData pushData = VideoData.createVideoData(sContext, rowId);
                    if (null != pushData) {
                        Logger.info(TAG, "HANDLE_VIDEO_DATA :: action " + pushData.getAction() + " rowId " + rowId);
                        switch (pushData.getAction()) {
                            case JSON_ACTION.DOWNLOAD:
                                VideoData.createAndSendAcknowledgementData(pushData.getTransactionID(), "Received");
                                    if (pushData.getAssets().get(0).getType().equalsIgnoreCase("news_video") || pushData.getAssets().get(0).getType().equalsIgnoreCase("news_image") || pushData.getAssets().get(0).getType().equalsIgnoreCase("ticker")) {
                                        String epochtime = pushData.getCloudTime();
                                        long etime = Long.parseLong(epochtime);
                                        DateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss");
                                        String fdate = sdf.format(etime);
                                        Date date = null;
                                        try {
                                            date = (Date) sdf.parseObject(fdate);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        Date curdate = new Date();
                                        SimpleDateFormat sdf2 = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss");
                                        String strDate = (sdf2.format(curdate.getTime() - 30 * 60 * 1000));
                                        Date curtime = null;
                                        try {
                                            curtime = (Date) sdf2.parseObject(strDate);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        long difftime = date.getTime() - curtime.getTime();
                                        if (difftime > 0) {
                                            downloadContent(pushData);
                                        }
                                    }else {
                                        downloadContent(pushData);
                                    }

                                break;
                            case JSON_ACTION.REFRESH:
                                //TODO:
                                break;
                            case JSON_ACTION.UPDATE:
                                VideoData.createAndSendAcknowledgementData(pushData.getTransactionID(), "Received");
                                String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
                                Log.d(TAG, "SD CARD ::" + sdcard);
                                VideoData.deleteRecursive(new File(sdcard + DownloadUtil.getDestinationDir(VideoProvider.VIDEO_TYPE.MOVIE) + "/"));
                                Log.d(TAG, "SD CARD ::" + sdcard + DownloadUtil.getDestinationDir(VideoProvider.VIDEO_TYPE.MOVIE));
                                VideoData.deleteRecursive(new File(sdcard + DownloadUtil.getDestinationDir(VideoProvider.VIDEO_TYPE.ADV) + "/"));
                                VideoData.deleteRecursive(new File(sdcard + DownloadUtil.getDestinationDir(VideoProvider.VIDEO_TYPE.BREAKING_NEWS) + "/"));
                                VideoData.deleteRecursive(new File(sdcard + DownloadUtil.getDestinationDir(VideoProvider.VIDEO_TYPE.BREAKING_VIDEO) + "/"));
                                VideoData.deleteRecursive(new File(sdcard + DownloadUtil.getDestinationDir(VideoProvider.VIDEO_TYPE.TRAILER) + "/"));
                                VideoData.deleteRecursive(new File(sdcard + DownloadUtil.getDestinationDir(VideoProvider.VIDEO_TYPE.DEVOTIONAL) + "/"));
                                VideoData.deleteRecursive(new File(sdcard + DownloadUtil.getDestinationDir(VideoProvider.VIDEO_TYPE.COMEDY_SHOW) + "/"));
                                VideoData.deleteRecursive(new File(sdcard + DownloadUtil.getDestinationDir(VideoProvider.VIDEO_TYPE.SERIAL) + "/"));
                                VideoData.deleteRecursive(new File(sdcard + DownloadUtil.getDestinationDir(VideoProvider.VIDEO_TYPE.SPORTS) + "/"));
                                VideoData.deleteRecursive(new File(sdcard + DownloadUtil.getDestinationDir(VideoProvider.VIDEO_TYPE.VIDEO_SONG) + "/"));
                                VideoData.deleteAllVideoDataExceptCompanyAndSafety();
                                downloadContent(pushData);
                                break;
                            case JSON_ACTION.DELETE:
                                List<Asset> assetsList = pushData.getAssets();
                                for (Asset asset : assetsList) {
                                    VideoData.deleteFileIfNotPlaying(asset);
                                }
                                break;
                            case JSON_ACTION.SEQUENCE:
                                Map<String, List<SequenceCloudData>> map = VideoData.createSequenceData(rowId);
                                for (String s : map.keySet()) {
                                    SequenceUtil.deleteSequence(s);
                                    for (SequenceCloudData data : map.get(s)) {
                                        SequenceUtil.insertSequence(s, data.getValue(), data.getOrder(), SequenceUtil.NOT_SELECTED, data.getCount());
                                    }
                                }
                                break;
                        }
                        break;
                    }
                }
                break;
            case TASK.HANDLE_DOWNLOADED_VIDEO:
                long downloadId = lBundle.getLong(KEY.DOWNLOAD_ID);
                if (DownloadUtil.isValid(sContext, downloadId)) {
                    DownloadData downloadData = DownloadUtil.getDownloadedFileData(sContext, downloadId);
                    if (null != downloadData && DownloadManager.STATUS_SUCCESSFUL == downloadData.getDownloadStatus()) {
                        //Successful download
                        Data data = VideoData.getVideoDataFrom(sContext, String.valueOf(downloadId));
                        if (data != null) {
                            data.setDownloadingId(String.valueOf(downloadId));
                            data.setPath(downloadData.getPath());
                            data.setDownloadStatus(VideoProvider.DOWNLOAD_STATUS.DOWNLOADED);
                            data.setLastPlayedTime(String.valueOf(System.currentTimeMillis()));
                            VideoData.insertOrUpdateVideoData(sContext, data);
                            VideoData.createAndSendAcknowledgementData(data.getTransactionId(), data.getAssetID(), "Downloaded");
                            Intent intent = new Intent(CustomIntent.ACTION_MEDIA_DOWNLOAD_COMPLETE);
                            intent.putExtra(CustomIntent.EXTRAS.VIDEO_ID, data.getAssetID());
                            intent.putExtra(CustomIntent.EXTRAS.TYPE, data.getType());
                            LocalBroadcastManager.getInstance(sContext).sendBroadcast(intent);
                        }
                    }
                } else {
                    //failed download
                    Data data = VideoData.getVideoDataFrom(sContext, String.valueOf(downloadId));
                    if (null != data) {
                        data.setDownloadStatus(VideoProvider.DOWNLOAD_STATUS.FAILED);
                        VideoData.insertOrUpdateVideoData(sContext, data);
                    }
                }
                break;
            case TASK.BACK_GROUND_BREAKING_NEWS_SEARCH:
                VideoData.backgroundSearchForBreaking(sContext);
                break;
            case TASK.HANDLE_ADS_SLOT_CONFIG: {
                if (null != lBundle) {
                    String rowId = (String) lBundle.get(KEY.ROW_ID);
                    AdsSlotsData data = VideoData.createAdsSlotsData(sContext, rowId);
                    if (null != data) {
                        Logger.info(TAG, "HANDLE_ADS_SLOT_CONFIG :: action " + data.getAction() + " rowId " + rowId);
                        switch (data.getAction()) {
                            case JSON_ACTION.ADS_SLOTS_CONFIG:
                                if (null != data.getSlot()) {
                                    SlotData slot = data.getSlot();
                                    if (null != slot && null != slot.getSlotType() && null != slot.getSlotsPerHour() && null != slot.getAdsPerSlot()) {
                                        AdsSlotConfigUtil.insertAdsSlotsConfiguration(slot.getSlotType(), Integer.parseInt(slot.getSlotsPerHour()), Integer.parseInt(slot.getAdsPerSlot()));
                                    }
                                }
                                VideoData.createAndSendAcknowledgementData(data.getTransactionID(), "Received");
                                break;
                        }
                    }
                    break;
                }
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void downloadContent(PushData pushData) {
        for (Asset asset : pushData.getAssets()) {
            Data data = copyAssetToData(asset);
           String path = DownloadUtil.getDestinationDir(asset.getType());
            //check is there any entry with the same assert id then ignore it .may be its a duplicate message
            if (!VideoData.isAssetExist(sContext, asset.getAssetID())) {
                if (null != asset.getType() && asset.getType().equalsIgnoreCase("ticker")) {
                    VideoData.deleteAllTicker();
                }
                long lDownloadId = DownloadUtil.beginDownload(sContext, asset.getUrl(), DownloadUtil.getDestinationDir(asset.getType()), asset.getName(),data);
                DownloadData downloadData = DownloadUtil.getDownloadedFileData(sContext, lDownloadId);data.setDownloadingId(String.valueOf(lDownloadId));
                data.setMessage(pushData.getContent());
                data.setDownloadingId(String.valueOf(lDownloadId));
                data.setTransactionId(pushData.getTransactionID());
                data.setCloudTime(pushData.getCloudTime());
                data.setReceivedTime(pushData.getReceivedTime());
                VideoData.insertOrUpdateVideoData(sContext, data);
            } else {
                boolean movieOrVideo = path.contains("/landing_video/")||path.contains("/movie/");
                if (!movieOrVideo){
                Logger.info(TAG, "HANDLE_VIDEO_DATA :: DOWNLOAD:: already exist in the table::  asset id " + asset.getAssetID());
                try {
                    dFilePath = VideoData.getAssetPath(asset.getAssetID());
                    if (dFilePath == null) {
                        dFilePath = DownloadUtil.getDestinationDir(asset.getType()) + "/" + asset.getName();
                    }
                    VideoData.deleteFileById(asset.getAssetID());
                } catch (Exception e) {
                    Logger.info("DeleteFilePath", dFilePath);
                }
                if (null != asset.getType() && asset.getType().equalsIgnoreCase("ticker")) {
                    VideoData.deleteAllTicker();
                }
                long lDownloadId = DownloadUtil.beginDownload(sContext, asset.getUrl(), DownloadUtil.getDestinationDir(asset.getType()), asset.getName(),data);
                //       DownloadData downloadData = DownloadUtil.getDownloadedFileData(sContext, lDownloadId);

                data.setMessage(pushData.getContent());
                data.setDownloadingId(String.valueOf(lDownloadId));
                data.setTransactionId(pushData.getTransactionID());
                data.setCloudTime(pushData.getCloudTime());
                data.setReceivedTime(pushData.getReceivedTime());
                VideoData.insertOrUpdateVideoData(sContext, data);
            }
        }
        }
    }

    private Data copyAssetToData(Asset asset) {
        Data data = new Data();
        data.setAssetID(asset.getAssetID());
        data.setName(asset.getName());
        data.setUrl(asset.getUrl());
        data.setLanguage(asset.getLanguage());
        data.setType(asset.getType());
        data.setPriority(asset.getPriority());
        return data;
    }

    }
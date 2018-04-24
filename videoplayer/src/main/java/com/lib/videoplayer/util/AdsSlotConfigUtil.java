package com.lib.videoplayer.util;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.lib.utility.util.Logger;
import com.lib.videoplayer.VideoApplication;
import com.lib.videoplayer.database.VideoProvider;

/**
 * Created by intel on 5/8/17.
 */

public class AdsSlotConfigUtil {
    private static final String TAG = AdsSlotConfigUtil.class.getSimpleName();
    public static int DEFAULT_SLOTS_PER_HOUR_COUNT = 1;
    public static int DEFAULT_ADS_PER_SLOT_COUNT = 1;


    public interface SLOT_TYPE {
        String LANDING_SLOT_TYPE = "landing";
    }

    public static Uri insertAdsSlotsConfiguration(String slotType, int slotsPerHourCount, int adsPerSlotCount) {
        String lSelection = VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.SLOT_TYPE + "= ?";
        String[] lSelectionArg = {slotType};
        ContentValues values = new ContentValues();
        Uri uri;
        try {
            values.put(VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.SLOT_TYPE, slotType);
            values.put(VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.SLOTS_PER_HOUR_COUNT, slotsPerHourCount);
            values.put(VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.ADS_PER_SLOT_COUNT, adsPerSlotCount);
            int updateCount = VideoApplication.getVideoContext().getContentResolver().update(VideoProvider.CONTENT_URI_ADS_SLOTS_CONFIG, values, lSelection, lSelectionArg);
            Logger.debug(TAG, "insertAdsSlotsConfiguration() :: update count  " + updateCount);
            if (updateCount == 0) {
                uri = VideoApplication.getVideoContext().getContentResolver().insert(VideoProvider.CONTENT_URI_ADS_SLOTS_CONFIG, values);
                Logger.debug(TAG, "insertAdsSlotsConfiguration() :: uri  " + uri);
            }
        } catch (Exception e) {
        }
        return VideoProvider.CONTENT_URI_ADS_SLOTS_CONFIG;
    }

    public static int getSlotsPerHourCount(String slotType) {
        String selection = VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.SLOT_TYPE + "= ?";
        String[] selectionArg = {slotType};
        Cursor cursor = null;
        int slotsPerHourCount = Integer.parseInt(String.valueOf((VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.SLOTS_PER_HOUR_COUNT).indexOf(0)));
        try {
            cursor = VideoApplication.getVideoContext().getContentResolver().query(VideoProvider.CONTENT_URI_ADS_SLOTS_CONFIG, null, selection, selectionArg, null);
            while (null != cursor && cursor.moveToNext()) {
                slotsPerHourCount = cursor.getInt(cursor.getColumnIndex(VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.SLOTS_PER_HOUR_COUNT));
                if (slotsPerHourCount==-1){
                    slotsPerHourCount=DEFAULT_SLOTS_PER_HOUR_COUNT;
                }
                break;
            }
        } catch (Exception e) {
            Logger.error(TAG, "Exception :: getSlotsPerHourCount() :: ", e);
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return slotsPerHourCount ;
    }

    public static int getAdsPerSlotCount(String slotType) {
        String selection = VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.SLOT_TYPE + "= ?";
        String[] selectionArg = {slotType};
        Cursor cursor = null;
        int adsPerSloCount = Integer.parseInt(String.valueOf((VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.ADS_PER_SLOT_COUNT).indexOf(0)));
        try {
            cursor = VideoApplication.getVideoContext().getContentResolver().query(VideoProvider.CONTENT_URI_ADS_SLOTS_CONFIG, null, selection, selectionArg, null);
            while (null != cursor && cursor.moveToNext()) {
                adsPerSloCount = cursor.getInt(cursor.getColumnIndex(VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.ADS_PER_SLOT_COUNT));
                if (adsPerSloCount==-1){
                    adsPerSloCount=DEFAULT_ADS_PER_SLOT_COUNT;
                }
                break;
            }
        } catch (Exception e) {
            Logger.error(TAG, "Exception :: getAdsPerSlotCount() :: ", e);
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return adsPerSloCount;
    }

    public static void deleteAdsSlotsConfigData() {

        try {
            int deleteCount = VideoApplication.getVideoContext().getContentResolver().delete(VideoProvider.CONTENT_URI_ADS_SLOTS_CONFIG, null, null);
            Logger.debug(TAG, "Count :: deleteAdsSlotsConfigData() :: " + deleteCount);
        } catch (Exception e) {
            Logger.error(TAG, "Exception :: deleteAdsSlotsConfigData() :: ", e);
        }
    }


    public static boolean isLast(String slotType) {
        String selection = VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.SLOT_TYPE + "= ?";
        String[] selectionArg = {slotType};
        Cursor cursor = null;
        boolean isLast = true;
        try {
            cursor = VideoApplication.getVideoContext().getContentResolver().query(VideoProvider.CONTENT_URI_ADS_SLOTS_CONFIG, null, selection, selectionArg, null);
            if (null != cursor && cursor.moveToNext()) {
                int adPerSlot = cursor.getInt(cursor.getColumnIndex(VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.ADS_PER_SLOT_COUNT));
                int currentRunningCount = cursor.getInt(cursor.getColumnIndex(VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.CURRENT_RUNNING_AD_COUNT));
                Logger.debug(TAG, "--<AD:: DEBUG > isLast() :: adPerSlot " + adPerSlot + " currentRunningCount " + currentRunningCount);
                if (adPerSlot > currentRunningCount) {
                    isLast = false;
                }
            }
        } catch (Exception e) {
            Logger.error(TAG, "Exception :: isLast() :: ", e);
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        Logger.debug(TAG, "--<AD:: DEBUG > isLast() ::" + isLast);
        return isLast;
    }

    public static int resetCurrentRunningCount(String slotType) {
        String selection = VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.SLOT_TYPE + "= ?";
        String[] selectionArg = {slotType};
        ContentValues content = new ContentValues();
        content.put(VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.CURRENT_RUNNING_AD_COUNT, 0);
        int count = VideoApplication.getVideoContext().getContentResolver().update(VideoProvider.CONTENT_URI_ADS_SLOTS_CONFIG, content, selection, selectionArg);
        Logger.debug(TAG, "--<AD:: DEBUG > resetCurrentRunningCount() :: rows count " + count);
        return count;
    }

    public static void updateCurrentRunningCount(String slotType) {
        String selection = VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.SLOT_TYPE + "= ?";
        String[] selectionArg = {slotType};
        Cursor cursor = null;
        int currentCount = 0;
        try {
            cursor = VideoApplication.getVideoContext().getContentResolver().query(VideoProvider.CONTENT_URI_ADS_SLOTS_CONFIG, null, selection, selectionArg, null);
            while (null != cursor && cursor.moveToNext()) {
                currentCount = cursor.getInt(cursor.getColumnIndex(VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.CURRENT_RUNNING_AD_COUNT));
                break;
            }
        } catch (Exception e) {
            Logger.error(TAG, "Exception :: updateCurrentRunningCount() :: ", e);
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        Log.d(TAG, "--<AD:: DEBUG > updateCurrentRunningCount :: currentCount " + currentCount + 1);
        ContentValues content = new ContentValues();
        content.put(VideoProvider.ADS_SLOTS_CONFIG_COLUMNS.CURRENT_RUNNING_AD_COUNT, currentCount + 1);
        int count = VideoApplication.getVideoContext().getContentResolver().update(VideoProvider.CONTENT_URI_ADS_SLOTS_CONFIG, content, selection, selectionArg);
        Logger.debug(TAG, "updateCurrentRunningCount() :: rows count " + count);
    }
}

package com.robj.notificationhelperlibrary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.robj.notificationhelperlibrary.models.PendingNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressLint("NewApi")
public abstract class BaseNotificationListener extends NotificationListenerService {

    private ArrayList<PendingNotification> pending = new ArrayList<>();
    private ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private List<Integer> notifHandled = new ArrayList<>();
    private Map<Integer, Integer> previouslyDismissed = new HashMap<>();

    private ArrayList<String> duplicateReplyPackages = new ArrayList<>();
    private String lastRemovedKey = "";

    protected void onEnabled(boolean enabled) {
        NotificationListenerUtils.setListenerEnabled(this, enabled);
    }

    private void initDupeReplyList() {
//        duplicateReplyPackages.add("com.google.android.talk3");
//        duplicateReplyPackages.add("com.google.android.apps.messaging");
        duplicateReplyPackages.add("com.whatsapp");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initDupeReplyList();
        onEnabled(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onEnabled(false);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        onEnabled(true);
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        onEnabled(false);
    }

    @Override
    public IBinder onBind(Intent mIntent) {
        IBinder mIBinder = super.onBind(mIntent);
        onEnabled(true);
        return mIBinder;
    }

    @Override
    public boolean onUnbind(Intent mIntent) {
        boolean mOnUnbind = super.onUnbind(mIntent);
        onEnabled(false);
        return mOnUnbind;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        if(shouldBeIgnored(sbn)) {
            if(BuildConfig.DEBUG) {
                Bundle extras = NotificationCompat.getExtras(sbn.getNotification());
                String title = NotificationContentUtils.getTitle(extras);
                String msg = NotificationContentUtils.getMessage(extras);
                Log.d(getClass().getSimpleName(), "Ignoring potential duplicate from " + sbn.getPackageName() + ":\n" + title + "\n" + msg);
            }
            return;
        }

        if(shouldAppBeAnnounced(sbn, rankingMap))
            handleSbn(sbn);
    }

    private boolean shouldBeIgnored(StatusBarNotification sbn) {
        if(!duplicateReplyPackages.contains(sbn.getPackageName()))
            return false;
        int hashCode = getHashCode(sbn);
        return notifHandled.indexOf(hashCode) > -1 || previouslyDismissed.containsValue(hashCode);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if(shouldBeIgnored(sbn))
            return;

        if (shouldAppBeAnnounced(sbn))
            handleSbn(sbn);
    }

    private int getHashCode(StatusBarNotification sbn) {
        Bundle extras = NotificationCompat.getExtras(sbn.getNotification());
        String title = NotificationContentUtils.getTitle(extras);
        String msg = NotificationContentUtils.getMessage(extras);
        return (title + msg + sbn.getPackageName()).hashCode();
    }

    private void handleSbn(StatusBarNotification sbn) {
        if(!lastRemovedKey.equals(getKey(sbn)))
            postDelayed(sbn);
        lastRemovedKey = "";
    }

    private void postDelayed(StatusBarNotification sbn) {
        PendingNotification pn = new PendingNotification(sbn);
        int index = pending.indexOf(pn);
        if (index >= 0) {
            boolean remove = false;
            if (NotificationCompat.isGroupSummary(sbn.getNotification())) {
                pending.get(index).setDismissKey(pn.getDismissKey());
                return;
            } else if (NotificationCompat.isGroupSummary(pending.get(index).getSbn().getNotification())) {
                pn.setDismissKey(pending.get(index).getDismissKey());
                remove = true;
            }
            if (remove) {
                pending.get(index).getScheduledFuture().cancel(false);
                pending.remove(index);
            }
        }

        Runnable task = new Runnable() {
            public void run() {
                if (pending.size() > 0) {
                    PendingNotification pn = pending.get(0);
                    pending.remove(0);
                    if(duplicateReplyPackages.contains(pn.getSbn().getPackageName()))
                        notifHandled.add(getHashCode(pn.getSbn()));
                    onNotifcationPosted(pn.getSbn(), pn.getDismissKey());
                }
            }
        };
        ScheduledFuture<?> scheduledFuture = worker.schedule(task, 200, TimeUnit.MILLISECONDS);
        pn.setScheduledFuture(scheduledFuture);
        pending.add(pn);
    }

    @Override
    public void onNotificationRemoved(final StatusBarNotification sbn) {
        if(duplicateReplyPackages.contains(sbn.getPackageName())) {
            int hashCode = getHashCode(sbn);
            int indexOf = notifHandled.indexOf(hashCode);
            if (indexOf > -1) {
                notifHandled.remove(indexOf);
                Bundle extras = NotificationCompat.getExtras(sbn.getNotification());
                String title = NotificationContentUtils.getTitle(extras);
                if(TextUtils.isEmpty(title))
                    return;
                int titleHashcode = title.hashCode();
                if(previouslyDismissed.containsKey(titleHashcode))
                    previouslyDismissed.put(titleHashcode, hashCode);
                else {
                    if (previouslyDismissed.size() >= 5)
                        previouslyDismissed.remove(previouslyDismissed.size() - 1);
                    previouslyDismissed.put(titleHashcode, hashCode);
                }
            }
        }
    }

    private String getKey(StatusBarNotification sbn) {
        return AppUtils.isLollipop() ? sbn.getKey() : String.valueOf(sbn.getId());
    }

    protected boolean shouldAppBeAnnounced(StatusBarNotification sbn, RankingMap rankingMap) {
        return shouldAppBeAnnounced(sbn);
    }
    protected abstract boolean shouldAppBeAnnounced(StatusBarNotification sbn);
    protected abstract void onNotifcationPosted(StatusBarNotification sbn, String dismissKey);

}
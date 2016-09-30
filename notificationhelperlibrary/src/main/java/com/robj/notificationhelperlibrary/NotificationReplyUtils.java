package com.robj.notificationhelperlibrary;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import com.robj.notificationhelperlibrary.models.Action;
import com.robj.notificationhelperlibrary.models.RemoteInputParcel;

import java.util.ArrayList;

/**
 * Created by Rob J on 30/09/2016.
 */

public class NotificationReplyUtils {

    private static final CharSequence REPLY_KEYWORD = "reply";

    /** Parses the notification to see if it contains a quick reply action **/
    public static Action getQuickReplyAction(Notification n, String packageName) {
        NotificationCompat.Action action = null;
        if(Build.VERSION.SDK_INT >= 24)
            action = getQuickReplyAction(n);
        if(action == null)
            action = getWearReplyAction(n);
        if(action == null)
            return null;
        return new Action(action, packageName, true);
    }

    /** Parses Marshmallow notificationf for in line quick reply **/
    private static NotificationCompat.Action getQuickReplyAction(Notification n) {
        for(int i = 0; i < NotificationCompat.getActionCount(n); i++) {
            NotificationCompat.Action action = NotificationCompat.getAction(n, i);
            if(action.title.toString().toLowerCase().contains(REPLY_KEYWORD))
                for(int x = 0; x < action.getRemoteInputs().length; x++) {
                    RemoteInput remoteInput = action.getRemoteInputs()[x];
                    if(remoteInput.getLabel().toString().toLowerCase().contains(REPLY_KEYWORD))
                        return action;
                    if(remoteInput.getResultKey().toLowerCase().contains(REPLY_KEYWORD))
                        return action;
                }
        }
        return null;
    }

    /** Parses Android Wear notification for reply options **/
    private static NotificationCompat.Action getWearReplyAction(Notification n) {
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender(n);
        for (NotificationCompat.Action action : wearableExtender.getActions())
            if(action.title.toString().toLowerCase().contains(REPLY_KEYWORD))
                return action;
        //TODO: Check for remoteinputs too like getQuickReplyAction??
        return null;
    }

    public static void sendReply(Context context, Action action, String msg) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        //TODO: Should only be one remote input use remoteIn.getLabel() to check for reply maybe??
        for (RemoteInputParcel remoteIn : action.getRemoteInputs()) {
            Log.i("", "RemoteInput: " + remoteIn.getLabel());
            bundle.putCharSequence(remoteIn.getResultKey(), msg);
        }

        int size = action.getRemoteInputs().size();
        RemoteInputParcel input;
        ArrayList<RemoteInput> actualInputs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            input = action.getRemoteInputs().get(i);
            RemoteInput.Builder builder = new RemoteInput.Builder(input.getResultKey());
            builder.setLabel(input.getLabel());
            builder.setChoices(input.getChoices());
            builder.setAllowFreeFormInput(input.isAllowFreeFormInput());
            builder.addExtras(input.getExtras());
            actualInputs.add(builder.build());
        }

        RemoteInput[] inputs = actualInputs.toArray(new RemoteInput[actualInputs.size()]);
        RemoteInput.addResultsToIntent(inputs, intent, bundle);
        try {
            action.getPendingIntent().send(context, 0, intent);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

}

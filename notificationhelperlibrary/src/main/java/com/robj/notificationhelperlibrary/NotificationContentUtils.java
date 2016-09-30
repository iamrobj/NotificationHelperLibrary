package com.robj.notificationhelperlibrary;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.robj.notificationhelperlibrary.models.NotificationIds;

/**
 * Created by Rob J on 30/09/2016.
 */

public class NotificationContentUtils {

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getTitle(Bundle extras) {
        Log.d("NOTIFICATIONUTILS", "Getting title from extras..");
        String msg = extras.getString(Notification.EXTRA_TITLE);
        Log.d("Title Big", "" + extras.getString(Notification.EXTRA_TITLE_BIG));
        return msg;
    }

    public static String getTitle(ViewGroup localView) {
        Log.d("NOTIFICATIONUTILS", "Getting title..");
        String msg = null;
        TextView tv = (TextView) localView.findViewById(NotificationIds.getInstance(localView.getContext()).TITLE);
        if (tv != null)
            msg = tv.getText().toString();
        return msg;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getMessage(Bundle extras) {
        Log.d("NOTIFICATIONUTILS", "Getting message from extras..");
        Log.d("Text", "" + extras.getCharSequence(Notification.EXTRA_TEXT));
        Log.d("Big Text", "" + extras.getCharSequence(Notification.EXTRA_BIG_TEXT));
        Log.d("Title Big", "" + extras.getCharSequence(Notification.EXTRA_TITLE_BIG));
//        Log.d("Text lines", "" + extras.getCharSequence(Notification.EXTRA_TEXT_LINES));
        Log.d("Info text", "" + extras.getCharSequence(Notification.EXTRA_INFO_TEXT));
        Log.d("Info text", "" + extras.getCharSequence(Notification.EXTRA_INFO_TEXT));
        Log.d("Subtext", "" + extras.getCharSequence(Notification.EXTRA_SUB_TEXT));
        Log.d("Summary", "" + extras.getString(Notification.EXTRA_SUMMARY_TEXT));
        CharSequence chars = extras.getCharSequence(Notification.EXTRA_TEXT);
        if(!TextUtils.isEmpty(chars))
            return chars.toString();
        else if(!TextUtils.isEmpty((chars = extras.getString(Notification.EXTRA_SUMMARY_TEXT))))
            return chars.toString();
        else
            return null;
    }

    public static String getMessage(ViewGroup localView) {
        Log.d("NOTIFICATIONUTILS", "Getting message..");
        String msg = null;
        TextView tv = (TextView) localView.findViewById(NotificationIds.getInstance(localView.getContext()).BIG_TEXT);
        if (tv != null && !TextUtils.isEmpty(tv.getText()))
            msg = tv.getText().toString();
        if (TextUtils.isEmpty(msg)) {
            tv = (TextView) localView.findViewById(NotificationIds.getInstance(localView.getContext()).TEXT);
            if (tv != null)
                msg = tv.getText().toString();
        }
        return msg;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getExtended(Bundle extras, ViewGroup v) {
        Log.d("NOTIFICATIONUTILS", "Getting message from extras..");

        CharSequence[] lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
        if(lines != null && lines.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (CharSequence msg : lines)
//                msg = msg.toString();//.replaceAll("(\\s+$|^\\s+)", "").replaceAll("\n+", "\n");
                if (!TextUtils.isEmpty(msg)) {
                    sb.append(msg.toString());
                    sb.append('\n');
                }
            return sb.toString().trim();
        }
        CharSequence chars = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        if(!TextUtils.isEmpty(chars))
            return chars.toString();
        else if(!AppUtils.isJellyBeanMR2())
            return getExtended(v);
        else
            return getMessage(extras);
    }

    public static String getExtended(ViewGroup localView) {
        Log.d("NOTIFICATIONUTILS", "Getting extended message..");
        String msg = "";
        NotificationIds notificationIds = NotificationIds.getInstance(localView.getContext());
        TextView tv = (TextView) localView.findViewById(notificationIds.EMAIL_0);
        if (tv != null && !TextUtils.isEmpty(tv.getText()))
            msg += tv.getText().toString() + '\n';
        tv = (TextView) localView.findViewById(notificationIds.EMAIL_1);
        if (tv != null && !TextUtils.isEmpty(tv.getText()))
            msg += tv.getText().toString() + '\n';
        tv = (TextView) localView.findViewById(notificationIds.EMAIL_2);
        if (tv != null && !TextUtils.isEmpty(tv.getText()))
            msg += tv.getText().toString() + '\n';
        tv = (TextView) localView.findViewById(notificationIds.EMAIL_3);
        if (tv != null && !TextUtils.isEmpty(tv.getText()))
            msg += tv.getText().toString() + '\n';
        tv = (TextView) localView.findViewById(notificationIds.EMAIL_4);
        if (tv != null && !TextUtils.isEmpty(tv.getText()))
            msg += tv.getText().toString() + '\n';
        tv = (TextView) localView.findViewById(notificationIds.EMAIL_5);
        if (tv != null && !TextUtils.isEmpty(tv.getText()))
            msg += tv.getText().toString() + '\n';
        tv = (TextView) localView.findViewById(notificationIds.EMAIL_6);
        if (tv != null && !TextUtils.isEmpty(tv.getText()))
            msg += tv.getText().toString() + '\n';
//        tv = (TextView) localView.findViewById(notificationIds.INBOX_MORE);
//        if (tv != null && !TextUtils.isEmpty(tv.getText()))
//            msg += tv.getText().toString() + '\n';
        if (msg.isEmpty())
            msg = getExpandedText(localView);
        if (msg.isEmpty())
            msg = getMessage(localView);
        return msg.trim();
    }

    @SuppressLint("NewApi")
    public static ViewGroup getLocalView(Context context, Notification n)
    {
        RemoteViews view = null;
        if(Build.VERSION.SDK_INT >= 16) { view = n.bigContentView; }

        if (view == null)
        {
            view = n.contentView;
        }
        ViewGroup localView = null;
        try
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            localView = (ViewGroup) inflater.inflate(view.getLayoutId(), null);
            view.reapply(context, localView);
        } catch (Exception exp) { }
        return localView;
    }

    //OLD METHOD
    public static String getExpandedText(ViewGroup localView)
    {
        NotificationIds notificationIds = NotificationIds.getInstance(localView.getContext());

        String text = "";
        if (localView != null)
        {
            View v;
            // try to get big text
            v = localView.findViewById(notificationIds.big_notification_content_text);
            if (v != null && v instanceof TextView)
            {
                String s = ((TextView)v).getText().toString();
                if (!s.equals(""))
                {
                    // add title string if available
                    View titleView = localView.findViewById(android.R.id.title);
                    if (v != null && v instanceof TextView)
                    {
                        String title = ((TextView)titleView).getText().toString();
                        if (!title.equals(""))
                            text = title + " " + s;
                        else
                            text = s;
                    }
                    else
                        text = s;
                }
            }

            // try to extract details lines
            v = localView.findViewById(notificationIds.inbox_notification_event_10_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    if (!s.equals(""))
                        text += s.toString();
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_9_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_8_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_7_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_6_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_5_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_4_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_3_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_2_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_1_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            if (text.equals("")) //Last resort for Kik
            {
                // get title string if available
                View titleView = localView.findViewById(notificationIds.notification_title_id );
                View bigTitleView = localView.findViewById(notificationIds.big_notification_title_id );
                View inboxTitleView = localView.findViewById(notificationIds.inbox_notification_title_id );
                if (titleView  != null && titleView  instanceof TextView)
                {
                    text += ((TextView)titleView).getText() + " - ";
                } else if (bigTitleView != null && bigTitleView instanceof TextView)
                {
                    text += ((TextView)titleView).getText();
                } else if  (inboxTitleView != null && inboxTitleView instanceof TextView)
                {
                    text += ((TextView)titleView).getText();
                }

                v = localView.findViewById(notificationIds.notification_subtext_id);
                if (v != null && v instanceof TextView)
                {
                    CharSequence s = ((TextView)v).getText();
                    if (!s.equals(""))
                    {
                        text += s.toString();
                    }
                }
            }

        }
        return text.trim();
    }

}

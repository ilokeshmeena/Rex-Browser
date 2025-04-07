package com.rexvit.browser.utils;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import com.rexvit.browser.R;

public class Task {
    public static final String ICON_TRANSITION = "%";
    private static final String MARKET_DETAILS_ID = "market://details?id=";
    private static final String PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=";
    private static final String TEXT = "Check out " + ", the free app for Bass Equalizer  " ;
    private static final String POLICY = "https://policy.com";
    public static  Context mContext;
    public static String mail;
    public static String appId;
    public static void RateApp(Context context, String str) {
        xmethod(context, "details?id=" + str);
    }

    public static void MoreApps(Context context, String str) {
        xmethod(context, "developer?id=" + str);
    }

    public static void Feedback(Context context) {
        try {
            Intent intent = new Intent("android.intent.action.SENDTO");
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra("android.intent.extra.EMAIL", new String[]{context.getString(R.string.email_dev)});
            intent.putExtra("android.intent.extra.SUBJECT", context.getString(R.string.contact_partnership) + " " + context.getString(R.string.app_name));
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_email)));
            intent.setType("text/plain");
        } catch (ActivityNotFoundException unused) {
            Toast.makeText(context, (int) R.string.no_email_app, Toast.LENGTH_SHORT).show();
        }
    }
    public static void getPolicy(Context context, String appId) {
        Task.mContext = context;
        Task.appId = appId;
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.link_policy))));

        } catch (ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.link_policy))));
        }
    }


    public static void ShareApp(Context context, String str, String str2, String str3) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.putExtra("android.intent.extra.SUBJECT", str2);
        intent.putExtra("android.intent.extra.TEXT", str3 + ": " + ("https://play.google.com/store/apps/details?id=" + str));
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_with)));
    }

    public static void ShareUrl(Context context, String str) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.putExtra("android.intent.extra.TEXT", str);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_with)));
    }

    public static void CopyText(Context context, String str) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData newPlainText = ClipData.newPlainText(context.getString(R.string.copied), str);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(newPlainText);
            showtoast(context, context.getString(R.string.copied));
        }
    }

    @SuppressLint({"InlinedApi", "WrongConstant"})
    private static void xmethod(Context context, String str) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://" + str));
        intent.addFlags(1208483840);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/" + str)));
        }
    }

    private static void showtoast(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }
}

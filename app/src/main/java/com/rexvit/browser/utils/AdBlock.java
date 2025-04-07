package com.rexvit.browser.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import com.rexvit.browser.app.BrowserApp;
import com.rexvit.browser.database.AdBlockDb;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton

public class AdBlock {
    private static String BLOCKED_DOMAINS_LIST_FILE_NAME = "hosts.txt";
    private static final String COMMENT = "#";
    private static final String LOCALHOST = "localhost";
    private static final String LOCAL_IP_V4 = "127.0.0.1";
    private static final String LOCAL_IP_V4_ALT = "0.0.0.0";
    private static final String LOCAL_IP_V6 = "::1";
    private static final String SPACE = " ";
    private static final String TAB = "\t";
    private static final String TAG = "AdBlock";
    public static boolean mBlockAds;
    Context c;
    private final HashSet mBlockedDomainsList = new HashSet();

    @Inject
    public AdBlock(@NonNull Context context) {
        this.c = context;
        BrowserApp.getAppComponent().inject(this);
        if (this.mBlockedDomainsList.isEmpty()) {
            loadHostsFile(context);
        }
        mBlockAds = Preference.adBlock(context);
    }

    public void updatePreference() {
        mBlockAds = Preference.adBlock(this.c);
    }

    public boolean isAd(@Nullable String str) {
        if (mBlockAds && str != null) {
            try {
                boolean contains = this.mBlockedDomainsList.contains(getDomainName(str));
                if (contains) {
                    saveBlockedDomains(str);
                }
                return contains;
            } catch (URISyntaxException e) {
                Log.d(TAG, "URL '" + str + "' is invalid", e);
            }
        }
        return false;
    }

    @NonNull
    private static String getDomainName(@NonNull String str) throws URISyntaxException {
        int indexOf = str.indexOf(47, 8);
        if (indexOf != -1) {
            str = str.substring(0, indexOf);
        }
        String host = new URI(str).getHost();
        return host == null ? str : host.startsWith("www.") ? host.substring(4) : host;
    }

    private void loadHostsFile(@NonNull final Context context) {
        final IOException[] ioe = new IOException[1];
        final Throwable[] throwable = new Throwable[1];
        BrowserApp.getTaskThread().execute(() -> {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open(AdBlock.BLOCKED_DOMAINS_LIST_FILE_NAME)));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.isEmpty() && !line.startsWith(AdBlock.COMMENT)) {
                        String replace = line.replace(AdBlock.LOCAL_IP_V4, "")
                                .replace(AdBlock.LOCAL_IP_V4_ALT, "")
                                .replace(AdBlock.LOCAL_IP_V6, "")
                                .replace(AdBlock.TAB, "");
                        int commentIndex = replace.indexOf(AdBlock.COMMENT);
                        if (commentIndex >= 0) {
                            replace = replace.substring(0, commentIndex);
                        }
                        String trimmed = replace.trim();
                        if (!trimmed.isEmpty() && !trimmed.equals(AdBlock.LOCALHOST)) {
                            String[] domains = trimmed.split(AdBlock.SPACE);
                            for (String domain : domains) {
                                AdBlock.this.mBlockedDomainsList.add(domain.trim());
                            }
                        }
                    }
                }
            } catch (IOException e) {
                ioe[0] = e;
                Log.wtf(AdBlock.TAG, "Reading blocked domains list from file '" + AdBlock.BLOCKED_DOMAINS_LIST_FILE_NAME + "' failed.", e);
            } finally {
                Utils.close(bufferedReader);
            }
        });
    }
    public static void saveBlockedDomains(String str) {
        String upperCase;
        if (Utils.getTitleForSearchBar(str).contains("https://www.")) {
            upperCase = Character.toString(Utils.getTitleForSearchBar(str).charAt(12)).toUpperCase();
        } else if (Utils.getTitleForSearchBar(str).contains("http://www.")) {
            upperCase = Character.toString(Utils.getTitleForSearchBar(str).charAt(11)).toUpperCase();
        } else if (Utils.getTitleForSearchBar(str).contains("www.")) {
            upperCase = Character.toString(Utils.getTitleForSearchBar(str).charAt(4)).toUpperCase();
        } else if (Utils.getTitleForSearchBar(str).contains("http://m.")) {
            upperCase = Character.toString(Utils.getTitleForSearchBar(str).charAt(9)).toUpperCase();
        } else if (Utils.getTitleForSearchBar(str).contains("https://m.")) {
            upperCase = Character.toString(Utils.getTitleForSearchBar(str).charAt(10)).toUpperCase();
        } else {
            upperCase = Character.toString(Utils.getTitleForSearchBar(str).charAt(0)).toUpperCase();
        }
        if (str != null) {
            try {
                new AdBlockDb(upperCase, Utils.getTitleForSearchBar(str), str, Utils.randomColors()).save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

package com.rexvit.browser.downloads;

import android.net.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebAddress {
    static Pattern sAddressPattern = Pattern.compile("(?:(http|https|file)\\:\\/\\/)?(?:([-A-Za-z0-9$_.+!*'(),;?&=]+(?:\\:[-A-Za-z0-9$_.+!*'(),;?&=]+)?)@)?([a-zA-Z0-9 -\ud7ff豈-﷏ﷰ-\uffef%_-][a-zA-Z0-9 -\ud7ff豈-﷏ﷰ-\uffef%_\\.-]*|\\[[0-9a-fA-F:\\.]+\\])?(?:\\:([0-9]*))?(\\/?[^#]*)?.*", 2);
    private String mAuthInfo;
    private String mHost;
    private String mPath;
    private int mPort;
    private String mScheme;

    public WebAddress(String str) throws ParseException {
        if (str == null) {
            throw new NullPointerException();
        }
        this.mScheme = "";
        this.mHost = "";
        this.mPort = -1;
        this.mPath = "/";
        this.mAuthInfo = "";
        Matcher matcher = sAddressPattern.matcher(str);
        if (matcher.matches()) {
            String group = matcher.group(1);
            if (group != null) {
                this.mScheme = group.toLowerCase(Locale.ROOT);
            }
            String group2 = matcher.group(2);
            if (group2 != null) {
                this.mAuthInfo = group2;
            }
            String group3 = matcher.group(3);
            if (group3 != null) {
                this.mHost = group3;
            }
            String group4 = matcher.group(4);
            if (group4 != null && group4.length() > 0) {
                try {
                    try {
                        this.mPort = Integer.parseInt(group4);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (NumberFormatException unused) {
                    try {
                        throw new Exception("Bad port");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            String group5 = matcher.group(5);
            if (group5 != null && group5.length() > 0) {
                if (group5.charAt(0) == '/') {
                    this.mPath = group5;
                } else {
                    this.mPath = "/" + group5;
                }
            }
        } else {
            try {
                throw new Exception("Bad address");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        if (this.mPort == 443 && this.mScheme.equals("")) {
            this.mScheme = "https";
        } else if (this.mPort == -1) {
            if (this.mScheme.equals("https")) {
                this.mPort = 443;
            } else {
                this.mPort = 80;
            }
        }
        if (this.mScheme.equals("")) {
            this.mScheme = "http";
        }
    }

    public String toString() {
        String str;
        String str2 = "";
        if ((this.mPort == 443 || !this.mScheme.equals("https")) && (this.mPort == 80 || !this.mScheme.equals("http"))) {
            str = "";
        } else {
            str = ":" + this.mPort;
        }
        if (this.mAuthInfo.length() > 0) {
            str2 = this.mAuthInfo + "@";
        }
        return this.mScheme + "://" + str2 + this.mHost + str + this.mPath;    }

    public String getScheme() {
        return this.mScheme;
    }

    public void setScheme(String str) {
        this.mScheme = str;
    }

    public String getHost() {
        return this.mHost;
    }

    public void setHost(String str) {
        this.mHost = str;
    }

    public int getPort() {
        return this.mPort;
    }

    public void setPort(int i) {
        this.mPort = i;
    }

    public String getPath() {
        return this.mPath;
    }

    public void setPath(String str) {
        this.mPath = str;
    }

    public String getAuthInfo() {
        return this.mAuthInfo;
    }

    public void setAuthInfo(String str) {
        this.mAuthInfo = str;
    }
}

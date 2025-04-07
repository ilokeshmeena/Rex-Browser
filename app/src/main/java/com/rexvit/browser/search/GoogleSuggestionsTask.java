package com.rexvit.browser.search;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.rexvit.browser.Interface.SuggestionsResult;
import com.rexvit.browser.R;
import com.rexvit.browser.database.HistoryItem;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


class GoogleSuggestionsTask extends BaseSuggestionsTask {
    private static final String ENCODING = "ISO-8859-1";
    @Nullable
    private static XmlPullParser sXpp;

    @Override 
    protected String getEncoding() {
        return ENCODING;
    }

    
    public GoogleSuggestionsTask(@NonNull String str, @NonNull Application application, @NonNull SuggestionsResult suggestionsResult) {
        super(str, application, suggestionsResult);
    }

    @Override 
    @NonNull
    protected String getQueryUrl(@NonNull String str, @NonNull String str2) {
        return "https://suggestqueries.google.com/complete/search?output=toolbar&hl=" + str2 + "&q=" + str;
    }

    @Override 
    protected void parseResults(FileInputStream fileInputStream, List<HistoryItem> list) throws Exception {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        XmlPullParser parser = getParser();
        parser.setInput(bufferedInputStream, ENCODING);
        int i = 0;
        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
            if (eventType == 2 && "suggestion".equals(parser.getName())) {
                String attributeValue = parser.getAttributeValue(null, "data");
                list.add(new HistoryItem(attributeValue, attributeValue, R.drawable.ic_search_home));
                i++;
                if (i >= 10) {
                    return;
                }
            }
        }
    }

    @NonNull
    private static synchronized XmlPullParser getParser() throws XmlPullParserException {
        XmlPullParser xmlPullParser;
        synchronized (GoogleSuggestionsTask.class) {
            if (sXpp == null) {
                XmlPullParserFactory newInstance = XmlPullParserFactory.newInstance();
                newInstance.setNamespaceAware(true);
                sXpp = newInstance.newPullParser();
            }
            xmlPullParser = sXpp;
        }
        return xmlPullParser;
    }
}

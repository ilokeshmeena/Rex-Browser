package com.rexvit.browser.Interface;

import androidx.annotation.NonNull;
import com.rexvit.browser.database.HistoryItem;

import java.util.List;


public interface SuggestionsResult {
    void resultReceived(@NonNull List<HistoryItem> list);
}

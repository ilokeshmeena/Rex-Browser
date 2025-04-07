package com.rexvit.browser.Interface;


public interface TabsView {
    void tabAdded();

    void tabChanged(int i);

    void tabRemoved(int i);

    void tabsInitialized();
}

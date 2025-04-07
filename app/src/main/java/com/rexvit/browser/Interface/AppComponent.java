package com.rexvit.browser.Interface;

import com.rexvit.browser.activities.MainActivity;
import com.rexvit.browser.adapter.SuggestionsAdapter;
import com.rexvit.browser.app.AppModule;
import com.rexvit.browser.app.BrowserApp;
import com.rexvit.browser.app.BrowserPresenter;
import com.rexvit.browser.downloads.DownloadStart;
import com.rexvit.browser.fragment.TabsFragment;
import com.rexvit.browser.manager.TabsManager;
import com.rexvit.browser.utils.AdBlock;
import com.rexvit.browser.view.BrowserView;
import com.rexvit.browser.view.webClient.WebClient;

import dagger.Component;
import javax.inject.Singleton;

@Component(modules = {AppModule.class})
@Singleton

public interface AppComponent {
    void inject(MainActivity mainActivity);

    void inject(SuggestionsAdapter suggestionsAdapter);

    void inject(BrowserApp browserApp);

    void inject(BrowserPresenter browserPresenter);

    void inject(DownloadStart downloadStart);

    void inject(TabsFragment tabsFragment);

    void inject(TabsManager tabsManager);

    void inject(AdBlock adBlock);

    void inject(BrowserView browserView);

    void inject(WebClient webClient);
}

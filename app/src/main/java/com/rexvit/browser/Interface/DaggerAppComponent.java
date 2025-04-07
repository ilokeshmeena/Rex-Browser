package com.rexvit.browser.Interface;

import com.rexvit.browser.activities.MainActivity;
import com.rexvit.browser.activities.MainActivity_MembersInjector;
import com.rexvit.browser.adapter.SuggestionsAdapter;
import com.rexvit.browser.adapter.SuggestionsAdapter_MembersInjector;
import com.rexvit.browser.app.AppModule;
import com.rexvit.browser.app.AppModule_ProvideApplicationFactory;
import com.rexvit.browser.app.AppModule_ProvideBusFactory;
import com.rexvit.browser.app.AppModule_ProvideContextFactory;
import com.rexvit.browser.app.BrowserApp;
import com.rexvit.browser.app.BrowserApp_MembersInjector;
import com.rexvit.browser.app.BrowserPresenter;
import com.rexvit.browser.app.BrowserPresenter_MembersInjector;
import com.rexvit.browser.database.HistoryDatabase;
import com.rexvit.browser.database.HistoryDatabase_Factory;
import com.rexvit.browser.downloads.DownloadStart;
import com.rexvit.browser.fragment.TabsFragment;
import com.rexvit.browser.fragment.TabsFragment_MembersInjector;
import com.rexvit.browser.manager.TabsManager;
import com.rexvit.browser.manager.TabsManager_MembersInjector;
import com.rexvit.browser.utils.AdBlock;
import com.rexvit.browser.utils.AdBlock_Factory;
import com.rexvit.browser.view.BrowserView_MembersInjector;
import com.rexvit.browser.view.BrowserView;
import com.rexvit.browser.view.webClient.WebClient;
import com.rexvit.browser.view.webClient.WebClient_MembersInjector;

import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import javax.inject.Provider;


public final class DaggerAppComponent implements AppComponent {
    private Provider<AdBlock> adBlockProvider;
    private AppModule appModule;
    private Provider<HistoryDatabase> historyDatabaseProvider;
    private AppModule_ProvideContextFactory provideContextProvider;

    @Override 
    public void inject(DownloadStart downloadStart) {
    }

    @Override 
    public void inject(AdBlock adBlock) {
    }

    private DaggerAppComponent(Builder builder) {
        initialize(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    private void initialize(Builder builder) {
        this.appModule = builder.appModule;
        this.provideContextProvider = AppModule_ProvideContextFactory.create(builder.appModule);
        this.historyDatabaseProvider = DoubleCheck.provider(HistoryDatabase_Factory.create(this.provideContextProvider));
        this.adBlockProvider = DoubleCheck.provider(AdBlock_Factory.create(this.provideContextProvider));
    }

    @Override 
    public void inject(MainActivity mainActivity) {
        injectMainActivity(mainActivity);
    }

    @Override 
    public void inject(TabsFragment tabsFragment) {
        injectTabsFragment(tabsFragment);
    }

    @Override 
    public void inject(BrowserView browserView) {
        injectBrowserView(browserView);
    }

    @Override 
    public void inject(BrowserApp browserApp) {
        injectBrowserApp(browserApp);
    }

    @Override 
    public void inject(WebClient webClient) {
        injectWebClient(webClient);
    }

    @Override 
    public void inject(BrowserPresenter browserPresenter) {
        injectBrowserPresenter(browserPresenter);
    }

    @Override 
    public void inject(TabsManager tabsManager) {
        injectTabsManager(tabsManager);
    }

    @Override 
    public void inject(SuggestionsAdapter suggestionsAdapter) {
        injectSuggestionsAdapter(suggestionsAdapter);
    }

    private MainActivity injectMainActivity(MainActivity mainActivity) {
        MainActivity_MembersInjector.injectMEventBus(mainActivity, AppModule_ProvideBusFactory.proxyProvideBus(this.appModule));
        MainActivity_MembersInjector.injectMHistoryDatabase(mainActivity, this.historyDatabaseProvider.get());
        return mainActivity;
    }

    private TabsFragment injectTabsFragment(TabsFragment tabsFragment) {
        TabsFragment_MembersInjector.injectMBus(tabsFragment, AppModule_ProvideBusFactory.proxyProvideBus(this.appModule));
        return tabsFragment;
    }

    private BrowserView injectBrowserView(BrowserView browserView) {
        BrowserView_MembersInjector.injectMEventBus(browserView, AppModule_ProvideBusFactory.proxyProvideBus(this.appModule));
        return browserView;
    }

    private BrowserApp injectBrowserApp(BrowserApp browserApp) {
        BrowserApp_MembersInjector.injectMBus(browserApp, AppModule_ProvideBusFactory.proxyProvideBus(this.appModule));
        return browserApp;
    }

    private WebClient injectWebClient(WebClient webClient) {
        WebClient_MembersInjector.injectMAdBlock(webClient, this.adBlockProvider.get());
        return webClient;
    }

    private BrowserPresenter injectBrowserPresenter(BrowserPresenter browserPresenter) {
        BrowserPresenter_MembersInjector.injectMEventBus(browserPresenter, AppModule_ProvideBusFactory.proxyProvideBus(this.appModule));
        return browserPresenter;
    }

    private TabsManager injectTabsManager(TabsManager tabsManager) {
        TabsManager_MembersInjector.injectMHistoryManager(tabsManager, this.historyDatabaseProvider.get());
        TabsManager_MembersInjector.injectMEventBus(tabsManager, AppModule_ProvideBusFactory.proxyProvideBus(this.appModule));
        TabsManager_MembersInjector.injectMApp(tabsManager, AppModule_ProvideApplicationFactory.proxyProvideApplication(this.appModule));
        return tabsManager;
    }

    private SuggestionsAdapter injectSuggestionsAdapter(SuggestionsAdapter suggestionsAdapter) {
        SuggestionsAdapter_MembersInjector.injectMDatabaseHandler(suggestionsAdapter, this.historyDatabaseProvider.get());
        return suggestionsAdapter;
    }

    
    public static final class Builder {
        private AppModule appModule;

        private Builder() {
        }

        public AppComponent build() {
            if (this.appModule == null) {
                throw new IllegalStateException(AppModule.class.getCanonicalName() + " must be set");
            }
            return new DaggerAppComponent(this);
        }

        public Builder appModule(AppModule appModule) {
            this.appModule = (AppModule) Preconditions.checkNotNull(appModule);
            return this;
        }
    }
}

package com.rexvit.browser.view.webClient;

import com.rexvit.browser.utils.AdBlock;
import dagger.MembersInjector;

import javax.inject.Inject;
import javax.inject.Provider;


public final class WebClient_MembersInjector implements MembersInjector<WebClient> {
    private final Provider<AdBlock> mAdBlockProvider;
    @Inject
    public WebClient_MembersInjector(Provider<AdBlock> provider) {
        this.mAdBlockProvider = provider;
    }

    public static MembersInjector<WebClient> create(Provider<AdBlock> provider) {
        return new WebClient_MembersInjector(provider);
    }

    @Override
    public void injectMembers(WebClient webClient) {
        injectMAdBlock(webClient, this.mAdBlockProvider.get());
    }

    public static void injectMAdBlock(WebClient webClient, AdBlock adBlock) {
        webClient.mAdBlock = adBlock;
    }
}

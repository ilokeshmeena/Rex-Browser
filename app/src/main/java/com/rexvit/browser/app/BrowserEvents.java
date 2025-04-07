package com.rexvit.browser.app;


public final class BrowserEvents {
    private BrowserEvents() {
    }

    
    public static final class OpenUrlInNewTab {
        public final Location location;
        public final String url;

        
        public enum Location {
            NEW_TAB,
            BACKGROUND,
            INCOGNITO
        }

        public OpenUrlInNewTab(String str) {
            this.url = str;
            this.location = Location.NEW_TAB;
        }

        public OpenUrlInNewTab(String str, Location location) {
            this.url = str;
            this.location = location;
        }
    }
}

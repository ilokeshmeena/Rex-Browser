package com.rexvit.browser.utils;


public class Preconditions {
    public static void checkNonNull(Object obj) {
        if (obj == null) {
            throw new RuntimeException("Object must not be null");
        }
    }
}

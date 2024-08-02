package com.mocicarazvan.userservice.crypt;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CodeVerifierStore {
    private static final ConcurrentMap<String, String> store = new ConcurrentHashMap<>();

    public static void storeCodeVerifier(String state, String codeVerifier) {
        store.put(state, codeVerifier);
    }

    public static String getCodeVerifier(String state) {
        return store.remove(state);
    }
}
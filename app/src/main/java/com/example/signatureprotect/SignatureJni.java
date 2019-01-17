package com.example.signatureprotect;

/**
 * Created by chang on 2019/1/17.
 */

public class SignatureJni {
    static {
        System.loadLibrary("SignatureJni");
    }

    public static native boolean isEqual(String val);
}

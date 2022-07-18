package com.futu.openapi;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;

public class FTAPI {
    public static void init() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void unInit() {


    }
}

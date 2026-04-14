package com.juliocodex.capacitor.ironsource;

import com.getcapacitor.Logger;

public class IronSource {

    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }
}

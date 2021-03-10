package com.starburstdata.openjson;

public class JSONOptions {
    private boolean isCaseInsensitive = true;

    public boolean isCaseInsensitive() {
        return isCaseInsensitive;
    }

    public JSONOptions setCaseInsensitive(boolean caseInsensitive) {
        isCaseInsensitive = caseInsensitive;
        return this;
    }

    public static JSONOptions globalOptions = new JSONOptions();
}

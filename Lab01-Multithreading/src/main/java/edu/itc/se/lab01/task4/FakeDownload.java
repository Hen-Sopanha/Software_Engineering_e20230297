package edu.itc.se.lab01.task4;

import java.io.IOException;
import java.util.concurrent.Callable;

public class FakeDownload implements Callable<String> {
    private final String url;
    private final boolean fail;
    private final long delayMs;

    public FakeDownload(String url, boolean fail, long delayMs) {
        this.url = url;
        this.fail = fail;
        this.delayMs = delayMs;
    }

    @Override
    public String call() throws Exception {
        Thread.sleep(delayMs);
        if (fail) throw new IOException("boom");
        return url + " -> " + url.length();
    }
}

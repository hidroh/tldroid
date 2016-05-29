package io.github.hidroh.tldroid;

import java.io.IOException;
import java.io.InputStream;

import okio.Okio;

class Utils {
    static String readUtf8(InputStream inputStream) throws IOException {
        return Okio.buffer(Okio.source(inputStream)).readUtf8();
    }
}

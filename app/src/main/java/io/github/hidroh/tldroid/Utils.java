package io.github.hidroh.tldroid;

import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okio.Okio;

public class Utils {
    public static String readUtf8(InputStream inputStream) throws IOException {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            // Okio uses some Java String APIs that are only available in later Android versions
            return Okio.buffer(Okio.source(inputStream)).readUtf8();
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
        return sb.toString();
    }
}

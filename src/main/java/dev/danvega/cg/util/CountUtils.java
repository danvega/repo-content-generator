package dev.danvega.cg.util;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;

public class CountUtils {

    public static int countTokens(String text) {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Encoding enc = registry.getEncoding(EncodingType.CL100K_BASE);
        return enc.encode(text).size();
    }

    public static String humanReadableByteCount(String text) {

        long bytes = text == null ? 0 : text.length();
        if (bytes < 1024) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String unit = "KMGTPE".charAt(exp-1) + "B";

        return String.format("%.1f %s", bytes / Math.pow(1024, exp), unit);
    }
}

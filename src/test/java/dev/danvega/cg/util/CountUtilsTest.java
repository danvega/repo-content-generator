package dev.danvega.cg.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountUtilsTest {

    @Test
    public void countTokensNull() {
        int tokenCount = CountUtils.countTokens(null);
        assertEquals(0, tokenCount);
    }

    @Test
    public void countTokens() {
        int tokenCount = CountUtils.countTokens("hello world");
        assertEquals(2, tokenCount);
    }

    @Test
    public void countTokensZeroLength() {
        int tokenCount = CountUtils.countTokens("");
        assertEquals(0, tokenCount);
    }

    @Test
    public void humanReadableByteCount() {
        String byteCount = CountUtils.humanReadableByteCount("hello world");
        assertEquals("11 B", byteCount);
    }

    @Test
    public void humanReadableByteCountZeroLength() {
        String byteCount = CountUtils.humanReadableByteCount("");
        assertEquals("0 B", byteCount);
    }

    @Test
    public void humanReadableByteCountNull() {
        String byteCount = CountUtils.humanReadableByteCount(null);
        assertEquals("0 B", byteCount);
    }
}

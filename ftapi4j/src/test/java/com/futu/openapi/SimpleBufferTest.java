package com.futu.openapi;

import org.junit.*;
import static org.junit.Assert.*;

public class SimpleBufferTest {

    @Test
    public void testCreate() {
        final int limit = 10;
        SimpleBuffer buffer = new SimpleBuffer(limit);
        assertEquals(buffer.start, 0);
        assertEquals(buffer.length, 0);
        assertEquals(buffer.limit, limit);
        assertEquals(buffer.buf.length, limit);
    }

    @Test
    public void testFirstAppend() {
        final int limit = 10;
        byte[] data = {1,2};
        SimpleBuffer buffer = new SimpleBuffer(limit);
        buffer.append(data, 0, data.length);
        assertEquals(buffer.start, 0);
        assertEquals(buffer.length, 2);
        for (int i = 0; i < data.length; i++) {
            assertEquals(buffer.buf[i], data[i]);
        }
    }

    @Test
    public void testSecondAppend() {
        final int limit = 10;
        byte[] data = {3,4};
        SimpleBuffer buffer = new SimpleBuffer(limit);
        buffer.buf[0] = 1;
        buffer.buf[1] = 2;
        buffer.length = 2;
        buffer.append(data, 1, 1);
        assertEquals(buffer.length, 3);
        assertEquals(buffer.start, 0);
        assertEquals(buffer.buf[0], 1);
        assertEquals(buffer.buf[1], 2);
        assertEquals(buffer.buf[2], 4);
    }

    @Test
    public void testCompact() {
        final int limit = 10;
        SimpleBuffer buffer = new SimpleBuffer(limit);
        buffer.start = 3;
        buffer.buf[buffer.start] = 1;
        buffer.buf[buffer.start+1] = 2;
        buffer.length = 2;

        buffer.compact();
        assertEquals(buffer.start, 0);
        assertEquals(buffer.length, 2);
        assertEquals(buffer.buf[0], 1);
        assertEquals(buffer.buf[1], 2);
    }

    @Test
    public void testAppendCompact() {
        final int limit = 10;
        SimpleBuffer buffer = new SimpleBuffer(limit);
        buffer.start = 3;
        buffer.length = 5;
        for (int i = buffer.start; i < buffer.start + buffer.length; ++i) {
            buffer.buf[i] = (byte)i;
        }
        byte[] data = {10, 11, 12};
        buffer.append(data, 0, data.length);
        assertEquals(buffer.start, 0);
        assertEquals(buffer.length, 8);
        for (int i = 0; i < 5; i++) {
            assertEquals(buffer.buf[i], i+3);
        }
        assertEquals(buffer.buf[5], 10);
        assertEquals(buffer.buf[6], 11);
        assertEquals(buffer.buf[7], 12);
    }

    @Test
    public void testAppendTooMany() {
        final int limit = 10;
        SimpleBuffer buffer = new SimpleBuffer(limit);
        buffer.start = 3;
        buffer.length = 5;
        for (int i = buffer.start; i < buffer.start + buffer.length; ++i) {
            buffer.buf[i] = (byte)i;
        }
        byte[] data = {10, 11, 12, 13, 14, 15, 16};
        int len = buffer.append(data, 0, data.length);
        assertEquals(len, 5);
        assertEquals(buffer.start, 0);
        assertEquals(buffer.length, 10);
        for (int i = 0; i < 5; i++) {
            assertEquals(buffer.buf[i], i+3);
        }
        assertEquals(buffer.buf[5], 10);
        assertEquals(buffer.buf[6], 11);
        assertEquals(buffer.buf[7], 12);
        assertEquals(buffer.buf[8], 13);
        assertEquals(buffer.buf[9], 14);
    }

    @Test
    public void testResizeSmaller() {
        final int limit = 10;
        SimpleBuffer buffer = new SimpleBuffer(limit);
        byte[] data = {1,2,3, 4, 5};
        buffer.append(data, 0, data.length);
        buffer.resize(3);
        assertEquals(buffer.start, 0);
        assertEquals(buffer.length, 3);
        assertEquals(buffer.limit, 3);
        assertEquals(buffer.buf[0], 1);
        assertEquals(buffer.buf[1], 2);
        assertEquals(buffer.buf[2], 3);
    }

    @Test
    public void testResizeLarger()
    {
        final int limit = 10;
        SimpleBuffer buffer = new SimpleBuffer(limit);
        byte[] data = {1,2,3, 4, 5};
        buffer.append(data, 0, data.length);
        buffer.resize(12);
        assertEquals(buffer.limit, 12);
        assertEquals(buffer.length, 5);
        assertEquals(buffer.buf.length, 12);
        for (int i = 0; i < buffer.length; i++) {
            assertEquals(buffer.buf[i], data[i]);
        }
        for (int i = 5; i < buffer.limit; i++) {
            assertEquals(buffer.buf[i], 0);
        }
    }
}

package ru.melod1n.vk.io;

import java.io.ByteArrayOutputStream;

public class BytesOutputStream extends ByteArrayOutputStream {

    public BytesOutputStream() {
        super(8192);
    }

    public BytesOutputStream(int size) {
        super(size);
    }

    public byte[] getByteArray() {
        return buf;
    }
}
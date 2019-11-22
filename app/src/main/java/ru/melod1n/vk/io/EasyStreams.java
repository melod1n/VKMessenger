package ru.melod1n.vk.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EasyStreams {

    public static final int BUFFER_SIZE = 8192;
    public static final int CHAR_BUFFER_SIZE = 4096;

    private EasyStreams() {
    }

    public static String read(InputStream from) throws IOException {
        return read(from, UTF_8);
    }

    public static String read(InputStream from, Charset encoding) throws IOException {
        return read(new InputStreamReader(from, encoding));
    }

    public static String read(Reader from) throws IOException {
        StringWriter builder = new StringWriter(CHAR_BUFFER_SIZE);
        try {
            copy(from, builder);
            return builder.toString();
        } finally {
            close(from);
        }
    }

    public static byte[] readBytes(InputStream from) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(Math.max(from.available(), BUFFER_SIZE));
        try {
            copy(from, output);
        } finally {
            close(from);
        }
        return output.toByteArray();
    }

    public static void write(byte[] from, OutputStream to) throws IOException {
        try {
            to.write(from);
            to.flush();
        } finally {
            close(to);
        }
    }

    public static void write(String from, OutputStream to) throws IOException {
        write(from, new OutputStreamWriter(to, UTF_8));
    }

    public static void write(char[] from, Writer to) throws IOException {
        try {
            to.write(from);
            to.flush();
        } finally {
            close(to);
        }
    }

    public static void write(String from, Writer to) throws IOException {
        try {
            to.write(from);
            to.flush();
        } finally {
            close(to);
        }
    }

    public static long copy(Reader from, Writer to) throws IOException {
        char[] buffer = new char[CHAR_BUFFER_SIZE];
        int read;
        long total = 0;

        while ((read = from.read(buffer)) != -1) {
            to.write(buffer, 0, read);
            total += read;
        }
        return total;
    }

    public static long copy(InputStream from, OutputStream to) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        long total = 0;

        while ((read = from.read(buffer)) != -1) {
            to.write(buffer, 0, read);
            total += read;
        }
        return total;
    }

    public static BufferedInputStream buffer(InputStream input) {
        return buffer(input, BUFFER_SIZE);
    }

    public static BufferedInputStream buffer(InputStream input, int size) {
        return input instanceof BufferedInputStream ? (BufferedInputStream) input
                : new BufferedInputStream(input, size);
    }

    public static BufferedOutputStream buffer(OutputStream output) {
        return buffer(output, BUFFER_SIZE);
    }

    public static BufferedOutputStream buffer(OutputStream output, int size) {
        return output instanceof BufferedOutputStream ? (BufferedOutputStream) output
                : new BufferedOutputStream(output, size);
    }

    public static BufferedReader buffer(Reader input) {
        return buffer(input, CHAR_BUFFER_SIZE);
    }

    public static BufferedReader buffer(Reader input, int size) {
        return input instanceof BufferedReader ? (BufferedReader) input
                : new BufferedReader(input, size);
    }

    public static BufferedWriter buffer(Writer output) {
        return buffer(output, CHAR_BUFFER_SIZE);
    }

    public static BufferedWriter buffer(Writer output, int size) {
        return output instanceof BufferedWriter ? (BufferedWriter) output
                : new BufferedWriter(output, size);
    }

    public static GZIPInputStream gzip(InputStream input) throws IOException {
        return gzip(input, BUFFER_SIZE);
    }

    public static GZIPInputStream gzip(InputStream input, int size) throws IOException {
        return input instanceof GZIPInputStream ? (GZIPInputStream) input
                : new GZIPInputStream(input, size);
    }

    public static GZIPOutputStream gzip(OutputStream input) throws IOException {
        return gzip(input, BUFFER_SIZE);
    }

    public static GZIPOutputStream gzip(OutputStream input, int size) throws IOException {
        return input instanceof GZIPOutputStream ? (GZIPOutputStream) input
                : new GZIPOutputStream(input, size);
    }

    public static boolean close(Closeable c) {
        if (c != null) {
            try {
                c.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
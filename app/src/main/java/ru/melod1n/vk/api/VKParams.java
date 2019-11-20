package ru.melod1n.vk.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.TreeMap;
import java.util.Map.Entry;

public class VKParams {
    private TreeMap<String, String> args = new TreeMap<>();
    private String methodName;

    public VKParams(String methodName) {
        this.methodName = methodName;
    }

    public static VKParams request(String methodName) {
        return new VKParams(methodName);
    }

    public boolean contains(String key) {
        return args.containsKey(key);
    }

    public VKParams put(String key, String value) {
        if (value == null || value.length() == 0)
            return this;
        args.put(key, value);

        return this;
    }

    public VKParams put(String key, long value) {
        args.put(key, Long.toString(value));
        return this;
    }

    public VKParams put(String key, int value) {
        args.put(key, Integer.toString(value));
        return this;
    }

    public VKParams put(String key, double value) {
        args.put(key, Double.toString(value));
        return this;
    }

    public VKParams put(String key, boolean value) {
        return put(key, value ? 1 : 0);
    }

    String getParamsString() {
        StringBuilder params = new StringBuilder();
        try {
            for (Entry<String, String> entry : args.entrySet()) {
                if (params.length() != 0)
                    params.append("&");
                params.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "utf-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return params.toString();
    }

    public String getMethodName() {
        return methodName;
    }
}

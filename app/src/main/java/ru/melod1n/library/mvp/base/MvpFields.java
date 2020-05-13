package ru.melod1n.library.mvp.base;

import android.util.ArrayMap;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.Objects;

public class MvpFields {

    @NonNull
    private ArrayMap<String, Object> fields = new ArrayMap<>();

    public MvpFields put(String key, Object value) {
        fields.put(key, value);
        return this;
    }

    public Long getLong(String key) {
        return (Long) fields.get(key);
    }

    public Integer getInt(String key) {
        return (Integer) fields.get(key);
    }

    public String getString(String key) {
        return (String) fields.get(key);
    }

    public Boolean getBoolean(String key) {
        return (Boolean) fields.get(key);
    }

    public Object get(String key) {
        return fields.get(key);
    }

    @NonNull
    public Object getNonNull(String key) {
        return Objects.requireNonNull(fields.get(key));
    }

    @NonNull
    public Object getNonNull(Object object) {
        return Objects.requireNonNull(object);
    }

    public Map<String, Object> getFields() {
        return fields;
    }

}

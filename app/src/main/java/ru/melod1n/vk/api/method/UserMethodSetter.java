package ru.melod1n.vk.api.method;

public class UserMethodSetter extends MethodSetter {

    public UserMethodSetter(String name) {
        super(name);
    }

    public UserMethodSetter extended(boolean extended) {
        put("extended", extended);
        return this;
    }

    public UserMethodSetter type(String type) {
        put("type", type);
        return this;
    }

    public UserMethodSetter comment(String comment) {
        put("comment", comment);
        return this;
    }

    public UserMethodSetter latitude(float latitude) {
        put("latitude", latitude);
        return this;
    }

    public UserMethodSetter longitude(float longitude) {
        put("longitude", longitude);
        return this;
    }

    public UserMethodSetter accuracy(int accuracy) {
        put("accuracy", accuracy);
        return this;
    }

    public UserMethodSetter timeout(int timeout) {
        put("timeout", timeout);
        return this;
    }

    public UserMethodSetter radius(int radius) {
        put("radius", radius);
        return this;
    }
}
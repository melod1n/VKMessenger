package ru.melod1n.vk.util;

import java.util.ArrayList;
import java.util.Collection;

public class ArrayUtil {

    public static final int VALUE_NOT_FOUND = -1;

    private ArrayUtil() {
    }

    public static int linearSearch(byte[] array, byte value) {
        return linearSearch(array, value, 0, array.length);
    }

    public static int linearSearch(byte[] array, byte value, int start, int end) {
        for (int i = start; i < end; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return VALUE_NOT_FOUND;
    }

    public static int linearSearch(char[] array, char value) {
        return linearSearch(array, value, 0, array.length);
    }

    public static int linearSearch(char[] array, char value, int start, int end) {
        for (int i = start; i < end; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return VALUE_NOT_FOUND;
    }

    public static int linearSearch(short[] array, short value) {
        return linearSearch(array, value, 0, array.length);
    }

    public static int linearSearch(short[] array, short value, int start, int end) {
        for (int i = start; i < end; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return VALUE_NOT_FOUND;
    }

    public static int linearSearch(int[] array, int value) {
        return linearSearch(array, value, 0, array.length);
    }

    public static int linearSearch(int[] array, int value, int start, int end) {
        for (int i = start; i < end; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return VALUE_NOT_FOUND;
    }

    public static int linearSearch(long[] array, long value) {
        return linearSearch(array, value, 0, array.length);
    }

    public static int linearSearch(long[] array, long value, int start, int end) {
        for (int i = start; i < end; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return VALUE_NOT_FOUND;
    }

    public static int linearSearch(float[] array, float value) {
        return linearSearch(array, value, 0, array.length);
    }

    public static int linearSearch(float[] array, float value, int start, int end) {
        for (int i = start; i < end; i++) {
            if (Float.compare(array[i], value) == 0) {
                return i;
            }
        }
        return VALUE_NOT_FOUND;
    }

    public static int linearSearch(double[] array, double value) {
        return linearSearch(array, value, 0, array.length);
    }

    public static int linearSearch(double[] array, double value, int start, int end) {
        for (int i = start; i < end; i++) {
            if (Double.compare(array[i], value) == 0) {
                return i;
            }
        }
        return VALUE_NOT_FOUND;
    }

    public static int linearSearch(Object[] array, Object value) {
        return linearSearch(array, value, 0, array.length);
    }

    public static int linearSearch(Object[] array, Object value, int start, int end) {
        for (int i = start; i < end; i++) {
            Object o = array[i];
            if (o == value || o.equals(value)) {
                return i;
            }
        }
        return VALUE_NOT_FOUND;
    }

    @SafeVarargs
    public static <T> String toString(T... array) {
        if (array == null || array.length == 0) {
            return null;
        }

        StringBuilder buffer = new StringBuilder(array.length * 12);
        buffer.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            buffer.append(',');
            buffer.append(array[i]);
        }
        return buffer.toString();
    }

    public static String toString(int... array) {
        if (array == null || array.length == 0) {
            return null;
        }

        StringBuilder buffer = new StringBuilder(array.length * 12);
        buffer.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            buffer.append(',');
            buffer.append(array[i]);
        }
        return buffer.toString();
    }

    public static <E> ArrayList<E> singletonList(E object) {
        ArrayList<E> list = new ArrayList<>(1);
        list.add(object);

        return list;
    }

    public static boolean isEmpty(byte[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(char[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(short[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(long[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(float[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(double[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean contains(int[] array, int i) {
        for (int iInt : array) {
            if (iInt == i) return true;
        }

        return false;
    }
}
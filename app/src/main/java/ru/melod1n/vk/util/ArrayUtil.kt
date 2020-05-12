package ru.melod1n.vk.util

object ArrayUtil {

    const val VALUE_NOT_FOUND = -1

    @JvmStatic
    @JvmOverloads
    fun linearSearch(array: ByteArray, value: Byte, start: Int = 0, end: Int = array.size): Int {
        for (i in start until end) {
            if (array[i] == value) {
                return i
            }
        }
        return VALUE_NOT_FOUND
    }

    @JvmStatic
    @JvmOverloads
    fun linearSearch(array: CharArray, value: Char, start: Int = 0, end: Int = array.size): Int {
        for (i in start until end) {
            if (array[i] == value) {
                return i
            }
        }
        return VALUE_NOT_FOUND
    }

    @JvmStatic
    @JvmOverloads
    fun linearSearch(array: ShortArray, value: Short, start: Int = 0, end: Int = array.size): Int {
        for (i in start until end) {
            if (array[i] == value) {
                return i
            }
        }
        return VALUE_NOT_FOUND
    }

    @JvmStatic
    @JvmOverloads
    fun linearSearch(array: IntArray, value: Int, start: Int = 0, end: Int = array.size): Int {
        for (i in start until end) {
            if (array[i] == value) {
                return i
            }
        }
        return VALUE_NOT_FOUND
    }

    @JvmStatic
    @JvmOverloads
    fun linearSearch(array: LongArray, value: Long, start: Int = 0, end: Int = array.size): Int {
        for (i in start until end) {
            if (array[i] == value) {
                return i
            }
        }
        return VALUE_NOT_FOUND
    }

    @JvmStatic
    @JvmOverloads
    fun linearSearch(array: FloatArray, value: Float, start: Int = 0, end: Int = array.size): Int {
        for (i in start until end) {
            if (array[i].compareTo(value) == 0) {
                return i
            }
        }
        return VALUE_NOT_FOUND
    }

    @JvmStatic
    @JvmOverloads
    fun linearSearch(array: DoubleArray, value: Double, start: Int = 0, end: Int = array.size): Int {
        for (i in start until end) {
            if (array[i].compareTo(value) == 0) {
                return i
            }
        }
        return VALUE_NOT_FOUND
    }

    @JvmStatic
    @JvmOverloads
    fun linearSearch(array: Array<Any>, value: Any, start: Int = 0, end: Int = array.size): Int {
        for (i in start until end) {
            val o = array[i]
            if (o == value) {
                return i
            }
        }
        return VALUE_NOT_FOUND
    }

    @JvmStatic
    @SafeVarargs
    fun <T> toString(vararg array: T): String {
        if (array.isEmpty()) {
            return ""
        }
        val buffer = StringBuilder(array.size * 12)
        buffer.append(array[0])
        for (i in 1 until array.size) {
            buffer.append(',')
            buffer.append(array[i])
        }
        return buffer.toString()
    }

    @JvmStatic
    fun toString(vararg array: Int): String {
        if (array.isEmpty()) {
            return ""
        }
        val buffer = StringBuilder(array.size * 12)
        buffer.append(array[0])
        for (i in 1 until array.size) {
            buffer.append(',')
            buffer.append(array[i])
        }
        return buffer.toString()
    }

    @JvmStatic
    fun <E> singletonList(`object`: E): ArrayList<E> {
        val list = ArrayList<E>(1)
        list.add(`object`)
        return list
    }

    @JvmStatic
    fun isEmpty(array: ByteArray?): Boolean {
        return array == null || array.isEmpty()
    }

    @JvmStatic
    fun isEmpty(array: CharArray?): Boolean {
        return array == null || array.isEmpty()
    }

    @JvmStatic
    fun isEmpty(array: ShortArray?): Boolean {
        return array == null || array.isEmpty()
    }

    @JvmStatic
    fun isEmpty(array: IntArray?): Boolean {
        return array == null || array.isEmpty()
    }

    @JvmStatic
    fun isEmpty(array: LongArray?): Boolean {
        return array == null || array.isEmpty()
    }

    @JvmStatic
    fun isEmpty(array: FloatArray?): Boolean {
        return array == null || array.isEmpty()
    }

    @JvmStatic
    fun isEmpty(array: DoubleArray?): Boolean {
        return array == null || array.isEmpty()
    }

    @JvmStatic
    fun isEmpty(array: Array<Any?>?): Boolean {
        return array == null || array.isEmpty()
    }

    @JvmStatic
    fun isEmpty(collection: Collection<*>?): Boolean {
        return collection == null || collection.isEmpty()
    }

    @JvmStatic
    fun contains(array: IntArray, i: Int): Boolean {
        for (iInt in array) {
            if (iInt == i) return true
        }
        return false
    }

    @JvmStatic
    fun <E> prepareList(list: ArrayList<E>, offset: Int, count: Int): ArrayList<E> {
        if (true) {
            //some prepares

            return list
        }

        var arrayList = list

        if (isEmpty(arrayList)) return arrayList

        if (arrayList.size > offset && offset > 0) {
            arrayList = ArrayList(arrayList.subList(offset - 1, arrayList.size))
        }

        if (arrayList.size > count && count > 0) {
            arrayList = ArrayList(arrayList.subList(0, count))
        }

        return arrayList
    }
}
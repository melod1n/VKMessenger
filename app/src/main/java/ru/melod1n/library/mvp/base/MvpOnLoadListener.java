package ru.melod1n.library.mvp.base;

import java.util.ArrayList;

public interface MvpOnLoadListener<T> {

    void onSuccessLoad(ArrayList<T> values);
    void onErrorLoad(Exception e);

}

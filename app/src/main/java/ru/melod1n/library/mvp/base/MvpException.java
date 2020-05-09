package ru.melod1n.library.mvp.base;

public class MvpException extends Exception {

    public static final String ERROR_EMPTY = "_empty";

    public String errorId;

    public MvpException(String errorId) {
        super(errorId);

        this.errorId = errorId;
    }
}

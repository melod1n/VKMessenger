package ru.melod1n.vk.api;

public class ErrorCodes {

    private ErrorCodes() {
    }

    public static final int UNKNOWN_ERROR = 1;
    public static final int APP_DISABLED = 2;
    public static final int UNKNOWN_METHOD = 3;
    public static final int INVALID_SIGNATURE = 4;
    public static final int USER_AUTHORIZATION_FAILED = 5;
    public static final int TOO_MANY_REQUESTS = 6;
    public static final int NO_RIGHTS = 7;
    public static final int BAD_REQUEST = 8;
    public static final int TOO_MANY_SIMILAR_ACTIONS = 9;
    public static final int INTERNAL_SERVER_ERROR = 10;
    public static final int IN_TEST_MODE = 11;
    public static final int EXECUTE_CODE_COMPILE_ERROR = 12;
    public static final int EXECUTE_CODE_RUNTIME_ERROR = 13;
    public static final int CAPTCHA_NEEDED = 14;
    public static final int ACCESS_DENIED = 15;
    public static final int REQUIRES_REQUESTS_OVER_HTTPS = 16;
    public static final int VALIDATION_REQUIRED = 17;
    public static final int USER_BANNED_OR_DELETED = 18;
    public static final int ACTION_PROHIBITED = 20;
    public static final int ACTION_ALLOWED_ONLY_FOR_STANDALONE = 21;
    public static final int METHOD_OFF = 23;
    public static final int CONFIRMATION_REQUIRED = 24;
    public static final int PARAMETER_IS_NOT_SPECIFIED = 100;
    public static final int INCORRECT_APP_ID = 101;
    public static final int OUT_OF_LIMITS = 103;
    public static final int INCORRECT_USER_ID = 113;
    public static final int INCORRECT_TIMESTAMP = 150;
    public static final int ACCESS_TO_ALBUM_DENIED = 200;
    public static final int ACCESS_TO_AUDIO_DENIED = 201;
    public static final int ACCESS_TO_GROUP_DENIED = 203;
    public static final int ALBUM_IS_FULL = 300;
    public static final int ACTION_DENIED = 500;
    public static final int PERMISSION_DENIED = 600;
    public static final int CANNOT_SEND_MESSAGE_BLACK_LIST = 900;
    public static final int CANNOT_SEND_MESSAGE_GROUP = 901;
    public static final int INVALID_DOC_ID = 1150;
    public static final int INVALID_DOC_TITLE = 1152;
    public static final int ACCESS_TO_DOC_DENIED = 1153;
}
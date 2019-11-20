package ru.melod1n.vk.api;

@SuppressWarnings("serial")
public class VKException extends Exception{
    VKException(int code, String message, String url){
        super(message);
        error_code=code;
        this.url=url;
    }
    public int error_code;
    public String url;
    
    //for captcha
    public String captcha_img;
    public String captcha_sid;
    
    //for "Validation required" error
    public String redirect_uri;
}

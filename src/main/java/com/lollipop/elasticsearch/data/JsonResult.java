package com.lollipop.elasticsearch.data;

/**
 * @author: lollipop
 * @date: 17/11/10
 */
public class JsonResult {

    private boolean success;
    private Object data;
    private String message;

    public JsonResult() {
    }

    public JsonResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public void markSuccess(String message,Object data) {
        this.success = true;
        this.message = message;
        this.data = data;
    }

    public void markError() {
        this.success = false;
    }

    public void markError(String message) {
        this.success = false;
        this.message = message;
    }
}

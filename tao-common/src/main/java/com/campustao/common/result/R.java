package com.campustao.common.result;

import lombok.Data;

@Data
public class R<T> {

    public static final int SUCCESS = 200;
    public static final int ERROR = 500;

    private int code;
    private String msg;
    private T data;

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(SUCCESS);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.setCode(ERROR);
        r.setMsg(msg);
        return r;
    }
}
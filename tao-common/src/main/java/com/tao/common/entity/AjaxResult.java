package com.tao.common.entity;
import lombok.Data;
import java.io.Serializable;
import java.util.HashMap;

/**
 * 封装HTTP响应的统一结果对象
 * 控制器方法的返回值统一使用 AjaxResult，便于前端统一处理
 */
@Data
public class AjaxResult extends HashMap<String, Object> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    public static final String CODE_TAG = "code";

    /** 返回内容 */
    public static final String MSG_TAG = "msg";

    /** 数据对象 */
    public static final String DATA_TAG = "data";

    /** 状态码: 成功 */
    public static final int SUCCESS = 200;

    /** 状态码: 失败 */
    public static final int ERROR = 500;

    public AjaxResult() {
    }

    public AjaxResult(int code, String msg) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }
    public AjaxResult(int code, String msg, Object data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (data != null) {
            super.put(DATA_TAG, data);
        }
    }

    /**
     * 返回成功消息
     */
    public static AjaxResult success() {
        return new AjaxResult(SUCCESS, "操作成功");
    }

    /**
     * 返回成功数据
     */
    public static AjaxResult success(Object data) {
        return new AjaxResult(SUCCESS, "操作成功", data);
    }

    /**
     * 返回成功消息和数据
     */
    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(SUCCESS, msg, data);
    }

    /**
     * 返回错误消息
     */
    public static AjaxResult error() {
        return new AjaxResult(ERROR, "操作失败");
    }

    /**
     * 返回错误消息
     */
    public static AjaxResult error(String msg) {
        return new AjaxResult(ERROR, msg);
    }
    /**
     * 返回错误消息和数据
     */
    public static AjaxResult error(String msg, Object data) {
        return new AjaxResult(ERROR, msg, data);
    }

    /**
     * 自定义状态码和消息
     */
    public static AjaxResult of(int code, String msg) {
        return new AjaxResult(code, msg);
    }

    /**
     * 链式追加自定义键值对
     */
    public AjaxResult put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}

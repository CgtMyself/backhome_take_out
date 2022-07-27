package com.imback.common;


import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 封装的通用返回结果类
 */
@Data
public class Result<T> implements Serializable {
    private Integer code;  //返回编码 1成功 0失败
    private String msg;
    private T data;
    private Map map = new HashMap<>();  //动态数据

    public static <T> Result<T> success(T obj){
        Result result = new Result<T>();
        result.data = obj;
        result.code = 1;
        return result;
    }

    public static <T> Result<T> error(String msg){
        Result result = new Result<T>();
        result.msg = msg;
        result.code = 0;
        return result;
    }

    public Result<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}

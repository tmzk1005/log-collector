package zk.logcollector.core.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class RestResponse<T> {

    public static final int OK = 0;

    public static final int ERROR = -1;

    private int code;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    private RestResponse() {
    }

    public static <E> RestResponse<E> of(int code, String message, E data) {
        RestResponse<E> resp = new RestResponse<>();
        resp.code = code;
        resp.message = message;
        resp.data = data;
        return resp;
    }

    public static RestResponse<Void> of(int code, String message) {
        RestResponse<Void> resp = new RestResponse<>();
        resp.code = code;
        resp.message = message;
        return resp;
    }

    public static RestResponse<Void> ofMessage(String message) {
        RestResponse<Void> resp = new RestResponse<>();
        resp.code = OK;
        resp.message = message;
        return resp;
    }

    public static RestResponse<Void> ofError(String message) {
        RestResponse<Void> resp = new RestResponse<>();
        resp.code = ERROR;
        resp.message = message;
        return resp;
    }

    public static <E> RestResponse<E> of(E data) {
        RestResponse<E> resp = new RestResponse<>();
        resp.code = OK;
        resp.message = "succeed";
        resp.data = data;
        return resp;
    }

    public static RestResponse<Void> ok() {
        RestResponse<Void> resp = new RestResponse<>();
        resp.code = OK;
        resp.message = "succeed";
        return resp;
    }

}

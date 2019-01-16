package io.scinapse.api.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonSerialize
@Getter
@Setter
public class Response<T> {

    private Error error;
    private Data<T> data;

    public static Response error(HttpServletRequest request) {
        Error error = Error.of(request);
        Response<Object> errorResponse = new Response<>();
        errorResponse.setError(error);
        return errorResponse;
    }

    public static Response error(HttpServletRequest request, Exception e, HttpStatus status) {
        Error error = Error.of(request, e, status);
        Response<Object> errorResponse = new Response<>();
        errorResponse.setError(error);
        return errorResponse;
    }

    public static <R> Response<R> success(R content) {
        Data<R> data = Data.of(content);
        Response<R> response = new Response<>();
        response.setData(data);
        return response;
    }

    public static <S, R extends Page<S>> Response<List<S>> success(R content) {
        Data<List<S>> data = Data.of(content);
        Response<List<S>> response = new Response<>();
        response.setData(data);
        return response;
    }

    public static <R> Response<R> success(R content, Object additional) {
        Data<R> data = Data.of(content, additional);
        Response<R> response = new Response<>();
        response.setData(data);
        return response;
    }

    public static <S, R extends Page<S>> Response<List<S>> success(R content, Object additional) {
        Data<List<S>> data = Data.of(content, additional);
        Response<List<S>> response = new Response<>();
        response.setData(data);
        return response;
    }

    public static Response success() {
        return success("success");
    }

}

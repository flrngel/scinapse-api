package io.scinapse.api.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletRequest;

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

    public static <R> Response<R> success(R content) {
        Data<R> data = Data.of(content);
        Response<R> response = new Response<>();
        response.setData(data);
        return response;
    }

}

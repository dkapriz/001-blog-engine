package main.exception;

import main.api.dto.ErrorDTO;
import main.api.response.ResultResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.WebUtils;

@ControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler({IllegalParameterException.class})
    public final ResponseEntity<ResultResponse> handleException(Exception ex, WebRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();

        if (ex instanceof IllegalParameterException) {
            HttpStatus httpStatus = HttpStatus.OK;
            IllegalParameterException illegalParameterException = (IllegalParameterException) ex;
            return handleIllegalParameterException(illegalParameterException, httpHeaders, httpStatus, request);
        }

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return handleExceptionInternal(ex, null, httpHeaders, httpStatus, request);
    }

    protected ResponseEntity<ResultResponse> handleIllegalParameterException
            (IllegalParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, new ErrorDTO(false, ex.getType(), ex.getMessage()),
                headers, status, request);
    }

    protected ResponseEntity<ResultResponse> handleExceptionInternal
            (Exception ex, ResultResponse body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
        ex.printStackTrace();
        return new ResponseEntity<>(body, headers, status);
    }
}

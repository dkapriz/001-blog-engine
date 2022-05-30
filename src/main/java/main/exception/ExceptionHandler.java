package main.exception;

import main.api.dto.ErrorDTO;
import main.api.response.ResultResponse;
import main.config.BlogConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.WebUtils;

@ControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler({
            UsernameNotFoundException.class,
            AuthenticationException.class,
            IllegalParameterException.class,
            PageNotFoundException.class
    })
    public final ResponseEntity<ResultResponse> handleException(Exception ex, WebRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();

        if (ex instanceof UsernameNotFoundException) {
            HttpStatus httpStatus = HttpStatus.NOT_FOUND;
            UsernameNotFoundException usernameNotFoundException = (UsernameNotFoundException) ex;
            BlogConfig.LOGGER.info(BlogConfig.MARKER_UNSUCCESSFUL_REQUEST, ex.getMessage());
            return handleUsernameNotFoundException(usernameNotFoundException, httpHeaders, httpStatus, request);
        }

        if (ex instanceof AuthenticationException) {
            HttpStatus httpStatus = HttpStatus.OK;
            AuthenticationException authenticationException = (AuthenticationException) ex;
            BlogConfig.LOGGER.info(BlogConfig.MARKER_UNSUCCESSFUL_REQUEST, ex.getMessage());
            return handleAuthenticationException(authenticationException, httpHeaders, httpStatus, request);
        }

        if (ex instanceof IllegalParameterException) {
            HttpStatus httpStatus = HttpStatus.OK;
            IllegalParameterException illegalParameterException = (IllegalParameterException) ex;
            BlogConfig.LOGGER.info(BlogConfig.MARKER_UNSUCCESSFUL_REQUEST, ex.getMessage());
            return handleIllegalParameterException(illegalParameterException, httpHeaders, httpStatus, request);
        }

        if (ex instanceof PageNotFoundException) {
            HttpStatus httpStatus = HttpStatus.NOT_FOUND;
            PageNotFoundException pageNotFoundException = (PageNotFoundException) ex;
            BlogConfig.LOGGER.info(BlogConfig.MARKER_UNSUCCESSFUL_REQUEST, ex.getMessage());
            return handlePageNotFoundException(pageNotFoundException, httpHeaders, httpStatus, request);
        }

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        BlogConfig.LOGGER.error(ex.getMessage());
        return handleExceptionInternal(ex, null, httpHeaders, httpStatus, request);
    }

    private ResponseEntity<ResultResponse> handleUsernameNotFoundException
            (UsernameNotFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, new ErrorDTO(false, "user", ex.getMessage()),
                headers, status, request);
    }

    private ResponseEntity<ResultResponse> handleAuthenticationException
            (AuthenticationException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, new ErrorDTO(false, ex.getMessage()), headers, status, request);
    }

    private ResponseEntity<ResultResponse> handlePageNotFoundException
            (PageNotFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, new ErrorDTO(false, ex.getMessage()), headers, status, request);
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

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

import javax.mail.MessagingException;
import java.io.IOException;

@ControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler({
            UsernameNotFoundException.class,
            AuthenticationException.class,
            ResultIllegalParameterException.class,
            IllegalParameterException.class,
            DataNotFoundException.class,
            IOException.class,
            MessagingException.class
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

        if (ex instanceof ResultIllegalParameterException) {
            HttpStatus httpStatus = HttpStatus.OK;
            ResultIllegalParameterException resultIllegalParameterException = (ResultIllegalParameterException) ex;
            BlogConfig.LOGGER.info(BlogConfig.MARKER_UNSUCCESSFUL_REQUEST, ex.getMessage());
            return handleResultIllegalParameterException(resultIllegalParameterException, httpHeaders, httpStatus, request);
        }

        if (ex instanceof IllegalParameterException) {
            HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
            IllegalParameterException illegalParameterException = (IllegalParameterException) ex;
            BlogConfig.LOGGER.info(BlogConfig.MARKER_UNSUCCESSFUL_REQUEST, ex.getMessage());
            return handleIllegalParameterException(illegalParameterException, httpHeaders, httpStatus, request);
        }

        if (ex instanceof DataNotFoundException) {
            HttpStatus httpStatus = HttpStatus.NOT_FOUND;
            DataNotFoundException dataNotFoundException = (DataNotFoundException) ex;
            BlogConfig.LOGGER.info(BlogConfig.MARKER_UNSUCCESSFUL_REQUEST, ex.getMessage());
            return handleDataNotFoundException(dataNotFoundException, httpHeaders, httpStatus, request);
        }

        if (ex instanceof IOException) {
            HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
            IOException ioException = (IOException) ex;
            BlogConfig.LOGGER.info(BlogConfig.MARKER_UNSUCCESSFUL_REQUEST, ex.getMessage());
            return handleIOException(ioException, httpHeaders, httpStatus, request);
        }

        if (ex instanceof MessagingException) {
            HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
            MessagingException messagingException = (MessagingException) ex;
            BlogConfig.LOGGER.info(BlogConfig.MARKER_UNSUCCESSFUL_REQUEST, ex.getMessage());
            return handleMessagingException(messagingException, httpHeaders, httpStatus, request);
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

    private ResponseEntity<ResultResponse> handleIllegalParameterException
            (IllegalParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, new ResultResponse(ex.getMessage()), headers, status, request);
    }

    private ResponseEntity<ResultResponse> handleDataNotFoundException
            (DataNotFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, new ErrorDTO(false, ex.getMessage()), headers, status, request);
    }

    private ResponseEntity<ResultResponse> handleIOException
            (IOException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, new ErrorDTO(false, ex.getMessage()), headers, status, request);
    }

    private ResponseEntity<ResultResponse> handleMessagingException
            (MessagingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, new ErrorDTO(false, ex.getMessage()), headers, status, request);
    }

    protected ResponseEntity<ResultResponse> handleResultIllegalParameterException
            (ResultIllegalParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, new ErrorDTO(false, ex.getType(), ex.getMessage()),
                headers, status, request);
    }

    protected ResponseEntity<ResultResponse> handleExceptionInternal
            (Exception ex, ResultResponse body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex.getMessage(), WebRequest.SCOPE_REQUEST);
        }
        ex.printStackTrace();
        return new ResponseEntity<>(body, headers, status);
    }
}

package main.exception;

public class IllegalParameterException extends RuntimeException {

    public IllegalParameterException(String massage) {
        super(massage);
    }
}

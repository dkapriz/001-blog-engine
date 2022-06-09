package main.exception;

public class DataNotFoundException extends RuntimeException {

    public DataNotFoundException(String massage) {
        super(massage);
    }
}

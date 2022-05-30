package main.exception;

public class PageNotFoundException extends RuntimeException {

    public PageNotFoundException(String massage) {
        super(massage);
    }
}

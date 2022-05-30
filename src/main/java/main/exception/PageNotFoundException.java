package main.exception;

public class PageNotFoundException extends RuntimeException{
    private String type;

    public PageNotFoundException(String massage) {
        super(massage);
    }
}

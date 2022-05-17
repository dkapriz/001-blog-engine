package main.exception;

import lombok.Getter;

@Getter
public class IllegalParameterException extends RuntimeException {

    private String type;

    public IllegalParameterException(String massage) {
        super(massage);
    }

    public IllegalParameterException(String type, String massage) {
        super(massage);
        this.type = type;
    }
}

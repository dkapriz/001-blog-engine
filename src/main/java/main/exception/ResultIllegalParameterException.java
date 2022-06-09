package main.exception;

import lombok.Getter;

@Getter
public class ResultIllegalParameterException extends RuntimeException {

    private String type;

    public ResultIllegalParameterException(String massage) {
        super(massage);
        type = "errors";
    }

    public ResultIllegalParameterException(String type, String massage) {
        super(massage);
        this.type = type;
    }
}

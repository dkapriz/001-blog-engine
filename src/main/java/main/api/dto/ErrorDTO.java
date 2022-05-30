package main.api.dto;

import lombok.Getter;
import lombok.Setter;
import main.api.response.ResultResponse;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ErrorDTO extends ResultResponse {
    private Map<String, String> errors;

    public ErrorDTO(boolean result) {
        super(result);
        errors = new HashMap<>();
    }

    public ErrorDTO(boolean result, HashMap<String, String> errors) {
        super(result);
        this.errors = errors;
    }

    public ErrorDTO(boolean result, String message) {
        super(result);
        this.errors = new HashMap<>();
        this.errors.put("message", message);
    }

    public ErrorDTO(boolean result, String type, String message) {
        super(result);
        this.errors = new HashMap<>();
        this.errors.put(type, message);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}

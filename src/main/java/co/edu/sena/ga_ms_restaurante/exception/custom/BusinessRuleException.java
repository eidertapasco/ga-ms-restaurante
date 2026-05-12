package co.edu.sena.ga_ms_restaurante.exception.custom;

public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}

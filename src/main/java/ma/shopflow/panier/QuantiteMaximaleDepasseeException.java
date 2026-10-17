package ma.shopflow.panier;

public class QuantiteMaximaleDepasseeException extends RuntimeException {
    public QuantiteMaximaleDepasseeException(String message) {
        super(message);
    }
}

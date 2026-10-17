package ma.shopflow.commande;

public class PaiementRefuseException extends RuntimeException {
    public PaiementRefuseException(String message) {
        super(message);
    }
}

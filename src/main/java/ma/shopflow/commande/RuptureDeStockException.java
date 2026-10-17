package ma.shopflow.commande;

public class RuptureDeStockException extends RuntimeException {
    public RuptureDeStockException(String message) {
        super(message);
    }
}

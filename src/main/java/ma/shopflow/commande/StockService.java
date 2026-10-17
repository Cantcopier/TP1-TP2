package ma.shopflow.commande;

public interface StockService {
    boolean estDisponible(String reference, int quantite);

    void reserver(String reference, int quantite);

    void liberer(String reference, int quantite);
}

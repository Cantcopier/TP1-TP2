package ma.shopflow.commande;

public interface NotificationService {
    void envoyerConfirmation(String email, Commande commande);
}

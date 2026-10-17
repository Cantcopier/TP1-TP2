package ma.shopflow.commande;

import ma.shopflow.panier.LignePanier;
import ma.shopflow.panier.Panier;

import java.util.List;
import java.util.UUID;

public class ServiceCommande {
    private static final System.Logger LOGGER = System.getLogger(ServiceCommande.class.getName());

    private final StockService stock;
    private final PaiementGateway paiement;
    private final CommandeRepository commandes;
    private final NotificationService notifications;

    public ServiceCommande(StockService stock, PaiementGateway paiement,
                           CommandeRepository commandes, NotificationService notifications) {
        this.stock = stock;
        this.paiement = paiement;
        this.commandes = commandes;
        this.notifications = notifications;
    }

    public Commande passerCommande(Client client, Panier panier) {
        List<LignePanier> lignes = panier.lignes();
        if (lignes.isEmpty()) {
            throw new IllegalArgumentException("Le panier ne peut pas être vide");
        }
        for (LignePanier ligne : lignes) {
            if (!stock.estDisponible(ligne.produit().reference(), ligne.quantite())) {
                throw new RuptureDeStockException(
                        "Stock insuffisant pour le produit " + ligne.produit().reference());
            }
        }
        for (LignePanier ligne : lignes) {
            stock.reserver(ligne.produit().reference(), ligne.quantite());
        }

        ResultatPaiement resultat = paiement.debiter(client.id(), panier.total());
        if (!resultat.accepte()) {
            for (LignePanier ligne : lignes) {
                stock.liberer(ligne.produit().reference(), ligne.quantite());
            }
            throw new PaiementRefuseException(resultat.motifRefus());
        }

        Commande commande = new Commande(
                UUID.randomUUID().toString(), client.id(), lignes, panier.total(),
                resultat.reference(), StatutCommande.CONFIRMEE);
        commandes.enregistrer(commande);
        try {
            notifications.envoyerConfirmation(client.email(), commande);
        } catch (RuntimeException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Échec de l'envoi de la confirmation pour la commande " + commande.id(), e);
        }
        return commande;
    }
}

package ma.shopflow.commande;

import ma.shopflow.panier.LignePanier;

import java.math.BigDecimal;
import java.util.List;

public record Commande(String id, String clientId, List<LignePanier> lignes, BigDecimal total,
                       String referencePaiement, StatutCommande statut) {
    public Commande {
        lignes = List.copyOf(lignes);
    }
}

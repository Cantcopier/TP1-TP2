package ma.shopflow.commande;

public record ResultatPaiement(boolean accepte, String reference, String motifRefus) {
}

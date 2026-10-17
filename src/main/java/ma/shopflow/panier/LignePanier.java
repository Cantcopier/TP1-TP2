package ma.shopflow.panier;

import java.math.BigDecimal;
import java.util.Objects;

public record LignePanier(Produit produit, int quantite) {
    public LignePanier {
        Objects.requireNonNull(produit, "Le produit ne peut pas être nul");
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être strictement positive");
        }
    }

    public BigDecimal sousTotal() {
        return produit.prixUnitaire().multiply(BigDecimal.valueOf(quantite));
    }
}

package ma.shopflow.panier;

import java.math.BigDecimal;
import java.util.Objects;

public record Produit(String reference, String nom, BigDecimal prixUnitaire) {
    public Produit {
        Objects.requireNonNull(reference, "La référence ne peut pas être nulle");
        Objects.requireNonNull(nom, "Le nom ne peut pas être nul");
        Objects.requireNonNull(prixUnitaire, "Le prix ne peut pas être nul");
        if (prixUnitaire.signum() <= 0) {
            throw new IllegalArgumentException("Le prix doit être strictement positif");
        }
    }
}

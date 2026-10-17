package ma.shopflow.panier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Panier {
    private static final int QUANTITE_MAXIMALE = 10;
    private static final BigDecimal SEUIL_LIVRAISON_GRATUITE = new BigDecimal("500.00");
    private static final BigDecimal FRAIS_LIVRAISON = new BigDecimal("30.00");
    private static final BigDecimal CENT = new BigDecimal("100");

    private final List<LignePanier> lignes = new ArrayList<>();
    private CodePromo codePromo;

    public void ajouterProduit(Produit produit, int quantite) {
        Objects.requireNonNull(produit, "Le produit ne peut pas être nul");
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être strictement positive");
        }
        int index = indexProduit(produit);
        int quantiteFinale = quantite + (index < 0 ? 0 : lignes.get(index).quantite());
        if (quantiteFinale > QUANTITE_MAXIMALE) {
            throw new QuantiteMaximaleDepasseeException(
                    "La quantité maximale par produit est " + QUANTITE_MAXIMALE);
        }
        if (index < 0) {
            lignes.add(new LignePanier(produit, quantite));
        } else {
            lignes.set(index, new LignePanier(produit, quantiteFinale));
        }
    }

    public void appliquerCodePromo(CodePromo codePromo) {
        this.codePromo = Objects.requireNonNull(codePromo, "Le code promo ne peut pas être nul");
    }

    public List<LignePanier> lignes() {
        return List.copyOf(lignes);
    }

    public BigDecimal sousTotal() {
        return lignes.stream()
                .map(LignePanier::sousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal remise() {
        if (codePromo == null) {
            return BigDecimal.ZERO;
        }
        return sousTotal().multiply(BigDecimal.valueOf(codePromo.pourcentage()))
                .divide(CENT);
    }

    public BigDecimal fraisLivraison() {
        return sousTotal().compareTo(SEUIL_LIVRAISON_GRATUITE) >= 0
                ? BigDecimal.ZERO
                : FRAIS_LIVRAISON;
    }

    public BigDecimal total() {
        return sousTotal().subtract(remise()).add(fraisLivraison());
    }

    private int indexProduit(Produit produit) {
        for (int i = 0; i < lignes.size(); i++) {
            if (lignes.get(i).produit().reference().equals(produit.reference())) {
                return i;
            }
        }
        return -1;
    }
}

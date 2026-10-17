package ma.shopflow.fidelite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class CalculateurFidelite {
    public static final int PLAFOND_PAR_COMMANDE = 500;
    private static final BigDecimal VALEUR_TRANCHE = BigDecimal.TEN;

    public int calculerPoints(BigDecimal montant, NiveauFidelite niveau) {
        Objects.requireNonNull(montant, "Le montant ne peut pas être nul");
        Objects.requireNonNull(niveau, "Le niveau ne peut pas être nul");
        if (montant.signum() < 0) {
            throw new IllegalArgumentException("Le montant ne peut pas être négatif");
        }
        BigDecimal points = montant.divide(VALEUR_TRANCHE, 0, RoundingMode.DOWN)
                .multiply(BigDecimal.valueOf(niveau.multiplicateur()));
        return points.min(BigDecimal.valueOf(PLAFOND_PAR_COMMANDE)).intValueExact();
    }
}

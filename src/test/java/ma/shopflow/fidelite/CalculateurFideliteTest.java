package ma.shopflow.fidelite;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculateurFideliteTest {
    private final CalculateurFidelite calculateur = new CalculateurFidelite();

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "9.99, 0",
            "10, 1",
            "19.99, 1",
            "25, 2"
    })
    void attribueUnPointParTrancheCompleteDeDix(String montant, int points) {
        assertEquals(points, calculateur.calculerPoints(new BigDecimal(montant), NiveauFidelite.STANDARD));
    }

    @ParameterizedTest
    @CsvSource({
            "10, 2",
            "25, 4"
    })
    void doubleLesPointsAuNiveauGold(String montant, int points) {
        assertEquals(points, calculateur.calculerPoints(new BigDecimal(montant), NiveauFidelite.GOLD));
    }

    @ParameterizedTest
    @CsvSource({
            "1250, 250",
            "5000, 500"
    })
    void plafonneLesPointsApresMultiplication(String montant, int points) {
        assertEquals(points, calculateur.calculerPoints(new BigDecimal(montant), NiveauFidelite.GOLD));
    }

    @ParameterizedTest
    @CsvSource({"-0.01", "-100"})
    void refuseUnMontantNegatif(String montant) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> calculateur.calculerPoints(new BigDecimal(montant), NiveauFidelite.STANDARD));
        assertEquals("Le montant ne peut pas être négatif", exception.getMessage());
    }
}

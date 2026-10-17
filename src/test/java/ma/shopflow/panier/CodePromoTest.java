package ma.shopflow.panier;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CodePromoTest {
    @ParameterizedTest
    @ValueSource(ints = {1, 25, 50})
    void accepteUnPourcentageValide(int pourcentage) {
        assertDoesNotThrow(() -> new CodePromo("PROMO", pourcentage));
    }

    @ParameterizedTest
    @ValueSource(ints = {-5, 0, 51, 100})
    void refuseUnPourcentageInvalide(int pourcentage) {
        assertThrows(IllegalArgumentException.class, () -> new CodePromo("PROMO", pourcentage));
    }
}

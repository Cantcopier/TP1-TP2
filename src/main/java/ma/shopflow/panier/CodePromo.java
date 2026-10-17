package ma.shopflow.panier;

import java.util.Objects;

public record CodePromo(String code, int pourcentage) {
    public CodePromo {
        Objects.requireNonNull(code, "Le code ne peut pas être nul");
        if (pourcentage < 1 || pourcentage > 50) {
            throw new IllegalArgumentException("Le pourcentage doit être compris entre 1 et 50");
        }
    }
}

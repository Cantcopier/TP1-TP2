package ma.shopflow.commande;

import java.math.BigDecimal;

public interface PaiementGateway {
    ResultatPaiement debiter(String clientId, BigDecimal montant);
}

package ma.shopflow.panier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Panier")
class PanierTest {
    private Panier panier;
    private Produit produit;

    @BeforeEach
    void setUp() {
        panier = new Panier();
        produit = new Produit("P1", "Produit 1", new BigDecimal("100.00"));
    }

    private void assertMontant(String attendu, BigDecimal actuel) {
        assertEquals(0, new BigDecimal(attendu).compareTo(actuel));
    }

    @Nested
    @DisplayName("R1 - panier vide")
    class PanierVide {
        @Test
        void totalVideComprendLesFraisDeLivraison() {
            assertMontant("30.00", panier.total());
        }
    }

    @Nested
    @DisplayName("R2 - ajout de produits")
    class AjoutProduits {
        @Test
        void ajouteUneLigne() {
            panier.ajouterProduit(produit, 1);
            assertEquals(1, panier.lignes().size());
            assertEquals(1, panier.lignes().getFirst().quantite());
        }

        @Test
        void fusionneLesAjoutsDuMemeProduit() {
            panier.ajouterProduit(produit, 2);
            panier.ajouterProduit(produit, 3);
            assertEquals(1, panier.lignes().size());
            assertEquals(5, panier.lignes().getFirst().quantite());
        }

        @Test
        void conserveLesProduitsDifferentsSurDesLignesDistinctes() {
            panier.ajouterProduit(produit, 1);
            panier.ajouterProduit(new Produit("P2", "Produit 2", new BigDecimal("25")), 1);
            assertEquals(2, panier.lignes().size());
        }
    }

    @Nested
    @DisplayName("R3 - limite de quantité")
    class QuantiteMaximale {
        @Test
        void accepteDixUnites() {
            panier.ajouterProduit(produit, 10);
            assertEquals(10, panier.lignes().getFirst().quantite());
        }

        @Test
        void refusePlusDeDixUnites() {
            assertThrows(QuantiteMaximaleDepasseeException.class,
                    () -> panier.ajouterProduit(produit, 11));
        }

        @Test
        void refuseUnAjoutIncrementalQuiDepasseDix() {
            panier.ajouterProduit(produit, 8);
            assertThrows(QuantiteMaximaleDepasseeException.class,
                    () -> panier.ajouterProduit(produit, 3));
            assertEquals(8, panier.lignes().getFirst().quantite());
        }
    }

    @Nested
    @DisplayName("R4 - sous-total")
    class SousTotal {
        @Test
        void calculeLeSousTotalDUneLigne() {
            panier.ajouterProduit(produit, 2);
            assertMontant("200.00", panier.sousTotal());
        }

        @Test
        void additionneLeSousTotalDesProduits() {
            panier.ajouterProduit(produit, 2);
            panier.ajouterProduit(new Produit("P2", "Produit 2", new BigDecimal("25.50")), 2);
            assertMontant("251.00", panier.sousTotal());
        }

        @Test
        void calculeLeSousTotalDeLaLigne() {
            assertMontant("300.00", new LignePanier(produit, 3).sousTotal());
        }
    }

    @Nested
    @DisplayName("R5 - frais de livraison")
    class FraisLivraison {
        @ParameterizedTest
        @CsvSource({"400, 30", "500, 0", "600, 0"})
        void appliqueLeSeuilDeLivraison(String montant, String frais) {
            Produit article = new Produit("LIVRAISON", "Article", new BigDecimal(montant));
            panier.ajouterProduit(article, 1);
            assertMontant(frais, panier.fraisLivraison());
        }

        @Test
        void factureLaLivraisonPourUnPanierVide() {
            assertMontant("30.00", panier.fraisLivraison());
        }
    }

    @Nested
    @DisplayName("R6 - code promotionnel")
    class CodePromotionnel {
        @Test
        void aucuneRemiseSansCode() {
            panier.ajouterProduit(produit, 2);
            assertMontant("0", panier.remise());
        }

        @Test
        void calculeLaRemiseEnPourcentage() {
            panier.ajouterProduit(produit, 2);
            panier.appliquerCodePromo(new CodePromo("PROMO25", 25));
            assertMontant("50.00", panier.remise());
        }

        @Test
        void calculeLaRemiseMaximale() {
            panier.ajouterProduit(produit, 2);
            panier.appliquerCodePromo(new CodePromo("PROMO50", 50));
            assertMontant("100.00", panier.remise());
        }
    }

    @Nested
    @DisplayName("R7 - total")
    class Total {
        @Test
        void ajouteLesFraisAuSousTotalSansCode() {
            panier.ajouterProduit(produit, 1);
            assertMontant("130.00", panier.total());
        }

        @Test
        void appliqueLaRemiseAuSousTotalEtPasAuxFrais() {
            panier.ajouterProduit(produit, 1);
            panier.appliquerCodePromo(new CodePromo("PROMO50", 50));
            assertMontant("80.00", panier.total());
        }

        @Test
        void nAjoutePasLesFraisSiLeSeuilEstAtteint() {
            panier.ajouterProduit(produit, 5);
            panier.appliquerCodePromo(new CodePromo("PROMO10", 10));
            assertMontant("450.00", panier.total());
        }
    }

    @Nested
    @DisplayName("R8 - validations")
    class Validations {
        @Test
        void refuseUneQuantiteNulle() {
            assertThrows(IllegalArgumentException.class, () -> panier.ajouterProduit(produit, 0));
        }

        @Test
        void refuseUnProduitNul() {
            assertThrows(NullPointerException.class, () -> panier.ajouterProduit(null, 1));
        }

        @Test
        void exposeUneVueNonModifiableDesLignes() {
            panier.ajouterProduit(produit, 1);
            assertAll(
                    () -> assertEquals(1, panier.lignes().size()),
                    () -> assertThrows(UnsupportedOperationException.class,
                            () -> panier.lignes().clear()));
        }
    }
}

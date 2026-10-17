package ma.shopflow.commande;

import ma.shopflow.panier.Panier;
import ma.shopflow.panier.Produit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceCommandeTest {
    @Mock private StockService stock;
    @Mock private PaiementGateway paiement;
    @Mock private CommandeRepository repository;
    @Mock private NotificationService notifications;
    @InjectMocks private ServiceCommande service;
    private Client client;
    private Panier panier;

    @BeforeEach
    void setUp() {
        client = new Client("C1", "client@example.com");
        panier = new Panier();
        panier.ajouterProduit(new Produit("P1", "Produit", new BigDecimal("100.00")), 2);
    }

    @Test
    void refuseUnPanierVideSansInteragirAvecLesDependances() {
        assertThrows(IllegalArgumentException.class,
                () -> service.passerCommande(client, new Panier()));
        verifyNoInteractions(stock, paiement, repository, notifications);
    }

    @Test
    void signaleUneRuptureSansReserverNiDebiter() {
        when(stock.estDisponible("P1", 2)).thenReturn(false);
        assertThrows(RuptureDeStockException.class, () -> service.passerCommande(client, panier));
        verify(stock, never()).reserver(anyString(), anyInt());
        verifyNoInteractions(paiement, repository, notifications);
    }

    @Test
    void reserveLesLignesAvantLePaiement() {
        when(stock.estDisponible("P1", 2)).thenReturn(true);
        when(paiement.debiter("C1", new BigDecimal("230.00")))
                .thenReturn(new ResultatPaiement(true, "PAY-1", null));
        service.passerCommande(client, panier);
        InOrder ordre = inOrder(stock, paiement);
        ordre.verify(stock).estDisponible("P1", 2);
        ordre.verify(stock).reserver("P1", 2);
        ordre.verify(paiement).debiter("C1", new BigDecimal("230.00"));
    }

    @Test
    void libereLeStockLorsqueLePaiementEstRefuse() {
        when(stock.estDisponible("P1", 2)).thenReturn(true);
        when(paiement.debiter(eq("C1"), any()))
                .thenReturn(new ResultatPaiement(false, null, "Carte refusée"));
        assertThrows(PaiementRefuseException.class, () -> service.passerCommande(client, panier));
        verify(stock).liberer("P1", 2);
        verify(repository, never()).enregistrer(any());
        verifyNoInteractions(notifications);
    }

    @Test
    void enregistreEtNotifieUneCommandeConfirmee() {
        when(stock.estDisponible("P1", 2)).thenReturn(true);
        when(paiement.debiter("C1", new BigDecimal("230.00")))
                .thenReturn(new ResultatPaiement(true, "PAY-1", null));
        ArgumentCaptor<Commande> captor = ArgumentCaptor.forClass(Commande.class);
        Commande resultat = service.passerCommande(client, panier);
        verify(repository).enregistrer(captor.capture());
        verify(notifications).envoyerConfirmation("client@example.com", resultat);
        assertAll(
                () -> assertEquals(StatutCommande.CONFIRMEE, resultat.statut()),
                () -> assertEquals("PAY-1", resultat.referencePaiement()),
                () -> assertEquals("C1", captor.getValue().clientId()),
                () -> assertEquals(new BigDecimal("230.00"), resultat.total()));
    }

    @Test
    void neFaitPasEchouerLaCommandeSiLaNotificationEchoue() {
        when(stock.estDisponible("P1", 2)).thenReturn(true);
        when(paiement.debiter("C1", new BigDecimal("230.00")))
                .thenReturn(new ResultatPaiement(true, "PAY-1", null));
        doThrow(new IllegalStateException("SMTP indisponible"))
                .when(notifications).envoyerConfirmation(eq("client@example.com"), any());
        Commande resultat = assertDoesNotThrow(() -> service.passerCommande(client, panier));
        assertEquals(StatutCommande.CONFIRMEE, resultat.statut());
        verify(repository).enregistrer(resultat);
    }
}

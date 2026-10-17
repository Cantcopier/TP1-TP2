# Anomalies corrigées

1. **TP1 - R5 : seuil de livraison gratuite**  
   Le seuil de 500,00 DH était traité comme strict. La livraison est maintenant gratuite dès que le sous-total atteint 500,00 DH, seuil inclus.
2. **TP1 - R6/R7 : assiette de la remise**  
   La remise promotionnelle pouvait réduire les frais de livraison. Elle ne s'applique désormais qu'au sous-total ; les frais sont ajoutés ensuite.
3. **TP2 - C5 : réservation de stock après refus de paiement**  
   Un refus de paiement laissait les articles réservés. Le service libère maintenant toutes les quantités réservées avant de signaler le refus.
4. **TP2 - C6 : échec de notification**  
   Une exception d'envoi d'e-mail pouvait faire échouer une commande pourtant payée et enregistrée. L'échec est maintenant journalisé sans annuler la commande.

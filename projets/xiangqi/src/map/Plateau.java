package map;

import java.awt.Color;

public class Plateau {

    private Piece[][] cases; // Plateau 11x11
    private boolean partieTerminee = false; // Indicateur pour savoir si la partie est terminée
    private int tour = 0; // Tour de jeu (0 = joueur rouge, 1 = joueur noir)
    private int dernierMouvementX, dernierMouvementY, dernierMouvementNewX, dernierMouvementNewY; // Pour restaurer l'état du plateau

    public Plateau() {
        cases = new Piece[11][11]; // Plateau 11x11
        initialiserPieces(); // Initialisation des pièces sur le plateau
    }

    // Initialisation des pièces sur le plateau
    private void initialiserPieces() {
        // Généraux
        cases[5][0] = new General(5, 0, Color.BLACK);
        cases[5][10] = new General(5, 10, Color.RED);

        // Gardes
        cases[4][0] = new Garde(4, 0, Color.BLACK);
        cases[6][0] = new Garde(6, 0, Color.BLACK);
        cases[4][10] = new Garde(4, 10, Color.RED);
        cases[6][10] = new Garde(6, 10, Color.RED);

        // Éléphants
        cases[2][0] = new Elephant(2, 0, Color.BLACK);
        cases[8][0] = new Elephant(8, 0, Color.BLACK);
        cases[2][10] = new Elephant(2, 10, Color.RED);
        cases[8][10] = new Elephant(8, 10, Color.RED);

        // Chariots (Tours)
        cases[0][0] = new Tour(0, 0, Color.BLACK);
        cases[10][0] = new Tour(10, 0, Color.BLACK);
        cases[0][10] = new Tour(0, 10, Color.RED);
        cases[10][10] = new Tour(10, 10, Color.RED);

        // Chevaux
        cases[1][0] = new Cheval(1, 0, Color.BLACK);
        cases[9][0] = new Cheval(9, 0, Color.BLACK);
        cases[1][10] = new Cheval(1, 10, Color.RED);
        cases[9][10] = new Cheval(9, 10, Color.RED);

        // Canons
        cases[1][2] = new Canon(1, 2, Color.BLACK);
        cases[9][2] = new Canon(9, 2, Color.BLACK);
        cases[1][8] = new Canon(1, 8, Color.RED);
        cases[9][8] = new Canon(9, 8, Color.RED);

        // Soldats
        cases[0][3] = new Soldat(0, 3, Color.BLACK);
        cases[2][3] = new Soldat(2, 3, Color.BLACK);
        cases[5][3] = new Soldat(5, 3, Color.BLACK);
        cases[8][3] = new Soldat(8, 3, Color.BLACK);
        cases[10][3] = new Soldat(10, 3, Color.BLACK);

        cases[0][7] = new Soldat(0, 7, Color.RED);
        cases[2][7] = new Soldat(2, 7, Color.RED);
        cases[5][7] = new Soldat(5, 7, Color.RED);
        cases[8][7] = new Soldat(8, 7, Color.RED);
        cases[10][7] = new Soldat(10, 7, Color.RED);
    }

    // Retourne la pièce située aux coordonnées (x, y)
    public Piece getPiece(int x, int y) {
        if (x < 0 || x >= 11 || y < 0 || y >= 11) {
            return null; // Si les coordonnées sont invalides, retourner null
        }
        return cases[x][y];
    }

    // Place une pièce à une nouvelle position
    public void setPiece(int x, int y, Piece piece) {
        if (x >= 0 && x < 11 && y >= 0 && y < 11) {
            cases[x][y] = piece; // Vérifier que les indices sont dans la plage valide (0 à 10)
        }
    }

    public Piece[][] getCases() {
        return cases; // Retourne l'ensemble du tableau de pièces
    }

    // Vérifie si le mouvement mettrait le général en échec
    private boolean metLeGeneralEnEchec(Piece piece, int x, int y, int newX, int newY) {
        // Sauvegarder la pièce capturée à la destination pour restauration
        Piece capture = getPiece(newX, newY);

        // Effectuer temporairement le mouvement
        setPiece(newX, newY, piece);
        setPiece(x, y, null);

        // Vérifier si le général est en échec
        boolean enEchec = estEnEchec(piece.getCouleur());

        // Restauration de la pièce capturée si nécessaire
        setPiece(x, y, piece);
        setPiece(newX, newY, capture);

        return enEchec;
    }

    // Méthode pour vérifier si la partie doit être arrêtée
    private void verifierFinDePartie(Piece piece, int x, int y, int newX, int newY) {
        if (generalFaceAFace()) {
            afficherMessageFinPartie("Les généraux sont face à face ! La partie est terminée !");
            partieTerminee = true;
        } else if (estEchecEtMat(piece.getCouleur())) {
            afficherMessageFinPartie("La partie est terminée : Échec et Mat !");
            partieTerminee = true;
        } else if (estPat(piece.getCouleur())) {
            afficherMessageFinPartie("La partie est terminée : Pat !");
            partieTerminee = true;
        }
    }

    // Déplacer une pièce d'une position à une autre
    public void deplacerPiece(int x, int y, int newX, int newY) {
        if (partieTerminee) {
            afficherMessageFinPartie("La partie est déjà terminée !");
            return; // Empêcher de déplacer une pièce si la partie est terminée
        }

        Piece piece = getPiece(x, y);
        Piece pieceCapturee = getPiece(newX, newY); // Sauvegarder la pièce capturée si elle existe

        if (piece != null) {
            // Vérifier si le mouvement mettrait le général en échec
            if (metLeGeneralEnEchec(piece, x, y, newX, newY)) {
                // Restaurer la pièce capturée si elle existe
                setPiece(newX, newY, pieceCapturee);
                System.out.println("Impossible de déplacer la pièce, le général serait en échec !");
                return; // Annuler le mouvement si le général serait en échec
            }

            // Effectuer le mouvement
            setPiece(newX, newY, piece);
            piece.setPosition(newX, newY);
            setPiece(x, y, null);

            // Vérifier si la partie est terminée après le mouvement
            verifierFinDePartie(piece, x, y, newX, newY);
        }
    }

    // Vérifier si le joueur de la couleur donnée est en échec
    public boolean estEnEchec(Color couleur) {
        // 1) Trouver les coordonnées du général de la couleur donnée
        int roiX = -1, roiY = -1;
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                Piece p = getPiece(x, y);
                if (p instanceof General && p.getCouleur() == couleur) {
                    roiX = x;
                    roiY = y;
                    break;
                }
            }
            if (roiX != -1) break;
        }

        // 2) Balayer toutes les pièces adverses
        Color couleurAdverse = (couleur == Color.RED) ? Color.BLACK : Color.RED;
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                Piece attaquante = getPiece(x, y);
                if (attaquante == null || attaquante.getCouleur() != couleurAdverse)
                    continue;

                // 3a) Si c'est un canon, test spécial
                if (attaquante instanceof Canon) {
                    if (canonPeutAttaquer((Canon) attaquante, roiX, roiY)) {
                        return true; // Le général est en échec
                    }
                }
                // 3b) Sinon, on teste simplement le mouvement normal
                else {
                    if (attaquante.mouvementValide(roiX, roiY, getCases())) {
                        return true; // Le général est en échec
                    }
                }
            }
        }

        return false; // Si aucune pièce ennemie ne peut attaquer le général, il n'est pas en échec
    }


    private boolean canonPeutAttaquer(Canon c, int xG, int yG) {
        int xC = c.getX(), yC = c.getY();
        // doit être aligné
        if (xC != xG && yC != yG) return false;

        int dx = Integer.signum(xG - xC);
        int dy = Integer.signum(yG - yC);

        int ecrans = 0;
        int x = xC + dx, y = yC + dy;

        // parcourir les cases strictement entre c et (xG,yG)
        while (x != xG || y != yG) {
            // si ce n'est pas la rivière (y==5) et qu'il y a une pièce, c'est un écran
            if (!(x == xC && y == 5) && getPiece(x, y) != null) {
                ecrans++;
            }
            x += dx;
            y += dy;
        }

        // la case (xG,yG) doit être occupée par le général adverse, et ecrans==1
        Piece cible = getPiece(xG, yG);
        return ecrans == 1
            && cible instanceof General
            && cible.getCouleur() != c.getCouleur();
    }
    
    /**
     * Retourne true s'il existe au moins un mouvement légal
     * pour le joueur de la couleur donnée qui sort de l'échec.
     */
    private boolean existeMouvementSauvant(Color couleur) {
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                Piece p = getPiece(x, y);
                if (p != null && p.getCouleur() == couleur) {
                    // pour chaque destination possible
                    for (int i = 0; i < 11; i++) {
                        for (int j = 0; j < 11; j++) {
                            if (!p.mouvementValide(i, j, cases)) continue;
                            // simuler
                            Piece cible = getPiece(i, j);
                            setPiece(i, j, p);
                            setPiece(x, y, null);
                            boolean enEchec = estEnEchec(couleur);
                            // restaurer
                            setPiece(x, y, p);
                            setPiece(i, j, cible);
                            if (!enEchec) {
                                return true;  // on a trouvé un coup qui sauve
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // Vérifier si le joueur est en échec et mat
    public boolean estEchecEtMat(Color couleur) {
        if (!estEnEchec(couleur)) return false;
        // si aucun coup ne peut sauver, c'est un mat
        return !existeMouvementSauvant(couleur);
    }

    // Vérifier si le joueur est en pat (aucun coup valide)
    public boolean estPat(Color couleur) {
        if (estEnEchec(couleur)) return false;

        // Vérifier s'il n'y a aucun mouvement valide pour ce joueur
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                Piece piece = getPiece(x, y);
                if (piece != null && piece.getCouleur() == couleur) {
                    for (int i = 0; i < 11; i++) {
                        for (int j = 0; j < 11; j++) {
                            if (piece.mouvementValide(i, j, getCases())) {
                                return false; // Il existe un mouvement valide
                            }
                        }
                    }
                }
            }
        }
        return true; // Pas de mouvements valides, c'est un pat
    }

    // Vérifier si le joueur est face à face avec l'autre général
    public boolean generalFaceAFace() {
        int generalRougeX = -1, generalRougeY = -1;
        int generalNoirX = -1, generalNoirY = -1;

        // Localiser les généraux
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                Piece piece = getPiece(x, y);
                if (piece instanceof General) {
                    if (piece.getCouleur() == Color.RED) {
                        generalRougeX = x;
                        generalRougeY = y;
                    } else if (piece.getCouleur() == Color.BLACK) {
                        generalNoirX = x;
                        generalNoirY = y;
                    }
                }
            }
        }

        // Vérifier si les généraux sont face à face (sur la même ligne ou colonne)
        if (generalRougeX == generalNoirX) {  // Générals sur la même colonne
            // Vérifier qu'aucune pièce n'est entre les généraux
            int minY = Math.min(generalRougeY, generalNoirY);
            int maxY = Math.max(generalRougeY, generalNoirY);
            for (int y = minY + 1; y < maxY; y++) {
                if (getPiece(generalRougeX, y) != null) {
                    return false; // Il y a une pièce entre les généraux
                }
            }
            return true; // Les généraux sont face à face
        }

        return false; // Les généraux ne sont pas sur la même colonne
    }

    // Afficher un message de fin de partie
    private void afficherMessageFinPartie(String message) {
        System.out.println(message);
    }
    
    public boolean isPartieTerminee() {
        return partieTerminee;
    }
    
    public void setPartieTerminee(boolean fini) {
    	partieTerminee = fini;
    }
}

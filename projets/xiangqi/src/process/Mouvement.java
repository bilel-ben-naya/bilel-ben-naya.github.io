package process;

import map.*;
import java.awt.Color;
import javax.swing.JOptionPane;

public class Mouvement {

    /**
     * Tente d'effectuer un mouvement et, en cas de réussite,
     * vérifie si le général adverse est en échec.
     */
	public static boolean effectuerMouvement(Piece piece, int xDepart, int yDepart, int xArrivee, int yArrivee, Plateau plateau) {
	    // 1) Vérifications basiques
	    if (piece == null) {
	        System.out.println("Aucune pièce à déplacer.");
	        return false;
	    }
	    if (plateau.isPartieTerminee()) {
	        System.out.println("La partie est déjà terminée.");
	        return false;
	    }
	    if (!piece.mouvementValide(xArrivee, yArrivee, plateau.getCases())) {
	        System.out.println("Mouvement invalide.");
	        return false;
	    }
	    Piece destination = plateau.getPiece(xArrivee, yArrivee);
	    if (destination != null && destination.getCouleur() == piece.getCouleur()) {
	        System.out.println("Impossible de capturer une pièce alliée.");
	        return false;
	    }

	    // 2) Simulation : on sauvegarde la pièce capturée (peut être null)
	    Piece captureTemp = plateau.getPiece(xArrivee, yArrivee);
	    //    on joue le coup
	    plateau.setPiece(xArrivee, yArrivee, piece);
	    plateau.setPiece(xDepart, yDepart, null);
	    //    on teste l’échec sur le général du joueur courant
	    boolean metEnEchec = plateau.estEnEchec(piece.getCouleur());
	    //    on restaure l’état initial
	    plateau.setPiece(xDepart, yDepart, piece);
	    plateau.setPiece(xArrivee, yArrivee, captureTemp);
	    if (metEnEchec) {
	        System.out.println("Mouvement annulé : le général serait en échec !");
	        return false;
	    }

	    // 3) Coup validé : on l’exécute vraiment
	    plateau.deplacerPiece(xDepart, yDepart, xArrivee, yArrivee);

	    // 4) Détection et message “Échec” vs “Échec et mat”
	    Color couleurAdverse = (piece.getCouleur() == Color.RED) ? Color.BLACK : Color.RED;
	    String nom = (couleurAdverse == Color.RED) ? "Rouge" : "Noir";

	    if (plateau.estEchecEtMat(couleurAdverse)) {
	        JOptionPane.showMessageDialog(
	            null,
	            "Échec et mat au général " + nom + " !",
	            "Échec et mat",
	            JOptionPane.INFORMATION_MESSAGE
	        );
	        plateau.setPartieTerminee(true);
	    }
	    else if (plateau.estEnEchec(couleurAdverse)) {
	        JOptionPane.showMessageDialog(
	            null,
	            "Échec au général " + nom + " !",
	            "Échec",
	            JOptionPane.WARNING_MESSAGE
	        );
	    }

	    return true;
	}



    /**
     * Simule le déplacement pour vérifier si le général du joueur
     * serait mis en échec.ça modifie pas le plateau.
     */
	public static boolean metLeGeneralEnEchec(Piece piece, int x, int y, int newX, int newY, Plateau plateau) {
	    // Sauvegarde de la pièce présente sur newX,newY (peut être null)
	    Piece captureTemp = plateau.getPiece(newX, newY);

	    // 1) Simuler
	    plateau.setPiece(newX, newY, piece);
	    plateau.setPiece(x, y, null);

	    // 2) Test
	    boolean enEchec = plateau.estEnEchec(piece.getCouleur());

	    // 3) Restauration
	    plateau.setPiece(x, y, piece);
	    plateau.setPiece(newX, newY, captureTemp);

	    return enEchec;
	}

    // Valider le mouvement d'une pièce
    public static boolean validerMouvement(Piece piece, int xDepart, int yDepart, int xArrivee, int yArrivee, Piece[][] plateau) {
        if (piece instanceof Soldat) {
            return validerMouvementSoldat((Soldat) piece, xDepart, yDepart, xArrivee, yArrivee, plateau);
        } else if (piece instanceof General) {
            return validerMouvementGeneral((General) piece, xDepart, yDepart, xArrivee, yArrivee, plateau);
        } else if (piece instanceof Tour) {
            return validerMouvementTour((Tour) piece, xDepart, yDepart, xArrivee, yArrivee, plateau);
        } else if (piece instanceof Canon) {
            return validerMouvementCanon((Canon) piece, xDepart, yDepart, xArrivee, yArrivee, plateau);
        } else if (piece instanceof Cheval) {
            return validerMouvementCheval((Cheval) piece, xDepart, yDepart, xArrivee, yArrivee, plateau);
        } else if (piece instanceof Elephant) {
            return validerMouvementElephant((Elephant) piece, xDepart, yDepart, xArrivee, yArrivee, plateau);
        } else if (piece instanceof Garde) {
            return validerMouvementGarde((Garde) piece, xDepart, yDepart, xArrivee, yArrivee, plateau);
        }
        return false; // Par défaut, mouvement invalide
    }


    // Valider le mouvement pour un Cheval
    private static boolean validerMouvementCheval(Cheval piece, int xDepart, int yDepart, int xArrivee, int yArrivee, Piece[][] plateau) {
        // Le cheval ne peut pas se déplacer sur la rivière (y = 5)
        if (yArrivee == 5) return false; // Le cheval ne peut pas se déplacer sur la rivière

        // Vérifier si le cheval doit traverser la rivière
        boolean traverseRiviere = (yDepart == 4 && yArrivee == 6) || (yDepart == 6 && yArrivee == 4) ||
                                  (yDepart == 4 && yArrivee == 7) || (yDepart == 6 && yArrivee == 3);

        // Vérification de mouvement spécial
        boolean mouvementSpecial = (yDepart == 3 && yArrivee == 6) || (yDepart == 7 && yArrivee == 4);

        // Déplacement standard en "L"
        boolean mouvementNormal = (Math.abs(xArrivee - xDepart) == 2 && Math.abs(yArrivee - yDepart) == 1) ||
                                  (Math.abs(xArrivee - xDepart) == 1 && Math.abs(yArrivee - yDepart) == 2);

        // Si le cheval doit traverser la rivière, adapter le déplacement
        if (traverseRiviere) {
            return (Math.abs(xArrivee - xDepart) == 2 && Math.abs(yArrivee - yDepart) == 2) ||
                   (Math.abs(xArrivee - xDepart) == 1 && Math.abs(yArrivee - yDepart) == 3);
        }

        // Gestion des mouvements spéciaux (ex: de y = 3 à y = 6 ou de y = 7 à y = 4)
        if (mouvementSpecial) {
            return (Math.abs(xArrivee - xDepart) == 2 && Math.abs(yArrivee - yDepart) == 1) ||
                   (Math.abs(xArrivee - xDepart) == 1 && Math.abs(yArrivee - yDepart) == 3);
        }

        // Si le mouvement est un "L" normal
        if (mouvementNormal) {
            // Vérifier l'obstruction : la case immédiatement adjacente dans la direction du déplacement

            // Si la direction est horizontale (déplacement de 2 cases horizontalement)
            if (Math.abs(xArrivee - xDepart) == 2) {
                // Vérifier la case juste à côté dans la direction horizontale (xDepart + 1 ou -1 selon la direction)
                int xObstacle = (xArrivee > xDepart) ? xDepart + 1 : xDepart - 1;
                if (plateau[xObstacle][yDepart] != null) { // Il y a une pièce sur la case a cote
                    return false; // Le cheval est bloqué
                }
            }

            // Si la direction est verticale (déplacement de 2 cases verticalement)
            if (Math.abs(yArrivee - yDepart) == 2) {
                // Vérifier la case juste à côté dans la direction verticale (yDepart + 1 ou -1 selon la direction)
                int yObstacle = (yArrivee > yDepart) ? yDepart + 1 : yDepart - 1;
                if (plateau[xDepart][yObstacle] != null) { // Il y a une pièce sur la case a cote
                    return false; // Le cheval est bloqué
                }
            }

            return true; // Le mouvement est valide si aucune obstruction n'est rencontrée
        }

        return false; // Si ce n'est pas un mouvement en "L", il est invalide
    }


    private static boolean validerMouvementElephant(Elephant piece, int xDepart, int yDepart, int xArrivee, int yArrivee, Piece[][] plateau) {
        // Définir les positions valides pour l'éléphant noir
        int[][] casesValidesNoir = {
            {2, 0}, {2, 4}, {8, 0}, {8, 4}, {0, 2}, {5, 2}, {10, 2}  // Cases valides pour l'éléphant noir
        };

        // Définir les positions valides pour l'éléphant rouge
        int[][] casesValidesRouge = {
            {2, 10}, {2, 6}, {8, 10}, {8, 6}, {0, 8}, {5, 8}, {10, 8}  // Cases valides pour l'éléphant rouge
        };

        // Vérifier que la destination est dans les cases valides
        int[][] casesValides;
        if (piece.getCouleur() == Color.RED) {
            casesValides = casesValidesRouge; // Si l'éléphant est rouge
        } else {
            casesValides = casesValidesNoir; // Si l'éléphant est noir
        }

        // Vérifier si la destination fait partie des cases valides
        boolean destinationValide = false;
        for (int[] caseValide : casesValides) {
            if (caseValide[0] == xArrivee && caseValide[1] == yArrivee) {
                destinationValide = true;
                break;
            }
        }

        if (!destinationValide) {
            return false; // La destination n'est pas valide pour l'éléphant
        }

        // Vérifier la contrainte du rayon : pas plus de 3 cases sur le côté (X) et pas plus de 2 cases en hauteur (Y)
        if (Math.abs(xArrivee - xDepart) > 3 || Math.abs(yArrivee - yDepart) > 2) {
            return false; // L'éléphant ne peut pas se déplacer trop loin
        }

        return true; // Le mouvement est valide
    }

    // Valider le mouvement pour un Soldat
    private static boolean validerMouvementSoldat(Soldat piece, int xDepart, int yDepart, int xArrivee, int yArrivee, Piece[][] plateau) {
        // Empêcher d'arrêter sur la rivière
        if (yArrivee == 5) return false;

        if (piece.getCouleur() == Color.BLACK) {
            // Si le soldat est rouge et se trouve sur la ligne 4, il peut sauter la rivière (y=4 -> y=6)
            if (yDepart == 4 && yArrivee == 6 && xArrivee == xDepart) return true; // Sauter la rivière
            if (yDepart <= 4) return (xArrivee == xDepart && yArrivee == yDepart + 1); // Avancer d'une case
            else return (xArrivee == xDepart && yArrivee == yDepart + 1) || (Math.abs(xArrivee - xDepart) == 1 && yArrivee == yDepart); // Avancer ou se déplacer latéralement
        } else {
            // Si le soldat est noir et se trouve sur la ligne 6, il peut sauter la rivière (y=6 -> y=4)
            if (yDepart == 6 && yArrivee == 4 && xArrivee == xDepart) return true; // Sauter la rivière
            if (yDepart >= 6) return (xArrivee == xDepart && yArrivee == yDepart - 1); // Avancer d'une case vers le haut
            else return (xArrivee == xDepart && yArrivee == yDepart - 1) || (Math.abs(xArrivee - xDepart) == 1 && yArrivee == yDepart); // Avancer ou se déplacer latéralement
        }
    }

    // Valider le mouvement pour un Général
    private static boolean validerMouvementGeneral(General piece, int xDepart, int yDepart, int xArrivee, int yArrivee, Piece[][] plateau) {
        if (Math.abs(xArrivee - xDepart) + Math.abs(yArrivee - yDepart) != 1) {
            return false;
        }
        return (xArrivee >= 3 && xArrivee <= 7) && (yArrivee <= 2 || yArrivee >= 8);
    }

    // Valider le mouvement pour une Tour
    private static boolean validerMouvementTour(Tour piece, int xDepart, int yDepart, int xArrivee, int yArrivee, Piece[][] plateau) {
        // La rivière ne compte pas comme une case utilisable
        if (yArrivee == 5) return false;

        // Vérifier si le déplacement est strictement vertical ou horizontal
        if (xArrivee == xDepart) { // Déplacement vertical
            int step = (yArrivee > yDepart) ? 1 : -1;
            
            for (int i = yDepart + step; i != yArrivee; i += step) {
                // Vérifier que l'indice est dans les limites du plateau
                if (i < 0 || i >= 11) return false; // Si l'indice sort des limites du plateau, retourner false
                
                if (i == 5) continue; // Ignorer la rivière (ligne y = 5)
                if (plateau[xDepart][i] != null) return false; // Si un obstacle est rencontré, mouvement invalide
            }
        } else if (yArrivee == yDepart) { // Déplacement horizontal
            int step = (xArrivee > xDepart) ? 1 : -1;

            for (int i = xDepart + step; i != xArrivee; i += step) {
                // Vérifier que l'indice est dans les limites du plateau
                if (i < 0 || i >= 11) return false; // Si l'indice sort des limites du plateau, retourner false
                
                if (plateau[i][yDepart] != null) return false; // Si un obstacle est rencontré, mouvement invalide
            }
        } else {
            return false; // Si ce n'est pas un déplacement vertical ou horizontal, le mouvement est invalide
        }

        // Vérification de la destination : la case d'arrivée ne peut pas être occupée par une pièce alliée
        if (plateau[xArrivee][yArrivee] != null && plateau[xArrivee][yArrivee].getCouleur().equals(piece.getCouleur())) {
            return false; // La tour ne peut pas capturer une pièce alliée
        }

        return true; // Si la destination est libre ou occupée par une pièce ennemie, le déplacement est valide
    }


    // Valider le mouvement pour un Canon
    public static boolean validerMouvementCanon(Canon piece, int xDepart, int yDepart, int xArrivee, int yArrivee, Piece[][] plateau) {
        // Le canon ne peut pas se déplacer sur la rivière (y = 5)
        if (yArrivee == 5) return false;

        // Le canon doit se déplacer soit horizontalement soit verticalement
        if (xDepart != xArrivee && yDepart != yArrivee) {
            return false; // Le canon ne peut se déplacer qu'en ligne droite, horizontalement ou verticalement
        }

        int obstacles = 0;  // Compteur d'obstacles sur le chemin

        // Vérification du déplacement vertical (mouvement sur la même colonne)
        if (xDepart == xArrivee) {
            int step = (yArrivee > yDepart) ? 1 : -1;

            // Vérifier les cases entre la position de départ et la position d'arrivée
            for (int y = yDepart + step; y != yArrivee; y += step) {
                if (y == 5) continue;  // Ignorer la rivière (ligne y = 5)
                if (y < 0 || y >= 11) return false; // Vérification si l'indice est hors limites
                if (plateau[xDepart][y] != null) obstacles++;  // Compter les pièces sur le chemin
            }
        }

        // Vérification du déplacement horizontal (mouvement sur la même ligne)
        else if (yDepart == yArrivee) {
            int step = (xArrivee > xDepart) ? 1 : -1;

            // Vérifier les cases entre la position de départ et la position d'arrivée
            for (int x = xDepart + step; x != xArrivee; x += step) {
                if (x < 0 || x >= 11) return false; // Vérification si l'indice est hors limites
                if (plateau[x][yDepart] != null) obstacles++;  // Compter les pièces sur le chemin
            }
        }

        // Vérification de la destination : la case d'arrivée ne peut être occupée par une pièce alliée
        if (plateau[xArrivee][yArrivee] != null && plateau[xArrivee][yArrivee].getCouleur().equals(piece.getCouleur())) {
            return false; // Le canon ne peut pas capturer une pièce alliée
        }

        // Si la destination est vide, le canon peut se déplacer uniquement s'il n'y a pas d'obstacles
        if (plateau[xArrivee][yArrivee] == null) {
            return obstacles == 0;  // Le canon ne peut se déplacer que s'il n'y a pas d'obstacle
        }

        // Si la destination est occupée par une pièce ennemie, il peut capturer mais seulement s'il y a exactement une pièce sur le chemin
        return obstacles == 1;  // Le canon peut capturer si une seule pièce est entre lui et la destination
    }


    // Valider le mouvement pour une Garde
    private static boolean validerMouvementGarde(Garde piece, int xDepart, int yDepart, int xArrivee, int yArrivee, Piece[][] plateau) {
        if (Math.abs(xArrivee - xDepart) == 1 && Math.abs(yArrivee - yDepart) == 1) {
            return (xArrivee >= 3 && xArrivee <= 7) && (yArrivee <= 2 || yArrivee >= 8);
        }
        return false;
    }
}

package process;

import map.*;
import process.Mouvement;
import java.awt.Color;
import java.util.*;

public class Bot {
    private final Plateau plateau;
    private final Color couleur = Color.BLACK;  // le bot joue Noir

    // Mémoire du dernier coup du bot {x0,y0,x1,y1}
    private int[] lastMove = null;

    // Valeurs standard des pièces pour la capture
    private static final Map<Class<?>, Integer> VAL_PIECE = Map.of(
            Soldat.class,   1,
            Garde.class,    2,
            Elephant.class, 2,
            Cheval.class,   4,
            Canon.class,    5,
            Tour.class,     9,
            General.class, 1000
    );

    public Bot(Plateau plateau) {
        this.plateau = plateau;
    }

    /** Calcule le meilleur coup en rejetant ceux qui mettent le général en échec. */
    public int[] calculerMeilleurCoup() {
        int meilleurScore = Integer.MIN_VALUE;
        int[] meilleurCoup = null;

        // Récupère les coordonnées du Général noir
        int genX = -1, genY = -1;
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                Piece tmp = plateau.getPiece(i, j);
                if (tmp instanceof General && tmp.getCouleur() == couleur) {
                    genX = i; genY = j;
                }
            }
        }

        // Pour chaque pièce du bot
        for (int x0 = 0; x0 < 11; x0++) {
            for (int y0 = 0; y0 < 11; y0++) {
                Piece p = plateau.getPiece(x0, y0);
                if (p == null || p.getCouleur() != couleur) continue;

                // Tester chaque destination
                for (int x1 = 0; x1 < 11; x1++) {
                    for (int y1 = 0; y1 < 11; y1++) {
                        Piece cible = plateau.getPiece(x1, y1);
                        // Filtre de base
                        if (cible != null && cible.getCouleur() == couleur) continue;
                        if (!p.mouvementValide(x1, y1, plateau.getCases())) continue;

                        // --- Simulation du coup ---
                        plateau.setPiece(x1, y1, p);
                        plateau.setPiece(x0, y0, null);

                        // 0) Rejeter si le général serait en échec
                        if (Mouvement.metLeGeneralEnEchec(p, x0, y0, x1, y1, plateau)) {
                            // Restauration
                            plateau.setPiece(x0, y0, p);
                            plateau.setPiece(x1, y1, cible);
                            continue;
                        }

                        int score = 0;
                        // 1) Capture
                        if (cible != null) {
                            score += 10 * VAL_PIECE.getOrDefault(cible.getClass(), 0);
                        }
                        // 2) Pénalité Général sans capture
                        if (p instanceof General && cible == null) {
                            score -= 5;
                        }
                        // 3) Avancement Soldat
                        if (p instanceof Soldat) {
                            int dy = (couleur == Color.BLACK) ? (y1 - y0) : (y0 - y1);
                            if (dy > 0) score += 2 * dy;
                        }
                        // 4) Mobilité
                        score += compterCoupsLegaux(p, x1, y1);
                        // 5) Menace
                        score += 3 * compterMenaces(p, x1, y1);
                        // 6) Protection du Général
                        if (Math.abs(x1 - genX) + Math.abs(y1 - genY) == 1) {
                            score += 5;
                        }
                        // 7) Pénalité allers-retours
                        if (lastMove != null
                                && lastMove[0] == x1 && lastMove[1] == y1
                                && lastMove[2] == x0 && lastMove[3] == y0) {
                            score -= 15;
                        }
                        // 8) Pénalité risque de capture (–15 par menace adverse)
                        int menaceAdv = compterMenacesAdverses(x1, y1);
                        score -= 15 * menaceAdv;

                        // --- Restauration après évaluation ---
                        plateau.setPiece(x0, y0, p);
                        plateau.setPiece(x1, y1, cible);

                        // Mémorisation si c’est le meilleur
                        if (score > meilleurScore) {
                            meilleurScore = score;
                            meilleurCoup  = new int[]{x0, y0, x1, y1};
                        }
                    }
                }
            }
        }

        return meilleurCoup;
    }

    /** Joue le meilleur coup trouvé et met à jour lastMove. */
    public int[] jouer() {
        //Ne rien faire si la partie est terminée
        if (plateau.isPartieTerminee()) {
            return null;
        }
        int[] coup = calculerMeilleurCoup();
        if (coup != null) {
            plateau.deplacerPiece(coup[0], coup[1], coup[2], coup[3]);
            lastMove = coup;
            System.out.println("Bot déplace (" +
                    coup[0] + "," + coup[1] + ")→(" +
                    coup[2] + "," + coup[3] + ")");
        }
        return coup;
    }

    /** Compte les coups légaux de p depuis (x0,y0). */
    private int compterCoupsLegaux(Piece p, int x0, int y0) {
        int cnt = 0;
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                if (p.mouvementValide(x, y, plateau.getCases())) cnt++;
            }
        }
        return cnt;
    }

    /** Compte les pièces ennemies que p menace depuis (x0,y0). */
    private int compterMenaces(Piece p, int x0, int y0) {
        int men = 0;
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                Piece adv = plateau.getPiece(x, y);
                if (adv != null && adv.getCouleur() != couleur
                        && p.mouvementValide(x, y, plateau.getCases())) {
                    men++;
                }
            }
        }
        return men;
    }

    /** Compte les coups adv. qui menacent (x0,y0), pour pénalité risque. */
    private int compterMenacesAdverses(int x0, int y0) {
        int men = 0;
        Color advColor = (couleur == Color.BLACK) ? Color.RED : Color.BLACK;
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                Piece p = plateau.getPiece(x, y);
                if (p != null && p.getCouleur() == advColor
                        && p.mouvementValide(x0, y0, plateau.getCases())) {
                    men++;
                }
            }
        }
        return men;
    }
}

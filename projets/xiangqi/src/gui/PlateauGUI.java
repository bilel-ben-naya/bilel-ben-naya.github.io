package gui;

import map.*;
import process.Mouvement;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class PlateauGUI extends JPanel {
    private final Plateau plateau;
    private int TAILLE_CASE = 60;
    private static final int MARGE = 10;

    private Piece pieceSelectionnee = null;
    private int xSelectionne = -1, ySelectionne = -1;
    private Color joueurActuel = Color.RED;

    private final AffichagePieces affichagePieces;
    private final JLabel turnLabel;
    private final JLabel messageLabel;
    final JTextArea historiqueArea;
    private final JScrollPane historiqueScroll;

    public PlateauGUI(Plateau plateau) {
        this.plateau = plateau;
        setLayout(null);
        setPreferredSize(new Dimension(1000, 800));
        setBackground(new Color(245, 245, 220)); // fond beige clair

        affichagePieces = new AffichagePieces();

        // Label du tour
        turnLabel = new JLabel("Tour du joueur Rouge", SwingConstants.CENTER);
        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 20f));
        turnLabel.setOpaque(true);
        turnLabel.setBackground(getBackground());
        add(turnLabel);

        // Label pour un seul message d’erreur
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 14f));
        messageLabel.setForeground(Color.RED);
        messageLabel.setOpaque(true);
        messageLabel.setBackground(getBackground());
        add(messageLabel);

        // Zone d’historique, 3/4 de la hauteur
        historiqueArea = new JTextArea();
        historiqueArea.setEditable(false);
        historiqueArea.setBackground(new Color(255, 255, 240)); // zone plus claire
        historiqueScroll = new JScrollPane(historiqueArea);
        historiqueScroll.getViewport().setBackground(new Color(255, 255, 240));
        historiqueScroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(historiqueScroll);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutComponents();
                repaint();
            }
        });
    }

    private void layoutComponents() {
        int w = getWidth() - 300;
        int h = getHeight();
        int infoHeight = h / 4;
        int histHeight = h - infoHeight;
        int turnH = infoHeight * 3 / 4;
        int msgH  = infoHeight - turnH;

        turnLabel.setBounds(w, 0, 300, turnH);
        messageLabel.setBounds(w, turnH, 300, msgH);
        historiqueScroll.setBounds(w, infoHeight, 300, histHeight);
    }

    public int getTailleCase() {
        return TAILLE_CASE;
    }

    public boolean gererClic(int x, int y) {
        if (plateau.isPartieTerminee()) return false;

        Piece clic = plateau.getPiece(x, y);
        if (pieceSelectionnee == null) {
            if (clic != null && clic.getCouleur() == joueurActuel) {
                pieceSelectionnee = clic;
                xSelectionne = x;
                ySelectionne = y;
            }
            repaint();
            return false;
        }
        if (clic != null && clic.getCouleur() == joueurActuel) {
            pieceSelectionnee = clic;
            xSelectionne = x;
            ySelectionne = y;
            repaint();
            return false;
        }
        if (!pieceSelectionnee.mouvementValide(x, y, plateau.getCases())) {
            pieceSelectionnee = null;
            messageLabel.setText("Mouvement invalide !");
            repaint();
            return false;
        }

        int oldX = xSelectionne, oldY = ySelectionne;
        boolean succes = Mouvement.effectuerMouvement(
                pieceSelectionnee, oldX, oldY, x, y, plateau
        );
        pieceSelectionnee = null;

        if (!succes) {
            messageLabel.setText("Ce mouvement met le général en échec !");
            repaint();
            return false;
        }

        String dep = convertirCoordonnee(oldX, oldY);
        String arr = convertirCoordonnee(x, y);
        historiqueArea.append(dep + arr + "\n");
        changerTour();

        messageLabel.setText("");
        repaint();
        return true;
    }

    public void changerTour() {
        joueurActuel = (joueurActuel == Color.RED) ? Color.BLACK : Color.RED;
        turnLabel.setText("Tour du joueur " +
                (joueurActuel == Color.RED ? "Rouge" : "Noir"));
    }

    public String convertirCoordonnee(int x, int y) {
        char c = (char)('A' + x);
        int l = 11 - y;
        return "" + c + l;
    }

    private void adapterTaillePlateau() {
        int w = getWidth(), h = getHeight();
        TAILLE_CASE = Math.min((w - MARGE - 300) / 11, (h - MARGE) / 11);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        adapterTaillePlateau();
        dessinerPlateau(g);

        // Dessiner les pièces
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                Piece piece = plateau.getPiece(x, y);
                if (piece != null) {
                    affichagePieces.dessiner(g, piece, TAILLE_CASE, MARGE);
                }
            }
        }

        dessinerCasesValides(g);  // Afficher les cases valides
        if (pieceSelectionnee != null) {
            surbrillanceCaseSelectionnee(g); // Mettre en surbrillance la pièce sélectionnée
        }
    }

    // Dessiner le plateau avec les cases redimensionnées
    private void dessinerPlateau(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Color marron = new Color(160, 120, 90);
        Color marronClair = new Color(190, 150, 120);
        Color beige = new Color(245, 245, 220);
        Color beigeFonce = new Color(230, 220, 190);
        Color bleuRivière = new Color(100, 140, 200);

        // Dessiner les cases
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                if (j < 5) {
                    g.setColor(marron);
                } else if (j > 5) {
                    g.setColor(beige);
                } else {
                    continue;
                }
                g.fillRect(i * TAILLE_CASE + MARGE, j * TAILLE_CASE + MARGE, TAILLE_CASE, TAILLE_CASE);
            }
        }

        // Dessin des palais
        g.setColor(marronClair);
        for (int i = 3; i < 8; i++) {
            for (int j = 0; j < 3; j++) {
                g.fillRect(i * TAILLE_CASE + MARGE, j * TAILLE_CASE + MARGE, TAILLE_CASE, TAILLE_CASE);
            }
        }

        g.setColor(beigeFonce);
        for (int i = 3; i < 8; i++) {
            for (int j = 8; j < 11; j++) {
                g.fillRect(i * TAILLE_CASE + MARGE, j * TAILLE_CASE + MARGE, TAILLE_CASE, TAILLE_CASE);
            }
        }

        g.setColor(Color.BLACK); // Lignes de séparation en noir
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i <= 11; i++) {
            g.drawLine(i * TAILLE_CASE + MARGE, MARGE, i * TAILLE_CASE + MARGE, 11 * TAILLE_CASE + MARGE);
            g.drawLine(MARGE, i * TAILLE_CASE + MARGE, 11 * TAILLE_CASE + MARGE, i * TAILLE_CASE + MARGE);
        }
        
        // Dessiner la rivière
        g.setColor(bleuRivière);
        g.fillRect(MARGE, 5 * TAILLE_CASE + MARGE, 11 * TAILLE_CASE, TAILLE_CASE);
    }

    // Affichage des cases valides où la pièce peut se déplacer
    private void dessinerCasesValides(Graphics g) {
        if (pieceSelectionnee == null) return;

        g.setColor(new Color(0, 255, 0, 100));

        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                // Vérifier si le mouvement est valide et si la case est vide ou contient une pièce ennemie
                if (pieceSelectionnee.mouvementValide(i, j, plateau.getCases()) && 
                   (plateau.getPiece(i, j) == null || plateau.getPiece(i, j).getCouleur() != pieceSelectionnee.getCouleur())) {
                    g.fillRect(i * TAILLE_CASE + MARGE, j * TAILLE_CASE + MARGE, TAILLE_CASE, TAILLE_CASE);
                }
            }
        }
    }

    // Mettre en surbrillance la case de la pièce sélectionnée
    private void surbrillanceCaseSelectionnee(Graphics g) {
        g.setColor(Color.YELLOW);
        g.drawRect(xSelectionne * TAILLE_CASE + MARGE, ySelectionne * TAILLE_CASE + MARGE, TAILLE_CASE, TAILLE_CASE);
    }


}


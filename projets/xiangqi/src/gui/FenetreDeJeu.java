package gui;

import map.Plateau;
import process.Bot;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FenetreDeJeu extends JFrame {
    private final Plateau plateau;
    private final PlateauGUI plateauGUI;
    private final boolean estContreLeBot;
    private Bot bot;
    private boolean tourJoueur = true; // true=Rouge, false=Noir

    public FenetreDeJeu(boolean contreLeBot) {
        super("Chess");
        this.estContreLeBot = contreLeBot;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Plateau central
        plateau = new Plateau();
        plateauGUI = new PlateauGUI(plateau);
        add(plateauGUI, BorderLayout.CENTER);

        if (estContreLeBot) bot = new Bot(plateau);

        // Menu avec Accueil et Quitter
        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("Jeu");

        // Accueil : revient à MenuPrincipal
        JMenuItem accueil = new JMenuItem("Accueil");
        accueil.addActionListener(ev -> {
            MenuPrincipal menuPrincipal = new MenuPrincipal();
            menuPrincipal.setVisible(true);
            dispose();
        });
        menu.add(accueil);

        // Quitter
        JMenuItem quit = new JMenuItem("Quitter");
        quit.addActionListener(ev -> System.exit(0));
        menu.add(quit);

        mb.add(menu);
        setJMenuBar(mb);

        setVisible(true);

        // Listener
        plateauGUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (plateau.isPartieTerminee()) return;
                if (estContreLeBot && !tourJoueur) return;

                int x = e.getX() / plateauGUI.getTailleCase();
                int y = e.getY() / plateauGUI.getTailleCase();

                // Coup joueur
                boolean moved = plateauGUI.gererClic(x, y);
                if (!moved) return;

                if (plateau.estEchecEtMat(Color.BLACK)) {
                    JOptionPane.showMessageDialog(
                            FenetreDeJeu.this,
                            "Échec et mat ! Le joueur Rouge gagne.",
                            "Fin de partie",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    plateau.setPartieTerminee(true);
                    return;
                }

                // Si contre bot, joue le bot
                if (estContreLeBot) {
                    tourJoueur = false;
                    SwingUtilities.invokeLater(() -> {
                        int[] coupBot = bot.jouer();
                        if (coupBot != null) {
                            plateauGUI.historiqueArea.append(
                                    plateauGUI.convertirCoordonnee(coupBot[0], coupBot[1]) +
                                            plateauGUI.convertirCoordonnee(coupBot[2], coupBot[3]) +
                                            "\n"
                            );
                        }
                        plateauGUI.repaint();

                        if (plateau.estEchecEtMat(Color.RED)) {
                            JOptionPane.showMessageDialog(
                                    FenetreDeJeu.this,
                                    "Échec et mat ! Le joueur Noir gagne.",
                                    "Fin de partie",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                            plateau.setPartieTerminee(true);
                            return;
                        }

                        // Changer de tour pour le joueur 
                        plateauGUI.changerTour();
                        tourJoueur = true;
                    });
                } else {
                    // PVP : le changement de tour est déjà fait dans gererClic()
                    tourJoueur = !tourJoueur;
                }
            }
        });
    }

    public static void main(String[] args) {
        boolean modeBot = JOptionPane.showConfirmDialog(
                null, "Jouer contre le bot ?", "Mode",
                JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION;
        new FenetreDeJeu(modeBot);
    }
}
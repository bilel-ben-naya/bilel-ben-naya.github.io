package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuPrincipal extends JFrame {
    private JButton btnJoueurVsJoueur;
    private JButton btnJoueurVsIA;

    public MenuPrincipal() {
        // Initialisation de la fenêtre
        setTitle("Chess");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrer la fenêtre
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 220));

        // Titre
        JLabel titleLabel = new JLabel("CHESS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 50));
        titleLabel.setForeground(new Color(123, 69, 45));
        add(titleLabel, BorderLayout.NORTH);

        // Sous-titre
        JLabel subtitleLabel = new JLabel("Veuillez choisir un mode de jeu", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Serif", Font.PLAIN, 20));
        subtitleLabel.setForeground(new Color(123, 69, 45));
        add(subtitleLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 20, 20)); // Deux boutons avec de l'espace entre
        buttonPanel.setBackground(new Color(245, 245, 220));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Ajout de marges autour des boutons

        // Bouton Joueur vs Joueur
        btnJoueurVsJoueur = new JButton("Joueur vs Joueur");
        btnJoueurVsJoueur.setFont(new Font("Arial", Font.PLAIN, 24));
        btnJoueurVsJoueur.setBackground(new Color(123, 69, 45));
        btnJoueurVsJoueur.setForeground(Color.WHITE);
        btnJoueurVsJoueur.setPreferredSize(new Dimension(250, 60));

        // Bouton Joueur vs IA
        btnJoueurVsIA = new JButton("Joueur vs IA");
        btnJoueurVsIA.setFont(new Font("Arial", Font.PLAIN, 24));
        btnJoueurVsIA.setBackground(new Color(123, 69, 45));
        btnJoueurVsIA.setForeground(Color.WHITE);
        btnJoueurVsIA.setPreferredSize(new Dimension(250, 60));

        buttonPanel.add(btnJoueurVsJoueur);
        buttonPanel.add(btnJoueurVsIA);
        add(buttonPanel, BorderLayout.SOUTH);

        // Actions des boutons
        btnJoueurVsJoueur.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame("Joueur vs Joueur");
            }
        });

        btnJoueurVsIA.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame("Joueur vs IA");
            }
        });
    }

    private void startGame(String mode) {
        // Créer une nouvelle fenêtre de jeu
        System.out.println("Démarrage du jeu en mode : " + mode);

        // Créer une nouvelle fenêtre de jeu avec le mode choisi
        FenetreDeJeu gameWindow = new FenetreDeJeu(mode.equals("Joueur vs IA"));
        gameWindow.setVisible(true);

        // Fermer la fenêtre du menu principal
        dispose();
    }

    public static void main(String[] args) {
        MenuPrincipal menu = new MenuPrincipal();
        menu.setVisible(true); // Afficher la fenêtre
    }
}

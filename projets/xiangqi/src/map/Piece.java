package map;

import java.awt.Color;

public abstract class Piece {
    protected int x, y;
    protected Color couleur;

    // Constructeur de la classe Piece
    public Piece(int x, int y, Color couleur) {
        this.x = x;
        this.y = y;
        this.couleur = couleur;
    }

    // Getter pour les coordonnées et la couleur
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getCouleur() {
        return couleur;
    }

    // Setter pour changer la position de la pièce
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Méthode abstraite pour vérifier si un mouvement est valide
    public abstract boolean mouvementValide(int newX, int newY, Piece[][] plateau);
}

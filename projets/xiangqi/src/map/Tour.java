package map;

import java.awt.Color;

import process.Mouvement;

public class Tour extends Piece {

    public Tour(int x, int y, Color couleur) {
        super(x, y, couleur);
    }

    @Override
    public boolean mouvementValide(int newX, int newY, Piece[][] plateau) {
        // Déléguer la validation du mouvement à la classe Mouvement
        return Mouvement.validerMouvement(this, getX(), getY(), newX, newY, plateau);
    }
}

package gui;

import map.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AffichagePieces {
    private static final Map<Class<?>, String> NAMES = new HashMap<>();
    static {
        NAMES.put(Canon.class,    "canon");
        NAMES.put(Cheval.class,   "cheval");
        NAMES.put(Elephant.class, "elephant");
        NAMES.put(Garde.class,    "garde");
        NAMES.put(General.class,  "general");
        NAMES.put(Soldat.class,   "soldat");
        NAMES.put(Tour.class,     "tour");
    }

    private final Map<String, BufferedImage> images = new HashMap<>();

    public AffichagePieces() {
        for (Map.Entry<Class<?>,String> e : NAMES.entrySet()) {
            String base = e.getValue();
            load(base + "Noir");
            load(base + "Rouge");
        }
    }

    private void load(String fileName) {
        try {
            BufferedImage img = ImageIO.read(
                    getClass().getResourceAsStream("/img/" + fileName + ".png")
            );
            images.put(fileName, img);
        } catch (IOException|IllegalArgumentException ex) {
            System.err.println("Impossible de charger img/" + fileName + ".png");
        }
    }

    public void dessiner(Graphics g, Piece p, int size, int margin) {
        String base = NAMES.get(p.getClass());
        String color = p.getCouleur() == Color.RED ? "Rouge" : "Noir";
        BufferedImage img = images.get(base + color);
        if (img != null) {
            g.drawImage(img,
                    p.getX() * size + margin,
                    p.getY() * size + margin,
                    size, size,
                    null
            );
        } else {
            // fallback si jamais
            g.setColor(p.getCouleur());
            g.fillOval(
                    p.getX()*size + margin + size/8,
                    p.getY()*size + margin + size/8,
                    size*3/4, size*3/4
            );
        }
    }
}

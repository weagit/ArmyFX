import java.awt.Point;

public abstract class GameElement {

    private int posX; // Position X de l'élément sur la carte.
    private int posY; // Position Y de l'élément sur la carte.

    public final int OUTSIDE_MAPX = -2; // Constante pour une position X hors de la carte.
    public final int OUTSIDE_MAPY = -2; // Constante pour une position Y hors de la carte.

    public HELBArmyController controller; // initialisation de la variable controller.

    private final String[] IMAGE_PATHS; // Chemins des images associées à cet élément.

    // Constructeur pour initialiser la position et les images associées
    public GameElement(int posX, int posY, String[] imagePaths) {
        this.posX = posX; 
        this.posY = posY;
        this.IMAGE_PATHS = imagePaths; // Stocke les chemins d'images
    }

    // Retourne la position actuelle sous forme d'objet Point
    public Point getPosition() {
        return new Point(posX, posY);
    }

    // Retourne la position X actuelle
    public int getPosX() {
        return posX;
    }

    // Définit une nouvelle position X
    public void setPosX(int newPosX) {
        posX = newPosX;
    }

    // Retourne la position Y actuelle
    public int getPosY() {
        return posY;
    }

    // Définit une nouvelle position Y
    public void setPosY(int newPosY) {
        posY = newPosY;
    }

    // Retourne le premier chemin d'image (index 0 par défaut)
    public String getPathToImage() {
        return IMAGE_PATHS[0];
    }

    // Méthode générique qui retourne toujours null pour éviter des valeurs inattendues
    public GameElement isEmpty() {
        return null;
    }

    // Méthode abstraite que chaque sous-classe doit implémenter
    public abstract void triggerAction(HELBArmyController controller);
}

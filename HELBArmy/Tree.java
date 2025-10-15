public class Tree extends GameElement {

    public int woodAmount; // Quantité de bois de l'arbre.
    private boolean isVisible = true; // Indique si l'arbre est visible ou non.
    public static long lastCollectedTime = System.currentTimeMillis(); // Temps de la dernière collecte, -1 signifie aucune collecte.
    private int initialPosX; // Position X initiale de l'arbre.
    private int initialPosY; // Position Y initiale de l'arbre.
    private final int maxWoodAmount = 100;
    private long lastCollectedTimeReset = -1;
    private int minimalWood = 0;

    // Constructeur principal qui initialise l'arbre avec une quantité spécifique de bois.
    public Tree(int posX, int posY) {
        super(posX, posY, new String[]{"/img/ic_tree_full.png"});
        this.woodAmount = 100; // Quantité de bois de l'arbre.
        this.isVisible = true; // Par défaut, l'arbre est visible.
        this.initialPosX = posX; // Enregistre la position initiale X.
        this.initialPosY = posY; // Enregistre la position initiale Y.
    }

    // Méthode pour collecter du bois. Si tout le bois est collecté, l'arbre devient invisible.
    public void decreaseWood(int amount) {
        if (isVisible && woodAmount > minimalWood) {
            woodAmount -= amount; // Diminue la quantité de bois disponible.
            if (woodAmount <= minimalWood) {
                woodAmount = minimalWood; // Assure que le bois ne devienne pas négatif.
                isVisible = false; // L'arbre disparaît après avoir été abattu.
                setPosX(OUTSIDE_MAPX); // Déplace l'arbre hors de la carte.
                setPosY(OUTSIDE_MAPY);
                lastCollectedTime = System.currentTimeMillis(); // Enregistre le moment de l'abattage.
            }
        }
    }

    // Méthode pour régénérer l'arbre avec sa quantité de bois initiale.
    public void regenerate() {
        if (!isVisible) { 
            woodAmount = maxWoodAmount; // Remet la quantité de bois à 100.
            setPosX(initialPosX); // Replace l'arbre à sa position initiale X.
            setPosY(initialPosY); // Replace l'arbre à sa position initiale Y.
            isVisible = true; // Rend l'arbre visible de nouveau.
            lastCollectedTime = lastCollectedTimeReset; // Réinitialise le temps de collecte.
        }
    }

    // Retourne si l'arbre est actuellement visible.
    public boolean isVisible() {
        return isVisible;
    }

    // Définit si l'arbre doit être visible ou non.
    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }


    @Override
    public void triggerAction(HELBArmyController controller) {
        // Méthode actuellement vide, mais extensible pour des comportements futurs.
    }
}

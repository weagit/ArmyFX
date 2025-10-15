import java.util.Random;

public class PhilosophicalStone extends GameElement {

    private final int IMMORTAL_POWER = 999999999; // Puissance d'immortalité accordée par la pierre.
    private HELBArmyController controller; // Référence au contrôleur de l'armée.
    private int randomSize = 2;
    private int dieEffect = 0;

    // Constructeur qui initialise la position de la pierre et le contrôleur
    public PhilosophicalStone(int posX, int posY, HELBArmyController controller) {
        super(posX, posY, new String[]{"/img/PhilosophalStoneHELBARMY.drawio.png"});
        this.controller = controller; // Enregistre le contrôleur pour interagir avec le jeu.
    }

    // Méthode déclenchée lorsqu'une unité entre en contact avec la pierre
    public void interactWithUnit(Unit unit) {
        // La pierre disparaît de la carte
        setPosX(OUTSIDE_MAPX); 
        setPosY(OUTSIDE_MAPY);

        // Appliquer un effet aléatoire sur l'unité (mort ou invincibilité)
        applyRandomEffect(unit);

        // Signaler au contrôleur que cet élément doit être retiré
        controller.addElementCollectedToRemove(this);
    }

    // Applique un effet aléatoire à l'unité : soit elle meurt, soit elle devient invincible
    private void applyRandomEffect(Unit unit) {
        Random rand = new Random();
        int effect = rand.nextInt(randomSize); // Génère un nombre 0 ou 1 (50% de chances pour chaque effet)

        if (effect == dieEffect) {
            // Effet de mort : les points de vie de l'unité tombent à zéro
            unit.die(controller); 
            System.out.println(unit.getClass().getSimpleName() + " has died due to the Philosophical Stone!");
        } else {
            // Effet d'invincibilité : les points de vie de l'unité deviennent illimités
            unit.healthPoints = IMMORTAL_POWER; 
            System.out.println(unit.getClass().getSimpleName() + " has become invincible!");
        }
    }

    @Override
    public void triggerAction(HELBArmyController controller) {
        // Pas de comportement spécifique à définir pour l'instant
    }
}

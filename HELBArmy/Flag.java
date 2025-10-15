public class Flag extends GameElement {

    private boolean isCollected = false; // Indique si le drapeau a été collecté.
    public static long collectionTime = System.currentTimeMillis(); // Temps de collecte du drapeau, partagé par toutes les instances.
    private String team; // Équipe qui a collecté le drapeau (par exemple, Blanche ou Noire).
    private final int HEALTH_POWER = 2; // Facteur pour le calcul du bonus de vie.

    // Constructeur pour initialiser la position et le contrôleur
    public Flag(int posX, int posY, HELBArmyController controller) {
        super(posX, posY, new String[]{"/img/RedFlagHELBARMY.drawio.png"});
        this.controller = controller; // Enregistre le contrôleur pour les interactions.
    }

    // Marquer le drapeau comme collecté et appliquer les effets
    public void collect(Unit unit) {
        if (!isCollected) {
            this.isCollected = true; // Marque le drapeau comme collecté.
            Flag.collectionTime = System.currentTimeMillis(); // Met à jour le temps de collecte.
            this.team = unit.getCityType(); // Associe le drapeau à l'équipe de l'unité.
            
            // Déplace le drapeau hors de la carte pour simuler sa disparition.
            setPosX(OUTSIDE_MAPX); 
            setPosY(OUTSIDE_MAPY);

            // Applique un bonus de vie aux unités alliées.
            applyHealthBonus(unit.getCityType(), controller);

            // Affiche un message de confirmation dans la console.
            System.out.println("Le drapeau a été collecté par l'équipe " + this.team);
            
            // Signale au contrôleur de retirer cet élément.
            controller.addElementCollectedToRemove(this);
        }
    }

    // Vérifie si le drapeau a été collecté.
    public boolean isCollected() {
        return this.isCollected;
    }

    // Retourne l'équipe qui a collecté le drapeau.
    public String getTeam() {
        return team;
    }

    // Applique un bonus de vie aux unités alliées de la même équipe.
    private void applyHealthBonus(String cityType, HELBArmyController controller) {
        // Parcourt tous les éléments pour trouver les unités alliées.
        for (GameElement element : controller.unitList) {
            if (element instanceof Unit) {
                Unit ally = (Unit) element;

                // Vérifie si l'unité est de la même équipe.
                if (ally.getCityType().equals(cityType)) {
                    // Calcule le bonus de vie en fonction des points de vie actuels.
                    int bonusHealth = ally.healthPoints / HEALTH_POWER;
                    ally.healthPoints += bonusHealth; // Ajoute le bonus de vie.

                    // Affiche un message pour indiquer l'application du bonus.
                    System.out.println("Bonus de " + bonusHealth + " points de vie appliqué à l'unité " + ally.getClass().getSimpleName() + " de l'équipe " + cityType);
                }
            }
        }
    }

    @Override
    public void triggerAction(HELBArmyController controller) {
        // Aucune action spécifique nécessaire pour le moment.
    }
}

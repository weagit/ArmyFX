import java.awt.Point;

public class Pikeman extends Unit {

    // Constantes et variables d'instance
    private static final int VISION_RADIUS_PER_PIKEMAN = 1; // Rayon de vision par piquier
    
    private Point assignedPosition; // Position assignée pour les actions du piquier
    
    // Enumération représentant les différents états du piquier
    private enum State {
        MOVING_TO_POSITION, // Se déplace vers la position assignée
        ENGAGING_ENEMY,    // Engage l'ennemi
        RETURNING_TO_POSITION // Retourne à la position assignée
    }

    private State currentState; // État actuel du piquier

    // Constructeur de la classe Pikeman
    public Pikeman(int posX, int posY, String[] imagePaths, City city, HELBArmyController controller) {
        super(posX, posY, imagePaths, city); // Appel au constructeur de la classe parente (Unit)
        this.controller = controller; // Initialisation du contrôleur

        // Initialisation de l'état du piquier et de sa position assignée
        this.currentState = State.MOVING_TO_POSITION; 
        this.assignedPosition = controller.generateRandomPosition(); 

        // Valeurs spécifiques du piquier
        this.attackValue = 15;
        this.healthPoints = 175;

        // Bonus contre les Cavaliers
        bonusMap.put("Cavalry", 3.0); 
    }

    // Méthode pour obtenir les chemins d'images en fonction du type de ville
    public static String[] getImagePaths(String cityType) {
        if (cityType.equals(City.cityTypeWhite)) {
            return new String[]{"/img/WhitePikemanHELBARMY.drawio.png"};
        } else {
            return new String[]{"/img/BlackPikemanHELBARMY.drawio.png"};
        }
    }

    // Méthode déclenchée pour effectuer les actions du piquier
    @Override
    public void triggerAction(HELBArmyController controller) {

        // Si l'action est désactivée, aucune action n'est effectuée
        if (!actionEnabled) {
            return; // Arrêt du traitement si l'action est désactivée
        }

        // Vérification de la priorité d'un drapeau
        if (handleFlagPriority(controller)) {
            return; // Si un drapeau est prioritaire, on ne fait rien d'autre
        }

        // Vérification si une pierre philosophale a été touchée
        if (checkForPhilosophicalStone(controller)) {
            return; // Si une pierre a été touchée, on arrête l'exécution
        }

        // Calcul du rayon de vision en fonction des piquiers alliés
        int visionRadius = calculateCollectiveVision(controller); 
        
        // Switch pour gérer les différents états du piquier
        switch (currentState) {
            case MOVING_TO_POSITION:
                moveToAssignedPositionPikeman(controller); // Déplacer vers la position assignée
                break;
            case ENGAGING_ENEMY:
                engageEnemyPikeman(controller, visionRadius); // Engager l'ennemi si dans le rayon de vision
                break;
            case RETURNING_TO_POSITION:
                returnToAssignedPosition(controller); // Retourner à la position assignée
                break;
        }

        // Vérification des ennemis adjacents à combattre
        if (!handleFlagPriority(controller)) {
            checkAndFightAdjacentEnemies(controller); // Combattre les ennemis adjacents
        }
    }

    // Déplacement vers la position assignée
    private void moveToAssignedPositionPikeman(HELBArmyController controller) {
        if (isAtAssignedPosition()) {
            // Si déjà à la position assignée, on change d'état
            currentState= State.ENGAGING_ENEMY;
        } else {
            moveTowards(assignedPosition, controller); // Se déplacer vers la position assignée
        }
    }

    // Engager l'ennemi si dans le rayon de vision
    private void engageEnemyPikeman(HELBArmyController controller, int visionRadius) {
        Unit targetEnemy = findClosestEnemyUnit(getEnemyUnits(controller), controller); // Trouver l'ennemi le plus proche

        if (targetEnemy == isEmpty()) {
            // Si aucun ennemi n'est trouvé, revenir à la position assignée
            currentState = State.RETURNING_TO_POSITION;
        } else if (!isWithinVision(targetEnemy, visionRadius, controller)) {
            // Si l'ennemi est hors de portée de vision, revenir à la position assignée
            currentState = State.RETURNING_TO_POSITION;
        } else if (isAdjacentTo(targetEnemy)) {
            attack(targetEnemy, controller); // Attaquer l'ennemi s'il est adjacent
        } else {
            moveTowards(targetEnemy, controller); // Se déplacer vers l'ennemi s'il n'est pas adjacent
        }
    }

    // Retourner à la position assignée après l'action
    private void returnToAssignedPosition(HELBArmyController controller) {
        if (!isAtAssignedPosition()) {
            moveTowards(assignedPosition, controller); // Déplacer vers la position assignée
        } else {
            currentState = State.MOVING_TO_POSITION; // Revenir à l'état de déplacement
        }
    }

    // Vérifie si le piquier est déjà à sa position assignée
    private boolean isAtAssignedPosition() {
        return getPosX() == assignedPosition.x && getPosY() == assignedPosition.y;
    }


    // Vérifie si un ennemi est dans le rayon de vision spécifique
    private boolean isWithinVision(Unit enemy, int visionRadius, HELBArmyController controller) {
        if (enemy == isEmpty()) {
            return false; // Si l'ennemi est nul, retour false
        }

        double distance = controller.calculateDistance(this, enemy);
        return distance <= visionRadius; // Renvoie true si l'ennemi est dans le rayon de vision
    }

    // Calcule le rayon de vision en fonction du nombre de piquiers alliés
    private int calculateCollectiveVision(HELBArmyController controller) {
        int countPikemen = 0;

        // On parcourt toutes les unités alliées pour compter les piquiers
        for (GameElement element : getAlliedUnits(controller)) {
            if (element instanceof Unit) {
                Unit unit = (Unit) element; // Cast de GameElement en Unit
                if (unit instanceof Pikeman) {
                    countPikemen++; // On compte seulement les piquiers
                }
            }
        }

        // Retourne le rayon de vision total basé sur le nombre de piquiers
        return countPikemen * VISION_RADIUS_PER_PIKEMAN;
    }
}

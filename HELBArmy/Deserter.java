import java.util.List;

public class Deserter extends Unit {

    // Enumération représentant les états du Deserter (Déserteur)
    public enum DeserterState {
        ATTACKING,  // Mode Attaque - Attaque un collecteur ennemi
        ESCAPING    // Mode Fuite - Fuit les unités ennemies non collecteurs
    }

    private DeserterState state;  // État actuel du déserteur
    private int enemyEnoughClose = 2;  // Distance maximale pour considérer un ennemi comme "proche"
    private int enemyEnoughFar = 6;   // Distance minimale pour que le déserteur se sente en sécurité

    // Constructeur de la classe Deserter
    public Deserter(int posX, int posY, String[] imagePaths, City city) {
        super(posX, posY, imagePaths, city); // Appel au constructeur de la classe parente (Unit)
        this.state = DeserterState.ATTACKING;  // Par défaut, commence en mode ATTACKING

        // Spécification des valeurs d'attaque et de points de vie du Deserter
        this.attackValue = 10;
        this.healthPoints = 125;
        
        // Définir les bonus contre les piquiers et contre d'autres déserteurs
        bonusMap.put("Pikeman", 1.5);   // Bonus contre les piquiers
        bonusMap.put("Deserter", 1.25); // Bonus contre les autres déserteurs
    }
   
    // Méthode pour obtenir les chemins d'images pour un Deserter en fonction du type de ville
    public static String[] getImagePaths(String cityType) {
        if (cityType.equals(City.cityTypeWhite)) {
            return new String[]{"/img/WhiteDeserterHELBARMY.drawio.png"}; // Image pour une ville blanche
        } else {
            return new String[]{"/img/BlackDeserterHELBARMY.drawio.png"};  // Image pour une ville noire
        }
    }

    // Méthode principale déclenchée pour exécuter les actions du Deserter
    @Override
    public void triggerAction(HELBArmyController controller) {
    
        // Si l'action est désactivée, on ne fait rien
        if (!actionEnabled) {
            return;  // Arrêt du traitement si l'action est désactivée
        }

        // Vérifie la priorité liée au flag
        if (handleFlagPriority(controller)) {
            return; // Si un drapeau est prioritaire, on ne fait rien d'autre
        }
        
        // Vérifier si l'unité touche une pierre philosophale
        if (checkForPhilosophicalStone(controller)) {
            return;  // Si une pierre philosophale est touchée, on ne fait rien d'autre
        }

        // Logique spécifique au Deserter
        if (state == DeserterState.ATTACKING) {
            attackModeDeserter(controller);  // Exécution de l'attaque
        } else if (state == DeserterState.ESCAPING) {
            escapeEnemies(controller);  // Exécution de la fuite
        }
    
        // Vérifier si le Deserter doit se battre contre des ennemis adjacents
        if (!handleFlagPriority(controller)) {
            checkAndFightAdjacentEnemies(controller);
        }
    }

    // Mode Attaque pour le Deserter : attaque les collecteurs ennemis
    private void attackModeDeserter(HELBArmyController controller) {
        List<GameElement> enemyUnits = getEnemyUnits(controller); // Obtenir les unités ennemies
        Unit closestEnemyUnit = findClosestEnemyUnit(enemyUnits, controller); // Trouver l'unité ennemie la plus proche
        Collector closestCollector = (Collector) isEmpty(); // Initialiser un collecteur vide

        // Séparer les collecteurs des autres unités ennemies
        for (GameElement element : enemyUnits) {
            if (element instanceof Collector) {
                Collector collector = (Collector) element;
                // Si aucun collecteur n'a été trouvé ou si le collecteur trouvé est plus proche, le mettre à jour
                if (closestCollector == isEmpty() || controller.calculateDistance(this, collector) < controller.calculateDistance(this, closestCollector)) {
                    closestCollector = collector;
                }
            }  
        }

        // Si un collecteur est trouvé et est adjacent, attaque directe
        if (closestCollector != isEmpty()) {
            if (isAdjacentTo(closestCollector)) {
                attack(closestCollector, controller); // Attaque directe si adjacent
            } else {
                moveTowards(closestCollector, controller); // Sinon, se déplacer vers le collecteur
            }
        }

        // Si un ennemi proche est trouvé et n'est pas un collecteur, passer en mode ESCAPING
        if (closestEnemyUnit != isEmpty() && !(closestEnemyUnit instanceof Collector) && controller.calculateDistance(this, closestEnemyUnit) <= enemyEnoughClose) {
            state = DeserterState.ESCAPING; // Passer en mode ESCAPING si un ennemi non collecteur est trop proche
        }
    }

    // Mode Fuite : Fuit les ennemis non collecteurs
    private void escapeEnemies(HELBArmyController controller) {
        List<GameElement> enemyUnits = getEnemyUnits(controller); // Obtenir les unités ennemies
        Unit closestEnemy = findClosestEnemyUnit(enemyUnits, controller); // Trouver l'unité ennemie la plus proche

        // Vérifier s'il y a un ennemi à proximité
        if (closestEnemy != isEmpty() && controller.calculateDistance(this, closestEnemy) <= enemyEnoughClose) {
            // Calculer la direction opposée à l'ennemi le plus proche
            int escapeX = getPosX() + Integer.signum(getPosX() - closestEnemy.getPosX());
            int escapeY = getPosY() + Integer.signum(getPosY() - closestEnemy.getPosY());

            // Essayer de fuir dans une direction valide
            boolean moved = false;
            if (controller.isMoveValid(escapeX, getPosY())) {
                setPosX(escapeX); // Se déplacer horizontalement
                moved = true;
            } else if (controller.isMoveValid(getPosX(), escapeY)) {
                setPosY(escapeY); // Se déplacer verticalement
                moved = true;
            }

            // Si aucune direction valide, rester sur place (bloqué)
            if (!moved) {
                return;
            }

            // Si l'ennemi est maintenant suffisamment éloigné, repasser en mode ATTACKING
            if (controller.calculateDistance(this, closestEnemy) > enemyEnoughFar) {
                state = DeserterState.ATTACKING; // Revenir en mode attaque si assez éloigné
            }
        } else {
            // Si aucun ennemi à proximité, repasser en mode ATTACKING
            state = DeserterState.ATTACKING; 
        }
    }
}

public class Cavalry extends Unit {

    public enum CavalryState {
        PURSUING,   // Le cavalier poursuit un déserteur
        REPOSITIONING // Le cavalier se réajuste par rapport à un allié
    }

    private CavalryState state; // État actuel du cavalier
    private static int safetyDistance = 1; // Distance de sécurité entre cavaliers
    private int safetyDistanceReset = 1;
    private static boolean combatOccurred = false; // Indicateur si un combat a eu lieu

    // Constructeur
    public Cavalry(int posX, int posY, String[] imagePaths, City city) {
        super(posX, posY, imagePaths, city);
        this.state = CavalryState.PURSUING;
        this.attackValue = 10;
        this.healthPoints = 200;
        bonusMap.put("Deserter", 2.0);
    }

    // Retourne les chemins des images en fonction du type de ville
    public static String[] getImagePaths(String cityType) {
        if (cityType.equals(City.cityTypeWhite)) {
            return new String[]{"/img/WhiteCavalryHELBARMY.drawio.png"};
        } else {
            return new String[]{"/img/BlackCavalryHELBARMY.drawio.png"};
        }
    }

    // Action déclenchée par le cavalier à chaque tour
    @Override
    public void triggerAction(HELBArmyController controller) {
        if (!actionEnabled) return; // Si l'action est désactivée, ne rien faire

        // Priorités (drapeaux, pierres)
        if (handleFlagPriority(controller)) return;
        if (checkForPhilosophicalStone(controller)) return;

        // Gérer l'état PURSUING ou REPOSITIONING
        if (state == CavalryState.PURSUING) {
            pursueAndAttack(controller); // Priorité à la poursuite et l'attaque
        } else if (state == CavalryState.REPOSITIONING) {
            reposition(controller); // Ajustement par rapport aux alliés
        }

        // Vérifier et combattre les ennemis adjacents
        checkAndFightAdjacentEnemies(controller);
    }

    // Poursuit les déserteurs et attaque si possible
    private void pursueAndAttack(HELBArmyController controller) {
        // Trouver l'ennemi et l'allié les plus proches
        GameElement closestEnemy = findClosestEnemyUnit(getEnemyUnits(controller), controller);
        Unit closestAlly = findClosestAlly(getAlliedUnits(controller), controller);

        // Si un allié est trop proche, passer en mode repositionnement
        if (closestAlly instanceof Cavalry) {
            double allyDistance = controller.calculateDistance(this, closestAlly);
            if (allyDistance < safetyDistance) {
                state = CavalryState.REPOSITIONING;
                return;  // Le cavalier se repositionne au lieu de poursuivre
            }
        }

        // Gestion des ennemis (déserteurs)
        if (closestEnemy instanceof Deserter) {
            Deserter deserter = (Deserter) closestEnemy;
            if (isAdjacentTo(deserter)) {
                attack(deserter, controller);                    // Attaque immédiate si adjacent
                combatOccurred = true;                           // Marquer qu'un combat a eu lieu
                safetyDistance = safetyDistanceReset;            // Réinitialiser la distance de sécurité après combat
            } else {
                moveTowards(deserter, controller);               // Poursuit le déserteur s'il n'est pas adjacent
            }
        }
    }

    // Réajuste la position par rapport aux alliés
    private void reposition(HELBArmyController controller) {
        // Parcours des alliés pour trouver le plus proche
        Unit closestAlly = findClosestAlly(getAlliedUnits(controller), controller);

        if (closestAlly instanceof Cavalry) {
            double distanceToAlly = controller.calculateDistance(this, closestAlly);

            // Si l'allié est trop proche, éloigner le cavalier
            if (distanceToAlly < safetyDistance) {
                moveAwayFromAlly((Cavalry) closestAlly, controller);
            } 
            // Si l'allié est trop loin, rapprocher le cavalier
            else if (distanceToAlly > safetyDistance) {
                moveTowards(closestAlly, controller);
            }
        } else {
            // Si aucun allié trouvé, passer en mode poursuite
            state = CavalryState.PURSUING;
        }
    }

    // S'éloigne d'un allié pour maintenir la distance de sécurité
    private void moveAwayFromAlly(Cavalry ally, HELBArmyController controller) {
        int moveX = getPosX() + Integer.signum(getPosX() - ally.getPosX());
        int moveY = getPosY() + Integer.signum(getPosY() - ally.getPosY());

        if (controller.isMoveValid(moveX, getPosY())) {
            setPosX(moveX);
        } else if (controller.isMoveValid(getPosX(), moveY)) {
            setPosY(moveY);
        }
    }

    // Ajuste la distance de sécurité après un tour
    public static void adjustSafetyDistanceAfterTurn() {
        if (!combatOccurred) {
            safetyDistance++;  // Augmenter la distance de sécurité si aucun combat n'a eu lieu
        }
        combatOccurred = false;  // Réinitialiser après l'ajustement
    }

}

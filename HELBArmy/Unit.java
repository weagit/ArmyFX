import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Unit extends GameElement {
    public String[] imagePaths;
    private City assignedCity;
    public  int healthPoints;
    public int attackValue;
    public boolean actionEnabled = true;  // Par défaut, l'action  des untiés est activée  
    public int dieUnitValue = 0;
    private int notGoodStep = 0;
    public int distanceToBeAdjacent = 1;
    // Map pour les bonus spécifiques contre certaines unités
    public Map<String, Double> bonusMap = new HashMap<>();
    
    // Constructeur pour initialiser l'unité avec sa position, son image et sa ville assignée
    public Unit(int posX, int posY, String[] imagePaths, City city) {
        super(posX, posY, imagePaths);
        this.imagePaths = imagePaths;
        this.assignedCity = city;
    }

    // Méthode pour activer ou désactiver l'action de l'unité
    public void setActionEnabled(boolean enabled) {
        this.actionEnabled = enabled;
    }


    public void attack(Unit target, HELBArmyController controller) {
        if (!isEnemyUnit(target)) {
            System.out.println("La cible n'est pas une unité ennemie.");
            return;
        }
        
        // Récupérer le bonus spécifique à l'attaque
        double bonusMultiplier = getBonusMultiplier(target);
        
        // Calculer les dégâts ajustés
        int adjustedDamage = (int) (attackValue * bonusMultiplier);
        
        // Appliquer les dégâts à la cible
        target.takeDamage(adjustedDamage, controller);
        
    }

    // Méthode pour récupérer le bonus spécifique
    private double getBonusMultiplier(Unit target) {
        return bonusMap.getOrDefault(target.getClass().getSimpleName(), 1.0);
    }

    // Méthode pour recevoir des dégâts
    private void takeDamage(int damage, HELBArmyController controller) {

        // Réduit la santé de l'unité en fonction des dégâts reçus
        this.healthPoints -= damage;

        // Si la santé tombe à zéro ou en dessous, l'unité meurt
        if (this.healthPoints <= dieUnitValue) {
            die(controller);
        }
    }
    
    // Méthode pour faire mourir l'unité
    public void die(HELBArmyController controller) {

        this.healthPoints = dieUnitValue;

        this.setPosX(OUTSIDE_MAPX);
        this.setPosY(OUTSIDE_MAPY);

        controller.addToDeadUnits(this);

        System.out.println(this.getClass().getSimpleName() + " has died!");
    }

    // Lorsqu'une unité d'armée rivale se trouve sur une position adjacente à une autre,un combat s'engage entre ces unités.
    public void checkAndFightAdjacentEnemies(HELBArmyController controller) {
        // Vérifier si l'unité a des ennemis adjacents
        List<Unit> adjacentEnemies = getAdjacentEnemies(controller);
    
        // Si l'unité a des ennemis adjacents, lancer le combat
        if (!adjacentEnemies.isEmpty()) {
            // Choisir un ennemi au hasard parmi ceux qui sont adjacents
            Unit enemyToFight = adjacentEnemies.get(new Random().nextInt(adjacentEnemies.size()));
    
            // Engager le combat avec l'ennemi choisi
            this.attack(enemyToFight, controller);
        }
    }
    
    // Méthode pour récupérer les ennemis adjacents
    private List<Unit> getAdjacentEnemies(HELBArmyController controller) {
        List<Unit> adjacentEnemies = new ArrayList<>();
    
        // Parcourir toutes les unités dans le jeu et vérifier si elles sont adjacentes et ennemies
        for (GameElement element : controller.gameElementList) {
            if (element instanceof Unit) {
                Unit unit = (Unit) element;
    
                // Vérifier si l'unité est ennemie et adjacente
                if (this.isEnemyUnit(unit) && isAdjacentTo(unit)) {
                    adjacentEnemies.add(unit);
                }
            }
        }
    
        return adjacentEnemies;
    }

    
    // Méthode gérant le comportement lié au flag
    public boolean handleFlagPriority(HELBArmyController controller) {
        boolean flagHandled = false; // Indicateur pour suivre si un drapeau est traité

        // Parcourt les éléments du jeu pour trouver un drapeau non collecté
        for (GameElement element : controller.gameElementList) {
            if (element instanceof Flag && !((Flag) element).isCollected()) {
                Flag flag = (Flag) element;
                Point flagPosition = flag.getPosition();

                // Déplacement vers le drapeau
                moveTowards(flagPosition, controller);

                // Vérifie si l'unité atteint la position du drapeau
                if (getPosition().equals(flagPosition)) {
                    // Collecte du drapeau et enregistrement de l'équipe de l'unité
                    flag.collect(this); // Passer 'this' pour attribuer l'équipe de l'unité
                    System.out.println(this.getClass().getSimpleName() + " a collecté un drapeau pour l'équipe " + flag.getTeam());
                }

                flagHandled = true; // Indique qu'un drapeau a été trouvé et traité
                break; // Arrête la recherche après avoir traité un drapeau
            }
        }

        return flagHandled; // Retourne si un drapeau a été traité
    }

    // Méthode gérant le comportement lié aux pierres philosophales
    public boolean checkForPhilosophicalStone(HELBArmyController controller) {
        // Vérifier si l'unité entre en contact avec une pierre philosophale
        for (GameElement element : controller.gameElementList) {
            if (element instanceof PhilosophicalStone) {
                PhilosophicalStone stone = (PhilosophicalStone) element;
                Point stonePosition = stone.getPosition();
    
                // Vérifier si l'unité est à la position de la pierre
                if (getPosition().equals(stonePosition)) {
                    stone.interactWithUnit(this);  // Interagir avec la pierre
                    return true;  // Indiquer que la pierre a été touchée
                }
            }
        }
        return false;  // Pas de contact avec la pierre
    }
    

    public City getAssignedCity() {
        return assignedCity;
    }

    public String getCityType() {
        return assignedCity.getCityType();  // Retourne le type de la ville
    }
    
    // Vérifie si l'unité cible appartient à une ville ennemie
    private boolean isEnemyUnit(Unit target) {
        return !this.getCityType().equals(target.getCityType());  // Si les villes sont différentes, l'unité est ennemie
    }

    // Liste qui va etre appelé a chaque fois qu'on aura besoin de travailler avec les untiés ennemies par rapport au pov utilisé.
    public List<GameElement> getEnemyUnits(HELBArmyController controller) {
        List<GameElement> enemyUnits = new ArrayList<>();
    
        // Parcours de la liste des éléments du jeu pour trouver les unités ennemies
        for (GameElement element : controller.unitList) {
            // Vérifie que l'élément est une instance de Unit et qu'il s'agit d'un ennemi
            if (element instanceof Unit && isEnemyUnit((Unit) element)) {
                enemyUnits.add(element);  // Ajoute l'unité ennemie à la liste
            }
        }
        return enemyUnits;
    }


    // Méthode utilitaire : obtenir les unités alliées
    public List<GameElement> getAlliedUnits(HELBArmyController controller) {
        List<GameElement> alliedUnits = new ArrayList<>();

        for (GameElement element : controller.unitList) {
            // Vérifie que l'élément est une unité et n'est pas l'ennemi ou lui-même
            if (element instanceof Unit && !isEnemyUnit((Unit) element) && element != this) {
                alliedUnits.add(element); // Ajouter l'allié, mais pas lui-même
            }
        }

        return alliedUnits;
    }

    // Recherche l'élément le plus proche dans gameElementList
    public GameElement findClosestElement(List<GameElement> gameElementList, HELBArmyController controller) {

        if (gameElementList.isEmpty()) {
            return isEmpty();  // Si la liste est vide, on retourne null
        }

        // Initialisation avec le premier élément de la liste
        GameElement closest = gameElementList.get(0);
        double minDistance = controller.calculateDistance(this, closest);

        // Parcours de tous les éléments de gameElementList
        for (GameElement element : gameElementList) {

            // Vérification de la validité de l'élément
            if (!isTargetValid(element, controller)) {
                continue;  // Passer à l'élément suivant si invalide
            }

            // Calcul de la distance
            double distance = controller.calculateDistance(this, element);

            // Mise à jour si un élément plus proche est trouvé
            if (distance < minDistance) {
                minDistance = distance;
                closest = element;
            }
        }

        // Retourne l'élément le plus proche trouvé
        return closest;
    }

    // Recherche l'unité ennemie la plus proche
    public Unit findClosestEnemyUnit(List<GameElement> enemyUnits, HELBArmyController controller) {
        return (Unit) findClosestElement(enemyUnits, controller);  // Cherche l'unité ennemie la plus proche
    }

    public Unit findClosestAlly(List<GameElement> alliedUnits, HELBArmyController controller) {
        return (Unit) findClosestElement(alliedUnits, controller);  // Cherche l'unité allié la plus proche
    }

    // Déplace l'unité vers une cible donnée si elle est valide (GameElement)
    public void moveTowards(GameElement target, HELBArmyController controller) {
        if (!isTargetValid(target, controller)) {
            return; // Pas de cible ou cible invalide
        }

        moveToPosition(target.getPosX(), target.getPosY(), controller);
    }

    // Déplace l'unité vers un point donné si elle est valide (Point)
    public void moveTowards(Point target, HELBArmyController controller) {
        moveToPosition((int) target.getX(), (int) target.getY(), controller);
    }

    // Déplace l'unité vers une position donnée (réutilisée par les deux méthodes ci-dessus)
    private void moveToPosition(int targetX, int targetY, HELBArmyController controller) {
        int currentX = getPosX();
        int currentY = getPosY();
        int deltaX = targetX - currentX;
        int deltaY = targetY - currentY;

        // Calcul des directions
        int stepX = Integer.signum(deltaX);
        int stepY = Integer.signum(deltaY);

        // Tentative de déplacement en diagonale en priorité (si valide)
        if (stepX != notGoodStep && stepY != notGoodStep && controller.isMoveValid(currentX + stepX, currentY + stepY)) {
            setPosX(currentX + stepX);
            setPosY(currentY + stepY);
        }
        // Tentative de déplacement horizontal
        else if (stepX != notGoodStep && controller.isMoveValid(currentX + stepX, currentY)) {
            setPosX(currentX + stepX);
        }
        // Tentative de déplacement vertical
        else if (stepY != notGoodStep && controller.isMoveValid(currentX, currentY + stepY)) {
            setPosY(currentY + stepY);
        } 
        // Recherche d'une case adjacente libre si toutes les directions principales sont bloquées
        else {
            tryAdjacentPositions(controller);
        }
    }

    // Recherche d'une case adjacente libre si bloqué
    private void tryAdjacentPositions(HELBArmyController controller) {
        int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}, // Déplacements horizontaux et verticaux
            {-1, -1}, {1, 1}, {1, -1}, {-1, 1} // Déplacements diagonaux
        };

        for (int[] dir : directions) {
            int newX = getPosX() + dir[0];
            int newY = getPosY() + dir[1];
            if (controller.isMoveValid(newX, newY)) {
                setPosX(newX);
                setPosY(newY);
                break; // Sortir dès qu'un mouvement valide est trouvé
            }
        }
    }

    // Vérifie si la position cible est valide (GameElement)
    public boolean isTargetValid(GameElement target, HELBArmyController controller) {
        return isPositionValid(target.getPosX(), target.getPosY(), controller);
    }

    // Logique centrale de validation d'une position
    public static boolean isPositionValid(int x, int y, HELBArmyController controller) {
        int mapWidth = HELBArmyController.ROWS;  // Largeur de la carte
        int mapHeight = HELBArmyController.COLS; // Hauteur de la carte
        return x >= 0 && x < mapWidth && y >= 0 && y < mapHeight;
    }

    // Vérifie si l'élément cible est adjacent à cet élément
    public boolean isAdjacentTo(GameElement target) {
        return isAdjacentTo(new Point(target.getPosX(), target.getPosY()));
    }

    // Vérifie si l'élément cible est adjacent à cet élément (utilisable pour des éléments statiques)
    public boolean isAdjacentTo(Point target) {
        return Math.abs(this.getPosX() - target.getX()) <= distanceToBeAdjacent && Math.abs(this.getPosY() - target.getY()) <= distanceToBeAdjacent;
    }
}

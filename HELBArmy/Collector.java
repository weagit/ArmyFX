import java.awt.Point;

public class Collector extends Unit {

    // Enumération des états possibles du collecteur
    public enum CollectorState {
        COLLECTING,             // Collecte de bois
        RETURNING_TO_CITY,      // Retourne à la ville
        DEPOSITING              // Dépose le bois collecté dans la ville
    }

    private int collectedWood;                                  // Quantité de bois collectée par le collecteur
    private int amountToCollect = 5;                            // Quantité de bois collectée par action
    private int maxWoodCapacity;                                // Capacité maximale de bois que le collecteur peut porter
    private CollectorState state;                               // État actuel du collecteur
    public City city = getAssignedCity();                       // Récupère la ville assignée au collecteur

    // Constructeur du collecteur, initialisant ses paramètres
    public Collector(int posX, int posY, String[] imagePaths, City city) {
        super(posX, posY, imagePaths, city);
        this.collectedWood = 0;
        this.maxWoodCapacity = 25;  // Capacité maximale de bois par défaut
        this.state = CollectorState.COLLECTING;  // Par défaut, le collecteur est en mode collecte
        
        this.attackValue = 5;
        this.healthPoints = 150;
    }

    // Méthode pour obtenir les chemins d'images pour un Collector
    public static String[] getImagePaths(String cityType) {
        if (cityType.equals(City.cityTypeWhite)) {
            return new String[]{"/img/WhiteCollectorHELBARMY.drawio.png"};
        } else {
            return new String[]{"/img/BlackCollectorHELBARMY.drawio.png"};
        }
    }

    // Gestion des actions du collecteur en fonction de son état
    @Override
    public void triggerAction(HELBArmyController controller) {
        
        // Si l'action est désactivée, on ne fait rien
        if (!actionEnabled) {
            return;  // On arrête le traitement ici si l'action est désactivée
        }

        // Vérifie la priorité liée au flag
        if (handleFlagPriority(controller)) {
            // Si un drapeau est prioritaire, ne pas exécuter d'autres actions
            return;
        }

        // Vérifier si l'unité touche une pierre philosophale
        if (checkForPhilosophicalStone(controller)) {
            return;  // Si une pierre a été touchée, ne pas exécuter d'autres actions
        }
        
        switch (state) {
            case COLLECTING:
                collectAction(controller);  // Collecte du bois
                break;
            case RETURNING_TO_CITY:
                returnToCityAction(controller);  // Retour à la ville
                break;
            case DEPOSITING:
                depositAction(controller);  // Dépôt du bois à la ville
                break;
        }

        // Vérifier si l'unité doit se battre contre des ennemis adjacents
        if (!handleFlagPriority(controller)) {
            checkAndFightAdjacentEnemies(controller);
        }
    }

    // Action de collecte de bois
    private void collectAction(HELBArmyController controller) {
        // Initialisation de closestTree à un objet vide par défaut
        Tree closestTree = (Tree) isEmpty();
        
        // Recherche du premier arbre valide à collecter dans la liste des éléments du jeu
        for (GameElement element : controller.gameElementList) {
            // Si l'élément est un arbre valide, on le considère comme l'arbre le plus proche
            if (element instanceof Tree && isTargetValid(element, controller)) {
                closestTree = (Tree) element;
                break;  // On s'arrête dès qu'on trouve un arbre valide
            }
        }
    
        // Si un arbre valide a été trouvé
        if (closestTree != isEmpty()) {
            // Recherche de l'arbre le plus proche parmi tous les arbres valides
            for (GameElement element : controller.gameElementList) {
                if (element instanceof Tree) {
                    Tree tree = (Tree) element;
    
                    // Si l'arbre est valide, on compare sa distance avec celle du plus proche trouvé jusqu'à présent
                    if (isTargetValid(tree, controller)) {
                        if (controller.calculateDistance(this, tree) < controller.calculateDistance(this, closestTree)) {
                            closestTree = tree;  // Mise à jour de l'arbre le plus proche si une distance plus courte est trouvée
                        }
                    }
                }
            }
    
            // Si l'unité est adjacente à l'arbre le plus proche
            if (isAdjacentTo(closestTree.getPosition())) {
                collectWood(closestTree);  // Collecte du bois de l'arbre
                if (collectedWood >= maxWoodCapacity) {
                    // Si le collecteur a atteint sa capacité maximale, il passe à l'état "Retour à la ville"
                    state = CollectorState.RETURNING_TO_CITY;
                }
            } else {
                // Si l'unité n'est pas adjacente à l'arbre, elle se déplace vers cet arbre
                moveTowards(closestTree.getPosition(), controller);
            }
        }
    }
    

    // Action pour retourner à la ville pour déposer le bois
    private void returnToCityAction(HELBArmyController controller) {
        Point depotPoint = city.getDepotPoint();  // Récupère le point de dépôt de la ville
        if (isAdjacentTo(depotPoint)) {
            state = CollectorState.DEPOSITING;  // Si le collecteur est adjacent au point de dépôt, il va déposer le bois
        } else {
            moveTowards(depotPoint, controller);  // Se déplacer vers la ville
        }
    }

    // Action de dépôt du bois dans la ville
    private void depositAction(HELBArmyController controller) {
        // Ajoute le bois collecté à la ville, en spécifiant le type de la ville
        city.addWood(collectedWood, city.getCityType());

        // Log pour vérifier le montant déposé
        System.out.println("Bois déposé à " + city.getCityType() + ": " + collectedWood);

        // Vider le bois collecté après dépôt
        collectedWood -= collectedWood;
        
        // Revenir à l'état de collecte
        state = CollectorState.COLLECTING;
    }

    // Collecter du bois à partir d'un arbre
    private void collectWood(Tree tree) {
        if (tree.woodAmount > 0) {
            collectedWood += amountToCollect;  // Ajouter du bois collecté
            tree.decreaseWood(amountToCollect);  // Réduire la quantité de bois dans l'arbre
        }
    }
}

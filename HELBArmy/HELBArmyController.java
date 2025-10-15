import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HELBArmyController {
    // Liste globale des éléments du jeu
    public final ArrayList<GameElement> gameElementList = new ArrayList<>();
    // Liste spécifiquement pour les unités
    public final ArrayList<Unit> unitList = new ArrayList<>();
    // Liste des drapeaux
    public final ArrayList<Flag> flagList = new ArrayList<>();
    // Liste temporaire pour les unités mortes et éléments à supprimer
    public List<Unit> deadUnitList = new ArrayList<>();
    public List<GameElement> elementsToRemoveList = new ArrayList<>();
    // Liste pour les arbres et les villes
    public final ArrayList<Tree> treeList = new ArrayList<>();
    public final ArrayList<City> cityList = new ArrayList<>();
    
    // Map associant le nom de la classe à son image pour éviter les duplications
    private final Map<String, Image> imageMap = new HashMap<>();
    
    // La vue du jeu
    private final HELBArmyView view;
    
    // Paramètres de la carte et du jeu
    public static final int ROWS = 20;
    public static final int COLS = ROWS;
    public static final int SQUARE_SIZE = 40;
    private final long timeTreeToRegenerate = 30000;  // Temps avant régénération des arbres
    private final double treeRatio = 0.04;            // Ratio d'arbres à générer
    public final long GENERATION_FLAG_DELAY = 120000; // Délai de génération des drapeaux (2 minutes)
    public final int NUMBER_OF_STONES = 2;

    private final int mathPow = 2;

    public enum UnitType { COLLECTOR, DESERTER, CAVALRY, PIKEMAN }

    // Constructeur pour initialiser le contrôleur
    public HELBArmyController(HELBArmyView view) {
        this.view = view;
        this.view.setController(this); // Lien entre la vue et le contrôleur
        setupKeyEventHandler(); // Configure l'écoute des événements clavier
    }

    // Démarre le jeu avec initialisation des éléments visuels et logiques
    public void startGame() {
        view.drawBackground(); // Dessine l'arrière-plan
        generateCities(); // Génère les villes
        generateInitialTrees(treeRatio); // Génère les arbres
        generatePhilosophicalStone(); // Crée les pierres philosophiques
        view.updateScene(gameElementList); // Met à jour la vue avec les éléments
    }

    // Mise à jour continue du jeu à chaque "tick"
    public void handleGameTick() {
        long currentTimeTick = System.currentTimeMillis();
        
        // Actions des unités (combat, déplacement, etc.)
        for (GameElement element : unitList) {
            element.triggerAction(this);
        }

        // Ajuste la distance de sécurité des cavaliers après que toutes les unités ont agi
        Cavalry.adjustSafetyDistanceAfterTurn();

        // Régénérer les arbres si nécessaire
        checkAndRegenerateTrees();

        // Vérification et génération du drapeau
        checkAndGenerateFlag();
        
        // Générer de nouvelles unités pour les villes
        for (City city : cityList) {
            city.generateUnits(currentTimeTick, unitList, gameElementList);
        }

        view.updateScene(gameElementList); // Met à jour la scène du jeu
        removeDeadUnits(); // Supprime les unités mortes
        removeCollectedElements(); // Supprime les éléments collectés
    }

    
    // Ajouter une unité morte à la liste
    public void addToDeadUnits(Unit unit) {
        deadUnitList.add(unit);
    }

    // Supprime les unités mortes de toutes les listes
    private void removeDeadUnits() {
        // Supprime les éléments de la liste des unités mortes
        gameElementList.removeAll(deadUnitList);
        unitList.removeAll(deadUnitList);
        deadUnitList.clear(); // Vide la liste temporaire
    }

    // Ajouter un élément à la liste des éléments à supprimer
    public void addElementCollectedToRemove(GameElement element) {
        elementsToRemoveList.add(element);  // Ajoute à la liste des éléments collectés
    }

    // Supprime les éléments collectés de la liste principale
    private void removeCollectedElements() {
        // Supprime les éléments collectés des listes correspondantes
        gameElementList.removeAll(elementsToRemoveList);
        flagList.removeAll(elementsToRemoveList);
        elementsToRemoveList.clear(); // Vide la liste des éléments collectés
    }


    // Configure un gestionnaire pour les événements clavier, permettant d'interagir avec le jeu via des raccourcis clavier.
    private void setupKeyEventHandler() {
        // Associe un écouteur d'événements à la scène principale du jeu.
        view.scene.setOnKeyPressed(event -> {
            // Récupère la touche qui a été pressée.
            KeyCode code = event.getCode();

            // Gestion des raccourcis clavier pour générer des unités.
            // Les touches A, Z, E, R génèrent des unités blanches de différents types.
            if (code == KeyCode.A) {
                generateUnitCheatCode("COLLECTOR", City.cityTypeWhite); // Génère un collecteur blanc.
            } else if (code == KeyCode.Z) {
                generateUnitCheatCode("DESERTER", City.cityTypeWhite); // Génère un déserteur blanc.
            } else if (code == KeyCode.E) {
                generateUnitCheatCode("CAVALRY", City.cityTypeWhite); // Génère une cavalerie blanche.
            } else if (code == KeyCode.R) {
                generateUnitCheatCode("PIKEMAN", City.cityTypeWhite); // Génère un piquier blanc.
            }
            // Les touches W, X, C, V génèrent des unités noires de différents types.
            else if (code == KeyCode.W) {
                generateUnitCheatCode("COLLECTOR", City.cityTypeBlack); // Génère un collecteur noir.
            } else if (code == KeyCode.X) {
                generateUnitCheatCode("DESERTER", City.cityTypeBlack); // Génère un déserteur noir.
            } else if (code == KeyCode.C) {
                generateUnitCheatCode("CAVALRY", City.cityTypeBlack); // Génère une cavalerie noire.
            } else if (code == KeyCode.V) {
                generateUnitCheatCode("PIKEMAN", City.cityTypeBlack); // Génère un piquier noir.
            }

            // Gestion des raccourcis clavier pour activer/désactiver les actions des unités spécifiques.
            else if (code == KeyCode.J) {
                toggleUnitActions(UnitType.COLLECTOR); // Active/désactive les collecteurs.
            } else if (code == KeyCode.K) {
                toggleUnitActions(UnitType.DESERTER); // Active/désactive les déserteurs.
            } else if (code == KeyCode.L) {
                toggleUnitActions(UnitType.CAVALRY); // Active/désactive les cavaliers.
            } else if (code == KeyCode.M) {
                toggleUnitActions(UnitType.PIKEMAN); // Active/désactive les piquiers.
            }

            // Gestion des raccourcis clavier pour des fonctionnalités spéciales du jeu.
            else if (code == KeyCode.U) {
                killAllUnitsCheatCode(); // Élimine toutes les unités du jeu.
            } else if (code == KeyCode.I) {
                generateFlag(); // Génère un nouveau drapeau sur la carte.
            } else if (code == KeyCode.O) {
                restartGame(); // Redémarre le jeu.
            }

            // Gestion de la création d'une pierre philosophale.
            else if (code == KeyCode.P) {
                Point position = generateRandomPosition(); // Génére une position aléatoire.
                PhilosophicalStone stone = new PhilosophicalStone(position.x, position.y, this); // Crée une nouvelle pierre philosophale.
                gameElementList.add(stone); // Ajoute la pierre à la liste des éléments du jeu.
            }
        });
    }


    // Méthode pour générer une unité dans la ville spécifiée
    private void generateUnitCheatCode(String unitType, String cityType) {
        City city = cityType.equals(City.cityTypeWhite) ? cityList.get(0) : cityList.get(1); // Choix de la ville
        Unit newUnit = city.createUnit(unitType); // Crée l'unité à partir de la ville
        gameElementList.add(newUnit);  
        unitList.add(newUnit); // Ajoute l'unité à la liste des unités
    }

    // Active ou désactive les actions des unités d'un type spécifique.
    // Cette méthode parcourt toutes les unités du jeu et bascule l'état d'activation de leurs actions si elles correspondent au type spécifié.
    private void toggleUnitActions(UnitType unitType) {
        // Parcourt la liste des unités du jeu.
        for (GameElement element : unitList) {
            // Vérifie si l'élément est une unité.
            if (element instanceof Unit) {
                Unit unit = (Unit) element; // Conversion de l'élément en unité.
                boolean isUnitType = false; // Indique si l'unité correspond au type spécifié.

                // Détermine si l'unité correspond au type donné.
                switch (unitType) {
                    case COLLECTOR:
                        isUnitType = unit instanceof Collector; // Vérifie si l'unité est un collecteur.
                        break;
                    case DESERTER:
                        isUnitType = unit instanceof Deserter; // Vérifie si l'unité est un déserteur.
                        break;
                    case CAVALRY:
                        isUnitType = unit instanceof Cavalry; // Vérifie si l'unité est une cavalerie.
                        break;
                    case PIKEMAN:
                        isUnitType = unit instanceof Pikeman; // Vérifie si l'unité est un piquier.
                        break;
                }

                // Si l'unité correspond au type spécifié, on bascule l'état d'activation de ses actions.
                if (isUnitType) {
                    unit.setActionEnabled(!unit.actionEnabled); // Active ou désactive l'action.
                }
            }
        }
    }

    // CheatCode pour tuer toutes les unités du jeu.
    // Cette méthode parcourt la liste des unités et force leur décès, 
    // quel que soit leur état actuel, via un appel à la méthode `die`.
    private void killAllUnitsCheatCode() {
        // Parcourt la liste des éléments du jeu.
        for (GameElement element : unitList) {
            // Vérifie si l'élément est une unité.
            if (element instanceof Unit) {
                Unit unit = (Unit) element; // Conversion de l'élément en unité.
                
                // Force l'unité à mourir en appelant la méthode dédiée.
                unit.die(this);
            }
        }
        // Affiche un message dans la console pour indiquer que toutes les unités ont été tuées.
        System.out.println("Toutes les unités ont été tuées.");
    }

    // Réinitialise le jeu en supprimant tous les éléments existants et en redémarrant une nouvelle partie.
    // Cette méthode est utilisée pour nettoyer l'état du jeu et recommencer depuis le début.
    private void restartGame() {
        // Vide la liste de tous les éléments du jeu (unités, villes, drapeaux, arbres, etc.).
        gameElementList.clear();  

        // Vide spécifiquement la liste des unités en jeu.
        unitList.clear();         

        // Vide la liste des villes associées au jeu.
        cityList.clear();

        // Vide la liste des arbres présents sur la carte.
        treeList.clear();

        // Vide la liste des drapeaux présents dans le jeu.
        flagList.clear();

        // Supprime les éléments marqués pour suppression.
        elementsToRemoveList.clear();         

        // Vide la liste des unités mortes pour réinitialiser cet état.
        deadUnitList.clear();     

        // Appelle la méthode `startGame` pour redémarrer une nouvelle partie avec un état initial.
        startGame();  
    }


    // Génère les deux villes de départ (blanche et noire) représentant les bases des factions.
    private void generateCities() {
        // Création de la ville blanche avec ses propriétés (position, image, type) et ajout aux listes.
        City whiteCity = new City(City.CITY_WHITE_POS_X, City.CITY_WHITE_POS_Y, City.imagePathCityWhite, City.cityTypeWhite, this);
        cityList.add(whiteCity);
        gameElementList.add(whiteCity);

        // Création de la ville noire avec ses propriétés (position, image, type) et ajout aux listes.
        City blackCity = new City(City.CITY_BLACK_POS_X, City.CITY_BLACK_POS_Y, City.imagePathCityBlack, City.cityTypeBlack, this);
        cityList.add(blackCity);
        gameElementList.add(blackCity);
    }


    // Génère des arbres initiaux sur la carte en fonction d'un ratio spécifié.
    private void generateInitialTrees(double treeRatio) {
        for (int x = 0; x < ROWS; x++) {
            for (int y = 0; y < COLS; y++) {
                // Ajoute un arbre si le ratio aléatoire est respecté et si la case n'est pas occupée.
                if (Math.random() <= treeRatio && !isOccupied(x, y)) {
                    Tree tree = new Tree(x, y); // Création de l'arbre.
                    treeList.add(tree);         // Ajout à la liste des arbres.
                    gameElementList.add(tree);  // Ajout aux éléments du jeu.
                    
                    // Enregistre l'image associée à l'arbre si elle n'existe pas encore.
                    imageMap.putIfAbsent(tree.getClass().getName(), new Image(tree.getPathToImage()));
                }
            }
        }
    }


    // Vérifie l'état des arbres récoltés et les régénère si le délai est écoulé.
    private void checkAndRegenerateTrees() {
        long currentTimeToGenerateTree = System.currentTimeMillis(); // Temps actuel en millisecondes.
        
        for (Tree tree : treeList) {
            // Vérifie si l'arbre n'est plus visible et si le délai de régénération est atteint.
            if (!tree.isVisible() && (currentTimeToGenerateTree - Tree.lastCollectedTime >= timeTreeToRegenerate)) {
                tree.regenerate(); // Régénère l'arbre.
                
                // Informe de la régénération de l'arbre dans la console.
                System.out.println("L'arbre à (" + tree.getPosX() + ", " + tree.getPosY() + ") a été régénéré !");
            }
        }
    }


    // Vérifie si un drapeau doit être généré en fonction du délai écoulé et de l'absence de drapeaux existants.
    private void checkAndGenerateFlag() {
        long currentTimeToGenerateFLag = System.currentTimeMillis(); // Temps actuel en millisecondes.
        
        // Vérifie que la liste des drapeaux est vide et que le délai pour la génération est écoulé.
        if (flagList.isEmpty() && (currentTimeToGenerateFLag - Flag.collectionTime >= GENERATION_FLAG_DELAY)) {
            generateFlag(); // Génère un nouveau drapeau si les conditions sont remplies.
        }
    }


    // Génère un drapeau à une position aléatoire sur le jeu.
    private void generateFlag() {
        // Génère une position aléatoire sur le terrain.
        Point position = generateRandomPosition();

        // Crée un nouveau drapeau à la position générée.
        Flag newFlag = new Flag(position.x, position.y, this);

        // Ajoute le drapeau à la liste des drapeaux.
        flagList.add(newFlag);

        // Ajoute le drapeau à la liste générale des éléments du jeu.
        gameElementList.add(newFlag);

        // Affiche dans la console la position où le drapeau a été généré.
        System.out.println("Un nouveau drapeau a été généré à la position (" + position.x + ", " + position.y + ").");
    }


    // Génère un nombre défini de pierres philosophiques à des positions aléatoires
    private void generatePhilosophicalStone() {
        // Boucle pour générer le nombre de pierres défini (NUMBER_OF_STONES)
        for (int i = 0; i < NUMBER_OF_STONES; i++) {
            // Génère une position aléatoire pour chaque pierre
            Point position = generateRandomPosition();

            // Crée une nouvelle pierre philosophique à la position générée
            PhilosophicalStone stone = new PhilosophicalStone(position.x, position.y, this);

            // Ajoute la pierre à la liste générale des éléments du jeu
            gameElementList.add(stone);
        }
    }


    // Vérifie si une case spécifique (x, y) est occupée par un élément du jeu
    private boolean isOccupied(int x, int y) {
        // Parcours la liste des éléments du jeu pour vérifier si la case est occupée
        for (GameElement gameElement : gameElementList) {
            // Ignore les éléments comme les drapeaux et les pierres philosophiques qui ne bloquent pas la position
            if (gameElement instanceof Flag || gameElement instanceof PhilosophicalStone) continue;

            // Vérifie si la position (x, y) correspond à celle de l'élément du jeu
            if (gameElement.getPosX() == x && gameElement.getPosY() == y) {
                return true; // Retourne true si un élément occupe la position
            }
        }
        
        // Vérifie si la case est occupée par une ville
        for (City city : cityList) {
            // Si la ville occupe la position (x, y), retourne true
            if (city.cityOccupiesPosition(x, y)) return true;
        }

        // Si aucune case n'est occupée par un élément du jeu ou une ville, retourne false
        return false;
    }


    // Génère une position aléatoire non occupée sur la carte
    public Point generateRandomPosition() {
        // Crée une liste pour stocker toutes les positions possibles sur la carte
        List<Point> allPositions = new ArrayList<>();

        // Parcourt toutes les cases de la carte (de 0 à ROWS-1 et 0 à COLS-1)
        for (int x = 0; x < ROWS; x++) {
            for (int y = 0; y < COLS; y++) {
                allPositions.add(new Point(x, y)); // Ajoute chaque position (x, y) à la liste
            }
        }

        // Mélange les positions de manière aléatoire pour ne pas privilégier certaines positions
        Collections.shuffle(allPositions);

        // Recherche la première position qui n'est pas occupée
        for (Point position : allPositions) {
            // Si la position n'est pas occupée, on la retourne
            if (!isOccupied(position.x, position.y)) {
                return position;
            }
        }

        // Si aucune position disponible n'est trouvée (toutes les positions sont occupées)
        throw new IllegalStateException("No available positions on the map!"); // Lance une exception
    }


    // Calcule la distance entre deux éléments du jeu (e1 et e2) en utilisant la formule de la distance Euclidienne
    public double calculateDistance(GameElement e1, GameElement e2) {
        // La distance Euclidienne est calculée en utilisant la différence des coordonnées (x et y)
        // et en appliquant la formule : distance = sqrt((x2 - x1)^2 + (y2 - y1)^2)
        return Math.sqrt(Math.pow(e2.getPosX() - e1.getPosX(), mathPow) + Math.pow(e2.getPosY() - e1.getPosY(), mathPow));
    }


    // Vérifie si un mouvement vers une nouvelle position (x, y) est valide
    // Cette méthode vérifie deux conditions :
    // 1. Si la nouvelle position (x, y) est libre (non occupée par un autre élément).
    // 2. Si la position (x, y) est valide selon les règles du jeu définies dans la classe Unit.
    public boolean isMoveValid(int x, int y) {
        // Vérifie d'abord si la position est libre (non occupée)
        // Ensuite, vérifie si la position est valide selon les règles spécifiques des unités
        return !isOccupied(x, y) && Unit.isPositionValid(x, y, this);
    }
}


import java.awt.Point;
import java.util.*;

/**
 * Classe représentant une ville dans le jeu.
 * Cette classe gère les ressources de bois, la génération des unités et les points de spawn et dépôt.
 */
public class City extends GameElement {
    
    // Taille de la ville
    public static final int size = 5;
    
    // Positions des villes blanches et noires sur la carte
    public static final int CITY_WHITE_POS_X = HELBArmyController.COLS / 2 - 3;
    public static final int CITY_WHITE_POS_Y = 0;
    public static final int CITY_BLACK_POS_X = HELBArmyController.COLS / 2 - 3;
    public static final int CITY_BLACK_POS_Y = HELBArmyController.ROWS - size;

    // Type de la ville : "WHITE" ou "BLACK"
    public String cityType;    
    public static final String cityTypeWhite = "WHITE";
    public static final String cityTypeBlack = "BLACK";

    // Chemins d'images pour les villes blanches et noires
    public static final String[] imagePathCityWhite = {"/img/WhiteCampHELBARMY.drawio.png"}; 
    public static final String[] imagePathCityBlack = {"/img/BlackCampHELBARMY.drawio.png"}; 

    // Ressources de bois disponibles dans la ville
    private int woodResources;

    // Dernier temps de génération pour chaque type d'unité
    private Map<String, Long> unitLastGenerationTime;

    // Coûts des unités et temps nécessaires pour les générer
    private static final Map<String, Integer> unitCosts = Map.of(
        "COLLECTOR", 0,
        "DESERTER", 50,
        "CAVALRY", 100,
        "PIKEMAN", 75
    );

    private static final Map<String, Integer> unitGenerationTimes = Map.of(
        "COLLECTOR", 5000,
        "DESERTER", 10000,
        "CAVALRY", 15000,
        "PIKEMAN", 5000
    );

    // Points de spawn et dépôt pour chaque type de ville
    private Point spawnPointWhite = new Point(9, 5);
    private Point spawnPointBlack = new Point(9, 14);
    private Point depotPointWhite = new Point(6, 2);
    private Point depotPointBlack = new Point(6, 17);

    /**
     * Constructeur de la ville, initialisant les ressources et le type de ville.
     */
    public City(int posX, int posY, String[] imagePaths, String cityType, HELBArmyController controller) {
        super(posX, posY, imagePaths);
        this.controller = controller;
        this.cityType = cityType;
        this.woodResources = 0;
        this.unitLastGenerationTime = new HashMap<>();

        // Initialiser le temps de génération pour chaque unité
        for (String unitType : unitCosts.keySet()) {
            unitLastGenerationTime.put(unitType, System.currentTimeMillis());
        }
    }

    /**
     * Retourne le type de la ville ("WHITE" ou "BLACK").
     */
    public String getCityType() {
        return cityType;
    }

    /**
     * Retourne le point de spawn des unités selon le type de la ville.
     */
    public Point getSpawnPoint() {
        return this.cityType.equals(cityTypeWhite) ? spawnPointWhite : spawnPointBlack;
    }

    /**
     * Retourne le point de dépôt des ressources de bois de la ville.
     */
    public Point getDepotPoint() {
        return this.cityType.equals(cityTypeWhite) ? depotPointWhite : depotPointBlack;
    }

    /**
     * Ajoute des ressources de bois à la ville si le bois provient de la même ville.
     */
    public void addWood(int amount, String depositorCityType) {
        if (this.cityType.equals(depositorCityType)) {
            this.woodResources += amount;
        }
    }

    /**
     * Vérifie si une position donnée (x, y) est occupée par la ville.
     */
    public boolean cityOccupiesPosition(int x, int y) {
        return x >= getPosX() && x < getPosX() + size && y >= getPosY() && y < getPosY() + size;
    }

    /**
     * Génère des unités selon les ressources et le temps écoulé depuis la dernière génération.
     */
    public void generateUnits(long currentTime, List<Unit> unitList, List<GameElement> gameElementList) {
        List<String> availableUnits = new ArrayList<>();

        // Vérifie si des unités peuvent être générées
        for (String unitType : unitCosts.keySet()) {
            long lastGenerated = unitLastGenerationTime.getOrDefault(unitType, currentTime);
            int cost = unitCosts.get(unitType);
            int generationTime = unitGenerationTimes.get(unitType);
            long timeElapsed = currentTime - lastGenerated;

            // Si les ressources et le temps permettent de générer l'unité
            if (woodResources >= cost && timeElapsed >= generationTime) {
                availableUnits.add(unitType);
            }
        }

        // Si des unités peuvent être générées, en choisir une au hasard
        if (!availableUnits.isEmpty()) {
            String selectedUnit = availableUnits.get(new Random().nextInt(availableUnits.size()));
            int cost = unitCosts.get(selectedUnit);
            woodResources -= cost;  // Déduit le coût des ressources

            // Met à jour le dernier temps de génération pour l'unité choisie
            unitLastGenerationTime.put(selectedUnit, currentTime);

            // Crée l'unité et l'ajoute à la liste des unités du jeu
            Unit newUnit = createUnit(selectedUnit);
            unitList.add(newUnit);
            gameElementList.add(newUnit);
        }
    }

    /**
     * Crée une unité en fonction du type sélectionné.
     */
    public Unit createUnit(String unitType) {
        Point spawnPoint = getSpawnPoint();
        String[] imagePaths;

        switch (unitType) {
            case "COLLECTOR":
                imagePaths = Collector.getImagePaths(cityType);
                return new Collector(spawnPoint.x, spawnPoint.y, imagePaths, this);
            case "DESERTER":
                imagePaths = Deserter.getImagePaths(cityType);
                return new Deserter(spawnPoint.x, spawnPoint.y, imagePaths, this);
            case "CAVALRY":
                imagePaths = Cavalry.getImagePaths(cityType);
                return new Cavalry(spawnPoint.x, spawnPoint.y, imagePaths, this);
            case "PIKEMAN":
                imagePaths = Pikeman.getImagePaths(cityType);
                return new Pikeman(spawnPoint.x, spawnPoint.y, imagePaths, this, this.controller);
            default:
                throw new IllegalArgumentException("Invalid unit type: " + unitType);
        }
    }

    /**
     * Méthode déclenchée pour effectuer une action (pas d'action définie pour la ville).
     */
    @Override
    public void triggerAction(HELBArmyController controller) {
        // Pas d'action spécifique pour la ville
    }
}

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;

public class HELBArmyView {
    private final int WIDTH; // Largeur de la fenêtre
    private final int HEIGHT; // Hauteur de la fenêtre
    private final long DURATION_MILLIS = 130; // Durée entre chaque rafraîchissement en millisecondes
    private final GraphicsContext gc; // Contexte graphique pour dessiner
    private HELBArmyController controller; // Référence au contrôleur pour gérer la logique du jeu
    public Scene scene; // Scène principale

    // Constructeur pour initialiser la vue et configurer les éléments de la scène
    // Ce constructeur crée la fenêtre de l'application, configure les éléments visuels
    // nécessaires pour afficher le jeu, et met en place un mécanisme de rafraîchissement
    // continu pour la mise à jour de l'affichage du jeu.

    public HELBArmyView(Stage primaryStage, int width, int height) {
        this.WIDTH = width;    
        this.HEIGHT = height;  

        // Création d'un groupe de base pour organiser les éléments graphiques
        Group root = new Group();
        
        // Création d'un canvas qui servira à dessiner les éléments du jeu
        Canvas canvas = new Canvas(width, height);
        root.getChildren().add(canvas);  // Ajout du canvas au groupe racine

        // Création de la scène avec le groupe racine contenant le canvas
        this.scene = new Scene(root);
        
        // Configuration de la scène du stage principal (fenêtre de l'application)
        primaryStage.setScene(scene);  // Attachement de la scène au stage
        primaryStage.setTitle("HELBArmy");  // Définition du titre de la fenêtre
        primaryStage.show();  // Affichage de la fenêtre

        // Obtention du contexte graphique du canvas pour dessiner dessus
        this.gc = canvas.getGraphicsContext2D();

        // Configuration d'une timeline pour gérer les mises à jour périodiques de l'affichage
        // La timeline se déclenche toutes les DURATION_MILLIS millisecondes et appelle
        // la méthode handleGameTick() du contrôleur à chaque tick (rafraîchissement)
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(DURATION_MILLIS), e -> controller.handleGameTick()));
        
        // Définir la timeline pour qu'elle s'exécute de manière indéfinie (en boucle)
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();  // Démarre la timeline pour lancer le rafraîchissement continu
    }


    // Associe un contrôleur à la vue
    public void setController(HELBArmyController controller) {
        this.controller = controller;
    }

    // Met à jour la scène avec les éléments actuels du jeu
    public void updateScene(ArrayList<GameElement> gameElementList) {
        drawBackground(); // Dessiner le fond de la scène
        drawGameElements(gameElementList); // Dessiner les éléments du jeu
    }

    // Dessine un damier pour représenter le fond de la carte
    // Cette méthode crée un fond en damier, alternant entre deux couleurs (blanc et gris) pour chaque case.
    // Chaque case du damier est dessinée sur le canvas en fonction de la taille de la grille du jeu.

    public void drawBackground() {
        // Parcours chaque ligne du damier
        for (int i = 0; i < HELBArmyController.ROWS; i++) {
            // Parcours chaque colonne du damier
            for (int j = 0; j < HELBArmyController.COLS; j++) {
                // Alterne les couleurs entre blanc et gris pour chaque case en fonction de la position
                // Si la somme des indices de ligne (i) et de colonne (j) est paire, la case sera blanche, sinon elle sera grise
                gc.setFill((i + j) % 2 == 0 ? Color.WHITE : Color.GRAY);

                // Dessine un rectangle pour chaque case à sa position correcte dans la grille
                // La position de chaque case est calculée en fonction de sa ligne (i) et de sa colonne (j)
                // La taille de chaque case est déterminée par SQUARE_SIZE
                gc.fillRect(i * HELBArmyController.SQUARE_SIZE, 
                            j * HELBArmyController.SQUARE_SIZE, 
                            HELBArmyController.SQUARE_SIZE, 
                            HELBArmyController.SQUARE_SIZE);
            }
        }
    }

    
    // Dessine les éléments du jeu sur le canvas
    // Cette méthode parcourt la liste des éléments du jeu (gameElementList) et dessine chaque élément à sa position respective sur le canvas.
    // Les éléments peuvent avoir des tailles ou des styles différents, comme les villes qui nécessitent un traitement particulier.

    private void drawGameElements(ArrayList<GameElement> gameElementList) {
        // Parcours de tous les éléments du jeu
        for (GameElement gameElem : gameElementList) {
            // Charge l'image associée à l'élément du jeu (chaque élément a une méthode getPathToImage())
            Image image = new Image(gameElem.getPathToImage());
            
            // Si l'élément est une ville, on utilise une taille spécifique pour dessiner l'image
            if (gameElem instanceof City) {
                // Dessine l'image de la ville en tenant compte de sa taille particulière (City.size)
                gc.drawImage(image, 
                            gameElem.getPosX() * HELBArmyController.SQUARE_SIZE, 
                            gameElem.getPosY() * HELBArmyController.SQUARE_SIZE, 
                            City.size * HELBArmyController.SQUARE_SIZE,  // Taille spécifique de la ville
                            City.size * HELBArmyController.SQUARE_SIZE);
            } else {
                // Dessine les autres éléments avec une taille standard (SQUARE_SIZE)
                gc.drawImage(image, 
                            gameElem.getPosX() * HELBArmyController.SQUARE_SIZE, 
                            gameElem.getPosY() * HELBArmyController.SQUARE_SIZE, 
                            HELBArmyController.SQUARE_SIZE,  // Taille standard pour les autres éléments
                            HELBArmyController.SQUARE_SIZE);
            }
        }
    }
}

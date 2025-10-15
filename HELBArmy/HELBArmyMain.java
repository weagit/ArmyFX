import javafx.application.Application;
import javafx.stage.Stage;

public class HELBArmyMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Création de la vue avec des dimensions de 800x800 pixels
        HELBArmyView view = new HELBArmyView(primaryStage, 800, 800);
        
        // Création du contrôleur et liaison avec la vue
        HELBArmyController controller = new HELBArmyController(view);
        
        // Initialisation et démarrage du jeu
        controller.startGame();
    }

    // Méthode principale qui lance l'application JavaFX
    public static void main(String[] args) {
        // Lancer l'application JavaFX
        launch(args); // Appelle la méthode 'launch' de la classe Application pour démarrer l'application
    }
}

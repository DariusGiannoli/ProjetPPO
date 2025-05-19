package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Représente l'interface de requête, c'est à dire la partie de l'interface graphique
 * qui permet à l'utilisateur de choisir les arrêts de départ et d'arrivée,
 * et la date/heure de voyage désiré.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 *
 * @param rootNode le nœud JavaFX à la racine de son graphe de scène.
 * @param depStopO une valeur observable contenant le nom de l'arrêt de départ.
 * @param arrStopO une valeur observable contenant le nom de l'arrêt d'arrivée.
 * @param dateO une valeur observable contenant la date de voyage.
 * @param timeO une valeur observable contenant l'heure de voyage.
 */
public record QueryUI(Node rootNode, ObservableValue<String> depStopO,
        ObservableValue<String> arrStopO, ObservableValue<LocalDate> dateO,
        ObservableValue<LocalTime> timeO) {

    private static final int SPACING = 5;
    private static final String THIN_SPACE_COLON = "\u202f:";


    /**
     * Construit le graphe de scène pour la recherche de voyages, avec les champs pour choisir
     * les arrêts de départ et d'arrivée ainsi que la date et l'heure souhaitée pour le trajet.
     *
     * @param index un StopIndex, à donner aux StopField pour proposer les arrêts.
     * @return retourne une instance de QueryUI contenant le nœud JavaFX qui se trouve à sa racine.
     */
    public static QueryUI create(StopIndex index) {
        // Création des champs d'arrêts
        StopField depField = createStopField(index, "depStop",
                "Nom de l'arrêt de départ");
        StopField arrField = createStopField(index, "arrStop",
                "Nom de l'arrêt d'arrivée");

        // Bouton d'échange
        // créer le bouton pour échanger l'arrêt de départ et d'arrivée dans les StopField
        Button swapButton = new Button("⟷");
        swapButton.setOnAction(e -> {
            String depText = depField.textField().getText();
            String arrText = arrField.textField().getText();
            arrField.setTo(depText);
            depField.setTo(arrText);
        });

        // Sélecteurs de date et heure
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setId("date");

        // Crée un TextFormatter pour le format de l'heure avec affichage standardisé
        // et analyse flexible de l'entrée utilisateur.
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter parseFormat = DateTimeFormatter.ofPattern("H:mm");

        TextFormatter<LocalTime> timeFormatter = new TextFormatter<>(
                new LocalTimeStringConverter(displayFormat, parseFormat), LocalTime.now());

        TextField timeField = new TextField();
        timeField.setId("time");
        timeField.setTextFormatter(timeFormatter);

        // Construction des panneaux
        HBox topBox = new HBox(SPACING,
                new Label("Départ" + THIN_SPACE_COLON), depField.textField(),
                swapButton,
                new Label("Arrivée" + THIN_SPACE_COLON), arrField.textField());
        HBox bottomBox = new HBox(SPACING,
                new Label("Date" + THIN_SPACE_COLON), datePicker,
                new Label("Heure" + THIN_SPACE_COLON), timeField);

        // Assemblage final
        VBox root = new VBox(SPACING, topBox, bottomBox);
        root.getStylesheets().add("query.css");

        return new QueryUI(root, depField.stopO(), arrField.stopO(), datePicker.valueProperty(),
                timeFormatter.valueProperty());
    }

    /**
     * Permet de créer un StopField présent dans l'interface de requète pour entrer
     * l'arrêt de départ ou d'arrivée du voyage.
     *
     * @param index  un StopIndex, à donner aux StopField pour proposer les arrêts.
     * @param id     l'id du StopField.
     * @param prompt le texte à afficher par défaut dans le champ lorsqu'il est vide.
     * @return retourne un StopField avec les caractéristiques passées en argument.
     */
    private static StopField createStopField(StopIndex index, String id, String prompt) {
        StopField field = StopField.create(index);
        field.textField().setId(id);
        field.textField().setPromptText(prompt);
        return field;
    }

}
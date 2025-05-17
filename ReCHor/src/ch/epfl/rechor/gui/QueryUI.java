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
 * A faire
 */
public record QueryUI(
        Node rootNode,
        ObservableValue<String> depStopO,
        ObservableValue<String> arrStopO,
        ObservableValue<LocalDate> dateO,
        ObservableValue<LocalTime> timeO
) {
    private static final String CSS_PATH = "query.css";
    private static final int SPACING = 5;

    /**
     * Construit le graphe de scène pour la recherche de voyages.
     */
    public static QueryUI create(StopIndex index) {
        VBox root = new VBox(SPACING);
        root.getStylesheets().add(CSS_PATH);

        // Champs d'arrêts (départ/arrivée)
        StopField depField = setupStopField(index, "depStop", "Nom de l'arrêt de départ");
        StopField arrField = setupStopField(index, "arrStop", "Nom de l'arrêt d'arrivée");

        // Bouton d'échange
        Button swapButton = createSwapButton(depField, arrField);

        HBox topo = new HBox(SPACING,
                new Label("Départ\u202f:"), depField.textField(),
                swapButton,
                new Label("Arrivée\u202f:"), arrField.textField()
        );

        // Sélecteurs de date et heure
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setId("date");

        TextFormatter<LocalTime> timeFormatter = createTimeFormatter();
        TextField timeField = new TextField();
        timeField.setId("time");
        timeField.setTextFormatter(timeFormatter);

        HBox bottom = new HBox(SPACING,
                new Label("Date\u202f:"), datePicker,
                new Label("Heure\u202f:"), timeField
        );

        root.getChildren().addAll(topo, bottom);

        return new QueryUI(
                root,
                depField.stopO(),
                arrField.stopO(),
                datePicker.valueProperty(),
                timeFormatter.valueProperty());
    }

    /**
     *
     * @param index
     * @param id
     * @param prompt
     * @return
     */
    private static StopField setupStopField(StopIndex index, String id, String prompt) {
        StopField field = StopField.create(index);
        field.textField().setId(id);
        field.textField().setPromptText(prompt);
        return field;
    }

    /**
     *
     * @param dep
     * @param arr
     * @return
     */
    private static Button createSwapButton(StopField dep, StopField arr) {
        Button swap = new Button("⟷");
        swap.setOnAction(e -> {
            String depText = dep.textField().getText();
            String arrText = arr.textField().getText();
            arr.setTo(depText);
            dep.setTo(arrText);
        });
        return swap;
    }

    /**
     *
     * @return
     */
    private static TextFormatter<LocalTime> createTimeFormatter() {
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("HH:mm");
        return new TextFormatter<>(
                new LocalTimeStringConverter(displayFormat, null),
                LocalTime.now()
        );
    }
}

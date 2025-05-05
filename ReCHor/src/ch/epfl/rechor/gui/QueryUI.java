// File: ch/epfl/rechor/gui/QueryUI.java
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
 * Interface de requête : arrêts, date et heure.
 */
public record QueryUI(
        Node rootNode,
        ObservableValue<String> depStopO,
        ObservableValue<String> arrStopO,
        ObservableValue<LocalDate> dateO,
        ObservableValue<LocalTime> timeO
) {
    /**
     * Construit le graphe de scène pour la recherche de voyages.
     */
    public static QueryUI create(StopIndex index) {
        VBox root = new VBox(5);
        root.getStylesheets().add("query.css");

        // StopFields départ / arrivée
        StopField depField = StopField.create(index);
        StopField arrField = StopField.create(index);
        depField.textField().setId("depStop");
        arrField.textField().setId("arrStop");
        depField.textField().setPromptText("Nom de l'arrêt de départ");
        arrField.textField().setPromptText("Nom de l'arrêt d'arrivée");

        // Bouton d'échange
        Button swap = new Button("⟷");
        swap.setOnAction(e -> {
            String d = depField.textField().getText();
            String a = arrField.textField().getText();
            depField.setTo(a);
            arrField.setTo(d);
        });

        HBox topo = new HBox(5,
                new Label("Départ\u202f:"), depField.textField(),
                swap,
                new Label("Arrivée\u202f:"), arrField.textField()
        );

        // DatePicker
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setId("date");

        // TimeFormatter pour LocalTime
        DateTimeFormatter fmtDisplay = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter fmtParse   = DateTimeFormatter.ofPattern("H:mm");
        TextFormatter<LocalTime> tfmt = new TextFormatter<>(
                new LocalTimeStringConverter(fmtDisplay, fmtParse)
        );
        TextField timeField = new TextField();
        timeField.setId("time");
        timeField.setTextFormatter(tfmt);

        HBox bottom = new HBox(5,
                new Label("Date\u202f:"), datePicker,
                new Label("Heure\u202f:"), timeField
        );

        root.getChildren().addAll(topo, bottom);

        return new QueryUI(
                root,
                depField.stopO(),
                arrField.stopO(),
                datePicker.valueProperty(),
                tfmt.valueProperty()
        );
    }
}

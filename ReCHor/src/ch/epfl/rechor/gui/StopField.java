// StopField.java
package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;

/**
 * Combinaison d'un TextField et d'une Popup pour sélectionner un arrêt.
 * La valeur observable stopO ne change que lorsque le champ perd le focus.
 */
public record StopField(TextField textField, ObservableValue<String> stopO) {
    /**
     * Crée un StopField associé à l'index donné.
     */
    public static StopField create(StopIndex index) {
        TextField tf = new TextField();
        tf.setPromptText("Nom de l'arrêt de départ");

        ReadOnlyStringWrapper selected = new ReadOnlyStringWrapper("");
        ObservableValue<String> stopProp = selected.getReadOnlyProperty();

        Popup popup = new Popup();
        popup.setHideOnEscape(false);
        ListView<String> list = new ListView<>();
        list.setFocusTraversable(false);
        list.setMaxHeight(240);
        popup.getContent().add(list);

        // Navigation clavier ↑/↓
        tf.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.UP) {
                if (list.getSelectionModel().getSelectedIndex() > 0)
                    list.getSelectionModel().selectPrevious();
                e.consume();
            } else if (e.getCode() == KeyCode.DOWN) {
                if (list.getSelectionModel().getSelectedIndex() < list.getItems().size() - 1)
                    list.getSelectionModel().selectNext();
                e.consume();
            }
        });

        // Afficher/cacher la popup à l'entrée/sortie du focus
        tf.focusedProperty().addListener((obs, was, isNow) -> {
            if (isNow) {
                // montrer et remplir la liste
                list.getItems().setAll(index.stopsMatching(tf.getText(), 30));
                list.getSelectionModel().selectFirst();
                Bounds b = tf.localToScreen(tf.getBoundsInLocal());
                popup.setX(b.getMinX());
                popup.setY(b.getMaxY());
                popup.show(tf.getScene().getWindow());
                // mise à jour dynamique au fil de la saisie
                tf.textProperty().addListener((o, old, nw) -> {
                    list.getItems().setAll(index.stopsMatching(nw, 30));
                    list.getSelectionModel().selectFirst();
                });
            } else {
                // cacher et copier la sélection
                tf.textProperty().removeListener((o, old, nw) -> {});
                popup.hide();
                String sel = list.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    tf.setText(sel);
                    selected.set(sel);
                } else {
                    tf.clear();
                    selected.set("");
                }
            }
        });

        return new StopField(tf, stopProp);
    }

    /**
     * Force la valeur du champ et de l'observable à une chaîne donnée.
     */
    public void setTo(String stop) {
        textField.setText(stop);
        ((ReadOnlyStringWrapper) selectedProperty()).set(stop);
    }

    // Récupère la property interne pour le set
    private ReadOnlyStringWrapper selectedProperty() {
        return (ReadOnlyStringWrapper) ((ReadOnlyStringWrapper) stopO).getBean();
    }
}

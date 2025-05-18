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
 * Combinaison d'un TextField et d'un Popup pour sélectionner un arrêt.
 * La valeur observable stopO change lorsque le curseur quitte le champ textuel.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record StopField(TextField textField, ObservableValue<String> stopO) {
    private static final int MAX_SUGGESTIONS = 30;
    private static final double LIST_MAX_HEIGHT = 240.0;


    /**
     * Crée un StopField donc le champ textuel et la fenêtre associés à l'index donné.
     *
     * @param index un StopIndex, à utiliser pour connaitre les arrêts à proposer.
     * @return retourne un StopField associé au StopIndex passé en argument.
     */
    public static StopField create(StopIndex index) {
        TextField textField = new TextField();
        ReadOnlyStringWrapper selected = new ReadOnlyStringWrapper("");

        // Configuration de la popup et de la liste d'arrêts
        ListView<String> list = new ListView<>();
        list.setFocusTraversable(false);
        list.setMaxHeight(LIST_MAX_HEIGHT);

        Popup popup = new Popup();
        popup.setHideOnEscape(false);
        popup.getContent().add(list);

        // Définition de la logique de validation
        StopSelectionHandler handler = new StopSelectionHandler(textField, list, popup, selected);

        handler.configureKeyNavigation();
        handler.configureFocusHandling(index);
        handler.configureSelectionHandling();


        return new StopField(textField, selected);
    }

    /**
     * Force la valeur du TextField et de l'ObservableValue à être la chaine donnée en argument.
     *
     * @param stop la chaine à mettre dans textField et stop0.
     */
    public void setTo(String stop) {
        textField.setText(stop);
        // Nous savons que stopO est un ReadOnlyStringWrapper grâce à la factory method
        ((ReadOnlyStringWrapper) stopO).setValue(stop);
    }


    /**
     * Classe interne pour gérer les interactions avec le champ de sélection d'arrêt.
     *
     * @param textField la valeur de textField donc le texte qui est dans le champ.
     * @param list la liste des noms d'arrêts proposés dans le champ de séléction d'arrêt.
     * @param popup la fenêtre dans laquelle on écrit notre requète.
     * @param selected la proposition d'arrêt sélectionnée.
     */
        private record StopSelectionHandler(TextField textField, ListView<String> list, Popup popup,
                                            ReadOnlyStringWrapper selected) {

            /**
             * Configure la navigation clavier avec les flèches dans la liste des arrêts proposés.
             */
            void configureKeyNavigation() {
                textField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                    var selModel = list.getSelectionModel();
                    if (selModel.isEmpty()) return;

                    if (e.getCode() == KeyCode.UP) {
                        selModel.selectPrevious();
                        list.scrollTo(selModel.getSelectedIndex());
                        e.consume();
                    } else if (e.getCode() == KeyCode.DOWN) {
                        selModel.selectNext();
                        list.scrollTo(selModel.getSelectedIndex());
                        e.consume();
                    }
                });
            }


        /**
         * Configure le comportement lors des changements de focus et de saisie.
         *
         * @param index un StopIndex, utilisé pour proposer les arrêts.
         */
            void configureFocusHandling(StopIndex index) {

                textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (isFocused) {
                        updateListItems(index);
                        positionAndShowPopup();
                    }
                });

                textField.textProperty().addListener((obs, oldText, newText) -> {
                    if(popup.isShowing()) {
                        updateListItems(index);
                        positionAndShowPopup();
                    }
                        });

            }

            /**
             * Configure les événements de validation de séléction d'un arrêt (perte de focus).
             */
            void configureSelectionHandling() {
                textField.focusedProperty().subscribe((isFocused) -> {
                    if (!isFocused) {
                        commitSelection();
                    }
                });
            }

            /**
             * Valide la sélection actuelle, met à jour le TextField et cache la popup.
             */
            private void commitSelection() {
                String selectedItem = list.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    textField.setText(selectedItem);
                    selected.set(selectedItem);
                }
                popup.hide();
            }


        /**
         * Met à jour le contenu de la liste selon le texte saisi dans le champ.
         *
         * @param index un StopIndex, utilisé pour proposer les nouveaux arrêts.
         */
            private void updateListItems(StopIndex index) {
                list.getItems().setAll(index.stopsMatching(textField.getText(), MAX_SUGGESTIONS));
                if (!list.getItems().isEmpty()) {
                    selectAndScrollToFirst();
                }
            }

            /**
             * Sélectionne le premier élément et fait défiler la liste à cet élément lorsque l'on
             * change le contenu saisi dans la.
             */
            private void selectAndScrollToFirst() {
                list.getSelectionModel().selectFirst();
                list.scrollTo(0);
            }

            /**
             * Positionne et affiche la popup sous le champ de texte.
             */
            private void positionAndShowPopup() {
                Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
                popup.setX(bounds.getMinX());
                popup.setY(bounds.getMaxY());
                if (!popup.isShowing()) {
                    popup.show(textField.getScene().getWindow());
                }
            }
        }
}
package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Journey;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {



}

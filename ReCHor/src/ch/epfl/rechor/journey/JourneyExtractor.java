package ch.epfl.rechor.journey;

import java.util.List;

public class JourneyExtractor {

    private JourneyExtractor() {
    }

    public List<Journey> journeys(Profile profile, int depStationId) {
        ParetoFront pf = profile.forStation(depStationId);
        pf.forEach((long criteria) -> {

        });

        return null; // pour l'instant
    }
}

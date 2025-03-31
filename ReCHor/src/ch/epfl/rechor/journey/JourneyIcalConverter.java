package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Classe utilitaire (non instanciable) permettant de convertir un Journey
 * en un événement au format iCalendar.
 * Le résultat est construit en respectant la structure iCalendar demandée.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class JourneyIcalConverter {

    //Constructeur privé pour empêcher l'instanciation.
    private JourneyIcalConverter() {}

    /**
     * Convertit le voyage donné en un événement iCalendar (VCALENDAR contenant un VEVENT).
     *
     * @param journey le voyage à convertir
     * @return une chaîne de caractères au format iCalendar
     */
    public static String toIcalendar(Journey journey){

        // Construction de la description : une étape par ligne, séparées par un saut de ligne
        StringJoiner joiner = new StringJoiner("\\n");
        for (Journey.Leg leg : journey.legs()) {
            switch (leg) {
                case Journey.Leg.Foot foot ->
                        joiner.add(FormatterFr.formatLeg(foot));
                case Journey.Leg.Transport transport ->
                        joiner.add(FormatterFr.formatLeg(transport));
            }
        }

        // Création du builder iCalendar
        IcalBuilder builder = new IcalBuilder();
        builder.begin(IcalBuilder.Component.VCALENDAR)
                .add(IcalBuilder.Name.VERSION, "2.0")
                .add(IcalBuilder.Name.PRODID, "ReCHor")
                .begin(IcalBuilder.Component.VEVENT)
                .add(IcalBuilder.Name.UID, UUID.randomUUID().toString())
                .add(IcalBuilder.Name.DTSTAMP, LocalDateTime.now())
                .add(IcalBuilder.Name.DTSTART, journey.depTime())
                .add(IcalBuilder.Name.DTEND, journey.arrTime())
                .add(IcalBuilder.Name.SUMMARY,
                        journey.depStop().name() + " → " + journey.arrStop().name())
                .add(IcalBuilder.Name.DESCRIPTION, joiner.toString())
                .end()  // Fermeture du VEVENT
                .end(); // Fermeture du VCALENDAR
        return builder.build();
    }
}

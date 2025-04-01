package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;
import ch.epfl.rechor.journey.Journey.Leg.Foot;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Objects;


/**
 * Classe utilitaire finale pour formatter les informations relatives aux trajets.
 * Fournit des méthodes statiques pour obtenir la représentation textuelle de durées,
 * d'heures, de noms de plateformes, et d'étapes de trajet.
 *
 * @author Antoine Lepin
 * @author Darius Giannoli
 */
public final class FormatterFr {

    /**
     * Constructeur privé pour empêcher l'instanciation de cette classe utilitaire.
     */
    private FormatterFr() {}

    /**
     * Formate une durée en chaîne de caractères.
     *
     * @param duration la durée à formater.
     * @return une chaîne représentant la durée au format "X min" ou "Y h Z min".
     */
    public static String formatDuration(Duration duration){
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (hours == 0) {
            return minutes + " min";
        } else {
            return hours + " h " + minutes + " min";
        }
    }

    /**
     * Formate une heure donnée sous le format "HHhmm", où HH représente l'heure (0-23)
     * et mm représente les minutes sur deux chiffres.
     *
     * @param dateTime la date et heure à formater.
     * @return une chaîne représentant l'heure au format "HHhmm".
     */
    public static String formatTime(LocalDateTime dateTime) {
        return dateTime.getHour() + "h" + String.format("%02d", dateTime.getMinute());
    }

    /**
     * Formate le nom de la plateforme (voie ou quai) d'un arrêt.
     *
     * @param stop l'arrêt dont la plateforme doit être formatée.
     * @return une chaîne vide si aucun nom de plateforme n'est défini,
     *         sinon "voie <nom>" si le nom commence par un chiffre, ou "quai <nom>" sinon.
     */
    public static String formatPlatformName(Stop stop) {
        String platformName = stop.platformName();

        if (platformName == null || platformName.isEmpty()){
            return "";
        }

        return Character.isDigit(platformName.charAt(0))
                ? "voie " + platformName
                : "quai " + platformName;
    }

    /**
     * Formate une étape à pied en indiquant son type et sa durée.
     *
     * @param footLeg une étape effectuée à pied.
     * @return une chaîne décrivant l'étape, par exemple "changement (5 min)" ou "trajet à pied (3 min)".
     */
    public static String formatLeg(Foot footLeg) {
        String description = footLeg.isTransfer() ? "changement" : "trajet à pied";
        Duration duration = footLeg.duration();
        String durationStr = formatDuration(duration);
        return description + " (" + durationStr + ")";
    }

    /**
     * Formate une étape en transport public.
     * La chaîne résultante est construite de la manière suivante :
     * "HHhmm NomGareDepart (voie/quai Nom) → NomGareArrivee (arr. HHhmm [voie/quai Nom])".
     *
     * @param leg une étape en transport public.
     * @return une chaîne représentant l'étape en transport public.
     */
    public static String formatLeg(Journey.Leg.Transport leg) {
        StringBuilder sb = new StringBuilder();

        sb.append(formatTime(leg.depTime())).append(' ')
                .append(leg.depStop().name());

        String depPlatform = formatPlatformName(leg.depStop());
        if (!depPlatform.isEmpty()) {
            sb.append(" (").append(depPlatform).append(")");
        }

        sb.append(" → ")
                .append(leg.arrStop().name())
                .append(" (arr. ")
                .append(formatTime(leg.arrTime()));

        String arrPlatform = formatPlatformName(leg.arrStop());
        if (!arrPlatform.isEmpty()) {
            sb.append(' ').append(arrPlatform);
        }
        sb.append(')');

        return sb.toString();
    }

    /**
     * Formate la ligne et le sens de parcours d'une étape en transport public.
     * Le format résultant est "NomLigne Direction Destination".
     *
     * @param transportLeg une étape en transport public.
     * @return une chaîne représentant la ligne et la destination, par exemple "IR 15 Direction Luzern".
     */
    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {
        return transportLeg.route() + " Direction " + transportLeg.destination();
    }
}

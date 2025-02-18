package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;
import ch.epfl.rechor.journey.Journey.Leg.Foot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public final class FormatterFr {

    private FormatterFr() {}

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

    public static String formatTime(LocalDateTime dateTime) {
        // Ex. on veut "jour/mois/année heurehminute"
        // On peut construire un DateTimeFormatter personnalisé :
        /*DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.DAY_OF_MONTH)
                .appendLiteral('/')
                .appendValue(ChronoField.MONTH_OF_YEAR)
                .appendLiteral('/')
                .appendValue(ChronoField.YEAR)
                .toFormatter();

         */

        String hourPart = dateTime.getHour() + "h" + String.format("%02d", dateTime.getMinute());

        // On assemble date + heure
        return dateFormatter.format(dateTime) + " " + hourPart;
    }

    public static String formatPlatformName(Stop stop) {
        // Récupère le nom de la voie/quai
        String platformName = stop.platformName();

        // Si la valeur est null ou vide => chaîne vide
        if (platformName == null || platformName.isEmpty()){
            return "";
        }

        // Si le premier caractère est un chiffre => "voie <nom>"
        if (Character.isDigit(platformName.charAt(0))) {
            return "voie " + platformName;
        } else {
            // Sinon => "quai <nom>"
            return "quai " + platformName;
        }
    }

    public static String formatLeg(Foot footLeg) {
        // Vérifie si c’est un changement au même arrêt (isTransfer = true)
        String description = footLeg.isTransfer() ? "changement" : "trajet à pied";

        // Calcule la durée de l’étape (depTime -> arrTime)
        // ou footLeg.duration() si c’est déjà implémenté dans Foot
        Duration duree = footLeg.duration();

        // Formate la durée (ex. "5 min" ou "1 h 10 min")
        String dureeStr = formatDuration(duree);

        // Construit la chaîne finale
        return description + " (" + dureeStr + ")";
    }

    public static String formatLeg(Journey.Leg.Transport leg) {

        StringBuilder sb = new StringBuilder();

        sb.append(formatTime(leg.depTime())).append(' ');

        sb.append(leg.depStop().name());

        String depPlatform = formatPlatformName(leg.depStop());
        if (!depPlatform.isEmpty()) {
            sb.append(" (").append(depPlatform).append(")");
        }

        // 4) Séparateur " -> "
        sb.append(" -> ");

        // 5) Nom de la gare d'arrivée
        sb.append(leg.arrStop().name());

        // 6) "(arr. HHhmm [voie/quai])"
        sb.append(" (arr. ").append(formatTime(leg.arrTime()));
        String arrPlatform = formatPlatformName(leg.arrStop());
        if (!arrPlatform.isEmpty()) {
            sb.append(' ').append(arrPlatform);
        }
        sb.append(')');

        // (Optionnel) Si vous voulez lister les arrêts intermédiaires :
        // if (!leg.intermediateStops().isEmpty()) {
        //     sb.append("\n  Arrêts intermédiaires : ");
        //     for (Journey.Leg.IntermediateStop is : leg.intermediateStops()) {
        //         sb.append(formatTime(is.arrivalTime()))
        //           .append(' ')
        //           .append(is.stop().name())
        //           // éventuellement voie/quai
        //           .append(", ");
        //     }
        //     // retirer la virgule finale, etc.
        // }

        return sb.toString();
    }

    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {
        // On récupère la ligne et la destination
        String route = transportLeg.route();
        String destination = transportLeg.destination();

        // On construit la chaîne, ex. "IR 15 Direction Luzern"
        return route + " Direction " + destination;
    }








}

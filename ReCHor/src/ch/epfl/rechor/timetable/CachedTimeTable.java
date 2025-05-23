package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * Représente un horaire dont les données qui dépendent de la date (les courses et les liaisons)
 * sont stockées dans un cache, pour un accès ultérieur plus rapide.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class CachedTimeTable implements TimeTable {

    // L'horaire sous-jacent dont les données seront mises en cache
    private final TimeTable underlying;

    // Cache pour les courses et liaisons (dépendant de la date)
    private Trips cachedTrips = null;
    private Connections cachedConnections = null;
    private LocalDate cachedDate = null;

    /**
     * Construit une instance de CachedTimeTable à partir de l'horaire sous-jacent.
     *
     * @param underlying l'horaire dont les courses et liaisons doivent être mises en cache.
     */
    public CachedTimeTable(TimeTable underlying) {
        this.underlying = underlying;
    }

    /**
     * Retourne les gares de l'horaire.
     * Méthode déléguée à l'horaire sous-jacent.
     */
    @Override
    public Stations stations() {
        return underlying.stations();
    }

    /**
     * Retourne les alias des gares.
     * Méthode déléguée à l'horaire sous-jacent.
     */
    @Override
    public StationAliases stationAliases() {
        return underlying.stationAliases();
    }

    /**
     * Retourne les plateformes de l'horaire.
     * Méthode déléguée à l'horaire sous-jacent.
     */
    @Override
    public Platforms platforms() {
        return underlying.platforms();
    }

    /**
     * Retourne les lignes de transport public.
     * Méthode déléguée à l'horaire sous-jacent.
     */
    @Override
    public Routes routes() {
        return underlying.routes();
    }

    /**
     * Retourne les changements (transfers) de l'horaire.
     * Méthode déléguée à l'horaire sous-jacent.
     */
    @Override
    public Transfers transfers() {
        return underlying.transfers();
    }

    /**
     * Retourne les courses actives pour la date donnée.
     * Si les données ont déjà été chargées pour cette date,
     * elles sont retournées directement depuis le cache.
     * Sinon, elles sont chargées depuis l'horaire sous-jacent et stockées dans le cache.
     *
     * @param date la date du voyage.
     * @return les courses actives pour cette date.
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        if (cachedTrips == null || !date.equals(cachedDate)) {
            // Si la date ne correspond pas au cache actuel, on recharge les données
            cachedTrips = underlying.tripsFor(date);
            cachedConnections = underlying.connectionsFor(date);
            cachedDate = date;
        }
        return cachedTrips;
    }

    /**
     * Retourne les liaisons actives pour la date donnée.
     * Si les données ont déjà été chargées pour cette date,
     * elles sont retournées directement depuis le cache.
     * Sinon, elles sont chargées depuis l'horaire sous-jacent et stockées dans le cache.
     *
     * @param date la date du voyage.
     * @return les liaisons actives pour cette date.
     */
    @Override
    public Connections connectionsFor(LocalDate date) {
        if (cachedConnections == null || !date.equals(cachedDate)) {
            cachedTrips = underlying.tripsFor(date);
            cachedConnections = underlying.connectionsFor(date);
            cachedDate = date;
        }
        return cachedConnections;
    }
}
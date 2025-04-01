package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Enregistrement qui permet d'obtenir les tableaux d'octets stockés dans des fichiers,
 * pour manipuler les différents éléments de l'horaire aplati à partir de fichiers.
 *
 * @param directory      le chemin d'accès au dossier contenant les fichiers des données horaires.
 * @param stringTable    la table des chaînes de caractères.
 * @param stations       les gares.
 * @param stationAliases les noms alternatifs des gares.
 * @param platforms      les voies/quais.
 * @param routes         les lignes.
 * @param transfers      les changements.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record FileTimeTable(Path directory, List<String> stringTable, Stations stations,
                            StationAliases stationAliases, Platforms platforms, Routes routes,
                            Transfers transfers)
        implements TimeTable {

    private static final String PATH_PLATFORMS       = "platforms.bin";
    private static final String PATH_ROUTES          = "routes.bin";
    private static final String PATH_ALIASES         = "station-aliases.bin";
    private static final String PATH_STATIONS        = "stations.bin";
    private static final String PATH_TRANSFERS       = "transfers.bin";
    private static final String PATH_FILE            = "strings.txt";
    private static final String PATH_CONNECTIONS     = "connections.bin";
    private static final String PATH_CONNECTIONS_SUC = "connections-succ.bin";
    private static final String PATH_TRIPS           = "trips.bin";

    /**
     * Crée une instance de FileTimeTable à partir du répertoire contenant les fichiers horaires.
     *
     * @param directory le chemin vers le dossier contenant les fichiers
     * @return une nouvelle instance de FileTimeTable
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    public static TimeTable in(Path directory) throws IOException {
        Path strings = directory.resolve(PATH_FILE);
        Charset charset = StandardCharsets.ISO_8859_1;
        List<String> list = List.copyOf(Files.readAllLines(strings, charset));

        ByteBuffer platformByteBuffer = bufferExtractor(directory, PATH_PLATFORMS);
        BufferedPlatforms bufferedPlatforms = new BufferedPlatforms(list, platformByteBuffer);

        ByteBuffer routesByteBuffer = bufferExtractor(directory, PATH_ROUTES);
        BufferedRoutes bufferedRoutes = new BufferedRoutes(list, routesByteBuffer);

        ByteBuffer aliasesByteBuffer = bufferExtractor(directory, PATH_ALIASES);
        BufferedStationAliases bufferedAliases = new BufferedStationAliases(list, aliasesByteBuffer);

        ByteBuffer stationsByteBuffer = bufferExtractor(directory, PATH_STATIONS);
        BufferedStations bufferedStations = new BufferedStations(list, stationsByteBuffer);

        ByteBuffer transfersByteBuffer = bufferExtractor(directory, PATH_TRANSFERS);
        BufferedTransfers bufferedTransfers = new BufferedTransfers(transfersByteBuffer);

        return new FileTimeTable(directory, list, bufferedStations, bufferedAliases, bufferedPlatforms,
                bufferedRoutes, bufferedTransfers);
    }

    /**
     * Extrait et retourne un ByteBuffer correspondant au contenu du fichier indiqué
     * et effectue un mapping en lecture seule de l'intégralité du contenu
     *
     * @param directory le chemin du répertoire contenant le fichier
     * @param path  le nom du fichier à mapper
     * @return  un ByteBuffer mappé en lecture seule contenant le contenu du fichier
     * @throws IOException  i une erreur d'entrée/sortie se produit lors de l'ouverture du fichier ou du mapping
     */
    private static ByteBuffer bufferExtractor(Path directory, String path) throws IOException {
        try (FileChannel s = FileChannel.open(directory.resolve(path))) {
            return  s.map(FileChannel.MapMode.READ_ONLY, 0, s.size());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param date la date du voyage
     * @return une instance de {@code Trips} correspondant aux courses de la date
     * @throws UncheckedIOException si une erreur d'entrée/sortie survient lors du chargement
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        try {
            Path dateDir = directory.resolve(date.toString());
            ByteBuffer tripsByteBuffer = bufferExtractor(dateDir, PATH_TRIPS);
            return new BufferedTrips(stringTable, tripsByteBuffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param date la date du voyage
     * @return une instance de {@code Connections} correspondant aux liaisons de la date
     * @throws UncheckedIOException si une erreur d'entrée/sortie survient lors du chargement
     */
    @Override
    public Connections connectionsFor(LocalDate date) {
        try {
            Path dateDir = directory.resolve(date.toString());
            ByteBuffer connectionsByteBuffer = bufferExtractor(dateDir, PATH_CONNECTIONS);
            ByteBuffer connectionsSuccByteBuffer = bufferExtractor(dateDir, PATH_CONNECTIONS_SUC);
            return new BufferedConnections(connectionsByteBuffer, connectionsSuccByteBuffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
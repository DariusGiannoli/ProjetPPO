package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * Enregistrement qui permet d'obtenir les tableaux d'octets stockés dans des fichiers, pour manipuler les différents éléments de l'horaire aplati à partir de fichiers.
 * @param directory est le chemin d'accès au dossier contenant les fichiers des données horaires.
 * @param stringTable est la table des chaînes de caractères.
 * @param stations sont les gares.
 * @param stationAliases sont les noms alternatifs des gares.
 * @param platforms sont les voies/quai.
 * @param routes sont les lignes.
 * @param transfers les changements.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record FileTimeTable(Path directory, List<String> stringTable, Stations stations, StationAliases stationAliases, Platforms platforms, Routes routes, Transfers transfers) implements TimeTable {

    private static final String pathPlatforms = "platforms.bin";
    private static final String pathRoutes = "routes.bin";
    private static final String pathAliases = "station-aliases.bin";
    private static final String pathStations = "stations.bin";
    private static final String pathTransfers = "transfers.bin";
    private static final String pathFile = "strings.txt";

    /**
     * @param directory est le chemin d'accès au dossier contenant les fichiers des données horaires.
     * @return retourne une nouvelle instance de FileTimeTable dont les données aplaties ont été obtenues à partir des fichiers se trouvant dans le dossier dont le chemin d'accès est donné.
     * @throws IOException est levée en cas d'erreur d'entrée/sortie.
     */
    public static TimeTable in(Path directory) throws IOException {
        Path strings = directory.resolve(pathFile);
        Charset charset = StandardCharsets.ISO_8859_1;
        List<String> list = Files.readAllLines(strings, charset);
        list = List.copyOf(list);

        ByteBuffer platformByteBuffer = bufferExtractor(directory, pathPlatforms);
        BufferedPlatforms bufferedPlatforms = new BufferedPlatforms(list, platformByteBuffer);

        ByteBuffer routesByteBuffer = bufferExtractor(directory, pathRoutes);
        BufferedRoutes bufferedRoutes = new BufferedRoutes(list, routesByteBuffer);

        ByteBuffer aliasesByteBuffer = bufferExtractor(directory, pathAliases);
        BufferedStationAliases bufferedAliases = new BufferedStationAliases(list, aliasesByteBuffer);

        ByteBuffer stationsByteBuffer = bufferExtractor(directory, pathStations);
        BufferedStations bufferedStations = new BufferedStations(list, stationsByteBuffer);

        ByteBuffer transfersByteBuffer = bufferExtractor(directory, pathTransfers);
        BufferedTransfers bufferedTransfers = new BufferedTransfers(transfersByteBuffer);

        return new FileTimeTable(directory, list, bufferedStations, bufferedAliases, bufferedPlatforms, bufferedRoutes, bufferedTransfers);
    }

    private static ByteBuffer bufferExtractor(Path directory, String path) throws IOException {
        try (FileChannel s = FileChannel.open(directory.resolve(path))) {
            return  s.map(FileChannel.MapMode.READ_ONLY, 0, s.size());
        }
    }

    @Override
    public Trips tripsFor(LocalDate date) {
        try {
            ByteBuffer tripsByteBuffer = bufferExtractor(directory.resolve(date.toString()), "trips.bin");
            BufferedTrips bufferedTrips = new BufferedTrips(stringTable, tripsByteBuffer);
            return bufferedTrips;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    @Override
    public Connections connectionsFor(LocalDate date) {
        try {
            Path path = Path.of(date.toString());
            ByteBuffer connectionsByteBuffer = bufferExtractor(directory.resolve(date.toString()), "connections.bin");
            ByteBuffer connectionsSuccByteBuffer = bufferExtractor(directory.resolve(date.toString()), "connections-succ.bin");
            BufferedConnections bufferedConnections = new BufferedConnections(connectionsByteBuffer, connectionsSuccByteBuffer);

            return bufferedConnections;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}

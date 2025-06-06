package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Routes;
import ch.epfl.rechor.journey.Vehicle;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Permet d'accéder à une table de lignes représentée de manière aplatie
 * et implémente l'interface Routes.
 * Cette classe utilise un buffer structuré pour accéder aux données des lignes.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */

public final class BufferedRoutes implements Routes {

    // Indices des champs dans la table principale des lignes
    private static final int NAME_ID = 0; // U16 index de chaîne du nom de la ligne
    private static final int KIND = 1; // U8 type de véhicule desservant la ligne

    // Définition de la structure d'une ligne (route) aplatie
    private static final Structure ROUTE_STRUCTURE = new Structure(
            Structure.field(NAME_ID, Structure.FieldType.U16),
            Structure.field(KIND, Structure.FieldType.U8)
    );

    private final List<String> stringTable;
    private final StructuredBuffer structuredBuffer;

    /**
     * Construit une instance de {@code BufferedRoutes}.
     *
     * @param stringTable la table de chaînes utilisée pour décoder le nom des lignes.
     * @param buffer      le {@code ByteBuffer} contenant les données aplaties des lignes.
     */
    public BufferedRoutes(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.structuredBuffer = new StructuredBuffer(ROUTE_STRUCTURE, buffer);
    }

    /**
     * {@inheritDoc}
     *
     * @param id l'index de la ligne
     * @return le type de véhicule correspondant
     */
    @Override
    public Vehicle vehicle(int id) {
        int vehicleCode = structuredBuffer.getU8(KIND, id);
        return Vehicle.ALL.get(vehicleCode);
    }

    /**
     * {@inheritDoc}
     *
     * @param id l'index de la ligne
     * @return le nom de la ligne
     */
    @Override
    public String name(int id) {
        int nameIndex = structuredBuffer.getU16(NAME_ID, id);
        return stringTable.get(nameIndex);
    }

    /**
     * {@inheritDoc}
     *
     * @return le nombre d'enregistrements (lignes)
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
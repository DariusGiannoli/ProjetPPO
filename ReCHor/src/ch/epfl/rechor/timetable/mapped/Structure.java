package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

/**
 * Class qui sert à faciliter la description de la structure des données aplaties.
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class Structure {

    /**
     * Un type énuméré représentant les trois types de champs possibles, à savoir U8 (un octet), U16 (deux octets) et S32 (quatre octets).
     */
    public enum FieldType {
        U8(1),
        U16(2),
        S32(4);

        /**
         * Taille de chaques champs en nombre de bytes.
         */
        private final int size;

        /**
         * Constructeur du type énuméré.
         * @param size taille du champs en nombre de bytes.
         */
        FieldType(int size) {
            this.size = size;
        }

        /**
         * @return retourne la taille du champ en nombre d'octets.
         */
        private int size() {
            return size;
        }

    }

    /**
     * Enregistrement qui représente un champ.
     * @param index l'index du champ dans la structure.
     * @param type le type du champ.
     */
    //constructeur compact
    public record Field(int index, FieldType type) {
        /**
         * Constructeur compact qui lève une NullPointerException si et seulement si type est null.
         */
        public Field {
            // Vérification pour s’assurer que type n’est pas null
            if (type == null) {
                throw new NullPointerException("FieldType ne doit pas être null");
            }
        }
    }

    /**
     * @param index l'index du champ dans la structure.
     * @param type le type du champ.
     * @return retourne une instance de Field avec les attributs donnés.
     */
    public static Field field(int index, FieldType type) {
            return new Field(index, type);
    }


    private final Field[] fields;
    // Tableau des offsets (en octets) de chaque champ dans la structure
    private final int[] fieldOffsets;
    // Taille totale (en octets) d'un enregistrement de cette structure
    private final int totalSize;


    /**
     * Constructeur de Structure, retourne une structure dont les champs sont ceux donnés,
     * ou lève une IllegalArgumentException si ces champs ne sont pas donnés dans l'ordre.
     * @param fields est la description des différents champs de la structure.
     */
    public Structure(Field... fields) {

        for (int i = 0; i < fields.length; i++) {
            Preconditions.checkArgument(fields[i].index() == i);
        }

        // On clone le tableau pour éviter les modifications extérieures
        this.fields = fields.clone();

        // pré-calcule les offsets (positions) de chaque champ pour accélérer les accès ultérieurs.

        this.fieldOffsets = new int[fields.length];
        int currentOffset = 0;
        for (int i = 0; i < fields.length; i++) {
            fieldOffsets[i] = currentOffset;
            currentOffset += fields[i].type().size();
        }
        this.totalSize = currentOffset;
    }

    /**
     * @return retourne la taille totale, en nombre d'octets, de la structure.
     */
    public int totalSize() {
        return totalSize;
    }

    /**
     * @param fieldIndex index du champ.
     * @param elementIndex index de l'élément.
     * @return retourne l'index, dans le tableau d'octets contenant les données aplaties, du premier octet du champ d'index fieldIndex de l'élément d'index elementIndex.
     */
    // offset d'un champ = somme de la taille de tous les champs qui le précèdent dans l'enregistrement
    public int offset(int fieldIndex, int elementIndex) {
        // L'accès à fieldOffsets[fieldIndex] déclenchera une IndexOutOfBoundsException si fieldIndex est hors limites
        return elementIndex * totalSize + fieldOffsets[fieldIndex];
    }

    /**
     * @return retourne le nombre de champs dans la Structure.
     */
    public int fieldCount() {
        return fields.length;
    }
}

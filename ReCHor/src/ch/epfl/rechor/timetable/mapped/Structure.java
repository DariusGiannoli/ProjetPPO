package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

public final class Structure {

    public enum FieldType {
        U8(1),
        U16(2),
        S32(4);

        private final int size;

        FieldType(int size) {
            this.size = size;
        }

        public int size() {
            return size;
        }

    }

    //constructeur compact
    public record Field(int index, FieldType type) {
        public Field {
            // Vérification pour s’assurer que type n’est pas null
            if (type == null) {
                throw new NullPointerException("FieldType ne doit pas être null");
            }
        }
    }

    public static Field field(int index, FieldType type) {
            return new Field(index, type);
    }


    private final Field[] fields;
    // Tableau des offsets (en octets) de chaque champ dans la structure
    private final int[] fieldOffsets;
    // Taille totale (en octets) d'un enregistrement de cette structure
    private final int totalSize;


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

    public int totalSize() {
        return totalSize;
    }

    // offset d'un champ = somme de la taille de tous les champs qui le précèdent dans l'enregistrement
    public int offset(int fieldIndex, int elementIndex) {
        // L'accès à fieldOffsets[fieldIndex] déclenchera une IndexOutOfBoundsException si fieldIndex est hors limites
        return elementIndex * totalSize + fieldOffsets[fieldIndex];
    }

    public int fieldCount() {
        return fields.length;
    }
}

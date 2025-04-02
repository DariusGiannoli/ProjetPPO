package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

/**
 Facilite la description de la structure des données aplaties.
 * Cette classe permet de définir des champs (Field) avec un type (FieldType) et de calculer
 * rapidement les offsets des champs dans une donnée aplatie.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class Structure {

    /**
     * Types de champs supportés avec leur taille en octets.
     */
    public enum FieldType {
        U8(1),
        U16(2),
        S32(4);

        private final int size;

        //Constructeur
        FieldType(int size) {
            this.size = size;
        }

        /**
         * Retourne la taille (en octets) du type.
         *
         * @return la taille en octets
         */
        private int size() {
            return size;
        }
    }

    /**
     * Enregistrement représentant un champ de la structure.
     *
     * @param index l'indice du champ (doit être 0 pour le premier, 1 pour le second, etc.)
     * @param type  le type du champ (non nul)
     */
    public record Field(int index, FieldType type) {

        /**
         * Constructeur compact qui lève une NullPointerException si et seulement si type est null.
         */
        public Field {
            if (type == null) {
                throw new NullPointerException("FieldType ne doit pas être null");
            }
        }
    }

    /**
     * Méthode utilitaire pour créer un Field sans avoir à écrire le mot-clé new.
     *
     * @param index l'indice du champ
     * @param type  le type du champ
     * @return un Field avec les attributs spécifiés
     */
    public static Field field(int index, FieldType type) {
            return new Field(index, type);
    }


    private final Field[] fields;
    private final int[] fieldOffsets;
    private final int totalSize;


    /**
     * Construit une structure en vérifiant que les champs sont donnés dans l'ordre.
     *
     * @param fields les champs décrivant la structure
     * @throws IllegalArgumentException si l'ordre des indices n'est pas correct
     */
    public Structure(Field... fields) {

        for (int i = 0; i < fields.length; i++) {
            Preconditions.checkArgument(fields[i].index() == i);
        }

        // Copie défensive pour préserver l'immuabilité
        this.fields = fields.clone();

        // Calcul des offsets et de la taille totale en octets
        this.fieldOffsets = new int[fields.length];
        int currentOffset = 0;
        for (int i = 0; i < fields.length; i++) {
            fieldOffsets[i] = currentOffset;
            currentOffset += fields[i].type().size();
        }
        this.totalSize = currentOffset;
    }

    /**
     * Retourne la taille totale (en octets) d'un enregistrement de cette structure.
     *
     * @return la taille totale en octets
     */
    public int totalSize() {
        return totalSize;
    }

    /**
     * Calcule l'offset dans le tableau d'octets pour un champ donné d'un élément donné.
     *
     * @param fieldIndex   l'indice du champ
     * @param elementIndex l'indice de l'élément
     * @return l'offset correspondant dans le tableau d'octets
     */
    public int offset(int fieldIndex, int elementIndex) {
        // L'accès à fieldOffsets[fieldIndex] déclenchera une IndexOutOfBoundsException
        // si fieldIndex est hors limites
        return elementIndex * totalSize + fieldOffsets[fieldIndex];
    }

    /**
     * Retourne le nombre de champs dans la structure.
     *
     * @return le nombre de champs
     */
    public int fieldCount() {
        return fields.length;
    }
}

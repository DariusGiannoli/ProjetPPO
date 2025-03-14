package ch.epfl.rechor.timetable.mapped;


import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.*;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;
import static ch.epfl.rechor.timetable.mapped.Structure.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MyStructureTest {

    @Test
    void testStructureStations() {

        List<String> stringStationList = new ArrayList<>();
        stringStationList.add("  ");
        stringStationList.add("  ");
        stringStationList.add("  ");
        stringStationList.add("  ");
        stringStationList.add("Lausanne");
        stringStationList.add("  ");
        stringStationList.add("Zurich");

        byte[] tableauBytes = {0x00, 0x04, 0x04, (byte) 0xb6, (byte) 0xca, 0x14, 0x21, 0x14, 0x1f, (byte) 0xa1, 0x00, 0x06, 0x04, (byte) 0xdc, (byte) 0xcc, 0x12, 0x21, 0x18, (byte) 0xda, 0x03};
        ByteBuffer buffer = ByteBuffer.wrap(tableauBytes);


        BufferedStations stations = new BufferedStations(stringStationList, buffer);
        assertEquals(stations.name(0), "Lausanne");
        assertEquals(stations.name(1), "Zurich");
        assertEquals(stations.latitude(0), 46.5167919639498);
        assertEquals(stations.latitude(1), 46.54276396147907);
        assertEquals(stations.longitude(0), 6.629091985523701);
        assertEquals(stations.longitude(1), 6.837874967604876);
        assertEquals(stations.size(), 2);
    }

    @Test
    void myStationsFrenchGamingTest_CE() {
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex("00 04 04 b6 ca 14 21 14 1f a1 00 06 04 dc cc 12 21 18 da 03 01 43 00 0a d4 e8 b0 c9 ad ef");
        // 00 0a d4 e8 = 709864 = 0.059500113129615784;
        // b0 c9 ad ef = 2966007279 = 248.6078581865876913;
        ByteBuffer bytesBuffer = ByteBuffer.wrap(bytes);

        List<String> stringStationList = new ArrayList<String>(Arrays.asList("kdfsk", "efz", "1", "70", "Anet", "Ins", "Lausanne", "Losanna",
                "Palezieux", "Poutschidi", "PipiPow", "scoubididou", "dripaw",
                "20 Allée des cramtés", "12", "Skibbidi gare", "train de vie",
                "🦕", "Norman Thavaux", "PolyRat", "les fesses d'Augustin",
                "95 Avenue Apagnan", "QuoicouBoulevard",

                // Noms absurdes
                "Gare de la Cramtance", "Tunnel du Rat Crevé", "St-Honorin-les-Chips",
                "Place du Yippee", "Impasse du ScoubiSnack", "Pont du TurboFlemmard",
                "Rue de la Friteuse", "Station McFlurry", "Arrêt KFC de l’Apocalypse",
                "Bordelais-les-Saucisses", "Cul-de-sac de la Schlagance",

                // Surnoms absurdes
                "Jean-Miche-Moi", "Tonton Flingueur", "Mémé Turbo", "Le Rat Pêcheur",
                "Sauciflard le Grand", "Maître Boulard", "El Chipiron", "Gégé la Débrouille",
                "Papy Braquage", "Mamie Beretta", "Didier La Chaussette", "Kéké Nitro",
                "Tata Mojito", "Jojo les Tongs", "Bertrand Canicule",

                // Lieux absurdes
                "Rond-Point du Yolo", "Échangeur des Quoicoubeh", "Avenue du Dab",
                "Cité des Enfants Perdus (et Bourrés)", "HLM des Champions",
                "Zone Industrielle des Ratés", "Jardin Public des Dépressifs",
                "Aire de Repos 'La Sieste Éternelle'", "Boulevard du Frisson",

                // Trucs d’internet
                "LOL City", "VroumVroumVille", "Ratio Town", "NoSkillLand",
                "Mdrlavideo", "SansSoucis.exe", "CerveauLag.com", "NoName99",
                "KarmaZone", "Trollinette-sur-Loire", "Genshin-Village",
                "Osu!Land", "Fortnite-les-Bains", "Skibidi-Hall",

                // Noms inspirés de vrais endroits, remixés
                "Montparnasse-les-Piges", "RER ZZZ", "Marseille-sur-Boue",
                "Toulouse-les-Bras-Cassés", "Orléans-la-Peuf", "Lille-la-Flemme",
                "Strasbourg-dans-l'Ombre", "Gare de Saint-Malodé", "Valence-la-Goudronnée",

                // Plus de surnoms et de brainrot français
                "Titi Turbo", "Grand-Mère Crameuse", "Bébert l’Incroyable",
                "Chabichou-de-Combat", "Nico le Malin", "Dédé Dynamite",
                "Pépito Bang Bang", "Francis le Taximan", "Raoul la Grosse Patate",
                "La Mouette du 93", "Poussin Frappé", "Momo Bagarre",
                "Jacky Nitro", "Patapouf Gangsta", "Jojo Racaillou",

                // Références et délires
                "Baguettosaurus Rex", "FromageLand", "Cordon Bleu City",
                "Tacos Mégablast", "Camembert-sur-Seine", "Croque-Monsieur Beach",
                "Vin Rouge-Les-Bains", "Pain Perdu Ville", "Omelette-du-Fromage-Central",
                "GigaChèvre 3000", "Le Bifteck Mystique", "La Buvette des Enfers",

                // Ajout de brainrot complet
                "Pépin le Radis", "Clémentine l’Éclair", "Capitaine Raclette",
                "Zizou des Abysses", "Dudu le Pendu", "Kévin TurboFlex",
                "Cacahuète Sauvage", "Le Big Mac Égaré", "Tonton Cactus",
                "Papy Boomer Max", "Jean-Miche-Rienafaire", "Didou le Fourbe",
                "Les Nuggets du Turfu", "M. Mouette", "Titi Tofu",
                "Le Rat Lucide", "Grand Chef Chaussette", "L’Empereur du Picon",

                // Émojis random et brainrot maximal
                "🔥 Gare du Feu", "🌊 Plage des Vagues Fatiguées", "🦆 CoinCoin-Les-Bains",
                "🦄 Licorneville", "🐢 L’Autoroute du Speedrun", "🤡 Cirque du Brainrot",
                "🎩 Monsieur Elégance", "🍕 Pizza-les-Flots", "🚀 Fusée Turbo",
                "🌭 Hot-Dog City", "🐸 Rainette-sur-Marne", "🥖 Tradition-sur-Seine",

                // Continue jusqu'à 400...
                "Tramway du Malaise", "Panneau Stop de l’Enfer", "Raccourci des Légendes",
                "Gare des âmes en peine", "Passage des Tunnels sans Fin", "Pépèreland",
                "Métro Bizarre", "Vélo-Éclair", "Périph’ des Damnés", "Rond-Point du Doute",
                "Pont de la Chèvre Perdue", "Autoroute de la Patate Chaude",
                "Porte des Pépitos", "Bretelle de la Sieste", "Route du Nutella Déversé",

                "La Maison qui Marche", "Boulevard des Crocs Mortels", "La Rue du Pourquoi",
                "Vortex du McDo", "Parking de la Désolation", "Place du Wesh",
                "Gouffre de l’Indécision", "Allée des Fantômes Fatigués",

                "Sac à dos Sentimental", "Tartiflette Sauvage", "La Ruelle des Croissants",
                "La Forêt des Incompréhensions", "Gare de la Sauce Blanche",

                "Borne d’Arcade du Destin", "La Brasserie du Frometon",
                "Café des Âmes Perdues", "Snack du Vide Émotionnel", "Kebab des Champions",
                "GigaBuvette 2000", "Caverne du Barbecue Ultime",

                "Métro Ligne 404", "Arrêt de Bus des Mots Perdus", "TGV vers l’Inconnu",
                "Train Intergalactique", "TER de la Maldance", "Gare des Ratés",
                "Autoroute des Gens Louches", "Station des Pépettes Égarées",
                "Périphérique du Doute", "Boulevard des AirPods Perdus",
                "Route des Chaussettes Trouées", "Impasse du Croque-Monsieur",
                "Chemin des Frites Sombres", "Pont du Dropkick", "Esplanade du Poulet Frit",

                "Dédale du Wesh", "Sentier du Jean-Michel", "Station WTF", "Rond-Point de l’Absurde",


                // Stations du Chaos
                "Gare de la Maldance", "Station des Âmes Perdues", "Tunnel du Fond du Gouffre",
                "Impasse de la Dernière Chance", "Route du Zbeul Infini", "Chemin de Traverse des Ratés",
                "Station Sans Issue", "RER du Malaise", "Avenue de la Sauce Blanche",
                "Autoroute des Clowns Tristes", "Bretelle de l’Incompréhension",
                "Station du Débat Éternel", "Arrêt de Bus de la Réflexion Tardive",

                // Rues et avenues de l'absurde
                "Rue des Crocs Morts", "Avenue du Turfu Sombre", "Boulevard du Croissant Dépressif",
                "Impasse des Clowns Fatigués", "Rond-Point du PLS", "Pont de la Mélancolie Joyeuse",
                "Allée des Pépettes Perdues", "Rue de la Chèvre Foudroyée",
                "Chemin du Rat Lucide", "Rond-Point des Pépitos Tombés",

                // Surnoms absurdes & personnages mystiques
                "Jojo Sauciflard", "Didier PLS", "Raoul le Turbo", "Jean-Michel Désastre",
                "Bernard Sauce Blanche", "Tonton Fumier", "Jacky Deux Vitesses",
                "Pépé Traquenard", "Mamie Javel", "Francis la Peur",
                "Kéké la Détresse", "Dudu la Semelle", "Chico Maléfique",
                "Jean-Kévin le Déçu", "Pépito Gargantua", "Momo Charcuterie",
                "Serge la Frite", "Gégé TurboFlemme", "Patrick Désintégré",

                // Lieux du Chaos
                "Snack du Vide Émotionnel", "Cimetière des Croissants Perdus",
                "Bistrot du Fromage Déchu", "Brasserie du Grand Déni",
                "La Buvette des Âmes Sèches", "Kebab du Dernier Espoir",
                "McDo du Jugement Dernier", "Café de la Giga-Raclée",
                "Brasserie des Pépis", "Auberge des Champions Déchus",

                // Brainrot ultime
                "Forteresse du Zbeul", "Tour de la Maldance", "Citadelle du Yippee",
                "Dédale du Jean-Michel", "Labyrinthe du Scoubididou",
                "Cave des Croutons", "Donjon de la Raclette",
                "Plage des Cornichons Tristes", "Volcan de la Défaite",

                // Émojis et absurdités visuelles
                "🦆 CoinCoin-Plage", "🔥 Gare du Braquage", "💀 Rond-Point du Non-Retour",
                "🛑 Stop-les-Dégâts", "🚀 Fusée TurboFlex", "🎭 Théâtre du Scandale",
                "🥖 Tradiland", "🧀 Camembert-Sur-Terre", "🥤 Fontaine de l'Orangina Sacré",
                "📉 Cours du Bitcoin Effondré", "👀 Station du Regard Pesant",

                // Noms de bouffe remixés
                "Pôle Nord du Tacos", "Antarctique du Kebab", "Archipel du Gratin Dauphinois",
                "Frontière du McBaguette", "Lac du Nutella Renversé",
                "Île du Croque-Monsieur Perdu", "Toundra du Camembert",
                "Désert du Curry-Dinde", "Pic de la Fondue Noire", "Vallée du Big Mac Sacré",

                // Transports de la Maldance
                "Métro Ligne 666", "Tramway de la Faucheuse", "Funiculaire du PLS",
                "TGV de l'Angoisse", "Train de la Nuit Sans Fin", "Navette Spatiale du Doute",
                "Bus de la Déchéance", "Scooter des Enfers", "Péniche du Mal Aigu",
                "Chemin de Fer des Fesses d'Augustin",

                // Délire complet
                "Temple des Rats Perchés", "Carrefour des Énergies Basses",
                "Hall du Burnout", "Stade des Petits Bras", "Parking du Grand Non",
                "Bibliothèque des TikTok Perdus", "Musée du Dernier Mouvement",
                "Grotte du Dernier Fromage", "Marécage du Malaise Chronique",
                "Pâtisserie des Âmes Errantes",

                // Titre et surnoms de champions du malaise
                "Baron de la Chaussette Solitaire", "Empereur du Pain Rassi",
                "Duc de la Flemme Universelle", "Seigneur du Jambon Perdu",
                "Comte du Saucisson Disparu", "Général du Plateau de Fromage",
                "Archiduc de la Ratitude", "Prince du Dernier Noyau d’Olive",

                // Encore plus de lieux absurdes
                "Forêt des Cornichons Mystiques", "Canyon des Peignoirs Fatigués",
                "Jungle des Tupperwares Perdus", "Désert des Gens qui Promettent de Rappeler",
                "Île des Codes Wifi Disparus", "Lac du Silure Sacré",
                "Mer du Gras Infinie", "Péninsule de la Chaise Qui Grince",

                // Derniers étages du brainrot
                "Sommet du Slip Perdu", "Crique des Rayures Inutiles",
                "Tunnel des Talons Qui Claquent", "Autoroute du Ralenti",
                "Pont des Pavés Mouillés", "Immeuble des Gens Qui Parlent Trop Fort",
                "Rivière du Mauvais Timing", "Bois du SMS Non Répondu",

                // Bonus TurboGoofy
                "Gare des Portes Qui Coincent", "Station de l'Imprimante Révoltée",
                "Terminal des Ongles Cassés", "Aire de Repos des Chaussures Mal Lacées",
                "Rond-Point de l’Allergie Surprise", "Kebab de l’Hésitation",
                "Snack de l’Inconnu", "Pôle Emploi du Fayotage Raté",
                "Grotte du Silence Pesant", "Jardin des Barbecue Maudits"));
        System.out.println(stringStationList.size());
        BufferedStations stations = new BufferedStations(stringStationList, bytesBuffer);
        assertEquals(stations.name(2), (stringStationList.get(323)));
        assertEquals(stations.longitude(2), 0.059500113129615784);
        assertEquals(stations.latitude(2), -111.39214181341231);
    }

    static public byte[] toByte(int[] xs) {
        byte[] res = new byte[xs.length];
        for (int i = 0; i < xs.length; i++) {
            res[i] = (byte) xs[i];
        }
        return res;
    }


    @Test
    void givenExempleBd() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");


        int[] stationsByteAsInt = {0x00, 0x04, 0x04, 0xb6, 0xca, 0x14, 0x21, 0x14, 0x1f, 0xa1, 0x00, 0x06, 0x04, 0xdc, 0xcc, 0x12, 0x21, 0x18, 0xda, 0x03};
        byte[] stationsByte = toByte(stationsByteAsInt);
        ByteBuffer stationsByteBuffer = ByteBuffer.wrap(stationsByte);
        BufferedStations bufferedStations = new BufferedStations(stringTable, stationsByteBuffer);


        assertEquals(2, bufferedStations.size());
        assertEquals("Lausanne", bufferedStations.name(0));
        assertEquals("Palézieux", bufferedStations.name(1));
        assertEquals(6.629092, bufferedStations.longitude(0), 0.001);
        assertEquals(6.837875, bufferedStations.longitude(1), 0.001);
        assertEquals(46.516792, bufferedStations.latitude(0), 0.001);
        assertEquals(46.542764, bufferedStations.latitude(1), 0.001);
    }

    public static ByteBuffer byteBufferOfLength(int n) {
        int[] aliasesByteAsInt = new int[n];
        byte[] aliasesByte = toByte(aliasesByteAsInt);
        return ByteBuffer.wrap(aliasesByte);
    }

    @Test
    void testSizeStationsBd() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");


        int bytePerUnit = 10;
        for (int i = 0; i < 100; i++) {
            ByteBuffer byteBuffer = byteBufferOfLength(i * bytePerUnit);
            BufferedStations bufferedStations = new BufferedStations(stringTable, byteBuffer);
            assertEquals(i, bufferedStations.size());
        }
    }

    @Test
    void testInvalidLengthStationsBd() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");


        int bytePerUnit = 10;
        for (int i = 0; i < 100; i++) {
            for (int j = 1; j < bytePerUnit; j++) {
                ByteBuffer byteBuffer = byteBufferOfLength(i * bytePerUnit + j);
                assertThrows(IllegalArgumentException.class, () -> {
                    BufferedStations bufferedStations = new BufferedStations(stringTable, byteBuffer);
                });
            }
        }
    }

    @Test
    void testNameGettersStationsBd() {
        int n = 100;
        List<String> stringTable = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("yolo");
            stringTable.add(sb.toString());
        }


        int bytePerUnit = 10;
        int offset = 1;
        for (int i = 0; i < n; i++) {
            int[] byteAsInt = new int[i * bytePerUnit];
            for (int j = 0; j < i; j++) {
                byteAsInt[j * bytePerUnit + offset] = j;
            }
            byte[] aliasesByte = toByte(byteAsInt);
            ByteBuffer byteBuffer = ByteBuffer.wrap(aliasesByte);


//            System.out.println(Arrays.toString(aliasesByte));


            BufferedStations bufferedStations = new BufferedStations(stringTable, byteBuffer);
            for (int j = 0; j < i; j++) {
//                System.out.printf("here's my j: %d; size: %d\n", j, bufferedStations.size());
                assertEquals(stringTable.get(j), bufferedStations.name(j));
            }
        }
    }
    @Test
    void testFieldWithNoFieldType(){
        assertThrows(NullPointerException.class, () -> field(1, null));
    }

    @Test
    void testSize(){
        Structure.Field field1 = field(0, U8);
        Structure.Field field2 = field(1, Structure.FieldType.U16);
        Structure.Field field3 = field(2, S32);
        Structure structure = new Structure(field1, field2, field3);
        assertEquals(1 + 2 + 4, structure.totalSize());
    }

    @Test
    void testInvalidIndex(){
        Structure.Field field1 = field(0, U8);
        Structure.Field field2 = field(1, Structure.FieldType.U16);
        Structure.Field field3 = field(2, S32);
        Structure structure = new Structure(field1, field2, field3);
        assertThrows(IndexOutOfBoundsException.class, ()-> structure.offset(-1, 3));
        assertThrows(IndexOutOfBoundsException.class, ()-> structure.offset(3, (structure.totalSize()+1)));
        assertThrows(IndexOutOfBoundsException.class, ()-> structure.offset(3, -1));
    }

    @Test
    void testUnorderedFields(){
        Structure.Field field1 = field(0, U8);
        Structure.Field field2 = field(1, Structure.FieldType.U16);
        Structure.Field field3 = field(2, S32);
        assertThrows(IllegalArgumentException.class, ()-> new Structure(field1, field3, field2));
    }

    @Test
    void testValidOffSet() {
        Structure.Field f1 = new Structure.Field(0, U8);
        Structure.Field f2 = new Structure.Field(1, Structure.FieldType.U16);
        Structure structure = new Structure(f1, f2);


        assertEquals(0, structure.offset(0, 0));
        assertEquals(1, structure.offset(1, 0));
    }


        // Existing tests...
        @Test
        void structureCreationWorks() {
            Structure structure = new Structure(
                    field(0, U8),
                    field(1, U16),
                    field(2, S32)
            );
            assertEquals(7, structure.totalSize()); // 1 + 2 + 4 bytes
        }


        @Test
        void structureWithIncorrectFieldOrderThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Structure(field(1, U8), field(0, U16)));
        }


        @Test
        void structureOffsetCalculationIsCorrect() {
            Structure structure = new Structure(
                    field(0, U8),   // offset 0
                    field(1, U16),  // offset 1
                    field(2, S32)   // offset 3
            );
            assertEquals(0, structure.offset(0, 0));
            assertEquals(1, structure.offset(1, 0));
            assertEquals(3, structure.offset(2, 0));


            // Next element
            assertEquals(7, structure.offset(0, 1));  // 7 = 0 + 1*totalSize
            assertEquals(8, structure.offset(1, 1));  // 8 = 1 + 1*totalSize
            assertEquals(10, structure.offset(2, 1)); // 10 = 3 + 1*totalSize
        }


        @Test
        void structuredBufferCreationWorks() {
            Structure structure = new Structure(field(0, U16));
            ByteBuffer buffer = ByteBuffer.allocate(10);
            StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);
            assertEquals(5, sBuffer.size());
        }


        @Test
        void structuredBufferThrowsForInvalidSize() {
            Structure structure = new Structure(field(0, U16));
            ByteBuffer buffer = ByteBuffer.allocate(3); // Not a multiple of 2
            assertThrows(IllegalArgumentException.class, () ->
                    new StructuredBuffer(structure, buffer));
        }


        @Test
        void structuredBufferGetMethodsWorkCorrectly() {
            Structure structure = new Structure(
                    field(0, U8),
                    field(1, U16),
                    field(2, S32)
            );


            ByteBuffer buffer = ByteBuffer.allocate(14); // For 2 elements


            // Set values for first element
            buffer.put(0, (byte) 200);             // U8 (will be unsigned 200)
            buffer.putShort(1, (short) 60000);     // U16 (will be unsigned 60000)
            buffer.putInt(3, -100000);             // S32 (signed -100000)


            // Set values for second element
            buffer.put(7, (byte) -1);              // U8 (will be unsigned 255)
            buffer.putShort(8, (short) -1);        // U16 (will be unsigned 65535)
            buffer.putInt(10, Integer.MAX_VALUE);  // S32 (signed max int)


            StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);


            // Test first element
            assertEquals(200, sBuffer.getU8(0, 0));
            assertEquals(60000, sBuffer.getU16(1, 0));
            assertEquals(-100000, sBuffer.getS32(2, 0));


            // Test second element
            assertEquals(255, sBuffer.getU8(0, 1));
            assertEquals(65535, sBuffer.getU16(1, 1));
            assertEquals(Integer.MAX_VALUE, sBuffer.getS32(2, 1));
        }


        @Test
        void bufferedStationsWorksCorrectly() {
            List<String> stringTable = Arrays.asList("Lausanne", "Genève", "Zürich");


            // Create a buffer with 2 stations
            ByteBuffer buffer = ByteBuffer.allocate(20); // 2 * (2 + 4 + 4) bytes


            // Station 0: Lausanne at 6.63, 46.52
            buffer.putShort(0, (short) 0);                // NAME_ID: "Lausanne"
            buffer.putInt(2, (int)((6.63 * Math.pow(2, 32)/360)));    // LON
            buffer.putInt(6, (int)(46.52 * Math.pow(2, 32)/360));   // LAT


            // Station 1: Genève at 6.14, 46.21
            buffer.putShort(10, (short) 1);               // NAME_ID: "Genève"
            buffer.putInt(12, (int)(6.14 * Math.pow(2, 32)/360));   // LON
            buffer.putInt(16, (int)(46.21 * Math.pow(2, 32)/360));  // LAT


            BufferedStations stations = new BufferedStations(stringTable, buffer);


            assertEquals(2, stations.size());
            assertEquals("Lausanne", stations.name(0));
            assertEquals(6.63, stations.longitude(0), 0.0001);
            assertEquals(46.52, stations.latitude(0), 0.0001);


            assertEquals("Genève", stations.name(1));
            assertEquals(6.14, stations.longitude(1), 0.0001);
            assertEquals(46.21, stations.latitude(1), 0.0001);
        }


        @Test
        void bufferedStationAliasesWorksCorrectly() {
            List<String> stringTable = Arrays.asList("Lausanne", "Losanna", "Geneva", "Genève");


            // Create a buffer with 2 aliases
            ByteBuffer buffer = ByteBuffer.allocate(8); // 2 * (2 + 2) bytes


            // Alias 0: Losanna -> Lausanne
            buffer.putShort(0, (short) 1);  // ALIAS_ID: "Losanna"
            buffer.putShort(2, (short) 0);  // STATION_NAME_ID: "Lausanne"


            // Alias 1: Geneva -> Genève
            buffer.putShort(4, (short) 2);  // ALIAS_ID: "Geneva"
            buffer.putShort(6, (short) 3);  // STATION_NAME_ID: "Genève"


            BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);


            assertEquals(2, aliases.size());
            assertEquals("Losanna", aliases.alias(0));
            assertEquals("Lausanne", aliases.stationName(0));


            assertEquals("Geneva", aliases.alias(1));
            assertEquals("Genève", aliases.stationName(1));
        }


        @Test
        void bufferedPlatformsWorksCorrectly() {
            List<String> stringTable = Arrays.asList("1", "70", "Lausanne", "Zürich");


            // Create a buffer with 2 platforms
            ByteBuffer buffer = ByteBuffer.allocate(8); // 2 * (2 + 2) bytes


            // Platform 0: "1" at Lausanne (stationId 0)
            buffer.putShort(0, (short) 0);  // NAME_ID: "1"
            buffer.putShort(2, (short) 0);  // STATION_ID: 0 (Lausanne)


            // Platform 1: "70" at Zürich (stationId 1)
            buffer.putShort(4, (short) 1);  // NAME_ID: "70"
            buffer.putShort(6, (short) 1);  // STATION_ID: 1 (Zürich)


            BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);


            assertEquals(2, platforms.size());
            assertEquals("1", platforms.name(0));
            assertEquals(0, platforms.stationId(0));


            assertEquals("70", platforms.name(1));
            assertEquals(1, platforms.stationId(1));
        }


        // Additional Structure tests


        @Test
        void structureWithEmptyFieldsWorks() {
            Structure structure = new Structure();
            assertEquals(0, structure.totalSize());
        }


        @Test
        void structureWithSingleFieldWorks() {
            Structure structure = new Structure(field(0, S32));
            assertEquals(4, structure.totalSize());
        }


        @Test
        void structureWithSameTypeFieldsWorks() {
            Structure structure = new Structure(
                    field(0, U8),
                    field(1, U8),
                    field(2, U8)
            );
            assertEquals(3, structure.totalSize());
            assertEquals(0, structure.offset(0, 0));
            assertEquals(1, structure.offset(1, 0));
            assertEquals(2, structure.offset(2, 0));
        }


        @Test
        void structureOffsetWithMultipleElementsIsCorrect() {
            Structure structure = new Structure(
                    field(0, U8),
                    field(1, U16)
            );
            assertEquals(0, structure.offset(0, 0));
            assertEquals(1, structure.offset(1, 0));


            assertEquals(3, structure.offset(0, 1));
            assertEquals(4, structure.offset(1, 1));


            assertEquals(6, structure.offset(0, 2));
            assertEquals(7, structure.offset(1, 2));
        }


        @Test
        void structureWithInvalidFieldIndexThrows() {
            assertThrows(IndexOutOfBoundsException.class, () -> {
                Structure structure = new Structure(field(0, U8));
                structure.offset(1, 0); // Trying to access field index 1 which doesn't exist
            });
        }


        // Additional StructuredBuffer tests


        @Test
        void structuredBufferWithZeroElementsWorks() {
            Structure structure = new Structure(field(0, U8), field(1, U16));
            ByteBuffer buffer = ByteBuffer.allocate(0);
            StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);
            assertEquals(0, sBuffer.size());
        }


        @Test
        void structuredBufferWithManyElementsWorks() {
            Structure structure = new Structure(field(0, U8));
            ByteBuffer buffer = ByteBuffer.allocate(1000);
            StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);
            assertEquals(1000, sBuffer.size());
        }


        @Test
        void structuredBufferThrowsOnOutOfBoundsAccess() {
            Structure structure = new Structure(field(0, U8));
            ByteBuffer buffer = ByteBuffer.allocate(3);
            StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);


            // This should work
            sBuffer.getU8(0, 2);


            // This should throw
            assertThrows(IndexOutOfBoundsException.class, () -> sBuffer.getU8(0, 3));
        }


        @Test
        void structuredBufferAccessesLastElementCorrectly() {
            Structure structure = new Structure(field(0, U8));
            ByteBuffer buffer = ByteBuffer.allocate(5);
            buffer.put(4, (byte) 123);  // Last element


            StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);
            assertEquals(123, sBuffer.getU8(0, 4));
        }


        @Test
        void structuredBufferHandlesDifferentFieldTypeCombinations() {
            Structure structure = new Structure(
                    field(0, U16),
                    field(1, S32),
                    field(2, U8)
            );


            ByteBuffer buffer = ByteBuffer.allocate(14); // For 2 elements (2+4+1)*2


            buffer.putShort(0, (short) 1000);
            buffer.putInt(2, 2000000);
            buffer.put(6, (byte) 200);


            buffer.putShort(7, (short) 2000);
            buffer.putInt(9, -2000000);
            buffer.put(13, (byte) 100);


            StructuredBuffer sBuffer = new StructuredBuffer(structure, buffer);


            assertEquals(1000, sBuffer.getU16(0, 0));
            assertEquals(2000000, sBuffer.getS32(1, 0));
            assertEquals(200, sBuffer.getU8(2, 0));


            assertEquals(2000, sBuffer.getU16(0, 1));
            assertEquals(-2000000, sBuffer.getS32(1, 1));
            assertEquals(100, sBuffer.getU8(2, 1));
        }


        // Additional BufferedStations tests


        @Test
        void bufferedStationsWorksWithEmptyBuffer() {
            List<String> stringTable = Collections.emptyList();
            ByteBuffer buffer = ByteBuffer.allocate(0);


            BufferedStations stations = new BufferedStations(stringTable, buffer);
            assertEquals(0, stations.size());
        }


        @Test
        void bufferedStationsWorksWithSingleStation() {
            List<String> stringTable = Arrays.asList("Bern");
            ByteBuffer buffer = ByteBuffer.allocate(10); // 1 * (2 + 4 + 4)


            buffer.putShort(0, (short) 0);
            buffer.putInt(2, (int)(7.45 * Math.pow(2, 32)/360));
            buffer.putInt(6, (int)(46.95 * Math.pow(2, 32)/360));


            BufferedStations stations = new BufferedStations(stringTable, buffer);


            assertEquals(1, stations.size());
            assertEquals("Bern", stations.name(0));
            assertEquals(7.45, stations.longitude(0), 0.0001);
            assertEquals(46.95, stations.latitude(0), 0.0001);
        }


        @Test
        void bufferedStationsWorksWithManyStations() {
            List<String> stringTable = Arrays.asList("Lausanne", "Genève", "Zürich", "Bern", "Basel");
            ByteBuffer buffer = ByteBuffer.allocate(50); // 5 * (2 + 4 + 4)


            // Just populate first and last for brevity
            buffer.putShort(0, (short) 0);
            buffer.putInt(2, (int)(6.63 * Math.pow(2, 32)/360));
            buffer.putInt(6, (int)(46.52 * Math.pow(2, 32)/360));


            buffer.putShort(40, (short) 4);
            buffer.putInt(42, (int)(7.58 * Math.pow(2, 32)/360));
            buffer.putInt(46, (int)(47.56 * Math.pow(2, 32)/360));


            BufferedStations stations = new BufferedStations(stringTable, buffer);


            assertEquals(5, stations.size());
            assertEquals("Lausanne", stations.name(0));
            assertEquals("Basel", stations.name(4));
            assertEquals(7.58, stations.longitude(4), 0.0001);
        }


        @Test
        void bufferedStationsHandlesExtremeCoordinates() {
            List<String> stringTable = Arrays.asList("North Pole", "Equator");
            ByteBuffer buffer = ByteBuffer.allocate(20); // 2 * (2 + 4 + 4)


            buffer.putShort(0, (short) 0);
            buffer.putInt(2, (int)(0.0 * Math.pow(2, 32)/360));
            buffer.putInt(6, (int)(90.0 * Math.pow(2, 32)/360));


            buffer.putShort(10, (short) 1);
            buffer.putInt(12, (int)(0.0 * Math.pow(2, 32)/360));
            buffer.putInt(16, (int)(0.0 * Math.pow(2, 32)/360));


            BufferedStations stations = new BufferedStations(stringTable, buffer);


            assertEquals(90.0, stations.latitude(0), 0.0001);
            assertEquals(0.0, stations.latitude(1), 0.0001);
        }


        @Test
        void bufferedStationsThrowsOnOutOfBoundsAccess() {
            List<String> stringTable = Arrays.asList("Lausanne");
            ByteBuffer buffer = ByteBuffer.allocate(10); // 1 * (2 + 4 + 4)


            BufferedStations stations = new BufferedStations(stringTable, buffer);


            assertThrows(IndexOutOfBoundsException.class, () -> stations.name(1));
        }


        // Additional BufferedStationAliases tests


        @Test
        void bufferedStationAliasesWorksWithEmptyBuffer() {
            List<String> stringTable = Collections.emptyList();
            ByteBuffer buffer = ByteBuffer.allocate(0);


            BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);
            assertEquals(0, aliases.size());
        }


        @Test
        void bufferedStationAliasesWorksWithSingleAlias() {
            List<String> stringTable = Arrays.asList("Lausanne", "Losanna");
            ByteBuffer buffer = ByteBuffer.allocate(4); // 1 * (2 + 2)


            buffer.putShort(0, (short) 1);  // ALIAS_ID: "Losanna"
            buffer.putShort(2, (short) 0);  // STATION_NAME_ID: "Lausanne"


            BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);


            assertEquals(1, aliases.size());
            assertEquals("Losanna", aliases.alias(0));
            assertEquals("Lausanne", aliases.stationName(0));
        }


        @Test
        void bufferedStationAliasesHandlesMultipleAliasesForSameStation() {
            List<String> stringTable = Arrays.asList("Zürich", "Zurich", "Zurigo", "Zurich HB");
            ByteBuffer buffer = ByteBuffer.allocate(12); // 3 * (2 + 2)


            // Three aliases for Zürich
            buffer.putShort(0, (short) 1);  // ALIAS_ID: "Zurich"
            buffer.putShort(2, (short) 0);  // STATION_NAME_ID: "Zürich"


            buffer.putShort(4, (short) 2);  // ALIAS_ID: "Zurigo"
            buffer.putShort(6, (short) 0);  // STATION_NAME_ID: "Zürich"


            buffer.putShort(8, (short) 3);  // ALIAS_ID: "Zurich HB"
            buffer.putShort(10, (short) 0); // STATION_NAME_ID: "Zürich"


            BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);


            assertEquals(3, aliases.size());
            assertEquals("Zurich", aliases.alias(0));
            assertEquals("Zurigo", aliases.alias(1));
            assertEquals("Zurich HB", aliases.alias(2));


            // All point to "Zürich"
            assertEquals("Zürich", aliases.stationName(0));
            assertEquals("Zürich", aliases.stationName(1));
            assertEquals("Zürich", aliases.stationName(2));
        }


        @Test
        void bufferedStationAliasesThrowsOnOutOfBoundsAccess() {
            List<String> stringTable = Arrays.asList("Lausanne", "Losanna");
            ByteBuffer buffer = ByteBuffer.allocate(4); // 1 * (2 + 2)


            BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);


            assertThrows(IndexOutOfBoundsException.class, () -> aliases.alias(1));
        }


        @Test
        void bufferedStationAliasesWorksWithComplexMapping() {
            List<String> stringTable = Arrays.asList(
                    "Genève", "Geneva", "Genf", "Ginevra",
                    "Zürich", "Zurich", "Zurigo"
            );


            ByteBuffer buffer = ByteBuffer.allocate(20); // 5 * (2 + 2)


            // Aliases for Genève
            buffer.putShort(0, (short) 1);  // ALIAS_ID: "Geneva"
            buffer.putShort(2, (short) 0);  // STATION_NAME_ID: "Genève"


            buffer.putShort(4, (short) 2);  // ALIAS_ID: "Genf"
            buffer.putShort(6, (short) 0);  // STATION_NAME_ID: "Genève"


            buffer.putShort(8, (short) 3);  // ALIAS_ID: "Ginevra"
            buffer.putShort(10, (short) 0); // STATION_NAME_ID: "Genève"


            // Aliases for Zürich
            buffer.putShort(12, (short) 5); // ALIAS_ID: "Zurich"
            buffer.putShort(14, (short) 4); // STATION_NAME_ID: "Zürich"


            buffer.putShort(16, (short) 6); // ALIAS_ID: "Zurigo"
            buffer.putShort(18, (short) 4); // STATION_NAME_ID: "Zürich"


            BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);


            assertEquals(5, aliases.size());
            assertEquals("Geneva", aliases.alias(0));
            assertEquals("Genève", aliases.stationName(0));


            assertEquals("Zurigo", aliases.alias(4));
            assertEquals("Zürich", aliases.stationName(4));
        }


        // Additional BufferedPlatforms tests


        @Test
        void bufferedPlatformsWorksWithEmptyBuffer() {
            List<String> stringTable = Collections.emptyList();
            ByteBuffer buffer = ByteBuffer.allocate(0);


            BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);
            assertEquals(0, platforms.size());
        }


        @Test
        void bufferedPlatformsWorksWithSinglePlatform() {
            List<String> stringTable = Arrays.asList("12A", "Lausanne");
            ByteBuffer buffer = ByteBuffer.allocate(4); // 1 * (2 + 2)


            buffer.putShort(0, (short) 0);  // NAME_ID: "12A"
            buffer.putShort(2, (short) 0);  // STATION_ID: 0


            BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);


            assertEquals(1, platforms.size());
            assertEquals("12A", platforms.name(0));
            assertEquals(0, platforms.stationId(0));
        }


        @Test
        void bufferedPlatformsHandlesMultiplePlatformsAtSameStation() {
            List<String> stringTable = Arrays.asList("1", "2", "3", "Bern");
            ByteBuffer buffer = ByteBuffer.allocate(12); // 3 * (2 + 2)


            // Three platforms at the same station
            buffer.putShort(0, (short) 0);  // NAME_ID: "1"
            buffer.putShort(2, (short) 0);  // STATION_ID: 0


            buffer.putShort(4, (short) 1);  // NAME_ID: "2"
            buffer.putShort(6, (short) 0);  // STATION_ID: 0


            buffer.putShort(8, (short) 2);  // NAME_ID: "3"
            buffer.putShort(10, (short) 0); // STATION_ID: 0


            BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);


            assertEquals(3, platforms.size());
            assertEquals("1", platforms.name(0));
            assertEquals("2", platforms.name(1));
            assertEquals("3", platforms.name(2));


            // All at the same station
            assertEquals(0, platforms.stationId(0));
            assertEquals(0, platforms.stationId(1));
            assertEquals(0, platforms.stationId(2));
        }


        @Test
        void bufferedPlatformsThrowsOnOutOfBoundsAccess() {
            List<String> stringTable = Arrays.asList("1", "Lausanne");
            ByteBuffer buffer = ByteBuffer.allocate(4); // 1 * (2 + 2)


            BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);


            assertThrows(IndexOutOfBoundsException.class, () -> platforms.name(1));
        }


        @Test
        void bufferedPlatformsWorksWithDifferentPlatformNameTypes() {
            List<String> stringTable = Arrays.asList("1", "3A", "4B/C", "12S", "Quai 7", "Lausanne", "Genève");
            ByteBuffer buffer = ByteBuffer.allocate(20); // 5 * (2 + 2)


            // Different platform name formats
            buffer.putShort(0, (short) 0);  // NAME_ID: "1" (numeric)
            buffer.putShort(2, (short) 0);  // STATION_ID: 0 (Lausanne)


            buffer.putShort(4, (short) 1);  // NAME_ID: "3A" (alphanumeric)
            buffer.putShort(6, (short) 0);  // STATION_ID: 0 (Lausanne)


            buffer.putShort(8, (short) 2);  // NAME_ID: "4B/C" (complex)
            buffer.putShort(10, (short) 0); // STATION_ID: 0 (Lausanne)


            buffer.putShort(12, (short) 3); // NAME_ID: "12S"
            buffer.putShort(14, (short) 1); // STATION_ID: 1 (Genève)


            buffer.putShort(16, (short) 4); // NAME_ID: "Quai 7" (descriptive)
            buffer.putShort(18, (short) 1); // STATION_ID: 1 (Genève)


            BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);


            assertEquals(5, platforms.size());
            assertEquals("1", platforms.name(0));
            assertEquals("3A", platforms.name(1));
            assertEquals("4B/C", platforms.name(2));
            assertEquals("12S", platforms.name(3));
            assertEquals("Quai 7", platforms.name(4));


            assertEquals(0, platforms.stationId(0));
            assertEquals(0, platforms.stationId(1));
            assertEquals(0, platforms.stationId(2));
            assertEquals(1, platforms.stationId(3));
            assertEquals(1, platforms.stationId(4));
        }
    }
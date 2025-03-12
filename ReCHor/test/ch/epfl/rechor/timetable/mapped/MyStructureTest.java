package ch.epfl.rechor.timetable.mapped;


import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;
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
        Structure.Field field1 = field(0, Structure.FieldType.U8);
        Structure.Field field2 = field(1, Structure.FieldType.U16);
        Structure.Field field3 = field(2, Structure.FieldType.S32);
        Structure structure = new Structure(field1, field2, field3);
        assertEquals(1 + 2 + 4, structure.totalSize());
    }


    @Test
    void testInvalidIndex(){
        Structure.Field field1 = field(0, Structure.FieldType.U8);
        Structure.Field field2 = field(1, Structure.FieldType.U16);
        Structure.Field field3 = field(2, Structure.FieldType.S32);
        Structure structure = new Structure(field1, field2, field3);
        assertThrows(IndexOutOfBoundsException.class, ()-> structure.offset(-1, 3));
        assertThrows(IndexOutOfBoundsException.class, ()-> structure.offset(3, (structure.totalSize()+1)));
        assertThrows(IndexOutOfBoundsException.class, ()-> structure.offset(3, -1));
    }


    @Test
    void testUnorderedFields(){
        Structure.Field field1 = field(0, Structure.FieldType.U8);
        Structure.Field field2 = field(1, Structure.FieldType.U16);
        Structure.Field field3 = field(2, Structure.FieldType.S32);
        assertThrows(IllegalArgumentException.class, ()-> new Structure(field1, field3, field2));
    }


    @Test
    void testValidOffSet() {
        Structure.Field f1 = new Structure.Field(0, Structure.FieldType.U8);
        Structure.Field f2 = new Structure.Field(1, Structure.FieldType.U16);
        Structure structure = new Structure(f1, f2);


        assertEquals(0, structure.offset(0, 0));
        assertEquals(1, structure.offset(1, 0));
    }


    }

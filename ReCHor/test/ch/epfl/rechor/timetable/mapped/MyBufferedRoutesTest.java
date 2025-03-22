package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyBufferedRoutesTest {

    @Test
    void testBufferedRoutesNormal() {

        List<String> stringStationList = new ArrayList<>();
        stringStationList.add("  ");
        stringStationList.add("  ");
        stringStationList.add("M1 Lausanne Flon-Renens VD");
        stringStationList.add("  ");
        stringStationList.add("M2");
        stringStationList.add("RE33");

        byte[] tableauBytes = {0x00, 0x02, 0x01, 0x00, 0x04, 0x01, 0x00, 0x05, 0x02};
        ByteBuffer buffer = ByteBuffer.wrap(tableauBytes);


        BufferedRoutes routes = new BufferedRoutes(stringStationList, buffer);
        assertEquals(routes.name(0), "M1 Lausanne Flon-Renens VD");
        assertEquals(routes.name(1), "M2");
        assertEquals(routes.name(2), "RE33");
        assertEquals(routes.size(), 3);
        assertEquals(routes.vehicle(0), Vehicle.METRO);
        assertEquals(routes.vehicle(1), Vehicle.METRO);
        assertEquals(routes.vehicle(2), Vehicle.TRAIN);
    }

    public static List<String> stringStationList = new ArrayList<String>(Arrays.asList("kdfsk", "efz", "1", "70", "Anet", "Ins", "Lausanne", "Losanna",
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

    public static HexFormat hexFormat = HexFormat.ofDelimiter(" ");
    //762
    public static  byte[] bytes = hexFormat.parseHex("b2 a3 5f 9c 12 d7 8b 44 ee 39 2a 71 c4 06 f8 5d 0e 99 b2 3c a1 47 50 68 1f e3 7d 82 94 ab 01 2A 01 bf 22 48 5a 77 ec d5 1b 63 fa 4c 0d 81 92 ae 3f 6b 28 55 cc 13 f7 09 74 b0 5e d9 8a 41 2d 97 e6 3a 01 7b 52 c8 19 f0 85 64 aa 3d 57 ef 0c 32 4f 98 dc 26 7e 14 b9 43 60 a5 8f 1d e2 75 c0 09 fb 30 56 d4 87 21 9b 5c ae 42 78 0f 69 c7 34 fa 98 27 b1 5d 03 e6 7a 50 cc 12 89 46 bf 3e 72 1c d5 8d 69 04 2a f3 58 96 e0 7b 11 43 bd 27 c9 8f 65 02 3c da 51 78 a3 0e 4d bf 29 97 60 c8 13 f4 82 5b 36 70 ac 01 d9 8f 45 e2 19 67 bd 32 4a 97 05 ce 7f 20 58 b3 6c 81 2d da 47 f9 10 63 c5 34 ab 75 02 e8 3c 51 96 0f d4 8a 29 7e b1 40 63 fc 19 75 2a 89 d7 4e b5 03 6c 58 a9 32 ef 10 c3 8d 57 21 4b a0 6d 3e 95 08 dc 72 51 bf 14 e3 49 60 a5 8f 1d c7 5a 28 9e 43 f1 07 6d 52 b9 30 84 ce 15 79 24 a3 58 9c 07 d6 4e 1f 75 b0 2a 89 34 fc 10 63 c5 5a ab 01 6d 48 9e 37 f1 0c 52 8d 26 bf 75 03 ea 41 5d 98 20 c7 6b 34 af 12 79 d5 48 9e 07 53 b0 2c 6d 41 fa 19 75 c5 3a 87 d2 5b a9 04 60 a1 ab e9 01 14 9b dd a4 9d 2e 9e 1a 3e 09 11 33 3e b8 1d d0 98 7f be a9 d7 71 10 0e bf bf a3 5f 9c 12 d7 8b 44 ee 39 2a 71 c4 06 f8 5d 0e 99 b2 3c a1 01 00 68 1f d7 be af af af b0");
    public static ByteBuffer bytesBuffer = ByteBuffer.wrap(bytes);

    @Test
    void ConfitureOfAbricotTest_CE() {
        BufferedRoutes bufferedRoutes = new BufferedRoutes(stringStationList, bytesBuffer);
        assertEquals(127, bufferedRoutes.size());
        assertEquals(stringStationList.get(298), bufferedRoutes.name(10));
        assertEquals(Vehicle.METRO , bufferedRoutes.vehicle(10));

    }


}

module ReCHor {
    requires javafx.controls;
    requires javafx.graphics;
    requires java.desktop;
    requires java.net.http;
    requires jdk.compiler;

    exports ch.epfl.rechor;
    exports ch.epfl.rechor.timetable;
    exports ch.epfl.rechor.gui;
    exports ch.epfl.rechor.journey;
    exports ch.epfl.rechor.timetable.mapped;
}
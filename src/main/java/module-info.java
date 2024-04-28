module cz.cvut.fel.pjv.hofmaad {
    requires javafx.controls;
    requires java.logging;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires com.fasterxml.jackson.databind;
    requires com.google.common;
    requires com.google.gson;
    exports entry;
    exports model;
    exports controller;
    exports common;
    exports view;
    exports io;
}

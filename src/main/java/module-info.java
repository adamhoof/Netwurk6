module cz.cvut.fel.pjv.hofmaad {
    requires javafx.controls;
    requires java.logging;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires com.fasterxml.jackson.databind;
    exports entry;
    exports model;
    exports controller;
    exports common;
    exports view;
    exports io;
}

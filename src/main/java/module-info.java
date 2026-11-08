module org.example.playermedia {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens org.example.playermedia to javafx.fxml;
    exports org.example.playermedia;
}
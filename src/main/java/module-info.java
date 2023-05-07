module com.example.codetovscodesnippetconverter {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.example.codetovscodesnippetconverter to javafx.fxml;
    exports com.example.codetovscodesnippetconverter;
}
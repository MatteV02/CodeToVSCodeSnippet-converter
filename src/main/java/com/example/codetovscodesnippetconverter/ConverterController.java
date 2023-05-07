package com.example.codetovscodesnippetconverter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.util.Map;

public class ConverterController {

    final Map<Character, String> specialCharacterMap = Map.of(
            '\t', "\\t",
            '\"', "\\\"",
            '\n', "\",\n\""
    );

    @FXML
    private TextArea codeTextArea;

    @FXML
    private TextArea snippetTextArea;

    @FXML
    void onConvertButtonClicked(ActionEvent ignoredEvent) {
        String codeText = codeTextArea.getText();
        StringBuilder snippetText = new StringBuilder();
        snippetText.append("\"");
        for (char c : codeText.toCharArray()) {
            snippetText.append(specialCharacterMap.getOrDefault(c, Character.toString(c)));
        }
        snippetText.append("\",\n");
        snippetTextArea.setText(snippetText.toString());
    }

}

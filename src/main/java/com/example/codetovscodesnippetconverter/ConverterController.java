package com.example.codetovscodesnippetconverter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;

import java.util.*;

public class ConverterController {

    int variableNumber = 0;

    boolean variableInsertion = false;

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

    int getVariableNumber() {
        return ++variableNumber;
    }

    @FXML
    void onAddVariableButtonClicked(ActionEvent ignoredEvent) {
        int caretPosition = snippetTextArea.getCaretPosition();

        snippetTextArea.insertText(caretPosition, "${" + getVariableNumber() + " : text}");

        changeCaretOnNextPosition(caretPosition);

        snippetTextArea.requestFocus();
    }

    @FXML
    void onKeyReleased_snippet(KeyEvent event) {
        switch(event.getCode()) {
            case TAB -> {
                if (variableInsertion) {
                    snippetTextArea.undo();
                    changeCaretOnNextPosition(snippetTextArea.getCaretPosition());
                }
            }
            case ESCAPE -> variableInsertion = false;
        }
    }

    void changeCaretOnNextPosition(int startCaretPosition) {
        String textAreaContent = snippetTextArea.getText();
        Deque<Integer> stopIndex = new ArrayDeque<>(List.of(
                textAreaContent.indexOf(Integer.toString(variableNumber), startCaretPosition),
                textAreaContent.indexOf(":", startCaretPosition),
                textAreaContent.indexOf("text", startCaretPosition),
                textAreaContent.indexOf("}", startCaretPosition)
        ));

        int newCaretPosition;

        try {
            newCaretPosition = stopIndex.removeFirst();

            while (newCaretPosition == -1) {
                newCaretPosition = stopIndex.removeFirst();
            }
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Error while changing caret position");
        }

        variableInsertion = stopIndex.size() != 0;

        snippetTextArea.positionCaret(newCaretPosition);

        if (variableInsertion) {
            snippetTextArea.selectEndOfNextWord();
        } else {
            snippetTextArea.nextWord();
        }
    }
}

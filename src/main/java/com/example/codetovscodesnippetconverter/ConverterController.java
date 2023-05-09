package com.example.codetovscodesnippetconverter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
    private TextField tabSizeTextField;

    @FXML
    void onConvertButtonClicked(ActionEvent ignoredEvent) {
        String codeText = codeTextArea.getText();

        try {
            codeText = spacesToTab(codeText, getTabSize());

            StringBuilder snippetText = new StringBuilder();
            snippetText.append("\"");
            for (char c : codeText.toCharArray()) {
                snippetText.append(specialCharacterMap.getOrDefault(c, Character.toString(c)));
            }
            snippetText.append("\",\n");
            snippetTextArea.setText(snippetText.toString());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Spaces to tab value is not a valid number").showAndWait();
        }
    }

    String spacesToTab(String inputCodeText, int spacesNumber) {
        return inputCodeText.replaceAll(" {" + spacesNumber + "}", "\t");
    }

    /**
     *
     * @return
     * @throws NumberFormatException
     */
    int getTabSize() throws NumberFormatException {
        int returnValue = Integer.parseInt(tabSizeTextField.getText());
        if (returnValue <= 0) {
            throw new NumberFormatException("The number in tabSizeTextField is less or equal to 0");
        }
        return returnValue;
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

    @FXML
    void onRemoveIndentationButtonClicked(ActionEvent ignoredEvent) {
        String codeTextAreaContent = codeTextArea.getText();
        StringBuilder removedIndentationContent = new StringBuilder();

        for (String s : codeTextAreaContent.split("\n")) {
            String regex;
            if (s.startsWith(" ")) {
                regex = " {" + getTabSize() + "}";
            } else if (s.startsWith("\t")) {
                regex = "\t";
            } else {
                continue;
            }
            removedIndentationContent.append(s.replaceFirst(regex, "")).append("\n");
        }

        codeTextArea.setText(removedIndentationContent.toString().trim());
    }
}

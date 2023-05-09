package com.example.codetovscodesnippetconverter;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.util.*;

public class ConverterController {

    int variableNumber = 0;

    SimpleBooleanProperty variableInsertion = new SimpleBooleanProperty();

    final Map<Character, String> specialCharacterMap = Map.of(
            '\t', "\\t",
            '\"', "\\\"",
            '\n', "\",\n\"",
            '\\', "\\\\"
    );

    @FXML
    private TextArea codeTextArea;

    @FXML
    private TextArea snippetTextArea;

    @FXML
    private TextField tabSizeTextField;

    @FXML
    private Label variableModeLabel;

    @FXML
    public void initialize() {
        variableInsertion.set(false);

        variableInsertion.addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                variableModeLabel.setText("ON (ESC to exit)");
            } else {
                variableModeLabel.setText("OFF");
            }
        }));
    }

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

        snippetTextArea.insertText(caretPosition, "${" + getVariableNumber() + ":text}");

        changeCaretOnNextPosition(caretPosition);

        snippetTextArea.requestFocus();
    }

    @FXML
    void onKeyReleased_snippet(KeyEvent event) {
        switch(event.getCode()) {
            case TAB -> {
                if (variableInsertion.get()) {
                    snippetTextArea.undo();
                    changeCaretOnNextPosition(snippetTextArea.getCaretPosition());
                }
            }
            case ESCAPE -> variableInsertion.set(false);
            case BACK_SLASH -> {
                if (event.isShiftDown() && variableInsertion.get()) {
                    // TODO this is unsafe: whenever variableInsertion is true and | is pressed, a | is typed before
                    //  } even if the first | is not in a correct position
                    int startCaretPosition = snippetTextArea.getCaretPosition();

                    String textAreaContent = snippetTextArea.getText();

                    snippetTextArea.insertText(textAreaContent.indexOf("}", startCaretPosition), "|");

                    snippetTextArea.positionCaret(startCaretPosition);
                }
            }
        }
    }

    void changeCaretOnNextPosition(int startCaretPosition) {
        final int INDEX_NOT_FOUND = -1;

        String textAreaContent = snippetTextArea.getText();

        Deque<String> stopStrings = new ArrayDeque<>(List.of(Integer.toString(variableNumber), ":", "text", "}"));
        Deque<Integer> stopIndex = new ArrayDeque<>();

        for (String s : stopStrings) {
            stopIndex.add(textAreaContent.indexOf(s, startCaretPosition));
        }

        int newCaretPosition;
        String selectedWord;

        try {
            do {
                newCaretPosition = stopIndex.removeFirst();
                selectedWord = stopStrings.removeFirst();
            } while (newCaretPosition == INDEX_NOT_FOUND);
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Error while changing caret position");
        }

        variableInsertion.set(stopIndex.size() != 0);

        snippetTextArea.positionCaret(newCaretPosition);

        int finalCaretPosition = newCaretPosition + selectedWord.length();

        if (variableInsertion.get()) {
            snippetTextArea.selectPositionCaret(finalCaretPosition);
        } else {
            snippetTextArea.positionCaret(finalCaretPosition);
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

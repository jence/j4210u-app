package jence.swing.app;

import javax.swing.DefaultCellEditor;
import javax.swing.InputVerifier;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class LimitedCharacterCellEditor extends DefaultCellEditor {
    private static final long serialVersionUID = 1L;

    private int maxCharacters;
    private String allowedCharacters;

    public LimitedCharacterCellEditor(int maxCharacters, String allowedCharacters) {
        super(new JTextField());
        this.maxCharacters = maxCharacters;
        this.allowedCharacters = allowedCharacters;

        JTextField textField = (JTextField) editorComponent;
        textField.setDocument(new LimitedDocument(maxCharacters, allowedCharacters));
        textField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(javax.swing.JComponent input) {
                JTextField textField = (JTextField) input;
                return textField.getText().length() <= maxCharacters;
            }
        });
    }

    class LimitedDocument extends PlainDocument {
        private static final long serialVersionUID = 1L;
        private int maxCharacters;
        private String allowedCharacters;

        public LimitedDocument(int maxCharacters, String allowedCharacters) {
            this.maxCharacters = maxCharacters;
            this.allowedCharacters = allowedCharacters;
        }

        @Override
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str == null) {
                return;
            }

            // Check if the resulting text would be within the character limit
            if (getLength() + str.length() <= maxCharacters) {
                // Check if all characters in 'str' are allowed
                for (char c : str.toCharArray()) {
                    if (allowedCharacters.indexOf(c) == -1) {
                        return; // Disallow insertion if any character is not allowed
                    }
                }
                super.insertString(offs, str.toUpperCase(), a);
            }
        }
    }
}

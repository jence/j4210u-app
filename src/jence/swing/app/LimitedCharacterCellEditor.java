package jence.swing.app;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class LimitedCharacterCellEditor extends AbstractCellEditor implements TableCellEditor {
	private JTextField textField;
	private Object originalValue; // Store original value
	private JTable table;
	private int row, col;

	public LimitedCharacterCellEditor(JTable table, int maxCharacters, String allowedCharacters) {
		this.table = table;
		textField = new JTextField();
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopCellEditing(); // Stop editing when Enter key is pressed
			}
		});
		textField.setDocument(new LimitedDocument(maxCharacters, allowedCharacters));
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		originalValue = value; // Store original value
		textField.setText(value != null ? value.toString() : ""); // Set the cell content when editing starts

		// Ensure the text is selected after the component gains focus
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				textField.requestFocusInWindow();
				textField.selectAll(); // Select all text for quick replacement
			}
		});

		this.row = row;
		this.col = column;
		return textField;
	}

	@Override
	public Object getCellEditorValue() {
		return textField.getText(); // Return edited value
	}

	@Override
	public boolean stopCellEditing() {
		String editedValue = textField.getText();
		if (editedValue.isEmpty()) {
			cancelCellEditing(); // Cancel editing if value is empty
			return true;
		} else if (!editedValue.equals(originalValue)) {
			// Apply editing if value has changed
			setValueAt(editedValue, table.getEditingRow(), table.getEditingColumn());
			UhfAppFrame.editedValue.add(new Tuple<Integer, Integer>(row, col));
		}
		return super.stopCellEditing();
	}

	@Override
	public void cancelCellEditing() {
		// Restore original value when editing is canceled
		setValueAt(originalValue, table.getEditingRow(), table.getEditingColumn());
		super.cancelCellEditing();
	}

	private void setValueAt(Object value, int row, int column) {
		System.out.println("row:"+row+" col"+column );
		table.getModel().setValueAt(value, row, column);
	}
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

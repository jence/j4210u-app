package jence.swing.app;

import javax.swing.text.AttributeSet;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.PlainDocument;

import javax.swing.*;
import javax.swing.text.*;

class JTextFieldLimit extends PlainDocument {
	private int limit;

	JTextFieldLimit(int limit) {
		super();
		this.limit = limit;
	}

	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		if (str == null) {
			str = str.toUpperCase();
			return;
		}

		if ((getLength() + str.length()) <= limit) {
			super.insertString(offset, str, attr);
		}
	}

	public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
		if (text != null) {
			text = text.toUpperCase(); // Convert replacement text to uppercase
		}
		super.replace(offset, length, text, attrs);
	}

}

class LimitedHexText extends PlainDocument {
	private int limit;

	LimitedHexText(int limit) {
		super();
		this.limit = limit;
	}

	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		if (str == null) {
			str = str.toUpperCase();
			return;
		}

		if ((getLength() + str.length()) <= limit) {
			super.insertString(offset, str, attr);
		}
	}

	public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
		if (isHex(text)) {
			super.replace(offset, length, text.toUpperCase(), attrs);
		}
	}

	private boolean isHex(String text) {
		return text.matches("[0-9A-Fa-f]*"); // Matches hexadecimal characters
	}

}
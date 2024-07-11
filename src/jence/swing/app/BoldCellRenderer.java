package jence.swing.app;
//
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.Font;
//import java.awt.Point;
//import java.util.Set;
//
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
//

import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import javax.swing.table.DefaultTableCellRenderer;

public class BoldCellRenderer extends DefaultTableCellRenderer {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Keep track of the row and column of the cell to render bold
    private Set<Point> boldCells = new HashSet<>();

    public void setBoldCell(int row, int column) {
        boldCells.add(new Point(row, column));
    }

    // Override the getTableCellRendererComponent method to customize cell rendering
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Get the default renderer component
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Check if the cell needs to be rendered bold
        if (boldCells.contains(new Point(row, column))) {
            // Set the font to bold
            Font currentFont = c.getFont();
            Font boldFont = new Font(currentFont.getFontName(), Font.BOLD, currentFont.getSize());
            c.setFont(boldFont);
        }

        return c;
    }
}
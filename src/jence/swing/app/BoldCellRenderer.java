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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import javax.swing.table.DefaultTableCellRenderer;

//public class BoldCellRenderer extends DefaultTableCellRenderer {
//
//    /**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//	
//	// Keep track of the row and column of the cell to render bold
//    private Set<Point> boldCells = new HashSet<>();
//
//    public void setBoldCell(int row, int column) {
//        boldCells.add(new Point(row, column));
//    }
//
//    // Override the getTableCellRendererComponent method to customize cell rendering
//    @Override
//    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//        // Get the default renderer component
//        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//
//        // Check if the cell needs to be rendered bold
//        if (boldCells.contains(new Point(row, column))) {
//            // Set the font to bold
//            Font currentFont = c.getFont();
//            Font boldFont = new Font(currentFont.getFontName(), Font.BOLD, currentFont.getSize());
//            c.setFont(boldFont);
//        }
//
//        return c;
//    }
//}

class BoldCellRenderer extends DefaultTableCellRenderer {

	private Set<Point> boldCells = new HashSet<>();

	public void setBoldCell(int row, int column) {
		boldCells.add(new Point(row, column));
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Font boldFont = new Font(table.getFont().getName(), Font.BOLD, table.getFont().getSize());
		
        cell.setFont(table.getFont());
        if (isSelected) {
            cell.setBackground(table.getSelectionBackground());
        } else {
            cell.setBackground(table.getBackground());
        }


		if (boldCells.contains(new Point(row, column))) {
			// Set the font to bold
			Font currentFont = cell.getFont();
			cell.setFont(boldFont);
		}

		
		if(row == 0 ) {
			//epc
			cell.setBackground(Color.decode("#ffff00"));
		}else if (row == 1 ) {
			//tid
			cell.setBackground(Color.decode("#00ff00"));
		} else {
			//memory
			cell.setBackground(Color.decode("#00ffff"));
		}
		
		
		for (Tuple<Integer, Integer> tuple : UhfAppFrame.editedValue) {
			if (tuple.getRow() == row && tuple.getCol() == column) {
	            cell.setFont(cell.getFont().deriveFont(Font.BOLD, 15f));
				cell.setBackground(Color.decode("#b5daf7"));
			}
		}

		return cell;
	}
}

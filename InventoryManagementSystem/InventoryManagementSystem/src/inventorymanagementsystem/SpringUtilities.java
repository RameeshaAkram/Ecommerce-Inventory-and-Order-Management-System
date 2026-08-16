package inventorymanagementsystem;

import javax.swing.*;
import javax.swing.SpringLayout;
import java.awt.*;

public class SpringUtilities {
    public static void makeCompactGrid(Container parent,
                                       int rows, int cols,
                                       int initialX, int initialY,
                                       int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout) parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        Spring xPadSpring  = Spring.constant(xPad);
        Spring yPadSpring  = Spring.constant(yPad);
        Spring initialXSpring = Spring.constant(initialX);
        Spring initialYSpring = Spring.constant(initialY);
        int max = rows * cols;

        // Align all cells in each column and make them the same width
        Spring maxWidthSpring = Spring.constant(0);
        for (int col = 0; col < cols; col++) {
            Spring maxWidth = Spring.constant(0);
            for (int row = 0; row < rows; row++) {
                SpringLayout.Constraints cons = layout.getConstraints(
                        parent.getComponent(row * cols + col));
                maxWidth = Spring.max(maxWidth, cons.getWidth());
            }
            for (int row = 0; row < rows; row++) {
                SpringLayout.Constraints cons = layout.getConstraints(
                        parent.getComponent(row * cols + col));
                cons.setWidth(maxWidth);
            }
        }

        // Align all cells in each row and make them the same height
        for (int row = 0; row < rows; row++) {
            Spring maxHeight = Spring.constant(0);
            for (int col = 0; col < cols; col++) {
                SpringLayout.Constraints cons = layout.getConstraints(
                        parent.getComponent(row * cols + col));
                maxHeight = Spring.max(maxHeight, cons.getHeight());
            }
            for (int col = 0; col < cols; col++) {
                SpringLayout.Constraints cons = layout.getConstraints(
                        parent.getComponent(row * cols + col));
                cons.setHeight(maxHeight);
            }
        }

        // Set x and y for all cells
        Spring y = initialYSpring;
        for (int row = 0; row < rows; row++) {
            Spring x = initialXSpring;
            for (int col = 0; col < cols; col++) {
                SpringLayout.Constraints cons = layout.getConstraints(
                        parent.getComponent(row * cols + col));
                cons.setX(x);
                cons.setY(y);
                x = Spring.sum(x, Spring.sum(cons.getWidth(), xPadSpring));
            }
            y = Spring.sum(y, Spring.sum(layout.getConstraints(
                    parent.getComponent(row * cols)).getHeight(), yPadSpring));
        }

        // Set parent's size
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST,
                Spring.sum(initialXSpring, Spring.sum(xPadSpring,
                        layout.getConstraints(parent.getComponent(max - 1)).getX())));
    }
}

package io.lolyay.addon.ui;

import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;

public class WGrid extends WContainer {
    public double horizontalSpacing = 3;
    public double verticalSpacing = 3;

    /**
     * Common layout logic used for both sizing and positioning.
     *
     * @param position If true, actually updates the cell positions.
     * @return An array containing { totalWidth, totalHeight }
     */
    private double[] layout(boolean position) {
        // Use the current width, but fallback to minWidth if width hasn't been calculated yet (is 0).
        // This prevents the grid from collapsing into a single vertical column on the first frame.
        double availableWidth = this.width > 0 ? this.width : theme.scale(minWidth);

        double currentX = 0;
        double currentY = 0;

        double maxRowHeight = 0;
        double maxRowWidth = 0;

        double spacingH = theme.scale(horizontalSpacing);
        double spacingV = theme.scale(verticalSpacing);

        // Loop through all cells in the container
        for (int i = 0; i < cells.size(); i++) {
            Cell<?> cell = cells.get(i);

            // Calculate total dimensions of this cell (widget + padding)
            double cellWidth = cell.padLeft() + cell.widget().width + cell.padRight();
            double cellHeight = cell.padTop() + cell.widget().height + cell.padBottom();

            // Check if we need to wrap to the next line
            // We wrap if: we are not the first item AND adding this item exceeds the width
            if (i > 0 && currentX + cellWidth > availableWidth) {
                currentX = 0;
                currentY += maxRowHeight + spacingV;
                maxRowHeight = 0;
            }

            // Apply positions if requested
            if (position) {
                cell.x = this.x + currentX + cell.padLeft();
                cell.y = this.y + currentY + cell.padTop();

                // We don't change cell.width/height here like WTable does
                // because WGrid respects the widget's preferred size usually.
                cell.width = cell.widget().width;
                cell.height = cell.widget().height;

                cell.alignWidget();
            }

            // Update row tracking
            maxRowHeight = Math.max(maxRowHeight, cellHeight);

            // Advance X
            currentX += cellWidth + spacingH;

            // Update total width tracking (handles the case where rows are shorter than max width)
            // Note: currentX includes a trailing spacingH, so we subtract it for the width calc
            maxRowWidth = Math.max(maxRowWidth, currentX - spacingH);
        }

        // Total height is the Y position of the last row + the height of that row
        double totalHeight = currentY + maxRowHeight;

        return new double[]{maxRowWidth, totalHeight};
    }

    @Override
    protected void onCalculateSize() {
        double[] size = layout(false);

        // If the grid isn't constrained by a parent width yet,
        // we can set our preferred width to the widest row.
        if (width == 0) width = size[0];

        height = size[1];
    }

    @Override
    protected void onCalculateWidgetPositions() {
        layout(true);
    }
}

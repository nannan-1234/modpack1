package com.reiryoku_expansion.item;

import appeng.api.storage.cells.ICellWorkbenchItem;

/**
 * A cell item that stores Reiryoku. Cells are not partitionable (like Applied
 * Botanics' mana cells), so all workbench methods return defaults.
 */
public interface IReiryokuCellItem extends ICellWorkbenchItem {

    long getTotalBytes();

    double getIdleDrain();
}

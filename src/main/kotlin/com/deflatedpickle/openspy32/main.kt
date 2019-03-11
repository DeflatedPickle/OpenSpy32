package com.deflatedpickle.openspy32

import org.eclipse.jface.window.ApplicationWindow
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.*

fun main(args: Array<String>) {
    val window = object : ApplicationWindow(null) {
        override fun createContents(parent: Composite?): Control {
            val table = Table(parent, SWT.FULL_SELECTION or SWT.VIRTUAL or SWT.H_SCROLL or SWT.V_SCROLL)
            table.headerVisible = true

            val titles = listOf("Name", "Process ID", "Handle", "Window Class")

            for (t in titles) {
                val column = TableColumn(table, SWT.NULL)
                column.text = t
                column.pack()
            }

            table.itemCount = 1
            table.addListener(SWT.SetData) {
                // Test item
                val item = it.item as TableItem
                item.text = "Test"
            }

            table.pack()

            return super.createContents(parent)
        }
    }
    window.setBlockOnOpen(true)

    window.open()
}
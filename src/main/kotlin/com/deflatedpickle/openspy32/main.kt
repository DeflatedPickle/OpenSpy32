package com.deflatedpickle.openspy32

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import org.eclipse.jface.window.ApplicationWindow
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.widgets.*

fun main(args: Array<String>) {
    val window = object : ApplicationWindow(null) {
        override fun create() {
            super.create()

            shell.text = "OpenSpy32"
            shell.setMinimumSize(400, 400)
        }

        override fun createContents(parent: Composite?): Control {
            val tree = Tree(parent, SWT.FULL_SELECTION or SWT.VIRTUAL or SWT.H_SCROLL or SWT.V_SCROLL or SWT.BORDER)
            tree.headerVisible = true

            val titles = listOf("Name", "Window Class", "Process ID", "Handle", "Thread ID")

            for (t in titles) {
                val column = TreeColumn(tree, SWT.NULL)
                column.text = t

                column.pack()
                column.width = 100
            }

            val monitorList = WinUtil.getAllDisplays()
            tree.itemCount = monitorList.size
            tree.data = monitorList

            val monitorInfo = WinUser.MONITORINFOEX()
            // val windowInfo = WinUser.WINDOWINFO()
            tree.addListener(SWT.SetData) {
                val item = it.item as TreeItem

                // Top-level monitor
                if (item.parentItem == null) {
                    val monitor = (tree.data as List<WinUser.HMONITOR>)[it.index]
                    User32.INSTANCE.GetMonitorInfo(monitor, monitorInfo)
                    // TODO: Get the proper name of the monitor
                    item.setText(
                        arrayOf(
                            monitorInfo.szDevice.joinToString(""),
                            "",
                            "",
                            WinUtil.handleToHex(monitor.pointer),
                            ""
                        )
                    )

                    val windowList = WinUtil.getMonitorWindows(monitor)
                    item.itemCount = windowList.size
                    item.data = windowList
                } else {
                    val window = (item.parentItem.data as List<WinDef.HWND>)[it.index]
                    // User32.INSTANCE.GetWindowInfo(window, windowInfo)
                    val process = IntByReference(0)
                    val thread = User32.INSTANCE.GetWindowThreadProcessId(window, process)

                    // TODO: Set the icon of the item to the windows icon
                    item.setText(
                        arrayOf(
                            WinUtil.getTitle(window),
                            WinUtil.getClass(window),
                            process.value.toString(),
                            window.toString().split("@")[1],
                            thread.toString()
                        )
                    )

                    val windowList = WinUtil.getChildWindows(window)
                    item.itemCount = windowList.size
                    item.data = windowList
                }
            }

            tree.addListener(SWT.MouseDoubleClick) {
                val point = Point(it.x, it.y)
                val item = tree.getItem(point)

                if (item != null) {
                    val propertiies = PropertiesDialog(shell)
                    propertiies.hwnd = WinDef.HWND(WinUtil.hexToHandle(item.getText(3)))
                    propertiies.open()
                }
            }

            tree.pack()

            return super.createContents(parent)
        }
    }
    window.setBlockOnOpen(true)

    window.open()
}
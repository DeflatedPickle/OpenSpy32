package com.deflatedpickle.openspy32

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import org.eclipse.jface.window.ApplicationWindow
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.graphics.Image
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

            val titles = listOf(Pair("Name", 200), Pair("Window Class", 140), Pair("Process ID", 70), Pair("Handle", 60), Pair("Thread ID", 60))

            for (t in titles) {
                val column = TreeColumn(tree, SWT.NULL)
                column.text = t.first

                column.pack()
                column.width = t.second
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

                    item.setText(
                        arrayOf(
                            WinUtil.getTitle(window),
                            WinUtil.getClass(window),
                            process.value.toString(),
                            window.toString().split("@")[1],
                            thread.toString()
                        )
                    )

                    val icon = Image.win32_new(shell.display, SWT.ICON, User32.INSTANCE.GetClassLong(window, User32.GCL_HICON).toLong())
                    if (!icon.isDisposed) {
                        val boundWidth = icon.imageData.width
                        val boundHeight = icon.imageData.height

                        val zoom = 1.0 / 1.6
                        val finalIcon = Image(shell.display, icon.imageData.scaledTo((boundWidth * zoom).toInt(),
                            (boundHeight * zoom).toInt()
                        ))

                        if (!finalIcon.isDisposed) {
                            item.image = finalIcon
                        }
                    }

                    val windowList = WinUtil.getChildWindows(window)
                    item.itemCount = windowList.size
                    item.data = windowList
                }
            }

            tree.addListener(SWT.MouseDoubleClick) {
                if (it.button == 1) {
                    val point = Point(it.x, it.y)
                    val item = tree.getItem(point)

                    if (item != null) {
                        val propertiies = PropertiesDialog(shell)
                        propertiies.hwnd = WinDef.HWND(WinUtil.hexToHandle(item.getText(3)))
                        propertiies.open()
                    }
                }
            }

            var selectedItem: TreeItem? = null

            val contextMenu = Menu(tree)
            tree.menu = contextMenu

            MenuItem(contextMenu, SWT.NONE).apply {
                text = "Properties"

                this.addSelectionListener(object : SelectionAdapter() {
                    override fun widgetDefaultSelected(e: SelectionEvent) {
                        val propertiies = PropertiesDialog(shell)
                        propertiies.hwnd = WinDef.HWND(WinUtil.hexToHandle(selectedItem!!.getText(3)))
                        propertiies.open()
                    }

                    override fun widgetSelected(e: SelectionEvent) {
                        widgetDefaultSelected(e)
                    }
                })
            }
            // MenuItem(contextMenu, SWT.NONE).apply { text = "Message Log" }
            // MenuItem(contextMenu, SWT.NONE).apply { text = "Highlight" }
            // MenuItem(contextMenu, SWT.SEPARATOR)
            // MenuItem(contextMenu, SWT.NONE).apply { text = "Refresh" }

            tree.addListener(SWT.MenuDetect) {
                val point = tree.toControl(Point(it.x, it.y))
                val item = tree.getItem(point)

                if (item == null) {
                    it.doit = false
                }
                else {
                    selectedItem = item
                }
            }

            tree.pack()

            return super.createContents(parent)
        }
    }
    window.setBlockOnOpen(true)

    window.open()
}
package com.deflatedpickle.openspy32

import com.deflatedpickle.jna.User32Extended
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import org.eclipse.jface.dialogs.Dialog
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import org.eclipse.swt.widgets.List

class PropertiesDialog(shell: Shell) : Dialog(shell) {
    var hwnd: WinDef.HWND? = null

    override fun createDialogArea(parent: Composite?): Control {
        val container = super.createDialogArea(parent) as Composite

        val tabFolder = TabFolder(container, SWT.TOP)
        tabFolder.layoutData = GridData(SWT.FILL, SWT.FILL, true, true)

        fun addItem(text: String, columns: Int): TabItem {
            return TabItem(tabFolder, SWT.NULL).apply {
                this.text = text
                control = ScrolledComposite(tabFolder, SWT.H_SCROLL or SWT.V_SCROLL).apply {
                    expandHorizontal = true
                    expandVertical = true
                }

                (control as ScrolledComposite).content = Composite(control as ScrolledComposite, SWT.NONE).apply {
                    layout = GridLayout().apply {
                        numColumns = columns
                    }
                }
            }
        }

        fun addLabelEntry(item: Composite, text: String): Pair<Label, Text> {
            val label = Label(item, SWT.RIGHT).apply {
                this.text = "$text:"
                layoutData = GridData(SWT.FILL, SWT.CENTER, true, false)
            }
            val entry = Text(item, SWT.BORDER or SWT.READ_ONLY).apply {
                layoutData = GridData(SWT.FILL, SWT.CENTER, true, false)
            }

            return Pair(label, entry)
        }

        addItem("General", 1).apply {
            val propertiesGroup = Group((this.control as ScrolledComposite).content as Composite, SWT.NONE).apply {
                text = "Properties"
                layout = GridLayout().apply {
                    numColumns = 2
                }
                layoutData = GridData(SWT.FILL, SWT.FILL, true, true)
            }

            addLabelEntry(propertiesGroup, "Window Caption").apply {
                this.second.text = WinUtil.getTitle(hwnd!!)
            }
            addLabelEntry(propertiesGroup, "Window Handle").apply {
                this.second.text = WinUtil.handleToHex(hwnd!!.pointer)
            }
            // addLabelEntry(this, "Window Proc")
            // addLabelEntry(this, "Instance Handle")

            addLabelEntry(propertiesGroup, "Menu Handle").apply {
                var pointer = "null"

                if (User32Extended.INSTANCE.GetMenu(hwnd) != null) {
                    pointer = WinUtil.handleToHex(User32Extended.INSTANCE.GetMenu(hwnd).pointer)
                }

                this.second.text = pointer
            }

            // TODO: Find where these are supposed to come from
            // addLabelEntry(this, "User Data")
            // addLabelEntry(this, "Window Bytes")

            val sizeAndPositionGroup = Group((this.control as ScrolledComposite).content as Composite, SWT.NONE).apply {
                text = "Size and Position"
                layout = GridLayout().apply {
                    numColumns = 2
                }
                layoutData = GridData(SWT.FILL, SWT.FILL, true, true)
            }

            // TODO: Make a widget for RECT values, displaying them better
            addLabelEntry(sizeAndPositionGroup, "Window Rectangle").apply {
                this.second.text = WinUtil.getWindowRect(hwnd!!).toString()
            }
            addLabelEntry(sizeAndPositionGroup, "Restored Rectangle").apply {
                this.second.text = WinUtil.getWindowPlacement(hwnd!!).rcNormalPosition.toString()
            }
            addLabelEntry(sizeAndPositionGroup, "Client Rectangle").apply {
                this.second.text = WinUtil.getClientRect(hwnd!!).toString()
            }
            addLabelEntry(sizeAndPositionGroup, "Window State").apply {
                this.second.text = when (WinUtil.getWindowPlacement(hwnd!!).showCmd) {
                    1 -> { "Normal" }
                    2 -> { "Minimized" }
                    3 -> { "Maximized" }
                    8 -> { "N/A" }
                    else -> { "" }
                }
            }
            addLabelEntry(sizeAndPositionGroup, "Z Order").apply {
                this.second.text = WinUtil.zOrder.indexOf(hwnd).toString()
            }

            (this.control as ScrolledComposite).setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT))
        }

        addItem("Styles", 4).apply {
            val content = (this.control as ScrolledComposite).content as Composite

            val normalStyles = User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_STYLE)
            val extendedStyles = User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_EXSTYLE)

            Label(content, SWT.NONE).apply {
                text = "Window Styles:"
            }
            Text(content, SWT.BORDER or SWT.READ_ONLY).apply {
                text = normalStyles.toString()
                layoutData = GridData(SWT.FILL, SWT.CENTER, true, false)
            }

            Label(content, SWT.NONE).apply {
                text = "Extended Styles:"
            }
            Text(content, SWT.BORDER or SWT.READ_ONLY).apply {
                text = extendedStyles.toString()
                layoutData = GridData(SWT.FILL, SWT.CENTER, true, false)
            }

            List(content, SWT.BORDER or SWT.H_SCROLL or SWT.V_SCROLL).apply {
                layoutData = GridData(SWT.FILL, SWT.FILL, true, true).apply {
                    horizontalSpan = 2
                }

                val styles = WinUtil.getWindowStyles(hwnd!!)

                for (s in styles) {
                    this.add(s)
                }
            }

            List(content, SWT.BORDER or SWT.H_SCROLL or SWT.V_SCROLL).apply {
                layoutData = GridData(SWT.FILL, SWT.FILL, true, true).apply {
                    horizontalSpan = 2
                }

                val styles = WinUtil.getExtendedStyles(hwnd!!)

                for (s in styles) {
                    this.add(s)
                }
            }
        }
        // addItem("Windows")
        // addItem("Class")
        // addItem("Process")

        return container
    }

    override fun configureShell(newShell: Shell) {
        super.configureShell(newShell)
        newShell.text = "Property Inspector"
        newShell.minimumSize = Point(320, 300)
    }

    override fun isResizable(): Boolean {
        return true
    }

    override fun getInitialSize(): Point {
        return Point(420, 400)
    }
}
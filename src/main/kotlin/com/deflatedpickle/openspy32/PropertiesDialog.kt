package com.deflatedpickle.openspy32

import com.deflatedpickle.jna.User32Extended
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.ptr.IntByReference
import org.eclipse.jface.dialogs.Dialog
import org.eclipse.jface.window.Window
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import org.eclipse.swt.widgets.List

class PropertiesDialog(shell: Shell) : Dialog(shell) {
    var hwnd: WinDef.HWND? = null

    init {
        shellStyle = (SWT.DIALOG_TRIM or SWT.APPLICATION_MODAL or SWT.RESIZE or Window.getDefaultOrientation())
    }

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

        fun addLabelEntry(item: Composite, text: String, labelWidth: Int = 120, labelSpan: Int = 1, textSpan: Int = 1): Text {
            Label(item, SWT.RIGHT).apply {
                this.text = "$text:"
                layoutData = GridData(SWT.RIGHT, SWT.CENTER, false, false).apply {
                    horizontalSpan = labelSpan
                }
            }
            return Text(item, SWT.BORDER or SWT.READ_ONLY).apply {
                layoutData = GridData(SWT.FILL, SWT.CENTER, true, false).apply {
                    horizontalSpan = textSpan
                }
            }
        }

        fun addLabelRect(item: Composite, text: String, rect: WinDef.RECT): kotlin.collections.List<Text> {
            var leftWidget: Text?
            var topWidget: Text?
            var widthWidget: Text?
            var heightWidget: Text?
            var resolutionWidget: Text?

            Label(item, SWT.RIGHT).apply {
                this.text = "$text:"
                layoutData = GridData(SWT.RIGHT, SWT.CENTER, false, false)
            }
            Composite(item, SWT.NONE).apply {
                layout = GridLayout().apply {
                    numColumns = 4
                }
                layoutData = GridData(SWT.FILL, SWT.CENTER, true, false)

                val width = rect.right - rect.left
                val height = rect.bottom - rect.top

                val labelWidth = 40
                leftWidget = addLabelEntry(this, "Left", labelWidth).apply { setText(rect.left.toString()) }
                topWidget = addLabelEntry(this, "Top", labelWidth).apply { setText(rect.top.toString()) }
                widthWidget = addLabelEntry(this, "Width", labelWidth).apply { setText(rect.right.toString()) }
                heightWidget = addLabelEntry(this, "Height", labelWidth).apply { setText(rect.bottom.toString()) }
                resolutionWidget = addLabelEntry(this, "Resolution", 60, 1, 3).apply { setText("${width}x$height") }
            }
            Label(item, SWT.SEPARATOR or SWT.HORIZONTAL).apply {
                layoutData = GridData(GridData.FILL_HORIZONTAL).apply {
                    horizontalSpan = 2
                }
            }

            return listOf(leftWidget!!, topWidget!!, widthWidget!!, heightWidget!!, resolutionWidget!!)
        }

        addItem("General", 1).apply {
            val propertiesGroup = Group((this.control as ScrolledComposite).content as Composite, SWT.NONE).apply {
                text = "Properties"
                layout = GridLayout().apply {
                    numColumns = 2
                }
                layoutData = GridData(GridData.FILL_BOTH)
            }

            addLabelEntry(propertiesGroup, "Window Caption").apply {
                this.text = WinUtil.getTitle(hwnd!!)
            }
            addLabelEntry(propertiesGroup, "Window Handle").apply {
                this.text = WinUtil.handleToHex(hwnd!!.pointer)
            }
            // addLabelEntry(this, "Window Proc")
            // addLabelEntry(this, "Instance Handle")

            addLabelEntry(propertiesGroup, "Menu Handle").apply {
                var pointer = "null"

                if (User32Extended.INSTANCE.GetMenu(hwnd) != null) {
                    pointer = WinUtil.handleToHex(User32Extended.INSTANCE.GetMenu(hwnd).pointer)
                }

                this.text = pointer
            }

            // TODO: Find where these are supposed to come from
            // addLabelEntry(this, "User Data")
            // addLabelEntry(this, "Window Bytes")

            val sizeAndPositionGroup = Group((this.control as ScrolledComposite).content as Composite, SWT.NONE).apply {
                text = "Size and Position"
                layout = GridLayout().apply {
                    numColumns = 2
                }
                layoutData = GridData(GridData.FILL_BOTH)
            }

            addLabelRect(sizeAndPositionGroup, "Window Rectangle", WinUtil.getWindowRect(hwnd!!))
            addLabelRect(sizeAndPositionGroup, "Restored Rectangle", WinUtil.getWindowPlacement(hwnd!!).rcNormalPosition)
            addLabelRect(sizeAndPositionGroup, "Client Rectangle", WinUtil.getClientRect(hwnd!!))

            addLabelEntry(sizeAndPositionGroup, "Window State").apply {
                this.text = when (WinUtil.getWindowPlacement(hwnd!!).showCmd) {
                    User32.SW_NORMAL -> { "Normal" }
                    User32.SW_SHOWMINIMIZED -> { "Minimized" }
                    User32.SW_SHOWMAXIMIZED -> { "Maximized" }
                    else -> { "" }
                }
            }
            addLabelEntry(sizeAndPositionGroup, "Z Order").apply {
                this.text = WinUtil.zOrder.indexOf(hwnd!!).toString()
            }

            (this.control as ScrolledComposite).setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT))
        }

        addItem("Styles", 5).apply {
            val content = (this.control as ScrolledComposite).content as Composite

            val normalStyles = User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_STYLE)
            val extendedStyles = User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_EXSTYLE)

            addLabelEntry((this.control as ScrolledComposite).content as Composite, "Window Styles").apply {
                this.text = normalStyles.toString()
            }

            addLabelEntry((this.control as ScrolledComposite).content as Composite, "Extended Styles").apply {
                this.text = extendedStyles.toString()
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
        // addItem("Class")
        addItem("Process", 2).apply {
            val process = IntByReference(0)
            val thread = User32.INSTANCE.GetWindowThreadProcessId(hwnd!!, process)

            addLabelEntry((this.control as ScrolledComposite).content as Composite, "Process ID").apply {
                this.text = process.value.toString()
            }
            addLabelEntry((this.control as ScrolledComposite).content as Composite, "Thread ID").apply {
                this.text = thread.toString()
            }
        }

        return container
    }

    override fun configureShell(newShell: Shell) {
        super.configureShell(newShell)
        newShell.text = "Property Inspector"
        newShell.minimumSize = Point(320, 300)
    }

    override fun getInitialSize(): Point {
        return Point(440, 400)
    }
}
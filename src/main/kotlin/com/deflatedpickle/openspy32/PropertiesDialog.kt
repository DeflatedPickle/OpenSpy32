package com.deflatedpickle.openspy32

import com.deflatedpickle.jna.User32Extended
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import org.eclipse.jface.dialogs.Dialog
import org.eclipse.jface.window.Window
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.internal.win32.OS
import org.eclipse.swt.internal.win32.TCHAR
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import org.eclipse.swt.widgets.List

class PropertiesDialog(shell: Shell) : Dialog(shell) {
    var hwnd: WinDef.HWND? = null

    init {
        shellStyle = (SWT.DIALOG_TRIM or SWT.APPLICATION_MODAL or SWT.RESIZE or Window.getDefaultOrientation())
    }

    inline fun <reified T : Control>addProperty(item: Composite, text: String, labelWidth: Int = 120, labelSpan: Int = 1, textSpan: Int = 1): T {
        Label(item, SWT.RIGHT).apply {
            this.text = "$text:"
            layoutData = GridData(SWT.RIGHT, SWT.CENTER, false, false).apply {
                horizontalSpan = labelSpan
            }
        }

        val second = when (T::class.java.typeName) {
            "org.eclipse.swt.widgets.Text" -> {
                Text(item, SWT.BORDER)
            }
            "org.eclipse.swt.widgets.Label" -> {
                Label(item, SWT.NONE)
            }
            "org.eclipse.swt.widgets.Combo" -> {
                Combo(item, SWT.BORDER or SWT.READ_ONLY)
            }
            "org.eclipse.swt.widgets.Spinner" -> {
                Spinner(item, SWT.BORDER)
            }
            else -> {
                Label(item, SWT.NONE)
            }
        }

        if (!WinUtil.isAdmin) {
            if (second !is Label) {
                second.enabled = false
            }
        }

        second.apply {
            layoutData = GridData(SWT.FILL, SWT.CENTER, true, false).apply {
                horizontalSpan = textSpan
            }
        }
        return second as T
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

        fun addLabelRect(item: Composite, text: String, rect: WinDef.RECT): Pair<kotlin.collections.List<Control>, WinDef.RECT> {
            var leftWidget: Spinner? = null
            var topWidget: Spinner? = null
            var widthWidget: Spinner? = null
            var heightWidget: Spinner? = null
            var resolutionWidget: Label? = null

            val localRect = rect

            var width = 0
            var height = 0

            fun updateResolution() {
                width = localRect.right - localRect.left
                height = localRect.bottom - localRect.top
            }

            updateResolution()

            Label(item, SWT.RIGHT).apply {
                this.text = "$text:"
                layoutData = GridData(SWT.RIGHT, SWT.CENTER, false, false)
            }
            Composite(item, SWT.NONE).apply {
                layout = GridLayout().apply {
                    numColumns = 4
                    makeColumnsEqualWidth = true
                }
                layoutData = GridData(SWT.FILL, SWT.CENTER, true, false)

                val monitor = User32.INSTANCE.MonitorFromWindow(hwnd, User32.MONITOR_DEFAULTTONEAREST)

                val monitorInfo = WinUser.MONITORINFO()
                User32.INSTANCE.GetMonitorInfo(monitor, monitorInfo)

                val increment = 6

                val labelWidth = 40
                leftWidget = addProperty<Spinner>(this, "Left", labelWidth).apply {
                    setValues(rect.left, monitorInfo.rcWork.left, monitorInfo.rcWork.right, 0, increment, 1)

                    this.addListener(SWT.KeyUp) {
                        if (WinUtil.isAdmin) {
                            localRect.left = this.text.toInt()

                            updateResolution()
                        }
                    }
                }
                topWidget = addProperty<Spinner>(this, "Top", labelWidth).apply {
                    setValues(rect.top, monitorInfo.rcWork.top, monitorInfo.rcWork.bottom, 0, increment, 1)

                    this.addListener(SWT.KeyUp) {
                        if (WinUtil.isAdmin) {
                            localRect.top = this.text.toInt()

                            updateResolution()
                        }
                    }
                }
                widthWidget = addProperty<Spinner>(this, "Width", labelWidth).apply {
                    setValues(rect.right - rect.left, monitorInfo.rcWork.left, monitorInfo.rcWork.right - monitorInfo.rcWork.left, 0, increment, 1)

                    this.addListener(SWT.KeyUp) {
                        if (WinUtil.isAdmin) {
                            localRect.right = this.text.toInt()

                            updateResolution()
                        }
                    }
                }
                heightWidget = addProperty<Spinner>(this, "Height", labelWidth).apply {
                    setValues(rect.bottom - rect.top, monitorInfo.rcWork.top, monitorInfo.rcWork.bottom - monitorInfo.rcWork.top, 0, increment, 1)

                    this.addListener(SWT.KeyUp) {
                        if (WinUtil.isAdmin) {
                            localRect.bottom = this.text.toInt()

                            updateResolution()
                        }
                    }
                }
                resolutionWidget = addProperty<Label>(this, "Resolution", 60, 1, 3).apply { setText("${width}x$height") }
            }
            Label(item, SWT.SEPARATOR or SWT.HORIZONTAL).apply {
                layoutData = GridData(GridData.FILL_HORIZONTAL).apply {
                    horizontalSpan = 2
                }
            }

            return Pair(listOf(leftWidget!!, topWidget!!, widthWidget!!, heightWidget!!, resolutionWidget!!), localRect)
        }

        addItem("General", 1).apply {
            val propertiesGroup = Group((this.control as ScrolledComposite).content as Composite, SWT.NONE).apply {
                text = "Properties"
                layout = GridLayout().apply {
                    numColumns = 2
                }
                layoutData = GridData(GridData.FILL_BOTH)
            }

            addProperty<Text>(propertiesGroup, "Window Caption").apply {
                this.text = WinUtil.getTitle(hwnd!!)

                this.addListener(SWT.KeyUp) {
                    if (WinUtil.isAdmin) {
                        User32Extended.INSTANCE.SetWindowText(hwnd, this.text)
                    }
                }
            }
            // TODO: Maybe allow this to be changed, or at least test it
            addProperty<Label>(propertiesGroup, "Window Handle").apply {
                this.text = WinUtil.handleToHex(hwnd!!.pointer)
            }
            addProperty<Label>(propertiesGroup, "Window Proc").apply {
                this.text = User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_WNDPROC).toString()
            }
            addProperty<Label>(propertiesGroup, "Instance Handle").apply {
                // FIXME: Seems to always be 0, probably grabbing it wrong
                this.text = User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_HINSTANCE).toString()
            }
            addProperty<Text>(propertiesGroup, "Menu Handle").apply {
                message = "0"
                var pointer = ""

                if (User32Extended.INSTANCE.GetMenu(hwnd) != null) {
                    pointer = WinUtil.handleToHex(User32Extended.INSTANCE.GetMenu(hwnd).pointer)
                }

                this.text = pointer

                this.addListener(SWT.KeyUp) {
                    if (WinUtil.isAdmin) {
                        if (!this.text.isEmpty()) {
                            User32Extended.INSTANCE.SetMenu(hwnd, WinDef.HMENU(WinUtil.hexToHandle(this.text)))
                        }
                        else {
                            User32Extended.INSTANCE.SetMenu(hwnd, WinDef.HMENU(WinUtil.hexToHandle(this.message)))
                        }
                    }
                }
            }
            addProperty<Label>(propertiesGroup, "User Data").apply {
                this.text = User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_USERDATA).toString()
            }
            // addProperty(this, "Window Bytes")

            val sizeAndPositionGroup = Group((this.control as ScrolledComposite).content as Composite, SWT.NONE).apply {
                text = "Size and Position"
                layout = GridLayout().apply {
                    numColumns = 2
                }
                layoutData = GridData(GridData.FILL_BOTH)
            }

            addLabelRect(sizeAndPositionGroup, "Window Rectangle", WinUtil.getWindowRect(hwnd!!)).apply {
                val arguments = mutableListOf("", "", "", "")

                var argNum = 0
                for (c in this.first.dropLast(1)) {
                    c.data = argNum
                    arguments[c.data as Int] = (c as Spinner).text
                    c.addListener(SWT.Selection) {
                        arguments[c.data as Int] = c.text

                        User32.INSTANCE.SetWindowPos(hwnd, User32Extended.HWND_NOTOPMOST, arguments[0].toInt(), arguments[1].toInt(), arguments[2].toInt(), arguments[3].toInt(), User32.SWP_ASYNCWINDOWPOS)
                    }

                    argNum++
                }
            }
            addLabelRect(sizeAndPositionGroup, "Restored Rectangle", WinUtil.getWindowPlacement(hwnd!!).rcNormalPosition).apply {
                for (c in this.first.dropLast(1)) {
                    c.enabled = false
                }
            }
            addLabelRect(sizeAndPositionGroup, "Client Rectangle", WinUtil.getClientRect(hwnd!!)).apply {
                for (c in this.first.dropLast(1)) {
                    c.enabled = false
                }
            }

            addProperty<Combo>(sizeAndPositionGroup, "Window State").apply {
                add("SW_NORMAL")
                add("SW_SHOWMINIMIZED")
                add("SW_SHOWMAXIMIZED")

                this.text = when (WinUtil.getWindowPlacement(hwnd!!).showCmd) {
                    User32.SW_NORMAL -> { "SW_NORMAL" }
                    User32.SW_SHOWMINIMIZED -> { "SW_SHOWMINIMIZED" }
                    User32.SW_SHOWMAXIMIZED -> { "SW_SHOWMAXIMIZED" }
                    else -> { "" }
                }

                this.addListener(SWT.Selection) {
                    val placement = WinUtil.getWindowPlacement(hwnd!!)
                    when ((it.widget as Combo).selectionIndex) {
                        0 -> {
                            placement.showCmd = User32.SW_NORMAL
                        }
                        1 -> {
                            placement.showCmd = User32.SW_SHOWMINIMIZED
                        }
                        2 -> {
                            placement.showCmd = User32.SW_SHOWMAXIMIZED
                        }
                    }
                    User32.INSTANCE.SetWindowPlacement(hwnd!!, placement)
                }
            }
            // TODO: Make this configurable
            addProperty<Label>(sizeAndPositionGroup, "Z Order").apply {
                text = WinUtil.zOrder.indexOf(hwnd!!).toString()
            }

            (this.control as ScrolledComposite).setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT))
        }

        // TODO: Add buttons to add/remove styles
        addItem("Styles", 5).apply {
            val widget = (this.control as ScrolledComposite).content as Composite

            val normalStyles = User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_STYLE)
            val extendedStyles = User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_EXSTYLE)

            addProperty<Label>(widget, "Window Styles").apply {
                this.text = normalStyles.toString()
            }

            addProperty<Label>(widget, "Extended Styles").apply {
                this.text = extendedStyles.toString()
            }

            List(widget, SWT.BORDER or SWT.H_SCROLL or SWT.V_SCROLL).apply {
                layoutData = GridData(SWT.FILL, SWT.FILL, true, true).apply {
                    horizontalSpan = 2
                }

                val styles = WinUtil.getWindowStyles(hwnd!!)

                for (s in styles) {
                    this.add(s)
                }
            }

            List(widget, SWT.BORDER or SWT.H_SCROLL or SWT.V_SCROLL).apply {
                layoutData = GridData(SWT.FILL, SWT.FILL, true, true).apply {
                    horizontalSpan = 2
                }

                val styles = WinUtil.getExtendedStyles(hwnd!!)

                for (s in styles) {
                    this.add(s)
                }
            }
        }

        addItem("Class", 2).apply {
            val widget = (this.control as ScrolledComposite).content as Composite

            addProperty<Label>(widget, "Class Name").apply { text = WinUtil.getClass(hwnd!!) }
            addProperty<Label>(widget, "Class Style").apply { text = User32.INSTANCE.GetClassLong(hwnd, User32.GCL_STYLE).toString() }
            // addProperty<Label>(widget, "Small Icon Handle").apply { text = User32.INSTANCE.GetClassLong(hwnd, User32.GCL_HICONSM).toString() }
            addProperty<Label>(widget, "Icon Handle").apply { text = User32.INSTANCE.GetClassLong(hwnd, User32.GCL_HICON).toString() }

            val icon = Image.win32_new(shell.display, SWT.ICON, User32.INSTANCE.GetClassLong(hwnd, User32.GCL_HICON).toLong())
            if (!icon.isDisposed) {
                Label(widget, SWT.RIGHT).apply {
                    this.image = icon
                    layoutData = GridData(GridData.FILL_HORIZONTAL).apply {
                        horizontalSpan = 2
                    }
                }
            }

            addProperty<Label>(widget, "Cursor Handle").apply { text = User32.INSTANCE.GetClassLong(hwnd, User32.GCLP_HCURSOR).toString() }

            val cursorIcon = Image.win32_new(shell.display, SWT.ICON, User32.INSTANCE.GetClassLong(hwnd, User32.GCLP_HCURSOR).toLong())
            if (!cursorIcon.isDisposed) {
                Label(widget, SWT.RIGHT).apply {
                    image = cursorIcon
                    layoutData = GridData(GridData.FILL_HORIZONTAL).apply {
                        horizontalSpan = 2
                    }
                }
            }

            addProperty<Label>(widget, "Background Brush Handle").apply { text = User32.INSTANCE.GetClassLong(hwnd, User32.GCLP_HBRBACKGROUND).toString() }
            addProperty<Label>(widget, "Module Handle").apply { text = User32.INSTANCE.GetClassLong(hwnd, User32.GCLP_HMODULE).toString() }
            addProperty<Label>(widget, "Extra Class Memory").apply { text = User32.INSTANCE.GetClassLong(hwnd, User32.GCL_CBCLSEXTRA).toString() }
            addProperty<Label>(widget, "Extra Window Memory").apply { text = User32.INSTANCE.GetClassLong(hwnd, User32.GCL_CBWNDEXTRA).toString() }
            addProperty<Label>(widget, "Atom").apply { text = User32.INSTANCE.GetClassLong(hwnd, User32.GCW_ATOM).toString() }
            addProperty<Label>(widget, "Menu Name").apply { text = User32.INSTANCE.GetClassLong(hwnd, User32.GCLP_MENUNAME).toString() }
            addProperty<Label>(widget, "Window Proc").apply { text = User32.INSTANCE.GetClassLong(hwnd, User32.GCLP_WNDPROC).toString() }

            (this.control as ScrolledComposite).setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT))
        }

        addItem("Process", 2).apply {
            val process = IntByReference(0)
            val thread = User32.INSTANCE.GetWindowThreadProcessId(hwnd!!, process)

            val widget = (this.control as ScrolledComposite).content as Composite

            addProperty<Label>(widget, "Process ID").apply { this.text = process.value.toString() }
            addProperty<Label>(widget, "Thread ID").apply { this.text = thread.toString() }
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

    fun Spinner.setText(value: String) {
        val checkWidget = Widget::class.java.getDeclaredMethod("checkWidget")
        checkWidget.isAccessible = true
        val getCodePage = Control::class.java.getDeclaredMethod("getCodePage")
        getCodePage.isAccessible = true

        val hwndText = Spinner::class.java.getDeclaredField("hwndText")
        hwndText.isAccessible = true

        checkWidget.invoke(this)
        val buffer = TCHAR(getCodePage.invoke(this).toString().toInt(), value, true)

        OS.SetWindowText(hwndText.getLong(this), buffer)
    }
}
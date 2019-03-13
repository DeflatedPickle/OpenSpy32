package com.deflatedpickle.openspy32

import com.deflatedpickle.jna.User32Extended
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference

object WinUtil {
    val zOrder = getZList()

    fun getMonitorWindows(monitor: WinUser.HMONITOR): List<WinDef.HWND> {
        val windows: MutableList<WinDef.HWND> = mutableListOf()

        User32.INSTANCE.EnumWindows({ hwnd, pntr ->
            if (monitor == User32.INSTANCE.MonitorFromWindow(hwnd, User32.MONITOR_DEFAULTTONULL)) {
                windows.add(hwnd)
            }
            true
        }, null)

        return windows
    }

    fun getChildWindows(window: WinDef.HWND): List<WinDef.HWND> {
        val windows: MutableList<WinDef.HWND> = mutableListOf()

        User32.INSTANCE.EnumChildWindows(window, { hWnd, data ->
            windows.add(hWnd)
            true
        }, null)

        return windows
    }

    fun getWindowProcess(window: WinDef.HWND): Int {
        val currentProcess = IntByReference(0)
        User32.INSTANCE.GetWindowThreadProcessId(window, currentProcess)

        return currentProcess.value
    }


    fun getTitle(hwnd: WinDef.HWND): String {
        val length = User32.INSTANCE.GetWindowTextLength(hwnd) + 1
        val windowText = CharArray(length)
        User32.INSTANCE.GetWindowText(hwnd, windowText, length)

        return Native.toString(windowText)
    }

    fun getClass(hwnd: WinDef.HWND): String {
        val className = CharArray(80)
        User32.INSTANCE.GetClassName(hwnd, className, 80)

        return Native.toString(className)
    }

    fun getAllDisplays(): List<WinUser.HMONITOR> {
        val monitors: MutableList<WinUser.HMONITOR> = mutableListOf()

        User32.INSTANCE.EnumDisplayMonitors(null, null, { hMonitor, hdcMonitor, lprcMonitor, dwData ->
            monitors.add(hMonitor)
            1
        }, null)

        return monitors
    }

    fun getWindowRect(hwnd: WinDef.HWND): WinDef.RECT {
        val rect = WinDef.RECT()
        User32.INSTANCE.GetWindowRect(hwnd, rect)
        return rect
    }

    fun getClientRect(hwnd: WinDef.HWND): WinDef.RECT {
        val rect = WinDef.RECT()
        User32.INSTANCE.GetClientRect(hwnd, rect)
        return rect
    }

    fun getWindowPlacement(hwnd: WinDef.HWND): WinUser.WINDOWPLACEMENT {
        val placement = WinUser.WINDOWPLACEMENT()
        User32.INSTANCE.GetWindowPlacement(hwnd, placement)
        return placement
    }

    fun hexToHandle(hex: String): Pointer {
        return Pointer(java.lang.Long.decode(hex))
    }

    fun handleToHex(handle: Pointer): String {
        return handle.toString().split("@")[1]
    }

    private fun getZList(): List<WinDef.HWND> {
        val zOrder = mutableListOf<WinDef.HWND>()

        var next = User32Extended.INSTANCE.GetTopWindow(null)
        while (next != null) {
            zOrder.add(next)
            next = User32.INSTANCE.GetWindow(next, WinDef.DWORD(User32.GW_HWNDNEXT.toLong()))
        }

        return zOrder
    }

    fun getWindowStyles(hwnd: WinDef.HWND): List<String> {
        val styleList = mutableListOf<String>()
        val style = User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_STYLE)

        if (style and User32.WS_BORDER != 0) { styleList.add("WS_BORDER") }
        if (style and User32.WS_CAPTION != 0) { styleList.add("WS_CAPTION") }
        if (style and User32.WS_CHILD != 0) { styleList.add("WS_CHILD") }
        if (style and User32.WS_CHILDWINDOW != 0) { styleList.add("WS_CHILDWINDOW") }
        if (style and User32.WS_CLIPCHILDREN != 0) { styleList.add("WS_CLIPCHILDREN") }
        if (style and User32.WS_CLIPSIBLINGS != 0) { styleList.add("WS_CLIPSIBLINGS") }
        if (style and User32.WS_DISABLED != 0) { styleList.add("WS_DISABLED") }
        if (style and User32.WS_DLGFRAME != 0) { styleList.add("WS_DLGFRAME") }
        if (style and User32.WS_GROUP != 0) { styleList.add("WS_GROUP") }
        if (style and User32.WS_HSCROLL != 0) { styleList.add("WS_HSCROLL") }
        if (style and User32.WS_ICONIC != 0) { styleList.add("WS_ICONIC") }
        if (style and User32.WS_MAXIMIZE != 0) { styleList.add("WS_MAXIMIZE") }
        if (style and User32.WS_MAXIMIZEBOX != 0) { styleList.add("WS_MAXIMIZEBOX") }
        if (style and User32.WS_MINIMIZE != 0) { styleList.add("WS_MINIMIZE") }
        if (style and User32.WS_MINIMIZEBOX != 0) { styleList.add("WS_MINIMIZEBOX") }
        if (style and User32.WS_OVERLAPPED != 0) { styleList.add("WS_OVERLAPPED") }
        if (style and User32.WS_POPUP != 0) { styleList.add("WS_POPUP") }
        if (style and User32.WS_SIZEBOX != 0) { styleList.add("WS_SIZEBOX") }
        if (style and User32.WS_SYSMENU != 0) { styleList.add("WS_SYSMENU") }
        if (style and User32.WS_TABSTOP != 0) { styleList.add("WS_TABSTOP") }
        if (style and User32.WS_THICKFRAME != 0) { styleList.add("WS_THICKFRAME") }
        if (style and User32.WS_TILED != 0) { styleList.add("WS_TILED") }
        if (style and User32.WS_VISIBLE != 0) { styleList.add("WS_VISIBLE") }
        if (style and User32.WS_VSCROLL != 0) { styleList.add("WS_VSCROLL") }

        return styleList
    }

    fun getExtendedStyles(hwnd: WinDef.HWND): List<String> {
        val styleList = mutableListOf<String>()
        val style = User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_EXSTYLE)

        if (style and User32Extended.WS_EX_ACCEPTFILES != 0) { styleList.add("WS_EX_ACCEPTFILES") }
        if (style and User32Extended.WS_EX_APPWINDOW != 0) { styleList.add("WS_EX_APPWINDOW") }
        if (style and User32Extended.WS_EX_CLIENTEDGE != 0) { styleList.add("WS_EX_CLIENTEDGE") }
        if (style and User32Extended.WS_EX_COMPOSITED != 0) { styleList.add("WS_EX_COMPOSITED") }
        if (style and User32Extended.WS_EX_CONTEXTHELP != 0) { styleList.add("WS_EX_CONTEXTHELP") }
        if (style and User32Extended.WS_EX_CONTROLPARENT != 0) { styleList.add("WS_EX_CONTROLPARENT") }
        if (style and User32Extended.WS_EX_DLGMODALFRAME != 0) { styleList.add("WS_EX_DLGMODALFRAME") }
        if (style and User32Extended.WS_EX_LAYERED != 0) { styleList.add("WS_EX_LAYERED") }
        if (style and User32Extended.WS_EX_LAYOUTRTL != 0) { styleList.add("WS_EX_LAYOUTRTL") }
        if (style and User32Extended.WS_EX_LEFT != 0) { styleList.add("WS_EX_LEFT") }
        if (style and User32Extended.WS_EX_LEFTSCROLLBAR != 0) { styleList.add("WS_EX_LEFTSCROLLBAR") }
        if (style and User32Extended.WS_EX_LTRREADING != 0) { styleList.add("WS_EX_LTRREADING") }
        if (style and User32Extended.WS_EX_MDICHILD != 0) { styleList.add("WS_EX_MDICHILD") }
        if (style and User32Extended.WS_EX_NOACTIVATE != 0) { styleList.add("WS_EX_NOACTIVATE") }
        if (style and User32Extended.WS_EX_NOINHERITLAYOUT != 0) { styleList.add("WS_EX_NOINHERITLAYOUT") }
        if (style and User32Extended.WS_EX_NOPARENTNOTIFY != 0) { styleList.add("WS_EX_NOPARENTNOTIFY") }
        if (style and User32Extended.WS_EX_NOREDIRECTIONBITMAP != 0) { styleList.add("WS_EX_NOREDIRECTIONBITMAP") }
        if (style and User32Extended.WS_EX_RIGHT != 0) { styleList.add("WS_EX_RIGHT") }
        if (style and User32Extended.WS_EX_RIGHTSCROLLBAR != 0) { styleList.add("WS_EX_RIGHTSCROLLBAR") }
        if (style and User32Extended.WS_EX_RTLREADING != 0) { styleList.add("WS_EX_RTLREADING") }
        if (style and User32Extended.WS_EX_STATICEDGE != 0) { styleList.add("WS_EX_STATICEDGE") }
        if (style and User32Extended.WS_EX_TOOLWINDOW != 0) { styleList.add("WS_EX_TOOLWINDOW") }
        if (style and User32Extended.WS_EX_TOPMOST != 0) { styleList.add("WS_EX_TOPMOST") }
        if (style and User32Extended.WS_EX_TRANSPARENT != 0) { styleList.add("WS_EX_TRANSPARENT") }
        if (style and User32Extended.WS_EX_WINDOWEDGE != 0) { styleList.add("WS_EX_WINDOWEDGE") }

        return styleList
    }
}
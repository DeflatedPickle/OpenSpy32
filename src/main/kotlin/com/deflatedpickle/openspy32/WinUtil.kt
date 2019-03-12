package com.deflatedpickle.openspy32

import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference

object WinUtil {
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
}
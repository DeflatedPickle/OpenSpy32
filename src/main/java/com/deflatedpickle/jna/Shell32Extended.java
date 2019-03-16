package com.deflatedpickle.jna;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.win32.W32APIOptions;

public interface Shell32Extended extends Shell32 {
    Shell32Extended INSTANCE = Native.load("shell32", Shell32Extended.class, W32APIOptions.DEFAULT_OPTIONS);

    boolean IsUserAnAdmin();
}

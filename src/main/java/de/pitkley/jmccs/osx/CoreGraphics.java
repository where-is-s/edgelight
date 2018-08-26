package de.pitkley.jmccs.osx;

import com.sun.jna.IntegerType;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public interface CoreGraphics extends Library {
    CoreGraphics INSTANCE = (CoreGraphics) Native.loadLibrary("CoreGraphics", CoreGraphics.class);

    public static class CGError extends IntegerType {

        /**
         * `CGError` is just an alias for `int32_t`, which equals 4 bytes.
         */
        public static final int SIZE = 4;

        public CGError() {
            this(0);
        }

        public CGError(long value) {
            super(SIZE, value, false);
        }
    }

    public static class CGDirectDisplayID extends IntegerType {

        /**
         * `CGDirectDisplayID` is just an alias for `uint32_t`, which equals 4 bytes.
         */
        public static final int SIZE = 4;

        public CGDirectDisplayID() {
            this(0);
        }

        public CGDirectDisplayID(long value) {
            super(SIZE, value, true);
        }
    }

    CGDirectDisplayID CGMainDisplayID();
    CGError CGGetOnlineDisplayList(int maxDisplays, Pointer onlineDisplays, IntByReference displayCount);
    CGError CGGetActiveDisplayList(int maxDisplays, Pointer activeDisplays, IntByReference displayCount);

}

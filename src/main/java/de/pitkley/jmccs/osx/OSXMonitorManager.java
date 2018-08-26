package de.pitkley.jmccs.osx;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import de.pitkley.jmccs.monitor.Monitor;
import de.pitkley.jmccs.monitor.MonitorManager;
import de.pitkley.jmccs.monitor.UnsupportedOperatingSystemException;
import de.pitkley.jmccs.monitor.VCPStringFormatException;

import java.util.ArrayList;
import java.util.List;

public class OSXMonitorManager extends MonitorManager {
    private final CoreGraphics CG;

    private static final int MAX_DISPLAYS = 10;

    private final List<Monitor> monitors = new ArrayList<>();

    public OSXMonitorManager() throws UnsupportedOperatingSystemException {
        if (!Platform.isMac()) {
            throw new UnsupportedOperatingSystemException("OS X", System.getProperty("os.name"));
        }

        CG = CoreGraphics.INSTANCE;
    }
    public List<Monitor> getMonitors() {
        if (monitors.size() == 0) {
            Pointer onlineDisplays = new Memory(MAX_DISPLAYS * Native.getNativeSize(Integer.TYPE));
            IntByReference ptrDisplayCount = new IntByReference();

            CG.CGGetOnlineDisplayList(MAX_DISPLAYS, onlineDisplays, ptrDisplayCount);

            for (int i = 0; i < ptrDisplayCount.getValue(); i++) {
                try {
                    monitors.add(new OSXMonitor(new CoreGraphics.CGDirectDisplayID(onlineDisplays.getInt(i * Native.getNativeSize(Integer.TYPE)))));
                } catch (VCPStringFormatException ignored) {
                    /**
                     * If we land here, we couldn't instantiate the monitor.
                     * This can have multiple reasons:
                     *   1. The monitor doesn't support DDC/CI
                     *   2. The monitors VCP-string was missing
                     *   3. The monitors VCP-string was malformatted
                     *   4. There is a communication issue with the monitor causing reason 2 or 3
                     * In my testings, the fourth point seemed to be by far the biggest issue. Whenever I tried to
                     * communicate with a monitor via DisplayPort, it was hit-and-miss if it worked or not. DVI gave
                     * me the best results and always worked.
                     */
                    ignored.printStackTrace(); // TODO This needs to be handled better
                }
            }
        }

        return new ArrayList<>(monitors);
    }

    @Override
    public void closeMonitors() {
        /**
         * OS X monitors need no closing.
         */
    }
}

package de.pitkley.jmccs.osx;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import de.pitkley.jmccs.utils.Utils;

import java.util.Arrays;
import java.util.List;

public interface LibMCCS extends Library {
    LibMCCS INSTANCE = (LibMCCS) Native.loadLibrary(Utils.createFileForResource("libmccs.dylib"), LibMCCS.class);

    public static class MCCSReadCommand extends Structure {

        public byte control_id;
        public byte max_value;
        public byte current_value;

        public MCCSReadCommand() {
            super();
        }

        public MCCSReadCommand(byte control_id, byte max_value, byte current_value) {
            super();
            this.control_id = control_id;
            this.max_value = max_value;
            this.current_value = current_value;
        }

        public MCCSReadCommand(Pointer peer) {
            super(peer);
        }

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("control_id", "max_value", "current_value");
        }

        public static class ByReference extends MCCSReadCommand implements Structure.ByReference {
        }

        public static class ByValue extends MCCSReadCommand implements Structure.ByValue {
        }

    }

    public static class MCCSWriteCommand extends Structure {

        public byte control_id;
        public byte new_value;

        public MCCSWriteCommand() {
            super();
        }

        public MCCSWriteCommand(int code, int brightness) {
            this((byte) code, (byte) brightness);
        }

        public MCCSWriteCommand(byte control_id, byte new_value) {
            super();
            this.control_id = control_id;
            this.new_value = new_value;
        }

        public MCCSWriteCommand(Pointer peer) {
            super(peer);
        }

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("control_id", "new_value");
        }

        public static class ByReference extends MCCSWriteCommand implements Structure.ByReference {
        }

        public static class ByValue extends MCCSWriteCommand implements Structure.ByValue {
        }
    }

    void cleanup_pointer(Pointer p);

    byte MCCSWrite(CoreGraphics.CGDirectDisplayID displayID, MCCSWriteCommand write);

    byte MCCSRead(CoreGraphics.CGDirectDisplayID displayID, MCCSReadCommand.ByReference read);

    int MCCSGetCapabilityString(CoreGraphics.CGDirectDisplayID displayID, PointerByReference ppCapabilityString);
}

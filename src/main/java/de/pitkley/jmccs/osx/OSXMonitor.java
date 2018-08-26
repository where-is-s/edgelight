package de.pitkley.jmccs.osx;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import de.pitkley.jmccs.monitor.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class OSXMonitor implements Monitor {
    private static final LibMCCS MCCS = LibMCCS.INSTANCE;
    private static final CoreGraphics CG = CoreGraphics.INSTANCE;

    private final CoreGraphics.CGDirectDisplayID displayId;

    private final Map<VCPCode, Optional<Set<Integer>>> supportedVCPCodes;

    protected OSXMonitor(CoreGraphics.CGDirectDisplayID displayId) throws VCPStringFormatException {
        this.displayId = displayId;

        PointerByReference ppCapabilityString = new PointerByReference();
        MCCS.MCCSGetCapabilityString(displayId, ppCapabilityString);
        Pointer pCapabilityString = ppCapabilityString.getValue();
        String capabilityString = pCapabilityString.getString(0);
        MCCS.cleanup_pointer(pCapabilityString);

        if (capabilityString == null || capabilityString.isEmpty()) {
            throw new VCPStringFormatException("Unable to get capabilities string");
        }

        String vcpString = CapabilityStringParser.parse(capabilityString).get("vcp");
        if (vcpString == null || vcpString.isEmpty()) {
            throw new VCPStringFormatException("Capabilities string is missing the supported VCP codes");
        }

        supportedVCPCodes = VCPStringParser.parse(vcpString);
    }

    @Override
    public boolean isMainMonitor() {
        return CG.CGMainDisplayID().equals(displayId);
    }

    @Override
    public boolean isVCPCodeSupported(VCPCode vcpCode) {
        return supportedVCPCodes.containsKey(vcpCode);
    }

    @Override
    public VCPReply getVCPFeature(VCPCode vcpCode) {
        LibMCCS.MCCSReadCommand.ByReference read = new LibMCCS.MCCSReadCommand.ByReference();
        read.control_id = (byte) vcpCode.getCode();
        MCCS.MCCSRead(this.displayId, read);

        VCPReply reply = new VCPReply(read.current_value, read.max_value);
        return reply;
    }

    @Override
    public boolean setVCPFeature(VCPCode vcpCode, int value) {
        LibMCCS.MCCSWriteCommand write = new LibMCCS.MCCSWriteCommand(vcpCode.getCode(), value);
        byte result = MCCS.MCCSWrite(this.displayId, write);
        return result != 0;
    }

    @Override
    public boolean isClosed() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // Not needed under OS X
    }
}

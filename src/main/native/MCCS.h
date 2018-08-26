//
//  MCCS.h
//
//  Originally created by Jonathan Taylor on 7/10/09.
//  See http://github.com/jontaylor/DDC-CI-Tools-for-OS-X
//
//  Modified version by SJ_UnderWater <http://www.tonymacx86.com/members/sj_underwater/> and/or
//  Joey Korkames <https://github.com/kfix/ddcctl>.
//
//
//  Licensed under GPLv3 <http://www.gnu.org/licenses/gpl-3.0.txt>
//    (see https://github.com/jontaylor/DDC-CI-Tools-for-OS-X/issues/3).
//
//
//  See ftp://ftp.cis.nctu.edu.tw/pub/csie/Software/X11/private/VeSaSpEcS/VESA_Document_Center_Monitor_Interface/mccsV3.pdf
//  See http://read.pudn.com/downloads110/ebook/456020/E-EDID%20Standard.pdf
//  See ftp://ftp.cis.nctu.edu.tw/pub/csie/Software/X11/private/VeSaSpEcS/VESA_Document_Center_Monitor_Interface/EEDIDrAr2.pdf
//

#ifndef MCCS_h
#define MCCS_h

#include <IOKit/i2c/IOI2CInterface.h>

#define MCCS_LENGTH_MAGIC 0x80

#define MCCS_CAPABILITIES_REQUEST_OVERHEAD 3
#define MCCS_CAPABILITIES_REQUEST_MAX_FRAGMENT_SIZE 32
#define MCCS_CAPABILITIES_REQUEST_COMMAND 0xF3
#define MCCS_CAPABILITIES_REPLY_OPCODE 0xE3

struct MCCSCapabilitiesRequest {
    UInt8 destination_address;
    UInt8 source_address;
    UInt8 length;
    UInt8 capabilities_request_command;
    UInt16 offset;
    UInt8 checksum;
};
const struct MCCSCapabilitiesRequest MCCS_CAPABILITIES_REQUEST_DEFAULT = {
    0x6E, 0x51, 0x83, MCCS_CAPABILITIES_REQUEST_COMMAND, 0x0000
};

struct MCCSCapabilitiesReply {
    UInt8 destination_address;
    UInt8 length;
    UInt8 capabilities_reply_opcode;
    UInt8 offset_high;
    UInt8 offset_low;
    UInt8 data[MCCS_CAPABILITIES_REQUEST_MAX_FRAGMENT_SIZE];
};

struct MCCSWriteCommand
{
    UInt8 control_id;
    UInt8 new_value;
};

struct MCCSReadCommand
{
    UInt8 control_id;
    UInt8 max_value;
    UInt8 current_value;
};


UInt8 checksum(int count, ...);
void cleanup_pointer(char *p);
bool MCCSWrite(CGDirectDisplayID displayID, struct MCCSWriteCommand *write);
bool MCCSRead(CGDirectDisplayID displayID, struct MCCSReadCommand *read);

int MCCSGetCapabilityString(CGDirectDisplayID displayID, char** ppCapabilityString);
int MCCSGetCapabilitiesByOffset(CGDirectDisplayID displayID, UInt16 offset, struct MCCSCapabilitiesReply *reply);

#endif

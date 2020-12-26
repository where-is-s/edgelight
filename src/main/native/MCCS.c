//
//  MCCS.c
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

#include <IOKit/IOKitLib.h>
#include <ApplicationServices/ApplicationServices.h>
#include "MCCS.h"
#define kDelayBase 60

UInt8 checksum(int count, ...) {
    int i;
    va_list al;
    va_start(al, count);

    UInt8 result = 0x00;

    for (i = 1; i <= count; i++) {
        UInt8 val = va_arg(al, int);
        result ^= val;
    }

    return result;
}

void cleanup_pointer(char* p) {
    free(p);
}

dispatch_semaphore_t DisplayQueue(CGDirectDisplayID displayID) {
    static UInt64 queueCount = 0;
    static struct DDCQueue {CGDirectDisplayID id; dispatch_semaphore_t queue;} *queues = NULL;
    dispatch_semaphore_t queue = NULL;
    if (!queues)
        queues = calloc(50, sizeof(*queues));//FIXME: specify
    UInt64 i = 0;
    while (i < queueCount)
        if (queues[i].id == displayID)
            break;
        else
            i++;
    if (queues[i].id == displayID)
        queue = queues[i].queue;
    else
        queues[queueCount++] = (struct DDCQueue){displayID, (queue = dispatch_semaphore_create(1))};
    return queue;
}

bool DisplayRequest(CGDirectDisplayID displayID, IOI2CRequest *request) {
    dispatch_semaphore_t queue = DisplayQueue(displayID);
    dispatch_semaphore_wait(queue, DISPATCH_TIME_FOREVER);
    bool result = false;
    io_service_t framebuffer;
    if ((framebuffer = CGDisplayIOServicePort(displayID))) {
        IOItemCount busCount;
        if (IOFBGetI2CInterfaceCount(framebuffer, &busCount) == KERN_SUCCESS) {
            IOOptionBits bus = 0;
            while (bus < busCount) {
                io_service_t interface;
                if (IOFBCopyI2CInterfaceForBus(framebuffer, bus++, &interface) != KERN_SUCCESS)
                    continue;
                CFNumberRef flags = NULL;
                CFIndex flag;
                if (request->minReplyDelay
                    && (flags = IORegistryEntryCreateCFProperty(interface, CFSTR(kIOI2CSupportedCommFlagsKey), kCFAllocatorDefault, 0))
                    && CFNumberGetValue(flags, kCFNumberCFIndexType, &flag)
                    && flag == kIOI2CUseSubAddressCommFlag)
                    request->minReplyDelay *= kMillisecondScale;
                if (flags)
                    CFRelease(flags);
                IOI2CConnectRef connect;
                if (IOI2CInterfaceOpen(interface, kNilOptions, &connect) == KERN_SUCCESS) {
                    result = (IOI2CSendRequest(connect, kNilOptions, request) == KERN_SUCCESS);
                    IOI2CInterfaceClose(connect, kNilOptions);
                }
                IOObjectRelease(interface);
                if (result) break;
            }
        }
    }
    if (request->replyTransactionType == kIOI2CNoTransactionType)
        usleep(kDelayBase * kMicrosecondScale);
    dispatch_semaphore_signal(queue);
    return result && request->result == KERN_SUCCESS;
}

bool MCCSWrite(CGDirectDisplayID displayID, struct MCCSWriteCommand *write) {
    IOI2CRequest request = {};
    request.commFlags = kIOI2CUseSubAddressCommFlag;
    request.sendAddress = 0x6E;
    request.sendSubAddress = 0x51;
    request.sendTransactionType = kIOI2CSimpleTransactionType;
    UInt8 data[6] = {0x80, 0x03, write->control_id, 0x0, write->new_value};
    data[0]+=sizeof(data)-2;
    data[5] = request.sendAddress ^ request.sendSubAddress ^ data[0] ^ data[1] ^ data[2] ^ data[3] ^ data[4];
    request.sendBuffer = (vm_address_t) data;
    request.sendBytes = sizeof(data);
    request.replyTransactionType = kIOI2CNoTransactionType;
    bool result = DisplayRequest(displayID, &request);
    return result;
}

bool MCCSRead(CGDirectDisplayID displayID, struct MCCSReadCommand *read) {
    IOI2CRequest request = {};
    UInt8 reply_data[11] = {};
    bool result = false;
    request.commFlags = kIOI2CUseSubAddressCommFlag;
    request.sendAddress = 0x6E;
    request.sendSubAddress = 0x51;
    request.sendTransactionType = kIOI2CSimpleTransactionType;
    UInt8 data[4] = {0x80, 0x1, read->control_id};
    data[0]+=sizeof(data)-2;
    data[3] = request.sendAddress ^ request.sendSubAddress ^ data[0] ^ data[1] ^ data[2];
    request.sendBuffer = (vm_address_t) data;
    request.sendBytes = sizeof(data);
    request.replyAddress = 0x6F;
    request.replySubAddress = request.sendSubAddress;
    request.replyTransactionType = kIOI2CDDCciReplyTransactionType;
    request.replyBuffer = (vm_address_t) reply_data;
    request.replyBytes = sizeof(reply_data);
    request.minReplyDelay = kDelayBase;
    result = DisplayRequest(displayID, &request);
    result = (result && reply_data[0] == request.sendAddress && reply_data[2] == 0x2 && reply_data[4] == read->control_id && reply_data[10] == (request.replyAddress ^ request.replySubAddress ^ reply_data[1] ^ reply_data[2] ^ reply_data[3] ^ reply_data[4] ^ reply_data[5] ^ reply_data[6] ^ reply_data[7] ^ reply_data[8] ^ reply_data[9]));
    read->max_value = reply_data[7];
    read->current_value = reply_data[9];
    return result;
}

int MCCSGetCapabilityString(CGDirectDisplayID displayID, char** ppCapabilityString) {
    /*
     Implementation inspired by oleg's tool `ddcci` <http://jaffar.cs.msu.su/oleg/ddcci/>, predecessor of `ddccontrol` <http://sourceforge.net/projects/ddccontrol/>.
     */
    size_t capstring_size = 256;
    unsigned int len = 0, offset = 0;
    *ppCapabilityString = (char*) malloc(capstring_size * sizeof(char));

    do {
        struct MCCSCapabilitiesReply capReply = {};
        len = MCCSGetCapabilitiesByOffset(displayID, offset, &capReply);
        if (len < MCCS_CAPABILITIES_REQUEST_OVERHEAD) {
            return 1;
        }

        unsigned int data_length = (len - MCCS_CAPABILITIES_REQUEST_OVERHEAD);
        if (data_length <= 0) {
            break; // End of string
        }

        if ((offset + data_length) >= /* greater-equal to account for null-termination */ capstring_size) {
            capstring_size += 256;
            char* capstring_realloc = realloc(*ppCapabilityString, capstring_size);
            if (capstring_realloc) {
                *ppCapabilityString = capstring_realloc;
            } else {
                return 1;
            }
        }

        memcpy((*ppCapabilityString + offset), capReply.data, data_length);
        offset += data_length;
    } while (len > MCCS_CAPABILITIES_REQUEST_OVERHEAD);

    return 0;
}

int MCCSGetCapabilitiesByOffset(CGDirectDisplayID displayID, UInt16 offset, struct MCCSCapabilitiesReply *reply) {
    /*
     See http://ftp.cis.nctu.edu.tw/csie/Software/X11/private/VeSaSpEcS/VESA_Document_Center_Monitor_Interface/ddcciv1r1.pdf page 21 for additional information.
     */
    IOI2CRequest request = {};
    struct MCCSCapabilitiesRequest capRequest = MCCS_CAPABILITIES_REQUEST_DEFAULT;
    capRequest.offset = offset;

    request.commFlags = kIOI2CUseSubAddressCommFlag;
    request.sendAddress = capRequest.destination_address;
    request.sendSubAddress = capRequest.source_address;
    request.sendTransactionType = kIOI2CSimpleTransactionType;

    UInt8 data[5] = {
        capRequest.length,
        capRequest.capabilities_request_command,
        (capRequest.offset >> 8), // Offset value High byte
        (capRequest.offset & 255) // Offset value Low byte
    };
    data[4] = checksum(6, capRequest.destination_address, capRequest.source_address, capRequest.length, capRequest.capabilities_request_command, (capRequest.offset >> 8), (capRequest.offset & 255));
    request.sendBuffer = (vm_address_t) data;
    request.sendBytes = sizeof(data);

    request.replyAddress = 0x6F;
    request.replySubAddress = request.sendSubAddress;
    request.replyTransactionType = kIOI2CDDCciReplyTransactionType;
    unsigned char _buf[sizeof(struct MCCSCapabilitiesReply)];
    request.replyBuffer = (vm_address_t) _buf;
    request.replyBytes = sizeof(_buf);

    request.minReplyDelay = kDelayBase;

    bool result = false;
    result = DisplayRequest(displayID, &request);

    if (!result || !reply) {
        return 0;
    }

    memcpy(reply, &_buf, sizeof(struct MCCSCapabilitiesReply));

    result = (result
              && reply->destination_address == capRequest.destination_address
              && (reply->length & 0x80) != 0
              && reply->capabilities_reply_opcode == 0xE3
              && reply->offset_high == (capRequest.offset >> 8)
              && reply->offset_low == (capRequest.offset & 255));

    if (!result) {
        return 0;
    }

    return (reply->length & ~MCCS_LENGTH_MAGIC);
}

package com.rtsffm.ertd.proto.request;

import com.rtsffm.ertd.inetcom.Rid;

//~--- interfaces -------------------------------------------------------------

public interface InetRequest extends InetStruct {
    Rid getRid();
}

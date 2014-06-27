package com.rtsffm.ertd.proto.request;

import com.rtsffm.ertd.inetcom.Fid;

//~--- interfaces -------------------------------------------------------------

public interface InetStructBuilder {
    InetStruct build();

    //~--- set methods --------------------------------------------------------

    void setRequestParam(Fid fid, Object obj);
}

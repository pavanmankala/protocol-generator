package com.rtsffm.ertd.proto.request;

import com.rtsffm.ertd.inetcom.Fid;

//~--- interfaces -------------------------------------------------------------

public interface InetStruct {
    String serialize();

    String toString();

    //~--- get methods --------------------------------------------------------

    boolean isValid();

    Fid[] getOptionalParams();

    Fid[] getMandatoryParams();

    boolean hasValue(Fid fid);
}

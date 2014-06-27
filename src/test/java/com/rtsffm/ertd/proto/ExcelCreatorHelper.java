package com.rtsffm.ertd.proto;

import com.rtsffm.ertd.inetcom.Fid;
import com.rtsffm.ertd.inetcom.Fid.RTDataTypeType;
import com.rtsffm.ertd.inetcom.Rid;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

//~--- classes ----------------------------------------------------------------

public class ExcelCreatorHelper {
    public static void main(String[] args) {
        Set<RTDataTypeType> dataTypes = new LinkedHashSet<Fid.RTDataTypeType>(Arrays.asList(RTDataTypeType.values()));
        Set<Fid>            fids      = new LinkedHashSet<Fid>(Arrays.asList(Fid.values()));


        {
            Set<RTDataTypeType> dataTypes_tmp = new HashSet<Fid.RTDataTypeType>(dataTypes);

            for (Fid fid : fids) {
                RTDataTypeType type = fid.getRTDataTypeType();

                if (dataTypes_tmp.contains(type)) {
                    dataTypes_tmp.remove(type);

                    System.out.println(type.toString() + "\t" + Fid.getObject(fid).getClass().getName());
                }
            }

            System.out.println(dataTypes_tmp);
        }


        {
            for (Fid.Type type : Fid.Type.values()) {
                System.out.println(type.toString());
            }
        }


        {
            Map<Fid.Type, Integer> type_offset = new HashMap<Fid.Type, Integer>();

            type_offset.put(Fid.Type.BASE, 0);
            type_offset.put(Fid.Type.CLIENT_ONLY, 32767);
            type_offset.put(Fid.Type.CLIENT_ONLY_PUT, 50000);
            type_offset.put(Fid.Type.INET_SPECIFIC, 65535);
            type_offset.put(Fid.Type.INET_SPECIFIC_ARRAY, 68000);
            type_offset.put(Fid.Type.CLIENT_ONLY_ALGO, 60000);
            type_offset.put(Fid.Type.CLIENT_ONLY_OTC, 62000);
            type_offset.put(Fid.Type.INET_SPECIFIC_OTC, 67000);


            for (Fid fid : fids) {
                int no_from_map = type_offset.get(fid.getFidType());

                System.out.println(fid.getFidName() + "\t" + fid.getFidType() + "\t" + (fid.getFidInt() - no_from_map)
                                   + "\t" + fid.getRTDataTypeType());
            }
        }


        {
            for (Rid.Type type : Rid.Type.values()) {
                System.out.println(type.toString());
            }
        }

        {
            Map<Rid.Type, Integer> type_offset = new HashMap<Rid.Type, Integer>();

            type_offset.put(Rid.Type.BASE, 0);
            type_offset.put(Rid.Type.CLIENT_ONLY, 32767);
            type_offset.put(Rid.Type.INET_SPECIFIC, 65535);

            for (Rid rid : Rid.values()) {
                int no_from_map = type_offset.get(rid.getRidType());

                System.out.println(rid.name() + "\t" + rid.getRidType() + "\t" + (rid.getRidInt() - no_from_map));
            }
        }
    }
}

package com.rtsffm.ertd.proto.gen.sheetprocessor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;

//~--- classes ----------------------------------------------------------------

class BooleanRetriever implements ValueRetriever<Boolean> {
    @Override
    public Boolean retrieveValue(XSSFCell cell) {
        try {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN :
                    return cell.getBooleanCellValue();

                case Cell.CELL_TYPE_STRING :
                    return Boolean.parseBoolean(cell.getStringCellValue());

                default :
                    return false;
            }
        } catch (Throwable e) {
            return false;
        }
    }
}


class ClassRetriever implements ValueRetriever<Class<?>> {
    private static final Map<String, Class<?>> PRIMITIVE_MAP = new HashMap<String, Class<?>>();

    //~--- static initializers ------------------------------------------------

    static {
        PRIMITIVE_MAP.put("int", Integer.TYPE);
        PRIMITIVE_MAP.put("long", Long.TYPE);
        PRIMITIVE_MAP.put("double", Double.TYPE);
        PRIMITIVE_MAP.put("float", Float.TYPE);
        PRIMITIVE_MAP.put("bool", Boolean.TYPE);
        PRIMITIVE_MAP.put("boolean", Boolean.TYPE);
        PRIMITIVE_MAP.put("string", String.class);
        PRIMITIVE_MAP.put("char", Character.TYPE);
        PRIMITIVE_MAP.put("byte", Byte.TYPE);
        PRIMITIVE_MAP.put("void", Void.TYPE);
        PRIMITIVE_MAP.put("short", Short.TYPE);
    }

    //~--- methods ------------------------------------------------------------

    @Override
    public Class<?> retrieveValue(XSSFCell cell) {
        try {
            return Class.forName(cell.getStringCellValue());
        } catch (ClassNotFoundException e) {
            return PRIMITIVE_MAP.get(cell.getStringCellValue().toLowerCase());
        }
    }
}


class DoubleRetriever implements ValueRetriever<Double> {
    @Override
    public Double retrieveValue(XSSFCell cell) {
        return (double) cell.getNumericCellValue();
    }
}


class FloatRetriever implements ValueRetriever<Float> {
    @Override
    public Float retrieveValue(XSSFCell cell) {
        return (float) cell.getNumericCellValue();
    }
}


class IntegerRetriever implements ValueRetriever<Integer> {
    @Override
    public Integer retrieveValue(XSSFCell cell) {
        return (int) cell.getNumericCellValue();
    }
}


class LongRetriever implements ValueRetriever<Long> {
    @Override
    public Long retrieveValue(XSSFCell cell) {
        return (long) cell.getNumericCellValue();
    }
}


public class SheetColumn {
    private static final Map<String, SheetColumn>         COLUMN_CACHE  = new HashMap<String, SheetColumn>();
    private static final Map<Class<?>, ValueRetriever<?>> RETRIEVER_MAP = new HashMap<Class<?>, ValueRetriever<?>>();

    //~--- static initializers ------------------------------------------------

    static {
        RETRIEVER_MAP.put(int.class, new IntegerRetriever());
        RETRIEVER_MAP.put(Integer.class, new IntegerRetriever());

        RETRIEVER_MAP.put(float.class, new FloatRetriever());
        RETRIEVER_MAP.put(Float.class, new FloatRetriever());

        RETRIEVER_MAP.put(long.class, new LongRetriever());
        RETRIEVER_MAP.put(Long.class, new LongRetriever());

        RETRIEVER_MAP.put(double.class, new DoubleRetriever());
        RETRIEVER_MAP.put(Double.class, new DoubleRetriever());

        RETRIEVER_MAP.put(boolean.class, new BooleanRetriever());
        RETRIEVER_MAP.put(Boolean.class, new BooleanRetriever());

        RETRIEVER_MAP.put(short.class, new ShortRetriever());
        RETRIEVER_MAP.put(Short.class, new ShortRetriever());

        RETRIEVER_MAP.put(String.class, new StringRetriever());
        RETRIEVER_MAP.put(Class.class, new ClassRetriever());
    }

    //~--- fields -------------------------------------------------------------

    private final Class<?> colClazz;
    private final String   colName;

    //~--- constructors -------------------------------------------------------

    private SheetColumn(String column, Class<?> clazz) {
        colName  = column;
        colClazz = clazz;
    }

    //~--- methods ------------------------------------------------------------

    @Override
    public int hashCode() {
        return colName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SheetColumn) {
            SheetColumn sc = (SheetColumn) obj;

            return sc.colName.equals(colName);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return colName + " - " + colClazz.getName();
    }

    //~--- get methods --------------------------------------------------------

    public String getColumnName() {
        return colName;
    }

    public Class<?> getColumnClass() {
        return colClazz;
    }

    public ValueRetriever<?> getRetriever() {
        return RETRIEVER_MAP.get(colClazz);
    }

    public static synchronized SheetColumn getOrCreateSheetColumn(String name, Class<?> clazz, boolean overwrite) {
        if (overwrite ||!COLUMN_CACHE.containsKey(name)) {
            SheetColumn sc = new SheetColumn(name, clazz);

            COLUMN_CACHE.put(name, sc);

            return sc;
        } else {
            return COLUMN_CACHE.get(name);
        }
    }

    public static synchronized SheetColumn getSheetColumn(String name) {
        return COLUMN_CACHE.get(name);
    }
}


class ShortRetriever implements ValueRetriever<Short> {
    @Override
    public Short retrieveValue(XSSFCell cell) {
        return (short) cell.getNumericCellValue();
    }
}


class StringRetriever implements ValueRetriever<String> {
    @Override
    public String retrieveValue(XSSFCell cell) {
        return cell.getStringCellValue();
    }
}


//~--- interfaces -------------------------------------------------------------

interface ValueRetriever<T> {
    T retrieveValue(XSSFCell cell);
}

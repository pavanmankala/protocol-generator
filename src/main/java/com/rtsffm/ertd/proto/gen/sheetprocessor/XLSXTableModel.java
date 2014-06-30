package com.rtsffm.ertd.proto.gen.sheetprocessor;

public interface XLSXTableModel {
    int getRowCount();

    int getColumnCount();

    Object getRowObject(int row);

    Object getValue(int row, int col);

    Class<?> getClass(int col);

    String getColumnName(int col);

    void setValue(int row, int col, Object value);
}

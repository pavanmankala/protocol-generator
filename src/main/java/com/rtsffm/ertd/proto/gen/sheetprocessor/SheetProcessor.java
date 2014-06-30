package com.rtsffm.ertd.proto.gen.sheetprocessor;

import net.sf.cglib.beans.BeanGenerator;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;

//~--- JDK imports ------------------------------------------------------------

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//~--- classes ----------------------------------------------------------------

public class SheetProcessor {
    public List<XLSXTableModel> readTable(SheetColumn[] columns, XSSFSheet sheet, UndefinedSheetColumnHandler handler) {
        Map<SheetColumn, Integer> columnOffsetMap = new LinkedHashMap<SheetColumn, Integer>();
        List<XLSXTableModel>      models          = new ArrayList<XLSXTableModel>();

        for (XSSFTable table : sheet.getTables()) {
            CTTable             ctTable       = table.getCTTable();
            int                 startRowIndex = table.getStartCellReference().getRow()
                                                + (int) ctTable.getHeaderRowCount(),
                                endRowIndex   = table.getEndCellReference().getRow(),
                                startColIndex = table.getStartCellReference().getCol(),
                                endColIndex   = table.getEndCellReference().getCol();
            List<CTTableColumn> cols          = ctTable.getTableColumns().getTableColumnList();
            boolean             hasTable      = false;
            SPTableModel        model;

            for (SheetColumn col : columns) {
                columnOffsetMap.put(col, -1);
            }

            for (int i = 0; i < cols.size(); i++) {
                CTTableColumn col      = cols.get(i);
                String        colName  = col.getName();
                SheetColumn   sheetCol = SheetColumn.getSheetColumn(colName);

                if (sheetCol == null) {
                    sheetCol = handler.handleUndefinedSheetColumn(colName);
                }

                if (columnOffsetMap.containsKey(sheetCol)) {
                    columnOffsetMap.put(sheetCol, startColIndex + i);
                    hasTable = true;
                } else {
                    columnOffsetMap.remove(sheetCol);
                }
            }

            for (Entry<SheetColumn, Integer> ent :
                    new ArrayList<Entry<SheetColumn, Integer>>(columnOffsetMap.entrySet())) {
                if (ent.getValue() == -1) {
                    columnOffsetMap.remove(ent.getKey());
                }
            }

            if (!hasTable) {
                continue;
            } else {
                model = new SPTableModel(table.getDisplayName(), new ArrayList<SheetColumn>(columnOffsetMap.keySet()));
            }

            for (int rowNumber = startRowIndex; rowNumber <= endRowIndex; rowNumber++) {
                XSSFRow  xssfRow = sheet.getRow(rowNumber);
                Object[] rowData = new Object[columnOffsetMap.size()];

                for (int colNumber = startColIndex; colNumber <= endColIndex; colNumber++) {
                    XSSFCell cell  = xssfRow.getCell(colNumber);
                    int      index = 0;

                    if (cell == null) {
                        continue;
                    }

                    for (Entry<SheetColumn, Integer> offsetEntry : columnOffsetMap.entrySet()) {
                        if (offsetEntry.getValue() == colNumber) {
                            rowData[index] = offsetEntry.getKey().getRetriever().retrieveValue(cell);

                            break;
                        }

                        index++;
                    }
                }

                model.addRow(Arrays.asList(rowData));
            }

            models.add(model);
        }

        return models;
    }

    //~--- inner interfaces ---------------------------------------------------

    public static interface UndefinedSheetColumnHandler {
        SheetColumn handleUndefinedSheetColumn(String sheetColName);
    }


    //~--- inner classes ------------------------------------------------------

    private static class SPTableModel implements XLSXTableModel {
        private final Map<Integer, SetterGetter> setterGetterMap = new LinkedHashMap<Integer, SetterGetter>();
        private final List<Object>               rowBeanData     = new ArrayList<Object>();
        private final BeanGenerator              beanGenerator;
        private final List<SheetColumn>          columns;

        //~--- constructors ---------------------------------------------------

        public SPTableModel(String tableName, List<SheetColumn> cols) {
            columns       = cols;
            beanGenerator = new BeanGenerator();

            for (SheetColumn col : columns) {
                beanGenerator.addProperty(propertyName(col.getColumnName()), col.getColumnClass());
            }

            Class<?> genBeanClass = (Class<?>) beanGenerator.createClass();

            for (SheetColumn col : columns) {
                final Method setterMethod, getterMethod;
                String       name = getterOrSetterName(col.getColumnName());

                try {
                    setterMethod = genBeanClass.getMethod("set" + name, col.getColumnClass());
                    getterMethod = genBeanClass.getMethod("get" + name);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }

                setterGetterMap.put(setterGetterMap.size(), new SetterGetter(setterMethod, getterMethod));
            }
        }

        //~--- methods --------------------------------------------------------

        private String propertyName(String name) {
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }

        private String getterOrSetterName(String name) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }

        void addRow(List<Object> row) {
            Object bean = beanGenerator.create();

            for (int col = 0; col < row.size(); col++) {
                SetterGetter sg = setterGetterMap.get(col);

                try {
                    sg.getSetter().invoke(bean, row.get(col));
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }

            rowBeanData.add(bean);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("[").append("RowCount: ").append(getRowCount()).append(",").append("ColumnCount: ").append(
                getColumnCount()).append("]");

            return sb.toString();
        }

        //~--- get methods ----------------------------------------------------

        @Override
        public int getRowCount() {
            return rowBeanData.size();
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public Object getValue(int row, int col) {
            Object bean = rowBeanData.get(row);

            try {
                return setterGetterMap.get(col).getGetter().invoke(bean);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Class<?> getClass(int col) {
            return columns.get(col).getColumnClass();
        }

        @Override
        public String getColumnName(int col) {
            return columns.get(col).getColumnName();
        }

        @Override
        public Object getRowObject(int row) {
            return rowBeanData.get(row);
        }

        //~--- set methods ----------------------------------------------------

        @Override
        public void setValue(int row, int col, Object value) {
            Object bean = rowBeanData.get(row);

            try {
                Method setter = setterGetterMap.get(col).getSetter();

                setter.invoke(bean, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static final class SetterGetter {
        private final Method setter, getter;

        //~--- constructors ---------------------------------------------------

        public SetterGetter(Method setter, Method getter) {
            this.setter = setter;
            this.getter = getter;
        }

        //~--- get methods ----------------------------------------------------

        public Method getGetter() {
            return getter;
        }

        public Method getSetter() {
            return setter;
        }
    }
}

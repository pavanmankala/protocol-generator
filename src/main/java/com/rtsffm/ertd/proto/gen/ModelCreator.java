package com.rtsffm.ertd.proto.gen;

import com.rtsffm.ertd.proto.gen.sheetprocessor.SheetColumn;
import com.rtsffm.ertd.proto.gen.sheetprocessor.SheetProcessor;
import com.rtsffm.ertd.proto.gen.sheetprocessor.SheetProcessor.UndefinedSheetColumnHandler;
import com.rtsffm.ertd.proto.gen.sheetprocessor.XLSXTableModel;

import org.apache.maven.plugin.logging.Log;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static com.rtsffm.ertd.proto.gen.sheetprocessor.SheetColumn.getOrCreateSheetColumn;
import static com.rtsffm.ertd.proto.gen.sheetprocessor.SheetColumn.getSheetColumn;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

//~--- classes ----------------------------------------------------------------

public class ModelCreator implements UndefinedSheetColumnHandler {
    private static final int    COL_TYPES_SHEET_INDEX = 0;
    private static final String
        COL_TYPES_SHEET_NAME                          = "COL_TYPES",
        COLUMN_COLUMN_NAME                            = "ColumnName",
        COLUMN_JAVA_TYPE                              = "JavaType";

    //~--- fields -------------------------------------------------------------

    private final Map<String, List<XLSXTableModel>> processedMap = new HashMap<String, List<XLSXTableModel>>();

    //~--- constructors -------------------------------------------------------

    public ModelCreator(final File xslxFile, final Log log, final Map<String, List<String>> sheetColumnMap) {
        final OPCPackage   pkg;
        final XSSFWorkbook workbook;

        try {
            pkg      = OPCPackage.open(xslxFile, PackageAccess.READ);
            workbook = new XSSFWorkbook(pkg);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        try {
            Map<Integer, XSSFSheet> sheetProcessingSequence = new TreeMap<Integer, XSSFSheet>();

            for (XSSFSheet sheet : workbook) {
                if (COL_TYPES_SHEET_NAME.equals(sheet.getSheetName())) {
                    sheetProcessingSequence.put(COL_TYPES_SHEET_INDEX, sheet);
                } else {
                    sheetProcessingSequence.put(sheetProcessingSequence.size() + 1, sheet);
                }
            }

            log.info("Processing sheet:" + COL_TYPES_SHEET_NAME);

            SheetProcessor processor = new SheetProcessor();
            //J-
            List<XLSXTableModel> columnTypesTableModels = processor.readTable(new SheetColumn[] {
                                                              getOrCreateSheetColumn(COLUMN_COLUMN_NAME, String.class,
                                                                  true),
                                                              getOrCreateSheetColumn(COLUMN_JAVA_TYPE, Class.class,
                                                                  true)
                                                          }, sheetProcessingSequence.get(COL_TYPES_SHEET_INDEX), this);
            //J+

            if (columnTypesTableModels.size() != 1) {
                throw new RuntimeException("Illegal number of tables exist in COL_TYPES sheet; expected : 1, exists : "
                                           + columnTypesTableModels.size());
            }

            XLSXTableModel        columnTypesTableModel = columnTypesTableModels.get(0);
            Map<String, Class<?>> columnTypeMap         = new HashMap<String, Class<?>>();

            for (int i = 0; i < columnTypesTableModel.getRowCount(); i++) {
                String   colName = columnTypesTableModel.getValue(i, 0).toString();
                Class<?> colTyp  = (Class<?>) columnTypesTableModel.getValue(i, 1);

                columnTypeMap.put(colName, colTyp);
                getOrCreateSheetColumn(colName, colTyp, true);
            }

            processedMap.put(COL_TYPES_SHEET_NAME, columnTypesTableModels);
            log.info("Processed Result: " + columnTypesTableModels);

            for (Entry<Integer, XSSFSheet> entry : sheetProcessingSequence.entrySet()) {
                if (entry.getKey() == COL_TYPES_SHEET_INDEX) {
                    continue;
                }

                XSSFSheet sheet     = entry.getValue();
                String    sheetName = sheet.getSheetName();

                if (sheetColumnMap.containsKey(sheetName)) {
                    List<String> sheetColumns = sheetColumnMap.get(sheetName);

                    if (sheetColumns == null) {
                        continue;
                    }

                    List<SheetColumn> columnList = new ArrayList<SheetColumn>();

                    for (String columnName : sheetColumns) {
                        SheetColumn sheetColumn = getSheetColumn(columnName);

                        if (sheetColumn == null) {
                            sheetColumn = handleUndefinedSheetColumn(columnName);
                        }

                        columnList.add(sheetColumn);
                    }

                    log.info("Processing sheet:" + sheetName);

                    List<XLSXTableModel> tableModels = processor.readTable(columnList.toArray(new SheetColumn[] {}),
                                                           sheet, this);

                    processedMap.put(sheetName, tableModels);
                    log.info("Processed Result: " + tableModels);
                } else {
                    continue;
                }
            }
        } finally {
            pkg.revert();
        }
    }

    //~--- methods ------------------------------------------------------------

    @Override
    public SheetColumn handleUndefinedSheetColumn(String sheetColName) {
        // TODO Auto-generated method stub
        return null;
    }

    //~--- get methods --------------------------------------------------------

    public Map<String, List<XLSXTableModel>> getProcessedMap() {
        return processedMap;
    }
}

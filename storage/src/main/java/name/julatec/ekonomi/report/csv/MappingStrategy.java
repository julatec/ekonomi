package name.julatec.ekonomi.report.csv;

import com.opencsv.bean.*;
import com.opencsv.exceptions.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MappingStrategy<T> extends HeaderColumnNameMappingStrategy<T> {

    private static final Logger logger = LoggerFactory.getLogger(MappingStrategy.class);

    public MappingStrategy(Class<T> type) {
        setType(type);
    }

    @Override
    public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        // overriding this method to allow us to preserve the header column name casing

        String[] header = super.generateHeader(bean);
        final int numColumns = header.length;
        if (numColumns == -1) {
            return header;
        }

        header = new String[numColumns];

        BeanField beanField;
        for (int i = 0; i < numColumns; i++) {
            beanField = findField(i);
            String columnHeaderName = extractHeaderName(beanField);
            header[i] = columnHeaderName;
        }
        return header;
    }


    @Override
    protected void loadFieldMap() throws CsvBadConverterException {
        // overriding this method to support setting column order by the custom `CsvBindByNameOrder` annotation
        if (writeOrder == null && type.isAnnotationPresent(CsvBindByNameOrder.class)) {
            setColumnOrderOnWrite(
                    new LiteralComparator<>(Arrays.stream(type.getAnnotation(CsvBindByNameOrder.class).value())
                            .map(String::toUpperCase).toArray(String[]::new)));
        }
        super.loadFieldMap();
    }

    private String extractHeaderName(final BeanField beanField) {
        if (beanField == null ||
                beanField.getField() == null ||
                beanField.getField().getDeclaredAnnotationsByType(CsvBindByName.class).length +
                        beanField.getField().getDeclaredAnnotationsByType(CsvCustomBindByName.class).length == 0) {
            return StringUtils.EMPTY;
        }

        if (beanField.getField().isAnnotationPresent(CsvBindByName.class)) {
            return beanField.getField().getDeclaredAnnotationsByType(CsvBindByName.class)[0].column();
        } else if (beanField.getField().isAnnotationPresent(CsvCustomBindByName.class)) {
            return beanField.getField().getDeclaredAnnotationsByType(CsvCustomBindByName.class)[0].column();
        }
        return StringUtils.EMPTY;

    }

    public T populateNewBean(Row line, List<FieldAccess<?>> fieldAccesses)
            throws
            CsvBeanIntrospectionException,
            CsvRequiredFieldEmptyException,
            CsvDataTypeMismatchException,
            CsvConstraintViolationException,
            CsvValidationException,
            IllegalAccessException,
            InvocationTargetException {

        final Map<Class<?>, Object> beanTree = createBean();
        final T subordinateBean = (T) beanTree.get(type);
        if (line.getRowNum() == 0) {
            int col = 0;
            for (String ignored : generateHeader(subordinateBean)) {
                fieldAccesses.add(new FieldAccess<>(findField(col++).getField()));
            }
            return null;
        }
        for (int col = 0; col < line.getLastCellNum(); col++) {
            final Cell cell = line.getCell(col);
            final BeanField<T, String> beanField = findField(col);
            if (beanField != null) {
                final XlsxFieldWriter writer = XlsxFieldWriter.getWriter(beanField.getField().getType());
                writer.read(subordinateBean, beanField, cell, fieldAccesses.get(col));
            }
        }
        return (T) beanTree.get(type);
    }

    public Stream<T> fromWorkbook(InputStream stream, BiConsumer<Row, Throwable> consumer) throws IOException {
        try (final Workbook workbook = new XSSFWorkbook(stream)) {
            final Sheet sheet = workbook.getSheetAt(0);
            final Iterable<Row> rows = sheet::rowIterator;
            final List<FieldAccess<?>> fieldAccesses = new ArrayList<>();
            return StreamSupport.stream(rows.spliterator(), false)
                    .flatMap(line -> {
                        try {
                            return Stream.ofNullable(populateNewBean(line, fieldAccesses));
                        } catch (Exception e) {
                            consumer.accept(line, e);
                        }
                        return Stream.empty();
                    });
        }
    }

    public Workbook toWorkbook(Iterable<T> iterable) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        return toWorkbook(iterable, null);
    }

    /**
     * El libro con las columnas pedidas, en el orden pedido.
     *
     * @param seleccion nombres de encabezado —los mismos que {@code @CsvBindByName}— en el
     *                  orden en que se quieren. {@code null} o vacía escribe todas en el orden
     *                  de {@code @CsvBindByNameOrder}, que es el comportamiento de siempre.
     */
    public Workbook toWorkbook(Iterable<T> iterable, List<String> seleccion)
            throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        int rowCount = 0;
        final Workbook workbook = new XSSFWorkbook();
        final Map<XlsxFieldWriter, CellStyle> styleMap = XlsxFieldWriter.getStyles(workbook);
        Sheet sheet = null;
        // Índice en el encabezado completo de cada columna que se escribe, en el orden pedido:
        // la posición en el libro y la posición del campo en el bean dejan de coincidir en
        // cuanto alguien reordena, y `findField` sigue hablando de la segunda.
        int[] columnas = null;
        for (T bean : iterable) {
            if (rowCount == 0) {
                sheet = workbook.createSheet(bean.getClass().getSimpleName());
                int columnCount = 0;
                final Row row = sheet.createRow(rowCount++);
                final String[] headers = generateHeader(bean);
                columnas = columnasPedidas(headers, seleccion);
                for (int indice : columnas) {
                    final Cell cell = row.createCell(columnCount++, CellType.STRING);
                    cell.setCellValue(headers[indice]);
                }
            }
            final Row row = sheet.createRow(rowCount++);
            for (int column = 0; column < columnas.length; column++) {
                final BeanField<T, String> beanField = findField(columnas[column]);
                final Object value = beanField.getFieldValue(bean);
                final XlsxFieldWriter writer = XlsxFieldWriter.getWriter(value);
                writer.write(writer == XlsxFieldWriter.DefaultWriter ? beanField.write(bean, "")[0] : value,
                        row, column, styleMap.get(writer));
            }
        }
        return workbook;
    }

    /**
     * Traduce nombres de encabezado a posiciones, descartando los que este bean no tiene.
     * <p>
     * Una selección que no reconoce ninguna columna cae al libro completo, y no a uno vacío: un
     * archivo sin ninguna columna no se distingue de un reporte sin datos. Queda en el log
     * porque es señal de una preferencia guardada contra una versión anterior del bean —caso
     * que {@code ColumnasDeReporte.reconciliar} ya filtra antes de llegar acá—.
     */
    private int[] columnasPedidas(String[] headers, List<String> seleccion) {
        if (seleccion == null || seleccion.isEmpty()) {
            return IntStream.range(0, headers.length).toArray();
        }
        final Map<String, Integer> porNombre = new LinkedHashMap<>();
        for (int i = 0; i < headers.length; i++) {
            porNombre.putIfAbsent(headers[i], i);
        }
        final int[] columnas = seleccion.stream()
                .map(porNombre::get)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .toArray();
        if (columnas.length == 0) {
            logger.warn("Ninguna de las columnas pedidas {} existe en {}; se escriben todas.",
                    seleccion, type.getSimpleName());
            return IntStream.range(0, headers.length).toArray();
        }
        return columnas;
    }

    private enum XlsxFieldWriter {

        StringWriter(String.class) {
            @Override
            public void write(Object value, Row row, int column, CellStyle style) {
                Cell dateCell = row.createCell(column, CellType.STRING);
                dateCell.setCellValue((String) value);
                dateCell.setCellStyle(style);
            }

            @Override
            public CellStyle getStyle(Workbook workbook) {
                final CellStyle dateStyle = workbook.createCellStyle();
                return dateStyle;
            }

            @Override
            public <T2> void read(T2 subordinateBean, BeanField<T2, String> beanField, Cell cell, FieldAccess<?> fieldAccess) throws CsvDataTypeMismatchException, CsvConstraintViolationException, CsvRequiredFieldEmptyException, CsvValidationException, IllegalAccessException, InvocationTargetException {
                switch (cell.getCellType()) {
                    case ERROR:
                    case _NONE:
                    case BLANK:


                        fieldAccess.setField(subordinateBean, null);
                        break;
                    case NUMERIC:
                        ((FieldAccess<String>) fieldAccess).setField(subordinateBean, String.valueOf((long) cell.getNumericCellValue()));
                        break;
                    default:
                        ((FieldAccess<String>) fieldAccess).setField(subordinateBean, cell.getStringCellValue());
                        break;
                }
            }
        },
        IntegerWriter(Integer.class) {
            @Override
            public void write(Object value, Row row, int column, CellStyle style) {
                Cell dateCell = row.createCell(column, CellType.NUMERIC);
                dateCell.setCellValue(((Number) value).doubleValue());
                dateCell.setCellStyle(style);
            }

            @Override
            public CellStyle getStyle(Workbook workbook) {
                final CellStyle dateStyle = workbook.createCellStyle();
                dateStyle.setDataFormat((short) 1);
                return dateStyle;
            }

            @Override
            public <T2> void read(T2 subordinateBean, BeanField<T2, String> beanField, Cell cell, FieldAccess<?> fieldAccess) throws CsvDataTypeMismatchException, CsvConstraintViolationException, CsvRequiredFieldEmptyException, CsvValidationException, IllegalAccessException, InvocationTargetException {
                switch (cell.getCellType()) {
                    case ERROR:
                    case _NONE:
                    case BLANK:
                        fieldAccess.setField(subordinateBean, null);
                        break;
                    default:
                        ((FieldAccess<Integer>) fieldAccess).setField(subordinateBean, Double.valueOf(cell.getNumericCellValue()).intValue());
                        break;
                }
            }
        },
        LongWriter(Long.class) {
            @Override
            public void write(Object value, Row row, int column, CellStyle style) {
                Cell dateCell = row.createCell(column, CellType.NUMERIC);
                dateCell.setCellValue(((Number) value).doubleValue());
                dateCell.setCellStyle(style);
            }

            @Override
            public CellStyle getStyle(Workbook workbook) {
                final CellStyle style = workbook.createCellStyle();
                style.setDataFormat((short) 1);
                return style;
            }

            @Override
            public <T2> void read(T2 subordinateBean, BeanField<T2, String> beanField, Cell cell, FieldAccess<?> fieldAccess) throws CsvDataTypeMismatchException, CsvConstraintViolationException, CsvRequiredFieldEmptyException, CsvValidationException, IllegalAccessException, InvocationTargetException {
                switch (cell.getCellType()) {
                    case ERROR:
                    case _NONE:
                    case BLANK:
                        fieldAccess.setField(subordinateBean, null);
                        break;
                    default:
                        ((FieldAccess<Long>) fieldAccess).setField(subordinateBean, Double.valueOf(cell.getNumericCellValue()).longValue());
                        break;
                }
            }
        },
        DoubleWriter(Double.class) {
            @Override
            public void write(Object value, Row row, int column, CellStyle style) {
                Cell cell = row.createCell(column, CellType.NUMERIC);
                cell.setCellStyle(style);
                cell.setCellValue(((Number) value).doubleValue());
            }

            @Override
            public CellStyle getStyle(Workbook workbook) {
                final CellStyle style = workbook.createCellStyle();
                final DataFormat format = workbook.createDataFormat();
                style.setDataFormat(format.getFormat("# ###,00"));
                return style;
            }

            @Override
            public <T2> void read(T2 subordinateBean, BeanField<T2, String> beanField, Cell cell, FieldAccess<?> fieldAccess) throws CsvDataTypeMismatchException, CsvConstraintViolationException, CsvRequiredFieldEmptyException, CsvValidationException, IllegalAccessException, InvocationTargetException {
                switch (cell.getCellType()) {
                    case ERROR:
                    case _NONE:
                    case BLANK:
                        fieldAccess.setField(subordinateBean, null);
                        break;
                    default:
                        ((FieldAccess<Double>) fieldAccess).setField(subordinateBean, cell.getNumericCellValue());
                        break;
                }
            }
        },
        BigDecimalWriter(BigDecimal.class) {
            @Override
            public void write(Object value, Row row, int column, CellStyle style) {
                Cell cell = row.createCell(column, CellType.NUMERIC);
                cell.setCellStyle(style);
                cell.setCellValue(((Number) value).doubleValue());
            }

            @Override
            public CellStyle getStyle(Workbook workbook) {
                final CellStyle style = workbook.createCellStyle();
                final DataFormat format = workbook.createDataFormat();
                style.setDataFormat(format.getFormat("####,00;[RED]-####,00"));
                return style;
            }

            @Override
            public <T2> void read(T2 subordinateBean, BeanField<T2, String> beanField, Cell cell, FieldAccess<?> fieldAccess) throws CsvDataTypeMismatchException, CsvConstraintViolationException, CsvRequiredFieldEmptyException, CsvValidationException, IllegalAccessException, InvocationTargetException {
                switch (cell.getCellType()) {
                    case ERROR:
                    case _NONE:
                    case BLANK:
                        fieldAccess.setField(subordinateBean, null);
                        break;
                    case STRING:
                        final String text = cell.getStringCellValue();
                        final double doubleValue = Double.valueOf(text);
                        BigDecimal bigDecimal = BigDecimal.valueOf(doubleValue);
                        ((FieldAccess<BigDecimal>) fieldAccess).setField(subordinateBean, bigDecimal.compareTo(BigDecimal.ZERO) != 0 ? bigDecimal : null);
                        break;
                    default:
                        ((FieldAccess<BigDecimal>) fieldAccess).setField(subordinateBean, BigDecimal.valueOf(cell.getNumericCellValue()));
                        break;
                }
            }
        },
        DateWriter(Date.class) {
            @Override
            public void write(Object value, Row row, int column, CellStyle style) {
                Cell cell = row.createCell(column, CellType.NUMERIC);
                cell.setCellValue((Date) value);
                cell.setCellStyle(style);
            }

            @Override
            public CellStyle getStyle(Workbook workbook) {
                final CellStyle style = workbook.createCellStyle();
                final DataFormat format = workbook.createDataFormat();
                style.setDataFormat(format.getFormat("m/d/yy"));
                return style;
            }

            @Override
            public <T2> void read(T2 subordinateBean, BeanField<T2, String> beanField, Cell cell, FieldAccess<?> fieldAccess) throws CsvDataTypeMismatchException, CsvConstraintViolationException, CsvRequiredFieldEmptyException, CsvValidationException, IllegalAccessException, InvocationTargetException {
                switch (cell.getCellType()) {
                    case NUMERIC:
                        ((FieldAccess<Date>) fieldAccess).setField(subordinateBean, cell.getDateCellValue());
                        break;
                    default:
                        fieldAccess.setField(subordinateBean, null);
                        break;
                }
            }
        },
        BooleanWriter(Boolean.class) {
            @Override
            public void write(Object value, Row row, int column, CellStyle style) {
                Cell cell = row.createCell(column, CellType.BOOLEAN);
                cell.setCellValue((Boolean) value);
                cell.setCellStyle(style);
            }

            @Override
            public CellStyle getStyle(Workbook workbook) {
                final CellStyle style = workbook.createCellStyle();
                final DataFormat format = workbook.createDataFormat();
                style.setDataFormat(format.getFormat("BOOLEAN"));
                return style;
            }

            @Override
            public <T2> void read(T2 subordinateBean, BeanField<T2, String> beanField, Cell cell, FieldAccess<?> fieldAccess) throws CsvDataTypeMismatchException, CsvConstraintViolationException, CsvRequiredFieldEmptyException, CsvValidationException, IllegalAccessException, InvocationTargetException {
                switch (cell.getCellType()) {
                    case ERROR:
                    case _NONE:
                    case BLANK:
                        fieldAccess.setField(subordinateBean, null);
                        break;
                    default:
                        ((FieldAccess<Boolean>) fieldAccess).setField(subordinateBean, cell.getBooleanCellValue());
                        break;
                }
            }
        },
        DefaultWriter(Object.class) {
            @Override
            public void write(Object value, Row row, int column, CellStyle style) {
                if (value == null) {
                    row.createCell(column, CellType.BLANK);
                } else {
                    Cell cell = row.createCell(column, CellType.STRING);
                    cell.setCellValue(String.valueOf(value));
                    cell.setCellStyle(style);
                }
            }

            @Override
            public CellStyle getStyle(Workbook workbook) {
                final CellStyle dateStyle = workbook.createCellStyle();
                dateStyle.setDataFormat((short) 28);
                return dateStyle;
            }

            @Override
            public <T2> void read(T2 subordinateBean, BeanField<T2, String> beanField, Cell cell, FieldAccess<?> fieldAccess) throws CsvDataTypeMismatchException, CsvConstraintViolationException, CsvRequiredFieldEmptyException, CsvValidationException, IllegalAccessException {
                switch (cell.getCellType()) {
                    case ERROR:
                    case _NONE:
                    case BLANK:
                        beanField.getField().set(subordinateBean, null);
                        break;
                    default:
                        beanField.setFieldValue(subordinateBean, cell.getStringCellValue(), "");
                        break;
                }
            }
        };

        final Class<?> targetClass;

        XlsxFieldWriter(Class<?> targetClass) {
            this.targetClass = targetClass;
        }

        public static Map<XlsxFieldWriter, CellStyle> getStyles(Workbook workbook) {
            Map<XlsxFieldWriter, CellStyle> result = new TreeMap<>();
            for (XlsxFieldWriter writer : values()) {
                result.put(writer, writer.getStyle(workbook));
            }
            return result;
        }

        public static XlsxFieldWriter getWriter(Class<?> clazz) {
            for (XlsxFieldWriter writer : values()) {
                if (writer.targetClass.isAssignableFrom(clazz)) {
                    return writer;
                }
            }
            return DefaultWriter;
        }

        public static XlsxFieldWriter getWriter(Object value) {
            if (value == null) {
                return DefaultWriter;
            }
            return getWriter(value.getClass());
        }

        public abstract void write(Object value, Row row, int column, CellStyle style);

        public abstract CellStyle getStyle(Workbook workbook);

        public abstract <T2> void read(T2 subordinateBean, BeanField<T2, String> beanField, Cell cell, FieldAccess<?> fieldAccess) throws CsvDataTypeMismatchException, CsvConstraintViolationException, CsvRequiredFieldEmptyException, CsvValidationException, IllegalAccessException, InvocationTargetException;
    }

}
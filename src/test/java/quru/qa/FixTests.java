
package quru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FixTests {
    ClassLoader cl = FixTests.class.getClassLoader();

    @Test
    void zipTestCsv() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("files.zip");
             ZipInputStream zis = new ZipInputStream(stream)) {

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("csv")) {
                    CSVReader csvReader = new CSVReader(new InputStreamReader(zis));
                    List<String[]> data = csvReader.readAll();

                    Assertions.assertEquals(3, data.size());

                    Assertions.assertArrayEquals(
                            new String[]{"1", "202699", "1284922"},
                            data.get(0));
                    Assertions.assertArrayEquals(
                            new String[]{"2", "202698", "578399"},
                            data.get(1));
                    Assertions.assertArrayEquals(
                            new String[]{"3", "202697", "58933"},
                            data.get(2));
                    System.out.println("Содержимое CSV успешно проверено");
                }
            }
        }
    }

    @Test
    void zipTestXlsx() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("qa guru.zip");
             ZipInputStream zis = new ZipInputStream(stream)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("xlsx")) {
                    XLS xls = new XLS(zis);
                    final String nickname = xls.excel.getSheetAt(1).getRow(3).getCell(1).getStringCellValue();
                    final int sheetCount = xls.excel.getNumberOfSheets();

                    //check sheet#2, humster's nickname
                    Assertions.assertEquals("Orange", nickname);

                    //check number of sheets
                    Assertions.assertEquals(2, sheetCount);
                }
            }
        }
    }

    @Test
    void zipTestPdf() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("files.zip");
             ZipInputStream zis = new ZipInputStream(stream)) {

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("pdf")) {
                    PDF pdf = new PDF(zis);

                    //check number of pages
                    Assertions.assertEquals(179, pdf.numberOfPages);

                    //check creator
                    Assertions.assertEquals("Microsoft® Word 2019", pdf.creator);

                    //check title
                    Assertions.assertEquals("Article about cats", pdf.title);
                }
            }
        }
    }

@Test
    void ZipFileParsingTestXls() throws Exception {
        try (InputStream is = cl.getResourceAsStream("files.zip");
             ZipInputStream zis = new ZipInputStream(is)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xlsx")) {
                    // Создайте новый InputStream для текущего файла
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) != -1) {
                            baos.write(buffer, 0, len);
                        }

                        try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
                            // Создайте объект XLS из нового InputStream
                            XLS xls = new XLS(bais);
                            final String actualValue = xls.excel.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
                            Assertions.assertTrue(actualValue.contains("Номер заказа"));
                            System.out.println("Содержимое XLS успешно проверено");
                        }
                    }
                }
            }
        }
    }
}


package quru.qa;

import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import quru.qa.model.Book;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZipFileAndJacksonTest {
    private final ClassLoader cl = ZipFileAndJacksonTest.class.getClassLoader();


    @Test
    void ZipFileParsingTestXls() throws Exception {
        try (InputStream is = cl.getResourceAsStream("files.zip");
             ZipInputStream zis = new ZipInputStream(is)) {
            assert is != null;
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains(".xlsx")) {
                    XLS xls = new XLS(zis);
                    final String actualValue = xls.excel.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
                    Assertions.assertTrue(actualValue.contains("Номер заказа"));
                }
            }
        }
    }


    @Test
    void ZipFileParsingPdfTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("files.zip")) {
            if (is == null) {
                throw new RuntimeException("Файл не найден");
            }
            try (ZipInputStream zis = new ZipInputStream(is)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String fileName = entry.getName();
                    System.out.println("Начало обработки файла: " + fileName);
                    if (fileName.endsWith(".pdf") && !fileName.startsWith("__MACOSX/")) {
                        try (PDDocument document = PDDocument.load(zis)) {
                            PDFTextStripper pdfStripper = new PDFTextStripper();
                            String text = pdfStripper.getText(document);
                            Assertions.assertTrue(text.contains("JUnit 5 User Guide"));
                            System.out.println("Содержимое PDF успешно проверено");
                        } catch (Exception e) {
                            throw new RuntimeException("Ошибка чтения файла PDF", e);
                        }
                    } else {
                        System.out.println("Пропуск файла: " + fileName);
                    }
                }
            }
        }
    }

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
    void jsonJacksonParsingTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream is = cl.getResourceAsStream("book.json");
             Reader reader = new InputStreamReader(is)) {
            Book book = objectMapper.readValue(reader, Book.class);
            assertEquals("Война и мир", book.getTitle());
            assertEquals("Лев Толстой", book.getAuthor());
            assertEquals(1869, book.getYear());
            assertEquals(List.of("Роман", "Исторический", "Философский"), book.getGenres());
            assertEquals(true, book.getAvailable());
        }
    }
}



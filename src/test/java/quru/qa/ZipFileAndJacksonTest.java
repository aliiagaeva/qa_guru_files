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
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZipFileAndJacksonTest {
    private final ClassLoader cl = ZipFileAndJacksonTest.class.getClassLoader();


    @Test
    void ZipFileParsingTestXls() throws Exception {
        try (InputStream is = cl.getResourceAsStream("qa guru.zip");
             ZipInputStream zis = new ZipInputStream(is)) {
            assert is != null;
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains(".xlsx")) {
                    XLS xls = new XLS(zis);
                    final String actualValue = xls.excel.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
                    Assertions.assertTrue(actualValue.contains("username"));
                }
            }
        }
    }


    @Test
    void ZipFileParsingPdfTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("qa guru.zip")) {
            if (is == null) {
                throw new RuntimeException("Файл не найден");
            }
            try (ZipInputStream zis = new ZipInputStream(is)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String fileName = entry.getName();
                    System.out.println("Начало обработки файла: " + fileName);
                    if (fileName.endsWith(".pdf")) {
                        try (PDDocument document = PDDocument.load(zis)) {
                            PDFTextStripper pdfStripper = new PDFTextStripper();
                            String text = pdfStripper.getText(document);
                            Assertions.assertTrue(text.contains("Where do cats come from?"));
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
        try (InputStream stream = cl.getResourceAsStream("qa guru.zip");
             ZipInputStream zis = new ZipInputStream(stream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("csv")) {
                    CSVReader csvReader = new CSVReader(new InputStreamReader(zis));
                    List<String[]> data = csvReader.readAll();
                    Assertions.assertEquals(3, data.size());
                    Assertions.assertArrayEquals(
                            new String[]{"Animal", "cat"},
                            Arrays.stream(data.get(0)).map(String::trim).toArray());
                    Assertions.assertArrayEquals(
                            new String[]{"Season", "summer"},
                            Arrays.stream(data.get(1)).map(String::trim).toArray());
                    Assertions.assertArrayEquals(
                            new String[]{"Food", "cheese"},
                            Arrays.stream(data.get(2)).map(String::trim).toArray());
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



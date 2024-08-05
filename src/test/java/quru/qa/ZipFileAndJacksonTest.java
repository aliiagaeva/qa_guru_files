package quru.qa;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private ClassLoader cl = SelenideFilesTests.class.getClassLoader();

    @Test
    void ZipFileParsingTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("testfile.zip")) {
            assert is != null;
            try (ZipInputStream zis = new ZipInputStream(is)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    System.out.println(entry.getName());
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

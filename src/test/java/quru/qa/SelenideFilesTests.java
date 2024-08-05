package quru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.selenide.Configuration;
import com.codeborne.xlstest.XLS;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import quru.qa.model.Glossary;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class SelenideFilesTests {
    private ClassLoader cl = SelenideFilesTests.class.getClassLoader();
    private static final Gson gson = new Gson();
    @Test
    void downloadFileTest() throws IOException {
        Configuration.timeout = 30000;
        Configuration.pageLoadStrategy = "eager";
        open("https://github.com/junit-team/junit5/blob/main/README.md");
        File downloaded = $(".react-blob-header-edit-and-raw-actions [href*='main/README.md']").download();
        String dataAsString = FileUtils.readFileToString(downloaded, StandardCharsets.UTF_8);
    }

    @Test
    void uploadFileTest(){
        open("https://fineuploader.com/demos.html");
        $("input[type='file']").uploadFromClasspath("ron.jpeg");
        $(".qq-file-id-0").shouldHave(text("ron.jpeg"));
    }

    @Test
    void pdfParsingTest() throws Exception {
        open("https://junit.org/junit5/docs/current/user-guide/");
        File downloaded = $("[href*='junit-user-guide-5.10.3.pdf']").download();
        PDF pdf = new PDF(downloaded);
        System.out.println();
        }

    @Test
    void xlsFileParsingTest() throws Exception {
        open("https://excelvba.ru/programmes/Teachers");
        File downloaded = $("[href*='https://ExcelVBA.ru/sites/default/files/teachers.xls']").download();
        XLS xls = new XLS(downloaded);
        String actualValue = xls.excel.getSheetAt(0).getRow(3).getCell(2).getStringCellValue();
        Assertions.assertTrue(actualValue.contains("Суммарное количество"));
    }

    @Test
    void csvFoleParsingTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("table.csv");
            CSVReader csvReader = new CSVReader(new InputStreamReader(is))){
            List<String[]> data = csvReader.readAll();
            Assertions.assertEquals(3, data.size());
            Assertions.assertArrayEquals(
                    new String[] {"1","202699","1284922"},
                    data.get(0));
            Assertions.assertArrayEquals(
                    new String[] {"2","202698","578399"},
                    data.get(1));
            Assertions.assertArrayEquals(
                    new String[] {"3","202697","58933"},
                    data.get(2));
        }
    }

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
    void jsonParsingTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("simple.json");
             Reader reader = new InputStreamReader(is)) {
            JsonObject actual =  gson.fromJson(reader, JsonObject.class);
            Assertions.assertEquals("example glossary", actual.get("title").getAsString());
            Assertions.assertEquals(123, actual.get("ID").getAsInt());
            JsonObject inner = actual.get("glossary").getAsJsonObject();
            Assertions.assertEquals("SGML", inner.get("SortAs").getAsString());
            Assertions.assertEquals("Standard Generalized Markup Language", inner.get("GlossTerm").getAsString());
            Assertions.assertEquals("SGML", inner.get("Acronym").getAsString());

        }
    }

    @Test
    void jsonParsingImprovedTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("simple.json");
             Reader reader = new InputStreamReader(is)) {
            Glossary actual =  gson.fromJson(reader, Glossary.class);
            Assertions.assertEquals("example glossary", actual.getTitle());
            Assertions.assertEquals(123, actual.getID());
            Assertions.assertEquals("SGML", actual.getGlossary().getSortAs());
            Assertions.assertEquals("Standard Generalized Markup Language", actual.getGlossary().getGlossTerm());
            Assertions.assertEquals("SGML", actual.getGlossary().getAcronym());
        }
    }
}

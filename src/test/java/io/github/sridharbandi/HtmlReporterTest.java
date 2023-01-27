package io.github.sridharbandi;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HtmlReporterTest {

    @Test
    public void testAxeReport() {
        String[] args = {"-j", "./src/test/resources/reports", "-e", "axe", "-o", "./target/html-reports/"};
        HtmlReporter.main(args);
        assertTrue(FileUtils.deleteQuietly(new File("./target/html-reports/axe/html/id_lddcwwgze3uqx.html")));
        assertTrue(FileUtils.deleteQuietly(new File("./target/html-reports/axe/html/index.html")));
    }

    @Test
    public void testHtmlcsReport() {
        String[] args = {"-j", "./src/test/resources/reports", "-e", "htmlcs", "-o", "./target/html-reports/"};
        HtmlReporter.main(args);
        assertTrue(FileUtils.deleteQuietly(new File("./target/html-reports/htmlcs/html/id_lddcwwph00vnv.html")));
        assertTrue(FileUtils.deleteQuietly(new File("./target/html-reports/htmlcs/html/index.html")));
    }
}

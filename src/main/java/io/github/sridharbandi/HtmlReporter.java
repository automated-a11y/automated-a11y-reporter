package io.github.sridharbandi;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.github.sridharbandi.reporter.ftl.FtlConfig;
import io.github.sridharbandi.reporter.modal.axe.Issues;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;

@CommandLine.Command(name = "Automated a11y HTML Reporter", mixinStandardHelpOptions = true, version = "v1.0.1", description = "Generates Accessibility HTML reports out from JSON created by the Organisation https://github.com/automated-a11y")
public class HtmlReporter implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(HtmlReporter.class);

    @CommandLine.Option(names = {"-j", "--jsondir"}, required = true, description = "JSON reports path")
    protected String jsonDir;

    @CommandLine.Option(names = {"-e", "--engine"}, required = true, description = "Engine name - axe/htmlcs")
    protected String engine;

    @CommandLine.Option(names = {"-o", "--outputdir"}, description = "Output directory to generate HTML reports")
    protected String outputDir;

    public static void main(String[] args) {
        new CommandLine(new HtmlReporter()).execute(args);
    }

    @Override
    public void run() {
        try {
            Template tmplIndex = FtlConfig.getInstance().getTemplate(engine.toLowerCase() + "/index.ftl");
            Template tmplPage = FtlConfig.getInstance().getTemplate(engine.toLowerCase() + "/page.ftl");
            Class<?> clazz = engine.equalsIgnoreCase("axe") ?
                    Issues.class :
                    io.github.sridharbandi.reporter.modal.htmlcs.Issues.class;
            List<?> issuesList = jsonReports(clazz);

            issuesList.forEach(issues -> {
                String id = engine.equalsIgnoreCase("axe") ?
                        ((Issues) issues).getId() :
                        ((io.github.sridharbandi.reporter.modal.htmlcs.Issues) issues).getId();
                save(tmplPage, issues, id);
            });

            Map<String, Object> root = new HashMap<>();
            root.put("list", issuesList);
            root.put("title", "Accessibility Report");
            save(tmplIndex, root, "index");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<?> jsonReports(Class<?> clazz) throws IOException {
        try (Stream<Path> list = walk(get(jsonDir + "/" + engine.toLowerCase() + "/json/"))) {
            return list.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(file -> FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("json"))
                    .map(file -> {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            return mapper.readValue(file, clazz);
                        } catch (IOException e) {
                            e.printStackTrace();
                            LOG.error("Failed to read json file {}", file.getAbsolutePath());
                        }
                        return null;
                    }).collect(Collectors.toList());
        }
    }

    private void save(Template tmpl, Object map, String name) {
        String reportPath = outputDir == null ? "./html-reports/" : outputDir;
        Path path = null;
        File report;
        try {
            path = get(reportPath + engine + "/html");
            createDirectories(path);
            report = new File(path + File.separator + name + ".html");
            Writer file = new FileWriter(report);
            tmpl.process(map, file);
            file.flush();
            file.close();
            String loggerMsg = name.equalsIgnoreCase("index") ? "Consoliated " : "Page ";
            LOG.info(loggerMsg + "report generated at " + report.getAbsolutePath());
        } catch (IOException e) {
            LOG.error("unable to write file: " + path + File.separator + name);
            e.printStackTrace();
        } catch (TemplateException e) {
            LOG.error("unable to find template: " + tmpl + " for " + name);
            e.printStackTrace();
        }
    }

}
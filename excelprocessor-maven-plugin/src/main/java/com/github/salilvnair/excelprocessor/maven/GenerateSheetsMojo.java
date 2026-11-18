package com.github.salilvnair.excelprocessor.maven;

import com.github.salilvnair.excelprocessor.v2.outline.OutlineOptions;
import com.github.salilvnair.excelprocessor.v2.outline.SheetOutline;
import com.github.salilvnair.excelprocessor.v2.outline.WorkbookOutline;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates an annotated {@code @Sheet} class for every sheet of the sample workbooks (XLSX, XLS)
 * and CSV/TSV files in {@code sourceDirectory}, and adds the output directory to the compile
 * sources.
 *
 * <pre>{@code
 * <plugin>
 *   <groupId>com.github.salilvnair</groupId>
 *   <artifactId>excelprocessor-maven-plugin</artifactId>
 *   <executions><execution><goals><goal>generate-sheets</goal></goals></execution></executions>
 *   <configuration><packageName>com.acme.sheets</packageName></configuration>
 * </plugin>
 * }</pre>
 */
@Mojo(name = "generate-sheets", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class GenerateSheetsMojo extends AbstractMojo {
    private static final List<String> EXTENSIONS = Arrays.asList(".xlsx", ".xlsm", ".xls", ".csv", ".tsv");

    /** Folder with the sample files; searched recursively. */
    @Parameter(property = "excelprocessor.sourceDirectory", defaultValue = "${project.basedir}/src/main/excel")
    File sourceDirectory;

    @Parameter(property = "excelprocessor.outputDirectory", defaultValue = "${project.build.directory}/generated-sources/excelprocessor")
    File outputDirectory;

    /** Package of the generated classes. */
    @Parameter(property = "excelprocessor.packageName", required = true)
    String packageName;

    /** Generate Lombok {@code @Getter}/{@code @Setter} instead of accessor methods (the project needs Lombok). */
    @Parameter(property = "excelprocessor.lombok", defaultValue = "false")
    boolean lombok;

    /** One based header row; -1 uses the first non-empty row of each sheet. */
    @Parameter(property = "excelprocessor.headerRow", defaultValue = "-1")
    int headerRow;

    /** Rows sampled per sheet to infer field types. */
    @Parameter(property = "excelprocessor.sampleRows", defaultValue = "1000")
    int sampleRows;

    /** Only generate these sheet names (all sheets when empty). */
    @Parameter
    List<String> sheets;

    @Parameter(property = "excelprocessor.skip", defaultValue = "false")
    boolean skip;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("excelprocessor: generation skipped");
            return;
        }
        if (project != null) {
            project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        }
        if (sourceDirectory == null || !sourceDirectory.isDirectory()) {
            getLog().info("excelprocessor: no sample files, " + sourceDirectory + " does not exist");
            return;
        }
        List<Path> files;
        try (Stream<Path> walk = Files.walk(sourceDirectory.toPath())) {
            files = walk.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().startsWith("~$"))
                    .filter(p -> EXTENSIONS.stream().anyMatch(ext -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(ext)))
                    .sorted()
                    .collect(Collectors.toList());
        }
        catch (IOException e) {
            throw new MojoExecutionException("Cannot list " + sourceDirectory, e);
        }
        OutlineOptions options = OutlineOptions.defaults().headerRow(headerRow).sampleRows(sampleRows);
        Path packageDir = outputDirectory.toPath().resolve(packageName.replace('.', File.separatorChar));
        Map<String, String> generatedFrom = new HashMap<>();
        int written = 0;
        int unchanged = 0;
        for (Path file : files) {
            String relative = sourceDirectory.toPath().relativize(file).toString().replace(File.separatorChar, '/');
            WorkbookOutline outline;
            try {
                outline = ExcelSheetReaderFactory.outline(file.toFile(), options);
            }
            catch (RuntimeException e) {
                throw new MojoExecutionException("Cannot read " + relative + ": " + e.getMessage(), e);
            }
            for (SheetOutline sheet : outline.sheets()) {
                if (sheet.isEmpty() || (sheets != null && !sheets.isEmpty() && !sheets.contains(sheet.name()))) {
                    continue;
                }
                String className = SheetSourceGenerator.className(sheet.name());
                String previous = generatedFrom.putIfAbsent(className, relative + " (" + sheet.name() + ")");
                if (previous != null) {
                    throw new MojoExecutionException("Class " + className + " would be generated from both " + previous
                            + " and " + relative + " (" + sheet.name() + "); rename a sheet or use <sheets>");
                }
                String source = SheetSourceGenerator.generate(sheet, packageName, className, relative, lombok);
                try {
                    if (writeIfChanged(packageDir.resolve(className + ".java"), source)) {
                        written++;
                    }
                    else {
                        unchanged++;
                    }
                }
                catch (IOException e) {
                    throw new MojoExecutionException("Cannot write " + className, e);
                }
            }
        }
        getLog().info("excelprocessor: " + written + " sheet class(es) generated, " + unchanged + " unchanged, in " + packageDir);
    }

    /** Leaves unchanged files alone so incremental compilation is not triggered. */
    private static boolean writeIfChanged(Path target, String source) throws IOException {
        if (Files.exists(target) && Files.readString(target, StandardCharsets.UTF_8).equals(source)) {
            return false;
        }
        Files.createDirectories(target.getParent());
        Files.writeString(target, source, StandardCharsets.UTF_8);
        return true;
    }
}

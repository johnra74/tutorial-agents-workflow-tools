package me.johnra.tutorial.agent;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class CodebaseTools {

    private final String projectRoot;

    public CodebaseTools(@Value("${project.root:./}") String projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Tool("Search the codebase for files containing a keyword or pattern. Returns matching file paths and line numbers.")
    public String searchCodebase(
            @P("The search term or regex pattern") String query,
            @P("Optional file extension filter such as .java — pass empty string to search all files") String fileExtension) {
        try {
            List<String> cmd = (fileExtension == null || fileExtension.isBlank())
                    ? List.of("grep", "-rn", query, ".")
                    : List.of("grep", "-rn", "--include", "*" + fileExtension, query, ".");

            Process process = new ProcessBuilder(cmd)
                    .directory(Path.of(projectRoot).toFile())
                    .redirectErrorStream(true)
                    .start();

            String output = new String(process.getInputStream().readAllBytes());
            if (output.isBlank()) return "No matches found for '" + query + "'";

            return output.length() > 4000 ? output.substring(0, 4000) + "\n[truncated]" : output;
        } catch (IOException e) {
            return "Search failed: " + e.getMessage();
        }
    }

    @Tool("Read the contents of a file at the given path.")
    public String readFile(
            @P("Absolute or relative path to the file") String path,
            @P("Line number to start reading from — 0 means beginning") int startLine,
            @P("Line number to stop reading at — 0 means end of file") int endLine) {
        try {
            Path filePath = Path.of(projectRoot).resolve(path);
            List<String> lines = Files.readAllLines(filePath);

            int from = startLine > 0 ? Math.min(startLine - 1, lines.size()) : 0;
            int to = endLine > 0 ? Math.min(endLine, lines.size()) : lines.size();
            List<String> slice = lines.subList(from, Math.min(to, from + 200));

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < slice.size(); i++) {
                sb.append(from + i + 1).append(": ").append(slice.get(i)).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return "File not found or unreadable: " + path;
        }
    }

    @Tool("Run the test suite (or a specific test class) and return the output.")
    public String runTests(
            @P("Name of a specific test class, or empty string to run all tests") String testPath) {
        try {
            List<String> cmd = (testPath == null || testPath.isBlank())
                    ? List.of("./mvnw", "test", "-q")
                    : List.of("./mvnw", "test", "-q", "-Dtest=" + testPath);

            Process process = new ProcessBuilder(cmd)
                    .directory(Path.of(projectRoot).toFile())
                    .redirectErrorStream(true)
                    .start();
            process.waitFor();

            String output = new String(process.getInputStream().readAllBytes());
            return output.length() > 3000
                    ? "...[truncated]\n" + output.substring(output.length() - 3000)
                    : output;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Test run failed: " + e.getMessage();
        }
    }
}

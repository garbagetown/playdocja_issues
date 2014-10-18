package playdocja;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public class IssueRegister {

    private static Logger logger = Logger.getLogger(IssueRegister.class.getName());
    private static String baseurl = "https://github.com/garbagetown/playdocja/blob/2.2.0/documentation";

    public static final String MARKDOWN_EXT = ".md";
    
    public static final String LABEL_UPDATE = "update";
    public static final String LABEL_NEW = "new";

    /**
     * 
     * @param basepath
     * @param olddir
     * @param newdir
     * @return
     */
    public List<Issue> getDiffsAsIssues(Path basepath, String olddir, String newdir) {

        Path oldpath = basepath.resolve(olddir).toAbsolutePath();
        Path newpath = basepath.resolve(newdir).toAbsolutePath();

        String command = String.format("diff -qr %s %s", oldpath, newpath);
        logger.debug(command);

        List<String> results = execute(command);

        List<Issue> issues = new ArrayList<Issue>();
        for (String result : results) {
            logger.debug(result);

            Pattern p = Pattern.compile(String.format("^Files %s/(.+) and %s/(.+) differ$", oldpath, newpath));
            Matcher m = p.matcher(result);
            if (m.find()) {
                String path = m.group(1);
                if (path.endsWith(MARKDOWN_EXT)) {
                    issues.add(toUpdate(oldpath, newpath, path));
                }
                continue;
            }
            
            p = Pattern.compile(String.format("^Only in %s/(.+)", newpath));
            m = p.matcher(result.replace(": ", "/"));
            if (m.find()) {
                String path = m.group(1);
                issues.addAll(toNew(newpath, path));
                continue;
            }
        }
        return issues;
    }
    
    /**
     * 
     * @param oldpath
     * @param newpath
     * @param path
     * @return
     */
    private Issue toUpdate(Path oldpath, Path newpath, String path) {
        logger.debug(String.format("[%-6s] %s/%s", LABEL_UPDATE, newpath, path));

        String command = String.format("diff -u %s/%s /%s/%s", oldpath, path, newpath, path);
        
        List<String> results = execute(command);

        StringBuilder body = new StringBuilder();
        body.append(String.format("- %s/%s\n", baseurl, path));
        body.append("```\n");
        for (String result : results) {
            result = result.replace("```", "'''");
            body.append(result);
            body.append("\n");
        }
        body.append("\n```");

        return new Issue(LABEL_UPDATE, path, body.toString());
    }
    
    /**
     * 
     * @param newpath
     * @param path
     * @return
     */
    private List<Issue> toNew(Path newpath, String path) {
        logger.debug(String.format("[%-6s] %s/%s", LABEL_NEW, newpath, path));

        List<File> files = findFiles(newpath.resolve(path).toFile());
        List<Issue> issues = new ArrayList<>();
        for (File file : files) {
            path = newpath.relativize(file.toPath()).toString();
            String body = String.format("- %s/%s", baseurl, path);
            issues.add(new Issue(LABEL_NEW, path, body));
        }
        return issues;
    }
    
    /**
     * 
     * @param file
     * @return
     */
    private List<File> findFiles(File file) {
        List<File> files = new ArrayList<>();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                files.addAll(findFiles(f));
            }
        } else {
            if (file.getName().endsWith(MARKDOWN_EXT)) {
                files.add(file);
            }
        }
        return files;
    }
    
    /**
     * 
     * @param command
     * @return
     */
    private List<String> execute(String command) {
        List<String> lines = new ArrayList<String>();
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }
    
    public static void main(String[] args) throws IOException {

        String basepath = args[0];
        String oldver = args[1];
        String newver = args[2];

        if (basepath.endsWith("/")) {
            basepath = basepath.substring(0, basepath.length() - 1);
        }
        String command = String.format("diff -qr %s/%s %s/%s", basepath, oldver, basepath, newver);
        logger.debug(command);

        List<String> results = exec(command);
        List<Issue> issues = new ArrayList<Issue>();
        for (String result : results) {
            if (result.endsWith(".DS_Store")) {
                continue;
            }
            if (result.endsWith(": code")) {
                continue;
            }
            Matcher deleted = Pattern.compile(String.format("^Only in %s/%s/(.+)", basepath, oldver)).matcher(result);
            Matcher added = Pattern.compile(String.format("^Only in %s/%s/(.+)", basepath, newver)).matcher(result);
            Matcher updated = Pattern.compile(String.format("^Files %s/%s/(.+) and %s/%s/(.+) differ$", basepath, oldver, basepath, newver)).matcher(result);
            if (deleted.find()) {
                issues.add(toDeleteIssue(oldver, deleted.group(1).replace(": ", "/")));
            } else if (added.find()) {
                issues.add(toNewIssue(newver, added.group(1).replace(": ", "/")));
            } else if (updated.find()) {
                issues.add(toUpdateIssue(basepath, oldver, newver, updated.group(1)));
            } else {
                throw new RuntimeException(result);
            }
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter username: ");
        String login = reader.readLine();
        System.out.print("Enter password: ");
        String password = reader.readLine();
        System.out.print("Enter repository: ");
        String name = reader.readLine();
        System.out.print("Enter milestone number: ");
        String number = reader.readLine();
        reader.close();

        GitHub github = GitHub.connectUsingPassword(login, password);
        GHRepository repository = github.getRepository(String.format("%s/%s", login, name));
        for (Issue issue : issues) {
            if (issue == null) {
                continue;
            }
            GHIssueBuilder builder = repository.createIssue(issue.path);
            builder.milestone(repository.getMilestone(Integer.parseInt(number)));
            builder.label(issue.label);
            builder.body(issue.body);
            builder.create();
            logger.debug(String.format("create %s issue: %s", issue.label, issue.path));
        }
    }

    /**
     * 
     * @param login
     * @param password
     * @param organization
     * @param repository
     * @return
     */
    public GHRepository getRepository(String login, String password, String organization, String repository) {
        try {
            GitHub g = GitHub.connectUsingPassword(login, password);
            GHRepository r = g.getRepository(organization + "/" + repository);
            return r;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Issue toDeleteIssue(String oldver, String path) throws IOException {
        logger.debug(String.format("[%-7s] %s/%s", "DELETED", oldver, path));
        return null;
    }

    private static Issue toNewIssue(String newver, String path) throws IOException {
        logger.debug(String.format("[%-7s] %s/%s", "ADDED", newver, path));
        String body = String.format("- %s/%s/%s", baseurl, newver, path);
        return new Issue("new", path, body);
    }

    private static Issue toUpdateIssue(String basepath, String oldver, String newver, String path) throws IOException {
        logger.debug(String.format("[%-7s] %s/%s", "UPDATED", newver, path));

        String command = String.format("diff -u %s/%s/%s %s/%s/%s", basepath, oldver, path, basepath, newver, path);
        List<String> results = exec(command);

        StringBuilder body = new StringBuilder();
        body.append(String.format("- %s/%s/%s\n", baseurl, newver, path));
        body.append("```\n");
        for (String result : results) {
            result = result.replace("```", "'''");
            body.append(result);
            body.append("\n");
        }
        body.append("\n```");

        return new Issue("update", path, body.toString());
    }

    private static List<String> exec(String command) throws IOException {
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        List<String> lines = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        return lines;
    }
}

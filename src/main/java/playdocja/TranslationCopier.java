package playdocja;

import static playdocja.TranslationParser.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;


/**
 * 
 * @author garbagetown
 *
 */
public class TranslationCopier {
    
    private static Logger logger = Logger.getLogger(TranslationCopier.class);

    public static final String MARKDOWN_EXT = ".md";
    
    /**
     * 
     * @param from
     * @param to
     */
    public void copy(Path from, Path to) {
        copy(from.toFile(), to.toFile());
    }

    /**
     * 
     * @param from
     * @param to
     */
    public void copy(File from, File to) {
        if ((from.isDirectory() && to.isFile()) ||
                from.isFile() && to.isDirectory()) {
            throw new RuntimeException();
        }
        if (from.isDirectory() && to.isDirectory()) {
            processDir(from, to);
        } else {
            processFile(from, to);
        }
    }
    
    /**
     * 
     * @param from
     * @param to
     */
    private void processDir(File from, File to) {
        for (File f : from.listFiles()) {
            File t = Paths.get(to.getAbsolutePath(), f.getName()).toFile();
            if (f.isDirectory()) {
                processDir(f, t);
            } else {
                processFile(f, t);
            }
        }
    }

    /**
     * 
     * @param from
     * @param to
     */
    private void processFile(File from, File to) {

        if (!from.getName().endsWith(MARKDOWN_EXT)) {
            return;
        }
        if (!to.exists()) {
            return;
        }
        
        Path f = from.toPath();
        Path t = to.toPath();
        
        logger.debug(f);
        logger.debug(t);
        
        List<String> lines = null;
        try {
            lines = Files.readAllLines(t);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TranslationParser parser = new TranslationParser(f);
        List<Pair<List<String>, List<String>>> pairs = parser.parse();

        for (Pair<List<String>, List<String>> pair : pairs) {
            List<String> en = pair.getLeft();
            List<String> ja = pair.getRight();
            int index = Collections.indexOfSubList(lines, en);
            if (index < 0) {
                continue;
            }
            // TODO take care the already translated document.
            lines.add(index , COMMENT_OPEN);
            lines.add(index + en.size() + 1, COMMENT_CLOSE);
            lines.addAll(index + en.size() + 2, ja);
        }
        try {
            Files.write(t, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
        
    /**
     * 
     * @param args
     */
    public static void main (String[] args) {
        File from = Paths.get(args[0]).toFile();
        File to = Paths.get(args[1]).toFile();
        new TranslationCopier().copy(from, to);
    }
}

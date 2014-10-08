package playdocja;

import static org.apache.commons.collections4.CollectionUtils.*;
import static org.apache.commons.lang3.StringUtils.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

public class TranslationParser {

    private static Logger logger = Logger.getLogger(TranslationParser.class);

    public static final String COMMENT_OPEN = "<!--";
    public static final String COMMENT_CLOSE = "-->";

    private enum Status {
        EN, JA, NONE
    }

    private Path path;
    private List<Pair<List<String>, List<String>>> pairs;
    private List<String> en;
    private List<String> ja;
    private Status status;

    public TranslationParser(Path path) {
        this.path = path;
        this.pairs = new ArrayList<>();
        this.en = new ArrayList<>();
        this.ja = new ArrayList<>();
        this.status = Status.NONE;
    }

    /**
     * 
     * @param path
     * @return
     * @throws IOException
     */
    public List<Pair<List<String>, List<String>>> parse() throws IOException {

        for (String line : Files.readAllLines(path)) {
            
            String s = line.trim();

            if (isBlank(s)) {
                processBlank(line);
            } else if (s.startsWith(COMMENT_OPEN)) {
                processOpen(line);
            } else if (s.endsWith(COMMENT_CLOSE)) {
                processClose(line);
            } else {
                processOther(line);
            }
        }
        addToPairs();

        return pairs;
    }

    /**
     * 
     * @param line
     */
    private void processBlank(String line) {
        switch (status) {
        case EN:
            en.add(line);
            break;
        case JA:
            addToPairs();
            break;
        default:
            break;
        }
    }

    /**
     * 
     * @param line
     */
    private void processOpen(String line) {

        addToPairs();
        
        String s = line.trim();
        if (s.endsWith(COMMENT_CLOSE)) {
            s = s.replace(COMMENT_OPEN, EMPTY);
            s = s.replace(COMMENT_CLOSE, EMPTY);
            en.add(s.trim());
            status = Status.JA;
            return;
        }
        if (!s.equals(COMMENT_OPEN)) {
            s = s.replace(COMMENT_OPEN, EMPTY);
            en.add(s.trim());
        }
        status = Status.EN;
    }

    /**
     * 
     * @param line
     */
    private void processClose(String line) {
        String s = line.trim();
        if (!s.equals(COMMENT_CLOSE)) {
            s = s.replace(COMMENT_CLOSE, EMPTY);
            en.add(s.trim());
        }
        status = Status.JA;
    }

    /**
     * 
     * @param line
     */
    private void processOther(String line) {
        switch (status) {
        case EN:
            en.add(line);
            break;
        case JA:
            ja.add(line);
            break;
        default:
            break;
        }
    }

    private void addToPairs() {
        
        if (isEmpty(en) && isEmpty(ja)) {
            return;
        }
        
        logger.debug(en);
        logger.debug(ja);
        
        if (en.size() != ja.size()) {
            StringBuilder message = new StringBuilder("size not match:\n");
            message.append(String.format("en = %d:%s\n", en.size(), en));
            message.append(String.format("ja = %d:%s", ja.size(), ja));
            logger.warn(message);
        }
        pairs.add(new ImmutablePair<List<String>, List<String>>(en, ja));
        
        status = Status.NONE;
        en = new ArrayList<>();
        ja = new ArrayList<>();
    }

}

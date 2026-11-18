package ruins.gen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The files of the original installation Ruins read: mods/resources/ruins (templates and rotation mappings, from the
 * data archives of the pack) and mods/ruins.txt (the pack's global options). The build copies them into the jar
 * under {@value #ROOT} with an index of the template folder, see original-assets.txt.
 */
public final class RuinsFiles {
    static final String ROOT = "ruins-original/";
    private static volatile List<String> index;

    private RuinsFiles() {
    }

    static InputStream open(String path) throws IOException {
        InputStream in = RuinsFiles.class.getClassLoader().getResourceAsStream(ROOT + path);
        if (in == null) {
            in = RuinsFiles.class.getResourceAsStream("/" + ROOT + path);
        }
        if (in == null) {
            throw new IOException("Ruins file missing in the jar: " + ROOT + path + " (see original-assets.txt)");
        }
        return in;
    }

    /** BufferedReader.readLine over the whole file (FileReader of the original, the files are ASCII). */
    static List<String> lines(String path) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(open(path), StandardCharsets.ISO_8859_1))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static List<String> index() {
        List<String> files = index;
        if (files == null) {
            try {
                files = List.copyOf(lines("ruins/.index").stream().map(String::trim).filter(s -> !s.isEmpty()).toList());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            index = files;
        }
        return files;
    }

    /**
     * The files directly in a folder of mods/resources/ruins ("forest"), or {@code null} when the folder does not
     * exist (File.listFiles). Sub-folders are left out: the original skipped them silently.
     */
    static List<String> list(String folder) {
        String prefix = folder + "/";
        List<String> out = new ArrayList<>();
        boolean exists = false;
        for (String file : index()) {
            if (file.startsWith(prefix)) {
                exists = true;
                String rest = file.substring(prefix.length());
                if (rest.indexOf('/') < 0) {
                    out.add(rest);
                }
            }
        }
        return exists ? out : null;
    }

    static boolean exists(String path) {
        return index().contains(path);
    }
}

package betterdungeons.gen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

/**
 * The "Chocolate" folder of the original (betterdungeons-data1/2 of the pack): building templates, dungeon and
 * chest configuration. The build copies it into the jar at {@value #ROOT} with an index of its files
 * (original-assets.txt), because a jar cannot list a folder.
 *
 * <p>{@code File.listFiles()} returned the files in the order of the file system, and the mod picked templates and
 * dungeon types by their position in it. Here the order is the one of NTFS (Windows, which most players of the
 * pack used): names compared character by character in upper case.
 */
public final class BDFiles {
    public static final String ROOT = "betterdungeons-original/Chocolate/";
    /** The order of a Windows directory listing. */
    public static final Comparator<String> NTFS_ORDER = (a, b) -> {
        int n = Math.min(a.length(), b.length());
        for (int i = 0; i < n; i++) {
            char ca = Character.toUpperCase(a.charAt(i));
            char cb = Character.toUpperCase(b.charAt(i));
            if (ca != cb) {
                return ca - cb;
            }
        }
        return a.length() - b.length();
    };
    private static volatile List<String> index;

    private BDFiles() {
    }

    public static InputStream open(String path) throws IOException {
        InputStream in = BDFiles.class.getClassLoader().getResourceAsStream(ROOT + path);
        if (in == null) {
            in = BDFiles.class.getResourceAsStream("/" + ROOT + path);
        }
        if (in == null) {
            throw new IOException("Better Dungeons file missing in the jar: " + ROOT + path + " (see original-assets.txt)");
        }
        return in;
    }

    private static List<String> index() {
        List<String> files = index;
        if (files == null) {
            files = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(open(".index"), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        files.add(line.trim());
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            index = files;
        }
        return files;
    }

    /** File.listFiles of a folder below Chocolate/ ("Building/castles"): the names of its files and sub-folders, in NTFS order. */
    public static List<String> list(String folder) {
        String prefix = folder.endsWith("/") ? folder : folder + "/";
        TreeSet<String> names = new TreeSet<>(NTFS_ORDER);
        for (String file : index()) {
            if (file.startsWith(prefix)) {
                String rest = file.substring(prefix.length());
                int slash = rest.indexOf('/');
                names.add(slash < 0 ? rest : rest.substring(0, slash));
            }
        }
        return new ArrayList<>(names);
    }

    /** A Properties file (Properties.load), keeping the order of its entries ({@link Ordered#keys}). */
    public static Ordered properties(String path) {
        Ordered properties = new Ordered();
        try (InputStream in = open(path)) {
            properties.load(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return properties;
    }

    /** Properties that remember the order in which load() put their keys. */
    public static final class Ordered extends Properties {
        public final List<String> keys = new ArrayList<>();

        @Override
        public synchronized Object put(Object key, Object value) {
            if (!this.containsKey(key)) {
                this.keys.add((String) key);
            }
            return super.put(key, value);
        }

        /**
         * Properties.elements() of Java 6 to 8, where Properties was a Hashtable: the order of its buckets (11,
         * doubled + 1 whenever 3/4 full, new entries at the head of their bucket), read from the last bucket to the first.
         */
        public List<String> hashtableValues() {
            List<List<String>> table = new ArrayList<>();
            for (int i = 0; i < 11; i++) {
                table.add(new ArrayList<>());
            }
            int threshold = (int) (11 * 0.75F);
            int count = 0;
            for (String key : this.keys) {
                if (count >= threshold) {
                    int capacity = table.size() * 2 + 1;
                    List<List<String>> grown = new ArrayList<>();
                    for (int i = 0; i < capacity; i++) {
                        grown.add(new ArrayList<>());
                    }
                    // Hashtable.rehash: old buckets from the last, each chain from its head, every entry to the head of its new bucket
                    for (int i = table.size() - 1; i >= 0; i--) {
                        for (String old : table.get(i)) {
                            grown.get((old.hashCode() & 0x7FFFFFFF) % capacity).add(0, old);
                        }
                    }
                    table = grown;
                    threshold = (int) Math.min(capacity * 0.75F, Integer.MAX_VALUE - 8 + 1);
                }
                table.get((key.hashCode() & 0x7FFFFFFF) % table.size()).add(0, key);
                count++;
            }
            List<String> values = new ArrayList<>();
            for (int i = table.size() - 1; i >= 0; i--) {
                for (String key : table.get(i)) {
                    values.add(this.getProperty(key));
                }
            }
            return values;
        }
    }
}

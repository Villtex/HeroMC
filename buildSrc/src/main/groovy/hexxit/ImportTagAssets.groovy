package hexxit

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Copies the assets (textures, models, blockstates, sounds, books, language files) of a mod from its release tag on
 * GitHub into the resources of a port (Teil 27: Mantle and Tinkers' Construct 3.12.1, MIT licensed, still not
 * committed because of their size).
 *
 * The tag is a shallow git clone in {@code .reference/tc3/<name>} (the same folder tools/reference_tc3.py fills);
 * when it is missing or on another ref, the task clones it. With {@code fromNamespace}/{@code toNamespace} set, the
 * mod's namespace is renamed on the way: the folder {@code assets/<from>/}, every {@code <from>:} in text files and,
 * in language files, the translation keys ({@code item.<from>.x} becomes {@code item.<to>.x}), because the port
 * registers its content under the new mod ID.
 */
abstract class ImportTagAssets extends DefaultTask {
    @Input
    abstract Property<String> getUrl()

    @Input
    abstract Property<String> getRef()

    /** Folders inside the clone whose content is copied, e.g. src/main/resources/assets (content lands in assets/). */
    @Input
    abstract ListProperty<String> getFolders()

    @Input
    abstract Property<String> getFromNamespace()

    @Input
    abstract Property<String> getToNamespace()

    @Internal
    abstract DirectoryProperty getCloneDir()

    @OutputDirectory
    abstract DirectoryProperty getOutputDir()

    private static final Set<String> TEXT = ['json', 'mcmeta', 'txt', 'md', 'properties', 'fsh', 'vsh', 'glsl'] as Set

    @TaskAction
    void run() {
        Path clone = cloneDir.get().asFile.toPath()
        Path marker = clone.resolve('.hexxit-ref')
        if (!Files.exists(marker) || Files.readString(marker).trim() != ref.get()) {
            clone.toFile().deleteDir()
            Files.createDirectories(clone.parent)
            def proc = ['git', '-c', 'advice.detachedHead=false', 'clone', '-q', '--depth', '1', '--branch', ref.get(),
                        url.get(), clone.toString()].execute()
            proc.waitForProcessOutput(System.out, System.err)
            if (proc.exitValue() != 0) {
                throw new GradleException("git clone ${url.get()} @ ${ref.get()} failed")
            }
            Files.writeString(marker, ref.get() + '\n')
        }
        Path out = outputDir.get().asFile.toPath()
        out.toFile().deleteDir()
        String from = fromNamespace.get()
        String to = toNamespace.get()
        boolean rename = from != to
        def idPattern = ~/(?<![A-Za-z0-9_.\-])${java.util.regex.Pattern.quote(from)}:/
        def keyPattern = ~/(?<=^|\.)${java.util.regex.Pattern.quote(from)}(?=\.)/
        int count = 0
        folders.get().each { String folder ->
            Path base = clone.resolve(folder)
            if (!Files.isDirectory(base)) {
                throw new GradleException("${base} missing in ${url.get()} @ ${ref.get()}")
            }
            // assets/ or data/: the last element of the folder path
            String top = base.fileName.toString()
            Files.walk(base).filter { Files.isRegularFile(it) }.each { Path file ->
                String rel = base.relativize(file).toString().replace('\\', '/')
                if (rename && (rel == from || rel.startsWith(from + '/'))) {
                    rel = to + rel.substring(from.length())
                }
                Path dst = out.resolve(top).resolve(rel)
                Files.createDirectories(dst.parent)
                String name = file.fileName.toString()
                String ext = name.contains('.') ? name.substring(name.lastIndexOf('.') + 1) : ''
                if (rename && ext in TEXT) {
                    String text = Files.readString(file, StandardCharsets.UTF_8)
                    text = text.replaceAll(idPattern, to + ':')
                    if (ext == 'json' && rel.contains('/lang/')) {
                        // keys only: "item.tconstruct.pickaxe": ... (the values are shown text)
                        text = text.readLines().collect { String line ->
                            def m = line =~ /^(\s*")([^"]*)(".*)$/
                            m.matches() ? m.group(1) + m.group(2).replaceAll(keyPattern, to) + m.group(3) : line
                        }.join('\n')
                    }
                    Files.writeString(dst, text, StandardCharsets.UTF_8)
                } else {
                    Files.copy(file, dst)
                }
                count++
            }
        }
        logger.lifecycle("${url.get()} @ ${ref.get()}: ${count} files")
    }
}

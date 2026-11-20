package hexxit

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Copies textures, sounds and other assets out of original Hexxit 1.0.11 archives into a mod's resources.
 *
 * The originals belong to their authors and are not part of this repository. Each mod lists what it needs in
 * original-assets.txt ("path in the original jar -> path in the new jar", a trailing slash copies a folder);
 * the archive is fetched from the Technic mirror (md5-checked, cached in .cache/zips) when the mod is built.
 * The lines take from the archive named like the mod; a line "[other-archive]" switches to another archive of
 * the pack manifest for the lines below it (data-only archives such as betterdungeons-data1). Next to every
 * copied folder an ".index" file lists its files (paths relative to the folder, one per line, sorted), because
 * a jar cannot list a directory.
 */
abstract class ImportOriginalAssets extends DefaultTask {
    @InputFile
    abstract RegularFileProperty getManifest()

    @InputFile
    abstract RegularFileProperty getMapping()

    /** Name of the archive in the pack manifest. */
    @Input
    abstract Property<String> getArchive()

    @Internal
    abstract DirectoryProperty getCacheDir()

    @OutputDirectory
    abstract DirectoryProperty getOutputDir()

    @TaskAction
    void run() {
        def mods = new JsonSlurper().parse(manifest.get().asFile).mods
        // archive name -> [files, folders], in the order of original-assets.txt
        Map<String, List<Map<String, String>>> sections = new LinkedHashMap<>()
        String current = archive.get()
        sections[current] = [[:], [:]]
        mapping.get().asFile.eachLine { String raw ->
            String line = raw.trim()
            if (line.isEmpty() || line.startsWith('#')) {
                return
            }
            if (line.startsWith('[') && line.endsWith(']')) {
                current = line.substring(1, line.length() - 1).trim()
                sections.computeIfAbsent(current) { [[:], [:]] }
                return
            }
            def parts = line.split(/\s*->\s*/)
            if (parts.length != 2) {
                throw new GradleException("${mapping.get().asFile}: expected 'source -> target', got: ${line}")
            }
            sections[current][parts[0].endsWith('/') ? 1 : 0][parts[0]] = parts[1]
        }

        File out = outputDir.get().asFile
        project.delete(out)
        out.mkdirs()
        Map<String, Set<String>> indexes = new TreeMap<>()
        sections.each { String name, List<Map<String, String>> section ->
            if (section[0].isEmpty() && section[1].isEmpty()) {
                return
            }
            def entry = mods.find { it.name == name }
            if (entry == null) {
                throw new GradleException("No archive named '${name}' in ${manifest.get().asFile.name}")
            }
            copy(entry, section[0], section[1], out, indexes)
        }
        indexes.each { String folder, Set<String> names ->
            File index = new File(out, folder + '.index')
            index.parentFile.mkdirs()
            index.text = names.join('\n') + '\n'
        }
    }

    void copy(def entry, Map<String, String> files, Map<String, String> folders, File out, Map<String, Set<String>> indexes) {
        Set<String> found = [] as Set
        def take = { String name, byte[] data ->
            String target = files[name]
            String source = name
            if (target == null) {
                def folder = folders.find { name.startsWith(it.key) }
                if (folder != null) {
                    target = folder.value + name.substring(folder.key.length())
                    source = folder.key
                    indexes.computeIfAbsent(folder.value) { new TreeSet<String>() } << name.substring(folder.key.length())
                }
            }
            if (target != null) {
                File file = new File(out, target)
                if (!file.canonicalPath.startsWith(out.canonicalPath + File.separator)) {
                    throw new GradleException("Target escapes the output directory: ${target}")
                }
                file.parentFile.mkdirs()
                if (name.toLowerCase().endsWith('.wav') && target.endsWith('.ogg')) {
                    // 26.3 only plays Ogg Vorbis: 1.5.2 sound packs (Falling Meteors) shipped WAV, converted with ffmpeg
                    File wav = File.createTempFile('original', '.wav')
                    wav.bytes = data
                    def proc = ['ffmpeg', '-y', '-loglevel', 'error', '-i', wav.absolutePath, '-c:a', 'libvorbis', '-q:a', '5', file.absolutePath].execute()
                    proc.waitForProcessOutput(System.out, System.err)
                    wav.delete()
                    if (proc.exitValue() != 0) {
                        throw new GradleException("ffmpeg could not convert ${name} (ffmpeg with libvorbis is needed for WAV sounds)")
                    }
                } else {
                    file.bytes = data
                }
                found << source
            }
        }

        // Technic archives wrap the actual mod jar (mods/Foo.jar); look into both levels.
        fetch(entry).withInputStream { stream ->
            eachEntry(new ZipInputStream(stream)) { String name, byte[] data ->
                take(name, data)
                String lower = name.toLowerCase()
                if (lower.endsWith('.jar') || lower.endsWith('.zip')) {
                    eachEntry(new ZipInputStream(new ByteArrayInputStream(data)), take)
                }
            }
        }

        def missing = (files.keySet() + folders.keySet()) - found
        if (!missing.isEmpty()) {
            throw new GradleException("Not found in ${entry.name} ${entry.version}: ${missing.join(', ')}")
        }
    }

    static void eachEntry(ZipInputStream zip, Closure action) {
        ZipEntry e
        while ((e = zip.nextEntry) != null) {
            if (!e.directory) {
                action(e.name, zip.readAllBytes())
            }
        }
    }

    File fetch(def entry) {
        File file = new File(cacheDir.get().asFile, "${entry.name}-${entry.version}.zip")
        if (file.exists() && md5(file) == entry.md5) {
            return file
        }
        file.parentFile.mkdirs()
        logger.lifecycle("Downloading original ${entry.name} ${entry.version}")
        File part = new File(file.path + '.part')
        new URL(entry.url as String).openConnection().with { connection ->
            connection.setRequestProperty('User-Agent', 'hexxit-port-tools')
            connection.connectTimeout = 30000
            connection.readTimeout = 120000
            connection.inputStream.withStream { input -> part.withOutputStream { it << input } }
        }
        if (md5(part) != entry.md5) {
            part.delete()
            throw new GradleException("Checksum mismatch for ${entry.url}")
        }
        if (!part.renameTo(file)) {
            file.bytes = part.bytes
            part.delete()
        }
        return file
    }

    static String md5(File file) {
        MessageDigest digest = MessageDigest.getInstance('MD5')
        file.eachByte(1 << 16) { byte[] buffer, int length -> digest.update(buffer, 0, length) }
        return digest.digest().encodeHex().toString()
    }
}

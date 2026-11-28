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

    /** If true, writes 26.3 item model definitions ({@code items/<id>.json}) for the item models, see {@link #writeItemDefinitions} */
    @Input
    @org.gradle.api.tasks.Optional
    abstract Property<Boolean> getItemDefinitions()

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
        int states = rewriteBlockstates(out.resolve('assets'))
        int items = itemDefinitions.getOrElse(false) ? writeItemDefinitions(out.resolve('assets'), to) : 0
        logger.lifecycle("${url.get()} @ ${ref.get()}: ${count} files, ${states} blockstates with Mantle block state models, ${items} item definitions")
    }

    /** Forge model namespaces of 1.20.1 that NeoForge 26.3 ships as neoforge: (parents item/default..., loader composite) */
    private static String forgeToNeo(String text) {
        return text.replace('"forge:item/', '"neoforge:item/').replace('"forge:block/', '"neoforge:block/')
                   .replaceAll(/("loader"\s*:\s*")forge:/, '$1neoforge:')
    }

    /** Model loaders whose blocks need a world-aware block state model in 26.3 (Mantle, Teil 27c) */
    private static final Set<String> BLOCKSTATE_LOADERS = ['mantle:connected', 'mantle:retextured'] as Set

    /**
     * 26.3 only lets block state models see the world (neighbours, block entity data). Every block state variant whose
     * model (or a parent) uses one of {@link #BLOCKSTATE_LOADERS} gets {@code "type": <loader>}, so Mantle's block state
     * model of the same name renders it. Also renames forge: model references, see {@link #forgeToNeo}.
     */
    private static int rewriteBlockstates(Path assets) {
        if (!Files.isDirectory(assets)) {
            return 0
        }
        def slurper = new groovy.json.JsonSlurper()
        // model id -> [loader, parent]
        Map<String, List<String>> models = [:]
        Files.walk(assets).filter { Files.isRegularFile(it) && it.toString().endsWith('.json') }.each { Path file ->
            String text = Files.readString(file, StandardCharsets.UTF_8)
            String fixed = forgeToNeo(text)
            if (fixed != text) {
                Files.writeString(file, fixed, StandardCharsets.UTF_8)
            }
            def parts = assets.relativize(file).toString().replace('\\', '/').split('/', 3)
            if (parts.length == 3 && parts[1] == 'models') {
                try {
                    def json = slurper.parseText(fixed)
                    if (json instanceof Map) {
                        def loader = json.loader instanceof Map ? json.loader.id : json.loader
                        models["${parts[0]}:${parts[2].replaceAll(/\.json$/, '')}".toString()] = [loader as String, json.parent as String]
                    }
                } catch (Exception ignored) {}
            }
        }
        def loaderOf
        loaderOf = { String id, int depth ->
            if (id == null || depth > 32) return null
            String full = id.contains(':') ? id : 'minecraft:' + id
            def entry = models[full]
            if (entry == null) return null
            if (entry[0] != null) return entry[0]
            return loaderOf(entry[1], depth + 1)
        }
        int changed = 0
        Files.walk(assets).filter { Files.isRegularFile(it) && it.toString().endsWith('.json') && it.parent.fileName.toString() == 'blockstates' }.each { Path file ->
            def json
            try {
                json = slurper.parseText(Files.readString(file, StandardCharsets.UTF_8))
            } catch (Exception ignored) {
                return
            }
            boolean dirty = false
            def visit = { model ->
                if (model instanceof Map && model.model != null && model.type == null) {
                    String loader = loaderOf(model.model as String, 0)
                    if (loader in BLOCKSTATE_LOADERS) {
                        model.type = loader
                        dirty = true
                    }
                }
            }
            def visitAll = { value -> value instanceof List ? value.each(visit) : visit(value) }
            if (json.variants instanceof Map) json.variants.values().each(visitAll)
            if (json.multipart instanceof List) json.multipart.each { visitAll(it.apply) }
            if (dirty) {
                Files.writeString(file, groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(json)), StandardCharsets.UTF_8)
                changed++
            }
        }
        return changed
    }

    /** Item model types for 1.20.1 model loaders (Tinkers 3.12.1, Teil 27f2), every other model is {@code minecraft:model} */
    private static final Map<String, String> ITEM_MODEL_TYPES = [
        'tconstruct3:tool': 'tconstruct3:tool',
        'tconstruct3:material': 'tconstruct3:material',
        'tconstruct3:fluid_container': 'tconstruct3:fluid_container',
    ]

    /**
     * 26.3 draws an item only with an item model definition {@code assets/<ns>/items/<id>.json}; the 1.20.1 assets have
     * none. For every top-level item model {@code models/item/<id>.json} of the namespace this writes one: the model
     * itself ({@code minecraft:model}, or the Tinkers type of its loader, {@link #ITEM_MODEL_TYPES}), and for each
     * 1.20.1 {@code overrides} entry a {@code minecraft:condition} on {@code tconstruct3:predicate} (true when every
     * property is at least the given value). The last override is checked first, as the last matching one won in 1.20.1.
     * Item colors of 1.20.1 (tints) are not known here; those items get their definition by hand with their part.
     */
    private static int writeItemDefinitions(Path assets, String namespace) {
        Path models = assets.resolve(namespace).resolve('models')
        if (!Files.isDirectory(models)) {
            return 0
        }
        def slurper = new groovy.json.JsonSlurper()
        // model id -> [loader, parent]
        Map<String, List<String>> chain = [:]
        Files.walk(assets).filter { Files.isRegularFile(it) && it.toString().endsWith('.json') }.each { Path file ->
            def parts = assets.relativize(file).toString().replace('\\', '/').split('/', 3)
            if (parts.length == 3 && parts[1] == 'models') {
                try {
                    def json = slurper.parseText(Files.readString(file, StandardCharsets.UTF_8))
                    if (json instanceof Map) {
                        def loader = json.loader instanceof Map ? json.loader.id : json.loader
                        chain["${parts[0]}:${parts[2].replaceAll(/\.json$/, '')}".toString()] = [loader as String, json.parent as String]
                    }
                } catch (Exception ignored) {}
            }
        }
        def full = { String id -> id.contains(':') ? id : 'minecraft:' + id }
        def typeOf
        typeOf = { String id, int depth ->
            if (id == null || depth > 32) return 'minecraft:model'
            def entry = chain[full(id)]
            if (entry == null) return 'minecraft:model'
            if (entry[0] != null && ITEM_MODEL_TYPES.containsKey(entry[0])) return ITEM_MODEL_TYPES[entry[0]]
            return typeOf(entry[1], depth + 1)
        }
        def modelFor = { String id -> [type: typeOf(id, 0), model: full(id)] }
        Path items = assets.resolve(namespace).resolve('items')
        int written = 0
        Path itemModels = models.resolve('item')
        if (!Files.isDirectory(itemModels)) {
            return 0
        }
        Files.list(itemModels).filter { Files.isRegularFile(it) && it.toString().endsWith('.json') }.sorted().each { Path file ->
            String name = file.fileName.toString().replaceAll(/\.json$/, '')
            Path dst = items.resolve(name + '.json')
            if (Files.exists(dst)) {
                return
            }
            def json
            try {
                json = slurper.parseText(Files.readString(file, StandardCharsets.UTF_8))
            } catch (Exception ignored) {
                return
            }
            def model = modelFor("${namespace}:item/${name}".toString())
            if (json instanceof Map && json.overrides instanceof List) {
                json.overrides.each { override ->
                    if (override instanceof Map && override.model != null && override.predicate instanceof Map) {
                        model = [type: 'minecraft:condition', property: 'tconstruct3:predicate', predicate: override.predicate,
                                 on_true: modelFor(override.model as String), on_false: model]
                    }
                }
            }
            Files.createDirectories(items)
            Files.writeString(dst, groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson([model: model])), StandardCharsets.UTF_8)
            written++
        }
        return written
    }
}

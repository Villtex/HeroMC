package invtweaks.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.minecraft.client.Minecraft;

/**
 * InvTweaksConfigManager: (re)loads the three config files when they change. Missing rules/tree files are created
 * from the defaults in the jar; a tree file of another tree version (e.g. the 1.5.2 tree with numeric ids) is moved
 * to InvTweaksTree.txt.bak and replaced by the converted default tree, as the original did on a new tree version.
 */
public final class InvTweaksConfigManager {
    private final Minecraft mc;
    private InvTweaksConfig config = null;
    private long storedConfigLastModified = 0L;
    private InvTweaksHandlerAutoRefill autoRefillHandler = null;
    private InvTweaksHandlerShortcuts shortcutsHandler = null;

    public InvTweaksConfigManager(Minecraft mc) {
        this.mc = mc;
    }

    public boolean makeSureConfigurationIsLoaded() {
        try {
            if (this.config != null && this.config.refreshProperties()) {
                this.shortcutsHandler = new InvTweaksHandlerShortcuts(this.mc, this.config);
                this.shortcutsHandler.loadShortcuts();
                InvTweaks.logInGameStatic("invtweaks.propsfile.loaded");
            }
        } catch (IOException e) {
            InvTweaks.logInGameErrorStatic("invtweaks.loadconfig.refresh.error", e);
        }
        long modified = this.computeConfigLastModified();
        if (this.config != null) {
            return this.storedConfigLastModified != modified ? this.loadConfig() : true;
        }
        this.storedConfigLastModified = modified;
        return this.loadConfig();
    }

    public InvTweaksConfig getConfig() {
        return this.config;
    }

    public InvTweaksHandlerAutoRefill getAutoRefillHandler() {
        return this.autoRefillHandler;
    }

    public InvTweaksHandlerShortcuts getShortcutsHandler() {
        return this.shortcutsHandler;
    }

    private long computeConfigLastModified() {
        return InvTweaksConfig.lastModified(InvTweaksConst.CONFIG_RULES_FILE) + InvTweaksConfig.lastModified(InvTweaksConst.CONFIG_TREE_FILE);
    }

    private boolean loadConfig() {
        try {
            Files.createDirectories(InvTweaksConst.CONFIG_DIR);
        } catch (IOException e) {
            InvTweaks.log.warn("Failed to create the config directory", e);
        }
        try {
            if (Files.exists(InvTweaksConst.CONFIG_TREE_FILE) && !InvTweaksItemTreeLoader.isValidVersion(InvTweaksConst.CONFIG_TREE_FILE)) {
                this.backupFile(InvTweaksConst.CONFIG_TREE_FILE);
            }
        } catch (Exception e) {
            InvTweaks.log.warn("Failed to check item tree version: {}", e.getMessage());
        }
        if (!Files.exists(InvTweaksConst.CONFIG_RULES_FILE) && this.extractFile(InvTweaksConst.DEFAULT_CONFIG_FILE, InvTweaksConst.CONFIG_RULES_FILE)) {
            InvTweaks.logInGameStatic(InvTweaksConst.CONFIG_RULES_FILE.getFileName() + " " + InvTweaksLocalization.get("invtweaks.loadconfig.filemissing"), true);
        }
        if (!Files.exists(InvTweaksConst.CONFIG_TREE_FILE) && this.extractFile(InvTweaksConst.DEFAULT_CONFIG_TREE_FILE, InvTweaksConst.CONFIG_TREE_FILE)) {
            InvTweaks.logInGameStatic(InvTweaksConst.CONFIG_TREE_FILE.getFileName() + " " + InvTweaksLocalization.get("invtweaks.loadconfig.filemissing"), true);
        }
        this.storedConfigLastModified = this.computeConfigLastModified();
        String error = null;
        Exception exception = null;
        try {
            if (this.config == null) {
                this.config = new InvTweaksConfig(InvTweaksConst.CONFIG_RULES_FILE, InvTweaksConst.CONFIG_TREE_FILE);
                this.autoRefillHandler = new InvTweaksHandlerAutoRefill(this.mc, this.config);
                this.shortcutsHandler = new InvTweaksHandlerShortcuts(this.mc, this.config);
            }
            this.config.load();
            this.shortcutsHandler.loadShortcuts();
            InvTweaks.debug = this.config.isDebugEnabled();
            InvTweaks.logInGameStatic("invtweaks.loadconfig.done");
            this.showConfigErrors(this.config);
        } catch (NoSuchFileException e) {
            error = "Config file not found";
        } catch (Exception e) {
            error = "Error while loading config";
            exception = e;
        }
        if (error != null) {
            InvTweaks.logInGameErrorStatic(error, exception);
            InvTweaks.log.error(error, exception);
            this.config = null;
            return false;
        }
        return true;
    }

    private void backupFile(Path file) {
        try {
            Files.move(file, file.resolveSibling(file.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            InvTweaks.log.warn("Failed to back up {}", file, e);
        }
    }

    private boolean extractFile(String resource, Path target) {
        try (InputStream in = InvTweaksConfigManager.class.getResourceAsStream(resource)) {
            if (in == null) {
                InvTweaks.logInGameStatic(String.format(InvTweaksLocalization.get("invtweaks.extract.find.error"), resource), true);
                InvTweaks.log.error("Cannot create {} file: {} not found", target, resource);
                return false;
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            InvTweaks.logInGameStatic(String.format(InvTweaksLocalization.get("invtweaks.extract.create.error"), target), true);
            InvTweaks.log.error("Cannot create {} file: {}", target, e.getMessage());
            return false;
        }
    }

    private void showConfigErrors(InvTweaksConfig config) {
        if (!config.getInvalidKeywords().isEmpty()) {
            StringBuilder message = new StringBuilder(InvTweaksLocalization.get("invtweaks.loadconfig.invalidkeywords")).append(": ");
            for (String keyword : config.getInvalidKeywords()) {
                message.append(keyword).append(' ');
            }
            InvTweaks.logInGameStatic(message.toString(), true);
        }
    }
}

# JEI-Plugin für einen portierten Mod

NEI aus dem Original entfällt, an seine Stelle tritt JEI (Just Enough Items). JEI zeigt alle normalen Werkbank-,
Ofen-, Räucher- und Steinmetz-Rezepte von selbst. Ein Plugin braucht ein Mod nur, wenn er **eigene Rezepttypen
oder Maschinen** hat (eigene Kategorie) oder etwas erklären muss, das kein Rezept zeigt (Info-Seite).

## Einbindung (schon erledigt, in `build.gradle`)

- Jedes Teilprojekt hat `compileOnly "mezz.jei:jei-26.3-neoforge-api:${jei_version}"`, also nur die API zum
  Übersetzen. Der Mod läuft ohne JEI.
- Die Runs des Packs (`./gradlew runClient`, `runServer`) haben JEI selbst als `runtimeOnly`.
- Version in `gradle.properties` (`jei_version`), Quelle: `https://maven.blamejared.com/mezz/jei/jei-26.3-neoforge/`.

## Vorlage

Eine Klasse im Mod, zum Beispiel `<paket>/compat/<Mod>JeiPlugin.java`. JEI findet sie über die Annotation, eine
Registrierung im Mod-Konstruktor ist nicht nötig. Die Klasse darf nur von JEI geladen werden, also von keiner
anderen Klasse des Mods aus angesprochen werden.

```java
@JeiPlugin
public class MyJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(MyMod.MOD_ID, "jei_plugin");
    }

    // Info-Seite (Text aus der Sprachdatei, Schlüssel jei.<modid>.<name>)
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(MyItems.THING.get(), Component.translatable("jei.mymod.thing"));
        // eigene Rezepte: registration.addRecipes(MY_TYPE, recipes)
    }

    // nur für eigene Rezepttypen / Maschinen:
    // registerCategories(IRecipeCategoryRegistration)   -> eine IRecipeCategory je Rezepttyp
    // registerRecipeCatalysts(IRecipeCatalystRegistration) -> die Maschine als Katalysator
    // registerGuiHandlers(IGuiHandlerRegistration)      -> Klickbereich in der GUI der Maschine
}
```

Für eine eigene Kategorie: `RecipeType.create(modid, "name", MyRecipe.class)` als Konstante, eine Klasse
`implements IRecipeCategory<MyRecipe>` (Titel, Icon, `setRecipe` legt die Slots an), die Rezepte aus dem
`RecipeManager` des Servers bzw. aus den Daten des Mods holen. Signaturen immer mit `javap` gegen
`jei-26.3-common-api-<version>.jar` (im Gradle-Cache) prüfen; die Online-Doku hinkt hinterher.

## Prüfen

- Server: `./gradlew runServer` startet mit JEI bis `Done` (JEI lädt dort nur seinen Server-Teil).
- Ob die Seiten und Kategorien erscheinen, sieht man nur im Client: als „Client-Prüfung offen“ vermerken.

Beispiel im Repo: `mods/ebxl/src/main/java/extrabiomes/compat/EbxlJeiPlugin.java` (nur Info-Seiten).

# Automata

## Running The Mod

1. Clone repo
2. Open in VS Code
3. Run **Data Generation** launch task (generated data is git ignored)
4. Run **Minecraft Client** launch task

## Adding Texts

1. Create a public static final **String** to act as a translation key in [`src\main\java\pro\piconico\automata\registry\AutomataTexts.java`](src\main\java\pro\piconico\automata\registry\AutomataTexts.java)
2. Document which arguments the text expects if any
3. Add it to all **FabricLanguageProvider**s in `src\client\java\pro\piconico\automata\datagen\lang\`
4. Run **Data Generation** launch task

Call **Text**.translatable with the translation key and arguments

## Adding Translations

1. Create a class that extends **FabricLanguageProvider** in `src\client\java\pro\piconico\automata\datagen\lang\`
2. Copy the members of [`src\client\java\pro\piconico\automata\datagen\lang\AutomataEnglishProvider.java`](src\client\java\pro\piconico\automata\datagen\lang\AutomataEnglishProvider.java)
3. Replace the language code in the constructor
4. Translate all strings in **generateTranslations**
5. Run **Data Generation** launch task

## Adding Components

1. Create a record (T for example) in `src\main\java\pro\piconico\automata\component\`
2. Give it a public static final **Codec\<T\>** and **PacketCodec<ByteBuf, T>**
3. Register it as a public static final **ComponentType\<T\>** in [`src\main\java\pro\piconico\automata\registry\AutomataComponents.java`](src\main\java\pro\piconico\automata\registry\AutomataComponents.java)

## Adding Entities

### Block Entities
1. Create a class (T for example) that extends **BlockEntity** in `src\main\java\pro\piconico\automata\block\entity\`
2. Register it as a public static final **BlockEntityType\<T\>** in [`src\main\java\pro\piconico\automata\registry\AutomataEntities.java`](src\main\java\pro\piconico\automata\registry\AutomataEntities.java)

## Adding Items

1. Create a class that extends **Item** in `src\main\java\pro\piconico\automata\item\`
2. Register it as a public static final **Item** in [`src\main\java\pro\piconico\automata\registry\AutomataItems.java`](src\main\java\pro\piconico\automata\registry\AutomataItems.java)
3. Add it to [`src\client\java\pro\piconico\automata\datagen\models\AutomataModelProvider.java`](src\client\java\pro\piconico\automata\datagen\models\AutomataModelProvider.java)
4. Add it to all **FabricLanguageProvider**s in `src\client\java\pro\piconico\automata\datagen\lang\`
5. Run **Data Generation** launch task

## Adding Blocks

1. Create a class that extends **Block** in `src\main\java\pro\piconico\automata\block\`
2. Register it as a public static final **Block** in [`src\main\java\pro\piconico\automata\registry\AutomataBlocks.java`](src\main\java\pro\piconico\automata\registry\AutomataBlocks.java) (Creates **Item** for you)
3. Add it to [`src\client\java\pro\piconico\automata\datagen\models\AutomataModelProvider.java`](src\client\java\pro\piconico\automata\datagen\models\AutomataModelProvider.java)
4. Add it to [`src\client\java\pro\piconico\automata\datagen\loot_table\AutomataBlockLootTableGeneratorProvider.java`](src\client\java\pro\piconico\automata\datagen\loot_table\AutomataBlockLootTableProvider.java)
5. Add it to all **FabricLanguageProvider**s in `src\client\java\pro\piconico\automata\datagen\lang\`
6. Run **Data Generation** launch task

## Adding Persistent States

1. Create a class (T for example) that extends **PersistentState** in `src\main\java\pro\piconico\automata\world\`
2. Give it a public static final **Codec\<T\>**
3. Register it as a public static final **PersistentStateType\<T\>** in [`src\main\java\pro\piconico\automata\registry\AutomataPersistentStates.java`](src\main\java\pro\piconico\automata\registry\AutomataPersistentStates.java)

## Adding Screens

1. Create a class (T for example) that extends **ScreenHandler** in `src\main\java\pro\piconico\automata\screen\`
2. Register it as a public static final **ScreenHandlerType\<T\>** in [`src\main\java\pro\piconico\automata\registry\AutomataScreenHandlers.java`](src\main\java\pro\piconico\automata\registry\AutomataScreenHandlers.java)
3. Optional: Wire it up to a **BlockWithEntity**'s **onUse** function.
4. Create a class that extends **HandledScreen\<T\>** in `src\client\java\pro\piconico\automata\client\screen\`
5. Register it in [`src\client\java\pro\piconico\automata\client\AutomataClient.java`](src\client\java\pro\piconico\automata\client\AutomataClient.java)

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.

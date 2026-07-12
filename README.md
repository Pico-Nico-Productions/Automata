# Automata

## Running The Mod

1. Clone repo
2. Open in VS Code
3. Run **Data Generation** launch task (generated data is git ignored)
4. Run **Minecraft Client** launch task

## Adding Blocks

1. Create a class that extends **Block** in `src\main\java\pro\piconico\automata\block\`
2. Register it as a public static **Block** in [`src\main\java\pro\piconico\automata\registry\AutomataBlocks.java`](src\main\java\pro\piconico\automata\registry\AutomataBlocks.java) (Creates **Item** for you and optionally **BlockEntity**)
3. Add it to [`src\client\java\pro\piconico\automata\datagen\models\AutomataBlockModelProvider.java`](src\client\java\pro\piconico\automata\datagen\models\AutomataBlockModelProvider.java)
4. Add it to [`src\main\java\pro\piconico\automata\datagen\loot_table\AutomataBlockLootTableGeneratorProvider.java`](src\main\java\pro\piconico\automata\datagen\loot_table\AutomataBlockLootTableProvider.java)
5. Add it to all **FabricLanguageProvider**s in `src\client\java\pro\piconico\automata\datagen\lang\`
6. Run **Data Generation** launch task

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.

# Automata

## Running The Mod

1. Clone repo
2. Open in VS Code
3. Run **Data Generation** launch task (generated data is git ignored)
4. Run **Minecraft Client** launch task

## Adding Items

1. Create a class that extends **Item** in `src\main\java\pro\piconico\automata\item\`
2. Register it as a public static **Item** in [`src\main\java\pro\piconico\automata\registry\AutomataItems.java`](src\main\java\pro\piconico\automata\registry\AutomataItems.java)
3. Add it to [`src\client\java\pro\piconico\automata\datagen\models\AutomataModelProvider.java`](src\client\java\pro\piconico\automata\datagen\models\AutomataModelProvider.java)
5. Add it to all **FabricLanguageProvider**s in `src\client\java\pro\piconico\automata\datagen\lang\`
6. Run **Data Generation** launch task

## Adding Blocks

1. Create a class that extends **Block** in `src\main\java\pro\piconico\automata\block\`
2. Register it as a public static **Block** in [`src\main\java\pro\piconico\automata\registry\AutomataBlocks.java`](src\main\java\pro\piconico\automata\registry\AutomataBlocks.java) (Creates **Item** for you)
3. Add it to [`src\client\java\pro\piconico\automata\datagen\models\AutomataModelProvider.java`](src\client\java\pro\piconico\automata\datagen\models\AutomataModelProvider.java)
4. Add it to [`src\client\java\pro\piconico\automata\datagen\loot_table\AutomataBlockLootTableGeneratorProvider.java`](src\client\java\pro\piconico\automata\datagen\loot_table\AutomataBlockLootTableProvider.java)
5. Add it to all **FabricLanguageProvider**s in `src\client\java\pro\piconico\automata\datagen\lang\`
6. Run **Data Generation** launch task

## Adding Entities

### Block Entities
1. Create a class that extends **BlockEntity** in `src\main\java\pro\piconico\automata\block\entity\`
2. Register it as a public static **BlockEntityType<>** in [`src\main\java\pro\piconico\automata\registry\AutomataEntities.java`](src\main\java\pro\piconico\automata\registry\AutomataEntities.java)

## Adding Screens

1. Create a class that extends **ScreenHandler** in `src\main\java\pro\piconico\automata\screen\`
2. Register it as a public static **ScreenHandlerType<>** in [`src\main\java\pro\piconico\automata\registry\AutomataScreenHandlers.java`](src\main\java\pro\piconico\automata\registry\AutomataScreenHandlers.java)
3. Optional: Wire it up to a **BlockWithEntity**'s **onUse** function.
4. Create a class that extends **HandledScreen<>** in `src\client\java\pro\piconico\automata\client\screen\`
5. Register it in [`src\client\java\pro\piconico\automata\client\AutomataClient.java`](src\client\java\pro\piconico\automata\client\AutomataClient.java)

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.

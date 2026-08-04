# Automata Developer Guide

## Overview

### Running The Mod

1. Clone repo
2. Open in VS Code
3. Run **Data Generation** launch task (generated data is git ignored)
4. Run **Minecraft Client** launch task

### Code Conventions

#### Avoid String Literals
- Define all strings used as **Identifier**s and **RegistryKey**s in [`\src\main\java\pro\piconico\automata\registry\AutomataRegistry.java`](\src\main\java\pro\piconico\automata\registry\AutomataRegistry.java)
- Define all strings used as translation keys in [`\src\main\java\pro\piconico\automata\registry\AutomataTexts.java`](\src\main\java\pro\piconico\automata\registry\AutomataTexts.java)
- Write all player facing strings in the **FabricLanguageProvider**s in [`\src\client\java\pro\piconico\automata\datagen\lang\`](\src\client\java\pro\piconico\automata\datagen\lang\)
- Developer facing string are the exception (e.g. **Codec** field names, **Exception**\log messages, commands)

## Content Creation

### Adding Texts

1. Create a public static final **String** to act as a translation key in [`\src\main\java\pro\piconico\automata\registry\AutomataTexts.java`](\src\main\java\pro\piconico\automata\registry\AutomataTexts.java)
2. Document which arguments the text expects if any
3. Add it to all **FabricLanguageProvider**s in [`\src\client\java\pro\piconico\automata\datagen\lang\`](\src\client\java\pro\piconico\automata\datagen\lang\)
4. Run **Data Generation** launch task

Call **Text**.translatable with the translation key and arguments

### Adding Translations

1. Create a class that extends **FabricLanguageProvider** in [`\src\client\java\pro\piconico\automata\datagen\lang\`](\src\client\java\pro\piconico\automata\datagen\lang\)
2. Copy the members of [`\src\client\java\pro\piconico\automata\datagen\lang\AutomataEnglishProvider.java`](\src\client\java\pro\piconico\automata\datagen\lang\AutomataEnglishProvider.java)
3. Replace the language code in the constructor
4. Translate all strings in **generateTranslations**
5. Run **Data Generation** launch task

### Adding Components

1. Create a record (**T** for example) in [`\src\main\java\pro\piconico\automata\component\`](\src\main\java\pro\piconico\automata\component\)
2. Give it a public static final **Codec\<T\>** and **PacketCodec<ByteBuf, T>**
3. Register it as a public static final **ComponentType\<T\>** in [`\src\main\java\pro\piconico\automata\registry\AutomataComponents.java`](\src\main\java\pro\piconico\automata\registry\AutomataComponents.java)

### Adding Entities

#### Living Entities
1. Create a class (**T** for example) that extends **LivingEntity** in [`\src\main\java\pro\piconico\automata\entity\`](\src\main\java\pro\piconico\automata\entity\)
2. Register it as a public static final **EntityType\<T\>** in [`\src\main\java\pro\piconico\automata\registry\AutomataEntities.java`](\src\main\java\pro\piconico\automata\registry\AutomataEntities.java)
3. Create a class (TRenderer for example) that extends **LivingEntityRenderer** in [`\src\client\java\pro\piconico\automata\client\render\entity\`](\src\client\java\pro\piconico\automata\client\render\entity\)
4. Register it in [`\src\client\java\pro\piconico\automata\client\AutomataClient.java`](\src\client\java\pro\piconico\automata\client\AutomataClient.java)

#### Block Entities
1. Create a class (**T** for example) that extends **BlockEntity** in [`\src\main\java\pro\piconico\automata\block\entity\`](\src\main\java\pro\piconico\automata\block\entity\)
2. Register it as a public static final **BlockEntityType\<T\>** in [`\src\main\java\pro\piconico\automata\registry\AutomataEntities.java`](\src\main\java\pro\piconico\automata\registry\AutomataEntities.java)

### Adding Items

1. Create a class that extends **Item** in [`\src\main\java\pro\piconico\automata\item\`](\src\main\java\pro\piconico\automata\item\)
2. Register it as a public static final **Item** in [`\src\main\java\pro\piconico\automata\registry\AutomataItems.java`](\src\main\java\pro\piconico\automata\registry\AutomataItems.java)
3. Add it to [`\src\client\java\pro\piconico\automata\datagen\models\AutomataModelProvider.java`](\src\client\java\pro\piconico\automata\datagen\models\AutomataModelProvider.java)
4. Add it to all **FabricLanguageProvider**s in [`\src\client\java\pro\piconico\automata\datagen\lang\`](\src\client\java\pro\piconico\automata\datagen\lang\)
5. Run **Data Generation** launch task

### Adding Blocks

1. Create a class that extends **Block** in [`\src\main\java\pro\piconico\automata\block\`](\src\main\java\pro\piconico\automata\block\)
2. Register it as a public static final **Block** in [`\src\main\java\pro\piconico\automata\registry\AutomataBlocks.java`](\src\main\java\pro\piconico\automata\registry\AutomataBlocks.java) (Creates **Item** for you)
3. Add it to [`\src\client\java\pro\piconico\automata\datagen\models\AutomataModelProvider.java`](\src\client\java\pro\piconico\automata\datagen\models\AutomataModelProvider.java)
4. Add it to [`\src\client\java\pro\piconico\automata\datagen\loot_table\AutomataBlockLootTableGeneratorProvider.java`](\src\client\java\pro\piconico\automata\datagen\loot_table\AutomataBlockLootTableProvider.java)
5. Add it to all **FabricLanguageProvider**s in [`\src\client\java\pro\piconico\automata\datagen\lang\`](\src\client\java\pro\piconico\automata\datagen\lang\)
6. Run **Data Generation** launch task

### Adding Persistent States

1. Create a class (**T** for example) that extends **PersistentState** in [`\src\main\java\pro\piconico\automata\world\`](\src\main\java\pro\piconico\automata\world\)
2. Give it a public static final **Codec\<T\>**
3. Register it as a public static final **PersistentStateType\<T\>** in [`\src\main\java\pro\piconico\automata\registry\AutomataPersistentStates.java`](\src\main\java\pro\piconico\automata\registry\AutomataPersistentStates.java)

### Adding Packets

1. Create a record (**T** for example) that implements **CustomPayload** in [`\src\main\java\pro\piconico\automata\network\packet\`](\src\main\java\pro\piconico\automata\network\packet\)
2. Register it as a public static final **CustomPayload.Type\<?, T\>** (C2S or S2C accordingly) in [`\src\main\java\pro\piconico\automata\registry\AutomataPackets.java`](\src\main\java\pro\piconico\automata\registry\AutomataPackets.java)
#### Client To Server (C2S)
3. Create a handler class in [`\src\main\java\pro\piconico\automata\network\handler\`](\src\main\java\pro\piconico\automata\network\handler\)
4. Register the handler in [`\src\main\java\pro\piconico\automata\registry\AutomataPackets.java`](\src\main\java\pro\piconico\automata\registry\AutomataPackets.java)

#### Server To Client (S2C)
3. Create a handler class in [`\src\client\java\pro\piconico\automata\client\network\handler\`](\src\client\java\pro\piconico\automata\client\network\handler\)
4. Register the handler in [`\src\client\java\pro\piconico\automata\client\AutomataClient.java`](\src\client\java\pro\piconico\automata\client\AutomataClient.java)

### Adding Screens

1. Create a class (**T** for example) that extends **ScreenHandler** in [`\src\main\java\pro\piconico\automata\screen\`](\src\main\java\pro\piconico\automata\screen\)
2. Register it as a public static final **ScreenHandlerType\<T\>** in [`\src\main\java\pro\piconico\automata\registry\AutomataScreenHandlers.java`](\src\main\java\pro\piconico\automata\registry\AutomataScreenHandlers.java)
3. Optional: Wire it up to a **BlockWithEntity**'s **onUse** function.
4. Create a class that extends **HandledScreen\<T\>** in [`\src\client\java\pro\piconico\automata\client\screen\`](\src\client\java\pro\piconico\automata\client\screen\)
5. Register it in [`\src\client\java\pro\piconico\automata\client\AutomataClient.java`](\src\client\java\pro\piconico\automata\client\AutomataClient.java)

### Adding Commands
1. Create a function that is private static **LiteralArgumentBuilder\<ServerCommandSource\>** in [`\src\main\java\pro\piconico\automata\registry\AutomataCommands.java`](\src\main\java\pro\piconico\automata\registry\AutomataCommands.java)
2. Add it to the command registration callback in *initialize*

### Adding Bot Jobs
1. Create a record (**T** for example) that implements **BotJob** in [`\src\main\java\pro\piconico\automata\bot\job\`](\src\main\java\pro\piconico\automata\bot\job\)
2. Register it as a public static final **BotJobType\<T\>** in [`\src\main\java\pro\piconico\automata\registry\AutomataBotJobs.java`](\src\main\java\pro\piconico\automata\registry\AutomataBotJobs.java)

### Adding Bots
1. [Add an item](#adding-items) that extends **BotItem**
2. [Add an entity](#living-entities) that extends **BotEntity**
3. Register a public static final **BotType** in [`\src\main\java\pro\piconico\automata\registry\AutomataBots.java`](\src\main\java\pro\piconico\automata\registry\AutomataBots.java)

### Adding Mixins

#### Main Mixins
1. Create a class with the @**Mixin** annotation in [`\src\main\java\pro\piconico\automata\mixin`](\src\main\java\pro\piconico\automata\mixin)
2. Add it to the "mixins" list in [`\src\main\resources\automata.mixins.json`](\src\main\resources\automata.mixins.json)

#### Client Mixins
1. Create a class with the @**Mixin** annotation in [`\src\client\java\pro\piconico\automata\client\mixin`](\src\client\java\pro\piconico\automata\client\mixin)
2. Add it to the "client" list in [`\src\client\resources\automata.client.mixins.json`](\src\client\resources\automata.client.mixins.json)


## License

This project is based on a template that is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.

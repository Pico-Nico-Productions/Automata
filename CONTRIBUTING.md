# Automata Contributor Guide

## Code Conventions

### Avoid Literals
Literal values are typically arbitrary so literals should be used to define final variables for adaptability and to avoid duplication. Developer facing values are the exception (e.g. **Codec** field names, **Exception**/log messages)
- Define all strings used as **Identifier**s and **RegistryKey**s in [**AutomataClientRegistry**](/src/main/java/pro/piconico/automata/registry/AutomataRegistry.java) for main code or in [**AutomataClientRegistry**](/src/client/java/pro/piconico/automata/client/registry/AutomataClientRegistry.java) for client code
- Define all strings used as texture names in [**AutomataClientTextures**](/src/client/java/pro/piconico/automata/client/registry/AutomataClientTextures.java)
- Define all colors in [**AutomataColors**](/src/client/java/pro/piconico/automata/client/design/AutomataColors.java)
- Define all strings used as translation keys in [**AutomataTexts**](/src/main/java/pro/piconico/automata/registry/AutomataTexts.java)
- Write all player facing strings in the **FabricLanguageProvider**s [here](/src/client/java/pro/piconico/automata/datagen/lang/)

### Avoid Null
Java has no explicit null safety so this repository uses **Optional\<\>** to explicitly declare a value is nullable



## Content Creation

### Texts

1. Create a public static final **String** to act as a translation key in [**AutomataTexts**](/src/main/java/pro/piconico/automata/registry/AutomataTexts.java)
2. Document which arguments the text expects if any
3. Create a public static **MutableText** function that converts the translation key into text
3. Add the key to all **FabricLanguageProvider**s [here](/src/client/java/pro/piconico/automata/datagen/lang/)
4. Run **Data Generation** launch task

Call **Text**.translatable with the translation key and arguments


### Translations

1. Create a class that extends **FabricLanguageProvider** [here](/src/client/java/pro/piconico/automata/datagen/lang/)
2. Copy the members of [**AutomataEnglishProvider**](/src/client/java/pro/piconico/automata/datagen/lang/AutomataEnglishProvider.java) into it
3. Replace the language code in the constructor
4. Translate all strings in **generateTranslations**
5. Run **Data Generation** launch task


### Components

1. Create a record (**TComponent** for example) [here](/src/main/java/pro/piconico/automata/component/)
2. Give it a public static final **Codec\<TComponent\>** and **PacketCodec<ByteBuf, TComponent>**
3. Register it as a public static final **ComponentType\<TComponent\>** in [**AutomataComponents**](/src/main/java/pro/piconico/automata/registry/AutomataComponents.java)


### Items

1. Create a class that extends **Item** [here](/src/main/java/pro/piconico/automata/item/)
2. Register it as a public static final **Item** in [**AutomataItems**](/src/main/java/pro/piconico/automata/registry/AutomataItems.java)
3. Add it to [**AutomataModelProvider**](/src/client/java/pro/piconico/automata/datagen/models/AutomataModelProvider.java)
4. Add it to all **FabricLanguageProvider**s [here](/src/client/java/pro/piconico/automata/datagen/lang/)
5. Run **Data Generation** launch task


### Blocks

1. Create a class that extends **Block** [here](/src/main/java/pro/piconico/automata/block/)
2. Register it as a public static final **Block** in [**AutomataBlocks**](/src/main/java/pro/piconico/automata/registry/AutomataBlocks.java) (Creates **Item** for you)
3. Add it to [**AutomataModelProvider**](/src/client/java/pro/piconico/automata/datagen/models/AutomataModelProvider.java)
4. Add it to [**AutomataBlockLootTableGeneratorProvider**](/src/client/java/pro/piconico/automata/datagen/loot_table/AutomataBlockLootTableProvider.java)
5. Add it to all **FabricLanguageProvider**s [here](/src/client/java/pro/piconico/automata/datagen/lang/)
6. Run **Data Generation** launch task


### Points Of Interest

1. Register a public static final **RegistryKey\<PointOfInterestType\>** in [**AutomataPointOfInterestTypes**](/src/main/java/pro/piconico/automata/registry/AutomataPointOfInterestTypes.java)


### Entities

#### Living Entities
1. Create a class (**TLivingEntity** for example) that extends **LivingEntity** [here](/src/main/java/pro/piconico/automata/entity/)
2. Register it as a public static final **EntityType\<TLivingEntity\>** in [**AutomataEntities**](/src/main/java/pro/piconico/automata/registry/AutomataEntities.java)
3. Create a class that extends **LivingEntityRenderer** [here](/src/client/java/pro/piconico/automata/client/render/entity/)
4. Register it in [**AutomataClientRenderers**](/src/client/java/pro/piconico/automata/client/registry/AutomataClientRenderers.java)

#### Block Entities
1. [Create a block](#blocks) that extends **BlockWithEntity**
2. Create a class (**TBlockEntity** for example) that extends **BlockEntity** [here](/src/main/java/pro/piconico/automata/block/entity/)
3. Register it as a public static final **BlockEntityType\<TBlockEntity\>** in [**AutomataEntities**](/src/main/java/pro/piconico/automata/registry/AutomataEntities.java)


### Persistent States

1. Create a class (**TPersistentState** for example) that extends **PersistentState** [here](/src/main/java/pro/piconico/automata/world/)
2. Give it a public static final **Codec\<TPersistentState\>**
3. Register it as a public static final **PersistentStateType\<TPersistentState\>** in [**AutomataPersistentStates**](/src/main/java/pro/piconico/automata/registry/AutomataPersistentStates.java)


### Packets

1. Create a record (**TCustomPayload** for example) that implements **CustomPayload** [here](/src/main/java/pro/piconico/automata/network/packet/)
2. Register it as a public static final **CustomPayload.Type\<?, TCustomPayload\>** (C2S or S2C accordingly) in [**AutomataPackets**](/src/main/java/pro/piconico/automata/registry/AutomataPackets.java)

#### Client To Server (C2S)
3. Create a handler class [here](/src/main/java/pro/piconico/automata/network/handler/)
4. Register the handler in [**AutomataPackets**](/src/main/java/pro/piconico/automata/registry/AutomataPackets.java)

#### Server To Client (S2C)
3. Create a handler class [here](/src/client/java/pro/piconico/automata/client/network/handler/)
4. Register the handler in [**AutomataClientPacketHandlers**](/src/client/java/pro/piconico/automata/client/registry/AutomataClientPacketHandlers.java)


### Messages (owo-lib)

1. Create a record (**TMessage** for example) [here](/src/main/java/pro/piconico/automata/network/message/)
2. Optional: Give it a public static final **Endec\<TMessage\>** (Optional because owo-lib messages can automatically serialize certain things and this repository uses them for light-weight communication)

#### Client To Server (C2S)
3. Give it a public static void function(**TMessage**, **ServerAccess**) that handles the message
4. Register it in [**AutomataMessages**](/src/main/java/pro/piconico/automata/registry/AutomataMessages.java)

#### Server To Client (S2C)
3. Create a class with a public static void function(**TMessage**, **ClientAccess**) that handles the message [here](/src/client/java/pro/piconico/automata/client/network/message/)
4. Register it in [**AutomataClientMessageHandlers**](/src/client/java/pro/piconico/automata/client/registry/AutomataClientMessageHandlers.java)


### Render Pipelines

1. Register a public static final **RenderPipeline** in [**AutomataClientRenderPipelines**](/src/client/java/pro/piconico/automata/client/registry/AutomataClientRenderPipelines.java)


### Screens

1. Create a class (**TScreenHandler** for example) that extends **ScreenHandler** [here](/src/main/java/pro/piconico/automata/screen/)
2. Register it as a public static final **ScreenHandlerType\<TScreenHandler\>** in [**AutomataScreenHandlers**](/src/main/java/pro/piconico/automata/registry/AutomataScreenHandlers.java)
3. Optional: Call **PlayerEntity**'s **openHandledScreen** in a **BlockWithEntity**'s **onUse** and/or an **Item**'s **use** function(s)
4. Create a class that extends **HandledScreen\<TScreenHandler\>** [here](/src/client/java/pro/piconico/automata/client/gui/screen/)
5. Register it in [**AutomataClientScreens**](/src/client/java/pro/piconico/automata/client/registry/AutomataClientScreens.java)


### Commands

1. Create a class with a public static **LiteralArgumentBuilder\<ServerCommandSource\>** function [here](/src/main/java/pro/piconico/automata/command/)
2. Append it to the mod command builder in [**AutomataCommands**](/src/main/java/pro/piconico/automata/registry/AutomataCommands.java)


### Mixins

#### Main Mixins
1. Create a class with the @**Mixin** annotation [here](/src/main/java/pro/piconico/automata/mixin)
2. Add it to the "mixins" list [here](/src/main/resources/automata.mixins.json)

#### Client Mixins
1. Create a class with the @**Mixin** annotation [here](/src/client/java/pro/piconico/automata/client/mixin)
2. Add it to the "client" list [here](/src/client/resources/automata.client.mixins.json)


### Bot Devices

#### Item Bot Devices
1. [Create an item](#items)
2. Call **PlayerEntity**'s **openHandledScreen** passing a new **ItemBotDevice** somewhere in the item's code
3. Create a class that extends **BotDeviceScreen\<ItemBotDeviceScreenHandler\>** [here](/src/client/java/pro/piconico/automata/client/gui/screen/)
4. Add it to the item bot device screen router in [**AutomataClientScreens**](/src/client/java/pro/piconico/automata/client/registry/AutomataClientScreens.java)

#### Block Bot Devices
1. [Create a block entity](#block-entities) that extends **BlockBotDevice**
2. [Create a screen](#screens) where the handler (**TScreenHandler** for example) extends **BotDeviceScreenHandler** and the screen extends **BotDeviceScreen\<TScreenHandler\>**

#### Bot Device Types
1. Create a class (**TBotDevice** for example) that implements **BotDevice** [here](/src/main/java/pro/piconico/automata/bot/device/)
2. Give it a public record (**Id** for example) that implements **BotDevice.Id** and give **Id** a public static final **MapCodec\<Id\>**
3. Give **TBotDevice** a public static **Optional\<TBotDevice\>** function(**PlayerEntity**, **Id**) that can independently resolve **TBotDevice**s from the given **Id**
4. Ensure your **TBotDevice** invokes **BotDevice**'s team changed event for synchronization
5. Register it as a public static final **BotDeviceType\<Id, TBotDevice\>** in [**AutomataBotDevices**](/src/main/java/pro/piconico/automata/registry/AutomataBotDevices.java)
6. [Create a screen](#screens) where the handler (**TScreenHandler** for example) extends **BotDeviceScreenHandler** and the screen extends **BotDeviceScreen\<TScreenHandler\>**


### Bot Jobs

1. Create a record (**TBotJob** for example) that implements **BotJob** [here](/src/main/java/pro/piconico/automata/bot/job/)
2. Give it a public static final **MapCodec\<TBotJob\>**
3. Register it as a public static final **BotJobType\<TBotJob\>** in [**AutomataBotJobs**](/src/main/java/pro/piconico/automata/registry/AutomataBotJobs.java)
4. Optional: Give it a color in the job function in [**AutomataColors**](/src/client/java/pro/piconico/automata/client/registry/AutomataColors.java) to have it render an outline


### Bots

1. [Create an item](#items) that extends **BotItem**
2. [Create an entity](#living-entities) that extends **BotEntity**
3. Register a public static final **BotType** in [**AutomataBots**](/src/main/java/pro/piconico/automata/registry/AutomataBots.java)
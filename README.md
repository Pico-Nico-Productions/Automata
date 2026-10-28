# Automata

## License
- Automata
    - [![License](https://img.shields.io/badge/License-CC0%201.0%20Universal-yellow.svg?style=flat-square)](https://creativecommons.org/publicdomain/zero/1.0/deed.en)
- GUI Library ([owo-lib](https://github.com/wisp-forest/owo-lib))
    - [![License](https://img.shields.io/badge/License-MIT-red.svg?style=flat-square)](http://opensource.org/licenses/MIT)



## Running The Mod

1. Open repository in VS Code ([Automata Workflow.bat](/Automata%20Workflow.bat) opens the relevant programs for development)
2. Install a version of Java compatible with the [gradle build](/build.gradle)
3. Install the [Extension Pack for Java](vscode:extension/vscjava.vscode-java-pack)
4. Open the Gradle tab and run the *ide/vscode* task (installs isolated Fabric development environment)
5. Open [generated launch profiles](/.vscode/launch.json), remove all *"projectName"* properties, and make *Data Generation*'s *"cwd"* the same as the others (fixes known bug)
6. Run [Java: Clean Java Language Server Workspace](command:java.clean.workspace) in the command palette
7. Run the [Data Generation](command:workbench.action.debug.selectandstart) launch task (because generated data is git ignored)
8. Run the [Minecraft Client](command:workbench.action.debug.selectandstart) launch task

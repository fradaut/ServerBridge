# ServerBridge

ServerBridge is a Paper 26.2 plugin that lets players walk into a configured
border region and move directly to another Minecraft server. It uses the
vanilla transfer protocol, so no Velocity/BungeeCord proxy is required.

## Requirements

- Paper 26.2 on both servers
- Java 25
- The destination server must accept transfers (`accepts-transfers=true` in
  `server.properties`)

## Build and install

```bash
./gradlew build
```

Copy `build/libs/ServerBridge-1.0.0.jar` to the `plugins` folder of both
servers, start once, and edit `plugins/ServerBridge/config.yml`. Add one bridge
box for every direction you want players to travel, then run `/sb reload`.
The optional `destination.arrival` block selects the world, coordinates and
view direction on the other server. ServerBridge carries it in a vanilla
transfer cookie and consumes it once when the player arrives.

To automatically deploy every successful build, create an untracked
`gradle.properties` in the project root containing:

```properties
serverBridge.pluginsDirectory=/path/to/server/plugins
```

Then every `./gradlew build` updates that server automatically. You can also
override the destination with `-PserverBridge.pluginsDirectory=...`, or run
`./gradlew deployPlugin` to rebuild and deploy the jar directly.

Commands:

- `/sb list` — list enabled bridges
- `/sb go <bridge>` — transfer without entering the region
- `/sb reload` — reload the config (`serverbridge.admin`)

Players need `serverbridge.use` (granted by default). Transfers cannot preserve
inventory or other plugin data by themselves; use a shared database or an
inventory synchronisation plugin if both servers should behave as one world.

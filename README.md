# ServerBridge

ServerBridge 是一款支援 Paper 26.2 的跨服連接插件。玩家走進設定好的邊界區域後，
即可直接移動到另一台 Minecraft 伺服器，營造出跨世界移動的體驗。

插件使用 Minecraft 原生 Transfer Protocol，因此不需要安裝 Velocity 或
BungeeCord 代理伺服器。

## 系統需求

- 兩台伺服器皆使用 Paper 26.2
- Java 25
- 目的伺服器的 `server.properties` 必須設定：

```properties
accepts-transfers=true
```

## 建置與安裝

執行：

```bash
./gradlew build
```

建置完成後，將 `build/libs/ServerBridge-1.0.0.jar` 複製到兩台伺服器的
`plugins` 資料夾。啟動伺服器一次，再編輯
`plugins/ServerBridge/config.yml`。

每個移動方向都需要設定一個 bridge 區域。例如 A 伺服器前往 B 伺服器，以及 B
伺服器返回 A 伺服器，需要分別在兩端建立設定。完成後執行：

```text
/sb reload
```

`destination.arrival` 是選用設定，可指定玩家抵達另一台伺服器時的世界、座標與
視角。ServerBridge 會透過 Minecraft 原生跨服 Cookie 傳遞這些資訊，並在玩家
抵達後使用一次及清除。

## 自動部署

若要在每次成功建置後自動更新伺服器內的插件，請在專案根目錄建立不會提交至
GitHub 的 `gradle.properties`：

```properties
serverBridge.pluginsDirectory=/path/to/server/plugins
```

設定後，每次執行下列指令都會自動將最新版 JAR 部署到指定伺服器：

```bash
./gradlew build
```

也可以只執行建置與部署：

```bash
./gradlew deployPlugin
```

臨時指定其他目的地時，可以使用：

```bash
./gradlew deployPlugin -PserverBridge.pluginsDirectory=/path/to/server/plugins
```

## 指令

- `/sb list`：列出所有已啟用的連接點
- `/sb go <bridge>`：不進入連接區域，直接前往指定伺服器
- `/sb reload`：重新載入設定

## 權限

- `serverbridge.use`：允許自動跨服及使用 `/sb go`，預設所有玩家皆有權限
- `serverbridge.admin`：允許使用 `/sb reload`，預設僅伺服器管理員有權限

## 玩家資料同步

ServerBridge 負責玩家的跨服移動與目的座標傳遞，不會自行同步背包、終界箱、
經濟、經驗值或其他插件資料。

如果兩台伺服器需要呈現為同一個世界系統，請另外搭配共享資料庫或玩家資料同步
插件。

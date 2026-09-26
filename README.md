# ServerBridge

ServerBridge 是 Minecraft 26.2 的跨伺服器連接插件。

玩家只要走進管理員指定的區域，就會自動前往另一台伺服器；抵達後還可以出現在
指定的世界與座標。使用感覺就像從一個世界走到另一個世界，不需要玩家先退出再
手動輸入另一台伺服器的位址。

## 支援版本

- 目標遊戲版本：Minecraft 26.2
- 實際測試環境：Paper 26.2 build 129
- Java：25

本插件使用 Paper API 開發。其他相容 Paper 的伺服器核心可能也能使用，但目前
沒有逐一測試；若希望最穩定，建議兩端都使用 Paper 26.2。

## 開始前要知道的事

ServerBridge 是把玩家從「伺服器 A」送到「伺服器 B」，並不是把兩張地圖合併成
同一個檔案。因此：

- 兩台伺服器必須各自能正常啟動，且玩家能連線
- ServerBridge 必須安裝在兩台伺服器上
- 若要能來回移動，A 與 B 都要設定一個前往對方的連接區域
- 背包、經驗、經濟與其他插件資料不會自動同步
- 原生跨服時，Minecraft 客戶端仍可能短暫顯示載入畫面

## 第一步：安裝插件

先建置插件：

```bash
./gradlew build
```

建置完成後，JAR 位於：

```text
build/libs/ServerBridge-1.0.0.jar
```

把這個 JAR 放入伺服器 A 和伺服器 B 的 `plugins` 資料夾，然後啟動兩台伺服器
一次。插件會建立：

```text
plugins/ServerBridge/config.yml
```

## 第二步：允許原生跨服連線

兩台伺服器都要停止，接著打開各自的 `server.properties`，找到：

```properties
accepts-transfers=false
```

改成：

```properties
accepts-transfers=true
```

儲存後重新啟動伺服器。只修改其中一台是不夠的，兩端都要允許 Transfer。

## 第三步：確認連線位址與連接埠

假設有以下兩台伺服器：

| 名稱 | 玩家可連線位址 | 連接埠 |
| --- | --- | ---: |
| 生存伺服器 A | `survival.example.com` | `25565` |
| 資源伺服器 B | `resource.example.com` | `25566` |

以上網域只是教學範例，不能直接拿來連線。請全部換成你自己的伺服器網域或 IP。

設定中的 `destination.host` 必須是「玩家的電腦可以連到的位址」，而不是只有伺服器
自己能使用的內部位址。

常見情況：

- 公開伺服器：填網域名稱或公開 IP，例如 `resource.example.com`
- 同一個區域網路：可填玩家能連到的區網 IP，例如 `192.168.1.50`
- 同一台電腦開兩個伺服器：仍要填玩家可以連到這台電腦的 IP 或網域
- 不要隨便填 `127.0.0.1` 或 `localhost`；轉服後是玩家的 Minecraft 客戶端去連線，
  這兩個位址通常會指向玩家自己的電腦

`destination.port` 要填目的伺服器 `server.properties` 裡的 `server-port`。若玩家
無法直接用該位址和連接埠加入伺服器，ServerBridge 也無法將玩家送過去。請同時
確認路由器轉發和防火牆已開放該連接埠。

## 第四步：取得連接區域座標

連接區域是一個長方體。玩家只要進入這個範圍，就會自動跨服。

1. 在遊戲內站到區域的一個角落，按 `F3` 查看目前的 X、Y、Z。
2. 記下這組座標，作為 `region.min`。
3. 走到長方體對面的另一個角落，再記下 X、Y、Z，作為 `region.max`。
4. `world` 要填這個區域所在的世界資料夾名稱，通常是 `world`、`world_nether`
   或 `world_the_end`。

`min` 和 `max` 寫反也沒有關係，插件會自動判斷較小與較大的數字。

例如，要讓玩家走進 X=100～104、Y=60～70、Z=-5～5 的門：

```yaml
region:
  min: { x: 100, y: 60, z: -5 }
  max: { x: 104, y: 70, z: 5 }
```

## 第五步：設定伺服器 A 前往 B

打開伺服器 A 的 `plugins/ServerBridge/config.yml`，在 `bridges` 底下加入：

```yaml
bridges:
  survival-to-resource:
    enabled: true
    display-name: "資源世界"
    world: world
    region:
      min: { x: 100, y: 60, z: -5 }
      max: { x: 104, y: 70, z: 5 }
    destination:
      host: resource.example.com
      port: 25566
      arrival:
        world: world
        x: 10.5
        y: 65
        z: 10.5
        yaw: 180
        pitch: 0
```

各欄位的意思：

| 欄位 | 說明 |
| --- | --- |
| `survival-to-resource` | 連接點 ID，之後 `/sb go` 會用到；不可重複 |
| `enabled` | `true` 表示啟用，`false` 表示暫停使用 |
| `display-name` | 玩家轉服時看到的名稱 |
| `world` | 連接區域位於伺服器 A 的哪個世界 |
| `region.min/max` | 玩家進入後會觸發跨服的長方體範圍 |
| `destination.host` | 玩家可連到伺服器 B 的 IP 或網域 |
| `destination.port` | 伺服器 B 的連接埠 |
| `arrival.world` | 抵達伺服器 B 後所在的世界 |
| `arrival.x/y/z` | 抵達伺服器 B 後的座標 |
| `arrival.yaw` | 玩家面向方向，`0` 南、`90` 西、`180` 北、`-90` 東 |
| `arrival.pitch` | 上下視角，`0` 平視、正數向下、負數向上 |

`arrival` 整段都可以省略。省略後，玩家會出現在伺服器 B 自己決定的登入位置，
通常是上次離線的位置或出生點。

## 第六步：設定伺服器 B 返回 A

如果希望玩家可以走回去，還要打開伺服器 B 的
`plugins/ServerBridge/config.yml`，建立反方向連接點：

```yaml
bridges:
  resource-to-survival:
    enabled: true
    display-name: "生存世界"
    world: world
    region:
      min: { x: 8, y: 60, z: 8 }
      max: { x: 12, y: 70, z: 12 }
    destination:
      host: survival.example.com
      port: 25565
      arrival:
        world: world
        x: 98.5
        y: 65
        z: 0.5
        yaw: 90
        pitch: 0
```

請不要把抵達座標放在另一端的觸發區域裡，否則玩家剛抵達就可能立刻被送回去。
建議讓抵達點與觸發區域至少相隔幾格。

## 在遊戲內套用與測試

目前連接區域需要在 `config.yml` 中建立；遊戲內指令負責重新載入、查看及測試，
不需要每次修改設定後重啟整台伺服器。

管理員在修改 `config.yml` 後輸入：

```text
/sb reload
```

接著確認插件讀到哪些連接點：

```text
/sb list
```

不走進區域也可以直接測試指定連接點：

```text
/sb go survival-to-resource
```

一般玩家只需要走進管理員設定好的區域，不必輸入任何指令。

### 指令一覽

| 指令 | 用途 |
| --- | --- |
| `/sb list` | 列出目前已啟用的連接點 |
| `/sb go <連接點 ID>` | 直接測試或前往指定連接點 |
| `/sb reload` | 儲存設定檔後重新載入 |

### 權限一覽

| 權限 | 用途 | 預設 |
| --- | --- | --- |
| `serverbridge.use` | 允許進入區域跨服及使用 `/sb go` | 所有玩家 |
| `serverbridge.admin` | 允許使用 `/sb reload` | OP |

## 常見問題

### 走進區域後沒有反應

依序檢查：

1. `/sb list` 是否看得到該連接點。
2. `enabled` 是否為 `true`。
3. `world` 名稱是否正確。
4. 玩家是否有 `serverbridge.use` 權限。
5. 修改設定後是否執行過 `/sb reload`。

### 顯示無法連線到目的伺服器

先從玩家的 Minecraft 伺服器列表，直接測試
`destination.host:destination.port`。若直接連線也失敗，請檢查目的伺服器是否啟動、
IP／網域、連接埠、防火牆和路由器轉發。

### 抵達後沒有出現在設定座標

確認目的伺服器也已安裝 ServerBridge，而且 `arrival.world` 指定的世界已載入、名稱
完全相同。世界名稱不是遊戲內顯示名稱，而是伺服器內的世界資料夾名稱。

### 玩家抵達後立刻被送回

抵達座標落在目的伺服器的另一個 bridge 觸發區域內。把 `arrival.x/y/z` 移到區域
外面即可。

### 背包和金錢沒有跟著過去

這是正常情況。ServerBridge 只處理跨服移動和抵達座標，不會同步背包、終界箱、
經驗、經濟或其他插件資料。若兩台伺服器需要共用資料，必須另外安裝玩家資料同步
插件，或使用共享資料庫。

## 自動部署（開發者選用）

若要在每次成功建置後自動更新測試伺服器內的插件，請在專案根目錄建立不會提交
至 GitHub 的 `gradle.properties`：

```properties
serverBridge.pluginsDirectory=/path/to/server/plugins
```

設定後執行：

```bash
./gradlew build
```

也可以只執行建置與部署：

```bash
./gradlew deployPlugin
```

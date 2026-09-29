# Modpack1

Minecraft **1.20.1** / Forge **47.4.22** 整合包的魔改内容：KubeJS 脚本、数据包，以及三个自建模组的源码。

## 目录

- `startup_scripts/` `server_scripts/` `client_scripts/` — KubeJS 脚本：物品 / 方块 / 流体 / 化学品注册，配方与服务端事件，客户端表现
- `data/` `assets/` — 数据包与资源包内容（配方、标签、贴图、模型、语言文件）
- `config/` — KubeJS 配置
- `tools/` — 离线工具脚本
- `nannan_multiblock/` — 自建多方块结构宿主模组（相变控制炉）
- `reiryoku_ae2_compat/` — 灵力（Urushi）× AE2 兼容层
- `openblocks_anvil_compat/` — 让 OpenBlocks 的自动化铁砧触发 `AnvilUpdateEvent` 的小型兼容 mod

## 主要模组

沉浸工程、机械动力、热力膨胀、工业先锋、通用机械、拔刀剑、无尽贪婪重制版、农夫乐事等。

## 构建

三个子模组都不依赖 Gradle 与网络，直接用脚本编译（需要 JDK 17）：在各自目录运行 `build.ps1`，
产物在对应的 `build/libs/` 下。

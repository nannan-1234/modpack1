# Modpack1

Minecraft **1.20.1** / Forge **47.4.22** 整合包的魔改内容：KubeJS 脚本、数据包，以及三个自建模组的源码。

## 目录

- `kubejs/` — KubeJS 内容根目录（直接对应游戏实例里的 `kubejs/`）
  - `startup_scripts/` `server_scripts/` `client_scripts/` — 脚本：注册、配方、事件、客户端表现
  - `data/` `assets/` — 数据包与资源包内容
  - `config/` — KubeJS 配置
- `mods/` — 三个独立 Java 模组
  - `nannan_multiblock/` — 自建多方块结构宿主模组（相变控制炉）
  - `reiryoku_ae2_compat/` — 灵力（Urushi）× AE2 兼容层
  - `openblocks_anvil_compat/` — 让 OpenBlocks 的自动化铁砧触发 `AnvilUpdateEvent`
- `tools/` — 构建、同步和数值工具
- `docs/` — 项目文档与设计记录

## 主要模组

沉浸工程、机械动力、热力膨胀、工业先锋、通用机械、拔刀剑、无尽贪婪重制版、农夫乐事等。

## 构建

三个子模组都通过各自目录下的脚本构建（需要 JDK 17），产物在各自的 `build/libs/` 下。

三个子模组都已迁移到 ForgeGradle（Gradle wrapper 8.1.1 + ForgeGradle 6.0.24 +
official mappings）；`build.sh` / `build.ps1` 现在调用 wrapper，首次构建需要联网，
缓存完整后可加 `--offline`。

- Linux / WSL：进入 `mods/<模组目录>` 运行 `./build.sh`
- Windows：进入 `mods\<模组目录>` 运行 `powershell -ExecutionPolicy Bypass -File .\build.ps1`

Forge、Mekanism、JEI、AE2 等公共依赖由 Maven 解析；Urushi、KubeJS、OpenBlocks
的本地 jar 放在对应模组被忽略的 `libs/` 目录，详见各模组 README。

项目约定与环境说明见 `AGENTS.md` §14。

## KubeJS 实时编辑

Windows 客户端仍然读取实例自己的 `kubejs/` 目录。Linux 侧修改后，用同步脚本把六个
KubeJS 内容目录写到实例；`exported/`、`logs/` 和运行时生成的
`kubejs/startup_scripts/@recipes/` 不会被删除。

```bash
# 默认同步到 1.20.1-Forge_47.4.22-wip
tools/sync_kubejs.sh

# 指定实例
tools/sync_kubejs.sh 1.20.1-Forge_47.4.22TEST

# 只看差异，不写入
DRY_RUN=1 tools/sync_kubejs.sh
```

需要保存后自动同步时：

```bash
tools/watch_kubejs.sh 1.20.1-Forge_47.4.22-wip
```

当前系统没有 `inotifywait`，监视脚本会退回每秒一次的元数据轮询；安装
`inotify-tools` 后会自动使用 inotify。同步脚本只复制文件，游戏内仍需执行：

- `/reload`：重载 server scripts 与数据包
- `F3+T`：重载客户端资源
- 修改 `startup_scripts` 或注册表内容后重启游戏

# Java 版 · Minor Safety Mod（原模组，授权分发）

- `minorsafety-1.0.0.jar` —— 原模组 JAR（Forge 1.20.1 客户端模组，直接放入 `mods/` 使用）
- `src/` —— 反编译源码（CFR 0.152 输出），供学习与二次开发参考

## 安装（Java 版）

1. 安装 Minecraft Java 1.20.1 + Forge 1.20.1（如使用 HMCL/PCL2 启动器）
2. 将 `minorsafety-1.0.0.jar` 放入 `.minecraft/mods/`
3. 启动游戏进存档，自动弹出身份验证

## 指令

| 指令 | 作用 |
| --- | --- |
| `/verify` | 打开身份验证界面 |
| `/minorsafety` | 同上 |
| `/msverify` | 同上 |

## 功能对照（原版）

- 进世界强制验证（姓名+年龄）
- 年龄 < 18：限玩 5 分钟（6000 tick），到时锁定物品栏与操作
- 锁定界面「我已长大」按钮 → 重新验证
- 年龄 ≥ 18：按年龄段弹趣味提示（多结局文案 56 条）
- 数据持久化：`config/minorsafety/players.json`

## 许可证

原模组 **CC BY-NC-SA 4.0**（© SereneCloud）。本目录内容经作者授权随转制仓库分发，仅限学习与二次开发。

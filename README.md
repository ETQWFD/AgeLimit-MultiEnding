# AgeLimit-MultiEnding · 年龄限制游戏时长，多结局版

未成年人保护模组 —— Java 版（Forge 1.20.1）**转制**为基岩版 Addon。

> **原模组**：Minor Safety Mod v1.0.0（Forge 1.20.1，作者 **SereneCloud**，许可证 CC BY-NC-SA 4.0）
> **本仓库**：经原作者授权的二次开发转制版，许可证 **MIT**（保留原作者署名），二次开发：**ET（ET协会）**
> 详见 [NOTICE.md](NOTICE.md)

## 包含内容

| 目录 | 说明 |
| --- | --- |
| `addon-src/` | 基岩版 Addon 完整源码（行为包 `BP/` + 资源包 `RP/`），逻辑在 `BP/scripts/main.js` |
| `*.mcaddon / *.mcpack` | 打包好的基岩版安装包（可导入 Minecraft） |
| `java/` | Java 版原模组（JAR + 反编译源码，仅供学习/二次开发参考） |

## 功能

- **身份验证**：进世界强制表单验证（姓名 + 年龄）
- **未成年人限时**：年龄 < 18 限玩 5 分钟，HUD 实时倒计时
- **到时锁定**：移动/跳跃/挖掘/攻击全锁定，弹出"游玩时间已结束"
- **我已长大**：锁定后可点按钮重新验证
- **成人多结局**：按年龄段弹出 56 条趣味提示（18 岁 → LUCA 生命始祖）
- **验证令牌**：进世界自动发放，右键打开验证
- **聊天指令兜底**：`/verify` `/minorsafety` `/msverify` `!verify`

## 兼容性

- 基岩版：**1.20.60 ~ 1.26+**（Script API 稳定版）。1.2.x~1.19 时代无脚本系统，物理上无法兼容，特此说明。
- Java 版：Forge 1.20.1（原模组直接可用）。

## 安装（基岩版）

1. 下载 Release 中的 `.mcaddon`
2. 用 Minecraft 打开导入，进世界自动弹出验证

## 构建

- Addon 源码改动后：`BP/`、`RP/` 分别打成 zip（或合包 `.mcaddon`）即可。
- `npm`/编译工具非必需，脚本为原生 ESM JavaScript。

## 许可证

MIT —— 见 [LICENSE](LICENSE)。原模组为 CC BY-NC-SA 4.0，经作者授权二次开发分发。

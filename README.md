# Offline Mode Fix (NeoForge)

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
![Minecraft](https://img.shields.io/badge/Minecraft-26.3-green)
![NeoForge](https://img.shields.io/badge/NeoForge-26.3.x-orange)

**纯客户端 mod · 修复离线服务器上"收到聊天消息就卡死几秒"的原版 bug**

> A client-side NeoForge mod that fixes chat-message freezing on offline-mode Minecraft servers.

---

## 这是什么问题

Minecraft **26.3** 原版在收到其他玩家的聊天消息时，会调用 `Minecraft.isBlocked()` 检查"发送者是否被我屏蔽"。这个检查需要访问 Mojang 的账户服务（屏蔽列表 / blocklist）：

- **正版账号**：认证成功，屏蔽列表拉取一次后本地缓存，无感。
- **离线账号**（v3 UUID）：认证**必然失败**，于是每次收到消息都在**渲染线程上同步强制拉取**（`forceFetchBlockList`），失败后**不缓存**，下一条消息再拉一次。

结果就是：离线服务器上**每一条聊天消息都会把客户端挂起 1.5~12.5 秒**（实测记录，连接超时时更长）。单人游戏不走这条路径所以完全无感——这也是很多人误以为是 mod 冲突的原因。

这是 Mojang 官方 bug 追踪器 [WEB-5587](https://bugs.mojang.com/browse/WEB-5587) 的离线模式变种：Mojira 上报告的是 API 故障导致的反复重试，离线账号则是 100% 触发。

## 修复方式

对以下三个注入点，检测到**离线 UUID（v3 / NIL）**时直接短路：

| 注入点 | 原版行为 | 修复后 |
|---|---|---|
| `Minecraft.isBlocked(UUID)` | 同步请求 Mojang 屏蔽列表 | 对离线 UUID 直接返回 `false`，**零网络请求** |
| `Minecraft.isTextFilteringEnabled()` | 依赖账户服务 | 离线时禁用文本过滤 |
| `Minecraft.prepareForMultiplayer()` | 进服前拉取 KeyPair/证书 | 离线时跳过（必然失败的操作） |

**正版（v4）UUID 完全不受影响**，走原版逻辑——所以带正版玩家的服务器也可以装。

实测效果：修复前聊天显示延迟 1.5~12.5 秒；修复后 **22 条消息全部 ≤ 15ms**。

## 安装

- Minecraft **26.3** + NeoForge **26.3.x**，Java 25
- 纯客户端 mod，服务端不需要装
- 把 jar 放进 `.minecraft/mods/`（或对应实例的 `versions/<版本>/mods/`）

## 构建

标准 ModDevGradle 工程（Gradle 9.9.2 + JDK 25）：

```bash
./gradlew build     # 产物在 build/libs/
```

首次构建会联网下载 NeoForge 与依赖，之后走本地缓存。也可在 IDEA 中直接运行 Gradle build 或 runClient。

## 致谢

- 逻辑移植自 [louiszn/offline-mode-fix](https://codeberg.org/louiszn/offline-mode-fix)（Fabric 版，MIT）——本项目是其思路在 NeoForge 26.3 上的移植实现
- 诊断过程受益于 [spark profiler](https://spark.lucko.me/) 抓到的 `isBlocked → forceFetchBlockList` 调用链

## 许可

MIT

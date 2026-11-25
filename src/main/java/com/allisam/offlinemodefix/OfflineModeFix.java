package com.allisam.offlinemodefix;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/**
 * Offline Mode Fix (NeoForge) — 修复离线模式下聊天消息触发
 * Mojang 屏蔽列表同步网络请求导致的客户端冻结。
 *
 * 核心逻辑见 mixin/OfflineClientMixin（通过 neoforge.mods.toml 的 [[mixins]] 注册）。
 * 逻辑参考 louiszn 的 Offline Mode Fix (MIT, Fabric)。
 */
@Mod(value = "offlinemodefix", dist = Dist.CLIENT)
public class OfflineModeFix {
    public OfflineModeFix() {
        // 所有修复都在 OfflineClientMixin 中，无需运行时初始化。
    }
}

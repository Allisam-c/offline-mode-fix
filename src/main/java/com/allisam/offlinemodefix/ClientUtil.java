package com.allisam.offlinemodefix;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

import java.util.UUID;

/**
 * 判定当前客户端账号是否为"离线/未认证"账号。
 *
 * 离线模式的 UUID 是 Version 3（v3）UUID（由玩家名哈希生成），
 * 正版账号是 Version 4。NIL UUID 用于系统消息。
 */
public final class ClientUtil {
    private ClientUtil() {}

    public static boolean isUnauthenticatedClient() {
        return isUnauthenticatedUUID(Minecraft.getInstance().getUser().getProfileId());
    }

    public static boolean isUnauthenticatedUUID(UUID uuid) {
        return uuid == null || uuid.version() == 3 || uuid.equals(Util.NIL_UUID);
    }
}

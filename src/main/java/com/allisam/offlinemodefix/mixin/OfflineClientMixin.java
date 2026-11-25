package com.allisam.offlinemodefix.mixin;

import com.allisam.offlinemodefix.ClientUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * 核心修复：
 * 1. isBlocked — 原版在收到聊天消息/处理玩家时会调用此方法检查发送者是否被屏蔽，
 *    离线 UUID 认证失败导致 blocklist 每次都通过网络同步强制拉取（forceFetchBlockList），
 *    挂起 1.5~12.5 秒（Mojira WEB-5587 的离线变种）。对离线 UUID 直接返回 false，
 *    完全跳过网络请求。
 * 2. isTextFilteringEnabled — 文本过滤同样依赖账户服务，离线下禁用。
 * 3. prepareForMultiplayer — 加入多人世界前的账户服务准备（KeyPair/证书），
 *    离线账号必然失败，跳过以减少无谓的认证请求。
 *
 * 仅影响 v3 离线 UUID / NIL / null；正版（v4）UUID 走原版逻辑，不受影响。
 */
@Mixin(Minecraft.class)
public class OfflineClientMixin {

    @Inject(method = "isBlocked", at = @At("HEAD"), cancellable = true)
    private void offlinemodefix$skipBlocklistCheck(UUID uuid, CallbackInfoReturnable<Boolean> cir) {
        if (ClientUtil.isUnauthenticatedUUID(uuid)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isTextFilteringEnabled", at = @At("HEAD"), cancellable = true)
    private void offlinemodefix$disableTextFiltering(CallbackInfoReturnable<Boolean> cir) {
        if (ClientUtil.isUnauthenticatedClient()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "prepareForMultiplayer", at = @At("HEAD"), cancellable = true)
    private void offlinemodefix$skipMultiplayerPreparation(CallbackInfo ci) {
        if (ClientUtil.isUnauthenticatedClient()) {
            ci.cancel();
        }
    }
}

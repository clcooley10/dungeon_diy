package net.drdooley.dungeon_diy.Screen;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum CodexPageEnum {
    NODE_VIEW_EDIT(0),
    REPL_PREFAB_IMPORT(1);

    private final int id;
    CodexPageEnum(int id) { this.id = id; }

    public int id() { return id; }

    public static CodexPageEnum fromId(int id) {
        for (CodexPageEnum page : values()) {
            if (page.id == id) {
                return page;
            }
        }
        return NODE_VIEW_EDIT;
    }

    public static final StreamCodec<ByteBuf, CodexPageEnum> CODEX_PAGE_CODEC =
      ByteBufCodecs.INT.map(
        CodexPageEnum::fromId,
        CodexPageEnum::id
      );
}

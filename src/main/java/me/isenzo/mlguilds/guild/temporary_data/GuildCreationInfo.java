package me.isenzo.mlguilds.guild.temporary_data;

import lombok.Getter;

import java.util.UUID;

@Getter
public class GuildCreationInfo {

    private final UUID playerUUID;
    private final String guildName;
    private final String guildTag;

    public GuildCreationInfo(UUID playerUUID, String guildName, String guildTag) {
        this.playerUUID = playerUUID;
        this.guildName = guildName;
        this.guildTag = guildTag;
    }
}

package me.isenzo.mlguilds.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PlayerData {
    private UUID uuid;
    private String nickname;
    private int guildId;
    private boolean isInGuild;
    private boolean canCreateGuild;
    private boolean isCreatingGuild;
}

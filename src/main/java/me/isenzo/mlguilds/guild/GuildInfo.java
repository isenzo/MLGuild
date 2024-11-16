package me.isenzo.mlguilds.guild;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GuildInfo {
    private int id;
    private String name;
    private String tag;
    private String founderUUID;
    private int memberCount;
    private boolean isCreated;
}

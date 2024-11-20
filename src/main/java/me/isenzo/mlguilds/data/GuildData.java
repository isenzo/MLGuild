package me.isenzo.mlguilds.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GuildData {

    private Integer id;
    private String name;
    private String tag;
    private Integer founderId;
    private Integer memberCount;
    private Boolean isCreated;
}

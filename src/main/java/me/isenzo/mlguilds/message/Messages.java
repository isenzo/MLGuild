package me.isenzo.mlguilds.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.isenzo.mlguilds.Main;

import static me.isenzo.mlguilds.Main.getMessageManager;

@RequiredArgsConstructor
@Getter
public enum Messages {
    GUILD_CREATE_SUCCESS("guild_create_success"),
    GUILD_CREATE_FAIL("guild_create_fail"),
    ALREADY_IN_GUILD("already_in_guild"),
    NOT_IN_ANY_GUILD("not_in_any_guild"),
    PLAYER_IN_ALREADY_CREATED_GUILD("player_in_already_created_guild"),
    INVALID_TAG_FORMAT("invalid_tag_format"),
    INVALID_USAGE("invalid_usage"),
    NAME_IS_ALREADY_TAKEN("name_is_already_taken"),
    TAG_IS_ALREADY_TAKEN("tag_is_already_taken"),
    ONLY_PLAYER_CAN_RUN_THIS_COMMAND("only_player_can_run_this_command"),
    REQUIREMENTS_NOT_MET("requirements_not_met"),
    GUILD_CREATION_ERROR("guild_creation_error");

    public static Main plugin;
    private final String path;

    public static String format(Messages messageKey, Object... args) {
        MessageManager messageManager = getMessageManager();
        return messageManager.getMessage(messageKey, args);
    }
}

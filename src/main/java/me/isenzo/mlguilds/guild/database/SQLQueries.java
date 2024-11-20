package me.isenzo.mlguilds.guild.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SQLQueries {

    CREATE_PLAYERS_TABLE("CREATE TABLE IF NOT EXISTS players (" +
            "id SERIAL PRIMARY KEY," +
            "uuid VARCHAR(36) NOT NULL," +
            "nickname VARCHAR(32) NOT NULL," +
            "guild_id INT," +
            "is_player_in_guild BOOLEAN DEFAULT FALSE NOT NULL," +
            "can_create_guild BOOLEAN DEFAULT FALSE NOT NULL," +
            "is_creating_guild BOOLEAN DEFAULT FALSE NOT NULL);"),

    CREATE_GUILDS_TABLE("CREATE TABLE IF NOT EXISTS guilds (" +
            "id SERIAL PRIMARY KEY," +
            "name VARCHAR(12) NOT NULL," +
            "tag VARCHAR(4) NOT NULL," +
            "founder_id INT NOT NULL," +
            "member_count INT DEFAULT 0," +
            "is_created BOOLEAN DEFAULT FALSE NOT NULL," +
            "FOREIGN KEY (founder_id) REFERENCES players(id) ON DELETE SET NULL);"),

    CREATE_GUILD_REQUIRED_ITEMS_TABLE("CREATE TABLE IF NOT EXISTS guild_required_items (" +
            "player_uuid VARCHAR(36) NOT NULL," +
            "item VARCHAR(255) NOT NULL," +
            "amount INT NOT NULL DEFAULT 0," +
            "depositMoney INT NOT NULL DEFAULT 0," +
            "PRIMARY KEY (player_uuid, item));"),

    IS_PLAYER_IN_CREATED_GUILD("SELECT is_created FROM guilds g " +
            "INNER JOIN players p ON p.guild_id = g.id " +
            "WHERE p.uuid = ?;"),

    GET_GUILD_NAME_BY_PLAYER_UUID("SELECT g.name FROM guilds g " +
            "INNER JOIN players p ON p.guild_id = g.id " +
            "WHERE p.uuid = ?;"),

    GET_GUILD_TAG_BY_PLAYER_UUID("SELECT g.tag FROM guilds g " +
            "INNER JOIN players p ON p.guild_id = g.id " +
            "WHERE p.uuid = ?;"),

    IS_PLAYER_FOUNDER("SELECT g.founder_id FROM guilds g " +
            "INNER JOIN players p ON p.guild_id = g.id " +
            "WHERE p.uuid = ?;"),


    ADD_PLAYER("INSERT INTO players (uuid, nickname, can_create_guild, is_player_in_guild, guild_id) VALUES (?, ?, true, false, NULL)"),
    ADD_GUILD("INSERT INTO guilds (name, tag, founder_id, member_count) VALUES (?, ?, (SELECT id FROM players WHERE uuid = ?), 1)"),
    ADD_GUILD_IN_PROGRESS("INSERT INTO guilds (name, tag, founder_id, member_count, is_created) VALUES (?, ?, (SELECT id FROM players WHERE uuid = ?), ?, false);"),
    ADD_PLAYER_TO_GUILD("UPDATE players SET guild_id = ?, is_player_in_guild = true WHERE uuid = ?"),

    CHECK_PLAYER_EXISTS("SELECT COUNT(*) FROM players WHERE uuid = ?"),
    DOES_GUILD_EXIST("SELECT COUNT(*) FROM guilds WHERE name = ?"),
    CHECK_PLAYER_IN_GUILD("SELECT is_player_in_guild FROM players WHERE uuid = ?;"),
    IS_PLAYER_IN_CREATE_PROCESS("SELECT is_creating_guild FROM players WHERE uuid = ?;"),
    CAN_PLAYER_CREATE_GUILD("SELECT can_create_guild FROM players WHERE uuid = ?"),

    UPDATE_PLAYER_AFTER_GUILD_CREATION("UPDATE players SET can_create_guild = false, is_player_in_guild = true, guild_id = (SELECT id FROM guilds WHERE name = ?) WHERE uuid = ?"),
    UPDATE_PLAYER_IN_PROGRESS_STATUS("UPDATE players SET is_creating_guild = ? WHERE uuid = ?;"),
    UPDATE_GUILD_MEMBER_COUNT("UPDATE guilds SET member_count = member_count + ? WHERE name = ?"),
    UPDATE_PLAYER_GUILD_STATUS("UPDATE players SET is_player_in_guild = ? WHERE uuid = ?"),
    UPDATE_DEPOSITED_ITEMS("INSERT INTO guild_required_items (player_uuid, item, amount) VALUES (?, ?, ?) ON CONFLICT (player_uuid, item) DO UPDATE SET amount = guild_required_items.amount + EXCLUDED.amount"),
    UPDATE_DEPOSITED_MONEY("INSERT INTO guild_required_items (player_uuid, item, amount) VALUES (?, 'MONEY', ?) ON CONFLICT (player_uuid, item) DO UPDATE SET amount = guild_required_items.amount + EXCLUDED.amount"),
    UPDATE_PLAYER_CREATING_GUILD_STATUS("UPDATE players SET is_creating_guild = ? WHERE uuid = ?;"),

    GET_DEPOSITED_ITEMS("SELECT item, amount FROM guild_required_items WHERE player_uuid = ?"),
    GET_DEPOSITED_MONEY("SELECT amount FROM guild_required_items WHERE player_uuid = ? AND item = 'MONEY'"),
    GET_PLAYER_ID("SELECT id FROM players WHERE uuid = ?;"),
    GET_PLAYER_DATA("SELECT * FROM players WHERE uuid = ?;"),
    GET_GUILD_ID_BY_NAME("SELECT ID FROM guilds WHERE name = ?;"),
    GET_GUILD_INFO_BY_NAME("SELECT * FROM guilds WHERE name = ?;"),

    REMOVE_PLAYER_FROM_GUILD("UPDATE players SET guild_id = NULL, is_player_in_guild = false WHERE uuid = ?");

    private final String query;
}

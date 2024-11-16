package me.isenzo.mlguilds.command.validation;

import lombok.RequiredArgsConstructor;
import me.isenzo.mlguilds.Main;
import me.isenzo.mlguilds.guild.repository.GuildRepository;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor
public class PlayerCommandValidation {

    private final Main plugin;
    private final GuildRepository guildRepository;

    public String validateGuildCreation(Player player, String name, String tag) {
        if (isPlayerInGuild(player)) {
            return "Już należysz do gildii!";
        }
        if (!isTagValid(tag)) {
            return "Tag gildii jest niepoprawny. Powinien mieć 4 litery lub cyfry.";
        }
        if (!isNameValid(name)) {
            return "Nazwa gildii jest niepoprawna. Powinna mieć od 4 do 12 liter i cyfr.";
        }
        if (!isNameUnique(name)) {
            return "Nazwa gildii jest już zajęta.";
        }
        if (!isTagUnique(tag)) {
            return "Tag gildii jest już zajęty.";
        }

        return null;
    }

    private boolean isNameValid(String name) {
        return name.matches("[a-zA-Z0-9]{1,12}");
    }

    private boolean isTagValid(String tag) {
        return tag.matches("[a-zA-Z0-9]{4}");
    }

    private boolean isPlayerInGuild(Player player) {
        try (Connection conn = guildRepository.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM guilds WHERE founder_id = (SELECT id FROM players WHERE uuid = ?)")) {
            pstmt.setString(1, player.getUniqueId().toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    private boolean isNameUnique(String name) {
        try (Connection conn = guildRepository.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM guilds WHERE name = ?")) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                return !rs.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    private boolean isTagUnique(String tag) {
        try (Connection conn = guildRepository.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM guilds WHERE tag = ?")) {
            pstmt.setString(1, tag);
            try (ResultSet rs = pstmt.executeQuery()) {
                return !rs.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }
}

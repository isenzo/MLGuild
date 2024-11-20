package me.isenzo.mlguilds.gui;

import me.isenzo.mlguilds.Main;
import me.isenzo.mlguilds.file.ConfigManager;
import me.isenzo.mlguilds.guild.temporary_data.GuildCreationInfo;
import me.isenzo.mlguilds.utils.InputType;
import me.isenzo.mlguilds.utils.WaitingForInput;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.isenzo.mlguilds.Main.getGuildService;

public class GuildItemsGUI implements Listener {
    private final Main plugin;
    private final Inventory inv;
    private final ConfigManager configManager;
    private final Map<UUID, GuildCreationInfo> guildCreationInfoMap = new HashMap<>();

    public GuildItemsGUI(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.inv = Bukkit.createInventory(null, 9 * 3, ChatColor.DARK_GREEN + "Itemy na gildie");

        plugin.getLogger().info("GuildItemsGUI: plugin is " + plugin);
    }

    private void initializeItems(Player player) {
        String playerUUID = player.getUniqueId().toString();

        CompletableFuture<String> guildNameFuture = plugin.getGuildRepository().getGuildNameByPlayerUUID(playerUUID);
        CompletableFuture<String> guildTagFuture = plugin.getGuildRepository().getGuildTagByPlayerUUID(playerUUID);
        CompletableFuture<Map<String, Integer>> depositedItemsFuture = plugin.getGuildRepository().getDepositedItems(playerUUID);
        CompletableFuture<Double> depositedMoneyFuture = plugin.getGuildRepository().getDepositedMoney(playerUUID);

        CompletableFuture.allOf(guildNameFuture, guildTagFuture, depositedItemsFuture, depositedMoneyFuture).thenRun(() -> {
            String guildName = guildNameFuture.join();
            String guildTag = guildTagFuture.join();
            Map<String, Integer> depositedItemsData = depositedItemsFuture.join();
            double depositedMoney = depositedMoneyFuture.join();

            if (guildName == null || guildTag == null) {
                Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "Błąd: Nie znaleziono informacji o gildii. Spróbuj ponownie."));
                return;
            }

            Map<Material, Integer> requiredItems = configManager.getRequiredItemsForGuildCreation();

            // Dynamiczne tworzenie GUI z odpowiednim tytułem
            String guiTitle = ChatColor.DARK_GREEN + "Nazwa: " + guildName + " Tag: " + guildTag;
            Inventory inv = Bukkit.createInventory(null, 9 * 3, guiTitle);

            int slot = 0;
            for (Map.Entry<Material, Integer> entry : requiredItems.entrySet()) {
                Material material = entry.getKey();
                int requiredAmount = entry.getValue();
                int depositedAmount = depositedItemsData.getOrDefault(material.toString(), 0);

                String lore = String.format("§7Potrzebujesz: §c%d §7sztuk, §aWpłaciłeś: §c%d", requiredAmount, depositedAmount);
                addItem(inv, material, slot++, lore);
            }

            String moneyLore = String.format("Wpłać pieniądze - koszt: %.2f zł", (double) configManager.getGuildCreationCost());
            String depositedMoneyLore = String.format("§aWpłaciłeś: §c%.2f zł", depositedMoney);
            addItem(inv, Material.GOLD_INGOT, 8, moneyLore, depositedMoneyLore);

            if (hasRequiredItemsAndMoney(depositedItemsData, depositedMoney, player)) {
                addItem(inv, Material.GREEN_STAINED_GLASS, 22, "§a§lUTWÓRZ GILDIĘ", "Masz wszystko, co potrzebne do utworzenia gildii");
            } else {
                addItem(inv, Material.RED_STAINED_GLASS, 22, "§c§lNIE SPEŁNIASZ WYMAGAŃ", "Brakuje niektórych przedmiotów lub pieniędzy");
            }

            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(inv));
        });
    }

    public void setGuildCreationInfo(UUID playerUUID, String guildName, String guildTag) {
        guildCreationInfoMap.put(playerUUID, new GuildCreationInfo(playerUUID, guildName, guildTag));
    }

    public GuildCreationInfo getGuildCreationInfo(UUID playerUUID) {
        return guildCreationInfoMap.get(playerUUID);
    }

    private void addItem(Inventory inv, Material material, int slot, String lore) {
        addItem(inv, material, slot, lore, null);
    }

    private void addItem(Inventory inv, Material material, int slot, String lore, String additionalLore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GREEN + material.toString().toLowerCase());

        List<String> lores = new ArrayList<>();
        lores.add(ChatColor.GRAY + lore);
        if (additionalLore != null) {
            lores.add(ChatColor.GRAY + additionalLore);
        }
        meta.setLore(lores);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    public void openInventory(Player player) {
        initializeItems(player);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String inventoryTitle = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();
        String playerUUID = player.getUniqueId().toString();

        CompletableFuture<String> guildNameFuture = plugin.getGuildRepository().getGuildNameByPlayerUUID(playerUUID);
        CompletableFuture<String> guildTagFuture = plugin.getGuildRepository().getGuildTagByPlayerUUID(playerUUID);

        CompletableFuture.allOf(guildNameFuture, guildTagFuture).thenRun(() -> {
            String guildName = guildNameFuture.join();
            String guildTag = guildTagFuture.join();

            if (guildName == null || guildTag == null) {
                return;
            }

            String expectedTitle = ChatColor.DARK_GREEN + "Nazwa: " + guildName + " Tag: " + guildTag;

            if (!inventoryTitle.equals(expectedTitle)) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                event.setCancelled(true);

                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

                handleItemClick(player, clickedItem);
            });
        }).exceptionally(throwable -> {
            plugin.getLogger().severe("Błąd podczas uzyskiwania danych o gildii: " + throwable.getMessage());
            return null;
        });
    }


    private void handleItemClick(Player player, ItemStack clickedItem) {
        String playerUUID = player.getUniqueId().toString();

        CompletableFuture<String> guildNameFuture = plugin.getGuildRepository().getGuildNameByPlayerUUID(playerUUID);
        CompletableFuture<String> guildTagFuture = plugin.getGuildRepository().getGuildTagByPlayerUUID(playerUUID);
        CompletableFuture<Boolean> isFounderFuture = plugin.getGuildRepository().isFounder(playerUUID);
        CompletableFuture<Map<String, Integer>> depositedItemsDataFuture = plugin.getGuildRepository().getDepositedItems(playerUUID);
        CompletableFuture<Double> depositedMoneyFuture = plugin.getGuildRepository().getDepositedMoney(playerUUID);

        CompletableFuture.allOf(guildNameFuture, guildTagFuture, isFounderFuture, depositedItemsDataFuture, depositedMoneyFuture).thenRun(() -> {
            String guildName = guildNameFuture.join();
            String guildTag = guildTagFuture.join();
            boolean isFounder = isFounderFuture.join();
            Map<String, Integer> depositedItemsData = depositedItemsDataFuture.join();
            double depositedMoney = depositedMoneyFuture.join();

            if (guildName == null || guildTag == null) {
                Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "Błąd: Nie znaleziono informacji o gildii. Spróbuj ponownie."));
                return;
            }

            if (!isFounder) {
                Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "Tylko założyciel gildii może utworzyć gildię."));
                return;
            }

            Material clickedMaterial = clickedItem.getType();
            Map<Material, Integer> requiredItems = configManager.getRequiredItemsForGuildCreation();

            if (requiredItems.containsKey(clickedMaterial)) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Wpisz na czacie ilość " + clickedMaterial + ", którą chcesz wpłacić.");
                });
                WaitingForInput.add(player.getUniqueId(), clickedMaterial);
            } else if (clickedMaterial == Material.GOLD_INGOT) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Wpisz na czacie ilość pieniędzy, którą chcesz wpłacić.");
                });
                WaitingForInput.add(player.getUniqueId(), InputType.MONEY);
            } else if (clickedMaterial == Material.GREEN_STAINED_GLASS) {
                if (hasRequiredItemsAndMoney(depositedItemsData, depositedMoney, player)) {
                    withdrawRequiredItemsAndMoney(player, depositedItemsData, depositedMoney);

                    boolean success = getGuildService().createGuild(player, guildName, guildTag);
                    if (success) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.sendMessage(ChatColor.GREEN + "Gildia została utworzona pomyślnie!");
                            guildCreationInfoMap.remove(player.getUniqueId());
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f); // Dźwięk po sukcesie
                        });
                    }
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(ChatColor.RED + "Nie spełniasz wszystkich wymagań, aby utworzyć gildię!");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_FALL, 1.0f, 0.5f); // Dźwięk w przypadku niepowodzenia
                    });
                }
            }
        });
    }

    private boolean hasRequiredItemsAndMoney(Map<String, Integer> depositedItemsData, double depositedMoney, Player player) {
        Map<Material, Integer> requiredItems = configManager.getRequiredItemsForGuildCreation();

        for (Map.Entry<Material, Integer> entry : requiredItems.entrySet()) {
            Material material = entry.getKey();
            int requiredAmount = entry.getValue();
            int alreadyDeposited = depositedItemsData.getOrDefault(material.toString(), 0);
            int remainingToDeposit = requiredAmount - alreadyDeposited;

            if (remainingToDeposit > 0 && !player.getInventory().contains(material, remainingToDeposit)) {
                return false;
            }
        }

        return (configManager.getGuildCreationCost() - depositedMoney) <= plugin.getEconomyApi().getBalance(player);
    }

    private void withdrawRequiredItemsAndMoney(Player player, Map<String, Integer> depositedItemsData, double depositedMoney) {
        String playerUUID = player.getUniqueId().toString();
        Map<Material, Integer> requiredItems = configManager.getRequiredItemsForGuildCreation();

        for (Map.Entry<Material, Integer> entry : requiredItems.entrySet()) {
            Material material = entry.getKey();
            int requiredAmount = entry.getValue();
            int alreadyDeposited = depositedItemsData.getOrDefault(material.toString(), 0);
            int remainingToWithdraw = requiredAmount - alreadyDeposited;

            if (remainingToWithdraw > 0) {
                player.getInventory().removeItem(new ItemStack(material, remainingToWithdraw));
            }
        }

        double remainingMoneyToWithdraw = configManager.getGuildCreationCost() - depositedMoney;
        if (remainingMoneyToWithdraw > 0) {
            plugin.getEconomyApi().withdrawPlayer(player, remainingMoneyToWithdraw);
        }
    }


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (WaitingForInput.isWaitingForInput(playerUUID, InputType.MONEY)) {
            handleMoneyDeposit(event, player);
        } else if (WaitingForInput.isWaitingForInput(playerUUID, InputType.ITEM)) {
            handleItemDeposit(event, player, playerUUID);
        }
    }

    private void handleMoneyDeposit(AsyncPlayerChatEvent event, Player player) {
        event.setCancelled(true);
        double amountToDeposit;
        try {
            amountToDeposit = Double.parseDouble(event.getMessage());
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Wprowadzona wartość nie jest liczbą!");
            return;
        }
        EconomyResponse response = plugin.getEconomyApi().withdrawPlayer(player, amountToDeposit);
        if (response.transactionSuccess()) {
            plugin.getGuildRepository().updateDepositedMoney(player.getUniqueId().toString(), amountToDeposit);
            player.sendMessage(ChatColor.GREEN + "Pomyślnie wpłaciłeś " + amountToDeposit + " do funduszu gildii.");
        } else {
            player.sendMessage(ChatColor.RED + "Nie masz wystarczającej ilości środków na koncie!");
        }
        WaitingForInput.remove(player.getUniqueId(), InputType.MONEY);
    }

    private void handleItemDeposit(AsyncPlayerChatEvent event, Player player, UUID playerUUID) {
        event.setCancelled(true);
        int amount;
        try {
            amount = Integer.parseInt(event.getMessage());
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Wprowadzona wartość nie jest liczbą!");
            return;
        }

        Material materialToDeposit = WaitingForInput.getMaterialForPlayer(playerUUID); // Musisz zaimplementować tę metodę
        if (player.getInventory().contains(materialToDeposit, amount)) {
            plugin.getGuildRepository().updateDepositedItems(player.getUniqueId().toString(), materialToDeposit.toString(), amount);
            player.getInventory().removeItem(new ItemStack(materialToDeposit, amount));
            player.sendMessage(ChatColor.GREEN + "Pomyślnie wpłaciłeś " + amount + " sztuk " + materialToDeposit);
        } else {
            player.sendMessage(ChatColor.RED + "Nie posiadasz wystarczającej ilości " + materialToDeposit + "!");
        }
        WaitingForInput.remove(player.getUniqueId(), InputType.ITEM);
    }

}

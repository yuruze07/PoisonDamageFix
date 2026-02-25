package com.fawwa.PoisonDamageFix;

import org.bukkit.plugin.java.JavaPlugin;

public class PoisonDamageFix extends JavaPlugin {

  private static PoisonDamageFix instance;
  private PoisonListener poisonListener;

  @Override
  public void onEnable() {
    instance = this;

    // Register listener
    poisonListener = new PoisonListener();
    getServer().getPluginManager().registerEvents(poisonListener, this);

    // Save default config
    saveDefaultConfig();

    getLogger().info("╔════════════════════════════════════╗");
    getLogger().info(
      "║   PoisonDamageFix v" + getDescription().getVersion() + " Enabled!   ║"
    );
    getLogger().info("║   Mode: Damage Multiplier System   ║");
    getLogger().info("╚════════════════════════════════════╝");
  }

  @Override
  public void onDisable() {
    getLogger().info("PoisonDamageFix disabled!");
  }

  public static PoisonDamageFix getInstance() {
    return instance;
  }

  public PoisonListener getPoisonListener() {
    return poisonListener;
  }
}

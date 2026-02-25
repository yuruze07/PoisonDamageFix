package com.fawwa.PoisonDamageFix;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class PoisonListener implements Listener {

  // Map untuk menyimpan task damage per entity
  private final Map<UUID, BukkitTask> poisonTasks = new HashMap<>();

  // Konfigurasi
  private final int LEVEL2_INTERVAL = 12; // tick untuk level 2 ke atas
  private final double BASE_DAMAGE = 1.0; // damage level 1 (0.5 heart)

  @EventHandler
  public void onPotionEffect(EntityPotionEffectEvent event) {
    if (!(event.getEntity() instanceof LivingEntity)) return;

    LivingEntity entity = (LivingEntity) event.getEntity();

    // Cek apakah ada efek poison baru
    if (
      event.getNewEffect() != null &&
      event.getNewEffect().getType().equals(PotionEffectType.POISON)
    ) {
      handlePoisonApplication(entity, event.getNewEffect());
    }

    // Cek jika poison hilang
    if (
      event.getOldEffect() != null &&
      event.getOldEffect().getType().equals(PotionEffectType.POISON) &&
      (event.getNewEffect() == null ||
        !event.getNewEffect().getType().equals(PotionEffectType.POISON))
    ) {
      cancelPoisonTask(entity);
    }
  }

  @EventHandler
  public void onPotionSplash(PotionSplashEvent event) {
    // Handle splash potion
    for (LivingEntity entity : event.getAffectedEntities()) {
      for (PotionEffect effect : event.getPotion().getEffects()) {
        if (effect.getType().equals(PotionEffectType.POISON)) {
          handlePoisonApplication(entity, effect);
        }
      }
    }
  }

  @EventHandler
  public void onPlayerConsume(PlayerItemConsumeEvent event) {
    // Handle makanan/minuman beracun
    if (
      event.getItem().getType().toString().contains("POTION") ||
      event.getItem().getType().toString().contains("SOUP")
    ) {
      Player player = event.getPlayer();
      // Cek apakah player kena poison dari makanan
      if (player.hasPotionEffect(PotionEffectType.POISON)) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
          if (effect.getType().equals(PotionEffectType.POISON)) {
            handlePoisonApplication(player, effect);
            break;
          }
        }
      }
    }
  }

  private void handlePoisonApplication(
    LivingEntity entity,
    PotionEffect effect
  ) {
    int amplifier = effect.getAmplifier(); // 0 = level 1, 1 = level 2, 2 = level 3, dst

    // Cancel existing task
    cancelPoisonTask(entity);

    // Hanya handle level 3 ke atas (amplifier >= 2)
    if (amplifier >= 2) {
      startCustomPoisonTask(entity, effect);
    }
    // Level 1-2 biarkan default Minecraft
  }

  private void startCustomPoisonTask(
    LivingEntity entity,
    PotionEffect originalEffect
  ) {
    int amplifier = originalEffect.getAmplifier(); // 2 = level 3, 3 = level 4, dst
    int poisonLevel = amplifier + 1; // 3, 4, 5, ...
    int duration = originalEffect.getDuration();

    // Hitung damage multiplier berdasarkan level
    // Level 3: 1.5x (0.75 heart)
    // Level 4: 2.0x (1.0 heart)
    // Level 5: 2.5x (1.25 heart)
    // Level 6: 3.0x (1.5 heart)
    // dst: 1.0 + ((level-2) * 0.5)
    double damageMultiplier = 1.0 + ((poisonLevel - 2) * 0.5);

    // Damage per tick = BASE_DAMAGE (1.0) * multiplier
    double damagePerTick = BASE_DAMAGE * damageMultiplier; // 1.5 untuk level 3, 2.0 untuk level 4, dst

    BukkitTask task = new BukkitRunnable() {
      int ticksElapsed = 0;
      double totalDamageDealt = 0;

      @Override
      public void run() {
        // Cek apakah entity masih valid dan masih hidup
        if (!entity.isValid() || entity.isDead()) {
          cancelPoisonTask(entity);
          return;
        }

        // Cek apakah masih ada efek poison
        PotionEffect currentEffect = entity.getPotionEffect(
          PotionEffectType.POISON
        );
        if (currentEffect == null) {
          cancelPoisonTask(entity);
          return;
        }

        // Cek apakah entity masih bisa menerima damage (health > 0.5)
        if (entity.getHealth() <= 0.5) {
          return; // Tunggu sampai di-heal
        }

        // Berikan damage
        double damageToDeal = damagePerTick;

        // Pastikan tidak melebihi health - 0.5 (biar tidak mati)
        if (entity.getHealth() - damageToDeal < 0.5) {
          damageToDeal = entity.getHealth() - 0.5;
        }

        if (damageToDeal > 0) {
          entity.damage(damageToDeal);
          totalDamageDealt += damageToDeal;

          // Debug untuk testing
          // if (entity instanceof Player) {
          //     ((Player) entity).sendMessage("§cPoison lvl " + poisonLevel +
          //         ": -" + String.format("%.1f", damageToDeal/2) + "❤️ (" +
          //         String.format("%.1f", damagePerTick/2) + "x)");
          // }
        }

        ticksElapsed += LEVEL2_INTERVAL;

        // Stop jika durasi habis
        if (ticksElapsed >= duration) {
          cancelPoisonTask(entity);
        }
      }
    }
      .runTaskTimer(
        PoisonDamageFix.getInstance(),
        LEVEL2_INTERVAL,
        LEVEL2_INTERVAL
      );

    poisonTasks.put(entity.getUniqueId(), task);

    // Kasih tahu player kena poison berat
    if (entity instanceof Player && poisonLevel >= 3) {
      ((Player) entity).sendMessage(
        "§c⚠️ Kamu terkena poison level " +
          poisonLevel +
          "! Damage " +
          String.format("%.1f", damagePerTick / 2) +
          "❤️ setiap 0.6 detik"
      );
    }
  }

  private void cancelPoisonTask(LivingEntity entity) {
    BukkitTask task = poisonTasks.remove(entity.getUniqueId());
    if (task != null) {
      task.cancel();
    }
  }
}

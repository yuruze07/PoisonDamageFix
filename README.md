# PoisonDamageFix

A lightweight Paper plugin that fixes poison damage mechanics in Minecraft.

## The Problem
In vanilla Minecraft, poison levels 3 and above deal the same damage as poison level 2, despite having higher amplifier values. This makes higher-level poison potions effectively useless.

## The Solution
This plugin enhances poison damage for level 3 and above while keeping the original behavior for levels 1-2:

| Level | Interval | Damage (Hearts) |
|-------|----------|-------------------------|
| 1 | 25 ticks | 0.5❤️ |
| 2 | 12 ticks | 0.5❤️ |
| 3 | 12 ticks | 0.75❤️ |
| 4 | 12 ticks | 1.0❤️ |
| 5 | 12 ticks | 1.25❤️ |
| 6 | 12 ticks | 1.5❤️ |
| n | 12 ticks | 0.5 + ((n-2) × 0.25)❤️ |

## Features
- ✅ Preserves vanilla behavior for poison levels 1-2
- ✅ Progressive damage scaling for level 3+
- ✅ Uses Minecraft's proper DamageSource system
- ✅ Lightweight and efficient
- ✅ No configuration needed (works out-of-the-box)

## Requirements
- Paper 1.21.11 or higher
- Java 21

## Installation
1. Download the latest JAR from releases
2. Place it in your server's `plugins` folder
3. Restart your server

## How it Works
The plugin detects when a player receives a poison effect. For level 3 and above, it replaces the vanilla damage with a custom damage task that:
- Maintains the same interval as level 2 (12 ticks)
- Applies progressively higher damage based on poison level
- Uses the proper `DamageType.MAGIC` damage source

## Building from Source
```bash
mvn clean package
```

## Technical Details
- **API**: Paper 1.21.11
- **Damage Type**: `DamageType.MAGIC` with entity source
- **Interval**: 12 ticks (0.6 seconds) for level 3+
- **Damage Calculation**: `damage = 1.0 × (1.0 + ((level - 2) × 0.5))`

## License
MIT

---

*Makes high-level poison actually dangerous again!* ☠️

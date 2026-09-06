# ⚔️ HvsV [![CodeFactor](https://www.codefactor.io/repository/github/mitriyf/hvsv/badge)](https://www.codefactor.io/repository/github/mitriyf/hvsv)
## 🆚 Play as either the victim or the hunter. Someone must win.
This plugin adds a mini‑game in the form of a hunter and a victim. The victims must hide and, after a certain amount of time, take an axe and kill the hunter. The hunters must find all the victims and kill them.
- $ Versions 1.8.1-26+ are supported. It may be 1.7, but it has not been tested.
- $ Has been tested on versions: 1.8.8, 1.12.2, 1.16.5, 1.18.2, 1.21, 26+. The best performance was observed on these versions: 1.8.8, 1.12.2, and 1.16.5.
- $ Some plugin updates on SpigotMC.ru may be delayed.
- $ There may be a future FAQ about the plugin here. JParkour was created using an older version of this plugin.
- $ Attention! An additional plugin is required for the plugin to work. You can read the requirements below.
- $ The plugin requires the server to be shut down properly, as if the server is killed (Killed java) during a player's game, it may have an abnormal number of hearts.
## ⚖️ Play as the victim
Hide, wait for the hatchet to take revenge on the hunter.

<img width="1920" height="1009" alt="2026-09-06_14 36 37" src="https://github.com/user-attachments/assets/d37951ca-1e51-410c-89bf-64eb5ba282ea" />
<img width="1920" height="1009" alt="2026-09-06_14 37 41" src="https://github.com/user-attachments/assets/182e6ed1-e0ad-4077-9f6a-e6d65716de3b" />
<img width="1920" height="1009" alt="2026-09-06_14 39 24" src="https://github.com/user-attachments/assets/adf841f7-2cc9-4366-bcc3-bf763af96af2" />
<img width="640" height="266" alt="2026-09-0614-51-39-ezgif com-video-to-gif-converter" src="https://github.com/user-attachments/assets/35e0a878-0720-448f-9f1b-325da8f75478" />

## 🪓 Play as a hunter
Find and kill all the victims before they do.

<img width="1920" height="1009" alt="2026-09-06_15 01 32" src="https://github.com/user-attachments/assets/22c61ded-6469-4665-9375-96eae8ff086e" />
<img width="1920" height="1009" alt="2026-09-06_15 02 50" src="https://github.com/user-attachments/assets/dd623264-5df6-459a-8d86-51f902ccf7d1" />
<img width="1920" height="1009" alt="2026-09-06_15 04 14" src="https://github.com/user-attachments/assets/a88638c9-8d74-433c-aba8-a19af8c84603" />

## 🥇 Win or accept defeat!
Find out the winner and go home!

<img width="1920" height="1009" alt="2026-09-06_15 27 34" src="https://github.com/user-attachments/assets/57d14113-65b3-4923-b343-cd18294c9005" />
<img width="1920" height="1009" alt="2026-09-06_15 27 54" src="https://github.com/user-attachments/assets/30d007fd-7399-4380-acc6-c91289627794" />
<img width="1920" height="1009" alt="2026-09-06_15 30 07" src="https://github.com/user-attachments/assets/c96552a9-d3f0-4cc1-baac-573462f39827" />
<img width="640" height="266" alt="2026-09-0615-30-09-ezgif com-video-to-gif-converter" src="https://github.com/user-attachments/assets/c4337b82-33e9-4bc6-9fd5-4eb55536dee3" />

## 🚀 Requirements:
- FastAsyncWorldEdit (FAWE).
  - You can download it here: https://intellectualsites.github.io/download/fawe.html
  - GitHub: https://github.com/IntellectualSites/FastAsyncWorldEdit
## 🛠️ Supported:
### 🔮 Support HEX (1.16+, it will also work on lower versions, but without displaying the correct colors), MiniMessage (1.18+)
### 🌍 Languages:
- en_US (English (US))
- ru_RU (Russian)
- de_DE (German)
- Others (Don't forget to enable locales in the plugin configuration for language support)
  - $ You can find out the language code by clicking on the link below and looking at the In-Game section. Or you can find out your language code by running the command (don't forget to change the language on the client): **/hvsv admin locale**
  - $ https://minecraft.fandom.com/wiki/Language#Languages
  - $ You can configure locales in the plugin folder where the locales. The folder will be created when you enable locales in the plugin configuration. You can create your own files.
### 🌐 Plugins: 
- PlaceholderAPI (The tops and scoreboard system).
  - You can download it here: https://github.com/PlaceholderAPI/PlaceholderAPI/releases
  - Don't forget to enable it in the plugin configuration.
  - Placeholders:
    - %hvsv_role% - Displays the player's role.
    - %hvsv_map% - Displays the name of the map.
    - %hvsv_id% - Displays the ID of the map.
    - %hvsv_status% - Displays the status of the map.
    - %hvsv_online% - Displays online this game.
    - %hvsv_maxonline% - Displays max online this game.
  - $ Ready-made configuration of scoreboards: https://github.com/mitriyf/HvsV/blob/main/downloads/tab/config.yml
<img width="789" height="213" alt="image" src="https://github.com/user-attachments/assets/d35e8cb1-256d-4f69-8b7a-77a87534f464" />

### 🔎 Checks:
- Automatic check for new versions that contain important updates. However, if it is a release without important fixes, there will be no alert unless it is enabled in the plugin configuration.
- Checking if there are any ready-made schematics. Automatic Downloading from GitHub if there are none: https://github.com/mitriyf/HvsV/tree/main/downloads
- The plugin will automatically detect your server version so that it starts working correctly with your project.
- Replacement of some parts of the configuration in case of their absence.
- Checking for an old configuration version and updating to a new one.
- Checking if a game with the same world name already exists.
- Checking whether the player is in the game.
- Checking if there is such an ID Map.
- Permissions verification.
- Fixing some user errors.
- And much more...

## ♾️ Functions:
### ⌨️ Command (/hvsv):
- /hvsv status - Check the status of the plugin.
- /hvsv join - Create or join any available room.
- /hvsv join roomId - Attempt to connect to the room (if it is free).
- /hvsv exit - Exit the game/queue.
- /hvsv admin - Get a Admin Help.
  - /hvsv admin add playerName - Add a player to a random game.
  - /hvsv admin add playerName Map - Add a player to a specific game.
  - /hvsv admin item - Get a Item Help.
    - /hvsv admin item add default/schematicName player/victim/hunter slot itemName - Add the item in your hand to the selected schematic.
    - /hvsv admin item list - Get a list of schematics.
    - /hvsv admin item list default/schematicName player/victim/hunter - Get a list of items in the selected schematic.
    - /hvsv admin item info default/schematicName player/victim/hunter itemName - Get the item from the selected schematic.
    - /hvsv admin item remove default/schematicName player/victim/hunter itemName - Remove an item from the selected schematic.
  - /hvsv admin kick playerName - Kick the player out of the game.
  - /hvsv admin locale - Get the client's language code.
- /hvsv reload - Reload the plugin configuration.

### 📖 Permissions:
- **hvsv.help** - Can a player get help with subcommands?
- **hvsv.join** - Can a player join/exit games?
- **hvsv.status** - Can the player find out the status of the games?
- **hvsv.reload** - Can the player reload the plugin configuration?
- **hvsv.admin** - Can the player access item, and other commands? + Removing restrictions on commands and walking through worlds.
- **hvsv.item** - Can the player access item settings?

### 🏃 Actions:
  - [actionbar] message - Send the actionbar with your message. For 1.11+
  - [connect] server - Send a player to a specific BungeeCord server.
    - WARNING: Requires BungeeMessaging. This is present on BungeeCord and WaterFall.
    - On Velocity it might be disabled by default. Check your proxy config.
  - [message] message - Send a message to the player.
  - [room] message - Send a message to the room's players.
  - [broadcast] message - Send a message to all players.
  - [log] message - Send a message to the console.
  - [delay] ticks - Make a delay between actions. In ticks (20 ticks = 1 second).
  - [player] command - Run the command on behalf of the player.
  - [teleport] world;x;y;z;yaw;pitch;delay - Teleport the player to the specified coordinates. The delay is measured in ticks.
  - [console] command - Run the command on behalf of the console.
  - [title] title;subtitle;fadeIn;stay;fadeOut - Send the title to the player. For 1.8+
  - [sound] sound;volume;pitch;delay - Perform a sound for the player. The delay is measured in ticks.
    - Search for sounds here: https://helpch.at/docs/$version$/org/bukkit/Sound.html
    - // Replace $version$ with the version of your server, for example: https://helpch.at/docs/1.8.8/org/bukkit/Sound.html
  - [effect] type;duration;amplifier;delay - Give the effect to the player. The delay and duration is measured in ticks.
    - Find the types of effects here: https://helpch.at/docs/$version$/org/bukkit/potion/PotionEffectType.html
  - [explosion] power;setFire;breakBlocks;delay;addX;addY;addZ - Create an explosion. The delay is measured in ticks.
    - setFire, breakBlocks - set to false or true. addX, addY, addZ - double values that are added to the player's explosion location.
  - [bossbar] message;color;type;time;style;flag - Send a bossbar to a player with a message for a specific time. For 1.9+
    - Types:
      - stop - The bossbar will disappear after the time you specified in time (seconds)
      - time - Bossbar will animate the time that is running out.
    - Functions:
      - %time% - Seconds left.
    - You can find all the functions like color, style, and flag here: https://helpch.at/docs/$version$/org/bukkit/boss/BossBar.html
  - Info:
    - Messages sent to the console may not replace placeholders or perform certain actions above.
  - Built-in functions:
    - %player% - Get the player name.
    - %world% - Get the player world.
### ⚙️ Config:
- Send actions to players using messages. (HEX support from 1.16+, MiniMessage support form 1.18+)
- Settings for schematics, default maps, and more.

### 🔐Storage:
- Backup of updated configurations after updating to a new plugin version.
- Automatic loading of schematics if they are missing.
- Automatic correction of broken configurations.
- Automatic folder creation.

### 🔄ConfigUpdater:
- The plugin will check all configuration conditions and update them as much as possible, and it will also create backups of previous ones.
- There are some moments where it can work when you are working with the editor.

## 📝 Configurations:
You can view the configurations by going to src/main/resources/:
- locales/ru_RU.yml
- locales/en_US.yml
- locales/de_DE.yml
- config.yml
- slots.yml
- slots13.yml
- schematics/default.yml

or by following the link:
- https://github.com/mitriyf/HvsV/blob/main/src/main/resources/locales/ru_RU.yml
- https://github.com/mitriyf/HvsV/blob/main/src/main/resources/locales/en_US.yml
- https://github.com/mitriyf/HvsV/blob/main/src/main/resources/locales/de_DE.yml
- https://github.com/mitriyf/HvsV/blob/main/src/main/resources/config.yml
- https://github.com/mitriyf/HvsV/blob/main/src/main/resources/slots.yml
- https://github.com/mitriyf/HvsV/blob/main/src/main/resources/slots13.yml
- https://github.com/mitriyf/HvsV/blob/main/src/main/resources/schematics/default.yml

# 🤗 You can consider the rest of the possibilities when using the plugin.

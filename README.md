Thirst System adds a survival thirst mechanic to your server, giving players a second need to manage alongside hunger. Staying hydrated becomes part of everyday survival, and neglecting it has real consequences, so players have to plan around finding and carrying water the same way they plan around food

Every player has a thirst level that ranges from 0 to 20. Thirst slowly drops as the player's hunger drains, so the more active a player is, the faster they get thirsty. As thirst falls, the player starts to suffer for it: at moderate thirst they get slowness, even lower thirst adds nausea, and very low thirst piles on mining fatigue as well. If thirst ever hits zero, the player takes damage over time and will eventually die of thirst if they don't drink, complete with a custom death message announcing it

To stay hydrated, players can drink several things. Milk, honey bottles, and ordinary water bottles all restore some thirst, but the centrepiece is the custom water flask. The water flask is a craftable, refillable container that holds up to 100 units of water. Players fill it by right clicking a water source with it in hand, and every time they drink from it the water level ticks down by one and its display updates to show how much is left. When the flask runs dry it turns into a reusable empty water flask, which can simply be refilled again, so a single flask lasts indefinitely as long as the player keeps topping it up. Both flasks are single-item stacks with a subtle enchanted glint so they stand out in the inventory

Under the hood, each player's thirst value is stored directly on the player using persistent data, so it survives logouts and server restarts without needing a separate database. The negative effects are applied as infinite-duration potion effects and are carefully managed so that drinking milk, which normally clears all effects, won't remove the thirst penalties while still letting it remove genuine effects the player picked up elsewhere. The damage at zero thirst is handled by a self-cancelling task that also accounts for absorption hearts and bypasses armor so it can't be blocked, and it flags the player if the thirst damage is what killed them so the correct death message can be shown. The water flask stores its remaining water amount in its own display text and is identified by a hidden data tag, which is how the plugin tells a real flask apart from an ordinary vanilla water bottle. Both the water flask and the empty water flask have a custom model data string assigned to them (water_flask and empty_water_flask respectively), so server owners can apply custom textures to them through a resource pack if they wish. The player's current thirst is exposed through PlaceholderAPI, so it can be displayed however you like

This plugin has no config file. Any custom behaviour requires modifying the source code directly and rebuilding the jar from scratch

This plugin was built for Minecraft 26.2 running on PaperMC. It may or may not work on older or newer versions of Minecraft or other server software

This plugin depends on PlaceholderAPI and AuraSkills

- AuraSkills: https://modrinth.com/plugin/auraskills
- PlaceholderAPI: https://modrinth.com/plugin/placeholderapi

- This plugin is intended to work alongside AuraSkills to display the thirst in AuraSkills' actionbar through the use of PlaceholderAPI however it can work without AuraSkills as well. AuraSkills' actionbar is just a way of displaying the thirst to the player. If you want to display the thirst through some other way such as a scoreboard that supports PlaceholderAPI, use %thirst_% for the placeholder, however you would need to remove AuraSkills from the dependency list in the plugin.yml file then rebuild the plugin
- If you intend to use AuraSkills alongside this plugin then make sure to configure AuraSkills properly by going to plugins/AuraSkills/messages/global.yml and placing the said placeholder wherever you deem appropriate

For support or questions, join my discord server: https://discord.gg/8yrbGUCFFD

Licensed under the Apache License 2.0

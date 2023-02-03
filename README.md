# StreamChat mod by mini_bomba

A simple chat client for Twitch put into Minecraft 1.8.9's chat box!

## Installation

The mod can be downloaded from the [release list](https://github.com/mini-bomba/StreamChatMod/releases).
You can ~~either~~ download ~~a stable full release, or~~ the "Latest Commit (that compiles)" prerelease which is automatically compiled on new commit.
After downloading, this mod can be installed simply by putting the .jar file in the mods folder of your minecraft installation.
It is located in `%APPDATA%\.minecraft` on Windows and `~/.minecraft` on Linux.
If you use [Prism Launcher](https://prismlauncher.org/) or [MultiMC](https://multimc.org), you can just drag & drop the file into the mods window

**NOTE: The last "stable" release was 2 years ago and probably doesn't work by now. Please use the [rolling "latest" pre-release](https://github.com/mini-bomba/StreamChatMod/releases/tag/latest) instead.**

## Configuration

**NOTE: All commands listed are NOT Twitch chat commands, and should be run in your Minecraft client, not the Twitch chat window.**

First, generate & set the Twitch token. To do that, simply run `/twitch token`.
A prompt for granting permission to your Twitch account will appear in your browser.
After granting permission, you will be redirected to a localhost page. If permission was granted
within around 120 seconds, the token will automatically be saved (as indicated by the ✅ emoji in your browser and a confirmation message in your minecraft client).
If you got a connection refused page instead, you can still manually set the token by copying it from the url (the `access_token` query param)
and use the command `/twitch settoken <token>`.
If the prompt window did not appear, you can use [this link](https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=q7s0qfrigoczrj1a1cltcebjx95q8g&redirect_uri=http://localhost:39571&scope=chat:read+chat:edit).
If you leak your token, you can quickly invalidate it by running `/twitch revoketoken`

After configuring the token, use the command `/twitch enable` to enable the Twitch chat and run `/twitch chat join <channel name>` for every channel's chat you want to see in-game.

If you want to send messages from the in-game chat to your stream chat, run `/twitch chat select <channel name>` to select the channel to send messages to.
Then you can use `/twitchchat <your message>` or `/tc <your message>` to send a message.

## Usage

After loading a world (either single- or multiplayer), any messages sent in any of the configured stream chats will be relayed to 
your player's local chat.

If you selected a channel in the configuration section, you can use `/tc <message>` to send any message from in-game to your selected stream chat.

You can delete a message (if you have permissions to do so) by clicking on it in-game and sending the suggested command. Since commit 0e6601e3 deleted messages are also deleted from minecraft's chat.

All Twitch related configuration commands can be viewed by running `/twitch help`.

## Moderation from Minecraft

Some moderation commands are available from in-game. They are listed below:
* `/twitch delete <channel> <message id>`: Delete the specified message. It is supposed to be entered by clicking on a
  message in-game, since there is no easy way to get the ID of a message.
* `/twitch clearchat`: Clears the currently selected Twitch Chat. Does not clear the in-game chat; use F3+D to do that.
* `/twitch timeout <user> <duration> [reason]`: Timeouts the given user in the currently selected Twitch Chat. There is
  no un-timeout command, time the user out for 1 second to do that.
* `/twitch ban <user> [reason]`: Bans the given user in the currently selected Twitch Chat.
* `/twitch unban <user>`: Unbans the given user in the currently selected Twitch Chat.
* Tip 19: *Ban an annoying humanoid from your stream with `/twitch ban <user> [reason]` (
  ex: `/twitch ban Rajdo Being an annoying humanoid.`)*

**Confirmation messages for moderation commands have been recently implemented in commit `45b4be14`**

## Other features

### Emotes

StreamChatMod allows Minecraft to render Twitch, BetterTTV and FrankerFaceZ emotes in your chat, including animated
ones!

Emotes are downloaded & cached mostly during game startup, but will also be updated when restarting the Twitch client or
joining new chats.<br>
Note that loading emotes while playing will cause the game to freeze for a few seconds. (like when reloading a texture
pack)

You can also prevent each type of emotes from being rendered in an image form, using the `/twitch emote` command.

**NOTE: If you are using the [Patcher](https://github.com/Sk1erLLC/Patcher) mod, you need to turn off the `Optimized Font Renderer` feature for emotes to work.**

![Emote feature showcase](https://cdn.upload.systems/uploads/0DfjY2HF.png)

### Badges

Since [8fea1932a](https://github.com/mini-bomba/StreamChatMod/commit/8fea1932a4abaf1203a8ce29c6a7c6e2f76f004f),
StreamChatMod also allows you to display your chatter's badges, as icons (same as the emotes).

This can be disabled using `/twitch emote`, which reverts to text-based display for the 5 main badges (streamer, twitch
staff, channel mod, VIP, subscriber)

![Badge icon showcase](https://cdn.upload.systems/uploads/6XmL8IYp.png)

### Showing Twitch emotes in all messages

Since [80489715](https://github.com/mini-bomba/StreamChatMod/commit/80489715adfb76f17ec8cf17d38154b271cdd21c),
StreamChatMod allows you to render Twitch emotes in all Minecraft messages!<br>
This is disabled by default and can be enabled in `/twitch emotes`.<br>
The channel emotes rendered are pulled from your currently selected channel (or your channel, if no channel is selected)

![Using a BTTV emote in guild chat](https://cdn.upload.systems/uploads/mrjQrWSF.png)

### Automatic update checker

The mod automatically checks for updates on startup.

You can also check for updates every 15 minutes, by running `/twitch updatechecker enable` (
or `/twitch updatechecker disable` to disable). This is automatically enabled on prerelease builds.

### Chat formatting

By default, any formatting codes are "neutralized" (the `§` character is replaced with `&`). However, this can be
changed: you may either allow everyone to use formatting codes, or only subscribers, VIPs and moderators.

When enabled, the inverse of the "neutralization" happens: the `&` is replaced with `§`, allowing viewers to use Essentials(X)-like color codes.

### Clip embedding
Whenever someone sends a link to a Twitch clip, that link is automatically looked up,
important data about it extracted and presented to you in your Minecraft chat.<br>
The link will be replaced with the clip name and you'll be able to hover over it to
view extra details about the clip or click it to view the clip in your browser.

Example:
![Clip Embedding demonstration](https://cdn.upload.systems/uploads/ZUz1hj1R.png)

### New follow events
When a new user follows, you will get a message about it & a sound will play. This may take longer than your event overlay in OBS, though.
This can also be completely disabled if you get a lot of followers.

### Send chat messages to the Twitch chat by default
You can use `/twitch chatmode` to make any non-command messages be automatically sent to the selected Twitch channel.

You may also set a "Minecraft chat prefix" using `/twitch mcchatprefix`.
Prepending this prefix to the message while the redirect mode is enabled will result in the message being sent to the Minecraft server instead (disabling redirection for that message only).
The default prefix is `!!` (since `!` is a commonly used prefix for chatbots).
The prefix is removed before the message is forwarded to the server.

Note: This may not work nicely with all mods and some mods may make this feature not work. If you find a well-known mod, which does not work with this, please submit a bug report.

ProTip: a purple outline will appear if you are in Twitch chat mode, so you always know where your messages are going.<br>
ProProTip: the outline will change to green when you enter the prefix while in Twitch chat mode to notify you that the message will be sent to the Minecraft server.

### Chat outline for selected SCM commands
The outline visible with Twitch chat mode enabled will also appear when typing some StreamChatMod commands. This
currently mostly applies to moderation commands & the `/twitchchat` command.

The outline will be either purple or red, indicating whether you've made an obvious mistake or not. The text above the
chat input bar will tell you what the command will do and what obvious mistake you've made.

Examples:
![Valid Ban Command](https://cdn.upload.systems/uploads/SkKnCNQl.png)
![Valid Timeout Command](https://cdn.upload.systems/uploads/oj0qxwGu.png)
![Invalid Timeout Duration](https://cdn.upload.systems/uploads/BIXbpaLC.png)
![Missing Parameters](https://cdn.upload.systems/uploads/cSGZ0Ghp.png)

### Clip & Marker commands & shortcuts

StreamChatMod allows you to quickly & easily create clips or markers on your streams with configurable keyboard
shortcuts!

Simply find the StreamChatMod category in Controls and assign keys to the shortcuts you need. All shortcuts are
currently unassigned by default.

You may also use the `/twitch clip` & `/twitch marker [optional description]` to create a clip or a marker.

![Shortcuts](https://cdn.upload.systems/uploads/z39N17dk.png)

**Notes:**

* Clips & markers are created on the currently selected channel. Change it using `/twitch channel select`
* Clip creation has a cooldown of 2 minutes, due to Twitch's API having a global limit on clip creation per app.
* Clips cannot be named via command. This is a Twitch API limitation.
* Markers cannot be named when using the `Create new marker` shortcut. A `Create new marker with description` shortcut
  is planned.
* You must either be the broadcaster or have editor permissions on the channel you want to create markers on.

### Customizable prefix for Twitch messages (thanks to NopoTheGamer)

Since [d3a56390ff](https://github.com/mini-bomba/StreamChatMod/commit/d3a56390ff444b28a24bd34f1ab91fb882250d6c), you can
change the text shown before each Twitch message in the chat!

Currently, this can only be done by editing the config file (config/streamchatmod.cfg), command will be added... someday

![Default prefix](https://cdn.upload.systems/uploads/6XmL8IYp.png)
![Customized prefix](https://cdn.upload.systems/uploads/lXCJNNg9.png)

### Quick revoking of your token

Accidentally showed the configuration file or authorization window on stream?

Use `/twitch revoketoken` to quickly invalidate that token to minimize damage.

## Note to self:

Run `setupDecompWorkspace` Gradle task before configuring Gradle project

[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/works-on-my-machine.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/powered-by-black-magic.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/uses-badges.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/uses-git.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/for-you.svg)](https://forthebadge.com)

[![discordapp](https://cdn.discordapp.com/attachments/585500299234639872/792049752563777536/ryszard-pizza-rolls2.png)](https://endermanolandia.xyz)

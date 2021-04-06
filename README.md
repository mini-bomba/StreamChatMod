# StreamChat mod by mini_bomba

A simple chat client for Twitch put into Minecraft 1.8.9's chat box!

## Installation

The mod can be downloaded from a release or the artifacts of a Github Actions job.
After downloading, this mod can be installed simply by putting the .jar file in the mods folder of your minecraft installation.
It is located in `%APPDATA%\.minecraft` on Windows and `~/.minecraft` on Linux.

## Configuration

**NOTE: All commands listed are NOT Twitch chat commands, and should be run in your Minecraft client, not the Twitch chat window.**

First, generate & set the Twitch token. To do that, simply run `/twitch token`.
A prompt for granting permission to your Twitch account will appear in your browser.
After granting permission, you will be redirected to a locahost page. If permission was granted
within around 120 seconds, the token will automatically be saved (as indicated by the ✅ emoji in your browser and a confirmation message in your minecraft client).
If you got a connection refused page instead, you can still manually set the token by copying it from the url (the `access_token` query param)
and use the command `/twitch settoken <token>`.
If the prompt window did not appear, you can use [this link](https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=q7s0qfrigoczrj1a1cltcebjx95q8g&redirect_uri=http://localhost:39571&scope=chat:read+chat:edit).

After configuring the token, use the command `/twitch enable` to enable the Twitch chat and run `/twitch chat join <channel name>` for every channe chat you want to see in-game.

If you want to send messages from the in-game chat to your stream chat, run `/twitch chat select <channel name>` to select the channel to send messages to.
Then you can use `/twitchchat <your message>` or `/tc <your message>` to send a message.

## Usage

After loading a world (either single- or multiplayer), any messages sent in any of the configured stream chats will be relayed to 
your player's local chat.

If you selected a channel in the configuration section, you can use `/tc <message>` to send any message from in-game to your selected stream chat.

All Twitch related configuration commands can be viewed by running `/twitch help`.

[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/works-on-my-machine.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/powered-by-black-magic.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/uses-badges.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/uses-git.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/for-you.svg)](https://forthebadge.com)

[![discordapp](https://cdn.discordapp.com/attachments/585500299234639872/792049752563777536/ryszard-pizza-rolls2.png)](https://endermanolandia.xyz)

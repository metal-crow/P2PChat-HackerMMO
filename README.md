A game of subterfuge, stealth, backstabbing, and l337 hacking
Multiplayer game where the goal is to try to stay in the chat and online as long as possible
Players compete to try to use various attacks to hack other players and kick them off the network

Based off my p2p chat engine, see that repo for unedited original chat program.
This code is probably hideous, a) the engine is 3 years old, written when i was in high school, b) this is done over a 24 hour deadline


/kick [player] kicks that player, unless that player blocked the player who kicked them. 
/disable [player] disables a random ability of the player
/scramble [player] makes all text that player views very difficult to read, or if they are already scrambled, unscrambles them.
/forceblock [player] [player2] forces player to block player2
/viewall view all the user ability commands usually not shown
/mimic [name] [message] allows user to mimic another user, even though they cannot claim their name

high score system
When a user disconnects, check their uptime, and broadcast it if it > another user's broadcasted high score
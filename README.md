# Bento Cash
Bento cash is a cross server crypto currency for Minecraft servers. It uses the BentoBox API.

## Features
* Cross-server universal crypto currency for players and admins
* Fast payment approvals - usually instant
* Easy and secure - players scan in-game QR codes with their phones to approve purchases

## How do players pay for things?
To buy things on a server, players scan an in-game QR code (shown on a map or in an item frame) with their mobile device. The QR code takes them to a web page where they confirm the purchase by entering their username and password (not their Minecraft password!). Therefore, the server owner never knows the player's Bento Cash password. Successful payments are reported to the server in a secure manner.

## How to create a QR code
You create a payment QR code map by running the admin `map` command with the following syntax:

`/bsb abc map <amount> <command>`

The placeholder @p denotes the player looking at the map.

e.g.:

`/bsb abc map 10 give @p diamond 1` will give the player one diamond if they pay 10.

Commands can be any command that can be run in the console. Be sure to test your commands before making them live.

Once you have a map, you can place it in item frames like any map.

## How to pay your players
Admins can pay players using the admin pay command. This can be used to reward players, e.g. for completing challenges, or to pay players ad hoc. The command requires a password.

## Creating an account
To create a Bento Cash account, or reset a password, players visit **bento.cash:12345** using their Minecraft client. Their client will immediately disconnect and shown a temporary password and a report of their balance. They should then go to https://bento.cash and reset their password to something only they know.

## How can I get Bento Cash?
When you first make an account by going to bento.cash:12345 you will receive some cash. Sponsors will receive extra Bento Cash allocations. 

## Is this a real crypto currency like BitCoin or Etherium?
No. This is just for fun right now. 

## Is it decentralized or centralized?
For now it is centralized.


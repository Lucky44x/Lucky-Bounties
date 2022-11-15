# Lucky-Bounties
Luckybounties is lightweight bounty plugin which let's your players set bounties on each other.
My main goal with this plugin was to make it as user friendly as possible, so the entire bounty system is based on chest GUIs.
To set a bounty you just have to type `/bounties` which will open the GUI:
![Main GUI](https://user-images.githubusercontent.com/47057120/125192652-4307b280-e249-11eb-817d-c48431a08721.png)

After you clicked on a players head, this will open the players bounty menu:
![Players bounty GUI](https://user-images.githubusercontent.com/47057120/125192763-bf01fa80-e249-11eb-95a6-a02c0e09243e.png)

Notice 
1: The clear button. This feather button will only appear for players with op and will completely clear a players bounties.
2: The set menu. When clicking this amethyst shard, the set bounty menu will open.

After clicking the set menu, the bounty set menu should appear:
![bounty set menu](https://user-images.githubusercontent.com/47057120/125192894-3afc4280-e24a-11eb-994b-566bd9be678e.png)

To set a bounty you first place the item/s you want to set in the middle field and then press confirm:
![set bounty](https://user-images.githubusercontent.com/47057120/125192928-5ebf8880-e24a-11eb-95c1-15bc9b6e6907.png)

After the bounty is set, it will appear in the players bounty menu, visible for every other player:
![set bounty 2](https://user-images.githubusercontent.com/47057120/125192954-7dbe1a80-e24a-11eb-8413-2f0ac8069410.png)

When the player now gets killed, he drops the bounty and a message appears in chat:
![bounty taken](https://user-images.githubusercontent.com/47057120/125193023-b78f2100-e24a-11eb-9b2f-2d53c5c4b0c4.png)

**IMPORTANT**
This plugin was developed as a private plugin, so it hasn't been optimised. Because of this it uses a very inefficient json save system and stores all data in the ram at runtime. If you plan on using it on a big server, I would recommend taking the source code and giving it to a experienced developer who can make this more efficient.

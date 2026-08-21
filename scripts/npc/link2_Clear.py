# Link 2 Control Device | Evolution System link 2
from net.swordie.ms.client.character.skills.temp import CharacterTemporaryStat

# The Link 2 course requires the player to be transformed into the Link 2
# monster. Item 2210091 applies Morph option 139 in the v232.2 WZ data.
if sm.getnOptionByCTS(CharacterTemporaryStat.Morph) == 139:
    sm.sendSayOkay("Access to the control device completed.")
    sm.createQuestWithQRValue(1832, "Access")
else:
    sm.sendSayOkay("You must access the control device while disguised as a Link 2 monster.")

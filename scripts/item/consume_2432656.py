# Mushroom Castle camera. Grant the three required photos without opening
# the unsupported client photo sequence.
from net.swordie.ms.enums import InvType

PHOTOS = (4034107, 4034108, 4034109)
missing_photos = [item_id for item_id in PHOTOS if not sm.hasItem(item_id)]

if not chr.hasQuestInProgress(30055):
    sm.chat("There is nothing to photograph right now.")
elif sm.getEmptyInventorySlots(InvType.ETC) >= len(missing_photos):
    for item_id in missing_photos:
        sm.giveItem(item_id)
    sm.consumeItem(parentID)
else:
    sm.chat("Please make room in your Etc inventory.")

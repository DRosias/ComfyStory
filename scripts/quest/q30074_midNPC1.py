POTION = 4034113

if sm.hasItem(POTION) or sm.canHold(POTION):
    if not sm.hasItem(POTION):
        sm.giveItem(POTION)
    sm.setQRValue(parentID, "NpcSpeech=13020211")
else:
    sm.chat("Please make room in your Etc inventory.")


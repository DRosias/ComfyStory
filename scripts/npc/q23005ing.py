# Flier board | Edelstein

if sm.hasItem(4032783):
    sm.setPlayerAsSpeaker()
    sm.sendNext("I carefully posted the flier on the bulletin board.")
    sm.consumeItem(4032783, 1)
    sm.addQRValue(23006, "1")
else:
    sm.setPlayerAsSpeaker()
    sm.sendSayOkay("I need the flier from the part-timer before I can post it here.")

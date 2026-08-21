# Mushroom Castle: Gorgeous Rose interaction for quest 30054.
ROSE = 4034106

if chr.hasQuestInProgress(30054) and not sm.hasItem(ROSE):
    if sm.canHold(ROSE):
        sm.giveItem(ROSE)
    else:
        sm.chat("Please make room in your Etc inventory.")


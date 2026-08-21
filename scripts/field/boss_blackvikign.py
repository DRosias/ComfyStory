# Start the Black Viking quest without its direction sequence and leave the
# player in control for the fight.
sm.lockInGameUI(False, True)
sm.forcedInput(0)

if not chr.hasQuestInProgress(30069) and not sm.hasQuestCompleted(30069):
    sm.startQuest(30069)

if chr.hasQuestInProgress(30069):
    if not sm.hasMobsInField():
        sm.spawnMob(3300110, -100, 285, False)
    sm.showHP()
else:
    sm.warp(106030000)

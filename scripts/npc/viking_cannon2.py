# Skip the return/carry sequence without leaving the player direction-locked.
sm.lockInGameUI(False, True)
sm.forcedInput(0)

if chr.hasQuestInProgress(30067):
    sm.completeQuestNoCheck(30067)

sm.warp(106030000)


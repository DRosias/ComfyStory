# Gate defense maps already contain Viking Squad mobs. Clearing every mob
# completes quest 30059 through the field-clear script.
sm.lockInGameUI(False, True)
sm.forcedInput(0)

if not chr.hasQuestInProgress(30059):
    sm.warp(106030302)


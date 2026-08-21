# Skip the wanted-poster camera sequence.
sm.lockInGameUI(False, True)
sm.forcedInput(0)

if not chr.hasQuestInProgress(30050) and not sm.hasQuestCompleted(30050):
    sm.startQuest(30050)

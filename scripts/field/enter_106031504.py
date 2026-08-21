# Skip Violetta's carry sequence. The sequence itself is quest 30067.
sm.lockInGameUI(False, True)
sm.forcedInput(0)

if not chr.hasQuestInProgress(30067) and not sm.hasQuestCompleted(30067):
    sm.startQuest(30067)
if chr.hasQuestInProgress(30067):
    sm.completeQuestNoCheck(30067)

sm.warp(106030000)

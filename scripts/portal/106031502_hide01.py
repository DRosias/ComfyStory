# Skip the carry sequence and always restore input/UI state.
sm.lockInGameUI(False, True)
sm.forcedInput(0)
if chr.hasQuestInProgress(30067):
    sm.completeQuestNoCheck(30067)
sm.warp(106030000)

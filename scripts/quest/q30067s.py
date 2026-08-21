# Skip the Violetta carry sequence and safely return to the Banquet Hall.
sm.lockInGameUI(False, True)
sm.forcedInput(0)
sm.startQuest(parentID)
sm.completeQuestNoCheck(parentID)
sm.warp(106030000)


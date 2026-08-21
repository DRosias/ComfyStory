# 106030600
PUZZLE_PIECE = 4034114

if chr.hasQuestInProgress(30068):
    if sm.hasItem(PUZZLE_PIECE, 10):
        sm.completeQuest(30068)
        sm.warp(106030800)
    else:
        sm.chat("You need 10 Secret Puzzle Pieces to enter the Captain's Quarters.")
elif chr.hasQuestInProgress(30069):
    sm.warp(106030800)
else:
    sm.chat("The Captain's Quarters are sealed.")

for character in field.getChars():
    if character.hasQuestInProgress(30059):
        character.getQuestManager().completeQuest(30059)

sm.warpField(106030000)


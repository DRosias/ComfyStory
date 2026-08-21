for character in field.getChars():
    if character.hasQuestInProgress(30069):
        quest = character.getQuestManager().getQuestById(30069)
        if quest is not None and quest.isComplete(character):
            character.getQuestManager().completeQuest(30069)

sm.warpField(106030000)


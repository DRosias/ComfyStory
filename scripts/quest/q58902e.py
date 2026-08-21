# Regards, Takeda Shingen Questline | Near Momiji Hills 1 (811000001)
# Completes Quest 58902

TAKEDA = 9000427 # Takeda Shingen
ITEMID = 4034126 # 100 Spells for the Serious Soldier

if "3" in sm.getQRValue(58901): # Regards, Takeda Shingen
    sm.setSpeakerID(TAKEDA)

    sm.flipSpeaker()
    sm.sendNext("Not bad, not bad. Thanks.")

    sm.flipSpeaker()
    sm.sendSay("Let me take a look and I'll surely find a way to lift the spell. Come back later.")


    # Quest completion consumes the required 30 spell scrolls. Do not remove
    # the player's surplus copies of the quest item.
    sm.completeQuest(parentID) # Regards, Takeda Shingen

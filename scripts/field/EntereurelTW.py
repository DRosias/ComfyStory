# Elluel
# 101050000
# User enter script
# Plays initial cutscene for Mercedes after exiting King's Seat once. This will kickstart the rest of Mercedes' quests.

from net.swordie.ms.constants import JobConstants

mercTutEnd = 24009

kingsSeatArrival = 7

def exitFromKingsSeat():
    return chr.getAvatarData().getCharacterStat().getPortal() == kingsSeatArrival

# Only play cutscene for Mercedes when exiting from King's Seat through the normal portal and without having the QRValue flag applied
if JobConstants.isMercedes(chr.getJob()) and sm.getQRValue(mercTutEnd) != "1" and exitFromKingsSeat():
    sm.lockInGameUI(True)
    sm.forcedInput(1)
    sm.sendDelay(1000)
    sm.forcedInput(0)

    sm.removeEscapeButton()
    sm.flipDialoguePlayerAsSpeaker()
    sm.sendNext("Elders?")
    
    sm.forcedInput(2)
    sm.sendDelay(2000)
    sm.forcedInput(0)
    sm.sendNext("Children...")

    sm.forcedInput(1)
    sm.sendDelay(800)
    sm.forcedInput(0)
    sm.sendNext("Everyone is still trapped in the ice...")

    sm.lockInGameUI(False)

    # Setting QRValue for 24009 will allow Awakening (24040) to start
    sm.startQuest(mercTutEnd)
    sm.setQRValue(mercTutEnd, "1", False)

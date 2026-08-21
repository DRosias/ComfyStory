 # Icebyrd Slimm: NLC Mayor (9201050) | NLC Town Center (600000000)
 # Quest: Welcome to New Leaf City Quiz (4900)
 # Author: Tiger

QUIZ_MIN_LEVEL = 90
QUIZ_END_QUEST = 4923
GINSENG_ROOT = 2002022

# Quest.wz defines the quiz as twelve consecutive quests (4900-4911),
# followed by the conclusion quest 4923. Keep each question as its own quest
# so the WZ EXP, item, and Fame rewards are granted normally.
QUIZ_QUESTIONS = (
    (4900, "Where are you?", (
        "Aqua Road",
        "The Forest of Golem",
        "New Leaf City",
        "Henesys",
    ), 2),
    (4901, "Who is Professor Foxwit?", (
        "Professional crackpot",
        "Mad scientist",
        "Time-traveler",
        "Madman who chases mushrooms",
    ), 2),
    (4902, "What's a Foxwit Door?", (
        "A door made of candy and hope",
        "The transport system of New Leaf City",
        "Someplace where foxes hide and play tennis",
        "A mysterious hole that predicts the future",
    ), 1),
    (4903, "Where are the MesoGears?", (
        "Earth",
        "Amoria",
        "Under the sea",
        "Under Bigger Ben",
    ), 3),
    (4904, "What is the Krakian Jungle?", (
        "A magical place of fish and pie",
        "A mall made of gold",
        "A Unicorn-sponsored tea party",
        "The dangerous area north and east of New Leaf City",
    ), 3),
    (4905, "What's a Gear Portal?", (
        "A special place where honey drips out",
        "A weird switch that transports you to a different place",
        "A bear's resting place",
        "A Mechanic skill",
    ), 1),
    (4906, "What do the street signs mean?", (
        "Do not enter until Doomsday",
        "Entering them sends you to the place listed",
        "No magic permitted",
        "You need a permit to enter",
    ), 1),
    (4907, "What do the stoplights do?", (
        "Stop",
        "Go",
        "Depending on the red or green light, the area may or may not be open",
        "They tell time, with colors",
    ), 2),
    (4908, "Who's Jack Masque?", (
        "A dashing bandit prince from Amoria",
        "A professional hobo",
        "An advocate of Compassion for Balrogs",
        "A garden clerk",
    ), 0),
    (4909, "Who's Lita Lawless?", (
        "Sheriff of New Leaf City",
        "A princess",
        "An exceptional juggler",
        "A cake-loving wacko",
    ), 0),
    (4910, "Who's John Barricade?", (
        "A famous treasure hunter and explorer",
        "A famed typist",
        "Mushmom's friend",
        "An obstacle builder",
    ), 0),
    (4911, "When will new boroughs open up in the city?", (
        "When I can eat Mushmom",
        "2019",
        "The moment they are ready to go",
        "When Golden Pigs fly",
    ), 2),
)


def askQuizQuestion(question, answers):
    text = question + "\r\n\r\n"
    for answerIndex in range(len(answers)):
        text += "#L" + str(answerIndex) + "##b" + answers[answerIndex] + "#k#l\r\n"
    return sm.sendNext(text)


def finishQuizQuestline():
    if sm.hasQuestCompleted(QUIZ_END_QUEST):
        return True

    if not sm.hasQuest(QUIZ_END_QUEST):
        sm.startQuest(QUIZ_END_QUEST)
    if not sm.hasQuest(QUIZ_END_QUEST):
        sm.sendSayOkay("I couldn't begin the final part of the quiz. Please speak with me again.")
        return False

    sm.sendNext("So what else would you like to know?\r\n\r\n#L0##bWhat inspired you to start New Leaf City?#k#l")
    sm.sendNext("Well, I always envisioned a place where people could come and live their dreams. When I arrived here, there was nothing, just the jungle and the machinery that was left. I had some help in building this place in exchange for a favor...\r\n\r\n#L0##bWhat kind of favor?#k#l")
    sm.sendNext("I'm really not allowed to talk about it, but it's nothing you have to worry about... hopefully.\r\n\r\n#L0##bWhat continent am I on?#k#l")
    sm.sendNext("You're on the continent of Masteria. There's an old legend that surrounds the continent, but you're standing in the rebuilt portion of Olde Sapp Village. There's an old saying that Masteria is where warriors of all kinds come to master their fate. The continent has risen once more from the sea, and people of all kinds are here, some good, some bad. We're clearing a path deeper into the continent. Who knows what we'll find?\r\n\r\n#L0##bWait, the city was already here?#k#l")
    sm.sendNext("Not exactly. This was once known as Olde Sapp Village, and was smaller than what you see here. Essentially, Masteria has an old fable, but I'm not sure you'd want to hear about that...\r\n\r\n#L0##bYou mentioned it had 'risen from the sea.' I'd like to hear about how it sank sometime...#k#l")
    sm.sendNext("It's only an old fable. I'm sure it wouldn't interest you. Maybe if you help out around the city, I can tell you some more...")

    sm.completeQuest(QUIZ_END_QUEST)
    if not sm.hasQuestCompleted(QUIZ_END_QUEST):
        sm.sendSayOkay("I couldn't finish recording your quiz completion. Please speak with me again.")
        return False

    sm.sendSayOkay("Okay, that's it for this time. Welcome to New Leaf City, and enjoy your stay here!")
    return True


def runQuizQuestline():
    if sm.hasQuestCompleted(QUIZ_END_QUEST):
        sm.sendSayOkay("You've already solved all my questions. Enjoy your trip in New Leaf City!")
        return

    if chr.getLevel() < QUIZ_MIN_LEVEL:
        sm.sendSayOkay("Sorry, but this quiz is only available for level " + str(QUIZ_MIN_LEVEL) + " and above. Please come back when you're ready.")
        return

    if not sm.hasQuest(QUIZ_QUESTIONS[0][0]) and not sm.hasQuestCompleted(QUIZ_QUESTIONS[0][0]):
        sm.sendNext("No problem! I'll give you something nice if you answer all of my questions correctly.")

    for questID, question, answers, correctAnswer in QUIZ_QUESTIONS:
        if sm.hasQuestCompleted(questID):
            continue

        if not sm.hasQuest(questID):
            sm.startQuest(questID)
        if not sm.hasQuest(questID):
            sm.sendSayOkay("I couldn't begin the next quiz question. Please speak with me again.")
            return

        # Question 12 awards 10 Ginseng Roots. Check space before asking so a
        # correct answer cannot leave the player stuck on a failed completion.
        if questID == 4911 and not sm.canHold(GINSENG_ROOT, 10):
            sm.sendSayOkay("You'll receive 10 #i" + str(GINSENG_ROOT) + ":##t" + str(GINSENG_ROOT) + "# for finishing the quiz. Please make room in your Use inventory first.")
            return

        answer = askQuizQuestion(question, answers)
        if answer != correctAnswer:
            sm.sendSayOkay("Wrong! Review what I've told you about New Leaf City, then try this question again.")
            return

        if questID == 4911:
            sm.sendNext("That's right! You've solved all the questions!")
        else:
            sm.sendNext("That's right! Here's the next question.")

        sm.completeQuest(questID)
        if not sm.hasQuestCompleted(questID):
            sm.sendSayOkay("I couldn't record that answer. Please check your inventory and speak with me again.")
            return

    finishQuizQuestline()


while True:
    selection = sm.sendNext("What up! Name's Icebyrd Slimm, mayor of New Leaf City! Happy to see you accepted my invite. So, what can I do for you?	\r\n#b"
			"#L0#What is this place? #l\r\n"
			"#L1#Who is Professor Foxwit? #l\r\n"
			"#L2#What's a Foxwit Door? #l\r\n"
			"#L3#Where are the MesoGears? #l\r\n"
			"#L4#What is the Krakian Jungle? #l\r\n"
			"#L5#What's a Gear Portal? #l\r\n"
			"#L6#What do the street signs mean? #l\r\n"
			"#L7#What's the deal with Jack Masque? #l\r\n"
			"#L8#Lita Lawless looks like a tough cookie, what's her story?#l\r\n"
			"#L9#When will new boroughs open up in the city? #l\r\n"
			"#L10#I want to take the quiz!#l#k")

    if selection == 0:
        sm.sendNext("I've always dreamed of building a city. Not just any city, but one where everyone was welcome. I used to live in Kerning City, so I decided to see if I could create a city. As I went along in finding the means to do so, I encountered many people, some of whom I've come to regard as friends. Like Professor Foxwit-he's our resident genius; saved him from a group of man-eating plants. Jack Masque is an old hunting buddy from Amoria-almost too smooth of a talker for his own good. Lita and I are old friends from Kerning City-she's saved me a few times with that weapon of hers; so I figured she was a perfect choice for Town Sheriff. It took a bit of persuasion, but she came to believe her destiny lies here. About our resident explorer, Barricade came searching for something; he agreed to bring whatever he found to the museum. I'd heard stories about him and his brother when I was still in Kerning City. And Elpam...well, let's just say he's not from around here. At all. We've spoken before, and he seems to mean well, so I've allowed him to stay. I just realized that I've rambled quite a bit! What else would you like to know?")
    elif selection == 1:
        sm.sendNext("A pretty spry guy for being 97. He' s a time-traveler I ran into outside the city one day. Old guy had a bit of trouble with some jungle creatures-like they tried to eat him. In return for me saving him, he agreed to build a time museum. I get the feeling that he's come here for another reason, as he's mentioned more than a few times that New Leaf City has an interesting role to play in the future. Maybe you can find out a bit more... ")
    elif selection == 2:
        sm.sendNext("Heh, I asked the same thing when I saw the Professor building them. They're warp points. Pressing Up will warp you to another location. I recommend getting the hang of them, they're our transport system.")
    elif selection == 3:
        sm.sendNext("The MesoGears are beneath Bigger Ben. It's a monster-infested section of Bigger Ben that Barricade discovered. It seems to reside in a separate section of the tower-quite strange if you ask me. I hear he needs a bit of help exploring it, you should see him. Be careful though, the Wolf Spiders in there are no joke.")
    elif selection == 4:
        sm.sendNext("Ah...well. The Krakian Jungle is located on the outskirts of New Leaf City. Many new and powerful creatures roam those areas, so you'd better be prepared to fight if you head out there. It's at the left end of town. Rumors abound that the Jungle leads to a lost city, but we haven't found anything yet.")
    elif selection == 5:
        sm.sendNext("Well, when John found himself in the MesoGears portion of Bigger Ben, he stood on one and went to another location. However, he could only head back and forth-they don't cycle through like the Foxwit Door. Ancient tech for you.")
    elif selection == 6:
        sm.sendNext("Well, you'll see them just about everywhere. They're areas under construction. The Red lights mean it's not finished, but the Green lights mean it's open. Check back often, we're always building!")
    elif selection == 7:
        sm.sendNext("Ah, Jack. You know those guys that are too cool for school? The ones who always seem to get away with everything? AND get the girl? Well, that's Jack, but without the girl. He thinks he blew his chance, and began wearing that mask to hide his true identity. My lips are sealed about who he is, but he's from Amoria. He might tell you a bit more if you ask him.")
    elif selection == 8:
        sm.sendNext("I've known Lita for a while, thought we've just recently rekindled our friendship. I didn't see her for a quite a bit, but I understand why. She trained for a very, very long time as a Thief. Matter of fact, that's how we first met? I was besieged a group of wayward Mushrooms, and she jumped in to help. When it was time to a pick a sheriff, it was a no-brainer. She's made a promise to help others in their training and protect the city, so if you're interested in a bit of civic duty, speak with her. ")
    elif selection == 9:
        sm.sendNext("Soon, my friend. Even though you can't see them, the city developers are hard at work. When they're ready, we'll open them. I know you're looking forward to it and so am I!")
    elif selection == 10:
        runQuizQuestline()
        break

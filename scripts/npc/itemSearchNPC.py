from net.swordie.ms.util import Util
from net.swordie.ms.loaders import StringData
from java.util import TreeMap

PAGE_SIZE = 10

def start():
    sm.sendNext("Hello! I can help you search for items by #bID#k or #bname#k.")

    query = sm.sendAskText("Enter an item ID or name (at least 3 characters):", "", 1, 30).strip()

    if len(query) < 3:
        sm.sendSayOkay("Please enter at least #b3 characters#k when searching.")
        return
    else:
        results = searchItems(query)

    if not results:
        sm.sendSayOkay("No items found.")
        return

    showPage(results, 0)

def searchItems(query):
    itemIds = []

    if Util.isNumber(query):
        name = StringData.getItemStringById(int(query))
        if (name is None):
            return []
        itemIds.append(int(query))
        return itemIds

    resultMap = StringData.getItemStringByName(query, False)
    if resultMap is None or resultMap.isEmpty():
        return []

    sortedMap = TreeMap(resultMap)

    for entry in sortedMap.entrySet():
        itemIds.append(entry.getKey())

    return itemIds

def showPage(results, page):
    startIdx = page * PAGE_SIZE
    endIdx = min(startIdx + PAGE_SIZE, len(results))

    text = "#bItem Search Results#k\r\n"
    text += "Page {} of {}\r\n\r\n".format(page + 1, (len(results) - 1) // PAGE_SIZE + 1)

    selectionMap = {}
    sel = 0

    for i in range(startIdx, endIdx):
        itemId = results[i]
        selectionMap[sel] = itemId
        text += "#L{}##i{}# #t{}# #b({})#k#l\r\n".format(sel, itemId, itemId, itemId)
        sel += 1

    if page > 0:
        selectionMap[sel] = -1
        text += "\r\n#L{}#Previous Page#l".format(sel)
        sel += 1

    if endIdx < len(results):
        selectionMap[sel] = -2
        text += "\r\n#L{}#Next Page#l".format(sel)

    choice = sm.sendNext(text)
    handleSelection(choice, selectionMap, results, page)

def handleSelection(choice, selectionMap, results, page):
    action = selectionMap.get(choice)

    if action == -1:
        showPage(results, page - 1)
    elif action == -2:
        showPage(results, page + 1)
    elif action is not None:
        showItemPreview(action, results, page)
    else:
        sm.sendSayOkay("Invalid selection.")

def showItemPreview(itemId, results, page):
    text = "#bItem Preview#k\r\n\r\n"
    text += "#i{}# #t{}# #b({})#k\r\n\r\n".format(itemId, itemId, itemId)
    text += "#L0#Get this item#l\r\n"
    text += "#L1#Back to list#l"

    choice = sm.sendNext(text)

    if choice == 0:
        askQuantity(itemId)
    else:
        showPage(results, page)

def askQuantity(itemId):
    qty = sm.sendAskNumber("How many #b#t{}##k would you like?".format(itemId), 1, 1, 1000)

    sm.giveItem(itemId, qty)
    sm.sendSayOkay("You received #b{}x #t{}##k.".format(qty, itemId))

# ----- S T A R T -----
start()
function doPost(e) {
  var lock = LockService.getScriptLock();
  lock.tryLock(10000);

  try {
    var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
    var data = JSON.parse(e.postData.contents);
    
    // 1. Column J (SX ID Column) එකේ තියෙන සියලුම Data ගන්න
    var lastRow = sheet.getLastRow();
    var sxColumnValues = sheet.getRange(1, 10, lastRow > 0 ? lastRow : 1, 1).getValues();
    
    var maxNumber = 1000; // Default base ID

    // 2. අන්තිමටම තියෙන ලොකුම SX Number එක සොයාගැනීම
    for (var i = 0; i < sxColumnValues.length; i++) {
      var val = sxColumnValues[i][0].toString().trim();
      var match = val.match(/SX(\d+)/i);
      if (match) {
        var num = parseInt(match[1], 10);
        if (num > maxNumber) {
          maxNumber = num;
        }
      }
    }

    // 3. හරියටම ඊළඟ Number එක සෑදීම
    var nextSxNumber = maxNumber + 1;
    var sxId = "SX" + nextSxNumber;

    var timestamp = new Date();

    // 4. Data append කිරීම (My Partner column එක අයින් කර ඇත)
    sheet.appendRow([
      timestamp,          // Column A: Timestamp
      data.name,          // Column B: Name
      data.broker,        // Column C: Broker Name
      data.accountId,     // Column D: Account ID
      data.telegram,      // Column E: Telegram
      data.whatsapp,      // Column F: WhatsApp
      "",                 // Column G: Email
      "",                 // Column H: Age
      data.nic,           // Column I: NIC
      sxId,               // Column J: SX ID
      "",                 // Column K: Add Group
      data.partner        // Column L: Partner
    ]);

    return ContentService
      .createTextOutput(JSON.stringify({ 'result': 'success', 'sxId': sxId }))
      .setMimeType(ContentService.MimeType.JSON);

  } catch (error) {
    return ContentService
      .createTextOutput(JSON.stringify({ 'result': 'error', 'error': error.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  } finally {
    lock.releaseLock();
  }
}
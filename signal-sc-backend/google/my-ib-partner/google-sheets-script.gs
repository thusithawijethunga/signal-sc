function doPost(e) {
  var lock = LockService.getScriptLock();
  lock.tryLock(10000);

  try {
    // Data එක JSON body (e.postData) හෝ form param (e.parameter.data) ලෙස එන්න පුළුවන්
    var raw = null;
    if (e && e.postData && e.postData.contents) {
      raw = e.postData.contents;
    } else if (e && e.parameter && e.parameter.data) {
      raw = e.parameter.data;
    }

    if (!raw) {
      return ContentService.createTextOutput(JSON.stringify({ 'result': 'error', 'error': 'no data received' }))
        .setMimeType(ContentService.MimeType.JSON);
    }

    var data = JSON.parse(raw);

    var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();

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

    // 4. Data append කිරීම
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

// Google Sheets එකෙන් Members ලබා ගැනීමට (GET)
function doGet(e) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  var rows = sheet.getDataRange().getValues();
  var data = [];

  for (var i = 1; i < rows.length; i++) {
    var row = rows[i];
    if (String(row[1] || "") !== "") { // Name column (B) එකේ data තිබේ නම්
      data.push({
        sxId: row[9],      // Column J: SX ID
        name: row[1],      // Column B: Name
        broker: row[2],    // Column C: Broker
        accountId: row[3], // Column D: Account ID
        telegram: row[4],  // Column E: Telegram
        whatsapp: row[5],  // Column F: WhatsApp
        nic: row[8],       // Column I: NIC
        partner: row[11]   // Column L: Partner
      });
    }
  }

  return ContentService.createTextOutput(JSON.stringify(data)).setMimeType(ContentService.MimeType.JSON);
}

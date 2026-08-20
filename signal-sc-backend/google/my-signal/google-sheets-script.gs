// Signal Xpress — Google Sheets Two-Way Sync
// Columns: no, date, pair, direction, entry1, entry2, sl, tp1, tp2, tp3, tp4, pips, profit, result, channel

function doPost(e) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  var data = JSON.parse(e.postData.contents);

  // දත්ත පෝලිමේ අදාළ කඳවුරු (Columns) වලට අදාළ අගයන් ලබා දීම
  var no = data.no || "";
  var date = data.date || "";
  var pair = data.pair || "";
  var direction = data.direction || "";
  var entry1 = data.entry1 || "";
  var entry2 = data.entry2 || "";
  var sl = data.sl || "";
  var tp1 = data.tp1 || "";
  var tp2 = data.tp2 || "";
  var tp3 = data.tp3 || "";
  var tp4 = data.tp4 || "";
  var pips = data.pips || "";
  var profit = data.profit || "";
  var result = data.result || "";
  var channel = data.channel || "";

  // මීට පෙර එකතු කළ trade එකක් නම් එය Update කිරීම, නැතහොත් අලුතින් එකතු කිරීම
  var rows = sheet.getDataRange().getValues();
  var rowIndex = -1;

  for (var i = 1; i < rows.length; i++) {
    if (rows[i][0] == no) { // 1 වන Column එක 'No' ලෙස සලකයි
      rowIndex = i + 1;
      break;
    }
  }

  var targetRow;
  if (rowIndex > -1) {
    // පවතින Row එක Update කිරීම
    sheet.getRange(rowIndex, 1, 1, 15).setValues([[no, date, pair, direction, entry1, entry2, sl, tp1, tp2, tp3, tp4, pips, profit, result, channel]]);
    targetRow = rowIndex;
  } else {
    // අලුත් Row එකක් Append කිරීම
    sheet.appendRow([no, date, pair, direction, entry1, entry2, sl, tp1, tp2, tp3, tp4, pips, profit, result, channel]);
    targetRow = sheet.getLastRow();
  }

  // Result අනුව Row එක Highlight කිරීම
  applyHighlight(sheet, targetRow, result);

  return ContentService.createTextOutput(JSON.stringify({"status": "success"})).setMimeType(ContentService.MimeType.JSON);
}

// Row Highlight (WIN = green, LOSS = red, BE = gold)
function applyHighlight(sheet, row, result) {
  var r = String(result || "").toUpperCase();
  var range = sheet.getRange(row, 1, 1, 15);

  if (r === "WIN") {
    range.setBackground("#0a3d2e");
    range.setFontColor("#92D050");
  } else if (r === "LOSS") {
    range.setBackground("#3d0a0a");
    range.setFontColor("#ff6b6b");
  } else if (r === "BE") {
    range.setBackground("#3d2e0a");
    range.setFontColor("#d4a04c");
  } else {
    range.setBackground(null);
    range.setFontColor("#ffffff");
  }
}

// Google Sheets එකෙන් දත්ත Dashboard එකට ලබා ගැනීමට (GET)
function doGet(e) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  var rows = sheet.getDataRange().getValues();
  var headers = rows[0];
  var data = [];

  for (var i = 1; i < rows.length; i++) {
    var row = rows[i];
    if(row[0] !== "") {
      var obj = {
        no: row[0],
        date: row[1],
        pair: row[2],
        direction: row[3],
        entry1: row[4],
        entry2: row[5],
        sl: row[6],
        tp1: row[7],
        tp2: row[8],
        tp3: row[9],
        tp4: row[10],
        pips: row[11],
        profit: row[12],
        result: row[13],
        channel: row[14]
      };
      data.push(obj);
    }
  }

  return ContentService.createTextOutput(JSON.stringify(data)).setMimeType(ContentService.MimeType.JSON);
}

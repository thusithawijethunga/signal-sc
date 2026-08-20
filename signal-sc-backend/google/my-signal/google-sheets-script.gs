// Signal Xpress — Google Sheets Two-Way Sync
// Columns: no, date, pair, direction, entry1, entry2, sl, tp1, tp2, tp3, tp4, pips, profit, result, channel

function doPost(e) {
  // Data එක JSON body (e.postData) හෝ form param (e.parameter.data) ලෙස එන්න පුළුවන්
  var raw = null;
  if (e && e.postData && e.postData.contents) {
    raw = e.postData.contents;
  } else if (e && e.parameter && e.parameter.data) {
    raw = e.parameter.data;
  }

  if (!raw) {
    return ContentService.createTextOutput(JSON.stringify({"status": "no data received"}))
      .setMimeType(ContentService.MimeType.JSON);
  }

  var data = JSON.parse(raw);

  var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();

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
  var hit_type = data.hit_type || ""; // TP1 / TP2 / TP3 / TP4 / SL / BE

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

  // Hit වුනු cell එක පමණක් Highlight කිරීම
  if (hit_type) {
    highlightHitCell(sheet, targetRow, hit_type);
  }

  return ContentService.createTextOutput(JSON.stringify({"status": "success"})).setMimeType(ContentService.MimeType.JSON);
}

// Hit වුනු cell එක (TP/SL/BE column) පමණක් Highlight කිරීම
function highlightHitCell(sheet, row, hitType) {
  var h = String(hitType || "").toUpperCase();

  // Column map (1-based): sl=7, tp1=8, tp2=9, tp3=10, tp4=11, result=14
  var colMap = {
    "SL": 7,
    "TP1": 8,
    "TP2": 9,
    "TP3": 10,
    "TP4": 11,
    "BE": 14
  };

  var col = colMap[h];
  if (!col) return;

  var cell = sheet.getRange(row, col);

  if (h === "SL") {
    cell.setBackground("#ff3b3b");
    cell.setFontColor("#ffffff");
  } else if (h === "BE") {
    cell.setBackground("#d4a04c");
    cell.setFontColor("#0a0e0c");
  } else {
    // TP1 - TP4
    cell.setBackground("#00B050");
    cell.setFontColor("#0a0e0c");
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

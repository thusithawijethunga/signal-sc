// Signal Xpress — Google Sheets Two-Way Sync
// Paste into Google Apps Script (Extensions > Apps Script) for your sheet.
// Columns: no, date, pair, direction, entry1, entry2, sl, tp1, tp2, tp3, tp4, pips, profit, result, channel

function doPost(e) {
  if (!e || !e.postData || !e.postData.contents) {
    return ContentService.createTextOutput(JSON.stringify({ "status": "no data received" }))
      .setMimeType(ContentService.MimeType.JSON);
  }

  var data;
  try {
    data = JSON.parse(e.postData.contents);
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({ "status": "invalid JSON" }))
      .setMimeType(ContentService.MimeType.JSON);
  }

  var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();

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

  var rows = sheet.getDataRange().getValues();
  var rowIndex = -1;

  for (var i = 1; i < rows.length; i++) {
    if (rows[i][0] == no) {
      rowIndex = i + 1;
      break;
    }
  }

  var targetRow;
  if (rowIndex > -1) {
    sheet.getRange(rowIndex, 1, 1, 15).setValues([[no, date, pair, direction, entry1, entry2, sl, tp1, tp2, tp3, tp4, pips, profit, result, channel]]);
    targetRow = rowIndex;
  } else {
    sheet.appendRow([no, date, pair, direction, entry1, entry2, sl, tp1, tp2, tp3, tp4, pips, profit, result, channel]);
    targetRow = sheet.getLastRow();
  }

  applyHighlight(sheet, targetRow, result);

  return ContentService.createTextOutput(JSON.stringify({ "status": "success" })).setMimeType(ContentService.MimeType.JSON);
}

// Highlight the row based on result
function applyHighlight(sheet, row, result) {
  var r = String(result || "").toUpperCase();
  var range = sheet.getRange(row, 1, 1, 15);

  if (r === "WIN") {
    range.setBackground("#0a3d2e");       // dark green
    sheet.getRange(row, 1, 1, 15).setFontColor("#92D050"); // green-light text
  } else if (r === "LOSS") {
    range.setBackground("#3d0a0a");       // dark red
    sheet.getRange(row, 1, 1, 15).setFontColor("#ff6b6b"); // red text
  } else if (r === "BE") {
    range.setBackground("#3d2e0a");       // dark amber
    sheet.getRange(row, 1, 1, 15).setFontColor("#d4a04c"); // gold text
  } else {
    // RUNNING / PENDING — clear fill, default white text
    range.setBackground(null);
    sheet.getRange(row, 1, 1, 15).setFontColor("#ffffff");
  }
}

function doGet(e) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  var rows = sheet.getDataRange().getValues();
  var headers = rows[0];
  var data = [];

  for (var i = 1; i < rows.length; i++) {
    var row = rows[i];
    if (row[0] !== "") {
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
function filterByDate() {
  const val = document.getElementById('date-filter').value;
  if (!val) {
    currentFilteredData = [...trades];
  } else {
    currentFilteredData = trades.filter(t => {
      if (!t.date) return false;
      const tradeDate = String(t.date).split('T')[0];
      return tradeDate === val;
    });
  }
  // තෝරාගත් දිනයට අදාළ දත්ත පමණක් Dashboard එකේ Metrics සහ Tables වල පෙන්වීම සඳහා renderAll අලුත් දත්ත සමඟ ක්‍රියාත්මක කරයි
  updateMetrics(currentFilteredData);
  renderDashboardTable(currentFilteredData);
  renderAdminTable();
}
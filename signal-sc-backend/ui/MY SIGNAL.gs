function doPost(e) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  var data = JSON.parse(e.postData.contents);
  
  // දත්ත පෝලිමේ අදාළ කඳවුරු (Columns) වලට අදාළ අගයන් ලබා දීම
  // (ඔබේ Sheet එකේ Column පිළිවෙළට අනුව මෙහි නාමයන් సరి කර ගන්න)
  var no = data.no || "";
  var date = data.date || "";
  var pair = data.pair || "";
  var direction = data.direction || "";
  var entry1 = data.entry1 || "";
  var entry2 = data.entry2 || "";
  var sl = data.sl || "";       // මෙන්න මෙතැන 'sl' අගය නිවැරදිව ලබා ගැනීම සිදු කරයි
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

  if (rowIndex > -1) {
    // පවතින Row එක Update කිරීම
    sheet.getRange(rowIndex, 1, 1, 15).setValues([[no, date, pair, direction, entry1, entry2, sl, tp1, tp2, tp3, tp4, pips, profit, result, channel]]);
  } else {
    // අලුත් Row එකක් Append කිරීම
    sheet.appendRow([no, date, pair, direction, entry1, entry2, sl, tp1, tp2, tp3, tp4, pips, profit, result, channel]);
  }

  return ContentService.createTextOutput(JSON.stringify({"status": "success"})).setMimeType(ContentService.MimeType.JSON);
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
        sl: row[6],       // Google Sheet එකේ Stop Loss අගය කියවා ගැනීම
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
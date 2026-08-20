package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardBackground
import com.example.ui.theme.PrimarySky
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextSecondary

@Composable
fun AddSignalDialog(
    onDismiss: () -> Unit,
    onSubmit: (pair: String, type: String, entry: String, tp1: String, tp2: String, tp3: String, tp4: String, sl: String) -> Unit
) {
    var pair by remember { mutableStateOf("XAU/USD") }
    var type by remember { mutableStateOf("BUY") }
    var entry by remember { mutableStateOf("4122 / 4120") }
    var tp1 by remember { mutableStateOf("4125") }
    var tp2 by remember { mutableStateOf("4130") }
    var tp3 by remember { mutableStateOf("4135") }
    var tp4 by remember { mutableStateOf("4140") }
    var sl by remember { mutableStateOf("4115") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "📡 Broadcast New Signal",
                    color = PrimarySky,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Direction selector (BUY vs SELL)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { type = "BUY" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "BUY") AccentEmerald else BorderColor
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("BUY", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { type = "SELL" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "SELL") AccentRed else BorderColor
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SELL", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(label = "Trading Pair (e.g. EUR/USD, XAU/USD)", value = pair) { pair = it }
                CustomTextField(label = "Entry Price", value = entry) { entry = it }
                CustomTextField(label = "Target TP1", value = tp1) { tp1 = it }
                CustomTextField(label = "Target TP2", value = tp2) { tp2 = it }
                CustomTextField(label = "Target TP3", value = tp3) { tp3 = it }
                CustomTextField(label = "Target TP4", value = tp4) { tp4 = it }
                CustomTextField(label = "Stop Loss (SL)", value = sl) { sl = it }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (pair.isNotBlank() && entry.isNotBlank()) {
                                onSubmit(pair, type, entry, tp1, tp2, tp3, tp4, sl)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimarySky)
                    ) {
                        Text("Broadcast Signal", color = TextLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary, fontSize = 12.sp) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimarySky,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor = PrimarySky,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextLight,
            unfocusedTextColor = TextLight
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true
    )
}

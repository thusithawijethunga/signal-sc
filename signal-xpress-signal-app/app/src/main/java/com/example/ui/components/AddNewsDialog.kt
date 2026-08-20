package com.example.ui.components

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardBackground
import com.example.ui.theme.PrimarySky
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextSecondary

@Composable
fun AddNewsDialog(
    onDismiss: () -> Unit,
    onSubmit: (time: String, currency: String, title: String, impact: String, forecast: String, previous: String, description: String) -> Unit
) {
    var time by remember { mutableStateOf("08:30 PM Today") }
    var currency by remember { mutableStateOf("USD") }
    var title by remember { mutableStateOf("Unemployment Claims") }
    var impact by remember { mutableStateOf("HIGH") }
    var forecast by remember { mutableStateOf("220K") }
    var previous by remember { mutableStateOf("215K") }
    var description by remember { mutableStateOf("Key US labor market indicator. Watch out for USD volatility!") }

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
                    text = "📰 Add Economic Calendar Event",
                    color = PrimarySky,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Impact selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { impact = "HIGH" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (impact == "HIGH") AccentRed else BorderColor
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("HIGH", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { impact = "MEDIUM" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (impact == "MEDIUM") AccentAmber else BorderColor
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("MEDIUM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { impact = "LOW" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (impact == "LOW") PrimarySky else BorderColor
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("LOW", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(label = "Release Time", value = time) { time = it }
                CustomTextField(label = "Currency (USD, EUR, GBP, JPY)", value = currency) { currency = it }
                CustomTextField(label = "Event Title", value = title) { title = it }
                CustomTextField(label = "Forecast Value", value = forecast) { forecast = it }
                CustomTextField(label = "Previous Value", value = previous) { previous = it }
                CustomTextField(label = "Risk Description / Guidance", value = description) { description = it }

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
                            if (title.isNotBlank()) {
                                onSubmit(time, currency, title, impact, forecast, previous, description)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimarySky)
                    ) {
                        Text("Publish News", color = TextLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

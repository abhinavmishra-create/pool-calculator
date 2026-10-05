package com.example.poolcalculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Line() = Box(
    Modifier.fillMaxWidth().height(1.dp)
        .background(MaterialTheme.colorScheme.outlineVariant)
)

@Composable
fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String = "",
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) onValueChange(it) },
        label = { Text(label) },
        trailingIcon = { if (suffix.isNotEmpty()) Text(suffix, Modifier.padding(end = 14.dp)) },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun <T> UnitSelector(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            options.forEach { opt ->
                Row(
                    Modifier
                        .selectable(selected = opt == selected, onClick = { onSelect(opt) })
                        .padding(end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = opt == selected, onClick = { onSelect(opt) })
                    Text(label(opt), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    title: String,
    value: String,
    formula: String? = null,
    highlight: Boolean = false
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = if (highlight) 24.sp else 19.sp, fontWeight = FontWeight.Bold)
            formula?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) = Text(
    text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
    modifier = Modifier.padding(top = 6.dp)
)

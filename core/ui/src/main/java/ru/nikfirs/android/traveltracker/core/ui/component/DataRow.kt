package ru.nikfirs.android.traveltracker.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.extension.clickableOnce

@Composable
fun InfoDataBox(
    header: String,
    data: String,
    modifier: Modifier = Modifier,
    dataLines: Int = Int.MAX_VALUE,
    dataColor: Color = MaterialTheme.colorScheme.onBackground,
    onDataClick: (() -> Unit)? = null,
) {
    if (data.isBlank()) return
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = header,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = data,
            style = MaterialTheme.typography.bodyLarge,
            color = dataColor,
            maxLines = dataLines,
            modifier = Modifier
                .fillMaxWidth()
                .clickableOnce(onDataClick != null) { onDataClick?.invoke() }
                .padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InfoDataBoxPreview(){
    InfoDataBox(
        header = "Header",
        data = "data"
    )
}
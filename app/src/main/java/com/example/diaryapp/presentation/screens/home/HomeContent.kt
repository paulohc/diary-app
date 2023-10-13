package com.example.diaryapp.presentation.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.diaryapp.model.Diary
import com.example.diaryapp.presentation.components.DiaryHolder
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    diarieNotes: Map<LocalDate, List<Diary>>,
    onClick: (String) -> Unit,
) {
    if (diarieNotes.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = paddingValues.calculateTopPadding())
                .padding(bottom = paddingValues.calculateBottomPadding())
                .padding(start = paddingValues.calculateStartPadding(LayoutDirection.Ltr))
                .padding(end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)),
        ) {
            diarieNotes.forEach { (localDate, diaries) ->
                stickyHeader(key = localDate) {
                    DateHeader(localDate = localDate)
                }

                items(
                    count = diaries.size,
                    key = { diaries[it]._id.toString() },
                ) {index ->
                    val diary = diaries[index]
                    DiaryHolder(diary = diary, onClick = onClick)
                }
            }
        }
    } else {
        EmptyPage()
    }
}

@Composable
fun DateHeader(localDate: LocalDate) {
    Row(
        modifier = Modifier
            .padding(vertical = 14.dp)
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format("%02d", localDate.dayOfMonth), style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Light,
                )
            )
            Text(
                text = localDate.dayOfWeek.toString().take(3), style = TextStyle(
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = FontWeight.Light,
                )
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = localDate.month.toString().lowercase().replaceFirstChar { it.titlecase() },
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Light,
                )
            )
            Text(
                text = "${localDate.year}",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                style = TextStyle(
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = FontWeight.Light,
                )
            )
        }
    }
}

@Composable
fun EmptyPage(
    title: String = "Empty Diary",
    subtitle: String = "Write Something",
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title, style = TextStyle(
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = subtitle, style = TextStyle(
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = FontWeight.Normal
            )
        )
    }
}

@Composable
@Preview(showBackground = true)
fun DateHeaderPreview() {
    DateHeader(localDate = LocalDate.now())
}
package com.example.wmtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.wmtest.ui.theme.WorkManagerTextTheme
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Нужно для viewModel()
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkInfo
import androidx.work.WorkManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаем ViewModel
        val viewModel: MainViewModel by viewModels()

        setContent {
            MaterialTheme {
                SyncScreen(viewModel)
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(viewModel: MainViewModel) {
    // Состояние поля ввода
    var urlText by remember { mutableStateOf("https://myserver.com/api/v1/users") }

    // Следим за ID текущей работы
    // В реальном коде лучше использовать SwitchMap, но для учебного примера упростим:
    // Мы просто получаем LiveData по ID, если работа запущена
    val workId by remember { mutableStateOf(viewModel) } // просто ссылка на VM

    // Наблюдаем за WorkInfo. Это самая важная часть!
    // Если статус изменится (Running -> Succeeded), Compose перерисует экран.
    // Примечание: тут упрощенная схема подписки
    var currentWorkId by remember { mutableStateOf<java.util.UUID?>(null) }

    // Если ID есть, подписываемся на него
    val workInfoState = if (currentWorkId != null) {
        WorkManager.getInstance(androidx.compose.ui.platform.LocalContext.current)
            .getWorkInfoByIdLiveData(currentWorkId!!)
            .observeAsState()
    } else {
        remember { mutableStateOf(null) }
    }

    val workInfo = workInfoState.value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Фоновая синхронизация") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Ввод URL
            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                label = { Text("URL сервера") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка запуска
            Button(
                onClick = {
                    // Запуск через ViewModel
                    viewModel.startSync(urlText)
                    // Грязный хак для получения ID последнего запроса в UI,
                    // в реальном проекте это делается реактивно внутри VM
                    // Но для лабораторной сойдет:
                    // (Мы предполагаем, что VM сохраняет ID, но тут мы его перехватим через энкью)
                    // Лучше перепишем логику кнопки, чтобы было честно:
                    val request = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>()
                        .setInputData(androidx.work.workDataOf("KEY_URL" to urlText))
                        .build()
                    androidx.work.WorkManager.getInstance(viewModel.getApplication()).enqueue(request)
                    currentWorkId = request.id
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = workInfo?.state != WorkInfo.State.RUNNING // Блокируем, если уже идет
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Начать синхронизацию")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ОТОБРАЖЕНИЕ СТАТУСА (Самое интересное)
            StatusCard(workInfo)
        }
    }
}

@Composable
fun StatusCard(workInfo: WorkInfo?) {
    // Если задачи еще не было
    if (workInfo == null) {
        Text("Ожидание задач...", color = Color.Gray)
        return
    }

    val state = workInfo.state

    // Выбираем цвет и текст в зависимости от статуса
    val (color, text) = when (state) {
        WorkInfo.State.ENQUEUED -> Color.Blue to "В очереди..."
        WorkInfo.State.RUNNING -> Color(0xFFFFA500) to "Выполняется (подождите)..." // Оранжевый
        WorkInfo.State.SUCCEEDED -> Color.Green to "Готово!"
        WorkInfo.State.FAILED -> Color.Red to "Ошибка!"
        WorkInfo.State.BLOCKED -> Color.Gray to "Блокировано"
        WorkInfo.State.CANCELLED -> Color.Gray to "Отменено"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Статус задачи:",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )

            // Показываем прогресс-бар, если выполняется
            if (state == WorkInfo.State.RUNNING) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Показываем результат (Output Data), если готово
            if (state == WorkInfo.State.SUCCEEDED) {
                val outputText = workInfo.outputData.getString("KEY_RESULT") ?: ""
                Spacer(Modifier.height(8.dp))
                Text(outputText, fontSize = 14.sp)
            }
        }
    }
}
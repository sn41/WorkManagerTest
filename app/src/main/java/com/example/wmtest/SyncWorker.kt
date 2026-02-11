    package com.example.wmtest

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

// Ключи для передачи данных
const val KEY_INPUT_URL = "KEY_URL"
const val KEY_OUTPUT_STATUS = "KEY_RESULT"

class SyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        // 1. Получаем входные данные
        val url = inputData.getString(KEY_INPUT_URL)

        // Имитация проверки: если URL пустой, задача провалена
        if (url.isNullOrBlank()) {
            Log.e("SyncWorker", "URL is missing!")
            return Result.failure()
        }

        try {
            // 2. Имитация долгой работы (синхронизация)
            Log.d("SyncWorker", "Starting sync with $url...")

            // Этап 2.1: Подготовка (1 секунда)
            Thread.sleep(1000) // Имитация задержки сети
            Log.d("SyncWorker", "Sync finished!")

            // Этап 2.2: Загрузка (2 секунды)
            // В реальности тут был бы сетевой запрос
            Log.d("SyncWorker", "Качаем данные...")
            Thread.sleep(2000)

            // 3. Возвращаем результат с данными
//            val output = workDataOf(KEY_OUTPUT_STATUS to "Success: Data synced")
            val output = workDataOf(KEY_OUTPUT_STATUS to "Данные с $url успешно загружены (ID: 12345)")
            return Result.success(output)

        } catch (e: Exception) {
            Log.e("SyncWorker", "Ошибка", e)
            // Если упало исключение, говорим системе "повторить позже"
            return Result.retry()
        }
    }
}
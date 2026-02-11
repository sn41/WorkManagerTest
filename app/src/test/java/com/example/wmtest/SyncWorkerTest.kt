package com.example.wmtest

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker.Result
import androidx.work.testing.TestWorkerBuilder
import androidx.work.workDataOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner // Требуется для Context в unit-тестах

// Используем Robolectric, чтобы симулировать Android-окружение без эмулятора
@RunWith(RobolectricTestRunner::class)
class SyncWorkerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun doWork_validInput_returnsSuccess() {
        // ARRANGE (Подготовка)
        // Создаем входные данные
        val input = workDataOf(KEY_INPUT_URL to "https://myserver.com/api")

        // Строим Воркер с помощью TestWorkerBuilder (специальный класс для тестов)
        val worker = TestWorkerBuilder.from(context, SyncWorker::class.java)
            .setInputData(input)
            .build()

        // ACT (Действие)
        // Запускаем работу синхронно (прямо здесь и сейчас)
        val result = worker.doWork()

        // ASSERT (Проверка)
        // Проверяем, что результат Success
        assertTrue(result is Result.Success)

        // Проверяем выходные данные
        val outputData = (result as Result.Success).outputData
        assertEquals("Success: Data synced", outputData.getString(KEY_OUTPUT_STATUS))
    }

    @Test
    fun doWork_emptyInput_returnsFailure() {
        // ARRANGE
        // Не передаем URL
        val worker =TestWorkerBuilder.from(context, SyncWorker::class.java)
            .build()

        // ACT
        val result = worker.doWork()

        // ASSERT
        assertTrue(result is Result.Failure)
    }
}
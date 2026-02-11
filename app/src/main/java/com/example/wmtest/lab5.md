#### Часть 1. Unit-тестирование Воркера (Local Test)

**Задача**: Проверить логику внутри метода doWork() изолированно. Работает ли наш код, если передать ему правильные данные?

**Перейдите в папку src/test/java/... (Локальные тесты).**

**Создайте файл SyncWorkerTest.kt.**

```kotlinotlin
package com.example.workmanagertest

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
        val worker = TestWorkerBuilder<SyncWorker>(context)
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
        val worker = TestWorkerBuilder<SyncWorker>(context)
            .build()

        // ACT
        val result = worker.doWork()

        // ASSERT
        assertTrue(result is Result.Failure)
    }
}
```

_Примечание: Для работы этого теста нужно добавить testImplementation("org.robolectric:robolectric:4.11.1") в build.gradle, 
либо перенести этот тест в androidTest, если не хотите использовать Robolectric._

#### Часть 2. Интеграционное тестирование (Instrumented Test)

**Задача**: Проверить, как WorkManager управляет очередью. Правильно ли задача переходит из состояния ENQUEUED в SUCCEEDED?

Здесь мы используем эмулятор, так как нам нужна настоящая (но подменяемая) база данных WorkManager.

**Перейдите в папку src/androidTest/java/....**

**Создайте файл WorkManagerIntegrationTest.kt.**

```kotlin
package com.example.workmanagertest

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class WorkManagerIntegrationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // ВАЖНЫЙ МОМЕНТ: Инициализируем WorkManager в тестовом режиме.
        // SynchronousExecutor заставляет все задачи выполняться мгновенно в том же потоке,
        // чтобы тест не ждал.
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Инициализация тестового хелпера
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun testPeriodicWork_isEnqueued() {
        // 1. Создаем запрос на периодическую задачу (раз в 15 минут)
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setInputData(workDataOf(KEY_INPUT_URL to "http://test.com"))
            .build()

        // 2. Получаем экземпляр менеджера
        val workManager = WorkManager.getInstance(context)
        
        // 3. Ставим задачу в очередь
        workManager.enqueue(request).result.get()

        // 4. Драйвер тестов (TestDriver) позволяет управлять временем и условиями
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)

        // Проверяем, что задача сейчас в состоянии ENQUEUED (В очереди)
        var workInfo = workManager.getWorkInfoById(request.id).get()
        assertEquals(WorkInfo.State.ENQUEUED, workInfo.state)

        // 5. МАГИЯ: Говорим системе "Считай, что прошло 15 минут"
        testDriver?.setPeriodDelayMet(request.id)

        // Получаем обновленный статус (так как executor синхронный, она уже выполнилась)
        workInfo = workManager.getWorkInfoById(request.id).get()
        
        // Для периодических задач статус снова станет ENQUEUED (ждет следующего раза),
        // но в логах мы увидим выполнение.
        assertEquals(WorkInfo.State.ENQUEUED, workInfo.state)
    }
    
    @Test
    fun testOneTimeWork_succeeds() {
        // Одноразовая задача
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf(KEY_INPUT_URL to "http://test.com"))
            .build()
            
        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(request).result.get()
        
        // Получаем инфо
        val workInfo = workManager.getWorkInfoById(request.id).get()
        
        // Т.к. мы используем SynchronousExecutor в конфиге, задача выполняется мгновенно
        assertEquals(WorkInfo.State.SUCCEEDED, workInfo.state)
    }
}
```


#### Вопросы для защиты лабораторной
Почему для тестирования WorkManager мы используем WorkManagerTestInitHelper? (Ответ: Чтобы не использовать настоящий системный планировщик задач, который работает асинхронно и медленно).

В чем разница между TestWorkerBuilder и реальным запуском WorkManager? (Ответ: Builder тестирует только класс воркера как обычный объект Java/Kotlin, а WorkManager проверяет запись в БД и соблюдение условий).

Как протестировать задержку (InitialDelay) без реального ожидания? (Ответ: testDriver.setInitialDelayMet(id)).
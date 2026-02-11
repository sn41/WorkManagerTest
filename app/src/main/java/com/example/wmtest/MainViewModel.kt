package com.example.wmtest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.work.*
import java.util.UUID

// Используем AndroidViewModel, чтобы получить доступ к Application Context для WorkManager
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    // Храним ID последней запущенной задачи
    private val _currentWorkId = MutableLiveData<UUID?>(null)

    // Преобразуем ID задачи в LiveData<WorkInfo>, за которой будет следить UI.
    // Если ID нет (null), возвращаем null.
    val workInfo: LiveData<WorkInfo?> = _currentWorkId.map { id ->
        if (id == null) null
        else workManager.getWorkInfoByIdLiveData(id).value
    }

    // Вспомогательная функция, так как .map выше срабатывает не всегда мгновенно для UI
    fun getWorkInfoLiveData(): LiveData<WorkInfo> {
        return workManager.getWorkInfoByIdLiveData(_currentWorkId.value ?: UUID.randomUUID())
    }


    // Запуск отлложенной задачи!!!
    fun startSync(url: String) {
        // 1. Упаковываем данные
        val inputData = workDataOf(KEY_INPUT_URL to url)

        // 2. Создаем запрос (OneTimeWorkRequest)
        // Добавим тег, чтобы можно было найти задачу по имени, если нужно
        val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(inputData)
            .addTag("SYNC_TAG")
            .build()

        // 3. Запускаем
        workManager.enqueue(request)

        // 4. Сохраняем ID, чтобы UI начал следить именно за этой задачей
        _currentWorkId.value = request.id
    }


    // Отмена задачи
    fun cancelWork() {
        _currentWorkId.value?.let { id ->
            workManager.cancelWorkById(id)
        }
    }
}
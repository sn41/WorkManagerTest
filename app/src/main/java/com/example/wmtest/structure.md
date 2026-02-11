Чтобы проект был целостным и архитектурно правильным), мы разобьем его на 3 основных файла:

**Worker** (Фоновая работа).

**ViewModel** (Связь UI и данных, управление состоянием).

**MainActivity** (Отрисовка UI на Compose).

Шаг 1. Зависимости (build.gradle.kts :app)

Убедитесь, что у вас добавлены эти библиотеки. Особенно важна `runtime-livedata`, чтобы Compose мог "слушать" статус задачи.

```Kotlin
dependencies {
    // ... стандартные зависимости Compose ...
    implementation(platform("androidx.compose:compose-bom:2026.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")

    // --- WORK MANAGER ---
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // --- COMPOSE LIVEDATA ---
    // Нужна, чтобы следить за статусом WorkManager (он отдает LiveData)
    implementation("androidx.compose.runtime:runtime-livedata")
}
```


Шаг 2. Фоновая задача (SyncWorker.kt)
Мы добавим сюда искусственную задержку, чтобы успеть увидеть прогресс-бар.


Шаг 3. Логика экрана (MainViewModel.kt)
ViewModel нужна, чтобы запускать WorkManager и хранить ID текущей задачи, за которой мы следим.

Шаг 4. Главный экран (MainActivity.kt)
Здесь мы собираем UI и связываем его с ViewModel.
# TFS Android Spring 2023
# Messenger-TFS
# (c) Иван Синтюрин

Stack: Clean architecture, Kotlin, Single Activity/Fragment, View/Custom View/Custom Layout, ViewPager2, RecyclerView Delegate Adapter, MVVM/MVI/UDF/TEA/Elmslie, Dagger 2, Coroutines/Flow, Retrofit2, JSON, Kotlinx Serialization, Glide, Cicerone, View binding, Figma, Gitlab, UI/unit tests (Kaspresso/JUnit4).

Возможности приложения (часть сделана дополнительно сверх запрошенного функционала):

- Авторизация пользователя (экран для ввода логина и пароля, активная кнопка для разлогина на экране профиля пользователя) с возможностью показа вводимого пароля и открытия в браузере страницы для восстановления пароля

- Возможность создания канала на экране с каналами. Если канал уже существует, то происходит подписка на него

- Лонгклик на названии канала открывает меню с набором действий: подписаться, отписаться, удалить (набор действий зависит от контекста и прав пользователя)

- Автоматическое появление/удаление каналов на экране с каналами, соответственно, при создании канала, подписке на канал или удалении канала/отписки

- При нажатии на канал открывается список со всеми сообщениями канала с указанием топиков

- Автоматически напротив названия каждого топика показывается количество новых сообщений

- При нажатии на стрелку справа от названия канала раскрывается список его топиков

- Можно переключаться в чате между вариантами отображения сообщений всех топиков или сообщений выбранного топика (стрелка вверх рядом с названием топика или клик по названию топика)

- Из общего списка сообщений можно отправить сообщение в любую тему (топик), в т.ч. в новую

- Поле ввода названия топика показывает возможные варианты выбора из уже существующих в канале топиков (автодополнение)

- Сообщения группируются по дате отправки

- Дата сообщений всегда видна наверху экрана

- Время сообщения показывается в его нижнем правом углу

- Есть кнопка перехода к последнему сообщению

- После отправки показывается отправленное сообщение

- Отображение сообщений в HTML-формате (ссылки на внешние ресурсы кликабельные, картинки и смайлики типа :smiley: отображаются в теле сообщения)

- Кнопка "отправить сообщение" (справа от поля для ввода текста сообщений) меняет иконку в зависимости от возможности отправки сообщения (сообщение есть, тема при необходимости ее указания есть) на плюсик или на самолетик

- Можно вкладывать в сообщения изображения или любые файлы, при необходимости запрашиваются разрешения у пользователя

- Клик на кнопку отправки сообщения, когда она в режиме "плюсика", дает возможность добавить вложение к сообщению, лонгклик на "самолетик" позволяет сделать то же самое, можно добавить несколько вложений в одно сообщение

- Лонгклик на сообщении открывает меню действий с сообщением (набор действий зависит от контекста и прав пользователя): добавление реакции, копирование текста в буфер обмена, изменение темы для отправленных сообщений (в т.ч. открывается отображение сообщений указанной темы), изменение текста своих сообщений, удаление сообщений, сохранение вложений в папку загрузки

- Автоматическое обновление списка сообщений при появлении новых сообщений от других пользователей, реакций других пользователей, изменении или удалении сообщений другими пользователями

- Автоматическое обновление онлайн-статуса пользователей на соответствующем экране

- Есть локальный кэш загруженных из интернета сообщений, сообщения загружаются из интернета порциями, в gradle можно указать ограничения по количеству сообщений для БД или загружаемых из интернета, поддерживается пагинация

- Ограничения по размеру (длине) сообщений, топиков и каналов, по времени изменения текста сообщений или топиков, размеру загружаемого на сервер файла берутся с сервера Zulip

- Автоматически проставляется флаг "прочитано" у отображенных на экране сообщений

- При ошибках показывается диалог с соответствующим сообщением

- Встроена поддержка тем (по умолчанию обе темы имеют одинаковые цвета, возможно добавление светлой темы)

- В debug-режиме утечки памяти контролируются при помощи leak canary

- Два языка: русский и английский
## Домашнее задание по лекции "Архитектура"

**1. Нужно реализовать понравившуюся архитектуру: MVP или MVI** (MVVM нельзя использовать!  Но можно сделать MVI на базе ViewModel)

Для MVI можно использовать библиотеки

- RxRedux  https://github.com/freeletics/RxRedux
- TEA https://github.com/vivid-money/elmslie (эта архитектура будет расмотрена на семинаре)

**2. Задание со звездочкой(не обязательно): сделать сохранение состояния**

Репозиторий семинара https://gitlab.com/android-tfs-mentors/tfs-android-spring-2023-architecture-seminar

**На всякий случай напоминаю**:
- Чистота оформления build.gradle-файла (не добавляйте лишние зависимости, удаляйте ненужные)
- Выносите версии зависимостей в ext так, как это показано [тут](https://github.com/JakeWharton/SdkSearch/blob/master/build.gradle)
- Подробнее про вынос зависимостей можно посмотреть [тут](https://habr.com/ru/post/468959/)
- Удалите папки test & androidTest – пока у вас нет тестов, эти папки вам не нужны
- Следите за чистотой кода, старайтесь избегать констант, состоящих из одной буквы и осмысленно называйте переменные
- Для домашки необходимо форкнуть мастер(если не делали этого ранее) и создать ветку hw_7. По завершении необходимо hw_7 направить на master и сделать merge request
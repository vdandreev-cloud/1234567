1. Оптимизация работы с ресурсами (try-with-resources)
Было:
java
var statement = conn.prepareStatement("SELECT * FROM public.excursion WHERE id = ?");
statement.setInt(1, id);
var result = statement.executeQuery();
result.next();
return new Excursion(...);
// Ресурсы не закрывались явно
Стало:
java
try (var statement = conn.prepareStatement(sql)) {
    statement.setInt(1, id);
    try (var result = statement.executeQuery()) {
        if (result.next()) {
            return mapToExcursion(result);
        }
        return null;
    }
}
// Statement и ResultSet закрываются автоматически
Преимущества:

Автоматическое закрытие ресурсов даже при возникновении исключений

Предотвращение утечек памяти и соединений

Упрощение кода (не нужны блоки finally)

2. Проверка состояния соединения в ConnectionManager
Было:
java
@Override
public void close() throws Exception {
    if (connection == null) {
        return;
    }
    connection.close();
}
Стало:
java
@Override
public void close() throws Exception {
    if (connection != null && !connection.isClosed()) {
        connection.close();
    }
}
Преимущества:

Предотвращение повторного закрытия уже закрытого соединения

Избежание SQLException при повторном вызове close()

Безопасная работа в многопоточной среде

3. Закрытие потока свойств в loadProperties()
Было:
java
var propertiesStream = ConnectionManager.class.getResourceAsStream("/db.properties");
var properties = new Properties();
properties.load(propertiesStream);
return properties;
// propertiesStream не закрывался
Стало:
java
try (var propertiesStream = ConnectionManager.class.getResourceAsStream("/db.properties")) {
    var properties = new Properties();
    properties.load(propertiesStream);
    return properties;
}
// propertiesStream закрывается автоматически
Преимущества:

Освобождение системных ресурсов

Предотвращение утечек файловых дескрипторов

4. Вынесение SQL-запросов (подготовка к оптимизации)
Было:
java
var statement = conn.prepareStatement("SELECT * FROM public.excursion WHERE id = ?");
Стало:
java
private static final String SELECT_BY_ID = "SELECT * FROM public.excursion WHERE id = ?";
try (var statement = conn.prepareStatement(SELECT_BY_ID))
Преимущества:

Упрощение поддержки и изменения запросов

Возможность повторного использования констант

Уменьшение дублирования кода

5. Вынесение маппинга ResultSet в отдельный метод
Было:
java
return new Excursion(
    result.getInt("id"),
    result.getString("name"),
    result.getBigDecimal("price"),
    result.getObject("from", LocalDate.class),
    result.getObject("to", LocalDate.class),
    result.getString("guide_name"),
    result.getString("excursion_type"),
    result.getBoolean("lunch_included"));
// Код повторялся в findById и findAll
Стало:
java
private Excursion mapToExcursion(ResultSet rs) throws SQLException {
    return new Excursion(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getBigDecimal("price"),
        rs.getObject("from", LocalDate.class),
        rs.getObject("to", LocalDate.class),
        rs.getString("guide_name"),
        rs.getString("excursion_type"),
        rs.getBoolean("lunch_included"));
}
// Метод используется в обоих запросах
Преимущества:

DRY (Don't Repeat Yourself)

Изменение маппинга в одном месте

Упрощение тестирования

6. Вынесение установки параметров в отдельный метод
Было:
java
statement.setString(1, excursion.getName());
statement.setBigDecimal(2, excursion.getPrice());
statement.setObject(3, excursion.getFrom());
statement.setObject(4, excursion.getTo());
statement.setString(5, excursion.getGuideName());
statement.setString(6, excursion.getExcursionType());
statement.setBoolean(7, excursion.isLunchIncluded());
// Код дублировался в insert и update
Стало:
java
private void setExcursionParams(PreparedStatement statement, Excursion excursion) throws SQLException {
    statement.setString(1, excursion.getName());
    statement.setBigDecimal(2, excursion.getPrice());
    statement.setObject(3, excursion.getFrom());
    statement.setObject(4, excursion.getTo());
    statement.setString(5, excursion.getGuideName());
    statement.setString(6, excursion.getExcursionType());
    statement.setBoolean(7, excursion.isLunchIncluded());
}
Преимущества:

Устранение дублирования кода

Единое место для изменения логики установки параметров

7. Проверка наличия результата в findById
Было:
java
var result = statement.executeQuery();
result.next();
return new Excursion(...);
// Если запись не найдена, будет исключение
Стало:
java
try (var result = statement.executeQuery()) {
    if (result.next()) {
        return mapToExcursion(result);
    }
    return null;
}
// Безопасная обработка отсутствия записи
Преимущества:

Предотвращение NoSuchElementException

Возможность проверки на null после вызова метода

8. Исправление структуры XML-файлов
Проблема: Содержимое файлов было перепутано:

pom.xml содержал конфигурацию IntelliJ IDEA

misc.xml содержал содержимое pom.xml

Исправление:

Восстановлена правильная структура каждого файла

pom.xml теперь содержит Maven конфигурацию

misc.xml содержит настройки проекта IntelliJ


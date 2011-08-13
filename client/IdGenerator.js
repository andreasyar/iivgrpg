
// Генератор уникальный идентификаторов.

function IdGenerator() {}
IdGenerator.id = 1;
IdGenerator.getId = function() {
    return IdGenerator.id++;
}

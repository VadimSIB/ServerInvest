package investment.websocket

//это урезанный ассет, но с ценами для передачи сообщения по сокету на клиента
class Quote(
        var type: String,   // тип - "shares" или "forts" (акции или фьючерсы)
        var secid: String, // биржевый код организации - эмитента акций
        var price: Double, // последняя цена, полученная с биржи
        var previousprice: Double) // предыдущая цена, полученная с биржы

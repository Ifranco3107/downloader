package com.macropay.data.dto
//Objeto para regresar los datos al caso de uso.

//Objeto para regresar los datos al caso de uso.
class ResponseData (
    var isSuccessful:Boolean,
    var message:String,
    var isDatosLocales:Boolean,
    var result: Any
)

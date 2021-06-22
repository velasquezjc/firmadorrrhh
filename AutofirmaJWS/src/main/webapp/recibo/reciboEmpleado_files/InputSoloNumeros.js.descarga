var InputSoloNumeros = function (input) {
    input.keyup(function () {
        var contenido = input.val();
        input.val(contenido.replace(/[^0-9]/g, ""));
        input.change();

    });

        input.change(function () {
            var contenido = input.val();
            input.val(contenido.replace(/[^0-9]/g, ""));
        });



}

jQuery(document).ready(function () {
    $('[soloNumero]').each(function () {
        var input_numerico = new InputSoloNumeros($(this));
    }); 
 });



 
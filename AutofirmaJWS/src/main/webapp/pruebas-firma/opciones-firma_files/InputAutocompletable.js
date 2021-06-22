var InputAutocompletable = function (input, listaElementos, getIdFromElemento, getLabelFromElemento, onElementoSeleccionado) {
    var dataSource = [];
    var elementoSeleccionado;
    for (var i = 0; i < listaElementos.length; i++) {
        var elemento = listaElementos[i];
        dataSource.push({
            value: getIdFromElemento(elemento),
            label: getLabelFromElemento(elemento)
        });
    }
    var getElementoSeleccionadoFromValue = function (value) {
        for (var i = 0; i < listaElementos.length; i++) {
            var elemento = listaElementos[i];
            if (value == getIdFromElemento(elemento)) return elemento;
        }
    }
    input.elementoSeleccionado = function () {
        if (input.val() != '') {
            var itemSeleccionado = input.data().typeahead.$menu.find('.active').data().item;
            return getElementoSeleccionadoFromValue(itemSeleccionado.value);
        }
        return undefined;
    }
    input.attr('data-source', JSON.stringify(dataSource));
    input.attr("autocomplete", "off");
    input.change(function () {
        input.blur();
    });
        input.blur(function () {
        var elementoSeleccionado = input.elementoSeleccionado();
        if (elementoSeleccionado === undefined) {
            input.val('');
        } else {
            input.val(getLabelFromElemento(elementoSeleccionado));
        }
        onElementoSeleccionado(elementoSeleccionado);
    });
    $.extend(true, this, input);
}
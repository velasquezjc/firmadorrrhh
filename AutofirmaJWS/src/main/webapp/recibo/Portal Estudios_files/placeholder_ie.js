jQuery(document).ready(function () {
    setTimeout(function () {
        $('[nullValue]').each(function () {
            HandlePlaceholder($(this));
        });
    }, 100);

    function HandlePlaceholder(input) {

        var label = $("<div>" + input.attr("nullValue") + "</div>");

        label.css('position', 'absolute');
        label.css('top', '0px');
        label.css('left', "0px");
        label.css('z-index', 5);
        label.css('cursor', 'text');
        label.css('margin-right', 'auto');
        label.css('text-align', 'left');
        label.css('color', '#666');
        label.css('background-color', 'transparent');
        label.css('margin-top', '6px');
        label.css('white-space', 'nowrap');
        label.css('pointer-events', 'none');
        label.css('width', '0px');

        var marginStrPx = input.css('margin-left');
        var marginStr = marginStrPx.replace('px', '');
        marginInt = parseInt(marginStr, 10);

        label.css('margin-left', (9 + marginInt).toString() + 'px');

        var contenedor = $('<div>');
        contenedor.css('position', 'relative');
        contenedor.css('display', input.css('display'));
        contenedor.css('width', input.css('width'));

        input.css('width', '97%');

        input.wrap(contenedor);
        input.after(label);

        if (!(input.val() === "")) label.hide();

        input.focus(function () {
            label.hide();
        });

        label.click(function () {
            label.hide();
            input.focus();
        });

        input.blur(function () {
            if (input.val() === "")
                label.show();
            else
                label.hide();
        });
    }
});
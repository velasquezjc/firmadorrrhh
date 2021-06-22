(function (window) {

    function _registerEvent(target, eventType, cb) {
        if (target.addEventListener) {
            target.addEventListener(eventType, cb);
            return {
                remove: function () {
                    target.removeEventListener(eventType, cb);
                }
            };
        } else {
            target.attachEvent(eventType, cb);
            return {
                remove: function () {
                    target.detachEvent(eventType, cb);
                }
            };
        }
    }

    function _createHiddenIframe(target, uri) {
        var iframe = document.createElement("iframe");
        iframe.src = uri;
        iframe.id = "hiddenIframe";
        iframe.style.display = "none";
        target.appendChild(iframe);
        return iframe;
    }

    function openUriWithHiddenFrame(uri, successCb, failCb) {

        var timeout = setTimeout(function () {
            failCb();
            handler.remove();
        }, 1000);

        var iframe = document.querySelector("#hiddenIframe");
        if (!iframe) {
            iframe = _createHiddenIframe(document.body, "about:blank");
        }

        var handler = _registerEvent(window, "blur", onBlur);

        function onBlur() {
            clearTimeout(timeout);
            handler.remove();
            successCb()
        }

        iframe.contentWindow.location.href = uri;
    }

    function openUriWithTimeoutHack(uri, successCb, failCb) {

        var timeout = setTimeout(function () {
            failCb();
            handler.remove();
        }, 1000);//1 segundos

        var handler = _registerEvent(window, "blur", onBlur);

        function onBlur() {
            clearTimeout(timeout);
            handler.remove();
            successCb()
        }
        window.location = uri;

    }

    function openUriUsingFirefox(uri, successCb, failCb) {
        var iframe = document.querySelector("#hiddenIframe");
        if (!iframe) {
            iframe = _createHiddenIframe(document.body, "about:blank");
        }
        try {
            iframe.contentWindow.location.href = uri;
            successCb();
        } catch (e) {
            if (e.name == "NS_ERROR_UNKNOWN_PROTOCOL") {
                failCb();
            }
            else {
                successCb();
            }
        }
    }

    function openUriUsingIEInOlderWindows(uri, failCb) {
        if (getInternetExplorerVersion() === 10) {
            openUriUsingIE10InWindows7(uri, failCb);
        } else if (getInternetExplorerVersion() === 9 || getInternetExplorerVersion() === 11) {
            openUriWithHiddenFrame(uri, failCb);
        } else {
            openUriInNewWindowHack(uri, failCb);
        }
    }

    function openUriUsingIE10InWindows7(uri, successCb, failCb) {
        var timeout = setTimeout(failCb, 1000);
        window.addEventListener("blur", function () {
            clearTimeout(timeout);
        });

        var iframe = document.querySelector("#hiddenIframe");
        if (!iframe) {
            iframe = _createHiddenIframe(document.body, "about:blank");
        }
        try {
            iframe.contentWindow.location.href = uri;
            successCb()
        } catch (e) {
            failCb();
            clearTimeout(timeout);
        }
    }

    function openUriInNewWindowHack(uri, successCb, failCb) {
        var myWindow = window.open('', '', 'width=0,height=0');

        myWindow.document.write("<iframe src='" + uri + "'></iframe>");
        setTimeout(function () {
            try {
                myWindow.location.href;
                myWindow.setTimeout("window.close()", 1000);
                successCb()
            } catch (e) {
                myWindow.close();
                failCb();
            }
        }, 1000);
    }

    function openUriWithMsLaunchUri(uri, successCb, failCb) {
        navigator.msLaunchUri(uri,
            successCb,
            failCb
        );
    }

    function checkBrowser() {
        var isOpera = !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;
        return {
            isOpera: isOpera,
            isFirefox: typeof InstallTrigger !== 'undefined',
            isSafari: Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') > 0,
            isChrome: !!window.chrome && !isOpera,
            isIE: /*@cc_on!@*/false || !!document.documentMode   // At least IE6
        }
    }

    function getInternetExplorerVersion() {
        var rv = -1;
        if (navigator.appName === "Microsoft Internet Explorer") {
            var ua = navigator.userAgent;
            var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
            if (re.exec(ua) != null)
                rv = parseFloat(RegExp.$1);
        }
        else if (navigator.appName === "Netscape") {
            var ua = navigator.userAgent;
            var re = new RegExp("Trident/.*rv:([0-9]{1,}[\.0-9]{0,})");
            if (re.exec(ua) != null) {
                rv = parseFloat(RegExp.$1);
            }
        }
        return rv;
    }
    window.openUriWithTimeoutHack = openUriWithTimeoutHack;
    window.protocolCheck = function (uri, successCb, failCb) {
        function successCallBack() {
            successCb && successCb();
        }
        function failCallback() {
            failCb && failCb();
        }

        if (navigator.msLaunchUri) { //for IE and Edge in Win 8 and Win 10
            openUriWithMsLaunchUri(uri, successCb, failCb);
        } else {
            var browser = checkBrowser();

            if (browser.isFirefox) {
                openUriUsingFirefox(uri, successCallBack, failCallback);
            } else if (browser.isChrome) {
                openUriWithTimeoutHack(uri, successCallBack, failCallback);
            } else if (browser.isIE) {
                openUriUsingIEInOlderWindows(uri, successCallBack, failCallback);
            } else {
                //not supported, implement please
            }
        }
    }
}(window));

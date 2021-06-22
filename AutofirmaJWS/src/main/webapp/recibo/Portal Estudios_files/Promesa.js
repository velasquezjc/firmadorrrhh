var Promesa = function () {
};

Promesa.prototype.then = function (on_success_callback, on_error_callback) {
    if(on_success_callback) this.onSuccess(on_success_callback);
    if(on_error_callback) this.onError(on_error_callback);
};

Promesa.prototype.success = function (data) {
    if (this._on_success_callback) this._on_success_callback(data);
    this._success = true;
    this._data_success = data;
};

Promesa.prototype.onSuccess = function (callback) {
    this._on_success_callback = callback;
    if (this._success) this._on_success_callback(this._data_success);
    return this;
};

Promesa.prototype.error = function (data) {
    if (this._on_error_callback) this._on_error_callback(data);
    this._error = true;
    this._data_error = data;
};

Promesa.prototype.onError = function (callback) {
    this._on_error_callback = callback;
    if (this._error) this._on_success_callback(this._data_error);
    return this;
};


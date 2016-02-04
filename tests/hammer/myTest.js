(function () {
    var callback = function () {};
    var manager = new Hammer.Manager(document.createElement("p"));
    callback(manager);
    new Hammer.Input(manager, callback)


})();
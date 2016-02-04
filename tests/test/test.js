function Klass(name) {
    this.name = name;
}

new Klass("string");

Klass.prototype = {
    doStuff: function () {
        return 2;
    }
};
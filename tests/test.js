var bool = true;

var str = "string";

var num = 123;

function id(a) {
    return a;
}

function minus1(a) {
    return a - 1;
}

function returnFunction(f) {
    return function () {

    };
}

// After this work, test within a function (because of the var transformation).
// TODO: This doesn't work yet, because i don't handle the environment. (And when i do, it will loop infinitely).
var recursive = function () {
    return recursive;
};

function returnFunc(a) {
    return function (b) {
        return a - b;
    }
}

function returnFunc2(a) {
    return function (b) {
        return "test" + (a - b);
    }
}

function callfunc(a) {
    var func = function (b) {
        return 2 - a - b;
    };
    return func();
}

function callfunc2() {
    var func = function (a) {
        return a;
    };
    func(2);
    return func();
}

function returnFromEnv() {
    return num;
}

function returnCallFromEnv() {
    return minus1(1);
}
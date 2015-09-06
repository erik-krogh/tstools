/*var bool = true;

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

var myValue1 = 1;
function returnFromEnv() {
    return myValue1;
}

function returnCallFromEnv() {
    return returnFromEnv();
}

function returnField() {
    var obj = {
        key: 2
    };

    return obj.key;
}

function returnAnUnionField() {
    var obj = {
        key: 2
    };
    obj.key = "string";
    return obj.key;
}

function returnFromEmptyObject() {
    var obj = {};
    return obj.key;
}

function returnFromInitiallyEmptyThenPopulatedObject() {
    var obj = {};
    obj.key = "test";
    return obj.key;
}

function functionsAndObjects() {
    var obj = {
        key: "value"
    };
    return (function () {
        return obj.key;
    })();
}

function functionsAndObjects2() {
    return (function () {
        return {
            key: "value"
        };
    })().key;
}

function objAssignArgument(a) {
    var obj = {
        key: 2
    };
    obj.key = a;
    return obj.key;
}

var returnFromEnv = (function () {
    var objInHeap = {
        key: "value"
    };

    function returnFunc() {
        return objInHeap.key;
    }

    return returnFunc;
})(); */


// TODO: This goes in an infinite loop.
/*var recursive = function () {
    return recursive;
};*/




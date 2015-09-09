/*
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
})();


// TODO: This goes in an infinite loop.
/!*var recursive = function () {
    return recursive;
};*!/

function getConstantFunction(constant) {
    return function () {
        return constant;
    }
}

var returnNumber = getConstantFunction(123);

var returnString = getConstantFunction("string");

var returnBool = getConstantFunction(true);

var returnNull = getConstantFunction(null);

var test = (function () {
    function MyClass() {
        this.stuff = 123;
    }

    MyClass.prototype.getString = function () {
        return "string"
    };

    return function () {
        return new MyClass().getString();
    }
})();
*/
var typeScriptInheritanceTest = (function () {
    var __extends = (this && this.__extends) || function (d, b) {
            for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
            function __() {
                this.constructor = d;
            }

            __.prototype = b.prototype;
            d.prototype = new __();
        };
    var Animal = (function () {
        function Animal(name) {
            this.name = name;
        }

        Animal.prototype.move = function (meters) {

        };
        Animal.prototype.getConstant = function () {
            return 123;
        };
        return Animal;
    })();
    var Snake = (function (_super) {
        __extends(Snake, _super);
        function Snake(name) {

        }

        Snake.prototype.move = function () {

        };
        Snake.prototype.getConstant = function () {
            return "string";
        };
        return Snake;
    })(Animal);
    var Horse = (function (_super) {
        __extends(Horse, _super);
        function Horse(name) {

        }

        Horse.prototype.move = function () {

        };
        return Horse;
    })(Animal);

    var snake = new Snake("Sammy the Python");
    function expectString() {
        return snake.getConstant();
    }

    var horse = new Horse("Tommy the Palomino");
    function expectNumber() {
        return horse.getConstant();
    }

    return {
        expectString: expectString,
        expectNumber: expectNumber
    }
})();




/* Missing:
 - Loops (for/while)
 - For in.
 - Arrays
 - Indexers (String and Number).
 - instanceof
 - typeof

  */
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

 function random() {
 return Math.random();
 }

function returnNativeNumber() {
    return [1].length;
}

function returnArray() {
    return [1];
}

function returnArraySlice() {
    return [1].slice();
}

function regExp() {
    return /ab+c/;
} */


// Testing doing functions in one lump, or separably.
/*function cross1(a) {
    return a - 1;
}

function cross2() {
    return cross1("invalid input string");
}

function id(a) {
    return a;
}

function number() {
    return id(123);
}*/

// This should return string.
/* uniqueId = function (prefix) {
    var id = ++idCounter + '';
    return prefix ? prefix + id : id;
}; */

/* var _ = {};

function isArrayLike() {
    return true;
}

_.each = function(obj, iteratee, context) {
    var i, length;
    if (isArrayLike(obj)) {
        for (i = 0, length = obj.length; i < length; i++) {
            iteratee(obj[i], i, obj);
        }
    } else {
        var keys = Object.keys(obj);
        for (i = 0, length = keys.length; i < length; i++) {
            iteratee(obj[keys[i]], keys[i], obj);
        }
    }
    return obj;
};

_.each(['Arguments', 'Function', 'String', 'Number', 'Date', 'RegExp', 'Error'], function(name) {
    _['is' + name] = function(obj) {
        return toString.call(obj) === '[object ' + name + ']';
    };
}); */

/* (function() {
    window.range = function(start, stop, step) {
        if (stop == null) {
            stop = start || 0;
            start = 0;
        }
        step = step || 1;

        var length = Math.max(Math.ceil((stop - start) / step), 0);
        var range = Array(length);

        for (var idx = 0; idx < length; idx++, start += step) {
            range[idx] = start;
        }

        return range;
    };
})();


(function () {
    var escapeMap = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#x27;',
        '`': '&#x60;'
    };

    var createEscaper = function(map) {
        var escaper = function(match) {
            return map[match];
        };
        // Regexes for identifying a key that needs to be escaped
        var source = '(?:' + Object.keys(map).join('|') + ')';
        var testRegexp = RegExp(source);
        var replaceRegexp = RegExp(source, 'g');
        return function(string) {
            string = string == null ? '' : '' + string;
            string.somePropertyThatIsntThere = true;
            return testRegexp.test(string) ? string.replace(replaceRegexp, escaper) : string;
        };
    };

    window.escape = createEscaper(escapeMap);
})();*/

// Just testing that it does not crash, not supposed to give anything meaningful.
/*try {
    throw new Error("error");
} catch(e) {
    window.test = e;
}*/


/* function primitiveToString() {
    return (1).toString() || (true).toString() || ("string").toString();
} */


/* var omit = function(obj, iteratee, context) {
    if (true) {
        iteratee = negate(iteratee);
    } else {
        iteratee = function(value, key) {
            return key === true;
        };
    }
    return "constant";
};*/

/*var test = (function () {
    function MyClass() {

    }

    MyClass.prototype.doStuff = function (a) {
        return a - 2;
    };

    return function () {
        return MyClass;
    };
})();*/

/*
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
}*/

/*var test = (function () {
    var MyUnderscore = function(obj) {
        this._wrapped = obj;
    };

// Return a random integer between min and max (inclusive).
    MyUnderscore.prototype.random = function(min, max) {
        if (max == null) {
            max = min;
            min = 0;
        }
        return min + Math.floor(Math.random() * (max - min + 1));
    };

    return MyUnderscore;
})();*/

/*(function (root) {
    function MyClass() {
        this.something = "test";
    }

    root.MyClass = MyClass;

    MyClass.prototype.doStuff = function (a) {
        return a - 2;
    };

    MyClass.random = function(min, max) {
        if (max == null) {
            max = min;
            min = 0;
        }
        return min + Math.floor(Math.random() * (max - min + 1));
    };

    return MyClass;
})(window);*/

/*var property = function(key) {
    return function(obj) {
        return obj == null ? void 0 : obj[key];
    };
};

var pluck = function(obj, key) {
    return map(obj, property(key));
};

var map = function () {
    return [];
};

function _() {

}
_.prototype.stuff = function () {
    return "strings";
};


// Add some isType methods: isArguments, isFunction, isString, isNumber, isDate, isRegExp, isError.
each(['Arguments', 'Function', 'String', 'Number', 'Date', 'RegExp', 'Error'], function(name) {
    _['is' + name] = function(obj) {
        return Object.prototype.toString.call(obj) === '[object ' + name + ']';
    };
});

function each(arr, callback) {
    for (var i = 0; i < arr.length; i++) {
        callback(arr[i]);
    }
}*/

/*var after = function(times, func) {
    func.apply(this, arguments);
    return "something";
};*/

/*
var func = (function () {
    var test = {};

    Object.defineProperties(test, {
        blur: {
            get: function () {
                return 2;
            },
            set: function (value) {
                value == "test";
            }
        }
    });

    return function () {
        return test;
    };
})();*/

/*var func = (function () {
    var test = {};

    var blab = 2;

    Object.defineProperties(test, {
        blur: {
            get: function () {
                test && 3;
                return blab;

            },
            set: function (value) {

            }
        }
    });

    return function () {
        return test;
    };
})();*/

/*
var MyModule = {
    str: "string",
    num: 123,
    Klass: function () {
        this.stuff = 123;
    },
    subModule : {
        subStr : "string"
    }
};
MyModule.Klass.prototype.doStuff = function () {
    return true;
};*/

var MyModule = {
    testFunc: (function (a) {
        return a;
    }).bind(MyModule, 5)
};
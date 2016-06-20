var app = angular.module('materializeApp', ['ui.materialize'])
    .controller('BodyController', ["$scope", "$http", function ($scope, $http) {
        $http({
            method: 'GET',
            url: 'diff.json'
        }).then(function (response) {
            Materialize.toast("Loaded diff", 1500);
            $scope.diff = response.data;
        }, function (response) {
            Materialize.toast("Could not load the diff");
        });

        $scope.diff = null;

        $scope.show = {
            new: false,
            old: false
        }

    }]).controller('StatementController', ["$scope", "$http", function ($scope, $http) {

        $scope.show = {
            all: false
        };

        var statementTypes = {
            changedType: {
                getTitle: function (statement) {
                    return "Type changed on " + statement.typePath;
                },
                getCodeSamples: function (statement) {
                    return [
                        {
                            description: "Type changed from: ",
                            code: statement.oldType
                        }, {
                            description: "To: ",
                            code: statement.newType
                        },
                        {
                            description: "In container: ",
                            code: statement.containerType
                        }
                    ]
                }
            },
            addedProperty: {
                getTitle: function (statement) {
                    if (statement.isClass) {
                        return "Static property \"" + statement.key + "\" added to " + statement.containerDescription + " on " + statement.typePath;
                    } else {
                        return "Property \"" + statement.key + "\" added to " + statement.containerDescription + " on " + statement.typePath;
                    }
                },
                getCodeSamples: function (statement) {
                    var result = [];
                    if (statement.isAny) {
                        result.push({
                            description: "A type could not be inferred"
                        })
                    } else {
                        result.push({
                            description: "Type: ",
                            code: statement.newType
                        })
                    }
                    result.push({
                        description: "In container: ",
                        code: statement.containerType
                    });
                    return result;
                }
            },
            removedProperty: {
                getTitle: function (statement) {
                    if (statement.isClass) {
                        return "Static property \"" + statement.key + "\" removed from " + statement.containerDescription + " on " + statement.typePath;
                    } else {
                        return "Property \"" + statement.key + "\" removed from " + statement.containerDescription + " on " + statement.typePath;
                    }
                },
                getCodeSamples: function (statement) {
                    return [{
                        description: "New container type: ",
                        code: statement.containerType
                    }]
                }
            },
            changedArgCount: {
                getTitle: function (statement) {
                    if (statement.isClass) {
                        return "Number of arguments changed from " + statement.oldArgCount + " to " + statement.newArgCount + " in constructor for " + statement.newTypeDescription + " on " + statement.typePath;
                    } else {
                        return "Number of arguments changed from " + statement.oldArgCount + " to " + statement.newArgCount + " on " + statement.typePath;
                    }
                },
                getCodeSamples: function (statement) {
                    return [
                        {
                            description: "Old function: ",
                            code: statement.oldType
                        }, {
                            description: "New function: ",
                            code: statement.newType
                        }]
                }
            }
        };

        $scope.getStatementTitle = function (statement) {
            return statementTypes[statement.type].getTitle(statement);
        };

        $scope.getCodeSamples = function (statement) {
            return statementTypes[statement.type].getCodeSamples(statement);
        };

    }]);


// Thanks to: http://stackoverflow.com/questions/22742899/using-prismjs-in-angular-app#answer-24697575
app.directive('nagPrism', ['$compile', function ($compile) {
    return {
        restrict: 'A',
        transclude: true,
        scope: {
            source: '@'
        },
        link: function (scope, element, attrs, controller, transclude) {
            scope.$watch('source', function (v) {
                element.find("code").html(v);

                Prism.highlightElement(element.find("code")[0]);
            });

            transclude(function (clone) {
                if (clone.html() !== undefined) {
                    element.find("code").html(clone.html());
                    $compile(element.contents())(scope.$parent);
                }
            });
        },
        template: "<code></code>"
    };
}]);
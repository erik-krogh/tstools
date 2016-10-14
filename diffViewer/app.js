String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

function getJSONPath() {
    // google - stackoverflow - copy - paste: http://stackoverflow.com/questions/12049620/how-to-get-get-variables-value-in-javascript#answer-12049737
    var $_GET = Object.create(null);
    if(document.location.toString().indexOf('?') !== -1) {
        var query = document.location
            .toString()
            // get the query string
            .replace(/^.*?\?/, '')
            // and remove any existing hash string (thanks, @vrijdenker)
            .replace(/#.*$/, '')
            .split('&');

        for(var i=0, l=query.length; i<l; i++) {
            var aux = decodeURIComponent(query[i]).split('=');
            $_GET[aux[0]] = aux[1];
        }
    }

    if ($_GET["json"] && $_GET["json"].endsWith(".json")) {
        return $_GET["json"];
    }

    return 'diff.json';
}
var app = angular.module('materializeApp', ['ui.materialize'])
    .controller('BodyController', ["$scope", "$http", function ($scope, $http) {
        var jsonPath = getJSONPath();
        $http({
            method: 'GET',
            url: jsonPath
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
        };

        $scope.filterOn = {
            old: true,
            new: false
        };

        $scope.getFilteredStatements = function () {
            var result = $scope.diff.statements.slice();

            if ($scope.filterOn.old) {
                result = result.filter(function (stmt) {
                    if (stmt.type == "removedProperty" || stmt.type == "addedProperty") {
                        if (!stmt.isInOldDecContainer) {
                            return false;
                        }
                        if (stmt.type == "removedProperty" && !stmt.isInOldDec) {
                            return false;
                        }
                    } else {
                        if (!stmt.isInOldDec) {
                            return false;
                        }
                    }
                    return true;
                });
            }

            if ($scope.filterOn.new) {
                result = result.filter(function (stmt) {
                    if (stmt.type == "removedProperty") {
                        if (!stmt.isInNewDec && stmt.isInNewDecContainer) {
                            return false;
                        }
                    }
                    if (stmt.type == "addedProperty" && stmt.isInNewDec) {
                        return false;
                    }

                    return true;
                });
            }
            console.log("Removed: " + ($scope.diff.statements.length - result.length));
            return result;
        }

    }]).controller('StatementController', ["$scope", "$http", function ($scope, $http) {

        $scope.show = {
            all: false,
            anything: true
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
                        }/*,
                        {
                            description: "In container: ",
                            code: statement.containerType
                        }*/
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
                    if (statement.isInOldDec) {
                        result.push({
                            description: "Was already in the old declaration"
                        });
                    } else if (!statement.isInOldDecContainer) {
                        result.push({
                            description: "The container couldn't be found in the old declaration"
                        });
                    }

                    if ($scope.diff.newDecAvailable) {
                        if (statement.isInNewDec) {
                            result.push({
                                description: "Has been added in the new declaration"
                            });
                        } else {
                            result.push({
                                description: "Was not found in the new declaration"
                            });
                            if (!statement.isInNewDecContainer) {
                                result.push({
                                    description: "But neither was the container"
                                })
                            }
                        }
                    }

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
                    /*result.push({
                        description: "In container: ",
                        code: statement.containerType
                    });*/
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
                    var result = [];
                    if (!statement.isInOldDec) {
                        result.push({
                            description: "Was already missing in the old declaration"
                        });
                        if (!statement.isInOldDecContainer) {
                            result.push({
                                description: "But the container was also missing in the old declaration"
                            });
                        }
                    }
                    if ($scope.diff.newDecAvailable) {
                        if (statement.isInNewDec) {
                            result.push({
                                description: "Is still implemented in the new declaration"
                            })
                        } else {
                            result.push({
                                description: "Has been removed from the new declaration"
                            });
                            if (!statement.isInNewDecContainer) {
                                result.push({
                                    description: "Or the container was simply missing, couldn't find it in the new declaration  "
                                })
                            }
                        }
                    }

                    /*result.push({
                        description: "New container type: ",
                        code: statement.containerType
                    });*/
                    return result;
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
                        }/*, {
                            description: "New container type: ",
                            code: statement.containerType
                        }*/]
                }
            }
        };

        $scope.getStatementTitle = function (statement) {
            return statementTypes[statement.type].getTitle(statement);
        };

        $scope.getCodeSamples = function (statement) {
            var samples = statementTypes[statement.type].getCodeSamples(statement);

            function addIfPresent(string, description) {
                if (string) {
                    samples.push({
                        description: description,
                        code: string
                    })
                }
            }

            addIfPresent(statement.newJSDoc, "JSDoc for new function: " + statement.newInclosingFunctionPath);
            addIfPresent(statement.oldJSDoc, "JSDoc for old function: " + statement.oldInclosingFunctionPath);
            addIfPresent(statement.newFunction, "New function source: " + statement.newInclosingFunctionPath);
            addIfPresent(statement.oldFunction, "Old function source: " + statement.oldInclosingFunctionPath);

            return samples;
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
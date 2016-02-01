(function () {

	var supportjQuery = jQuery;

// Store the old counts so that we only assert on tests that have actually leaked,
// instead of asserting every time a test has leaked sometime in the past
	var oldCacheLength = 0,
		oldActive = 0,

		expectedDataKeys = {},
		splice = [].splice,
		ajaxSettings = jQuery.ajaxSettings;

	/**
	 * QUnit configuration
	 */

// Max time for stop() and asyncTest() until it aborts test
// and start()'s the next test.
	QUnit.config.testTimeout = 12e4; // 2 minutes

// Enforce an "expect" argument or expect() call in all test bodies.
//	QUnit.config.requireExpects = true;

	/**
	 * @param {jQuery|HTMLElement|Object|Array} elems Target (or array of targets) for jQuery.data.
	 * @param {string} key
	 */
	QUnit.assert.expectJqData = function (env, elems, key) {
		var i, elem, expando;

		// As of jQuery 2.0, there will be no "cache"-data is
		// stored and managed completely below the API surface
		if (jQuery.cache) {
			env.checkJqData = true;

			if (elems.jquery && elems.toArray) {
				elems = elems.toArray();
			}
			if (!supportjQuery.isArray(elems)) {
				elems = [elems];
			}

			for (i = 0; i < elems.length; i++) {
				elem = elems[i];

				// jQuery.data only stores data for nodes in jQuery.cache,
				// for other data targets the data is stored in the object itself,
				// in that case we can't test that target for memory leaks.
				// But we don't have to since in that case the data will/must will
				// be available as long as the object is not garbage collected by
				// the js engine, and when it is, the data will be removed with it.
				if (!elem.nodeType) {

					// Fixes false positives for dataTests(window), dataTests({}).
					continue;
				}

				expando = elem[jQuery.expando];

				if (expando === undefined) {

					// In this case the element exists fine, but
					// jQuery.data (or internal data) was never (in)directly
					// called.
					// Since this method was called it means some data was
					// expected to be found, but since there is nothing, fail early
					// (instead of in teardown).
					this.notStrictEqual(
						expando,
						undefined,
						"Target for expectJqData must have an expando, " +
						"for else there can be no data to expect."
					);
				} else {
					if (expectedDataKeys[expando]) {
						expectedDataKeys[expando].push(key);
					} else {
						expectedDataKeys[expando] = [key];
					}
				}
			}
		}

	};
	QUnit.config.urlConfig.push({
		id: "jqdata",
		label: "Always check jQuery.data",
		tooltip: "Trigger QUnit.expectJqData detection for all tests " +
		"instead of just the ones that call it"
	});

	/**
	 * Ensures that tests have cleaned up properly after themselves. Should be passed as the
	 * teardown function on all modules' lifecycle object.
	 */
	var moduleTeardown = function (assert) {
		var i, expectedKeys, actualKeys,
			cacheLength = 0;

		// Only look for jQuery data problems if this test actually
		// provided some information to compare against.
		if (QUnit.urlParams.jqdata || this.checkJqData) {
			for (i in jQuery.cache) {
				expectedKeys = expectedDataKeys[i];
				actualKeys = jQuery.cache[i] ? Object.keys(jQuery.cache[i]) : jQuery.cache[i];
				if (!QUnit.equiv(expectedKeys, actualKeys)) {
					assert.deepEqual(actualKeys, expectedKeys, "Expected keys exist in jQuery.cache");
				}
				delete jQuery.cache[i];
				delete expectedDataKeys[i];
			}

			// In case it was removed from cache before (or never there in the first place)
			for (i in expectedDataKeys) {
				assert.deepEqual(
					expectedDataKeys[i],
					undefined,
					"No unexpected keys were left in jQuery.cache (#" + i + ")"
				);
				delete expectedDataKeys[i];
			}
		}

		// Reset data register
		expectedDataKeys = {};

		// Check for (and clean up, if possible) incomplete animations/requests/etc.
		if (jQuery.timers && jQuery.timers.length !== 0) {
			assert.equal(jQuery.timers.length, 0, "No timers are still running");
			splice.call(jQuery.timers, 0, jQuery.timers.length);
			jQuery.fx.stop();
		}
		if (jQuery.active !== undefined && jQuery.active !== oldActive) {
			assert.equal(jQuery.active, oldActive, "No AJAX requests are still active");
			if (ajaxTest.abort) {
				ajaxTest.abort("active requests");
			}
			oldActive = jQuery.active;
		}

		Globals.cleanup();

		for (i in jQuery.cache) {
			++cacheLength;
		}

		// Because QUnit doesn't have a mechanism for retrieving
		// the number of expected assertions for a test,
		// if we unconditionally assert any of these,
		// the test will fail with too many assertions :|
		if (cacheLength !== oldCacheLength) {
			assert.equal(cacheLength, oldCacheLength, "No unit tests leak memory in jQuery.cache");
			oldCacheLength = cacheLength;
		}
	};

	QUnit.done(function () {

		// Remove our own fixtures outside #qunit-fixture
		supportjQuery("#qunit ~ *").remove();
	});

	QUnit.testDone(function () {

		// Ensure jQuery events and data on the fixture are properly removed
		jQuery("#qunit-fixture").empty();

		// ...even if the jQuery under test has a broken .empty()
		supportjQuery("#qunit-fixture").empty();

		// Reset internal jQuery state
		jQuery.event.global = {};
		if (ajaxSettings) {
			jQuery.ajaxSettings = jQuery.extend(true, {}, ajaxSettings);
		} else {
			delete jQuery.ajaxSettings;
		}

		// Cleanup globals
		Globals.cleanup();
	});

// Register globals for cleanup and the cleanup code itself
	var Globals = (function () {
		var globals = {};

		return {
			register: function (name) {
				window[name] = globals[name] = true;
			},

			cleanup: function () {
				var name;

				for (name in globals) {
					delete window[name];
				}

				globals = {};
			}
		};
	})();

	QUnit.module("callbacks", {
		teardown: moduleTeardown
	});

	(function () {

		var output,
			addToOutput = function (string) {
				return function () {
					output += string;
				};
			},
			outputA = addToOutput("A"),
			outputB = addToOutput("B"),
			outputC = addToOutput("C"),
			tests = {
				"": "XABC   X     XABCABCC  X  XBB X   XABA  X   XX",
				"once": "XABC   X     X         X  X   X   XABA  X   XX",
				"memory": "XABC   XABC  XABCABCCC XA XBB XB  XABA  XC  XX",
				"unique": "XABC   X     XABCA     X  XBB X   XAB   X   X",
				"stopOnFalse": "XABC   X     XABCABCC  X  XBB X   XA    X   XX",
				"once memory": "XABC   XABC  X         XA X   XA  XABA  XC  XX",
				"once unique": "XABC   X     X         X  X   X   XAB   X   X",
				"once stopOnFalse": "XABC   X     X         X  X   X   XA    X   XX",
				"memory unique": "XABC   XA    XABCA     XA XBB XB  XAB   XC  X",
				"memory stopOnFalse": "XABC   XABC  XABCABCCC XA XBB XB  XA    X   XX",
				"unique stopOnFalse": "XABC   X     XABCA     X  XBB X   XA    X   X"
			},
			filters = {
				"no filter": undefined,
				"filter": function (fn) {
					return function () {
						return fn.apply(this, arguments);
					};
				}
			};

		function showFlags(flags) {
			if (typeof flags === "string") {
				return "'" + flags + "'";
			}
			var output = [], key;
			for (key in flags) {
				output.push("'" + key + "': " + flags[key]);
			}
			return "{ " + output.join(", ") + " }";
		}

		jQuery.each(tests, function (strFlags, resultString) {

			var objectFlags = {};

			jQuery.each(strFlags.split(" "), function () {
				if (this.length) {
					objectFlags[this] = true;
				}
			});

			jQuery.each(filters, function (filterLabel) {

				jQuery.each({
					"string": strFlags,
					"object": objectFlags
				}, function (flagsTypes, flags) {

					QUnit.test("jQuery.Callbacks( " + showFlags(flags) + " ) - " + filterLabel, function (assert) {

						assert.expect(29);

						var cblist,
							results = resultString.split(/\s+/);

						// Basic binding and firing
						output = "X";
						cblist = jQuery.Callbacks(flags);
						assert.strictEqual(cblist.locked(), false, ".locked() initially false");
						assert.strictEqual(cblist.disabled(), false, ".disabled() initially false");
						assert.strictEqual(cblist.fired(), false, ".fired() initially false");
						cblist.add(function (str) {
							output += str;
						});
						assert.strictEqual(cblist.fired(), false, ".fired() still false after .add");
						cblist.fire("A");
						assert.strictEqual(output, "XA", "Basic binding and firing");
						assert.strictEqual(cblist.fired(), true, ".fired() detects firing");
						output = "X";
						cblist.disable();
						cblist.add(function (str) {
							output += str;
						});
						assert.strictEqual(output, "X", "Adding a callback after disabling");
						cblist.fire("A");
						assert.strictEqual(output, "X", "Firing after disabling");
						assert.strictEqual(cblist.disabled(), true, ".disabled() becomes true");
						assert.strictEqual(cblist.locked(), true, "disabling locks");

						// Emptying while firing (#13517)
						cblist = jQuery.Callbacks(flags);
						cblist.add(cblist.empty);
						cblist.add(function () {
							assert.ok(false, "not emptied");
						});
						cblist.fire();

						// Disabling while firing
						cblist = jQuery.Callbacks(flags);
						cblist.add(cblist.disable);
						cblist.add(function () {
							assert.ok(false, "not disabled");
						});
						cblist.fire();

						// Basic binding and firing (context, arguments)
						output = "X";
						cblist = jQuery.Callbacks(flags);
						cblist.add(function () {
							assert.equal(this, window, "Basic binding and firing (context)");
							output += Array.prototype.join.call(arguments, "");
						});
						cblist.fireWith(window, ["A", "B"]);
						assert.strictEqual(output, "XAB", "Basic binding and firing (arguments)");

						// fireWith with no arguments
						output = "";
						cblist = jQuery.Callbacks(flags);
						cblist.add(function () {
							assert.equal(this, window, "fireWith with no arguments (context is window)");
							assert.strictEqual(arguments.length, 0, "fireWith with no arguments (no arguments)");
						});
						cblist.fireWith();

						// Basic binding, removing and firing
						output = "X";
						cblist = jQuery.Callbacks(flags);
						cblist.add(outputA, outputB, outputC);
						cblist.remove(outputB, outputC);
						cblist.fire();
						assert.strictEqual(output, "XA", "Basic binding, removing and firing");

						// Empty
						output = "X";
						cblist = jQuery.Callbacks(flags);
						cblist.add(outputA);
						cblist.add(outputB);
						cblist.add(outputC);
						cblist.empty();
						cblist.fire();
						assert.strictEqual(output, "X", "Empty");

						// Locking
						output = "X";
						cblist = jQuery.Callbacks(flags);
						cblist.add(function (str) {
							output += str;
						});
						cblist.lock();
						cblist.add(function (str) {
							output += str;
						});
						cblist.fire("A");
						cblist.add(function (str) {
							output += str;
						});
						assert.strictEqual(output, "X", "Lock early");
						assert.strictEqual(cblist.locked(), true, "Locking reflected in accessor");

						// Locking while firing (gh-1990)
						output = "X";
						cblist = jQuery.Callbacks(flags);
						cblist.add(cblist.lock);
						cblist.add(function (str) {
							output += str;
						});
						cblist.fire("A");
						assert.strictEqual(output, "XA", "Locking doesn't abort execution (gh-1990)");

						// Ordering
						output = "X";
						cblist = jQuery.Callbacks(flags);
						cblist.add(function () {
							cblist.add(outputC);
							outputA();
						}, outputB);
						cblist.fire();
						assert.strictEqual(output, results.shift(), "Proper ordering");

						// Add and fire again
						output = "X";
						cblist.add(function () {
							cblist.add(outputC);
							outputA();
						}, outputB);
						assert.strictEqual(output, results.shift(), "Add after fire");

						output = "X";
						cblist.fire();
						assert.strictEqual(output, results.shift(), "Fire again");

						// Multiple fire
						output = "X";
						cblist = jQuery.Callbacks(flags);
						cblist.add(function (str) {
							output += str;
						});
						cblist.fire("A");
						assert.strictEqual(output, "XA", "Multiple fire (first fire)");
						output = "X";
						cblist.add(function (str) {
							output += str;
						});
						assert.strictEqual(output, results.shift(), "Multiple fire (first new callback)");
						output = "X";
						cblist.fire("B");
						assert.strictEqual(output, results.shift(), "Multiple fire (second fire)");
						output = "X";
						cblist.add(function (str) {
							output += str;
						});
						assert.strictEqual(output, results.shift(), "Multiple fire (second new callback)");

						// Return false
						output = "X";
						cblist = jQuery.Callbacks(flags);
						cblist.add(outputA, function () {
							return false;
						}, outputB);
						cblist.add(outputA);
						cblist.fire();
						assert.strictEqual(output, results.shift(), "Callback returning false");

						// Add another callback (to control lists with memory do not fire anymore)
						output = "X";
						cblist.add(outputC);
						assert.strictEqual(output, results.shift(), "Adding a callback after one returned false");

						// Callbacks are not iterated
						output = "";
						function handler() {
							output += "X";
						}

						handler.method = function () {
							output += "!";
						};
						cblist = jQuery.Callbacks(flags);
						cblist.add(handler);
						cblist.add(handler);
						cblist.fire();
						assert.strictEqual(output, results.shift(), "No callback iteration");
					});
				});
			});
		});

	})();

	QUnit.test("jQuery.Callbacks( options ) - options are copied", function (assert) {

		assert.expect(1);

		var options = {
				"unique": true
			},
			cb = jQuery.Callbacks(options),
			count = 0,
			fn = function () {
				assert.ok(!( count++ ), "called once");
			};
		options["unique"] = false;
		cb.add(fn, fn);
		cb.fire();
	});

	QUnit.test("jQuery.Callbacks.fireWith - arguments are copied", function (assert) {

		assert.expect(1);

		var cb = jQuery.Callbacks("memory"),
			args = ["hello"];

		cb.fireWith(null, args);
		args[0] = "world";

		cb.add(function (hello) {
			assert.strictEqual(hello, "hello", "arguments are copied internally");
		});
	});

	QUnit.test("jQuery.Callbacks.remove - should remove all instances", function (assert) {

		assert.expect(1);

		var cb = jQuery.Callbacks();

		function fn() {
			assert.ok(false, "function wasn't removed");
		}

		cb.add(fn, fn, function () {
			assert.ok(true, "end of test");
		}).remove(fn).fire();
	});

	QUnit.test("jQuery.Callbacks.has", function (assert) {

		assert.expect(13);

		var cb = jQuery.Callbacks();

		function getA() {
			return "A";
		}

		function getB() {
			return "B";
		}

		function getC() {
			return "C";
		}

		cb.add(getA, getB, getC);
		assert.strictEqual(cb.has(), true, "No arguments to .has() returns whether callback function(s) are attached or not");
		assert.strictEqual(cb.has(getA), true, "Check if a specific callback function is in the Callbacks list");

		cb.remove(getB);
		assert.strictEqual(cb.has(getB), false, "Remove a specific callback function and make sure its no longer there");
		assert.strictEqual(cb.has(getA), true, "Remove a specific callback function and make sure other callback function is still there");

		cb.empty();
		assert.strictEqual(cb.has(), false, "Empty list and make sure there are no callback function(s)");
		assert.strictEqual(cb.has(getA), false, "Check for a specific function in an empty() list");

		cb.add(getA, getB, function () {
			assert.strictEqual(cb.has(), true, "Check if list has callback function(s) from within a callback function");
			assert.strictEqual(cb.has(getA), true, "Check if list has a specific callback from within a callback function");
		}).fire();

		assert.strictEqual(cb.has(), true, "Callbacks list has callback function(s) after firing");

		cb.disable();
		assert.strictEqual(cb.has(), false, "disabled() list has no callback functions (returns false)");
		assert.strictEqual(cb.has(getA), false, "Check for a specific function in a disabled() list");

		cb = jQuery.Callbacks("unique");
		cb.add(getA);
		cb.add(getA);
		assert.strictEqual(cb.has(), true, "Check if unique list has callback function(s) attached");
		cb.lock();
		assert.strictEqual(cb.has(), false, "locked() list is empty and returns false");
	});

	QUnit.test("jQuery.Callbacks() - adding a string doesn't cause a stack overflow", function (assert) {

		assert.expect(1);

		jQuery.Callbacks().add("hello world");

		assert.ok(true, "no stack overflow");
	});

	QUnit.test("jQuery.Callbacks() - disabled callback doesn't fire (gh-1790)", function (assert) {

		assert.expect(1);

		var cb = jQuery.Callbacks(),
			fired = false,
			shot = function () {
				fired = true;
			};

		cb.disable();
		cb.empty();
		cb.add(shot);
		cb.fire();
		assert.ok(!fired, "Disabled callback function didn't fire");
	});
})();
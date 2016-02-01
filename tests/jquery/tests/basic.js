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
	QUnit.config.requireExpects = true;

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

	QUnit.module("basic", {teardown: moduleTeardown});


	QUnit.test("attributes", function (assert) {
		assert.expect(6);

		var a = jQuery("<a/>").appendTo("#qunit-fixture"),
			input = jQuery("<input/>").appendTo("#qunit-fixture");

		assert.strictEqual(a.attr("foo", "bar").attr("foo"), "bar", ".attr getter/setter");
		assert.strictEqual(a.removeAttr("foo").attr("foo"), undefined, ".removeAttr");
		assert.strictEqual(a.prop("href", "#5").prop("href"),
			location.href.replace(/\#.*$/, "") + "#5",
			".prop getter/setter");

		a.addClass("abc def ghj").removeClass("def ghj");
		assert.strictEqual(a.hasClass("abc"), true, ".(add|remove|has)Class, class present");
		assert.strictEqual(a.hasClass("def"), false, ".(add|remove|has)Class, class missing");

		assert.strictEqual(input.val("xyz").val(), "xyz", ".val getter/setter");
	});

	if (jQuery.css) {
		QUnit.test("css", function (assert) {
			assert.expect(1);

			var div = jQuery("<div/>").appendTo("#qunit-fixture");

			assert.strictEqual(div.css("width", "50px").css("width"), "50px", ".css getter/setter");
		});
	}

	if (jQuery.fn.show && jQuery.fn.hide) {
		QUnit.test("show/hide", function (assert) {
			assert.expect(2);

			var div = jQuery("<div/>").appendTo("#qunit-fixture");

			div.hide();
			assert.strictEqual(div.css("display"), "none", "div hidden");
			div.show();
			assert.strictEqual(div.css("display"), "block", "div shown");
		});
	}

	QUnit.test("core", function (assert) {
		assert.expect(28);

		var elem = jQuery("<div></div><span></span>");

		assert.strictEqual(elem.length, 2, "Correct number of elements");
		assert.strictEqual(jQuery.trim("  hello   "), "hello", "jQuery.trim");

		assert.strictEqual(jQuery.type(null), "null", "jQuery.type(null)");
		assert.strictEqual(jQuery.type(undefined), "undefined", "jQuery.type(undefined)");
		assert.strictEqual(jQuery.type("a"), "string", "jQuery.type(String)");

		assert.ok(jQuery.isPlainObject({"a": 2}), "jQuery.isPlainObject(object)");
		assert.ok(!jQuery.isPlainObject("foo"), "jQuery.isPlainObject(String)");

		assert.ok(jQuery.isFunction(jQuery.noop), "jQuery.isFunction(jQuery.noop)");
		assert.ok(!jQuery.isFunction(2), "jQuery.isFunction(Number)");

		assert.ok(jQuery.isNumeric("-2"), "jQuery.isNumeric(String representing a number)");
		assert.ok(!jQuery.isNumeric(""), "jQuery.isNumeric(\"\")");

		assert.ok(jQuery.isXMLDoc(jQuery.parseXML(
			"<?xml version='1.0' encoding='UTF-8'?><foo bar='baz'></foo>"
		)), "jQuery.isXMLDoc");

		assert.ok(jQuery.isWindow(window), "jQuery.isWindow(window)");
		assert.ok(!jQuery.isWindow(2), "jQuery.isWindow(Number)");

		assert.strictEqual(jQuery.inArray(3, ["a", 6, false, 3, {}]), 3, "jQuery.inArray - true");
		assert.strictEqual(
			jQuery.inArray(3, ["a", 6, false, "3", {}]),
			-1,
			"jQuery.inArray - false"
		);

		assert.strictEqual(elem.get(1), elem[1], ".get");
		assert.strictEqual(elem.first()[0], elem[0], ".first");
		assert.strictEqual(elem.last()[0], elem[1], ".last");

		assert.deepEqual(jQuery.map(["a", "b", "c"], function (v, k) {
			return k + v;
		}), ["0a", "1b", "2c"], "jQuery.map");

		assert.deepEqual(jQuery.merge([1, 2], ["a", "b"]), [1, 2, "a", "b"], "jQuery.merge");

		assert.deepEqual(jQuery.grep([1, 2, 3], function (value) {
			return value % 2 !== 0;
		}), [1, 3], "jQuery.grep");

		assert.deepEqual(jQuery.extend({a: 2}, {b: 3}), {a: 2, b: 3}, "jQuery.extend");

		jQuery.each([0, 2], function (k, v) {
			assert.strictEqual(k * 2, v, "jQuery.each");
		});

		assert.deepEqual(jQuery.makeArray({0: "a", 1: "b", 2: "c", length: 3}),
			["a", "b", "c"], "jQuery.makeArray");

		assert.strictEqual(jQuery.parseHTML("<div></div><span></span>").length,
			2, "jQuery.parseHTML");

		assert.deepEqual(jQuery.parseJSON("{\"a\": 2}"), {a: 2}, "jQuery.parseJON");
	});

	QUnit.test("data", function (assert) {
		assert.expect(4);

		var elem = jQuery("<div data-c='d'/>").appendTo("#qunit-fixture");

		assert.ok(!jQuery.hasData(elem[0]), "jQuery.hasData - false");
		assert.strictEqual(elem.data("a", "b").data("a"), "b", ".data getter/setter");
		assert.strictEqual(elem.data("c"), "d", ".data from data-* attributes");
		assert.ok(jQuery.hasData(elem[0]), "jQuery.hasData - true");
	});

	QUnit.test("dimensions", function (assert) {
		assert.expect(3);

		var elem = jQuery(
			"<div style='margin: 10px; padding: 7px; border: 2px solid black;' /> "
		).appendTo("#qunit-fixture");

		assert.strictEqual(elem.width(50).width(), 50, ".width getter/setter");
		assert.strictEqual(elem.innerWidth(), 64, ".innerWidth getter");
		assert.strictEqual(elem.outerWidth(), 68, ".outerWidth getter");
	});

	QUnit.test("event", function (assert) {
		assert.expect(1);

		var elem = jQuery("<div/>").appendTo("#qunit-fixture");

		elem
			.on("click", function () {
				assert.ok(false, "click should not fire");
			})
			.off("click")
			.trigger("click")
			.on("click", function () {
				assert.ok(true, "click should fire");
			})
			.trigger("click");
	});

	QUnit.test("manipulation", function (assert) {
		assert.expect(5);

		var child,
			elem1 = jQuery("<div><span></span></div>").appendTo("#qunit-fixture"),
			elem2 = jQuery("<div/>").appendTo("#qunit-fixture");

		assert.strictEqual(elem1.text("foo").text(), "foo", ".html getter/setter");

		assert.strictEqual(
			// Support: IE 8 only
			// IE 8 prints tag names in upper case.
			elem1.html("<span/>").html().toLowerCase(),
			"<span></span>",
			".html getter/setter"
		);

		assert.strictEqual(elem1.append(elem2)[0].childNodes[1], elem2[0], ".append");
		assert.strictEqual(elem1.prepend(elem2)[0].childNodes[0], elem2[0], ".prepend");

		child = elem1.find("span");
		child.after("<a/>");
		child.before("<b/>");

		assert.strictEqual(
			// Support: IE 8 only
			// IE 8 prints tag names in upper case.
			elem1.html().toLowerCase(),
			"<div></div><b></b><span></span><a></a>",
			".after/.before"
		);
	});

	QUnit.test("offset", function (assert) {
		assert.expect(3);

		var parent = jQuery("<div style='position:fixed;top:20px;'/>").appendTo("#qunit-fixture"),
			elem = jQuery("<div style='position:absolute;top:5px;'/>").appendTo(parent);

		assert.strictEqual(elem.offset().top, 25, ".offset getter");
		assert.strictEqual(elem.position().top, 5, ".position getter");
		assert.strictEqual(elem.offsetParent()[0], parent[0], ".offsetParent");
	});

	QUnit.test("selector", function (assert) {
		assert.expect(2);

		var elem = jQuery("<div><span class='a'></span><span class='b'><a></a></span></div>")
			.appendTo("#qunit-fixture");

		assert.strictEqual(elem.find(".a a").length, 0, ".find - no result");
		assert.strictEqual(elem.find("span.b a")[0].nodeName, "A", ".find - one result");
	});

	QUnit.test("serialize", function (assert) {
		assert.expect(2);

		var params = {"someName": [1, 2, 3], "regularThing": "blah"};
		assert.strictEqual(jQuery.param(params),
			"someName%5B%5D=1&someName%5B%5D=2&someName%5B%5D=3&regularThing=blah",
			"jQuery.param");

		assert.strictEqual(jQuery("#form").serialize(),
			"action=Test&radio2=on&check=on&hidden=&foo%5Bbar%5D=&name=name&search=search" +
			"&select1=&select2=3&select3=1&select3=2&select5=3",
			"form serialization as query string");
	});

	QUnit.test("traversing", function (assert) {
		assert.expect(12);

		var elem = jQuery("<div><a><b><em></em></b></a><i></i><span></span>foo</div>")
			.appendTo("#qunit-fixture");

		assert.strictEqual(elem.find("em").parent()[0].nodeName, "B", ".parent");
		assert.strictEqual(elem.find("em").parents()[1].nodeName, "A", ".parents");
		assert.strictEqual(elem.find("em").parentsUntil("div").length, 2, ".parentsUntil");
		assert.strictEqual(elem.find("i").next()[0].nodeName, "SPAN", ".next");
		assert.strictEqual(elem.find("i").prev()[0].nodeName, "A", ".prev");
		assert.strictEqual(elem.find("a").nextAll()[1].nodeName, "SPAN", ".nextAll");
		assert.strictEqual(elem.find("span").prevAll()[1].nodeName, "A", ".prevAll");
		assert.strictEqual(elem.find("a").nextUntil("span").length, 1, ".nextUntil");
		assert.strictEqual(elem.find("span").prevUntil("a").length, 1, ".prevUntil");
		assert.strictEqual(elem.find("i").siblings().length, 2, ".siblings");
		assert.strictEqual(elem.children()[2].nodeName, "SPAN", ".children");
		assert.strictEqual(elem.contents()[3].nodeType, 3, ".contents");
	});

	QUnit.test("wrap", function (assert) {
		assert.expect(3);

		var elem = jQuery("<div><a><b></b></a><a></a></div>");

		elem.find("b").wrap("<span>");

		assert.strictEqual(
			// Support: IE 8 only
			// IE 8 prints tag names in upper case.
			elem.html().toLowerCase(),
			"<a><span><b></b></span></a><a></a>",
			".wrap"
		);

		elem.find("span").wrapInner("<em>");

		assert.strictEqual(
			// Support: IE 8 only
			// IE 8 prints tag names in upper case.
			elem.html().toLowerCase(),
			"<a><span><em><b></b></em></span></a><a></a>",
			".wrapInner"
		);

		elem.find("a").wrapAll("<i>");

		assert.strictEqual(
			// Support: IE 8 only
			// IE 8 prints tag names in upper case.
			elem.html().toLowerCase(),
			"<i><a><span><em><b></b></em></span></a><a></a></i>",
			".wrapAll"
		);

	});
})();
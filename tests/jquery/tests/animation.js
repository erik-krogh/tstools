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
//    QUnit.config.requireExpects = true;

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

// Can't test what ain't there
    if (!jQuery.fx) {
        return;
    }

    var oldRaf = window.requestAnimationFrame,
        defaultPrefilter = jQuery.Animation.prefilters[0],
        defaultTweener = jQuery.Animation.tweeners["*"][0],
        startTime = 505877050;

// This module tests jQuery.Animation and the corresponding 1.8+ effects APIs
    QUnit.module("animation", {
        setup: function () {
            window.requestAnimationFrame = null;
            this.sandbox = sinon.sandbox.create();
            this.clock = this.sandbox.useFakeTimers(startTime);
            this._oldInterval = jQuery.fx.interval;
            jQuery.fx.step = {};
            jQuery.fx.interval = 10;
            jQuery.now = Date.now;
            jQuery.Animation.prefilters = [defaultPrefilter];
            jQuery.Animation.tweeners = {"*": [defaultTweener]};
        },
        teardown: function () {
            this.sandbox.restore();
            jQuery.now = Date.now;
            jQuery.fx.stop();
            jQuery.fx.interval = this._oldInterval;
            window.requestAnimationFrame = oldRaf;
            return moduleTeardown.apply(this, arguments);
        }
    });

    QUnit.test("Animation( subject, props, opts ) - shape", function (assert) {
        assert.expect(20);

        var subject = {test: 0},
            props = {test: 1},
            opts = {queue: "fx", duration: 100},
            animation = jQuery.Animation(subject, props, opts);

        assert.equal(
            animation.elem,
            subject,
            ".elem is set to the exact object passed"
        );
        assert.equal(
            animation.originalOptions,
            opts,
            ".originalOptions is set to options passed"
        );
        assert.equal(
            animation.originalProperties,
            props,
            ".originalProperties is set to props passed"
        );

        assert.notEqual(animation.props, props, ".props is not the original however");
        assert.deepEqual(animation.props, props, ".props is a copy of the original");

        assert.deepEqual(animation.opts, {
            duration: 100,
            queue: "fx",
            specialEasing: {test: undefined},
            easing: jQuery.easing._default
        }, ".options is filled with default easing and specialEasing");

        assert.equal(animation.startTime, startTime, "startTime was set");
        assert.equal(animation.duration, 100, ".duration is set");

        assert.equal(animation.tweens.length, 1, ".tweens has one Tween");
        assert.equal(typeof animation.tweens[0].run, "function", "which has a .run function");

        assert.equal(typeof animation.createTween, "function", ".createTween is a function");
        assert.equal(typeof animation.stop, "function", ".stop is a function");

        assert.equal(typeof animation.done, "function", ".done is a function");
        assert.equal(typeof animation.fail, "function", ".fail is a function");
        assert.equal(typeof animation.always, "function", ".always is a function");
        assert.equal(typeof animation.progress, "function", ".progress is a function");

        assert.equal(jQuery.timers.length, 1, "Added a timers function");
        assert.equal(jQuery.timers[0].elem, subject, "...with .elem as the subject");
        assert.equal(jQuery.timers[0].anim, animation, "...with .anim as the animation");
        assert.equal(jQuery.timers[0].queue, opts.queue, "...with .queue");

        // Cleanup after ourselves by ticking to the end
        this.clock.tick(100);
    });

    QUnit.test("Animation.prefilter( fn ) - calls prefilter after defaultPrefilter",
        function (assert) {
            assert.expect(1);

            var prefilter = this.sandbox.stub(),
                defaultSpy = this.sandbox.spy(jQuery.Animation.prefilters, 0);

            jQuery.Animation.prefilter(prefilter);

            jQuery.Animation({}, {}, {});
            assert.ok(prefilter.calledAfter(defaultSpy), "our prefilter called after");
        }
    );

    QUnit.test("Animation.prefilter( fn, true ) - calls prefilter before defaultPrefilter",
        function (assert) {
            assert.expect(1);

            var prefilter = this.sandbox.stub(),
                defaultSpy = this.sandbox.spy(jQuery.Animation.prefilters, 0);

            jQuery.Animation.prefilter(prefilter, true);

            jQuery.Animation({}, {}, {});
            assert.ok(prefilter.calledBefore(defaultSpy), "our prefilter called before");
        }
    );

    QUnit.test("Animation.prefilter - prefilter return hooks", function (assert) {
        assert.expect(34);

        var animation, realAnimation, element,
            sandbox = this.sandbox,
            ourAnimation = {stop: this.sandbox.spy()},
            target = {height: 50},
            props = {height: 100},
            opts = {duration: 100},
            prefilter = this.sandbox.spy(function () {
                realAnimation = this;
                sandbox.spy(realAnimation, "createTween");

                assert.deepEqual(realAnimation.originalProperties, props, "originalProperties");
                assert.equal(arguments[0], this.elem, "first param elem");
                assert.equal(arguments[1], this.props, "second param props");
                assert.equal(arguments[2], this.opts, "third param opts");
                return ourAnimation;
            }),
            defaultSpy = sandbox.spy(jQuery.Animation.prefilters, 0),
            queueSpy = sandbox.spy(function (next) {
                next();
            }),
            TweenSpy = sandbox.spy(jQuery, "Tween");

        jQuery.Animation.prefilter(prefilter, true);

        sandbox.stub(jQuery.fx, "timer");

        animation = jQuery.Animation(target, props, opts);

        assert.equal(prefilter.callCount, 1, "Called prefilter");

        assert.equal(
            defaultSpy.callCount,
            0,
            "Returning something from a prefilter caused remaining prefilters to not run"
        );
        assert.equal(jQuery.fx.timer.callCount, 0, "Returning something never queues a timer");
        assert.equal(
            animation,
            ourAnimation,
            "Returning something returned it from jQuery.Animation"
        );
        assert.equal(
            realAnimation.createTween.callCount,
            0,
            "Returning something never creates tweens"
        );
        assert.equal(TweenSpy.callCount, 0, "Returning something never creates tweens");

        // Test overridden usage on queues:
        prefilter.reset();
        element = jQuery("<div>")
            .css("height", 50)
            .animate(props, 100)
            .queue(queueSpy)
            .animate(props, 100)
            .queue(queueSpy)
            .animate(props, 100)
            .queue(queueSpy);

        assert.equal(prefilter.callCount, 1, "Called prefilter");
        assert.equal(queueSpy.callCount, 0, "Next function in queue not called");

        realAnimation.opts.complete.call(realAnimation.elem);
        assert.equal(queueSpy.callCount, 1, "Next function in queue called after complete");

        assert.equal(prefilter.callCount, 2, "Called prefilter again - animation #2");
        assert.equal(ourAnimation.stop.callCount, 0, ".stop() on our animation hasn't been called");

        element.stop();
        assert.equal(ourAnimation.stop.callCount, 1, ".stop() called ourAnimation.stop()");
        assert.ok(
            !ourAnimation.stop.args[0][0],
            ".stop( falsy ) (undefined or false are both valid)"
        );

        assert.equal(queueSpy.callCount, 2, "Next queue function called");
        assert.ok(queueSpy.calledAfter(ourAnimation.stop), "After our animation was told to stop");

        // ourAnimation.stop.reset();
        assert.equal(prefilter.callCount, 3, "Got the next animation");

        ourAnimation.stop.reset();

        // do not clear queue, gotoEnd
        element.stop(false, true);
        assert.ok(ourAnimation.stop.calledWith(true), ".stop(true) calls .stop(true)");
        assert.ok(queueSpy.calledAfter(ourAnimation.stop),
            "and the next queue function ran after we were told");
    });

    QUnit.test("Animation.tweener( fn ) - unshifts a * tweener", function (assert) {
        assert.expect(2);
        var starTweeners = jQuery.Animation.tweeners["*"];

        jQuery.Animation.tweener(jQuery.noop);
        assert.equal(starTweeners.length, 2);
        assert.deepEqual(starTweeners, [jQuery.noop, defaultTweener]);
    });

    QUnit.test("Animation.tweener( 'prop', fn ) - unshifts a 'prop' tweener", function (assert) {
        assert.expect(4);
        var tweeners = jQuery.Animation.tweeners,
            fn = function () {
            };

        jQuery.Animation.tweener("prop", jQuery.noop);
        assert.equal(tweeners.prop.length, 1);
        assert.deepEqual(tweeners.prop, [jQuery.noop]);

        jQuery.Animation.tweener("prop", fn);
        assert.equal(tweeners.prop.length, 2);
        assert.deepEqual(tweeners.prop, [fn, jQuery.noop]);
    });

    QUnit.test(
        "Animation.tweener( 'list of props', fn ) - unshifts a tweener to each prop",
        function (assert) {
            assert.expect(2);
            var tweeners = jQuery.Animation.tweeners,
                fn = function () {
                };

            jQuery.Animation.tweener("list of props", jQuery.noop);
            assert.deepEqual(tweeners, {
                list: [jQuery.noop],
                of: [jQuery.noop],
                props: [jQuery.noop],
                "*": [defaultTweener]
            });

            // Test with extra whitespaces
            jQuery.Animation.tweener(" list\t of \tprops\n*", fn);
            assert.deepEqual(tweeners, {
                list: [fn, jQuery.noop],
                of: [fn, jQuery.noop],
                props: [fn, jQuery.noop],
                "*": [fn, defaultTweener]
            });
        }
    );

})();

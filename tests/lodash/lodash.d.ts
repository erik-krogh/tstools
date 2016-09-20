// Type definitions for Lo-Dash 4.14
// Project: http://lodash.com/
// Definitions by: Brian Zengel <https://github.com/bczengel>, Ilya Mochalov <https://github.com/chrootsu>, Stepan Mikhaylyuk <https://github.com/stepancar>
// Definitions: https://github.com/DefinitelyTyped/DefinitelyTyped


/**
 ### 4.0.0 Changelog (https://github.com/lodash/lodash/wiki/Changelog)

 #### TODO:
 removed:
 - [x] Removed lodash_support
 - [x] Removed lodash_findWhere in favor of lodash_find with iteratee shorthand
 - [x] Removed lodash_where in favor of lodash_filter with iteratee shorthand
 - [x] Removed lodash_pluck in favor of lodash_map with iteratee shorthand

 renamed:
 - [x] Renamed lodash_first to lodash_head
 - [x] Renamed lodash_indexBy to lodash_keyBy
 - [x] Renamed lodash_invoke to lodash_invokeMap
 - [x] Renamed lodash_overArgs to lodash_overArgs
 - [x] Renamed lodash_padLeft & lodash_padRight to lodash_padStart & lodash_padEnd
 - [x] Renamed lodash_pairs to lodash_toPairs
 - [x] Renamed lodash_rest to lodash_tail
 - [x] Renamed lodash_restParam to lodash_rest
 - [x] Renamed lodash_sortByOrder to lodash_orderBy
 - [x] Renamed lodash_trimLeft & lodash_trimRight to lodash_trimStart & lodash_trimEnd
 - [x] Renamed lodash_trunc to lodash_truncate

 split:
 - [x] Split lodash_indexOf & lodash_lastIndexOf into lodash_sortedIndexOf & lodash_sortedLastIndexOf
 - [x] Split lodash_max & lodash_min into lodash_maxBy & lodash_minBy
 - [x] Split lodash_omit & lodash_pick into lodash_omitBy & lodash_pickBy
 - [x] Split lodash_sample into lodash_sampleSize
 - [x] Split lodash_sortedIndex into lodash_sortedIndexBy
 - [x] Split lodash_sortedLastIndex into lodash_sortedLastIndexBy
 - [x] Split lodash_uniq into lodash_sortedUniq, lodash_sortedUniqBy, & lodash_uniqBy

 changes:
 - [x] Absorbed lodash_sortByAll into lodash_sortBy
 - [x] Changed the category of lodash_at to “Object”
 - [x] Changed the category of lodash_bindAll to “Utility”
 - [x] Made lodash_capitalize uppercase the first character & lowercase the rest
 - [x] Made lodash_functions return only own method names


 added 23 array methods:
 - [x] lodash_concat
 - [x] lodash_differenceBy
 - [x] lodash_differenceWith
 - [x] lodash_flatMap
 - [x] lodash_fromPairs
 - [x] lodash_intersectionBy
 - [x] lodash_intersectionWith
 - [x] lodash_join
 - [x] lodash_pullAll
 - [x] lodash_pullAllBy
 - [x] lodash_reverse
 - [x] lodash_sortedIndexBy
 - [x] lodash_sortedIndexOf
 - [x] lodash_sortedLastIndexBy
 - [x] lodash_sortedLastIndexOf
 - [x] lodash_sortedUniq
 - [x] lodash_sortedUniqBy
 - [x] lodash_unionBy
 - [x] lodash_unionWith
 - [x] lodash_uniqBy
 - [x] lodash_uniqWith
 - [x] lodash_xorBy
 - [x] lodash_xorWith

 added 18 lang methods:
 - [x] lodash_cloneDeepWith
 - [x] lodash_cloneWith
 - [x] lodash_eq
 - [x] lodash_isArrayLike
 - [x] lodash_isArrayLikeObject
 - [x] lodash_isEqualWith
 - [x] lodash_isInteger
 - [x] lodash_isLength
 - [x] lodash_isMatchWith
 - [x] lodash_isNil
 - [x] lodash_isObjectLike
 - [x] lodash_isSafeInteger
 - [x] lodash_isSymbol
 - [x] lodash_toInteger
 - [x] lodash_toLength
 - [x] lodash_toNumber
 - [x] lodash_toSafeInteger
 - [x] lodash_toString

 added 13 object methods:
 - [x] lodash_assignIn
 - [x] lodash_assignInWith
 - [x] lodash_assignWith
 - [x] lodash_functionsIn
 - [x] lodash_hasIn
 - [x] lodash_mergeWith
 - [x] lodash_omitBy
 - [x] lodash_pickBy


 added 8 string methods:
 - [x] lodash_lowerCase
 - [x] lodash_lowerFirst
 - [x] lodash_upperCase
 - [x] lodash_upperFirst
 - [x] lodash_toLower
 - [x] lodash_toUpper

 added 8 utility methods:
 - [x] lodash_toPath

 added 4 math methods:
 - [x] lodash_maxBy
 - [x] lodash_mean
 - [x] lodash_minBy
 - [x] lodash_sumBy

 added 2 function methods:
 - [x] lodash_flip
 - [x] lodash_unary

 added 2 number methods:
 - [x] lodash_clamp
 - [x] lodash_subtract

 added collection method:
 - [x] lodash_sampleSize

 Added 3 aliases

 - [x] lodash_first as an alias of lodash_head

 Removed 17 aliases
 - [x] Removed aliase lodash_all
 - [x] Removed aliase lodash_any
 - [x] Removed aliase lodash_backflow
 - [x] Removed aliase lodash_callback
 - [x] Removed aliase lodash_collect
 - [x] Removed aliase lodash_compose
 - [x] Removed aliase lodash_contains
 - [x] Removed aliase lodash_detect
 - [x] Removed aliase lodash_foldl
 - [x] Removed aliase lodash_foldr
 - [x] Removed aliase lodash_include
 - [x] Removed aliase lodash_inject
 - [x] Removed aliase lodash_methods
 - [x] Removed aliase lodash_object
 - [x] Removed aliase lodash_run
 - [x] Removed aliase lodash_select
 - [x] Removed aliase lodash_unique

 Other changes
 - [x] Added support for array buffers to lodash_isEqual
 - [x] Added support for converting iterators to lodash_toArray
 - [x] Added support for deep paths to lodash_zipObject
 - [x] Changed UMD to export to window or self when available regardless of other exports
 - [x] Ensured debounce cancel clears args & thisArg references
 - [x] Ensured lodash_add, lodash_subtract, & lodash_sum don’t skip NaN values
 - [x] Ensured lodash_clone treats generators like functions
 - [x] Ensured lodash_clone produces clones with the source’s [[Prototype]]
 - [x] Ensured lodash_defaults assigns properties that shadow Object.prototype
 - [x] Ensured lodash_defaultsDeep doesn’t merge a string into an array
 - [x] Ensured lodash_defaultsDeep & lodash_merge don’t modify sources
 - [x] Ensured lodash_defaultsDeep works with circular references
 - [x] Ensured lodash_keys skips “length” on strict mode arguments objects in Safari 9
 - [x] Ensured lodash_merge doesn’t convert strings to arrays
 - [x] Ensured lodash_merge merges plain-objects onto non plain-objects
 - [x] Ensured _#plant resets iterator data of cloned sequences
 - [x] Ensured lodash_random swaps min & max if min is greater than max
 - [x] Ensured lodash_range preserves the sign of start of -0
 - [x] Ensured lodash_reduce & lodash_reduceRight use getIteratee in their array branch
 - [x] Fixed rounding issue with the precision param of lodash_floor
 - [x] Added flush method to debounced & throttled functions

 ** LATER **
 Misc:
 - [ ] Made lodash_forEach, lodash_forIn, lodash_forOwn, & lodash_times implicitly end a chain sequence
 - [ ] Removed thisArg params from most methods
 - [ ] Made “By” methods provide a single param to iteratees
 - [ ] Made lodash_words chainable by default
 - [ ] Removed isDeep params from lodash_clone & lodash_flatten
 - [ ] Removed lodash_bindAll support for binding all methods when no names are provided
 - [ ] Removed func-first param signature from lodash_before & lodash_after
 - [ ] lodash_extend as an alias of lodash_assignIn
 - [ ] lodash_extendWith as an alias of lodash_assignInWith
 - [ ] Added clear method to lodash_memoize.Cache
 - [ ] Added support for ES6 maps, sets, & symbols to lodash_clone, lodash_isEqual, & lodash_toArray
 - [ ] Enabled lodash_flow & lodash_flowRight to accept an array of functions
 - [ ] Ensured “Collection” methods treat functions as objects
 - [ ] Ensured lodash_assign, lodash_defaults, & lodash_merge coerce object values to objects
 - [ ] Ensured lodash_bindKey bound functions call object[key] when called with the new operator
 - [ ] Ensured lodash_isFunction returns true for generator functions
 - [ ] Ensured lodash_merge assigns typed arrays directly
 - [ ] Made _(...) an iterator & iterable
 - [ ] Made lodash_drop, lodash_take, & right forms coerce n of undefined to 0

 Methods:
 - [ ] lodash_concat
 - [ ] lodash_differenceBy
 - [ ] lodash_differenceWith
 - [ ] lodash_flatMap
 - [ ] lodash_fromPairs
 - [ ] lodash_intersectionBy
 - [ ] lodash_intersectionWith
 - [ ] lodash_join
 - [ ] lodash_pullAll
 - [ ] lodash_pullAllBy
 - [ ] lodash_reverse
 - [ ] lodash_sortedLastIndexOf
 - [ ] lodash_unionBy
 - [ ] lodash_unionWith
 - [ ] lodash_uniqWith
 - [ ] lodash_xorBy
 - [ ] lodash_xorWith
 - [ ] lodash_toString

 - [ ] lodash_invoke
 - [ ] lodash_setWith
 - [ ] lodash_toPairs
 - [ ] lodash_toPairsIn
 - [ ] lodash_unset

 - [ ] lodash_replace
 - [ ] lodash_split

 - [ ] lodash_cond
 - [ ] lodash_conforms
 - [ ] lodash_nthArg
 - [ ] lodash_over
 - [ ] lodash_overEvery
 - [ ] lodash_overSome
 - [ ] lodash_rangeRight

 - [ ] lodash_next
 */

declare var _: lodash.LoDashStatic;

declare module lodash {
    interface LoDashStatic {
        /**
         * Creates a lodash object which wraps the given value to enable intuitive method chaining.
         *
         * In addition to Lo-Dash methods, wrappers also have the following Array methods:
         * concat, join, pop, push, reverse, shift, slice, sort, splice, and unshift
         *
         * Chaining is supported in custom builds as long as the value method is implicitly or
         * explicitly included in the build.
         *
         * The chainable wrapper functions are:
         * after, assign, bind, bindAll, bindKey, chain, chunk, compact, compose, concat, countBy,
         * createCallback, curry, debounce, defaults, defer, delay, difference, filter, flatten,
         * forEach, forEachRight, forIn, forInRight, forOwn, forOwnRight, functions, groupBy,
         * keyBy, initial, intersection, invert, invoke, keys, map, max, memoize, merge, min,
         * object, omit, once, pairs, partial, partialRight, pick, pluck, pull, push, range, reject,
         * remove, rest, reverse, sample, shuffle, slice, sort, sortBy, splice, tap, throttle, times,
         * toArray, transform, union, uniq, unset, unshift, unzip, values, where, without, wrap, and zip
         *
         * The non-chainable wrapper functions are:
         * clone, cloneDeep, contains, escape, every, find, findIndex, findKey, findLast,
         * findLastIndex, findLastKey, has, identity, indexOf, isArguments, isArray, isBoolean,
         * isDate, isElement, isEmpty, isEqual, isFinite, isFunction, isNaN, isNull, isNumber,
         * isObject, isPlainObject, isRegExp, isString, isUndefined, join, lastIndexOf, mixin,
         * noConflict, parseInt, pop, random, reduce, reduceRight, result, shift, size, some,
         * sortedIndex, runInContext, template, unescape, uniqueId, and value
         *
         * The wrapper functions first and last return wrapped values when n is provided, otherwise
         * they return unwrapped values.
         *
         * Explicit chaining can be enabled by using the lodash_chain method.
         **/
        (value: number): LoDashImplicitWrapper<number>;
        (value: string): LoDashImplicitStringWrapper;
        (value: boolean): LoDashImplicitWrapper<boolean>;
        (value: Array<number>): LoDashImplicitNumberArrayWrapper;
        <T>(value: Array<T>): LoDashImplicitArrayWrapper<T>;
        <T extends {}>(value: T): LoDashImplicitObjectWrapper<T>;
        (value: any): LoDashImplicitWrapper<any>;

        /**
         * The semantic version number.
         **/
        VERSION: string;

        /**
         * By default, the template delimiters used by Lo-Dash are similar to those in embedded Ruby
         * (ERB). Change the following template settings to use alternative delimiters.
         **/
        templateSettings: TemplateSettings;
    }

    /**
     * By default, the template delimiters used by Lo-Dash are similar to those in embedded Ruby
     * (ERB). Change the following template settings to use alternative delimiters.
     **/
    interface TemplateSettings {
        /**
         * The "escape" delimiter.
         **/
        escape?: RegExp;

        /**
         * The "evaluate" delimiter.
         **/
        evaluate?: RegExp;

        /**
         * An object to import into the template as local variables.
         **/
        imports?: Dictionary<any>;

        /**
         * The "interpolate" delimiter.
         **/
        interpolate?: RegExp;

        /**
         * Used to reference the data object in the template text.
         **/
        variable?: string;
    }

    /**
     * Creates a cache object to store key/value pairs.
     */
    interface MapCache {
        /**
         * Removes `key` and its value from the cache.
         * @param key The key of the value to remove.
         * @return Returns `true` if the entry was removed successfully, else `false`.
         */
        delete(key: string): boolean;

        /**
         * Gets the cached value for `key`.
         * @param key The key of the value to get.
         * @return Returns the cached value.
         */
        get(key: string): any;

        /**
         * Checks if a cached value for `key` exists.
         * @param key The key of the entry to check.
         * @return Returns `true` if an entry for `key` exists, else `false`.
         */
        has(key: string): boolean;

        /**
         * Sets `value` to `key` of the cache.
         * @param key The key of the value to cache.
         * @param value The value to cache.
         * @return Returns the cache object.
         */
        set(key: string, value: any): Dictionary<any>;
    }

    interface LoDashWrapperBase<T, TWrapper> { }

    interface LoDashImplicitWrapperBase<T, TWrapper> extends LoDashWrapperBase<T, TWrapper> { }

    interface LoDashExplicitWrapperBase<T, TWrapper> extends LoDashWrapperBase<T, TWrapper> { }

    interface LoDashImplicitWrapper<T> extends LoDashImplicitWrapperBase<T, LoDashImplicitWrapper<T>> { }

    interface LoDashExplicitWrapper<T> extends LoDashExplicitWrapperBase<T, LoDashExplicitWrapper<T>> { }

    interface LoDashImplicitStringWrapper extends LoDashImplicitWrapper<string> { }

    interface LoDashExplicitStringWrapper extends LoDashExplicitWrapper<string> { }

    interface LoDashImplicitObjectWrapper<T> extends LoDashImplicitWrapperBase<T, LoDashImplicitObjectWrapper<T>> { }

    interface LoDashExplicitObjectWrapper<T> extends LoDashExplicitWrapperBase<T, LoDashExplicitObjectWrapper<T>> { }

    interface LoDashImplicitArrayWrapper<T> extends LoDashImplicitWrapperBase<T[], LoDashImplicitArrayWrapper<T>> {
        pop(): T;
        push(...items: T[]): LoDashImplicitArrayWrapper<T>;
        shift(): T;
        sort(compareFn?: (a: T, b: T) => number): LoDashImplicitArrayWrapper<T>;
        splice(start: number): LoDashImplicitArrayWrapper<T>;
        splice(start: number, deleteCount: number, ...items: any[]): LoDashImplicitArrayWrapper<T>;
        unshift(...items: T[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> extends LoDashExplicitWrapperBase<T[], LoDashExplicitArrayWrapper<T>> { }

    interface LoDashImplicitNumberArrayWrapper extends LoDashImplicitArrayWrapper<number> { }

    interface LoDashExplicitNumberArrayWrapper extends LoDashExplicitArrayWrapper<number> { }

    /*********
     * Array *
     *********/

        //lodash_chunk
    interface LoDashStatic {
        /**
         * Creates an array of elements split into groups the length of size. If collection can’t be split evenly, the
         * final chunk will be the remaining elements.
         *
         * @param array The array to process.
         * @param size The length of each chunk.
         * @return Returns the new array containing chunks.
         */
        chunk<T>(
            array: List<T>,
            size?: number
        ): T[][];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_chunk
         */
        chunk(size?: number): LoDashImplicitArrayWrapper<T[]>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_chunk
         */
        chunk<TResult>(size?: number): LoDashImplicitArrayWrapper<TResult[]>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_chunk
         */
        chunk(size?: number): LoDashExplicitArrayWrapper<T[]>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_chunk
         */
        chunk<TResult>(size?: number): LoDashExplicitArrayWrapper<TResult[]>;
    }

    //lodash_compact
    interface LoDashStatic {
        /**
         * Creates an array with all falsey values removed. The values false, null, 0, "", undefined, and NaN are
         * falsey.
         *
         * @param array The array to compact.
         * @return (Array) Returns the new array of filtered values.
         */
        compact<T>(array?: List<T>): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_compact
         */
        compact(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_compact
         */
        compact<TResult>(): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_compact
         */
        compact(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_compact
         */
        compact<TResult>(): LoDashExplicitArrayWrapper<TResult>;
    }

    //lodash_concat DUMMY
    interface LoDashStatic {
        /**
         * Creates a new array concatenating `array` with any additional arrays
         * and/or values.
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The array to concatenate.
         * @param {...*} [values] The values to concatenate.
         * @returns {Array} Returns the new concatenated array.
         * @example
         *
         * var array = [1];
         * var other = lodash_concat(array, 2, [3], [[4]]);
         *
         * console.log(other);
         * // => [1, 2, 3, [4]]
         *
         * console.log(array);
         * // => [1]
         */
        concat<T>(...values: (T[]|List<T>)[]) : T[];
    }

    //lodash_difference
    interface LoDashStatic {
        /**
         * Creates an array of unique array values not included in the other provided arrays using SameValueZero for
         * equality comparisons.
         *
         * @param array The array to inspect.
         * @param values The arrays of values to exclude.
         * @return Returns the new array of filtered values.
         */
        difference<T>(
            array: T[]|List<T>,
            ...values: Array<T[]|List<T>>
        ): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_difference
         */
        difference(...values: (T[]|List<T>)[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_difference
         */
        difference<TValue>(...values: (TValue[]|List<TValue>)[]): LoDashImplicitArrayWrapper<TValue>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_difference
         */
        difference(...values: (T[]|List<T>)[]): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_difference
         */
        difference<TValue>(...values: (TValue[]|List<TValue>)[]): LoDashExplicitArrayWrapper<TValue>;
    }

    //lodash_differenceBy
    interface LoDashStatic {
        /**
         * This method is like lodash_difference except that it accepts iteratee which is invoked for each element of array
         * and values to generate the criterion by which uniqueness is computed. The iteratee is invoked with one
         * argument: (value).
         *
         * @param array The array to inspect.
         * @param values The values to exclude.
         * @param iteratee The iteratee invoked per element.
         * @returns Returns the new array of filtered values.
         */
        differenceBy<T>(
            array: T[]|List<T>,
            values?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): T[];

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            array: T[]|List<T>,
            values?: T[]|List<T>,
            iteratee?: W
        ): T[];

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            array: T[]|List<T>,
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): T[];

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            array: T[]|List<T>,
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            iteratee?: W
        ): T[];

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            array: T[]|List<T>,
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): T[];

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            array: T[]|List<T>,
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            iteratee?: W
        ): T[];

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            array: T[]|List<T>,
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            iteratee?: W
        ): T[];

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            array: T[]|List<T>,
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): T[];

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            array: T[]|List<T>,
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            values5?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): T[];

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            array: T[]|List<T>,
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            values5?: T[]|List<T>,
            iteratee?: W
        ): T[];

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            array: T[]|List<T>,
            ...values: any[]
        ): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values?: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            values5?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            values5?: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            ...values: any[]
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values?: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            values5?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            values5?: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            ...values: any[]
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values?: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            values5?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            values5?: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            ...values: any[]
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values?: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            values5?: T[]|List<T>,
            iteratee?: ((value: T) => any)|string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T, W extends Object>(
            values1?: T[]|List<T>,
            values2?: T[]|List<T>,
            values3?: T[]|List<T>,
            values4?: T[]|List<T>,
            values5?: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_differenceBy
         */
        differenceBy<T>(
            ...values: any[]
        ): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_differenceWith DUMMY
    interface LoDashStatic {
        /**
         * Creates an array of unique `array` values not included in the other
         * provided arrays using [`SameValueZero`](http://ecma-international.org/ecma-262/6.0/#sec-samevaluezero)
         * for equality comparisons.
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The array to inspect.
         * @param {...Array} [values] The values to exclude.
         * @returns {Array} Returns the new array of filtered values.
         * @example
         *
         * lodash_difference([3, 2, 1], [4, 2]);
         * // => [3, 1]
         */
        differenceWith(
            array: any[]|List<any>,
            ...values: any[]
        ): any[];
    }

    //lodash_drop
    interface LoDashStatic {
        /**
         * Creates a slice of array with n elements dropped from the beginning.
         *
         * @param array The array to query.
         * @param n The number of elements to drop.
         * @return Returns the slice of array.
         */
        drop<T>(array: T[]|List<T>, n?: number): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_drop
         */
        drop(n?: number): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_drop
         */
        drop<T>(n?: number): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_drop
         */
        drop(n?: number): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_drop
         */
        drop<T>(n?: number): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_dropRight
    interface LoDashStatic {
        /**
         * Creates a slice of array with n elements dropped from the end.
         *
         * @param array The array to query.
         * @param n The number of elements to drop.
         * @return Returns the slice of array.
         */
        dropRight<T>(
            array: List<T>,
            n?: number
        ): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_dropRight
         */
        dropRight(n?: number): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_dropRight
         */
        dropRight<TResult>(n?: number): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_dropRight
         */
        dropRight(n?: number): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_dropRight
         */
        dropRight<TResult>(n?: number): LoDashExplicitArrayWrapper<TResult>;
    }

    //lodash_dropRightWhile
    interface LoDashStatic {
        /**
         * Creates a slice of array excluding elements dropped from the end. Elements are dropped until predicate
         * returns falsey. The predicate is bound to thisArg and invoked with three arguments: (value, index, array).
         *
         * If a property name is provided for predicate the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for predicate the created lodash_matches style callback returns true for elements that
         * match the properties of the given object, else false.
         *
         * @param array The array to query.
         * @param predicate The function invoked per iteration.
         * @param thisArg The this binding of predicate.
         * @return Returns the slice of array.
         */
        dropRightWhile<TValue>(
            array: List<TValue>,
            predicate?: ListIterator<TValue, boolean>
        ): TValue[];

        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile<TValue>(
            array: List<TValue>,
            predicate?: string
        ): TValue[];

        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile<TWhere, TValue>(
            array: List<TValue>,
            predicate?: TWhere
        ): TValue[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile(
            predicate?: ListIterator<T, boolean>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile(
            predicate?: string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile<TWhere>(
            predicate?: TWhere
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile<TValue>(
            predicate?: ListIterator<TValue, boolean>
        ): LoDashImplicitArrayWrapper<TValue>;

        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile<TValue>(
            predicate?: string
        ): LoDashImplicitArrayWrapper<TValue>;

        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile<TWhere, TValue>(
            predicate?: TWhere
        ): LoDashImplicitArrayWrapper<TValue>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile(
            predicate?: ListIterator<T, boolean>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile(
            predicate?: string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile<TWhere>(
            predicate?: TWhere
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile<TValue>(
            predicate?: ListIterator<TValue, boolean>
        ): LoDashExplicitArrayWrapper<TValue>;

        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile<TValue>(
            predicate?: string
        ): LoDashExplicitArrayWrapper<TValue>;

        /**
         * @see lodash_dropRightWhile
         */
        dropRightWhile<TWhere, TValue>(
            predicate?: TWhere
        ): LoDashExplicitArrayWrapper<TValue>;
    }

    //lodash_dropWhile
    interface LoDashStatic {
        /**
         * Creates a slice of array excluding elements dropped from the beginning. Elements are dropped until predicate
         * returns falsey. The predicate is bound to thisArg and invoked with three arguments: (value, index, array).
         *
         * If a property name is provided for predicate the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for predicate the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * @param array The array to query.
         * @param predicate The function invoked per iteration.
         * @param thisArg The this binding of predicate.
         * @return Returns the slice of array.
         */
        dropWhile<TValue>(
            array: List<TValue>,
            predicate?: ListIterator<TValue, boolean>
        ): TValue[];

        /**
         * @see lodash_dropWhile
         */
        dropWhile<TValue>(
            array: List<TValue>,
            predicate?: string
        ): TValue[];

        /**
         * @see lodash_dropWhile
         */
        dropWhile<TWhere, TValue>(
            array: List<TValue>,
            predicate?: TWhere
        ): TValue[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_dropWhile
         */
        dropWhile(
            predicate?: ListIterator<T, boolean>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_dropWhile
         */
        dropWhile(
            predicate?: string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_dropWhile
         */
        dropWhile<TWhere>(
            predicate?: TWhere
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_dropWhile
         */
        dropWhile<TValue>(
            predicate?: ListIterator<TValue, boolean>
        ): LoDashImplicitArrayWrapper<TValue>;

        /**
         * @see lodash_dropWhile
         */
        dropWhile<TValue>(
            predicate?: string
        ): LoDashImplicitArrayWrapper<TValue>;

        /**
         * @see lodash_dropWhile
         */
        dropWhile<TWhere, TValue>(
            predicate?: TWhere
        ): LoDashImplicitArrayWrapper<TValue>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_dropWhile
         */
        dropWhile(
            predicate?: ListIterator<T, boolean>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_dropWhile
         */
        dropWhile(
            predicate?: string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_dropWhile
         */
        dropWhile<TWhere>(
            predicate?: TWhere
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_dropWhile
         */
        dropWhile<TValue>(
            predicate?: ListIterator<TValue, boolean>
        ): LoDashExplicitArrayWrapper<TValue>;

        /**
         * @see lodash_dropWhile
         */
        dropWhile<TValue>(
            predicate?: string
        ): LoDashExplicitArrayWrapper<TValue>;

        /**
         * @see lodash_dropWhile
         */
        dropWhile<TWhere, TValue>(
            predicate?: TWhere
        ): LoDashExplicitArrayWrapper<TValue>;
    }

    //lodash_fill
    interface LoDashStatic {
        /**
         * Fills elements of array with value from start up to, but not including, end.
         *
         * Note: This method mutates array.
         *
         * @param array The array to fill.
         * @param value The value to fill array with.
         * @param start The start position.
         * @param end The end position.
         * @return Returns array.
         */
        fill<T>(
            array: any[],
            value: T,
            start?: number,
            end?: number
        ): T[];

        /**
         * @see lodash_fill
         */
        fill<T>(
            array: List<any>,
            value: T,
            start?: number,
            end?: number
        ): List<T>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_fill
         */
        fill<T>(
            value: T,
            start?: number,
            end?: number
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_fill
         */
        fill<T>(
            value: T,
            start?: number,
            end?: number
        ): LoDashImplicitObjectWrapper<List<T>>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_fill
         */
        fill<T>(
            value: T,
            start?: number,
            end?: number
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_fill
         */
        fill<T>(
            value: T,
            start?: number,
            end?: number
        ): LoDashExplicitObjectWrapper<List<T>>;
    }

    //lodash_findIndex
    interface LoDashStatic {
        /**
         * This method is like lodash_find except that it returns the index of the first element predicate returns truthy
         * for instead of the element itself.
         *
         * If a property name is provided for predicate the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for predicate the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * @param array The array to search.
         * @param predicate The function invoked per iteration.
         * @param thisArg The this binding of predicate.
         * @return Returns the index of the found element, else -1.
         */
        findIndex<T>(
            array: List<T>,
            predicate?: ListIterator<T, boolean>
        ): number;

        /**
         * @see lodash_findIndex
         */
        findIndex<T>(
            array: List<T>,
            predicate?: string
        ): number;

        /**
         * @see lodash_findIndex
         */
        findIndex<W, T>(
            array: List<T>,
            predicate?: W
        ): number;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_findIndex
         */
        findIndex(
            predicate?: ListIterator<T, boolean>
        ): number;

        /**
         * @see lodash_findIndex
         */
        findIndex(
            predicate?: string
        ): number;

        /**
         * @see lodash_findIndex
         */
        findIndex<W>(
            predicate?: W
        ): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_findIndex
         */
        findIndex<TResult>(
            predicate?: ListIterator<TResult, boolean>
        ): number;

        /**
         * @see lodash_findIndex
         */
        findIndex(
            predicate?: string
        ): number;

        /**
         * @see lodash_findIndex
         */
        findIndex<W>(
            predicate?: W
        ): number;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_findIndex
         */
        findIndex(
            predicate?: ListIterator<T, boolean>
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_findIndex
         */
        findIndex(
            predicate?: string
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_findIndex
         */
        findIndex<W>(
            predicate?: W
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_findIndex
         */
        findIndex<TResult>(
            predicate?: ListIterator<TResult, boolean>
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_findIndex
         */
        findIndex(
            predicate?: string
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_findIndex
         */
        findIndex<W>(
            predicate?: W
        ): LoDashExplicitWrapper<number>;
    }

    //lodash_findLastIndex
    interface LoDashStatic {
        /**
         * This method is like lodash_findIndex except that it iterates over elements of collection from right to left.
         *
         * If a property name is provided for predicate the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for predicate the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * @param array The array to search.
         * @param predicate The function invoked per iteration.
         * @param thisArg The function invoked per iteration.
         * @return Returns the index of the found element, else -1.
         */
        findLastIndex<T>(
            array: List<T>,
            predicate?: ListIterator<T, boolean>
        ): number;

        /**
         * @see lodash_findLastIndex
         */
        findLastIndex<T>(
            array: List<T>,
            predicate?: string
        ): number;

        /**
         * @see lodash_findLastIndex
         */
        findLastIndex<W, T>(
            array: List<T>,
            predicate?: W
        ): number;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_findLastIndex
         */
        findLastIndex(
            predicate?: ListIterator<T, boolean>
        ): number;

        /**
         * @see lodash_findLastIndex
         */
        findLastIndex(
            predicate?: string
        ): number;

        /**
         * @see lodash_findLastIndex
         */
        findLastIndex<W>(
            predicate?: W
        ): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_findLastIndex
         */
        findLastIndex<TResult>(
            predicate?: ListIterator<TResult, boolean>
        ): number;

        /**
         * @see lodash_findLastIndex
         */
        findLastIndex(
            predicate?: string
        ): number;

        /**
         * @see lodash_findLastIndex
         */
        findLastIndex<W>(
            predicate?: W
        ): number;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_findLastIndex
         */
        findLastIndex(
            predicate?: ListIterator<T, boolean>
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_findLastIndex
         */
        findLastIndex(
            predicate?: string
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_findLastIndex
         */
        findLastIndex<W>(
            predicate?: W
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_findLastIndex
         */
        findLastIndex<TResult>(
            predicate?: ListIterator<TResult, boolean>
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_findLastIndex
         */
        findLastIndex(
            predicate?: string
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_findLastIndex
         */
        findLastIndex<W>(
            predicate?: W
        ): LoDashExplicitWrapper<number>;
    }

    //lodash_first
    interface LoDashStatic {
        /**
         * @see lodash_head
         */
        first<T>(array: List<T>): T;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_head
         */
        first(): string;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_head
         */
        first(): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_head
         */
        first<T>(): T;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_head
         */
        first(): LoDashExplicitWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_head
         */
        first<T>(): T;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_head
         */
        first<T>(): T;
    }

    interface RecursiveArray<T> extends Array<T|RecursiveArray<T>> {}
    interface ListOfRecursiveArraysOrValues<T> extends List<T|RecursiveArray<T>> {}

    //lodash_flatten
    interface LoDashStatic {
        /**
         * Flattens a nested array. If isDeep is true the array is recursively flattened, otherwise it’s only
         * flattened a single level.
         *
         * @param array The array to flatten.
         * @param isDeep Specify a deep flatten.
         * @return Returns the new flattened array.
         */
        flatten<T>(array: ListOfRecursiveArraysOrValues<T>, isDeep: boolean): T[];

        /**
         * @see lodash_flatten
         */
        flatten<T>(array: List<T|T[]>): T[];

        /**
         * @see lodash_flatten
         */
        flatten<T>(array: ListOfRecursiveArraysOrValues<T>): RecursiveArray<T>;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_flatten
         */
        flatten(): LoDashImplicitArrayWrapper<string>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_flatten
         */
        flatten<TResult>(isDeep?: boolean): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_flatten
         */
        flatten<TResult>(isDeep?: boolean): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_flatten
         */
        flatten(): LoDashExplicitArrayWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_flatten
         */
        flatten<TResult>(isDeep?: boolean): LoDashExplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_flatten
         */
        flatten<TResult>(isDeep?: boolean): LoDashExplicitArrayWrapper<TResult>;
    }

    //lodash_flattenDeep
    interface LoDashStatic {
        /**
         * Recursively flattens a nested array.
         *
         * @param array The array to recursively flatten.
         * @return Returns the new flattened array.
         */
        flattenDeep<T>(array: ListOfRecursiveArraysOrValues<T>): T[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_flattenDeep
         */
        flattenDeep(): LoDashImplicitArrayWrapper<string>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_flattenDeep
         */
        flattenDeep<T>(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_flattenDeep
         */
        flattenDeep<T>(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_flattenDeep
         */
        flattenDeep(): LoDashExplicitArrayWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_flattenDeep
         */
        flattenDeep<T>(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_flattenDeep
         */
        flattenDeep<T>(): LoDashExplicitArrayWrapper<T>;
    }

    // lodash_flattenDepth
    interface LoDashStatic {
        /**
         * Recursively flatten array up to depth times.
         *
         * @param array The array to recursively flatten.
         * @param number The maximum recursion depth.
         * @return Returns the new flattened array.
         */
        flattenDepth<T>(array: ListOfRecursiveArraysOrValues<T>, depth?: number): T[];
    }

    //lodash_fromPairs
    interface LoDashStatic {
        /**
         * The inverse of `lodash_toPairs`; this method returns an object composed
         * from key-value `pairs`.
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} pairs The key-value pairs.
         * @returns {Object} Returns the new object.
         * @example
         *
         * lodash_fromPairs([['fred', 30], ['barney', 40]]);
         * // => { 'fred': 30, 'barney': 40 }
         */
        fromPairs<T>(
            array: List<[StringRepresentable, T]>
        ): Dictionary<T>;

        /**
         @see lodash_fromPairs
         */
        fromPairs(
            array: List<any[]>
        ): Dictionary<any>;
    }

    //lodash_fromPairs DUMMY
    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_fromPairs
         */
        fromPairs(): LoDashImplicitObjectWrapper<any>;
    }

    //lodash_fromPairs DUMMY
    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_fromPairs
         */
        fromPairs(): LoDashExplicitObjectWrapper<any>;
    }

    //lodash_head
    interface LoDashStatic {
        /**
         * Gets the first element of array.
         *
         * @alias lodash_first
         *
         * @param array The array to query.
         * @return Returns the first element of array.
         */
        head<T>(array: List<T>): T;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_head
         */
        head(): string;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_head
         */
        head(): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_head
         */
        head<T>(): T;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_head
         */
        head(): LoDashExplicitWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_head
         */
        head<T>(): T;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_head
         */
        head<T>(): T;
    }

    //lodash_indexOf
    interface LoDashStatic {
        /**
         * Gets the index at which the first occurrence of `value` is found in `array`
         * using [`SameValueZero`](http://ecma-international.org/ecma-262/6.0/#sec-samevaluezero)
         * for equality comparisons. If `fromIndex` is negative, it's used as the offset
         * from the end of `array`. If `array` is sorted providing `true` for `fromIndex`
         * performs a faster binary search.
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The array to search.
         * @param {*} value The value to search for.
         * @param {number} [fromIndex=0] The index to search from.
         * @returns {number} Returns the index of the matched value, else `-1`.
         * @example
         *
         * lodash_indexOf([1, 2, 1, 2], 2);
         * // => 1
         *
         * // using `fromIndex`
         * lodash_indexOf([1, 2, 1, 2], 2, 2);
         * // => 3
         */
        indexOf<T>(
            array: List<T>,
            value: T,
            fromIndex?: boolean|number
        ): number;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_indexOf
         */
        indexOf(
            value: T,
            fromIndex?: boolean|number
        ): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_indexOf
         */
        indexOf<TValue>(
            value: TValue,
            fromIndex?: boolean|number
        ): number;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_indexOf
         */
        indexOf(
            value: T,
            fromIndex?: boolean|number
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_indexOf
         */
        indexOf<TValue>(
            value: TValue,
            fromIndex?: boolean|number
        ): LoDashExplicitWrapper<number>;
    }

    //lodash_intersectionBy DUMMY
    interface LoDashStatic {
        /**
         * This method is like `lodash_intersection` except that it accepts `iteratee`
         * which is invoked for each element of each `arrays` to generate the criterion
         * by which uniqueness is computed. The iteratee is invoked with one argument: (value).
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {...Array} [arrays] The arrays to inspect.
         * @param {Function|Object|string} [iteratee=lodash_identity] The iteratee invoked per element.
         * @returns {Array} Returns the new array of shared values.
         * @example
         *
         * lodash_intersectionBy([2.1, 1.2], [4.3, 2.4], Math.floor);
         * // => [2.1]
         *
         * // using the `lodash_property` iteratee shorthand
         * lodash_intersectionBy([{ 'x': 1 }], [{ 'x': 2 }, { 'x': 1 }], 'x');
         * // => [{ 'x': 1 }]
         */
        intersectionBy(
            array: any[]|List<any>,
            ...values: any[]
        ): any[];
    }

    //lodash_intersectionWith DUMMY
    interface LoDashStatic {
        /**
         * This method is like `lodash_intersection` except that it accepts `comparator`
         * which is invoked to compare elements of `arrays`. The comparator is invoked
         * with two arguments: (arrVal, othVal).
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {...Array} [arrays] The arrays to inspect.
         * @param {Function} [comparator] The comparator invoked per element.
         * @returns {Array} Returns the new array of shared values.
         * @example
         *
         * var objects = [{ 'x': 1, 'y': 2 }, { 'x': 2, 'y': 1 }];
         * var others = [{ 'x': 1, 'y': 1 }, { 'x': 1, 'y': 2 }];
         *
         * lodash_intersectionWith(objects, others, lodash_isEqual);
         * // => [{ 'x': 1, 'y': 2 }]
         */
        intersectionWith(
            array: any[]|List<any>,
            ...values: any[]
        ): any[];
    }

    //lodash_join
    interface LoDashStatic {
        /**
         * Converts all elements in `array` into a string separated by `separator`.
         *
         * @param array The array to convert.
         * @param separator The element separator.
         * @returns Returns the joined string.
         */
        join(
            array: List<any>,
            separator?: string
        ): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_join
         */
        join(separator?: string): string;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_join
         */
        join(separator?: string): string;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_join
         */
        join(separator?: string): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_join
         */
        join(separator?: string): LoDashExplicitWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_join
         */
        join(separator?: string): LoDashExplicitWrapper<string>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_join
         */
        join(separator?: string): LoDashExplicitWrapper<string>;
    }

    //lodash_pullAll DUMMY
    interface LoDashStatic {
        /**
         * This method is like `lodash_pull` except that it accepts an array of values to remove.
         *
         * **Note:** Unlike `lodash_difference`, this method mutates `array`.
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The array to modify.
         * @param {Array} values The values to remove.
         * @returns {Array} Returns `array`.
         * @example
         *
         * var array = [1, 2, 3, 1, 2, 3];
         *
         * lodash_pull(array, [2, 3]);
         * console.log(array);
         * // => [1, 1]
         */
        pullAll(
            array: any[]|List<any>,
            ...values: any[]
        ): any[];
    }

    //lodash_pullAllBy DUMMY
    interface LoDashStatic {
        /**
         * This method is like `lodash_pullAll` except that it accepts `iteratee` which is
         * invoked for each element of `array` and `values` to to generate the criterion
         * by which uniqueness is computed. The iteratee is invoked with one argument: (value).
         *
         * **Note:** Unlike `lodash_differenceBy`, this method mutates `array`.
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The array to modify.
         * @param {Array} values The values to remove.
         * @param {Function|Object|string} [iteratee=lodash_identity] The iteratee invoked per element.
         * @returns {Array} Returns `array`.
         * @example
         *
         * var array = [{ 'x': 1 }, { 'x': 2 }, { 'x': 3 }, { 'x': 1 }];
         *
         * lodash_pullAllBy(array, [{ 'x': 1 }, { 'x': 3 }], 'x');
         * console.log(array);
         * // => [{ 'x': 2 }]
         */
        pullAllBy(
            array: any[]|List<any>,
            ...values: any[]
        ): any[];
    }

    //lodash_reverse DUMMY
    interface LoDashStatic {
        /**
         * Reverses `array` so that the first element becomes the last, the second
         * element becomes the second to last, and so on.
         *
         * **Note:** This method mutates `array` and is based on
         * [`Array#reverse`](https://mdn.io/Array/reverse).
         *
         * @memberOf _
         * @category Array
         * @returns {Array} Returns `array`.
         * @example
         *
         * var array = [1, 2, 3];
         *
         * lodash_reverse(array);
         * // => [3, 2, 1]
         *
         * console.log(array);
         * // => [3, 2, 1]
         */
        reverse(
            array: any[]|List<any>,
            ...values: any[]
        ): any[];
    }

    //lodash_sortedIndexOf
    interface LoDashStatic {
        /**
         * This method is like `lodash_indexOf` except that it performs a binary
         * search on a sorted `array`.
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The array to search.
         * @param {*} value The value to search for.
         * @returns {number} Returns the index of the matched value, else `-1`.
         * @example
         *
         * lodash_sortedIndexOf([1, 1, 2, 2], 2);
         * // => 2
         */
        sortedIndexOf<T>(
            array: List<T>,
            value: T
        ): number;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedIndexOf
         */
        sortedIndexOf(
            value: T
        ): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_sortedIndexOf
         */
        sortedIndexOf<TValue>(
            value: TValue
        ): number;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedIndexOf
         */
        sortedIndexOf(
            value: T
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_sortedIndexOf
         */
        sortedIndexOf<TValue>(
            value: TValue
        ): LoDashExplicitWrapper<number>;
    }

    //lodash_initial
    interface LoDashStatic {
        /**
         * Gets all but the last element of array.
         *
         * @param array The array to query.
         * @return Returns the slice of array.
         */
        initial<T>(array: List<T>): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_initial
         */
        initial(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_initial
         */
        initial<T>(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_initial
         */
        initial(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_initial
         */
        initial<T>(): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_intersection
    interface LoDashStatic {
        /**
         * Creates an array of unique values that are included in all of the provided arrays using SameValueZero for
         * equality comparisons.
         *
         * @param arrays The arrays to inspect.
         * @return Returns the new array of shared values.
         */
        intersection<T>(...arrays: (T[]|List<T>)[]): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_intersection
         */
        intersection<TResult>(...arrays: (TResult[]|List<TResult>)[]): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_intersection
         */
        intersection<TResult>(...arrays: (TResult[]|List<TResult>)[]): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_intersection
         */
        intersection<TResult>(...arrays: (TResult[]|List<TResult>)[]): LoDashExplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_intersection
         */
        intersection<TResult>(...arrays: (TResult[]|List<TResult>)[]): LoDashExplicitArrayWrapper<TResult>;
    }

    //lodash_last
    interface LoDashStatic {
        /**
         * Gets the last element of array.
         *
         * @param array The array to query.
         * @return Returns the last element of array.
         */
        last<T>(array: List<T>): T;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_last
         */
        last(): string;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_last
         */
        last(): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_last
         */
        last<T>(): T;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_last
         */
        last(): LoDashExplicitWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_last
         */
        last<T>(): T;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_last
         */
        last<T>(): T;
    }

    //lodash_lastIndexOf
    interface LoDashStatic {
        /**
         * This method is like lodash_indexOf except that it iterates over elements of array from right to left.
         *
         * @param array The array to search.
         * @param value The value to search for.
         * @param fromIndex The index to search from or true to perform a binary search on a sorted array.
         * @return Returns the index of the matched value, else -1.
         */
        lastIndexOf<T>(
            array: List<T>,
            value: T,
            fromIndex?: boolean|number
        ): number;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_lastIndexOf
         */
        lastIndexOf(
            value: T,
            fromIndex?: boolean|number
        ): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_lastIndexOf
         */
        lastIndexOf<TResult>(
            value: TResult,
            fromIndex?: boolean|number
        ): number;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_lastIndexOf
         */
        lastIndexOf(
            value: T,
            fromIndex?: boolean|number
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_lastIndexOf
         */
        lastIndexOf<TResult>(
            value: TResult,
            fromIndex?: boolean|number
        ): LoDashExplicitWrapper<number>;
    }

    //lodash_pull
    interface LoDashStatic {
        /**
         * Removes all provided values from array using SameValueZero for equality comparisons.
         *
         * Note: Unlike lodash_without, this method mutates array.
         *
         * @param array The array to modify.
         * @param values The values to remove.
         * @return Returns array.
         */
        pull<T>(
            array: T[],
            ...values: T[]
        ): T[];

        /**
         * @see lodash_pull
         */
        pull<T>(
            array: List<T>,
            ...values: T[]
        ): List<T>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_pull
         */
        pull(...values: T[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_pull
         */
        pull<TValue>(...values: TValue[]): LoDashImplicitObjectWrapper<List<TValue>>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_pull
         */
        pull(...values: T[]): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_pull
         */
        pull<TValue>(...values: TValue[]): LoDashExplicitObjectWrapper<List<TValue>>;
    }

    //lodash_pullAt
    interface LoDashStatic {
        /**
         * Removes elements from array corresponding to the given indexes and returns an array of the removed elements.
         * Indexes may be specified as an array of indexes or as individual arguments.
         *
         * Note: Unlike lodash_at, this method mutates array.
         *
         * @param array The array to modify.
         * @param indexes The indexes of elements to remove, specified as individual indexes or arrays of indexes.
         * @return Returns the new array of removed elements.
         */
        pullAt<T>(
            array: List<T>,
            ...indexes: (number|number[])[]
        ): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_pullAt
         */
        pullAt(...indexes: (number|number[])[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_pullAt
         */
        pullAt<T>(...indexes: (number|number[])[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_pullAt
         */
        pullAt(...indexes: (number|number[])[]): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_pullAt
         */
        pullAt<T>(...indexes: (number|number[])[]): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_remove
    interface LoDashStatic {
        /**
         * Removes all elements from array that predicate returns truthy for and returns an array of the removed
         * elements. The predicate is bound to thisArg and invoked with three arguments: (value, index, array).
         *
         * If a property name is provided for predicate the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for predicate the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * Note: Unlike lodash_filter, this method mutates array.
         *
         * @param array The array to modify.
         * @param predicate The function invoked per iteration.
         * @param thisArg The this binding of predicate.
         * @return Returns the new array of removed elements.
         */
        remove<T>(
            array: List<T>,
            predicate?: ListIterator<T, boolean>
        ): T[];

        /**
         * @see lodash_remove
         */
        remove<T>(
            array: List<T>,
            predicate?: string
        ): T[];

        /**
         * @see lodash_remove
         */
        remove<W, T>(
            array: List<T>,
            predicate?: W
        ): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_remove
         */
        remove(
            predicate?: ListIterator<T, boolean>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_remove
         */
        remove(
            predicate?: string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_remove
         */
        remove<W>(
            predicate?: W
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_remove
         */
        remove<TResult>(
            predicate?: ListIterator<TResult, boolean>
        ): LoDashImplicitArrayWrapper<TResult>;

        /**
         * @see lodash_remove
         */
        remove<TResult>(
            predicate?: string
        ): LoDashImplicitArrayWrapper<TResult>;

        /**
         * @see lodash_remove
         */
        remove<W, TResult>(
            predicate?: W
        ): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_remove
         */
        remove(
            predicate?: ListIterator<T, boolean>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_remove
         */
        remove(
            predicate?: string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_remove
         */
        remove<W>(
            predicate?: W
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_remove
         */
        remove<TResult>(
            predicate?: ListIterator<TResult, boolean>
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_remove
         */
        remove<TResult>(
            predicate?: string
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_remove
         */
        remove<W, TResult>(
            predicate?: W
        ): LoDashExplicitArrayWrapper<TResult>;
    }

    //lodash_tail
    interface LoDashStatic {
        /**
         * Gets all but the first element of array.
         *
         * @alias lodash_tail
         *
         * @param array The array to query.
         * @return Returns the slice of array.
         */
        tail<T>(array: List<T>): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_tail
         */
        tail(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_tail
         */
        tail<T>(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_tail
         */
        tail(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_tail
         */
        tail<T>(): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_slice
    interface LoDashStatic {
        /**
         * Creates a slice of array from start up to, but not including, end.
         *
         * @param array The array to slice.
         * @param start The start position.
         * @param end The end position.
         * @return Returns the slice of array.
         */
        slice<T>(
            array: T[],
            start?: number,
            end?: number
        ): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_slice
         */
        slice(
            start?: number,
            end?: number
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_slice
         */
        slice(
            start?: number,
            end?: number
        ): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_sortedIndex
    interface LoDashStatic {
        /**
         * Uses a binary search to determine the lowest index at which `value` should
         * be inserted into `array` in order to maintain its sort order.
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The sorted array to inspect.
         * @param {*} value The value to evaluate.
         * @returns {number} Returns the index at which `value` should be inserted into `array`.
         * @example
         *
         * lodash_sortedIndex([30, 50], 40);
         * // => 1
         *
         * lodash_sortedIndex([4, 5], 4);
         * // => 0
         */
        sortedIndex<T, TSort>(
            array: List<T>,
            value: T
        ): number;

        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<T>(
            array: List<T>,
            value: T
        ): number;

        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<T>(
            array: List<T>,
            value: T
        ): number;

        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<W, T>(
            array: List<T>,
            value: T
        ): number;

        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<T>(
            array: List<T>,
            value: T
        ): number;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<TSort>(
            value: string
        ): number;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<TSort>(
            value: T
        ): number;

        /**
         * @see lodash_sortedIndex
         */
        sortedIndex(
            value: T
        ): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<T, TSort>(
            value: T
        ): number;

        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<T>(
            value: T
        ): number;

        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<W, T>(
            value: T
        ): number;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<TSort>(
            value: string
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<TSort>(
            value: T
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedIndex
         */
        sortedIndex(
            value: T
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<W>(
            value: T
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<T, TSort>(
            value: T
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<T>(
            value: T
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedIndex
         */
        sortedIndex<W, T>(
            value: T
        ): LoDashExplicitWrapper<number>;


    }

    //lodash_sortedIndexBy
    interface LoDashStatic {
        /**
         * This method is like `lodash_sortedIndex` except that it accepts `iteratee`
         * which is invoked for `value` and each element of `array` to compute their
         * sort ranking. The iteratee is invoked with one argument: (value).
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The sorted array to inspect.
         * @param {*} value The value to evaluate.
         * @param {Function|Object|string} [iteratee=lodash_identity] The iteratee invoked per element.
         * @returns {number} Returns the index at which `value` should be inserted into `array`.
         * @example
         *
         * var dict = { 'thirty': 30, 'forty': 40, 'fifty': 50 };
         *
         * lodash_sortedIndexBy(['thirty', 'fifty'], 'forty', lodash_propertyOf(dict));
         * // => 1
         *
         * // using the `lodash_property` iteratee shorthand
         * lodash_sortedIndexBy([{ 'x': 4 }, { 'x': 5 }], { 'x': 4 }, 'x');
         * // => 0
         */
        sortedIndexBy<T, TSort>(
            array: List<T>,
            value: T,
            iteratee: (x: T) => TSort
        ): number;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<T>(
            array: List<T>,
            value: T,
            iteratee: (x: T) => any
        ): number;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<T>(
            array: List<T>,
            value: T,
            iteratee: string
        ): number;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<W, T>(
            array: List<T>,
            value: T,
            iteratee: W
        ): number;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<T>(
            array: List<T>,
            value: T,
            iteratee: Object
        ): number;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<TSort>(
            value: string,
            iteratee: (x: string) => TSort
        ): number;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<TSort>(
            value: T,
            iteratee: (x: T) => TSort
        ): number;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy(
            value: T,
            iteratee: string
        ): number;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<W>(
            value: T,
            iteratee: W
        ): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<T, TSort>(
            value: T,
            iteratee: (x: T) => TSort
        ): number;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<T>(
            value: T,
            iteratee: (x: T) => any
        ): number;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<T>(
            value: T,
            iteratee: string
        ): number;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<W, T>(
            value: T,
            iteratee: W
        ): number;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<T>(
            value: T,
            iteratee: Object
        ): number;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<TSort>(
            value: string,
            iteratee: (x: string) => TSort
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<TSort>(
            value: T,
            iteratee: (x: T) => TSort
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy(
            value: T,
            iteratee: string
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<W>(
            value: T,
            iteratee: W
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<T, TSort>(
            value: T,
            iteratee: (x: T) => TSort
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<T>(
            value: T,
            iteratee: (x: T) => any
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<T>(
            value: T,
            iteratee: string
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<W, T>(
            value: T,
            iteratee: W
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedIndexBy
         */
        sortedIndexBy<T>(
            value: T,
            iteratee: Object
        ): LoDashExplicitWrapper<number>;
    }

    //lodash_sortedLastIndex
    interface LoDashStatic {
        /**
         * This method is like `lodash_sortedIndex` except that it returns the highest
         * index at which `value` should be inserted into `array` in order to
         * maintain its sort order.
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The sorted array to inspect.
         * @param {*} value The value to evaluate.
         * @returns {number} Returns the index at which `value` should be inserted into `array`.
         * @example
         *
         * lodash_sortedLastIndex([4, 5], 4);
         * // => 1
         */
        sortedLastIndex<T, TSort>(
            array: List<T>,
            value: T
        ): number;

        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<T>(
            array: List<T>,
            value: T
        ): number;

        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<T>(
            array: List<T>,
            value: T
        ): number;

        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<W, T>(
            array: List<T>,
            value: T
        ): number;

        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<T>(
            array: List<T>,
            value: T
        ): number;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<TSort>(
            value: string
        ): number;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<TSort>(
            value: T
        ): number;

        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex(
            value: T
        ): number;

        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<W>(
            value: T
        ): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<T, TSort>(
            value: T
        ): number;

        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<T>(
            value: T
        ): number;

        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<W, T>(
            value: T
        ): number;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<TSort>(
            value: string
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<TSort>(
            value: T
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex(
            value: T
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<T, TSort>(
            value: T
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<T>(
            value: T
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedLastIndex
         */
        sortedLastIndex<W, T>(
            value: T
        ): LoDashExplicitWrapper<number>;
    }

    //lodash_sortedLastIndexBy
    interface LoDashStatic {
        /**
         * This method is like `lodash_sortedLastIndex` except that it accepts `iteratee`
         * which is invoked for `value` and each element of `array` to compute their
         * sort ranking. The iteratee is invoked with one argument: (value).
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The sorted array to inspect.
         * @param {*} value The value to evaluate.
         * @param {Function|Object|string} [iteratee=lodash_identity] The iteratee invoked per element.
         * @returns {number} Returns the index at which `value` should be inserted into `array`.
         * @example
         *
         * // using the `lodash_property` iteratee shorthand
         * lodash_sortedLastIndexBy([{ 'x': 4 }, { 'x': 5 }], { 'x': 4 }, 'x');
         * // => 1
         */
        sortedLastIndexBy<T, TSort>(
            array: List<T>,
            value: T,
            iteratee: (x: T) => TSort
        ): number;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<T>(
            array: List<T>,
            value: T,
            iteratee: (x: T) => any
        ): number;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<T>(
            array: List<T>,
            value: T,
            iteratee: string
        ): number;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<W, T>(
            array: List<T>,
            value: T,
            iteratee: W
        ): number;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<T>(
            array: List<T>,
            value: T,
            iteratee: Object
        ): number;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<TSort>(
            value: string,
            iteratee: (x: string) => TSort
        ): number;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<TSort>(
            value: T,
            iteratee: (x: T) => TSort
        ): number;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy(
            value: T,
            iteratee: string
        ): number;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<W>(
            value: T,
            iteratee: W
        ): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<T, TSort>(
            value: T,
            iteratee: (x: T) => TSort
        ): number;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<T>(
            value: T,
            iteratee: (x: T) => any
        ): number;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<T>(
            value: T,
            iteratee: string
        ): number;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<W, T>(
            value: T,
            iteratee: W
        ): number;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<T>(
            value: T,
            iteratee: Object
        ): number;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<TSort>(
            value: string,
            iteratee: (x: string) => TSort
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<TSort>(
            value: T,
            iteratee: (x: T) => TSort
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy(
            value: T,
            iteratee: string
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<W>(
            value: T,
            iteratee: W
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<T, TSort>(
            value: T,
            iteratee: (x: T) => TSort
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<T>(
            value: T,
            iteratee: (x: T) => any
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<T>(
            value: T,
            iteratee: string
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<W, T>(
            value: T,
            iteratee: W
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sortedLastIndexBy
         */
        sortedLastIndexBy<T>(
            value: T,
            iteratee: Object
        ): LoDashExplicitWrapper<number>;
    }

    //lodash_sortedLastIndexOf DUMMY
    interface LoDashStatic {
        /**
         * This method is like `lodash_lastIndexOf` except that it performs a binary
         * search on a sorted `array`.
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The array to search.
         * @param {*} value The value to search for.
         * @returns {number} Returns the index of the matched value, else `-1`.
         * @example
         *
         * lodash_sortedLastIndexOf([1, 1, 2, 2], 2);
         * // => 3
         */
        sortedLastIndexOf(
            array: any[]|List<any>,
            ...values: any[]
        ): any[];
    }

    //lodash_tail
    interface LoDashStatic {
        /**
         * @see lodash_rest
         */
        tail<T>(array: List<T>): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_rest
         */
        tail(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_rest
         */
        tail<T>(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_rest
         */
        tail(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_rest
         */
        tail<T>(): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_take
    interface LoDashStatic {
        /**
         * Creates a slice of array with n elements taken from the beginning.
         *
         * @param array The array to query.
         * @param n The number of elements to take.
         * @return Returns the slice of array.
         */
        take<T>(
            array: List<T>,
            n?: number
        ): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_take
         */
        take(n?: number): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_take
         */
        take<TResult>(n?: number): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_take
         */
        take(n?: number): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_take
         */
        take<TResult>(n?: number): LoDashExplicitArrayWrapper<TResult>;
    }

    //lodash_takeRight
    interface LoDashStatic {
        /**
         * Creates a slice of array with n elements taken from the end.
         *
         * @param array The array to query.
         * @param n The number of elements to take.
         * @return Returns the slice of array.
         */
        takeRight<T>(
            array: List<T>,
            n?: number
        ): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_takeRight
         */
        takeRight(n?: number): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_takeRight
         */
        takeRight<TResult>(n?: number): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_takeRight
         */
        takeRight(n?: number): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_takeRight
         */
        takeRight<TResult>(n?: number): LoDashExplicitArrayWrapper<TResult>;
    }

    //lodash_takeRightWhile
    interface LoDashStatic {
        /**
         * Creates a slice of array with elements taken from the end. Elements are taken until predicate returns
         * falsey. The predicate is bound to thisArg and invoked with three arguments: (value, index, array).
         *
         * If a property name is provided for predicate the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for predicate the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * @param array The array to query.
         * @param predicate The function invoked per iteration.
         * @param thisArg The this binding of predicate.
         * @return Returns the slice of array.
         */
        takeRightWhile<TValue>(
            array: List<TValue>,
            predicate?: ListIterator<TValue, boolean>
        ): TValue[];

        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile<TValue>(
            array: List<TValue>,
            predicate?: string
        ): TValue[];

        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile<TWhere, TValue>(
            array: List<TValue>,
            predicate?: TWhere
        ): TValue[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile(
            predicate?: ListIterator<T, boolean>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile(
            predicate?: string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile<TWhere>(
            predicate?: TWhere
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile<TValue>(
            predicate?: ListIterator<TValue, boolean>
        ): LoDashImplicitArrayWrapper<TValue>;

        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile<TValue>(
            predicate?: string
        ): LoDashImplicitArrayWrapper<TValue>;

        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile<TWhere, TValue>(
            predicate?: TWhere
        ): LoDashImplicitArrayWrapper<TValue>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile(
            predicate?: ListIterator<T, boolean>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile(
            predicate?: string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile<TWhere>(
            predicate?: TWhere
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile<TValue>(
            predicate?: ListIterator<TValue, boolean>
        ): LoDashExplicitArrayWrapper<TValue>;

        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile<TValue>(
            predicate?: string
        ): LoDashExplicitArrayWrapper<TValue>;

        /**
         * @see lodash_takeRightWhile
         */
        takeRightWhile<TWhere, TValue>(
            predicate?: TWhere
        ): LoDashExplicitArrayWrapper<TValue>;
    }

    //lodash_takeWhile
    interface LoDashStatic {
        /**
         * Creates a slice of array with elements taken from the beginning. Elements are taken until predicate returns
         * falsey. The predicate is bound to thisArg and invoked with three arguments: (value, index, array).
         *
         * If a property name is provided for predicate the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for predicate the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * @param array The array to query.
         * @param predicate The function invoked per iteration.
         * @param thisArg The this binding of predicate.
         * @return Returns the slice of array.
         */
        takeWhile<TValue>(
            array: List<TValue>,
            predicate?: ListIterator<TValue, boolean>
        ): TValue[];

        /**
         * @see lodash_takeWhile
         */
        takeWhile<TValue>(
            array: List<TValue>,
            predicate?: string
        ): TValue[];

        /**
         * @see lodash_takeWhile
         */
        takeWhile<TWhere, TValue>(
            array: List<TValue>,
            predicate?: TWhere
        ): TValue[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_takeWhile
         */
        takeWhile(
            predicate?: ListIterator<T, boolean>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_takeWhile
         */
        takeWhile(
            predicate?: string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_takeWhile
         */
        takeWhile<TWhere>(
            predicate?: TWhere
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_takeWhile
         */
        takeWhile<TValue>(
            predicate?: ListIterator<TValue, boolean>
        ): LoDashImplicitArrayWrapper<TValue>;

        /**
         * @see lodash_takeWhile
         */
        takeWhile<TValue>(
            predicate?: string
        ): LoDashImplicitArrayWrapper<TValue>;

        /**
         * @see lodash_takeWhile
         */
        takeWhile<TWhere, TValue>(
            predicate?: TWhere
        ): LoDashImplicitArrayWrapper<TValue>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_takeWhile
         */
        takeWhile(
            predicate?: ListIterator<T, boolean>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_takeWhile
         */
        takeWhile(
            predicate?: string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_takeWhile
         */
        takeWhile<TWhere>(
            predicate?: TWhere
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_takeWhile
         */
        takeWhile<TValue>(
            predicate?: ListIterator<TValue, boolean>
        ): LoDashExplicitArrayWrapper<TValue>;

        /**
         * @see lodash_takeWhile
         */
        takeWhile<TValue>(
            predicate?: string
        ): LoDashExplicitArrayWrapper<TValue>;

        /**
         * @see lodash_takeWhile
         */
        takeWhile<TWhere, TValue>(
            predicate?: TWhere
        ): LoDashExplicitArrayWrapper<TValue>;
    }

    //lodash_union
    interface LoDashStatic {
        /**
         * Creates an array of unique values, in order, from all of the provided arrays using SameValueZero for
         * equality comparisons.
         *
         * @param arrays The arrays to inspect.
         * @return Returns the new array of combined values.
         */
        union<T>(...arrays: List<T>[]): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_union
         */
        union(...arrays: List<T>[]): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_union
         */
        union<T>(...arrays: List<T>[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_union
         */
        union<T>(...arrays: List<T>[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_union
         */
        union(...arrays: List<T>[]): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_union
         */
        union<T>(...arrays: List<T>[]): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_union
         */
        union<T>(...arrays: List<T>[]): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_unionBy
    interface LoDashStatic {
        /**
         * This method is like `lodash_union` except that it accepts `iteratee` which is
         * invoked for each element of each `arrays` to generate the criterion by which
         * uniqueness is computed. The iteratee is invoked with one argument: (value).
         *
         * @param arrays The arrays to inspect.
         * @param iteratee The iteratee invoked per element.
         * @return Returns the new array of combined values.
         */
        unionBy<T>(
            arrays: T[]|List<T>,
            iteratee?: (value: T) => any
        ): T[];

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays: T[]|List<T>,
            iteratee?: W
        ): T[];

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays1: T[]|List<T>,
            arrays2: T[]|List<T>,
            iteratee?: (value: T) => any
        ): T[];

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays1: T[]|List<T>,
            arrays2: T[]|List<T>,
            iteratee?: W
        ): T[];

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays1: T[]|List<T>,
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            iteratee?: (value: T) => any
        ): T[];

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays1: T[]|List<T>,
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            iteratee?: W
        ): T[];

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays1: T[]|List<T>,
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            iteratee?: (value: T) => any
        ): T[];

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays1: T[]|List<T>,
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            iteratee?: W
        ): T[];

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays1: T[]|List<T>,
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            arrays5: T[]|List<T>,
            iteratee?: (value: T) => any
        ): T[];

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays1: T[]|List<T>,
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            arrays5: T[]|List<T>,
            iteratee?: W
        ): T[];

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays: T[]|List<T>,
            ...iteratee: any[]
        ): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            iteratee?: (value: T) => any
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            arrays5: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            arrays5: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            ...iteratee: any[]
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            iteratee?: (value: T) => any
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            arrays5: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            arrays5: T[]|List<T>,
            iteratee?: W
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            ...iteratee: any[]
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            iteratee?: (value: T) => any
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            arrays5: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            arrays5: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            ...iteratee: any[]
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            iteratee?: (value: T) => any
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            arrays5: T[]|List<T>,
            iteratee?: (value: T) => any
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T, W extends Object>(
            arrays2: T[]|List<T>,
            arrays3: T[]|List<T>,
            arrays4: T[]|List<T>,
            arrays5: T[]|List<T>,
            iteratee?: W
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_unionBy
         */
        unionBy<T>(
            ...iteratee: any[]
        ): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_uniq
    interface LoDashStatic {
        /**
         * Creates a duplicate-free version of an array, using
         * [`SameValueZero`](http://ecma-international.org/ecma-262/6.0/#sec-samevaluezero)
         * for equality comparisons, in which only the first occurrence of each element
         * is kept.
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The array to inspect.
         * @returns {Array} Returns the new duplicate free array.
         * @example
         *
         * lodash_uniq([2, 1, 2]);
         * // => [2, 1]
         */
        uniq<T>(
            array: List<T>
        ): T[];

        /**
         * @see lodash_uniq
         */
        uniq<T, TSort>(
            array: List<T>
        ): T[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_uniq
         */
        uniq<TSort>(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_uniq
         */
        uniq<TSort>(): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_uniq
         */
        uniq(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        uniq<T>(): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_uniq
         */
        uniq<T, TSort>(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_uniq
         */
        uniq<TSort>(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_uniq
         */
        uniq<TSort>(): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_uniq
         */
        uniq(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_uniq
         */
        uniq<T>(): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_uniq
         */
        uniq<T, TSort>(): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_uniqBy
    interface LoDashStatic {
        /**
         * This method is like `lodash_uniq` except that it accepts `iteratee` which is
         * invoked for each element in `array` to generate the criterion by which
         * uniqueness is computed. The iteratee is invoked with one argument: (value).
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The array to inspect.
         * @param {Function|Object|string} [iteratee=lodash_identity] The iteratee invoked per element.
         * @returns {Array} Returns the new duplicate free array.
         * @example
         *
         * lodash_uniqBy([2.1, 1.2, 2.3], Math.floor);
         * // => [2.1, 1.2]
         *
         * // using the `lodash_property` iteratee shorthand
         * lodash_uniqBy([{ 'x': 1 }, { 'x': 2 }, { 'x': 1 }], 'x');
         * // => [{ 'x': 1 }, { 'x': 2 }]
         */
        uniqBy<T>(
            array: List<T>,
            iteratee: ListIterator<T, any>
        ): T[];

        /**
         * @see lodash_uniqBy
         */
        uniqBy<T, TSort>(
            array: List<T>,
            iteratee: ListIterator<T, TSort>
        ): T[];

        /**
         * @see lodash_uniqBy
         */
        uniqBy<T>(
            array: List<T>,
            iteratee: string
        ): T[];

        /**
         * @see lodash_uniqBy
         */
        uniqBy<T>(
            array: List<T>,
            iteratee: Object
        ): T[];

        /**
         * @see lodash_uniqBy
         */
        uniqBy<TWhere extends {}, T>(
            array: List<T>,
            iteratee: TWhere
        ): T[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_uniqBy
         */
        uniqBy<TSort>(
            iteratee: ListIterator<T, TSort>
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_uniqBy
         */
        uniqBy<TSort>(
            iteratee: ListIterator<T, TSort>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_uniqBy
         */
        uniqBy(
            iteratee: string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_uniqBy
         */
        uniqBy<TWhere extends {}>(
            iteratee: TWhere
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_uniqBy
         */
        uniqBy<T>(
            iteratee: ListIterator<T, any>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_uniqBy
         */
        uniqBy<T, TSort>(
            iteratee: ListIterator<T, TSort>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_uniqBy
         */
        uniqBy<T>(
            iteratee: string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_uniqBy
         */
        uniqBy<T>(
            iteratee: Object
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_uniqBy
         */
        uniqBy<TWhere extends {}, T>(
            iteratee: TWhere
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_uniqBy
         */
        uniqBy<TSort>(
            iteratee: ListIterator<T, TSort>
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_uniqBy
         */
        uniqBy<TSort>(
            iteratee: ListIterator<T, TSort>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_uniqBy
         */
        uniqBy(
            iteratee: string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_uniqBy
         */
        uniqBy<TWhere extends {}>(
            iteratee: TWhere
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_uniqBy
         */
        uniqBy<T>(
            iteratee: ListIterator<T, any>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_uniqBy
         */
        uniqBy<T, TSort>(
            iteratee: ListIterator<T, TSort>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_uniqBy
         */
        uniqBy<T>(
            iteratee: string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_uniqBy
         */
        uniqBy<T>(
            iteratee: Object
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_uniqBy
         */
        uniqBy<TWhere extends {}, T>(
            iteratee: TWhere
        ): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_sortedUniq
    interface LoDashStatic {
        /**
         * This method is like `lodash_uniq` except that it's designed and optimized
         * for sorted arrays.
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The array to inspect.
         * @returns {Array} Returns the new duplicate free array.
         * @example
         *
         * lodash_sortedUniq([1, 1, 2]);
         * // => [1, 2]
         */
        sortedUniq<T>(
            array: List<T>
        ): T[];

        /**
         * @see lodash_sortedUniq
         */
        sortedUniq<T, TSort>(
            array: List<T>
        ): T[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_sortedUniq
         */
        sortedUniq<TSort>(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedUniq
         */
        sortedUniq<TSort>(): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniq
         */
        sortedUniq(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        sortedUniq<T>(): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniq
         */
        sortedUniq<T, TSort>(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_sortedUniq
         */
        sortedUniq<TSort>(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedUniq
         */
        sortedUniq<TSort>(): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniq
         */
        sortedUniq(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_sortedUniq
         */
        sortedUniq<T>(): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniq
         */
        sortedUniq<T, TSort>(): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_sortedUniqBy
    interface LoDashStatic {
        /**
         * This method is like `lodash_uniqBy` except that it's designed and optimized
         * for sorted arrays.
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The array to inspect.
         * @param {Function} [iteratee] The iteratee invoked per element.
         * @returns {Array} Returns the new duplicate free array.
         * @example
         *
         * lodash_sortedUniqBy([1.1, 1.2, 2.3, 2.4], Math.floor);
         * // => [1.1, 2.2]
         */
        sortedUniqBy<T>(
            array: List<T>,
            iteratee: ListIterator<T, any>
        ): T[];

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<T, TSort>(
            array: List<T>,
            iteratee: ListIterator<T, TSort>
        ): T[];

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<T>(
            array: List<T>,
            iteratee: string
        ): T[];

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<T>(
            array: List<T>,
            iteratee: Object
        ): T[];

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<TWhere extends {}, T>(
            array: List<T>,
            iteratee: TWhere
        ): T[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<TSort>(
            iteratee: ListIterator<T, TSort>
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<TSort>(
            iteratee: ListIterator<T, TSort>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy(
            iteratee: string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<TWhere extends {}>(
            iteratee: TWhere
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<T>(
            iteratee: ListIterator<T, any>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<T, TSort>(
            iteratee: ListIterator<T, TSort>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<T>(
            iteratee: string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<T>(
            iteratee: Object
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<TWhere extends {}, T>(
            iteratee: TWhere
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<TSort>(
            iteratee: ListIterator<T, TSort>
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<TSort>(
            iteratee: ListIterator<T, TSort>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy(
            iteratee: string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<TWhere extends {}>(
            iteratee: TWhere
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<T>(
            iteratee: ListIterator<T, any>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<T, TSort>(
            iteratee: ListIterator<T, TSort>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<T>(
            iteratee: string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<T>(
            iteratee: Object
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortedUniqBy
         */
        sortedUniqBy<TWhere extends {}, T>(
            iteratee: TWhere
        ): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_unionWith DUMMY
    interface LoDashStatic {
        /**
         * This method is like `lodash_union` except that it accepts `comparator` which
         * is invoked to compare elements of `arrays`. The comparator is invoked
         * with two arguments: (arrVal, othVal).
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {...Array} [arrays] The arrays to inspect.
         * @param {Function} [comparator] The comparator invoked per element.
         * @returns {Array} Returns the new array of combined values.
         * @example
         *
         * var objects = [{ 'x': 1, 'y': 2 }, { 'x': 2, 'y': 1 }];
         * var others = [{ 'x': 1, 'y': 1 }, { 'x': 1, 'y': 2 }];
         *
         * lodash_unionWith(objects, others, lodash_isEqual);
         * // => [{ 'x': 1, 'y': 2 }, { 'x': 2, 'y': 1 }, { 'x': 1, 'y': 1 }]
         */
        unionWith(
            array: any[]|List<any>,
            ...values: any[]
        ): any[];
    }

    //lodash_uniqWith DUMMY
    interface LoDashStatic {
        /**
         * This method is like `lodash_uniq` except that it accepts `comparator` which
         * is invoked to compare elements of `array`. The comparator is invoked with
         * two arguments: (arrVal, othVal).
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {Array} array The array to inspect.
         * @param {Function} [comparator] The comparator invoked per element.
         * @returns {Array} Returns the new duplicate free array.
         * @example
         *
         * var objects = [{ 'x': 1, 'y': 2 }, { 'x': 2, 'y': 1 },  { 'x': 1, 'y': 2 }];
         *
         * lodash_uniqWith(objects, lodash_isEqual);
         * // => [{ 'x': 1, 'y': 2 }, { 'x': 2, 'y': 1 }]
         */
        uniqWith(
            array: any[]|List<any>,
            ...values: any[]
        ): any[];
    }

    //lodash_unzip
    interface LoDashStatic {
        /**
         * This method is like lodash_zip except that it accepts an array of grouped elements and creates an array
         * regrouping the elements to their pre-zip configuration.
         *
         * @param array The array of grouped elements to process.
         * @return Returns the new array of regrouped elements.
         */
        unzip<T>(array: List<List<T>>): T[][];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_unzip
         */
        unzip<T>(): LoDashImplicitArrayWrapper<T[]>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_unzip
         */
        unzip<T>(): LoDashImplicitArrayWrapper<T[]>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_unzip
         */
        unzip<T>(): LoDashExplicitArrayWrapper<T[]>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_unzip
         */
        unzip<T>(): LoDashExplicitArrayWrapper<T[]>;
    }

    //lodash_unzipWith
    interface LoDashStatic {
        /**
         * This method is like lodash_unzip except that it accepts an iteratee to specify how regrouped values should be
         * combined. The iteratee is bound to thisArg and invoked with four arguments: (accumulator, value, index,
         * group).
         *
         * @param array The array of grouped elements to process.
         * @param iteratee The function to combine regrouped values.
         * @param thisArg The this binding of iteratee.
         * @return Returns the new array of regrouped elements.
         */
        unzipWith<TArray, TResult>(
            array: List<List<TArray>>,
            iteratee?: MemoIterator<TArray, TResult>
        ): TResult[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_unzipWith
         */
        unzipWith<TArr, TResult>(
            iteratee?: MemoIterator<TArr, TResult>
        ): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_unzipWith
         */
        unzipWith<TArr, TResult>(
            iteratee?: MemoIterator<TArr, TResult>
        ): LoDashImplicitArrayWrapper<TResult>;
    }

    //lodash_without
    interface LoDashStatic {
        /**
         * Creates an array excluding all provided values using SameValueZero for equality comparisons.
         *
         * @param array The array to filter.
         * @param values The values to exclude.
         * @return Returns the new array of filtered values.
         */
        without<T>(
            array: List<T>,
            ...values: T[]
        ): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_without
         */
        without(...values: T[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_without
         */
        without<T>(...values: T[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_without
         */
        without(...values: T[]): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_without
         */
        without<T>(...values: T[]): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_xor
    interface LoDashStatic {
        /**
         * Creates an array of unique values that is the symmetric difference of the provided arrays.
         *
         * @param arrays The arrays to inspect.
         * @return Returns the new array of values.
         */
        xor<T>(...arrays: List<T>[]): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_xor
         */
        xor(...arrays: List<T>[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_xor
         */
        xor<T>(...arrays: List<T>[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_xor
         */
        xor(...arrays: List<T>[]): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_xor
         */
        xor<T>(...arrays: List<T>[]): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_xorBy DUMMY
    interface LoDashStatic {
        /**
         * This method is like `lodash_xor` except that it accepts `iteratee` which is
         * invoked for each element of each `arrays` to generate the criterion by which
         * uniqueness is computed. The iteratee is invoked with one argument: (value).
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {...Array} [arrays] The arrays to inspect.
         * @param {Function|Object|string} [iteratee=lodash_identity] The iteratee invoked per element.
         * @returns {Array} Returns the new array of values.
         * @example
         *
         * lodash_xorBy([2.1, 1.2], [4.3, 2.4], Math.floor);
         * // => [1.2, 4.3]
         *
         * // using the `lodash_property` iteratee shorthand
         * lodash_xorBy([{ 'x': 1 }], [{ 'x': 2 }, { 'x': 1 }], 'x');
         * // => [{ 'x': 2 }]
         */
        xorBy(
            array: any[]|List<any>,
            ...values: any[]
        ): any[];
    }

    //lodash_xorWith DUMMY
    interface LoDashStatic {
        /**
         * This method is like `lodash_xor` except that it accepts `comparator` which is
         * invoked to compare elements of `arrays`. The comparator is invoked with
         * two arguments: (arrVal, othVal).
         *
         * @static
         * @memberOf _
         * @category Array
         * @param {...Array} [arrays] The arrays to inspect.
         * @param {Function} [comparator] The comparator invoked per element.
         * @returns {Array} Returns the new array of values.
         * @example
         *
         * var objects = [{ 'x': 1, 'y': 2 }, { 'x': 2, 'y': 1 }];
         * var others = [{ 'x': 1, 'y': 1 }, { 'x': 1, 'y': 2 }];
         *
         * lodash_xorWith(objects, others, lodash_isEqual);
         * // => [{ 'x': 2, 'y': 1 }, { 'x': 1, 'y': 1 }]
         */
        xorWith(
            array: any[]|List<any>,
            ...values: any[]
        ): any[];
    }

    //lodash_zip
    interface LoDashStatic {
        /**
         * Creates an array of grouped elements, the first of which contains the first elements of the given arrays,
         * the second of which contains the second elements of the given arrays, and so on.
         *
         * @param arrays The arrays to process.
         * @return Returns the new array of grouped elements.
         */
        zip<T>(...arrays: List<T>[]): T[][];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_zip
         */
        zip<T>(...arrays: List<T>[]): LoDashImplicitArrayWrapper<T[]>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_zip
         */
        zip<T>(...arrays: List<T>[]): LoDashImplicitArrayWrapper<T[]>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_zip
         */
        zip<T>(...arrays: List<T>[]): LoDashExplicitArrayWrapper<T[]>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_zip
         */
        zip<T>(...arrays: List<T>[]): LoDashExplicitArrayWrapper<T[]>;
    }

    //lodash_zipObject
    interface LoDashStatic {
        /**
         * The inverse of lodash_pairs; this method returns an object composed from arrays of property names and values.
         * Provide either a single two dimensional array, e.g. [[key1, value1], [key2, value2]] or two arrays, one of
         * property names and one of corresponding values.
         *
         * @param props The property names.
         * @param values The property values.
         * @return Returns the new object.
         */
        zipObject<TValues, TResult extends {}>(
            props: List<StringRepresentable>|List<List<any>>,
            values?: List<TValues>
        ): TResult;

        /**
         * @see lodash_zipObject
         */
        zipObject<TResult extends {}>(
            props: List<StringRepresentable>|List<List<any>>,
            values?: List<any>
        ): TResult;

        /**
         * @see lodash_zipObject
         */
        zipObject(
            props: List<StringRepresentable>|List<List<any>>,
            values?: List<any>
        ): Dictionary<any>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_zipObject
         */
        zipObject<TValues, TResult extends {}>(
            values?: List<TValues>
        ): LoDashImplicitObjectWrapper<TResult>;

        /**
         * @see lodash_zipObject
         */
        zipObject<TResult extends {}>(
            values?: List<any>
        ): LoDashImplicitObjectWrapper<TResult>;

        /**
         * @see lodash_zipObject
         */
        zipObject(
            values?: List<any>
        ): LoDashImplicitObjectWrapper<Dictionary<any>>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_zipObject
         */
        zipObject<TValues, TResult extends {}>(
            values?: List<TValues>
        ): LoDashImplicitObjectWrapper<TResult>;

        /**
         * @see lodash_zipObject
         */
        zipObject<TResult extends {}>(
            values?: List<any>
        ): LoDashImplicitObjectWrapper<TResult>;

        /**
         * @see lodash_zipObject
         */
        zipObject(
            values?: List<any>
        ): LoDashImplicitObjectWrapper<Dictionary<any>>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_zipObject
         */
        zipObject<TValues, TResult extends {}>(
            values?: List<TValues>
        ): LoDashExplicitObjectWrapper<TResult>;

        /**
         * @see lodash_zipObject
         */
        zipObject<TResult extends {}>(
            values?: List<any>
        ): LoDashExplicitObjectWrapper<TResult>;

        /**
         * @see lodash_zipObject
         */
        zipObject(
            values?: List<any>
        ): LoDashExplicitObjectWrapper<Dictionary<any>>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_zipObject
         */
        zipObject<TValues, TResult extends {}>(
            values?: List<TValues>
        ): LoDashExplicitObjectWrapper<TResult>;

        /**
         * @see lodash_zipObject
         */
        zipObject<TResult extends {}>(
            values?: List<any>
        ): LoDashExplicitObjectWrapper<TResult>;

        /**
         * @see lodash_zipObject
         */
        zipObject(
            values?: List<any>
        ): LoDashExplicitObjectWrapper<Dictionary<any>>;
    }

    //lodash_zipWith
    interface LoDashStatic {
        /**
         * This method is like lodash_zip except that it accepts an iteratee to specify how grouped values should be
         * combined. The iteratee is bound to thisArg and invoked with four arguments: (accumulator, value, index,
         * group).
         * @param {...Array} [arrays] The arrays to process.
         * @param {Function} [iteratee] The function to combine grouped values.
         * @param {*} [thisArg] The `this` binding of `iteratee`.
         * @return Returns the new array of grouped elements.
         */
        zipWith<TResult>(...args: any[]): TResult[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_zipWith
         */
        zipWith<TResult>(...args: any[]): LoDashImplicitArrayWrapper<TResult>;
    }

    /*********
     * Chain *
     *********/

        //lodash_chain
    interface LoDashStatic {
        /**
         * Creates a lodash object that wraps value with explicit method chaining enabled.
         *
         * @param value The value to wrap.
         * @return Returns the new lodash wrapper instance.
         */
        chain(value: number): LoDashExplicitWrapper<number>;
        chain(value: string): LoDashExplicitWrapper<string>;
        chain(value: boolean): LoDashExplicitWrapper<boolean>;
        chain<T>(value: T[]): LoDashExplicitArrayWrapper<T>;
        chain<T extends {}>(value: T): LoDashExplicitObjectWrapper<T>;
        chain(value: any): LoDashExplicitWrapper<any>;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_chain
         */
        chain(): LoDashExplicitWrapper<T>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_chain
         */
        chain(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_chain
         */
        chain(): LoDashExplicitObjectWrapper<T>;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_chain
         */
        chain(): TWrapper;
    }

    //lodash_tap
    interface LoDashStatic {
        /**
         * This method invokes interceptor and returns value. The interceptor is bound to thisArg and invoked with one
         * argument; (value). The purpose of this method is to "tap into" a method chain in order to perform operations
         * on intermediate results within the chain.
         *
         * @param value The value to provide to interceptor.
         * @param interceptor The function to invoke.
         * @parem thisArg The this binding of interceptor.
         * @return Returns value.
         **/
        tap<T>(
            value: T,
            interceptor: (value: T) => void
        ): T;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_tap
         */
        tap(
            interceptor: (value: T) => void
        ): TWrapper;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_tap
         */
        tap(
            interceptor: (value: T) => void
        ): TWrapper;
    }

    //lodash_thru
    interface LoDashStatic {
        /**
         * This method is like lodash_tap except that it returns the result of interceptor.
         *
         * @param value The value to provide to interceptor.
         * @param interceptor The function to invoke.
         * @param thisArg The this binding of interceptor.
         * @return Returns the result of interceptor.
         */
        thru<T, TResult>(
            value: T,
            interceptor: (value: T) => TResult
        ): TResult;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_thru
         */
        thru<TResult extends number>(
            interceptor: (value: T) => TResult): LoDashImplicitWrapper<TResult>;

        /**
         * @see lodash_thru
         */
        thru<TResult extends string>(
            interceptor: (value: T) => TResult): LoDashImplicitWrapper<TResult>;

        /**
         * @see lodash_thru
         */
        thru<TResult extends boolean>(
            interceptor: (value: T) => TResult): LoDashImplicitWrapper<TResult>;

        /**
         * @see lodash_thru
         */
        thru<TResult extends {}>(
            interceptor: (value: T) => TResult): LoDashImplicitObjectWrapper<TResult>;

        /**
         * @see lodash_thru
         */
        thru<TResult>(
            interceptor: (value: T) => TResult[]): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_thru
         */
        thru<TResult extends number>(
            interceptor: (value: T) => TResult
        ): LoDashExplicitWrapper<TResult>;

        /**
         * @see lodash_thru
         */
        thru<TResult extends string>(
            interceptor: (value: T) => TResult
        ): LoDashExplicitWrapper<TResult>;

        /**
         * @see lodash_thru
         */
        thru<TResult extends boolean>(
            interceptor: (value: T) => TResult
        ): LoDashExplicitWrapper<TResult>;

        /**
         * @see lodash_thru
         */
        thru<TResult extends {}>(
            interceptor: (value: T) => TResult
        ): LoDashExplicitObjectWrapper<TResult>;

        /**
         * @see lodash_thru
         */
        thru<TResult>(
            interceptor: (value: T) => TResult[]
        ): LoDashExplicitArrayWrapper<TResult>;
    }

    //lodash_prototype.commit
    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * Executes the chained sequence and returns the wrapped result.
         *
         * @return Returns the new lodash wrapper instance.
         */
        commit(): TWrapper;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_commit
         */
        commit(): TWrapper;
    }

    //lodash_prototype.concat
    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * Creates a new array joining a wrapped array with any additional arrays and/or values.
         *
         * @param items
         * @return Returns the new concatenated array.
         */
        concat<TItem>(...items: Array<TItem|Array<TItem>>): LoDashImplicitArrayWrapper<TItem>;

        /**
         * @see lodash_concat
         */
        concat(...items: Array<T|Array<T>>): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_concat
         */
        concat<TItem>(...items: Array<TItem|Array<TItem>>): LoDashExplicitArrayWrapper<TItem>;

        /**
         * @see lodash_concat
         */
        concat(...items: Array<T|Array<T>>): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_prototype.plant
    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * Creates a clone of the chained sequence planting value as the wrapped value.
         * @param value The value to plant as the wrapped value.
         * @return Returns the new lodash wrapper instance.
         */
        plant(value: number): LoDashImplicitWrapper<number>;

        /**
         * @see lodash_plant
         */
        plant(value: string): LoDashImplicitStringWrapper;

        /**
         * @see lodash_plant
         */
        plant(value: boolean): LoDashImplicitWrapper<boolean>;

        /**
         * @see lodash_plant
         */
        plant(value: number[]): LoDashImplicitNumberArrayWrapper;

        /**
         * @see lodash_plant
         */
        plant<T>(value: T[]): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_plant
         */
        plant<T extends {}>(value: T): LoDashImplicitObjectWrapper<T>;

        /**
         * @see lodash_plant
         */
        plant(value: any): LoDashImplicitWrapper<any>;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_plant
         */
        plant(value: number): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_plant
         */
        plant(value: string): LoDashExplicitStringWrapper;

        /**
         * @see lodash_plant
         */
        plant(value: boolean): LoDashExplicitWrapper<boolean>;

        /**
         * @see lodash_plant
         */
        plant(value: number[]): LoDashExplicitNumberArrayWrapper;

        /**
         * @see lodash_plant
         */
        plant<T>(value: T[]): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_plant
         */
        plant<T extends {}>(value: T): LoDashExplicitObjectWrapper<T>;

        /**
         * @see lodash_plant
         */
        plant(value: any): LoDashExplicitWrapper<any>;
    }

    //lodash_prototype.reverse
    interface LoDashImplicitArrayWrapper<T> {
        /**
         * Reverses the wrapped array so the first element becomes the last, the second element becomes the second to
         * last, and so on.
         *
         * Note: This method mutates the wrapped array.
         *
         * @return Returns the new reversed lodash wrapper instance.
         */
        reverse(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_reverse
         */
        reverse(): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_prototype.toJSON
    interface LoDashWrapperBase<T, TWrapper> {
        /**
         * @see lodash_value
         */
        toJSON(): T;
    }

    //lodash_prototype.toString
    interface LoDashWrapperBase<T, TWrapper> {
        /**
         * Produces the result of coercing the unwrapped value to a string.
         *
         * @return Returns the coerced string value.
         */
        toString(): string;
    }

    //lodash_prototype.value
    interface LoDashWrapperBase<T, TWrapper> {
        /**
         * Executes the chained sequence to extract the unwrapped value.
         *
         * @alias lodash_toJSON, lodash_valueOf
         *
         * @return Returns the resolved unwrapped value.
         */
        value(): T;
    }

    //lodash_valueOf
    interface LoDashWrapperBase<T, TWrapper> {
        /**
         * @see lodash_value
         */
        valueOf(): T;
    }

    /**************
     * Collection *
     **************/

        //lodash_at
    interface LoDashStatic {
        /**
         * Creates an array of elements corresponding to the given keys, or indexes, of collection. Keys may be
         * specified as individual arguments or as arrays of keys.
         *
         * @param collection The collection to iterate over.
         * @param props The property names or indexes of elements to pick, specified individually or in arrays.
         * @return Returns the new array of picked elements.
         */
        at<T>(
            collection: List<T>|Dictionary<T>,
            ...props: (number|string|(number|string)[])[]
        ): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_at
         */
        at(...props: (number|string|(number|string)[])[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_at
         */
        at<T>(...props: (number|string|(number|string)[])[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_at
         */
        at(...props: (number|string|(number|string)[])[]): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_at
         */
        at<T>(...props: (number|string|(number|string)[])[]): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_countBy
    interface LoDashStatic {
        /**
         * Creates an object composed of keys generated from the results of running each element of collection through
         * iteratee. The corresponding value of each key is the number of times the key was returned by iteratee. The
         * iteratee is bound to thisArg and invoked with three arguments:
         * (value, index|key, collection).
         *
         * If a property name is provided for iteratee the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for iteratee the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * @param collection The collection to iterate over.
         * @param iteratee The function invoked per iteration.
         * @param thisArg The this binding of iteratee.
         * @return Returns the composed aggregate object.
         */
        countBy<T>(
            collection: List<T>,
            iteratee?: ListIterator<T, any>
        ): Dictionary<number>;

        /**
         * @see lodash_countBy
         */
        countBy<T>(
            collection: Dictionary<T>,
            iteratee?: DictionaryIterator<T, any>
        ): Dictionary<number>;

        /**
         * @see lodash_countBy
         */
        countBy<T>(
            collection: NumericDictionary<T>,
            iteratee?: NumericDictionaryIterator<T, any>
        ): Dictionary<number>;

        /**
         * @see lodash_countBy
         */
        countBy<T>(
            collection: List<T>|Dictionary<T>|NumericDictionary<T>,
            iteratee?: string
        ): Dictionary<number>;

        /**
         * @see lodash_countBy
         */
        countBy<W, T>(
            collection: List<T>|Dictionary<T>|NumericDictionary<T>,
            iteratee?: W
        ): Dictionary<number>;

        /**
         * @see lodash_countBy
         */
        countBy<T>(
            collection: List<T>|Dictionary<T>|NumericDictionary<T>,
            iteratee?: Object
        ): Dictionary<number>;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_countBy
         */
        countBy(
            iteratee?: ListIterator<T, any>
        ): LoDashImplicitObjectWrapper<Dictionary<number>>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_countBy
         */
        countBy(
            iteratee?: ListIterator<T, any>
        ): LoDashImplicitObjectWrapper<Dictionary<number>>;

        /**
         * @see lodash_countBy
         */
        countBy(
            iteratee?: string
        ): LoDashImplicitObjectWrapper<Dictionary<number>>;

        /**
         * @see lodash_countBy
         */
        countBy<W>(
            iteratee?: W
        ): LoDashImplicitObjectWrapper<Dictionary<number>>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_countBy
         */
        countBy<T>(
            iteratee?: ListIterator<T, any>|DictionaryIterator<T, any>|NumericDictionaryIterator<T, any>
        ): LoDashImplicitObjectWrapper<Dictionary<number>>;

        /**
         * @see lodash_countBy
         */
        countBy(
            iteratee?: string
        ): LoDashImplicitObjectWrapper<Dictionary<number>>;

        /**
         * @see lodash_countBy
         */
        countBy<W>(
            iteratee?: W
        ): LoDashImplicitObjectWrapper<Dictionary<number>>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_countBy
         */
        countBy(
            iteratee?: ListIterator<T, any>
        ): LoDashExplicitObjectWrapper<Dictionary<number>>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_countBy
         */
        countBy(
            iteratee?: ListIterator<T, any>
        ): LoDashExplicitObjectWrapper<Dictionary<number>>;

        /**
         * @see lodash_countBy
         */
        countBy(
            iteratee?: string
        ): LoDashExplicitObjectWrapper<Dictionary<number>>;

        /**
         * @see lodash_countBy
         */
        countBy<W>(
            iteratee?: W
        ): LoDashExplicitObjectWrapper<Dictionary<number>>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_countBy
         */
        countBy<T>(
            iteratee?: ListIterator<T, any>|DictionaryIterator<T, any>|NumericDictionaryIterator<T, any>
        ): LoDashExplicitObjectWrapper<Dictionary<number>>;

        /**
         * @see lodash_countBy
         */
        countBy(
            iteratee?: string
        ): LoDashExplicitObjectWrapper<Dictionary<number>>;

        /**
         * @see lodash_countBy
         */
        countBy<W>(
            iteratee?: W
        ): LoDashExplicitObjectWrapper<Dictionary<number>>;
    }

    //lodash_each
    interface LoDashStatic {
        /**
         * @see lodash_forEach
         */
        each<T>(
            collection: T[],
            iteratee?: ListIterator<T, any>
        ): T[];

        /**
         * @see lodash_forEach
         */
        each<T>(
            collection: List<T>,
            iteratee?: ListIterator<T, any>
        ): List<T>;

        /**
         * @see lodash_forEach
         */
        each<T>(
            collection: Dictionary<T>,
            iteratee?: DictionaryIterator<T, any>
        ): Dictionary<T>;

        /**
         * @see lodash_forEach
         */
        each<T extends {}>(
            collection: T,
            iteratee?: ObjectIterator<any, any>
        ): T;

        /**
         * @see lodash_forEach
         */
        each<T extends {}, TValue>(
            collection: T,
            iteratee?: ObjectIterator<TValue, any>
        ): T;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_forEach
         */
        each(
            iteratee: ListIterator<string, any>
        ): LoDashImplicitWrapper<string>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_forEach
         */
        each(
            iteratee: ListIterator<T, any>
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_forEach
         */
        each<TValue>(
            iteratee?: ListIterator<TValue, any>|DictionaryIterator<TValue, any>
        ): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_forEach
         */
        each(
            iteratee: ListIterator<string, any>
        ): LoDashExplicitWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_forEach
         */
        each(
            iteratee: ListIterator<T, any>
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_forEach
         */
        each<TValue>(
            iteratee?: ListIterator<TValue, any>|DictionaryIterator<TValue, any>
        ): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_eachRight
    interface LoDashStatic {
        /**
         * @see lodash_forEachRight
         */
        eachRight<T>(
            collection: T[],
            iteratee?: ListIterator<T, any>
        ): T[];

        /**
         * @see lodash_forEachRight
         */
        eachRight<T>(
            collection: List<T>,
            iteratee?: ListIterator<T, any>
        ): List<T>;

        /**
         * @see lodash_forEachRight
         */
        eachRight<T>(
            collection: Dictionary<T>,
            iteratee?: DictionaryIterator<T, any>
        ): Dictionary<T>;

        /**
         * @see lodash_forEachRight
         */
        eachRight<T extends {}>(
            collection: T,
            iteratee?: ObjectIterator<any, any>
        ): T;

        /**
         * @see lodash_forEachRight
         */
        eachRight<T extends {}, TValue>(
            collection: T,
            iteratee?: ObjectIterator<TValue, any>
        ): T;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_forEachRight
         */
        eachRight(
            iteratee: ListIterator<string, any>
        ): LoDashImplicitWrapper<string>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_forEachRight
         */
        eachRight(
            iteratee: ListIterator<T, any>
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_forEachRight
         */
        eachRight<TValue>(
            iteratee?: ListIterator<TValue, any>|DictionaryIterator<TValue, any>
        ): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_forEachRight
         */
        eachRight(
            iteratee: ListIterator<string, any>
        ): LoDashExplicitWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_forEachRight
         */
        eachRight(
            iteratee: ListIterator<T, any>
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_forEachRight
         */
        eachRight<TValue>(
            iteratee?: ListIterator<TValue, any>|DictionaryIterator<TValue, any>
        ): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_every
    interface LoDashStatic {
        /**
         * Checks if predicate returns truthy for all elements of collection. Iteration is stopped once predicate
         * returns falsey. The predicate is invoked with three arguments: (value, index|key, collection).
         *
         * @param collection The collection to iterate over.
         * @param predicate The function invoked per iteration.
         * @return Returns true if all elements pass the predicate check, else false.
         */
        every<T>(
            collection: List<T>,
            predicate?: ListIterator<T, boolean>
        ): boolean;

        /**
         * @see lodash_every
         */
        every<T>(
            collection: Dictionary<T>,
            predicate?: DictionaryIterator<T, boolean>
        ): boolean;

        /**
         * @see lodash_every
         */
        every<T>(
            collection: NumericDictionary<T>,
            predicate?: NumericDictionaryIterator<T, boolean>
        ): boolean;

        /**
         * @see lodash_every
         */
        every<T>(
            collection: List<T>|Dictionary<T>|NumericDictionary<T>,
            predicate?: string|any[]
        ): boolean;

        /**
         * @see lodash_every
         */
        every<TObject extends {}, T>(
            collection: List<T>|Dictionary<T>|NumericDictionary<T>,
            predicate?: TObject
        ): boolean;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_every
         */
        every(
            predicate?: ListIterator<T, boolean>|NumericDictionaryIterator<T, boolean>
        ): boolean;

        /**
         * @see lodash_every
         */
        every(
            predicate?: string|any[]
        ): boolean;

        /**
         * @see lodash_every
         */
        every<TObject extends {}>(
            predicate?: TObject
        ): boolean;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_every
         */
        every<TResult>(
            predicate?: ListIterator<TResult, boolean>|DictionaryIterator<TResult, boolean>|NumericDictionaryIterator<T, boolean>
        ): boolean;

        /**
         * @see lodash_every
         */
        every(
            predicate?: string|any[]
        ): boolean;

        /**
         * @see lodash_every
         */
        every<TObject extends {}>(
            predicate?: TObject
        ): boolean;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_every
         */
        every(
            predicate?: ListIterator<T, boolean>|NumericDictionaryIterator<T, boolean>
        ): LoDashExplicitWrapper<boolean>;

        /**
         * @see lodash_every
         */
        every(
            predicate?: string|any[]
        ): LoDashExplicitWrapper<boolean>;

        /**
         * @see lodash_every
         */
        every<TObject extends {}>(
            predicate?: TObject
        ): LoDashExplicitWrapper<boolean>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_every
         */
        every<TResult>(
            predicate?: ListIterator<TResult, boolean>|DictionaryIterator<TResult, boolean>|NumericDictionaryIterator<T, boolean>
        ): LoDashExplicitWrapper<boolean>;

        /**
         * @see lodash_every
         */
        every(
            predicate?: string|any[]
        ): LoDashExplicitWrapper<boolean>;

        /**
         * @see lodash_every
         */
        every<TObject extends {}>(
            predicate?: TObject
        ): LoDashExplicitWrapper<boolean>;
    }

    //lodash_filter
    interface LoDashStatic {
        /**
         * Iterates over elements of collection, returning an array of all elements predicate returns truthy for. The
         * predicate is bound to thisArg and invoked with three arguments: (value, index|key, collection).
         *
         * If a property name is provided for predicate the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for predicate the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * @param collection The collection to iterate over.
         * @param predicate The function invoked per iteration.
         * @param thisArg The this binding of predicate.
         * @return Returns the new filtered array.
         */
        filter<T>(
            collection: List<T>,
            predicate?: ListIterator<T, boolean>
        ): T[];

        /**
         * @see lodash_filter
         */
        filter<T>(
            collection: Dictionary<T>,
            predicate?: DictionaryIterator<T, boolean>
        ): T[];

        /**
         * @see lodash_filter
         */
        filter(
            collection: string,
            predicate?: StringIterator<boolean>
        ): string[];

        /**
         * @see lodash_filter
         */
        filter<T>(
            collection: List<T>|Dictionary<T>,
            predicate: string
        ): T[];

        /**
         * @see lodash_filter
         */
        filter<W extends {}, T>(
            collection: List<T>|Dictionary<T>,
            predicate: W
        ): T[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_filter
         */
        filter(
            predicate?: StringIterator<boolean>
        ): LoDashImplicitArrayWrapper<string>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_filter
         */
        filter(
            predicate: ListIterator<T, boolean>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_filter
         */
        filter(
            predicate: string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_filter
         */
        filter<W>(predicate: W): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_filter
         */
        filter<T>(
            predicate: ListIterator<T, boolean>|DictionaryIterator<T, boolean>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_filter
         */
        filter<T>(
            predicate: string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_filter
         */
        filter<W, T>(predicate: W): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_filter
         */
        filter(
            predicate?: StringIterator<boolean>
        ): LoDashExplicitArrayWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_filter
         */
        filter(
            predicate: ListIterator<T, boolean>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_filter
         */
        filter(
            predicate: string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_filter
         */
        filter<W>(predicate: W): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_filter
         */
        filter<T>(
            predicate: ListIterator<T, boolean>|DictionaryIterator<T, boolean>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_filter
         */
        filter<T>(
            predicate: string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_filter
         */
        filter<W, T>(predicate: W): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_find
    interface LoDashStatic {
        /**
         * Iterates over elements of collection, returning the first element predicate returns truthy for.
         * The predicate is bound to thisArg and invoked with three arguments: (value, index|key, collection).
         *
         * If a property name is provided for predicate the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for predicate the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * @param collection The collection to search.
         * @param predicate The function invoked per iteration.
         * @param fromIndex The index to search from.
         * @return Returns the matched element, else undefined.
         */
        find<T>(
            collection: List<T>,
            predicate?: ListIterator<T, boolean>,
            fromIndex?: number
        ): T;

        /**
         * @see lodash_find
         */
        find<T>(
            collection: Dictionary<T>,
            predicate?: DictionaryIterator<T, boolean>,
            fromIndex?: number
        ): T;

        /**
         * @see lodash_find
         */
        find<T>(
            collection: List<T>|Dictionary<T>,
            predicate?: string,
            fromIndex?: number
        ): T;

        /**
         * @see lodash_find
         */
        find<TObject extends {}, T>(
            collection: List<T>|Dictionary<T>,
            predicate?: TObject,
            fromIndex?: number
        ): T;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_find
         */
        find(
            predicate?: ListIterator<T, boolean>,
            fromIndex?: number
        ): T;

        /**
         * @see lodash_find
         */
        find(
            predicate?: string,
            fromIndex?: number
        ): T;

        /**
         * @see lodash_find
         */
        find<TObject extends {}>(
            predicate?: TObject,
            fromIndex?: number
        ): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_find
         */
        find<TResult>(
            predicate?: ListIterator<TResult, boolean>|DictionaryIterator<TResult, boolean>,
            fromIndex?: number
        ): TResult;

        /**
         * @see lodash_find
         */
        find<TResult>(
            predicate?: string,
            fromIndex?: number
        ): TResult;

        /**
         * @see lodash_find
         */
        find<TObject extends {}, TResult>(
            predicate?: TObject,
            fromIndex?: number
        ): TResult;
    }

    //lodash_findLast
    interface LoDashStatic {
        /**
         * This method is like lodash_find except that it iterates over elements of a collection from
         * right to left.
         * @param collection Searches for a value in this list.
         * @param callback The function called per iteration.
         * @param thisArg The this binding of callback.
         * @return The found element, else undefined.
         **/
        findLast<T>(
            collection: Array<T>,
            callback: ListIterator<T, boolean>): T;

        /**
         * @see lodash_find
         **/
        findLast<T>(
            collection: List<T>,
            callback: ListIterator<T, boolean>): T;

        /**
         * @see lodash_find
         **/
        findLast<T>(
            collection: Dictionary<T>,
            callback: DictionaryIterator<T, boolean>): T;

        /**
         * @see lodash_find
         * @param lodash_pluck style callback
         **/
        findLast<W, T>(
            collection: Array<T>,
            whereValue: W): T;

        /**
         * @see lodash_find
         * @param lodash_pluck style callback
         **/
        findLast<W, T>(
            collection: List<T>,
            whereValue: W): T;

        /**
         * @see lodash_find
         * @param lodash_pluck style callback
         **/
        findLast<W, T>(
            collection: Dictionary<T>,
            whereValue: W): T;

        /**
         * @see lodash_find
         * @param lodash_where style callback
         **/
        findLast<T>(
            collection: Array<T>,
            pluckValue: string): T;

        /**
         * @see lodash_find
         * @param lodash_where style callback
         **/
        findLast<T>(
            collection: List<T>,
            pluckValue: string): T;

        /**
         * @see lodash_find
         * @param lodash_where style callback
         **/
        findLast<T>(
            collection: Dictionary<T>,
            pluckValue: string): T;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_findLast
         */
        findLast(
            callback: ListIterator<T, boolean>): T;
        /**
         * @see lodash_findLast
         * @param lodash_where style callback
         */
        findLast<W>(
            whereValue: W): T;

        /**
         * @see lodash_findLast
         * @param lodash_where style callback
         */
        findLast(
            pluckValue: string): T;
    }

    //lodash_flatMap
    interface LoDashStatic {
        /**
         * Creates an array of flattened values by running each element in collection through iteratee
         * and concating its result to the other mapped values. The iteratee is invoked with three arguments:
         * (value, index|key, collection).
         *
         * @param collection The collection to iterate over.
         * @param iteratee The function invoked per iteration.
         * @return Returns the new flattened array.
         */
        flatMap<T, TResult>(
            collection: List<T>,
            iteratee?: ListIterator<T, TResult|TResult[]>
        ): TResult[];

        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(
            collection: List<any>,
            iteratee?: ListIterator<any, TResult|TResult[]>
        ): TResult[];

        /**
         * @see lodash_flatMap
         */
        flatMap<T, TResult>(
            collection: Dictionary<T>,
            iteratee?: DictionaryIterator<T, TResult|TResult[]>
        ): TResult[];

        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(
            collection: Dictionary<any>,
            iteratee?: DictionaryIterator<any, TResult|TResult[]>
        ): TResult[];

        /**
         * @see lodash_flatMap
         */
        flatMap<T, TResult>(
            collection: NumericDictionary<T>,
            iteratee?: NumericDictionaryIterator<T, TResult|TResult[]>
        ): TResult[];

        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(
            collection: NumericDictionary<any>,
            iteratee?: NumericDictionaryIterator<any, TResult|TResult[]>
        ): TResult[];

        /**
         * @see lodash_flatMap
         */
        flatMap<TObject extends Object, TResult>(
            collection: TObject,
            iteratee?: ObjectIterator<any, TResult|TResult[]>
        ): TResult[];

        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(
            collection: Object,
            iteratee?: ObjectIterator<any, TResult|TResult[]>
        ): TResult[];

        /**
         * @see lodash_flatMap
         */
        flatMap<TWhere extends Object, TObject extends Object>(
            collection: TObject,
            iteratee: TWhere
        ): boolean[];

        /**
         * @see lodash_flatMap
         */
        flatMap<TObject extends Object, TResult>(
            collection: TObject,
            iteratee: Object|string
        ): TResult[];

        /**
         * @see lodash_flatMap
         */
        flatMap<TObject extends Object>(
            collection: TObject,
            iteratee: [string, any]
        ): boolean[];

        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(
            collection: string
        ): string[];

        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(
            collection: Object,
            iteratee?: Object|string
        ): TResult[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(
            iteratee: ListIterator<string, TResult|TResult[]>
        ): LoDashImplicitArrayWrapper<TResult>;

        /**
         * @see lodash_flatMap
         */
        flatMap(): LoDashImplicitArrayWrapper<string>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(
            iteratee: ListIterator<T, TResult|TResult[]>|string
        ): LoDashImplicitArrayWrapper<TResult>;

        /**
         * @see lodash_flatMap
         */
        flatMap<TWhere extends Object>(
            iteratee: TWhere
        ): LoDashImplicitArrayWrapper<boolean>;

        /**
         * @see lodash_flatMap
         */
        flatMap(
            iteratee: [string, any]
        ): LoDashImplicitArrayWrapper<boolean>;

        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_flatMap
         */
        flatMap<T, TResult>(
            iteratee: ListIterator<T, TResult|TResult[]>|DictionaryIterator<T, TResult|TResult[]>|NumericDictionaryIterator<T, TResult|TResult[]>
        ): LoDashImplicitArrayWrapper<TResult>;

        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(
            iteratee: ObjectIterator<any, TResult|TResult[]>|string
        ): LoDashImplicitArrayWrapper<TResult>;

        /**
         * @see lodash_flatMap
         */
        flatMap<TWhere extends Object>(
            iteratee: TWhere
        ): LoDashImplicitArrayWrapper<boolean>;

        /**
         * @see lodash_flatMap
         */
        flatMap(
            iteratee: [string, any]
        ): LoDashImplicitArrayWrapper<boolean>;

        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(
            iteratee: ListIterator<string, TResult|TResult[]>
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_flatMap
         */
        flatMap(): LoDashExplicitArrayWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(
            iteratee: ListIterator<T, TResult|TResult[]>|string
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_flatMap
         */
        flatMap<TWhere extends Object>(
            iteratee: TWhere
        ): LoDashExplicitArrayWrapper<boolean>;

        /**
         * @see lodash_flatMap
         */
        flatMap(
            iteratee: [string, any]
        ): LoDashExplicitArrayWrapper<boolean>;

        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(): LoDashExplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_flatMap
         */
        flatMap<T, TResult>(
            iteratee: ListIterator<T, TResult|TResult[]>|DictionaryIterator<T, TResult|TResult[]>|NumericDictionaryIterator<T, TResult|TResult[]>
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(
            iteratee: ObjectIterator<any, TResult|TResult[]>|string
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_flatMap
         */
        flatMap<TWhere extends Object>(
            iteratee: TWhere
        ): LoDashExplicitArrayWrapper<boolean>;

        /**
         * @see lodash_flatMap
         */
        flatMap(
            iteratee: [string, any]
        ): LoDashExplicitArrayWrapper<boolean>;

        /**
         * @see lodash_flatMap
         */
        flatMap<TResult>(): LoDashExplicitArrayWrapper<TResult>;
    }

    //lodash_forEach
    interface LoDashStatic {
        /**
         * Iterates over elements of collection invoking iteratee for each element. The iteratee is bound to thisArg
         * and invoked with three arguments:
         * (value, index|key, collection). Iteratee functions may exit iteration early by explicitly returning false.
         *
         * Note: As with other "Collections" methods, objects with a "length" property are iterated like arrays. To
         * avoid this behavior lodash_forIn or lodash_forOwn may be used for object iteration.
         *
         * @alias lodash_each
         *
         * @param collection The collection to iterate over.
         * @param iteratee The function invoked per iteration.
         * @param thisArg The this binding of iteratee.
         */
        forEach<T>(
            collection: T[],
            iteratee?: ListIterator<T, any>
        ): T[];

        /**
         * @see lodash_forEach
         */
        forEach<T>(
            collection: List<T>,
            iteratee?: ListIterator<T, any>
        ): List<T>;

        /**
         * @see lodash_forEach
         */
        forEach<T>(
            collection: Dictionary<T>,
            iteratee?: DictionaryIterator<T, any>
        ): Dictionary<T>;

        /**
         * @see lodash_forEach
         */
        forEach<T extends {}>(
            collection: T,
            iteratee?: ObjectIterator<any, any>
        ): T;

        /**
         * @see lodash_forEach
         */
        forEach<T extends {}, TValue>(
            collection: T,
            iteratee?: ObjectIterator<TValue, any>
        ): T;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_forEach
         */
        forEach(
            iteratee: ListIterator<string, any>
        ): LoDashImplicitWrapper<string>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_forEach
         */
        forEach(
            iteratee: ListIterator<T, any>
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_forEach
         */
        forEach<TValue>(
            iteratee?: ListIterator<TValue, any>|DictionaryIterator<TValue, any>
        ): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_forEach
         */
        forEach(
            iteratee: ListIterator<string, any>
        ): LoDashExplicitWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_forEach
         */
        forEach(
            iteratee: ListIterator<T, any>
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_forEach
         */
        forEach<TValue>(
            iteratee?: ListIterator<TValue, any>|DictionaryIterator<TValue, any>
        ): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_forEachRight
    interface LoDashStatic {
        /**
         * This method is like lodash_forEach except that it iterates over elements of collection from right to left.
         *
         * @alias lodash_eachRight
         *
         * @param collection The collection to iterate over.
         * @param iteratee The function called per iteration.
         * @param thisArg The this binding of callback.
         */
        forEachRight<T>(
            collection: T[],
            iteratee?: ListIterator<T, any>
        ): T[];

        /**
         * @see lodash_forEachRight
         */
        forEachRight<T>(
            collection: List<T>,
            iteratee?: ListIterator<T, any>
        ): List<T>;

        /**
         * @see lodash_forEachRight
         */
        forEachRight<T>(
            collection: Dictionary<T>,
            iteratee?: DictionaryIterator<T, any>
        ): Dictionary<T>;

        /**
         * @see lodash_forEachRight
         */
        forEachRight<T extends {}>(
            collection: T,
            iteratee?: ObjectIterator<any, any>
        ): T;

        /**
         * @see lodash_forEachRight
         */
        forEachRight<T extends {}, TValue>(
            collection: T,
            iteratee?: ObjectIterator<TValue, any>
        ): T;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_forEachRight
         */
        forEachRight(
            iteratee: ListIterator<string, any>
        ): LoDashImplicitWrapper<string>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_forEachRight
         */
        forEachRight(
            iteratee: ListIterator<T, any>
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_forEachRight
         */
        forEachRight<TValue>(
            iteratee?: ListIterator<TValue, any>|DictionaryIterator<TValue, any>
        ): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_forEachRight
         */
        forEachRight(
            iteratee: ListIterator<string, any>
        ): LoDashExplicitWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_forEachRight
         */
        forEachRight(
            iteratee: ListIterator<T, any>
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_forEachRight
         */
        forEachRight<TValue>(
            iteratee?: ListIterator<TValue, any>|DictionaryIterator<TValue, any>
        ): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_groupBy
    interface LoDashStatic {
        /**
         * Creates an object composed of keys generated from the results of running each element of collection through
         * iteratee. The corresponding value of each key is an array of the elements responsible for generating the
         * key. The iteratee is bound to thisArg and invoked with three arguments:
         * (value, index|key, collection).
         *
         * If a property name is provided for iteratee the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for iteratee the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * @param collection The collection to iterate over.
         * @param iteratee The function invoked per iteration.
         * @param thisArg The this binding of iteratee.
         * @return Returns the composed aggregate object.
         */
        groupBy<T, TKey>(
            collection: List<T>,
            iteratee?: ListIterator<T, TKey>
        ): Dictionary<T[]>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T>(
            collection: List<any>,
            iteratee?: ListIterator<T, any>
        ): Dictionary<T[]>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T, TKey>(
            collection: Dictionary<T>,
            iteratee?: DictionaryIterator<T, TKey>
        ): Dictionary<T[]>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T>(
            collection: Dictionary<any>,
            iteratee?: DictionaryIterator<T, any>
        ): Dictionary<T[]>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T, TValue>(
            collection: List<T>|Dictionary<T>,
            iteratee?: string
        ): Dictionary<T[]>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T>(
            collection: List<T>|Dictionary<T>,
            iteratee?: string
        ): Dictionary<T[]>;

        /**
         * @see lodash_groupBy
         */
        groupBy<TWhere, T>(
            collection: List<T>|Dictionary<T>,
            iteratee?: TWhere
        ): Dictionary<T[]>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T>(
            collection: List<T>|Dictionary<T>,
            iteratee?: Object
        ): Dictionary<T[]>;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_groupBy
         */
        groupBy<TKey>(
            iteratee?: ListIterator<T, TKey>
        ): LoDashImplicitObjectWrapper<Dictionary<T[]>>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_groupBy
         */
        groupBy<TKey>(
            iteratee?: ListIterator<T, TKey>
        ): LoDashImplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<TValue>(
            iteratee?: string
        ): LoDashImplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<TWhere>(
            iteratee?: TWhere
        ): LoDashImplicitObjectWrapper<Dictionary<T[]>>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_groupBy
         */
        groupBy<T, TKey>(
            iteratee?: ListIterator<T, TKey>|DictionaryIterator<T, TKey>
        ): LoDashImplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T>(
            iteratee?: ListIterator<T, any>|DictionaryIterator<T, any>
        ): LoDashImplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T, TValue>(
            iteratee?: string
        ): LoDashImplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T>(
            iteratee?: string
        ): LoDashImplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<TWhere, T>(
            iteratee?: TWhere
        ): LoDashImplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T>(
            iteratee?: Object
        ): LoDashImplicitObjectWrapper<Dictionary<T[]>>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_groupBy
         */
        groupBy<TKey>(
            iteratee?: ListIterator<T, TKey>
        ): LoDashExplicitObjectWrapper<Dictionary<T[]>>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_groupBy
         */
        groupBy<TKey>(
            iteratee?: ListIterator<T, TKey>
        ): LoDashExplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<TValue>(
            iteratee?: string
        ): LoDashExplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<TWhere>(
            iteratee?: TWhere
        ): LoDashExplicitObjectWrapper<Dictionary<T[]>>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_groupBy
         */
        groupBy<T, TKey>(
            iteratee?: ListIterator<T, TKey>|DictionaryIterator<T, TKey>
        ): LoDashExplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T>(
            iteratee?: ListIterator<T, any>|DictionaryIterator<T, any>
        ): LoDashExplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T, TValue>(
            iteratee?: string
        ): LoDashExplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T>(
            iteratee?: string
        ): LoDashExplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<TWhere, T>(
            iteratee?: TWhere
        ): LoDashExplicitObjectWrapper<Dictionary<T[]>>;

        /**
         * @see lodash_groupBy
         */
        groupBy<T>(
            iteratee?: Object
        ): LoDashExplicitObjectWrapper<Dictionary<T[]>>;
    }

    //lodash_includes
    interface LoDashStatic {
        /**
         * Checks if target is in collection using SameValueZero for equality comparisons. If fromIndex is negative,
         * it’s used as the offset from the end of collection.
         *
         * @param collection The collection to search.
         * @param target The value to search for.
         * @param fromIndex The index to search from.
         * @return True if the target element is found, else false.
         */
        includes<T>(
            collection: List<T>|Dictionary<T>,
            target: T,
            fromIndex?: number
        ): boolean;

        /**
         * @see lodash_includes
         */
        includes(
            collection: string,
            target: string,
            fromIndex?: number
        ): boolean;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_includes
         */
        includes(
            target: T,
            fromIndex?: number
        ): boolean;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_includes
         */
        includes<TValue>(
            target: TValue,
            fromIndex?: number
        ): boolean;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_includes
         */
        includes(
            target: string,
            fromIndex?: number
        ): boolean;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_includes
         */
        includes(
            target: T,
            fromIndex?: number
        ): LoDashExplicitWrapper<boolean>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_includes
         */
        includes<TValue>(
            target: TValue,
            fromIndex?: number
        ): LoDashExplicitWrapper<boolean>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_includes
         */
        includes(
            target: string,
            fromIndex?: number
        ): LoDashExplicitWrapper<boolean>;
    }

    //lodash_keyBy
    interface LoDashStatic {
        /**
         * Creates an object composed of keys generated from the results of running each element of collection through
         * iteratee. The corresponding value of each key is the last element responsible for generating the key. The
         * iteratee function is bound to thisArg and invoked with three arguments:
         * (value, index|key, collection).
         *
         * If a property name is provided for iteratee the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for iteratee the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * @param collection The collection to iterate over.
         * @param iteratee The function invoked per iteration.
         * @param thisArg The this binding of iteratee.
         * @return Returns the composed aggregate object.
         */
        keyBy<T>(
            collection: List<T>,
            iteratee?: ListIterator<T, any>
        ): Dictionary<T>;

        /**
         * @see lodash_keyBy
         */
        keyBy<T>(
            collection: NumericDictionary<T>,
            iteratee?: NumericDictionaryIterator<T, any>
        ): Dictionary<T>;

        /**
         * @see lodash_keyBy
         */
        keyBy<T>(
            collection: Dictionary<T>,
            iteratee?: DictionaryIterator<T, any>
        ): Dictionary<T>;

        /**
         * @see lodash_keyBy
         */
        keyBy<T>(
            collection: List<T>|NumericDictionary<T>|Dictionary<T>,
            iteratee?: string
        ): Dictionary<T>;

        /**
         * @see lodash_keyBy
         */
        keyBy<W extends Object, T>(
            collection: List<T>|NumericDictionary<T>|Dictionary<T>,
            iteratee?: W
        ): Dictionary<T>;

        /**
         * @see lodash_keyBy
         */
        keyBy<T>(
            collection: List<T>|NumericDictionary<T>|Dictionary<T>,
            iteratee?: Object
        ): Dictionary<T>;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_keyBy
         */
        keyBy(
            iteratee?: ListIterator<T, any>
        ): LoDashImplicitObjectWrapper<Dictionary<T>>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_keyBy
         */
        keyBy(
            iteratee?: ListIterator<T, any>
        ): LoDashImplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_keyBy
         */
        keyBy(
            iteratee?: string
        ): LoDashImplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_keyBy
         */
        keyBy<W extends Object>(
            iteratee?: W
        ): LoDashImplicitObjectWrapper<Dictionary<T>>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_keyBy
         */
        keyBy<T>(
            iteratee?: ListIterator<T, any>|NumericDictionaryIterator<T, any>|DictionaryIterator<T, any>
        ): LoDashImplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_keyBy
         */
        keyBy<T>(
            iteratee?: string
        ): LoDashImplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_keyBy
         */
        keyBy<W extends Object, T>(
            iteratee?: W
        ): LoDashImplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_keyBy
         */
        keyBy<T>(
            iteratee?: Object
        ): LoDashImplicitObjectWrapper<Dictionary<T>>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_keyBy
         */
        keyBy(
            iteratee?: ListIterator<T, any>
        ): LoDashExplicitObjectWrapper<Dictionary<T>>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_keyBy
         */
        keyBy(
            iteratee?: ListIterator<T, any>
        ): LoDashExplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_keyBy
         */
        keyBy(
            iteratee?: string
        ): LoDashExplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_keyBy
         */
        keyBy<W extends Object>(
            iteratee?: W
        ): LoDashExplicitObjectWrapper<Dictionary<T>>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_keyBy
         */
        keyBy<T>(
            iteratee?: ListIterator<T, any>|NumericDictionaryIterator<T, any>|DictionaryIterator<T, any>
        ): LoDashExplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_keyBy
         */
        keyBy<T>(
            iteratee?: string
        ): LoDashExplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_keyBy
         */
        keyBy<W extends Object, T>(
            iteratee?: W
        ): LoDashExplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_keyBy
         */
        keyBy<T>(
            iteratee?: Object
        ): LoDashExplicitObjectWrapper<Dictionary<T>>;
    }

    //lodash_invoke
    interface LoDashStatic {
        /**
         * Invokes the method at path of object.
         * @param object The object to query.
         * @param path The path of the method to invoke.
         * @param args The arguments to invoke the method with.
         **/
        invoke<TObject extends Object, TResult>(
            object: TObject,
            path: StringRepresentable|StringRepresentable[],
            ...args: any[]): TResult;

        /**
         * @see lodash_invoke
         **/
        invoke<TValue, TResult>(
            object: Dictionary<TValue>|TValue[],
            path: StringRepresentable|StringRepresentable[],
            ...args: any[]): TResult;

        /**
         * @see lodash_invoke
         **/
        invoke<TResult>(
            object: any,
            path: StringRepresentable|StringRepresentable[],
            ...args: any[]): TResult;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_invoke
         **/
        invoke<TResult>(
            path: StringRepresentable|StringRepresentable[],
            ...args: any[]): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_invoke
         **/
        invoke<TResult>(
            path: StringRepresentable|StringRepresentable[],
            ...args: any[]): TResult;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_invoke
         **/
        invoke<TResult>(
            path: StringRepresentable|StringRepresentable[],
            ...args: any[]): TResult;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_invoke
         **/
        invoke<TResult>(
            path: StringRepresentable|StringRepresentable[],
            ...args: any[]): TResult;
    }

    //lodash_invokeMap
    interface LoDashStatic {
        /**
         * Invokes the method named by methodName on each element in the collection returning
         * an array of the results of each invoked method. Additional arguments will be provided
         * to each invoked method. If methodName is a function it will be invoked for, and this
         * bound to, each element in the collection.
         * @param collection The collection to iterate over.
         * @param methodName The name of the method to invoke.
         * @param args Arguments to invoke the method with.
         **/
        invokeMap<TValue extends {}, TResult>(
            collection: TValue[],
            methodName: string,
            ...args: any[]): TResult[];

        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TValue extends {}, TResult>(
            collection: Dictionary<TValue>,
            methodName: string,
            ...args: any[]): TResult[];

        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TResult>(
            collection: {}[],
            methodName: string,
            ...args: any[]): TResult[];

        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TResult>(
            collection: Dictionary<{}>,
            methodName: string,
            ...args: any[]): TResult[];

        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TValue extends {}, TResult>(
            collection: TValue[],
            method: (...args: any[]) => TResult,
            ...args: any[]): TResult[];

        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TValue extends {}, TResult>(
            collection: Dictionary<TValue>,
            method: (...args: any[]) => TResult,
            ...args: any[]): TResult[];

        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TResult>(
            collection: {}[],
            method: (...args: any[]) => TResult,
            ...args: any[]): TResult[];

        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TResult>(
            collection: Dictionary<{}>,
            method: (...args: any[]) => TResult,
            ...args: any[]): TResult[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TResult>(
            methodName: string,
            ...args: any[]): LoDashImplicitArrayWrapper<TResult>;

        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TResult>(
            method: (...args: any[]) => TResult,
            ...args: any[]): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TResult>(
            methodName: string,
            ...args: any[]): LoDashImplicitArrayWrapper<TResult>;

        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TResult>(
            method: (...args: any[]) => TResult,
            ...args: any[]): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TResult>(
            methodName: string,
            ...args: any[]): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TResult>(
            method: (...args: any[]) => TResult,
            ...args: any[]): LoDashExplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TResult>(
            methodName: string,
            ...args: any[]): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_invokeMap
         **/
        invokeMap<TResult>(
            method: (...args: any[]) => TResult,
            ...args: any[]): LoDashExplicitArrayWrapper<TResult>;
    }

    //lodash_map
    interface LoDashStatic {
        /**
         * Creates an array of values by running each element in collection through iteratee. The iteratee is bound to
         * thisArg and invoked with three arguments: (value, index|key, collection).
         *
         * If a property name is provided for iteratee the created lodash_property style callback returns the property value
         * of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for iteratee the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * Many lodash methods are guarded to work as iteratees for methods like lodash_every, lodash_filter, lodash_map, lodash_mapValues,
         * lodash_reject, and lodash_some.
         *
         * The guarded methods are:
         * ary, callback, chunk, clone, create, curry, curryRight, drop, dropRight, every, fill, flatten, invert, max,
         * min, parseInt, slice, sortBy, take, takeRight, template, trim, trimLeft, trimRight, trunc, random, range,
         * sample, some, sum, uniq, and words
         *
         * @param collection The collection to iterate over.
         * @param iteratee The function invoked per iteration.
         * @param thisArg The this binding of iteratee.
         * @return Returns the new mapped array.
         */
        map<T, TResult>(
            collection: List<T>,
            iteratee?: ListIterator<T, TResult>
        ): TResult[];

        /**
         * @see lodash_map
         */
        map<T extends {}, TResult>(
            collection: Dictionary<T>,
            iteratee?: DictionaryIterator<T, TResult>
        ): TResult[];

        map<T extends {}, TResult>(
            collection: NumericDictionary<T>,
            iteratee?: NumericDictionaryIterator<T, TResult>
        ): TResult[];

        /**
         * @see lodash_map
         */
        map<T, TResult>(
            collection: List<T>|Dictionary<T>|NumericDictionary<T>,
            iteratee?: string
        ): TResult[];

        /**
         * @see lodash_map
         */
        map<T, TObject extends {}>(
            collection: List<T>|Dictionary<T>|NumericDictionary<T>,
            iteratee?: TObject
        ): boolean[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_map
         */
        map<TResult>(
            iteratee?: ListIterator<T, TResult>
        ): LoDashImplicitArrayWrapper<TResult>;

        /**
         * @see lodash_map
         */
        map<TResult>(
            iteratee?: string
        ): LoDashImplicitArrayWrapper<TResult>;

        /**
         * @see lodash_map
         */
        map<TObject extends {}>(
            iteratee?: TObject
        ): LoDashImplicitArrayWrapper<boolean>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_map
         */
        map<TValue, TResult>(
            iteratee?: ListIterator<TValue, TResult>|DictionaryIterator<TValue, TResult>
        ): LoDashImplicitArrayWrapper<TResult>;

        /**
         * @see lodash_map
         */
        map<TValue, TResult>(
            iteratee?: string
        ): LoDashImplicitArrayWrapper<TResult>;

        /**
         * @see lodash_map
         */
        map<TObject extends {}>(
            iteratee?: TObject
        ): LoDashImplicitArrayWrapper<boolean>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_map
         */
        map<TResult>(
            iteratee?: ListIterator<T, TResult>
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_map
         */
        map<TResult>(
            iteratee?: string
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_map
         */
        map<TObject extends {}>(
            iteratee?: TObject
        ): LoDashExplicitArrayWrapper<boolean>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_map
         */
        map<TValue, TResult>(
            iteratee?: ListIterator<TValue, TResult>|DictionaryIterator<TValue, TResult>
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_map
         */
        map<TValue, TResult>(
            iteratee?: string
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_map
         */
        map<TObject extends {}>(
            iteratee?: TObject
        ): LoDashExplicitArrayWrapper<boolean>;
    }

    //lodash_partition
    interface LoDashStatic {
        /**
         * Creates an array of elements split into two groups, the first of which contains elements predicate returns truthy for,
         * while the second of which contains elements predicate returns falsey for.
         * The predicate is bound to thisArg and invoked with three arguments: (value, index|key, collection).
         *
         * If a property name is provided for predicate the created lodash_property style callback
         * returns the property value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback
         * returns true for elements that have a matching property value, else false.
         *
         * If an object is provided for predicate the created lodash_matches style callback returns
         * true for elements that have the properties of the given object, else false.
         *
         * @param collection The collection to iterate over.
         * @param callback The function called per iteration.
         * @param thisArg The this binding of predicate.
         * @return Returns the array of grouped elements.
         **/
        partition<T>(
            collection: List<T>,
            callback: ListIterator<T, boolean>): T[][];

        /**
         * @see lodash_partition
         **/
        partition<T>(
            collection: Dictionary<T>,
            callback: DictionaryIterator<T, boolean>): T[][];

        /**
         * @see lodash_partition
         **/
        partition<W, T>(
            collection: List<T>,
            whereValue: W): T[][];

        /**
         * @see lodash_partition
         **/
        partition<W, T>(
            collection: Dictionary<T>,
            whereValue: W): T[][];

        /**
         * @see lodash_partition
         **/
        partition<T>(
            collection: List<T>,
            path: string,
            srcValue: any): T[][];

        /**
         * @see lodash_partition
         **/
        partition<T>(
            collection: Dictionary<T>,
            path: string,
            srcValue: any): T[][];

        /**
         * @see lodash_partition
         **/
        partition<T>(
            collection: List<T>,
            pluckValue: string): T[][];

        /**
         * @see lodash_partition
         **/
        partition<T>(
            collection: Dictionary<T>,
            pluckValue: string): T[][];
    }

    interface LoDashImplicitStringWrapper {
        /**
         * @see lodash_partition
         */
        partition(
            callback: ListIterator<string, boolean>): LoDashImplicitArrayWrapper<string[]>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_partition
         */
        partition(
            callback: ListIterator<T, boolean>): LoDashImplicitArrayWrapper<T[]>;
        /**
         * @see lodash_partition
         */
        partition<W>(
            whereValue: W): LoDashImplicitArrayWrapper<T[]>;
        /**
         * @see lodash_partition
         */
        partition(
            path: string,
            srcValue: any): LoDashImplicitArrayWrapper<T[]>;
        /**
         * @see lodash_partition
         */
        partition(
            pluckValue: string): LoDashImplicitArrayWrapper<T[]>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_partition
         */
        partition<TResult>(
            callback: ListIterator<TResult, boolean>): LoDashImplicitArrayWrapper<TResult[]>;

        /**
         * @see lodash_partition
         */
        partition<TResult>(
            callback: DictionaryIterator<TResult, boolean>): LoDashImplicitArrayWrapper<TResult[]>;

        /**
         * @see lodash_partition
         */
        partition<W, TResult>(
            whereValue: W): LoDashImplicitArrayWrapper<TResult[]>;

        /**
         * @see lodash_partition
         */
        partition<TResult>(
            path: string,
            srcValue: any): LoDashImplicitArrayWrapper<TResult[]>;

        /**
         * @see lodash_partition
         */
        partition<TResult>(
            pluckValue: string): LoDashImplicitArrayWrapper<TResult[]>;
    }

    //lodash_reduce
    interface LoDashStatic {
        /**
         * Reduces a collection to a value which is the accumulated result of running each
         * element in the collection through the callback, where each successive callback execution
         * consumes the return value of the previous execution. If accumulator is not provided the
         * first element of the collection will be used as the initial accumulator value. The callback
         * is bound to thisArg and invoked with four arguments; (accumulator, value, index|key, collection).
         * @param collection The collection to iterate over.
         * @param callback The function called per iteration.
         * @param accumulator Initial value of the accumulator.
         * @param thisArg The this binding of callback.
         * @return Returns the accumulated value.
         **/
        reduce<T, TResult>(
            collection: Array<T>,
            callback: MemoIterator<T, TResult>,
            accumulator: TResult): TResult;

        /**
         * @see lodash_reduce
         **/
        reduce<T, TResult>(
            collection: List<T>,
            callback: MemoIterator<T, TResult>,
            accumulator: TResult): TResult;

        /**
         * @see lodash_reduce
         **/
        reduce<T, TResult>(
            collection: Dictionary<T>,
            callback: MemoIterator<T, TResult>,
            accumulator: TResult): TResult;

        /**
         * @see lodash_reduce
         **/
        reduce<T, TResult>(
            collection: NumericDictionary<T>,
            callback: MemoIterator<T, TResult>,
            accumulator: TResult): TResult;

        /**
         * @see lodash_reduce
         **/
        reduce<T, TResult>(
            collection: Array<T>,
            callback: MemoIterator<T, TResult>): TResult;

        /**
         * @see lodash_reduce
         **/
        reduce<T, TResult>(
            collection: List<T>,
            callback: MemoIterator<T, TResult>): TResult;

        /**
         * @see lodash_reduce
         **/
        reduce<T, TResult>(
            collection: Dictionary<T>,
            callback: MemoIterator<T, TResult>): TResult;

        /**
         * @see lodash_reduce
         **/
        reduce<T, TResult>(
            collection: NumericDictionary<T>,
            callback: MemoIterator<T, TResult>): TResult;

    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_reduce
         **/
        reduce<TResult>(
            callback: MemoIterator<T, TResult>,
            accumulator: TResult): TResult;

        /**
         * @see lodash_reduce
         **/
        reduce<TResult>(
            callback: MemoIterator<T, TResult>): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_reduce
         **/
        reduce<TValue, TResult>(
            callback: MemoIterator<TValue, TResult>,
            accumulator: TResult): TResult;

        /**
         * @see lodash_reduce
         **/
        reduce<TValue, TResult>(
            callback: MemoIterator<TValue, TResult>): TResult;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_reduce
         **/
        reduce<TValue, TResult>(
            callback: MemoIterator<TValue, TResult>,
            accumulator: TResult): LoDashExplicitObjectWrapper<TResult>;

        /**
         * @see lodash_reduce
         **/
        reduce<TValue, TResult>(
            callback: MemoIterator<TValue, TResult>): LoDashExplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**LoDashExplicitWrapper
         * @see lodash_reduce
         */
        reduce<TResult>(
            callback: MemoIterator<T, TResult>,
            accumulator: TResult): LoDashExplicitWrapper<TResult>;

        /**
         * @see lodash_reduce
         */
        reduce<TResult>(
            callback: MemoIterator<T, TResult>): LoDashExplicitWrapper<TResult>;
    }

    //lodash_reduceRight
    interface LoDashStatic {
        /**
         * This method is like lodash_reduce except that it iterates over elements of a collection from
         * right to left.
         * @param collection The collection to iterate over.
         * @param callback The function called per iteration.
         * @param accumulator Initial value of the accumulator.
         * @param thisArg The this binding of callback.
         * @return The accumulated value.
         **/
        reduceRight<T, TResult>(
            collection: Array<T>,
            callback: MemoIterator<T, TResult>,
            accumulator: TResult): TResult;

        /**
         * @see lodash_reduceRight
         **/
        reduceRight<T, TResult>(
            collection: List<T>,
            callback: MemoIterator<T, TResult>,
            accumulator: TResult): TResult;

        /**
         * @see lodash_reduceRight
         **/
        reduceRight<T, TResult>(
            collection: Dictionary<T>,
            callback: MemoIterator<T, TResult>,
            accumulator: TResult): TResult;

        /**
         * @see lodash_reduceRight
         **/
        reduceRight<T, TResult>(
            collection: Array<T>,
            callback: MemoIterator<T, TResult>): TResult;

        /**
         * @see lodash_reduceRight
         **/
        reduceRight<T, TResult>(
            collection: List<T>,
            callback: MemoIterator<T, TResult>): TResult;

        /**
         * @see lodash_reduceRight
         **/
        reduceRight<T, TResult>(
            collection: Dictionary<T>,
            callback: MemoIterator<T, TResult>): TResult;
    }

    //lodash_reject
    interface LoDashStatic {
        /**
         * The opposite of lodash_filter; this method returns the elements of collection that predicate does not return
         * truthy for.
         *
         * @param collection The collection to iterate over.
         * @param predicate The function invoked per iteration.
         * @param thisArg The this binding of predicate.
         * @return Returns the new filtered array.
         */
        reject<T>(
            collection: List<T>,
            predicate?: ListIterator<T, boolean>
        ): T[];

        /**
         * @see lodash_reject
         */
        reject<T>(
            collection: Dictionary<T>,
            predicate?: DictionaryIterator<T, boolean>
        ): T[];

        /**
         * @see lodash_reject
         */
        reject(
            collection: string,
            predicate?: StringIterator<boolean>
        ): string[];

        /**
         * @see lodash_reject
         */
        reject<T>(
            collection: List<T>|Dictionary<T>,
            predicate: string
        ): T[];

        /**
         * @see lodash_reject
         */
        reject<W extends {}, T>(
            collection: List<T>|Dictionary<T>,
            predicate: W
        ): T[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_reject
         */
        reject(
            predicate?: StringIterator<boolean>
        ): LoDashImplicitArrayWrapper<string>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_reject
         */
        reject(
            predicate: ListIterator<T, boolean>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_reject
         */
        reject(
            predicate: string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_reject
         */
        reject<W>(predicate: W): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_reject
         */
        reject<T>(
            predicate: ListIterator<T, boolean>|DictionaryIterator<T, boolean>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_reject
         */
        reject<T>(
            predicate: string
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_reject
         */
        reject<W, T>(predicate: W): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_reject
         */
        reject(
            predicate?: StringIterator<boolean>
        ): LoDashExplicitArrayWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_reject
         */
        reject(
            predicate: ListIterator<T, boolean>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_reject
         */
        reject(
            predicate: string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_reject
         */
        reject<W>(predicate: W): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_reject
         */
        reject<T>(
            predicate: ListIterator<T, boolean>|DictionaryIterator<T, boolean>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_reject
         */
        reject<T>(
            predicate: string
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_reject
         */
        reject<W, T>(predicate: W): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_sample
    interface LoDashStatic {
        /**
         * Gets a random element from collection.
         *
         * @param collection The collection to sample.
         * @return Returns the random element.
         */
        sample<T>(
            collection: List<T>|Dictionary<T>|NumericDictionary<T>
        ): T;

        /**
         * @see lodash_sample
         */
        sample<O extends Object, T>(
            collection: O
        ): T;

        /**
         * @see lodash_sample
         */
        sample<T>(
            collection: Object
        ): T;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_sample
         */
        sample(): string;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_sample
         */
        sample(): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_sample
         */
        sample<T>(): T;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_sample
         */
        sample(): LoDashExplicitWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_sample
         */
        sample<TWrapper>(): TWrapper;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_sample
         */
        sample<TWrapper>(): TWrapper;
    }

    //lodash_sampleSize
    interface LoDashStatic {
        /**
         * Gets n random elements at unique keys from collection up to the size of collection.
         *
         * @param collection The collection to sample.
         * @param n The number of elements to sample.
         * @return Returns the random elements.
         */
        sampleSize<T>(
            collection: List<T>|Dictionary<T>|NumericDictionary<T>,
            n?: number
        ): T[];

        /**
         * @see lodash_sampleSize
         */
        sampleSize<O extends Object, T>(
            collection: O,
            n?: number
        ): T[];

        /**
         * @see lodash_sampleSize
         */
        sampleSize<T>(
            collection: Object,
            n?: number
        ): T[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_sampleSize
         */
        sampleSize(
            n?: number
        ): LoDashImplicitArrayWrapper<string>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_sampleSize
         */
        sampleSize(
            n?: number
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_sampleSize
         */
        sampleSize<T>(
            n?: number
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_sampleSize
         */
        sampleSize(
            n?: number
        ): LoDashExplicitArrayWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_sampleSize
         */
        sampleSize(
            n?: number
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_sampleSize
         */
        sampleSize<T>(
            n?: number
        ): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_shuffle
    interface LoDashStatic {
        /**
         * Creates an array of shuffled values, using a version of the Fisher-Yates shuffle.
         *
         * @param collection The collection to shuffle.
         * @return Returns the new shuffled array.
         */
        shuffle<T>(collection: List<T>|Dictionary<T>): T[];

        /**
         * @see lodash_shuffle
         */
        shuffle(collection: string): string[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_shuffle
         */
        shuffle(): LoDashImplicitArrayWrapper<string>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_shuffle
         */
        shuffle(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_shuffle
         */
        shuffle<T>(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_shuffle
         */
        shuffle(): LoDashExplicitArrayWrapper<string>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_shuffle
         */
        shuffle(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_shuffle
         */
        shuffle<T>(): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_size
    interface LoDashStatic {
        /**
         * Gets the size of collection by returning its length for array-like values or the number of own enumerable
         * properties for objects.
         *
         * @param collection The collection to inspect.
         * @return Returns the size of collection.
         */
        size<T>(collection: List<T>|Dictionary<T>): number;

        /**
         * @see lodash_size
         */
        size(collection: string): number;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_size
         */
        size(): number;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_size
         */
        size(): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_size
         */
        size(): number;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_size
         */
        size(): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_size
         */
        size(): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_size
         */
        size(): LoDashExplicitWrapper<number>;
    }

    //lodash_some
    interface LoDashStatic {
        /**
         * Checks if predicate returns truthy for any element of collection. Iteration is stopped once predicate
         * returns truthy. The predicate is invoked with three arguments: (value, index|key, collection).
         *
         * @param collection The collection to iterate over.
         * @param predicate The function invoked per iteration.
         * @return Returns true if any element passes the predicate check, else false.
         */
        some<T>(
            collection: List<T>,
            predicate?: ListIterator<T, boolean>
        ): boolean;

        /**
         * @see lodash_some
         */
        some<T>(
            collection: Dictionary<T>,
            predicate?: DictionaryIterator<T, boolean>
        ): boolean;

        /**
         * @see lodash_some
         */
        some<T>(
            collection: NumericDictionary<T>,
            predicate?: NumericDictionaryIterator<T, boolean>
        ): boolean;

        /**
         * @see lodash_some
         */
        some(
            collection: Object,
            predicate?: ObjectIterator<any, boolean>
        ): boolean;

        /**
         * @see lodash_some
         */
        some<T>(
            collection: List<T>|Dictionary<T>|NumericDictionary<T>,
            predicate?: string|[string, any]
        ): boolean;


        /**
         * @see lodash_some
         */
        some(
            collection: Object,
            predicate?: string|[string, any]
        ): boolean;

        /**
         * @see lodash_some
         */
        some<TObject extends {}, T>(
            collection: List<T>|Dictionary<T>|NumericDictionary<T>,
            predicate?: TObject
        ): boolean;

        /**
         * @see lodash_some
         */
        some<T>(
            collection: List<T>|Dictionary<T>|NumericDictionary<T>,
            predicate?: Object
        ): boolean;

        /**
         * @see lodash_some
         */
        some<TObject extends {}>(
            collection: Object,
            predicate?: TObject
        ): boolean;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_some
         */
        some(
            predicate?: ListIterator<T, boolean>|NumericDictionaryIterator<T, boolean>
        ): boolean;

        /**
         * @see lodash_some
         */
        some(
            predicate?: string|[string, any]
        ): boolean;

        /**
         * @see lodash_some
         */
        some<TObject extends {}>(
            predicate?: TObject
        ): boolean;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_some
         */
        some<TResult>(
            predicate?: ListIterator<TResult, boolean>|DictionaryIterator<TResult, boolean>|NumericDictionaryIterator<T, boolean>|ObjectIterator<any, boolean>
        ): boolean;

        /**
         * @see lodash_some
         */
        some(
            predicate?: string|[string, any]
        ): boolean;

        /**
         * @see lodash_some
         */
        some<TObject extends {}>(
            predicate?: TObject
        ): boolean;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_some
         */
        some(
            predicate?: ListIterator<T, boolean>|NumericDictionaryIterator<T, boolean>
        ): LoDashExplicitWrapper<boolean>;

        /**
         * @see lodash_some
         */
        some(
            predicate?: string|[string, any]
        ): LoDashExplicitWrapper<boolean>;

        /**
         * @see lodash_some
         */
        some<TObject extends {}>(
            predicate?: TObject
        ): LoDashExplicitWrapper<boolean>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_some
         */
        some<TResult>(
            predicate?: ListIterator<TResult, boolean>|DictionaryIterator<TResult, boolean>|NumericDictionaryIterator<T, boolean>|ObjectIterator<any, boolean>
        ): LoDashExplicitWrapper<boolean>;

        /**
         * @see lodash_some
         */
        some(
            predicate?: string|[string, any]
        ): LoDashExplicitWrapper<boolean>;

        /**
         * @see lodash_some
         */
        some<TObject extends {}>(
            predicate?: TObject
        ): LoDashExplicitWrapper<boolean>;
    }

    //lodash_sortBy
    interface LoDashStatic {
        /**
         * Creates an array of elements, sorted in ascending order by the results of
         * running each element in a collection through each iteratee. This method
         * performs a stable sort, that is, it preserves the original sort order of
         * equal elements. The iteratees are invoked with one argument: (value).
         *
         * @static
         * @memberOf _
         * @category Collection
         * @param {Array|Object} collection The collection to iterate over.
         * @param {...(Function|Function[]|Object|Object[]|string|string[])} [iteratees=[lodash_identity]]
         *  The iteratees to sort by, specified individually or in arrays.
         * @returns {Array} Returns the new sorted array.
         * @example
         *
         * var users = [
         *   { 'user': 'fred',   'age': 48 },
         *   { 'user': 'barney', 'age': 36 },
         *   { 'user': 'fred',   'age': 42 },
         *   { 'user': 'barney', 'age': 34 }
         * ];
         *
         * lodash_sortBy(users, function(o) { return o.user; });
         * // => objects for [['barney', 36], ['barney', 34], ['fred', 48], ['fred', 42]]
         *
         * lodash_sortBy(users, ['user', 'age']);
         * // => objects for [['barney', 34], ['barney', 36], ['fred', 42], ['fred', 48]]
         *
         * lodash_sortBy(users, 'user', function(o) {
         *   return Math.floor(o.age / 10);
         * });
         * // => objects for [['barney', 36], ['barney', 34], ['fred', 48], ['fred', 42]]
         */
        sortBy<T, TSort>(
            collection: List<T>,
            iteratee?: ListIterator<T, TSort>
        ): T[];

        /**
         * @see lodash_sortBy
         */
        sortBy<T, TSort>(
            collection: Dictionary<T>,
            iteratee?: DictionaryIterator<T, TSort>
        ): T[];

        /**
         * @see lodash_sortBy
         */
        sortBy<T>(
            collection: List<T>|Dictionary<T>,
            iteratee: string
        ): T[];

        /**
         * @see lodash_sortBy
         */
        sortBy<W extends {}, T>(
            collection: List<T>|Dictionary<T>,
            whereValue: W
        ): T[];

        /**
         * @see lodash_sortBy
         */
        sortBy<T>(
            collection: List<T>|Dictionary<T>
        ): T[];

        /**
         * @see lodash_sortBy
         */
        sortBy<T>(
            collection: (Array<T>|List<T>),
            iteratees: (ListIterator<T, any>|string|Object)[]): T[];

        /**
         * @see lodash_sortBy
         */
        sortBy<T>(
            collection: (Array<T>|List<T>),
            ...iteratees: (ListIterator<T, boolean>|Object|string)[]): T[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_sortBy
         */
        sortBy<TSort>(
            iteratee?: ListIterator<T, TSort>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         */
        sortBy(iteratee: string): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         */
        sortBy<W extends {}>(whereValue: W): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         */
        sortBy(): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         */
        sortBy(...iteratees: (ListIterator<T, boolean>|Object|string)[]): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         **/
        sortBy(iteratees: (ListIterator<T, any>|string|Object)[]): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_sortBy
         */
        sortBy<T, TSort>(
            iteratee?: ListIterator<T, TSort>|DictionaryIterator<T, TSort>
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         */
        sortBy<T>(iteratee: string): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         */
        sortBy<W extends {}, T>(whereValue: W): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         */
        sortBy<T>(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_sortBy
         */
        sortBy<TSort>(
            iteratee?: ListIterator<T, TSort>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         */
        sortBy(iteratee: string): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         */
        sortBy<W extends {}>(whereValue: W): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         */
        sortBy(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_sortBy
         */
        sortBy<T, TSort>(
            iteratee?: ListIterator<T, TSort>|DictionaryIterator<T, TSort>
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         */
        sortBy<T>(iteratee: string): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         */
        sortBy<W extends {}, T>(whereValue: W): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_sortBy
         */
        sortBy<T>(): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_orderBy
    interface LoDashStatic {
        /**
         * This method is like `lodash_sortBy` except that it allows specifying the sort
         * orders of the iteratees to sort by. If `orders` is unspecified, all values
         * are sorted in ascending order. Otherwise, specify an order of "desc" for
         * descending or "asc" for ascending sort order of corresponding values.
         *
         * @static
         * @memberOf _
         * @category Collection
         * @param {Array|Object} collection The collection to iterate over.
         * @param {Function[]|Object[]|string[]} [iteratees=[lodash_identity]] The iteratees to sort by.
         * @param {string[]} [orders] The sort orders of `iteratees`.
         * @param- {Object} [guard] Enables use as an iteratee for functions like `lodash_reduce`.
         * @returns {Array} Returns the new sorted array.
         * @example
         *
         * var users = [
         *   { 'user': 'fred',   'age': 48 },
         *   { 'user': 'barney', 'age': 34 },
         *   { 'user': 'fred',   'age': 42 },
         *   { 'user': 'barney', 'age': 36 }
         * ];
         *
         * // sort by `user` in ascending order and by `age` in descending order
         * lodash_orderBy(users, ['user', 'age'], ['asc', 'desc']);
         * // => objects for [['barney', 36], ['barney', 34], ['fred', 48], ['fred', 42]]
         */
        orderBy<W extends Object, T>(
            collection: List<T>,
            iteratees: ListIterator<T, any>|string|W|(ListIterator<T, any>|string|W)[],
            orders?: boolean|string|(boolean|string)[]
        ): T[];

        /**
         * @see lodash_orderBy
         */
        orderBy<T>(
            collection: List<T>,
            iteratees: ListIterator<T, any>|string|Object|(ListIterator<T, any>|string|Object)[],
            orders?: boolean|string|(boolean|string)[]
        ): T[];

        /**
         * @see lodash_orderBy
         */
        orderBy<W extends Object, T>(
            collection: NumericDictionary<T>,
            iteratees: NumericDictionaryIterator<T, any>|string|W|(NumericDictionaryIterator<T, any>|string|W)[],
            orders?: boolean|string|(boolean|string)[]
        ): T[];

        /**
         * @see lodash_orderBy
         */
        orderBy<T>(
            collection: NumericDictionary<T>,
            iteratees: NumericDictionaryIterator<T, any>|string|Object|(NumericDictionaryIterator<T, any>|string|Object)[],
            orders?: boolean|string|(boolean|string)[]
        ): T[];

        /**
         * @see lodash_orderBy
         */
        orderBy<W extends Object, T>(
            collection: Dictionary<T>,
            iteratees: DictionaryIterator<T, any>|string|W|(DictionaryIterator<T, any>|string|W)[],
            orders?: boolean|string|(boolean|string)[]
        ): T[];

        /**
         * @see lodash_orderBy
         */
        orderBy<T>(
            collection: Dictionary<T>,
            iteratees: DictionaryIterator<T, any>|string|Object|(DictionaryIterator<T, any>|string|Object)[],
            orders?: boolean|string|(boolean|string)[]
        ): T[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_orderBy
         */
        orderBy(
            iteratees: ListIterator<T, any>|string|(ListIterator<T, any>|string)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_orderBy
         */
        orderBy<W extends Object>(
            iteratees: ListIterator<T, any>|string|W|(ListIterator<T, any>|string|W)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_orderBy
         */
        orderBy<W extends Object, T>(
            iteratees: ListIterator<T, any>|string|W|(ListIterator<T, any>|string|W)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_orderBy
         */
        orderBy<T>(
            iteratees: ListIterator<T, any>|string|Object|(ListIterator<T, any>|string|Object)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_orderBy
         */
        orderBy<W extends Object, T>(
            iteratees: NumericDictionaryIterator<T, any>|string|W|(NumericDictionaryIterator<T, any>|string|W)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_orderBy
         */
        orderBy<T>(
            iteratees: NumericDictionaryIterator<T, any>|string|Object|(NumericDictionaryIterator<T, any>|string|Object)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_orderBy
         */
        orderBy<W extends Object, T>(
            iteratees: DictionaryIterator<T, any>|string|W|(DictionaryIterator<T, any>|string|W)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashImplicitArrayWrapper<T>;

        /**
         * @see lodash_orderBy
         */
        orderBy<T>(
            iteratees: DictionaryIterator<T, any>|string|Object|(DictionaryIterator<T, any>|string|Object)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_orderBy
         */
        orderBy(
            iteratees: ListIterator<T, any>|string|(ListIterator<T, any>|string)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_orderBy
         */
        orderBy<W extends Object>(
            iteratees: ListIterator<T, any>|string|W|(ListIterator<T, any>|string|W)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_orderBy
         */
        orderBy<W extends Object, T>(
            iteratees: ListIterator<T, any>|string|W|(ListIterator<T, any>|string|W)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_orderBy
         */
        orderBy<T>(
            iteratees: ListIterator<T, any>|string|Object|(ListIterator<T, any>|string|Object)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_orderBy
         */
        orderBy<W extends Object, T>(
            iteratees: NumericDictionaryIterator<T, any>|string|W|(NumericDictionaryIterator<T, any>|string|W)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_orderBy
         */
        orderBy<T>(
            iteratees: NumericDictionaryIterator<T, any>|string|Object|(NumericDictionaryIterator<T, any>|string|Object)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_orderBy
         */
        orderBy<W extends Object, T>(
            iteratees: DictionaryIterator<T, any>|string|W|(DictionaryIterator<T, any>|string|W)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashExplicitArrayWrapper<T>;

        /**
         * @see lodash_orderBy
         */
        orderBy<T>(
            iteratees: DictionaryIterator<T, any>|string|Object|(DictionaryIterator<T, any>|string|Object)[],
            orders?: boolean|string|(boolean|string)[]
        ): LoDashExplicitArrayWrapper<T>;
    }

    /********
     * Date *
     ********/

        //lodash_now
    interface LoDashStatic {
        /**
         * Gets the number of milliseconds that have elapsed since the Unix epoch (1 January 1970 00:00:00 UTC).
         *
         * @return The number of milliseconds.
         */
        now(): number;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_now
         */
        now(): number;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_now
         */
        now(): LoDashExplicitWrapper<number>;
    }

    /*************
     * Functions *
     *************/

        //lodash_after
    interface LoDashStatic {
        /**
         * The opposite of lodash_before; this method creates a function that invokes func once it’s called n or more times.
         *
         * @param n The number of calls before func is invoked.
         * @param func The function to restrict.
         * @return Returns the new restricted function.
         */
        after<TFunc extends Function>(
            n: number,
            func: TFunc
        ): TFunc;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_after
         **/
        after<TFunc extends Function>(func: TFunc): LoDashImplicitObjectWrapper<TFunc>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_after
         **/
        after<TFunc extends Function>(func: TFunc): LoDashExplicitObjectWrapper<TFunc>;
    }

    //lodash_ary
    interface LoDashStatic {
        /**
         * Creates a function that accepts up to n arguments ignoring any additional arguments.
         *
         * @param func The function to cap arguments for.
         * @param n The arity cap.
         * @returns Returns the new function.
         */
        ary<TResult extends Function>(
            func: Function,
            n?: number
        ): TResult;

        ary<T extends Function, TResult extends Function>(
            func: T,
            n?: number
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_ary
         */
        ary<TResult extends Function>(n?: number): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_ary
         */
        ary<TResult extends Function>(n?: number): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_before
    interface LoDashStatic {
        /**
         * Creates a function that invokes func, with the this binding and arguments of the created function, while
         * it’s called less than n times. Subsequent calls to the created function return the result of the last func
         * invocation.
         *
         * @param n The number of calls at which func is no longer invoked.
         * @param func The function to restrict.
         * @return Returns the new restricted function.
         */
        before<TFunc extends Function>(
            n: number,
            func: TFunc
        ): TFunc;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_before
         **/
        before<TFunc extends Function>(func: TFunc): LoDashImplicitObjectWrapper<TFunc>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_before
         **/
        before<TFunc extends Function>(func: TFunc): LoDashExplicitObjectWrapper<TFunc>;
    }

    //lodash_bind
    interface FunctionBind {
        placeholder: any;

        <T extends Function, TResult extends Function>(
            func: T,
            thisArg: any,
            ...partials: any[]
        ): TResult;

        <TResult extends Function>(
            func: Function,
            thisArg: any,
            ...partials: any[]
        ): TResult;
    }

    interface LoDashStatic {
        /**
         * Creates a function that invokes func with the this binding of thisArg and prepends any additional lodash_bind
         * arguments to those provided to the bound function.
         *
         * The lodash_bind.placeholder value, which defaults to _ in monolithic builds, may be used as a placeholder for
         * partially applied arguments.
         *
         * Note: Unlike native Function#bind this method does not set the "length" property of bound functions.
         *
         * @param func The function to bind.
         * @param thisArg The this binding of func.
         * @param partials The arguments to be partially applied.
         * @return Returns the new bound function.
         */
        bind: FunctionBind;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_bind
         */
        bind<TResult extends Function>(
            thisArg: any,
            ...partials: any[]
        ): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_bind
         */
        bind<TResult extends Function>(
            thisArg: any,
            ...partials: any[]
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_bindAll
    interface LoDashStatic {
        /**
         * Binds methods of an object to the object itself, overwriting the existing method. Method names may be
         * specified as individual arguments or as arrays of method names. If no method names are provided all
         * enumerable function properties, own and inherited, of object are bound.
         *
         * Note: This method does not set the "length" property of bound functions.
         *
         * @param object The object to bind and assign the bound methods to.
         * @param methodNames The object method names to bind, specified as individual method names or arrays of
         * method names.
         * @return Returns object.
         */
        bindAll<T>(
            object: T,
            ...methodNames: (string|string[])[]
        ): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_bindAll
         */
        bindAll(...methodNames: (string|string[])[]): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_bindAll
         */
        bindAll(...methodNames: (string|string[])[]): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_bindKey
    interface FunctionBindKey {
        placeholder: any;

        <T extends Object, TResult extends Function>(
            object: T,
            key: any,
            ...partials: any[]
        ): TResult;

        <TResult extends Function>(
            object: Object,
            key: any,
            ...partials: any[]
        ): TResult;
    }

    interface LoDashStatic {
        /**
         * Creates a function that invokes the method at object[key] and prepends any additional lodash_bindKey arguments
         * to those provided to the bound function.
         *
         * This method differs from lodash_bind by allowing bound functions to reference methods that may be redefined
         * or don’t yet exist. See Peter Michaux’s article for more details.
         *
         * The lodash_bindKey.placeholder value, which defaults to _ in monolithic builds, may be used as a placeholder
         * for partially applied arguments.
         *
         * @param object The object the method belongs to.
         * @param key The key of the method.
         * @param partials The arguments to be partially applied.
         * @return Returns the new bound function.
         */
        bindKey: FunctionBindKey;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_bindKey
         */
        bindKey<TResult extends Function>(
            key: any,
            ...partials: any[]
        ): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_bindKey
         */
        bindKey<TResult extends Function>(
            key: any,
            ...partials: any[]
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_createCallback
    interface LoDashStatic {
        /**
         * Produces a callback bound to an optional thisArg. If func is a property name the created
         * callback will return the property value for a given element. If func is an object the created
         * callback will return true for elements that contain the equivalent object properties,
         * otherwise it will return false.
         * @param func The value to convert to a callback.
         * @param thisArg The this binding of the created callback.
         * @param argCount The number of arguments the callback accepts.
         * @return A callback function.
         **/
        createCallback(
            func: string,
            argCount?: number): () => any;

        /**
         * @see lodash_createCallback
         **/
        createCallback(
            func: Dictionary<any>,
            argCount?: number): () => boolean;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_createCallback
         **/
        createCallback(
            argCount?: number): LoDashImplicitObjectWrapper<() => any>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_createCallback
         **/
        createCallback(
            argCount?: number): LoDashImplicitObjectWrapper<() => any>;
    }

    //lodash_curry
    interface LoDashStatic {
        /**
         * Creates a function that accepts one or more arguments of func that when called either invokes func returning
         * its result, if all func arguments have been provided, or returns a function that accepts one or more of the
         * remaining func arguments, and so on. The arity of func may be specified if func.length is not sufficient.
         * @param func The function to curry.
         * @return Returns the new curried function.
         */
        curry<T1, R>(func: (t1: T1) => R):
            CurriedFunction1<T1, R>;
        /**
         * Creates a function that accepts one or more arguments of func that when called either invokes func returning
         * its result, if all func arguments have been provided, or returns a function that accepts one or more of the
         * remaining func arguments, and so on. The arity of func may be specified if func.length is not sufficient.
         * @param func The function to curry.
         * @return Returns the new curried function.
         */
        curry<T1, T2, R>(func: (t1: T1, t2: T2) => R):
            CurriedFunction2<T1, T2, R>;
        /**
         * Creates a function that accepts one or more arguments of func that when called either invokes func returning
         * its result, if all func arguments have been provided, or returns a function that accepts one or more of the
         * remaining func arguments, and so on. The arity of func may be specified if func.length is not sufficient.
         * @param func The function to curry.
         * @return Returns the new curried function.
         */
        curry<T1, T2, T3, R>(func: (t1: T1, t2: T2, t3: T3) => R):
            CurriedFunction3<T1, T2, T3, R>;
        /**
         * Creates a function that accepts one or more arguments of func that when called either invokes func returning
         * its result, if all func arguments have been provided, or returns a function that accepts one or more of the
         * remaining func arguments, and so on. The arity of func may be specified if func.length is not sufficient.
         * @param func The function to curry.
         * @return Returns the new curried function.
         */
        curry<T1, T2, T3, T4, R>(func: (t1: T1, t2: T2, t3: T3, t4: T4) => R):
            CurriedFunction4<T1, T2, T3, T4, R>;
        /**
         * Creates a function that accepts one or more arguments of func that when called either invokes func returning
         * its result, if all func arguments have been provided, or returns a function that accepts one or more of the
         * remaining func arguments, and so on. The arity of func may be specified if func.length is not sufficient.
         * @param func The function to curry.
         * @return Returns the new curried function.
         */
        curry<T1, T2, T3, T4, T5, R>(func: (t1: T1, t2: T2, t3: T3, t4: T4, t5: T5) => R):
            CurriedFunction5<T1, T2, T3, T4, T5, R>;
        /**
         * Creates a function that accepts one or more arguments of func that when called either invokes func returning
         * its result, if all func arguments have been provided, or returns a function that accepts one or more of the
         * remaining func arguments, and so on. The arity of func may be specified if func.length is not sufficient.
         * @param func The function to curry.
         * @param arity The arity of func.
         * @return Returns the new curried function.
         */
        curry<TResult extends Function>(
            func: Function,
            arity?: number): TResult;
    }

    interface CurriedFunction1<T1, R> {
        (): CurriedFunction1<T1, R>;
        (t1: T1): R;
    }

    interface CurriedFunction2<T1, T2, R> {
        (): CurriedFunction2<T1, T2, R>;
        (t1: T1): CurriedFunction1<T2, R>;
        (t1: T1, t2: T2): R;
    }

    interface CurriedFunction3<T1, T2, T3, R> {
        (): CurriedFunction3<T1, T2, T3, R>;
        (t1: T1): CurriedFunction2<T2, T3, R>;
        (t1: T1, t2: T2): CurriedFunction1<T3, R>;
        (t1: T1, t2: T2, t3: T3): R;
    }

    interface CurriedFunction4<T1, T2, T3, T4, R> {
        (): CurriedFunction4<T1, T2, T3, T4, R>;
        (t1: T1): CurriedFunction3<T2, T3, T4, R>;
        (t1: T1, t2: T2): CurriedFunction2<T3, T4, R>;
        (t1: T1, t2: T2, t3: T3): CurriedFunction1<T4, R>;
        (t1: T1, t2: T2, t3: T3, t4: T4): R;
    }

    interface CurriedFunction5<T1, T2, T3, T4, T5, R> {
        (): CurriedFunction5<T1, T2, T3, T4, T5, R>;
        (t1: T1): CurriedFunction4<T2, T3, T4, T5, R>;
        (t1: T1, t2: T2): CurriedFunction3<T3, T4, T5, R>;
        (t1: T1, t2: T2, t3: T3): CurriedFunction2<T4, T5, R>;
        (t1: T1, t2: T2, t3: T3, t4: T4): CurriedFunction1<T5, R>;
        (t1: T1, t2: T2, t3: T3, t4: T4, t5: T5): R;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_curry
         **/
        curry<TResult extends Function>(arity?: number): LoDashImplicitObjectWrapper<TResult>;
    }

    //lodash_curryRight
    interface LoDashStatic {
        /**
         * This method is like lodash_curry except that arguments are applied to func in the manner of lodash_partialRight
         * instead of lodash_partial.
         * @param func The function to curry.
         * @return Returns the new curried function.
         */
        curryRight<T1, R>(func: (t1: T1) => R):
            CurriedFunction1<T1, R>;
        /**
         * This method is like lodash_curry except that arguments are applied to func in the manner of lodash_partialRight
         * instead of lodash_partial.
         * @param func The function to curry.
         * @return Returns the new curried function.
         */
        curryRight<T1, T2, R>(func: (t1: T1, t2: T2) => R):
            CurriedFunction2<T2, T1, R>;
        /**
         * This method is like lodash_curry except that arguments are applied to func in the manner of lodash_partialRight
         * instead of lodash_partial.
         * @param func The function to curry.
         * @return Returns the new curried function.
         */
        curryRight<T1, T2, T3, R>(func: (t1: T1, t2: T2, t3: T3) => R):
            CurriedFunction3<T3, T2, T1, R>;
        /**
         * This method is like lodash_curry except that arguments are applied to func in the manner of lodash_partialRight
         * instead of lodash_partial.
         * @param func The function to curry.
         * @return Returns the new curried function.
         */
        curryRight<T1, T2, T3, T4, R>(func: (t1: T1, t2: T2, t3: T3, t4: T4) => R):
            CurriedFunction4<T4, T3, T2, T1, R>;
        /**
         * This method is like lodash_curry except that arguments are applied to func in the manner of lodash_partialRight
         * instead of lodash_partial.
         * @param func The function to curry.
         * @return Returns the new curried function.
         */
        curryRight<T1, T2, T3, T4, T5, R>(func: (t1: T1, t2: T2, t3: T3, t4: T4, t5: T5) => R):
            CurriedFunction5<T5, T4, T3, T2, T1, R>;
        /**
         * This method is like lodash_curry except that arguments are applied to func in the manner of lodash_partialRight
         * instead of lodash_partial.
         * @param func The function to curry.
         * @param arity The arity of func.
         * @return Returns the new curried function.
         */
        curryRight<TResult extends Function>(
            func: Function,
            arity?: number): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_curryRight
         **/
        curryRight<TResult extends Function>(arity?: number): LoDashImplicitObjectWrapper<TResult>;
    }

    //lodash_debounce
    interface DebounceSettings {
        /**
         * Specify invoking on the leading edge of the timeout.
         */
        leading?: boolean;

        /**
         * The maximum time func is allowed to be delayed before it’s invoked.
         */
        maxWait?: number;

        /**
         * Specify invoking on the trailing edge of the timeout.
         */
        trailing?: boolean;
    }

    interface LoDashStatic {
        /**
         * Creates a debounced function that delays invoking func until after wait milliseconds have elapsed since
         * the last time the debounced function was invoked. The debounced function comes with a cancel method to
         * cancel delayed invocations and a flush method to immediately invoke them. Provide an options object to
         * indicate that func should be invoked on the leading and/or trailing edge of the wait timeout. Subsequent
         * calls to the debounced function return the result of the last func invocation.
         *
         * Note: If leading and trailing options are true, func is invoked on the trailing edge of the timeout only
         * if the the debounced function is invoked more than once during the wait timeout.
         *
         * See David Corbacho’s article for details over the differences between lodash_debounce and lodash_throttle.
         *
         * @param func The function to debounce.
         * @param wait The number of milliseconds to delay.
         * @param options The options object.
         * @param options.leading Specify invoking on the leading edge of the timeout.
         * @param options.maxWait The maximum time func is allowed to be delayed before it’s invoked.
         * @param options.trailing Specify invoking on the trailing edge of the timeout.
         * @return Returns the new debounced function.
         */
        debounce<T extends Function>(
            func: T,
            wait?: number,
            options?: DebounceSettings
        ): Cancelable;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_debounce
         */
        debounce(
            wait?: number,
            options?: DebounceSettings
        ): LoDashImplicitObjectWrapper<Cancelable>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_debounce
         */
        debounce(
            wait?: number,
            options?: DebounceSettings
        ): LoDashExplicitObjectWrapper<Cancelable>;
    }

    //lodash_defer
    interface LoDashStatic {
        /**
         * Defers invoking the func until the current call stack has cleared. Any additional arguments are provided to
         * func when it’s invoked.
         *
         * @param func The function to defer.
         * @param args The arguments to invoke the function with.
         * @return Returns the timer id.
         */
        defer<T extends Function>(
            func: T,
            ...args: any[]
        ): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_defer
         */
        defer(...args: any[]): LoDashImplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_defer
         */
        defer(...args: any[]): LoDashExplicitWrapper<number>;
    }

    //lodash_delay
    interface LoDashStatic {
        /**
         * Invokes func after wait milliseconds. Any additional arguments are provided to func when it’s invoked.
         *
         * @param func The function to delay.
         * @param wait The number of milliseconds to delay invocation.
         * @param args The arguments to invoke the function with.
         * @return Returns the timer id.
         */
        delay<T extends Function>(
            func: T,
            wait: number,
            ...args: any[]
        ): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_delay
         */
        delay(
            wait: number,
            ...args: any[]
        ): LoDashImplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_delay
         */
        delay(
            wait: number,
            ...args: any[]
        ): LoDashExplicitWrapper<number>;
    }

    interface LoDashStatic {
        /**
         * Creates a function that invokes `func` with arguments reversed.
         *
         * @static
         * @memberOf _
         * @category Function
         * @param {Function} func The function to flip arguments for.
         * @returns {Function} Returns the new function.
         * @example
         *
         * var flipped = lodash_flip(function() {
         *   return lodash_toArray(arguments);
         * });
         *
         * flipped('a', 'b', 'c', 'd');
         * // => ['d', 'c', 'b', 'a']
         */
        flip<T extends Function>(func: T): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_flip
         */
        flip(): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_flip
         */
        flip(): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_flow
    interface LoDashStatic {
        /**
         * Creates a function that returns the result of invoking the provided functions with the this binding of the
         * created function, where each successive invocation is supplied the return value of the previous.
         *
         * @param funcs Functions to invoke.
         * @return Returns the new function.
         */
        // 1-argument first function
        flow<A1, R1, R2>(f1: (a1: A1) => R1, f2: (a: R1) => R2): (a1: A1) => R2;
        flow<A1, R1, R2, R3>(f1: (a1: A1) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3): (a1: A1) => R3;
        flow<A1, R1, R2, R3, R4>(f1: (a1: A1) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4): (a1: A1) => R4;
        flow<A1, R1, R2, R3, R4, R5>(f1: (a1: A1) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4, f5: (a: R4) => R5): (a1: A1) => R5;
        flow<A1, R1, R2, R3, R4, R5, R6>(f1: (a1: A1) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4, f5: (a: R4) => R5, f6: (a: R5) => R6): (a1: A1) => R6;
        flow<A1, R1, R2, R3, R4, R5, R6, R7>(f1: (a1: A1) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4, f5: (a: R4) => R5, f6: (a: R5) => R6, f7: (a: R6) => R7): (a1: A1) => R7;
        // 2-argument first function
        flow<A1, A2, R1, R2>(f1: (a1: A1, a2: A2) => R1, f2: (a: R1) => R2): (a1: A1, a2: A2) => R2;
        flow<A1, A2, R1, R2, R3>(f1: (a1: A1, a2: A2) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3): (a1: A1, a2: A2) => R3;
        flow<A1, A2, R1, R2, R3, R4>(f1: (a1: A1, a2: A2) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4): (a1: A1, a2: A2) => R4;
        flow<A1, A2, R1, R2, R3, R4, R5>(f1: (a1: A1, a2: A2) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4, f5: (a: R4) => R5): (a1: A1, a2: A2) => R5;
        flow<A1, A2, R1, R2, R3, R4, R5, R6>(f1: (a1: A1, a2: A2) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4, f5: (a: R4) => R5, f6: (a: R5) => R6): (a1: A1, a2: A2) => R6;
        flow<A1, A2, R1, R2, R3, R4, R5, R6, R7>(f1: (a1: A1, a2: A2) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4, f5: (a: R4) => R5, f6: (a: R5) => R6, f7: (a: R6) => R7): (a1: A1, a2: A2) => R7;
        // 3-argument first function
        flow<A1, A2, A3, R1, R2>(f1: (a1: A1, a2: A2, a3: A3) => R1, f2: (a: R1) => R2): (a1: A1, a2: A2, a3: A3) => R2;
        flow<A1, A2, A3, R1, R2, R3>(f1: (a1: A1, a2: A2, a3: A3) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3): (a1: A1, a2: A2, a3: A3) => R3;
        flow<A1, A2, A3, R1, R2, R3, R4>(f1: (a1: A1, a2: A2, a3: A3) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4): (a1: A1, a2: A2, a3: A3) => R4;
        flow<A1, A2, A3, R1, R2, R3, R4, R5>(f1: (a1: A1, a2: A2, a3: A3) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4, f5: (a: R4) => R5): (a1: A1, a2: A2, a3: A3) => R5;
        flow<A1, A2, A3, R1, R2, R3, R4, R5, R6>(f1: (a1: A1, a2: A2, a3: A3) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4, f5: (a: R4) => R5, f6: (a: R5) => R6): (a1: A1, a2: A2, a3: A3) => R6;
        flow<A1, A2, A3, R1, R2, R3, R4, R5, R6, R7>(f1: (a1: A1, a2: A2, a3: A3) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4, f5: (a: R4) => R5, f6: (a: R5) => R6, f7: (a: R6) => R7): (a1: A1, a2: A2, a3: A3) => R7;
        // 4-argument first function
        flow<A1, A2, A3, A4, R1, R2>(f1: (a1: A1, a2: A2, a3: A3, a4: A4) => R1, f2: (a: R1) => R2): (a1: A1, a2: A2, a3: A3, a4: A4) => R2;
        flow<A1, A2, A3, A4, R1, R2, R3>(f1: (a1: A1, a2: A2, a3: A3, a4: A4) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3): (a1: A1, a2: A2, a3: A3, a4: A4) => R3;
        flow<A1, A2, A3, A4, R1, R2, R3, R4>(f1: (a1: A1, a2: A2, a3: A3, a4: A4) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4): (a1: A1, a2: A2, a3: A3, a4: A4) => R4;
        flow<A1, A2, A3, A4, R1, R2, R3, R4, R5>(f1: (a1: A1, a2: A2, a3: A3, a4: A4) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4, f5: (a: R4) => R5): (a1: A1, a2: A2, a3: A3, a4: A4) => R5;
        flow<A1, A2, A3, A4, R1, R2, R3, R4, R5, R6>(f1: (a1: A1, a2: A2, a3: A3, a4: A4) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4, f5: (a: R4) => R5, f6: (a: R5) => R6): (a1: A1, a2: A2, a3: A3, a4: A4) => R6;
        flow<A1, A2, A3, A4, R1, R2, R3, R4, R5, R6, R7>(f1: (a1: A1, a2: A2, a3: A3, a4: A4) => R1, f2: (a: R1) => R2, f3: (a: R2) => R3, f4: (a: R3) => R4, f5: (a: R4) => R5, f6: (a: R5) => R6, f7: (a: R6) => R7): (a1: A1, a2: A2, a3: A3, a4: A4) => R7;
        // generic function
        flow<TResult extends Function>(...funcs: Function[]): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_flow
         */
        flow<TResult extends Function>(...funcs: Function[]): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_flow
         */
        flow<TResult extends Function>(...funcs: Function[]): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_flowRight
    interface LoDashStatic {
        /**
         * This method is like lodash_flow except that it creates a function that invokes the provided functions from right
         * to left.
         *
         * @param funcs Functions to invoke.
         * @return Returns the new function.
         */
        flowRight<TResult extends Function>(...funcs: Function[]): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_flowRight
         */
        flowRight<TResult extends Function>(...funcs: Function[]): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_flowRight
         */
        flowRight<TResult extends Function>(...funcs: Function[]): LoDashExplicitObjectWrapper<TResult>;
    }


    //lodash_memoize
    interface MemoizedFunction extends Function {
        cache: MapCache;
    }

    interface LoDashStatic {
        /**
         * Creates a function that memoizes the result of func. If resolver is provided it determines the cache key for
         * storing the result based on the arguments provided to the memoized function. By default, the first argument
         * provided to the memoized function is coerced to a string and used as the cache key. The func is invoked with
         * the this binding of the memoized function.
         *
         * @param func The function to have its output memoized.
         * @param resolver The function to resolve the cache key.
         * @return Returns the new memoizing function.
         */
        memoize: {
            <T extends Function>(func: T, resolver?: Function): MemoizedFunction;
            Cache: MapCache;
        }
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_memoize
         */
        memoize(resolver?: Function): LoDashImplicitObjectWrapper<MemoizedFunction>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_memoize
         */
        memoize(resolver?: Function): LoDashExplicitObjectWrapper<MemoizedFunction>;
    }

    //lodash_overArgs (was lodash_modArgs)
    interface LoDashStatic {
        /**
         * Creates a function that runs each argument through a corresponding transform function.
         *
         * @param func The function to wrap.
         * @param transforms The functions to transform arguments, specified as individual functions or arrays
         * of functions.
         * @return Returns the new function.
         */
        overArgs<T extends Function, TResult extends Function>(
            func: T,
            ...transforms: Function[]
        ): TResult;

        /**
         * @see lodash_overArgs
         */
        overArgs<T extends Function, TResult extends Function>(
            func: T,
            transforms: Function[]
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_overArgs
         */
        overArgs<TResult extends Function>(...transforms: Function[]): LoDashImplicitObjectWrapper<TResult>;

        /**
         * @see lodash_overArgs
         */
        overArgs<TResult extends Function>(transforms: Function[]): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_overArgs
         */
        overArgs<TResult extends Function>(...transforms: Function[]): LoDashExplicitObjectWrapper<TResult>;

        /**
         * @see lodash_overArgs
         */
        overArgs<TResult extends Function>(transforms: Function[]): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_negate
    interface LoDashStatic {
        /**
         * Creates a function that negates the result of the predicate func. The func predicate is invoked with
         * the this binding and arguments of the created function.
         *
         * @param predicate The predicate to negate.
         * @return Returns the new function.
         */
        negate<T extends Function>(predicate: T): (...args: any[]) => boolean;

        /**
         * @see lodash_negate
         */
        negate<T extends Function, TResult extends Function>(predicate: T): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_negate
         */
        negate(): LoDashImplicitObjectWrapper<(...args: any[]) => boolean>;

        /**
         * @see lodash_negate
         */
        negate<TResult extends Function>(): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_negate
         */
        negate(): LoDashExplicitObjectWrapper<(...args: any[]) => boolean>;

        /**
         * @see lodash_negate
         */
        negate<TResult extends Function>(): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_once
    interface LoDashStatic {
        /**
         * Creates a function that is restricted to invoking func once. Repeat calls to the function return the value
         * of the first call. The func is invoked with the this binding and arguments of the created function.
         *
         * @param func The function to restrict.
         * @return Returns the new restricted function.
         */
        once<T extends Function>(func: T): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_once
         */
        once(): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_once
         */
        once(): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_partial
    interface LoDashStatic {
        /**
         * Creates a function that, when called, invokes func with any additional partial arguments
         * prepended to those provided to the new function. This method is similar to lodash_bind except
         * it does not alter the this binding.
         * @param func The function to partially apply arguments to.
         * @param args Arguments to be partially applied.
         * @return The new partially applied function.
         **/
        partial: Partial;
    }

    type PH = LoDashStatic;

    interface Function0<R> {
        (): R;
    }
    interface Function1<T1, R> {
        (t1: T1): R;
    }
    interface Function2<T1, T2, R> {
        (t1: T1, t2: T2): R;
    }
    interface Function3<T1, T2, T3, R> {
        (t1: T1, t2: T2, t3: T3): R;
    }
    interface Function4<T1, T2, T3, T4, R> {
        (t1: T1, t2: T2, t3: T3, t4: T4): R;
    }

    interface Partial {
        // arity 0
        <R>(func: Function0<R>): Function0<R>;
        // arity 1
        <T1, R>(func: Function1<T1, R>): Function1<T1, R>;
        <T1, R>(func: Function1<T1, R>, arg1: T1): Function0<R>;
        // arity 2
        <T1, T2, R>(func: Function2<T1, T2, R>):                      Function2<T1, T2, R>;
        <T1, T2, R>(func: Function2<T1, T2, R>, arg1: T1):            Function1<    T2, R>;
        <T1, T2, R>(func: Function2<T1, T2, R>, plc1: PH, arg2: T2):  Function1<T1,     R>;
        <T1, T2, R>(func: Function2<T1, T2, R>, arg1: T1, arg2: T2):  Function0<        R>;
        // arity 3
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>):                                Function3<T1, T2, T3, R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>, arg1: T1):                      Function2<    T2, T3, R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>, plc1: PH, arg2: T2):            Function2<T1,     T3, R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>, arg1: T1, arg2: T2):            Function1<        T3, R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>, plc1: PH, plc2: PH, arg3: T3):  Function2<T1, T2,     R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>, arg1: T1, plc2: PH, arg3: T3):  Function1<    T2,     R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>, plc1: PH, arg2: T2, arg3: T3):  Function1<T1,         R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>, arg1: T1, arg2: T2, arg3: T3):  Function0<            R>;
        // arity 4
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>):                                          Function4<T1, T2, T3, T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1):                                Function3<    T2, T3, T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, plc1: PH, arg2: T2):                      Function3<T1,     T3, T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, arg2: T2):                      Function2<        T3, T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, plc1: PH, plc2: PH, arg3: T3):            Function3<T1, T2,     T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, plc2: PH, arg3: T3):            Function2<    T2,     T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, plc1: PH, arg2: T2, arg3: T3):            Function2<T1,         T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, arg2: T2, arg3: T3):            Function1<            T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, plc1: PH, plc2: PH, plc3: PH, arg4: T4):  Function3<T1, T2, T3,     R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, plc2: PH, plc3: PH, arg4: T4):  Function2<    T2, T3,     R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, plc1: PH, arg2: T2, plc3: PH, arg4: T4):  Function2<T1,     T3,     R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, arg2: T2, plc3: PH, arg4: T4):  Function1<        T3,     R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, plc1: PH, plc2: PH, arg3: T3, arg4: T4):  Function2<T1, T2,         R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, plc2: PH, arg3: T3, arg4: T4):  Function1<    T2,         R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, plc1: PH, arg2: T2, arg3: T3, arg4: T4):  Function1<T1,             R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, arg2: T2, arg3: T3, arg4: T4):  Function0<                R>;
        // catch-all
        (func: Function, ...args: any[]): Function;
    }

    //lodash_partialRight
    interface LoDashStatic {
        /**
         * This method is like lodash_partial except that partial arguments are appended to those provided
         * to the new function.
         * @param func The function to partially apply arguments to.
         * @param args Arguments to be partially applied.
         * @return The new partially applied function.
         **/
        partialRight: PartialRight
    }

    interface PartialRight {
        // arity 0
        <R>(func: Function0<R>): Function0<R>;
        // arity 1
        <T1, R>(func: Function1<T1, R>): Function1<T1, R>;
        <T1, R>(func: Function1<T1, R>, arg1: T1): Function0<R>;
        // arity 2
        <T1, T2, R>(func: Function2<T1, T2, R>):                      Function2<T1, T2, R>;
        <T1, T2, R>(func: Function2<T1, T2, R>, arg1: T1, plc2: PH):  Function1<    T2, R>;
        <T1, T2, R>(func: Function2<T1, T2, R>,           arg2: T2):  Function1<T1,     R>;
        <T1, T2, R>(func: Function2<T1, T2, R>, arg1: T1, arg2: T2):  Function0<        R>;
        // arity 3
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>):                                Function3<T1, T2, T3, R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>, arg1: T1, plc2: PH, plc3: PH):  Function2<    T2, T3, R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>,           arg2: T2, plc3: PH):  Function2<T1,     T3, R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>, arg1: T1, arg2: T2, plc3: PH):  Function1<        T3, R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>,                     arg3: T3):  Function2<T1, T2,     R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>, arg1: T1, plc2: PH, arg3: T3):  Function1<    T2,     R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>,           arg2: T2, arg3: T3):  Function1<T1,         R>;
        <T1, T2, T3, R>(func: Function3<T1, T2, T3, R>, arg1: T1, arg2: T2, arg3: T3):  Function0<            R>;
        // arity 4
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>):                                          Function4<T1, T2, T3, T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, plc2: PH, plc3: PH, plc4: PH):  Function3<    T2, T3, T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>,           arg2: T2, plc3: PH, plc4: PH):  Function3<T1,     T3, T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, arg2: T2, plc3: PH, plc4: PH):  Function2<        T3, T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>,                     arg3: T3, plc4: PH):  Function3<T1, T2,     T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, plc2: PH, arg3: T3, plc4: PH):  Function2<    T2,     T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>,           arg2: T2, arg3: T3, plc4: PH):  Function2<T1,         T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, arg2: T2, arg3: T3, plc4: PH):  Function1<            T4, R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>,                               arg4: T4):  Function3<T1, T2, T3,     R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, plc2: PH, plc3: PH, arg4: T4):  Function2<    T2, T3,     R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>,           arg2: T2, plc3: PH, arg4: T4):  Function2<T1,     T3,     R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, arg2: T2, plc3: PH, arg4: T4):  Function1<        T3,     R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>,                     arg3: T3, arg4: T4):  Function2<T1, T2,         R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, plc2: PH, arg3: T3, arg4: T4):  Function1<    T2,         R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>,           arg2: T2, arg3: T3, arg4: T4):  Function1<T1,             R>;
        <T1, T2, T3, T4, R>(func: Function4<T1, T2, T3, T4, R>, arg1: T1, arg2: T2, arg3: T3, arg4: T4):  Function0<                R>;
        // catch-all
        (func: Function, ...args: any[]): Function;
    }

    //lodash_rearg
    interface LoDashStatic {
        /**
         * Creates a function that invokes func with arguments arranged according to the specified indexes where the
         * argument value at the first index is provided as the first argument, the argument value at the second index
         * is provided as the second argument, and so on.
         * @param func The function to rearrange arguments for.
         * @param indexes The arranged argument indexes, specified as individual indexes or arrays of indexes.
         * @return Returns the new function.
         */
        rearg<TResult extends Function>(func: Function, indexes: number[]): TResult;

        /**
         * @see lodash_rearg
         */
        rearg<TResult extends Function>(func: Function, ...indexes: number[]): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_rearg
         */
        rearg<TResult extends Function>(indexes: number[]): LoDashImplicitObjectWrapper<TResult>;

        /**
         * @see lodash_rearg
         */
        rearg<TResult extends Function>(...indexes: number[]): LoDashImplicitObjectWrapper<TResult>;
    }

    //lodash_rest
    interface LoDashStatic {
        /**
         * Creates a function that invokes func with the this binding of the created function and arguments from start
         * and beyond provided as an array.
         *
         * Note: This method is based on the rest parameter.
         *
         * @param func The function to apply a rest parameter to.
         * @param start The start position of the rest parameter.
         * @return Returns the new function.
         */
        rest<TResult extends Function>(
            func: Function,
            start?: number
        ): TResult;

        /**
         * @see lodash_rest
         */
        rest<TResult extends Function, TFunc extends Function>(
            func: TFunc,
            start?: number
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_rest
         */
        rest<TResult extends Function>(start?: number): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_rest
         */
        rest<TResult extends Function>(start?: number): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_spread
    interface LoDashStatic {
        /**
         * Creates a function that invokes func with the this binding of the created function and an array of arguments
         * much like Function#apply.
         *
         * Note: This method is based on the spread operator.
         *
         * @param func The function to spread arguments over.
         * @return Returns the new function.
         */
        spread<F extends Function, T extends Function>(func: F): T;

        /**
         * @see lodash_spread
         */
        spread<T extends Function>(func: Function): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_spread
         */
        spread<T extends Function>(): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_spread
         */
        spread<T extends Function>(): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_throttle
    interface ThrottleSettings {
        /**
         * If you'd like to disable the leading-edge call, pass this as false.
         */
        leading?: boolean;

        /**
         * If you'd like to disable the execution on the trailing-edge, pass false.
         */
        trailing?: boolean;
    }

    interface LoDashStatic {
        /**
         * Creates a throttled function that only invokes func at most once per every wait milliseconds. The throttled
         * function comes with a cancel method to cancel delayed invocations and a flush method to immediately invoke
         * them. Provide an options object to indicate that func should be invoked on the leading and/or trailing edge
         * of the wait timeout. Subsequent calls to the throttled function return the result of the last func call.
         *
         * Note: If leading and trailing options are true, func is invoked on the trailing edge of the timeout only if
         * the the throttled function is invoked more than once during the wait timeout.
         *
         * @param func The function to throttle.
         * @param wait The number of milliseconds to throttle invocations to.
         * @param options The options object.
         * @param options.leading Specify invoking on the leading edge of the timeout.
         * @param options.trailing Specify invoking on the trailing edge of the timeout.
         * @return Returns the new throttled function.
         */
        throttle<T extends Function>(
            func: T,
            wait?: number,
            options?: ThrottleSettings
        ): Cancelable;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_throttle
         */
        throttle(
            wait?: number,
            options?: ThrottleSettings
        ): LoDashImplicitObjectWrapper<Cancelable>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_throttle
         */
        throttle(
            wait?: number,
            options?: ThrottleSettings
        ): LoDashExplicitObjectWrapper<Cancelable>;
    }

    //lodash_unary
    interface LoDashStatic {
        /**
         * Creates a function that accepts up to one argument, ignoring any
         * additional arguments.
         *
         * @static
         * @memberOf _
         * @category Function
         * @param {Function} func The function to cap arguments for.
         * @returns {Function} Returns the new function.
         * @example
         *
         * lodash_map(['6', '8', '10'], lodash_unary(parseInt));
         * // => [6, 8, 10]
         */
        unary<T extends Function>(func: T): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_unary
         */
        unary(): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_unary
         */
        unary(): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_wrap
    interface LoDashStatic {
        /**
         * Creates a function that provides value to the wrapper function as its first argument. Any additional
         * arguments provided to the function are appended to those provided to the wrapper function. The wrapper is
         * invoked with the this binding of the created function.
         *
         * @param value The value to wrap.
         * @param wrapper The wrapper function.
         * @return Returns the new function.
         */
        wrap<V, W extends Function, R extends Function>(
            value: V,
            wrapper: W
        ): R;

        /**
         * @see lodash_wrap
         */
        wrap<V, R extends Function>(
            value: V,
            wrapper: Function
        ): R;

        /**
         * @see lodash_wrap
         */
        wrap<R extends Function>(
            value: any,
            wrapper: Function
        ): R;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_wrap
         */
        wrap<W extends Function, R extends Function>(wrapper: W): LoDashImplicitObjectWrapper<R>;

        /**
         * @see lodash_wrap
         */
        wrap<R extends Function>(wrapper: Function): LoDashImplicitObjectWrapper<R>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_wrap
         */
        wrap<W extends Function, R extends Function>(wrapper: W): LoDashImplicitObjectWrapper<R>;

        /**
         * @see lodash_wrap
         */
        wrap<R extends Function>(wrapper: Function): LoDashImplicitObjectWrapper<R>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_wrap
         */
        wrap<W extends Function, R extends Function>(wrapper: W): LoDashImplicitObjectWrapper<R>;

        /**
         * @see lodash_wrap
         */
        wrap<R extends Function>(wrapper: Function): LoDashImplicitObjectWrapper<R>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_wrap
         */
        wrap<W extends Function, R extends Function>(wrapper: W): LoDashExplicitObjectWrapper<R>;

        /**
         * @see lodash_wrap
         */
        wrap<R extends Function>(wrapper: Function): LoDashExplicitObjectWrapper<R>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_wrap
         */
        wrap<W extends Function, R extends Function>(wrapper: W): LoDashExplicitObjectWrapper<R>;

        /**
         * @see lodash_wrap
         */
        wrap<R extends Function>(wrapper: Function): LoDashExplicitObjectWrapper<R>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_wrap
         */
        wrap<W extends Function, R extends Function>(wrapper: W): LoDashExplicitObjectWrapper<R>;

        /**
         * @see lodash_wrap
         */
        wrap<R extends Function>(wrapper: Function): LoDashExplicitObjectWrapper<R>;
    }

    /********
     * Lang *
     ********/

        //lodash_castArray
    interface LoDashStatic {
        /**
         * Casts value as an array if it’s not one.
         *
         * @param value The value to inspect.
         * @return Returns the cast array.
         */
        castArray<T>(value: T): T[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_castArray
         */
        castArray(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_castArray
         */
        castArray(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_castArray
         */
        castArray(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_castArray
         */
        castArray(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_castArray
         */
        castArray(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_castArray
         */
        castArray(): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_clone
    interface LoDashStatic {
        /**
         * Creates a shallow clone of value.
         *
         * Note: This method is loosely based on the structured clone algorithm and supports cloning arrays,
         * array buffers, booleans, date objects, maps, numbers, Object objects, regexes, sets, strings, symbols,
         * and typed arrays. The own enumerable properties of arguments objects are cloned as plain objects. An empty
         * object is returned for uncloneable values such as error objects, functions, DOM nodes, and WeakMaps.
         *
         * @param value The value to clone.
         * @return Returns the cloned value.
         */
        clone<T>(value: T): T;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_clone
         */
        clone(): T;
    }

    interface LoDashImplicitArrayWrapper<T> {

        /**
         * @see lodash_clone
         */
        clone(): T[];
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_clone
         */
        clone(): T;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_clone
         */
        clone(): LoDashExplicitWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {

        /**
         * @see lodash_clone
         */
        clone(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_clone
         */
        clone(): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_cloneDeep
    interface LoDashStatic {
        /**
         * This method is like lodash_clone except that it recursively clones value.
         *
         * @param value The value to recursively clone.
         * @return Returns the deep cloned value.
         */
        cloneDeep<T>(value: T): T;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_cloneDeep
         */
        cloneDeep(): T;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_cloneDeep
         */
        cloneDeep(): T[];
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_cloneDeep
         */
        cloneDeep(): T;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_cloneDeep
         */
        cloneDeep(): LoDashExplicitWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_cloneDeep
         */
        cloneDeep(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_cloneDeep
         */
        cloneDeep(): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_cloneDeepWith
    interface CloneDeepWithCustomizer<TValue, TResult> {
        (value: TValue): TResult;
    }

    interface LoDashStatic {
        /**
         * This method is like lodash_cloneWith except that it recursively clones value.
         *
         * @param value The value to recursively clone.
         * @param customizer The function to customize cloning.
         * @return Returns the deep cloned value.
         */
        cloneDeepWith<TResult>(
            value: any,
            customizer?: CloneDeepWithCustomizer<any, TResult>
        ): TResult;

        /**
         * @see lodash_clonDeepeWith
         */
        cloneDeepWith<T, TResult>(
            value: T,
            customizer?: CloneDeepWithCustomizer<T, TResult>
        ): TResult;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_cloneDeepWith
         */
        cloneDeepWith<TResult>(
            customizer?: CloneDeepWithCustomizer<T, TResult>
        ): TResult;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_cloneDeepWith
         */
        cloneDeepWith<TResult>(
            customizer?: CloneDeepWithCustomizer<T[], TResult>
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_cloneDeepWith
         */
        cloneDeepWith<TResult>(
            customizer?: CloneDeepWithCustomizer<T, TResult>
        ): TResult;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_cloneDeepWith
         */
        cloneDeepWith<TResult extends (number|string|boolean)>(
            customizer?: CloneDeepWithCustomizer<T, TResult>
        ): LoDashExplicitWrapper<TResult>;

        /**
         * @see lodash_cloneDeepWith
         */
        cloneDeepWith<TResult>(
            customizer?: CloneDeepWithCustomizer<T, TResult[]>
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_cloneDeepWith
         */
        cloneDeepWith<TResult extends Object>(
            customizer?: CloneDeepWithCustomizer<T, TResult>
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_cloneDeepWith
         */
        cloneDeepWith<TResult extends (number|string|boolean)>(
            customizer?: CloneDeepWithCustomizer<T[], TResult>
        ): LoDashExplicitWrapper<TResult>;

        /**
         * @see lodash_cloneDeepWith
         */
        cloneDeepWith<TResult>(
            customizer?: CloneDeepWithCustomizer<T[], TResult[]>
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_cloneDeepWith
         */
        cloneDeepWith<TResult extends Object>(
            customizer?: CloneDeepWithCustomizer<T[], TResult>
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_cloneDeepWith
         */
        cloneDeepWith<TResult extends (number|string|boolean)>(
            customizer?: CloneDeepWithCustomizer<T, TResult>
        ): LoDashExplicitWrapper<TResult>;

        /**
         * @see lodash_cloneDeepWith
         */
        cloneDeepWith<TResult>(
            customizer?: CloneDeepWithCustomizer<T, TResult[]>
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_cloneDeepWith
         */
        cloneDeepWith<TResult extends Object>(
            customizer?: CloneDeepWithCustomizer<T, TResult>
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_cloneWith
    interface CloneWithCustomizer<TValue, TResult> {
        (value: TValue): TResult;
    }

    interface LoDashStatic {
        /**
         * This method is like lodash_clone except that it accepts customizer which is invoked to produce the cloned value.
         * If customizer returns undefined cloning is handled by the method instead.
         *
         * @param value The value to clone.
         * @param customizer The function to customize cloning.
         * @return Returns the cloned value.
         */
        cloneWith<TResult>(
            value: any,
            customizer?: CloneWithCustomizer<any, TResult>
        ): TResult;

        /**
         * @see lodash_cloneWith
         */
        cloneWith<T, TResult>(
            value: T,
            customizer?: CloneWithCustomizer<T, TResult>
        ): TResult;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_cloneWith
         */
        cloneWith<TResult>(
            customizer?: CloneWithCustomizer<T, TResult>
        ): TResult;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_cloneWith
         */
        cloneWith<TResult>(
            customizer?: CloneWithCustomizer<T[], TResult>
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_cloneWith
         */
        cloneWith<TResult>(
            customizer?: CloneWithCustomizer<T, TResult>
        ): TResult;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_cloneWith
         */
        cloneWith<TResult extends (number|string|boolean)>(
            customizer?: CloneWithCustomizer<T, TResult>
        ): LoDashExplicitWrapper<TResult>;

        /**
         * @see lodash_cloneWith
         */
        cloneWith<TResult>(
            customizer?: CloneWithCustomizer<T, TResult[]>
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_cloneWith
         */
        cloneWith<TResult extends Object>(
            customizer?: CloneWithCustomizer<T, TResult>
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_cloneWith
         */
        cloneWith<TResult extends (number|string|boolean)>(
            customizer?: CloneWithCustomizer<T[], TResult>
        ): LoDashExplicitWrapper<TResult>;

        /**
         * @see lodash_cloneWith
         */
        cloneWith<TResult>(
            customizer?: CloneWithCustomizer<T[], TResult[]>
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_cloneWith
         */
        cloneWith<TResult extends Object>(
            customizer?: CloneWithCustomizer<T[], TResult>
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_cloneWith
         */
        cloneWith<TResult extends (number|string|boolean)>(
            customizer?: CloneWithCustomizer<T, TResult>
        ): LoDashExplicitWrapper<TResult>;

        /**
         * @see lodash_cloneWith
         */
        cloneWith<TResult>(
            customizer?: CloneWithCustomizer<T, TResult[]>
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_cloneWith
         */
        cloneWith<TResult extends Object>(
            customizer?: CloneWithCustomizer<T, TResult>
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_eq
    interface LoDashStatic {
        /**
         * Performs a [`SameValueZero`](http://ecma-international.org/ecma-262/6.0/#sec-samevaluezero)
         * comparison between two values to determine if they are equivalent.
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to compare.
         * @param {*} other The other value to compare.
         * @returns {boolean} Returns `true` if the values are equivalent, else `false`.
         * @example
         *
         * var object = { 'user': 'fred' };
         * var other = { 'user': 'fred' };
         *
         * lodash_eq(object, object);
         * // => true
         *
         * lodash_eq(object, other);
         * // => false
         *
         * lodash_eq('a', 'a');
         * // => true
         *
         * lodash_eq('a', Object('a'));
         * // => false
         *
         * lodash_eq(NaN, NaN);
         * // => true
         */
        eq(
            value: any,
            other: any
        ): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isEqual
         */
        eq(
            other: any
        ): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isEqual
         */
        eq(
            other: any
        ): LoDashExplicitWrapper<boolean>;
    }

    //lodash_gt
    interface LoDashStatic {
        /**
         * Checks if value is greater than other.
         *
         * @param value The value to compare.
         * @param other The other value to compare.
         * @return Returns true if value is greater than other, else false.
         */
        gt(
            value: any,
            other: any
        ): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_gt
         */
        gt(other: any): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_gt
         */
        gt(other: any): LoDashExplicitWrapper<boolean>;
    }

    //lodash_gte
    interface LoDashStatic {
        /**
         * Checks if value is greater than or equal to other.
         *
         * @param value The value to compare.
         * @param other The other value to compare.
         * @return Returns true if value is greater than or equal to other, else false.
         */
        gte(
            value: any,
            other: any
        ): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_gte
         */
        gte(other: any): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_gte
         */
        gte(other: any): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isArguments
    interface LoDashStatic {
        /**
         * Checks if value is classified as an arguments object.
         *
         * @param value The value to check.
         * @return Returns true if value is correctly classified, else false.
         */
        isArguments(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isArguments
         */
        isArguments(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isArguments
         */
        isArguments(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isArray
    interface LoDashStatic {
        /**
         * Checks if value is classified as an Array object.
         * @param value The value to check.
         *
         * @return Returns true if value is correctly classified, else false.
         */
        isArray<T>(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T,TWrapper> {
        /**
         * @see lodash_isArray
         */
        isArray(): boolean;
    }

    interface LoDashExplicitWrapperBase<T,TWrapper> {
        /**
         * @see lodash_isArray
         */
        isArray(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isArrayBuffer
    interface LoDashStatic {
        /**
         * Checks if value is classified as an ArrayBuffer object.
         *
         * @param value The value to check.
         * @return Returns true if value is correctly classified, else false.
         */
        isArrayBuffer(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isArrayBuffer
         */
        isArrayBuffer(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isArrayBuffer
         */
        isArrayBuffer(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isArrayLike
    interface LoDashStatic {
        /**
         * Checks if `value` is array-like. A value is considered array-like if it's
         * not a function and has a `value.length` that's an integer greater than or
         * equal to `0` and less than or equal to `Number.MAX_SAFE_INTEGER`.
         *
         * @static
         * @memberOf _
         * @type Function
         * @category Lang
         * @param {*} value The value to check.
         * @returns {boolean} Returns `true` if `value` is array-like, else `false`.
         * @example
         *
         * lodash_isArrayLike([1, 2, 3]);
         * // => true
         *
         * lodash_isArrayLike(document.body.children);
         * // => true
         *
         * lodash_isArrayLike('abc');
         * // => true
         *
         * lodash_isArrayLike(lodash_noop);
         * // => false
         */
        isArrayLike<T>(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T,TWrapper> {
        /**
         * @see lodash_isArrayLike
         */
        isArrayLike(): boolean;
    }

    interface LoDashExplicitWrapperBase<T,TWrapper> {
        /**
         * @see lodash_isArrayLike
         */
        isArrayLike(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isArrayLikeObject
    interface LoDashStatic {
        /**
         * This method is like `lodash_isArrayLike` except that it also checks if `value`
         * is an object.
         *
         * @static
         * @memberOf _
         * @type Function
         * @category Lang
         * @param {*} value The value to check.
         * @returns {boolean} Returns `true` if `value` is an array-like object, else `false`.
         * @example
         *
         * lodash_isArrayLikeObject([1, 2, 3]);
         * // => true
         *
         * lodash_isArrayLikeObject(document.body.children);
         * // => true
         *
         * lodash_isArrayLikeObject('abc');
         * // => false
         *
         * lodash_isArrayLikeObject(lodash_noop);
         * // => false
         */
        isArrayLikeObject<T>(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T,TWrapper> {
        /**
         * @see lodash_isArrayLikeObject
         */
        isArrayLikeObject(): boolean;
    }

    interface LoDashExplicitWrapperBase<T,TWrapper> {
        /**
         * @see lodash_isArrayLikeObject
         */
        isArrayLikeObject(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isBoolean
    interface LoDashStatic {
        /**
         * Checks if value is classified as a boolean primitive or object.
         *
         * @param value The value to check.
         * @return Returns true if value is correctly classified, else false.
         */
        isBoolean(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isBoolean
         */
        isBoolean(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isBoolean
         */
        isBoolean(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isBuffer
    interface LoDashStatic {
        /**
         * Checks if value is a buffer.
         *
         * @param value The value to check.
         * @return Returns true if value is a buffer, else false.
         */
        isBuffer(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isBuffer
         */
        isBuffer(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isBuffer
         */
        isBuffer(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isDate
    interface LoDashStatic {
        /**
         * Checks if value is classified as a Date object.
         * @param value The value to check.
         *
         * @return Returns true if value is correctly classified, else false.
         */
        isDate(value?: any): Date;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isDate
         */
        isDate(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isDate
         */
        isDate(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isElement
    interface LoDashStatic {
        /**
         * Checks if value is a DOM element.
         *
         * @param value The value to check.
         * @return Returns true if value is a DOM element, else false.
         */
        isElement(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isElement
         */
        isElement(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isElement
         */
        isElement(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isEmpty
    interface LoDashStatic {
        /**
         * Checks if value is empty. A value is considered empty unless it’s an arguments object, array, string, or
         * jQuery-like collection with a length greater than 0 or an object with own enumerable properties.
         *
         * @param value The value to inspect.
         * @return Returns true if value is empty, else false.
         */
        isEmpty(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isEmpty
         */
        isEmpty(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isEmpty
         */
        isEmpty(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isEqual
    interface LoDashStatic {
        /**
         * Performs a deep comparison between two values to determine if they are
         * equivalent.
         *
         * **Note:** This method supports comparing arrays, array buffers, booleans,
         * date objects, error objects, maps, numbers, `Object` objects, regexes,
         * sets, strings, symbols, and typed arrays. `Object` objects are compared
         * by their own, not inherited, enumerable properties. Functions and DOM
         * nodes are **not** supported.
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to compare.
         * @param {*} other The other value to compare.
         * @returns {boolean} Returns `true` if the values are equivalent, else `false`.
         * @example
         *
         * var object = { 'user': 'fred' };
         * var other = { 'user': 'fred' };
         *
         * lodash_isEqual(object, other);
         * // => true
         *
         * object === other;
         * // => false
         */
        isEqual(
            value: any,
            other: any
        ): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isEqual
         */
        isEqual(
            other: any
        ): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isEqual
         */
        isEqual(
            other: any
        ): LoDashExplicitWrapper<boolean>;
    }

    // lodash_isEqualWith
    interface IsEqualCustomizer {
        (value: any, other: any, indexOrKey?: number|string): boolean;
    }

    interface LoDashStatic {
        /**
         * This method is like `lodash_isEqual` except that it accepts `customizer` which is
         * invoked to compare values. If `customizer` returns `undefined` comparisons are
         * handled by the method instead. The `customizer` is invoked with up to seven arguments:
         * (objValue, othValue [, index|key, object, other, stack]).
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to compare.
         * @param {*} other The other value to compare.
         * @param {Function} [customizer] The function to customize comparisons.
         * @returns {boolean} Returns `true` if the values are equivalent, else `false`.
         * @example
         *
         * function isGreeting(value) {
         *   return /^h(?:i|ello)$/.test(value);
         * }
         *
         * function customizer(objValue, othValue) {
         *   if (isGreeting(objValue) && isGreeting(othValue)) {
         *     return true;
         *   }
         * }
         *
         * var array = ['hello', 'goodbye'];
         * var other = ['hi', 'goodbye'];
         *
         * lodash_isEqualWith(array, other, customizer);
         * // => true
         */
        isEqualWith(
            value: any,
            other: any,
            customizer: IsEqualCustomizer
        ): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isEqualWith
         */
        isEqualWith(
            other: any,
            customizer: IsEqualCustomizer
        ): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isEqualWith
         */
        isEqualWith(
            other: any,
            customizer: IsEqualCustomizer
        ): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isError
    interface LoDashStatic {
        /**
         * Checks if value is an Error, EvalError, RangeError, ReferenceError, SyntaxError, TypeError, or URIError
         * object.
         *
         * @param value The value to check.
         * @return Returns true if value is an error object, else false.
         */
        isError(value: any): value is Error;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isError
         */
        isError(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isError
         */
        isError(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isFinite
    interface LoDashStatic {
        /**
         * Checks if value is a finite primitive number.
         *
         * Note: This method is based on Number.isFinite.
         *
         * @param value The value to check.
         * @return Returns true if value is a finite number, else false.
         */
        isFinite(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isFinite
         */
        isFinite(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isFinite
         */
        isFinite(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isFunction
    interface LoDashStatic {
        /**
         * Checks if value is classified as a Function object.
         *
         * @param value The value to check.
         * @return Returns true if value is correctly classified, else false.
         */
        isFunction(value?: any): value is Function;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isFunction
         */
        isFunction(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isFunction
         */
        isFunction(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isInteger
    interface LoDashStatic {
        /**
         * Checks if `value` is an integer.
         *
         * **Note:** This method is based on [`Number.isInteger`](https://mdn.io/Number/isInteger).
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to check.
         * @returns {boolean} Returns `true` if `value` is an integer, else `false`.
         * @example
         *
         * lodash_isInteger(3);
         * // => true
         *
         * lodash_isInteger(Number.MIN_VALUE);
         * // => false
         *
         * lodash_isInteger(Infinity);
         * // => false
         *
         * lodash_isInteger('3');
         * // => false
         */
        isInteger(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isInteger
         */
        isInteger(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isInteger
         */
        isInteger(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isLength
    interface LoDashStatic {
        /**
         * Checks if `value` is a valid array-like length.
         *
         * **Note:** This function is loosely based on [`ToLength`](http://ecma-international.org/ecma-262/6.0/#sec-tolength).
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to check.
         * @returns {boolean} Returns `true` if `value` is a valid length, else `false`.
         * @example
         *
         * lodash_isLength(3);
         * // => true
         *
         * lodash_isLength(Number.MIN_VALUE);
         * // => false
         *
         * lodash_isLength(Infinity);
         * // => false
         *
         * lodash_isLength('3');
         * // => false
         */
        isLength(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isLength
         */
        isLength(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isLength
         */
        isLength(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isMap
    interface LoDashStatic {
        /**
         * Checks if value is classified as a Map object.
         *
         * @param value The value to check.
         * @returns Returns true if value is correctly classified, else false.
         */
        isMap<K, V>(value?: any): value is Map<K, V>;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isMap
         */
        isMap(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isMap
         */
        isMap(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isMatch
    interface isMatchCustomizer {
        (value: any, other: any, indexOrKey?: number|string): boolean;
    }

    interface LoDashStatic {
        /**
         * Performs a deep comparison between `object` and `source` to determine if
         * `object` contains equivalent property values.
         *
         * **Note:** This method supports comparing the same values as `lodash_isEqual`.
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {Object} object The object to inspect.
         * @param {Object} source The object of property values to match.
         * @returns {boolean} Returns `true` if `object` is a match, else `false`.
         * @example
         *
         * var object = { 'user': 'fred', 'age': 40 };
         *
         * lodash_isMatch(object, { 'age': 40 });
         * // => true
         *
         * lodash_isMatch(object, { 'age': 36 });
         * // => false
         */
        isMatch(object: Object, source: Object): boolean;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_isMatch
         */
        isMatch(source: Object): boolean;
    }

    //lodash_isMatchWith
    interface isMatchWithCustomizer {
        (value: any, other: any, indexOrKey?: number|string): boolean;
    }

    interface LoDashStatic {
        /**
         * This method is like `lodash_isMatch` except that it accepts `customizer` which
         * is invoked to compare values. If `customizer` returns `undefined` comparisons
         * are handled by the method instead. The `customizer` is invoked with three
         * arguments: (objValue, srcValue, index|key, object, source).
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {Object} object The object to inspect.
         * @param {Object} source The object of property values to match.
         * @param {Function} [customizer] The function to customize comparisons.
         * @returns {boolean} Returns `true` if `object` is a match, else `false`.
         * @example
         *
         * function isGreeting(value) {
         *   return /^h(?:i|ello)$/.test(value);
         * }
         *
         * function customizer(objValue, srcValue) {
         *   if (isGreeting(objValue) && isGreeting(srcValue)) {
         *     return true;
         *   }
         * }
         *
         * var object = { 'greeting': 'hello' };
         * var source = { 'greeting': 'hi' };
         *
         * lodash_isMatchWith(object, source, customizer);
         * // => true
         */
        isMatchWith(object: Object, source: Object, customizer: isMatchWithCustomizer): boolean;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_isMatchWith
         */
        isMatchWith(source: Object, customizer: isMatchWithCustomizer): boolean;
    }

    //lodash_isNaN
    interface LoDashStatic {
        /**
         * Checks if value is NaN.
         *
         * Note: This method is not the same as isNaN which returns true for undefined and other non-numeric values.
         *
         * @param value The value to check.
         * @return Returns true if value is NaN, else false.
         */
        isNaN(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isNaN
         */
        isNaN(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isNaN
         */
        isNaN(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isNative
    interface LoDashStatic {
        /**
         * Checks if value is a native function.
         * @param value The value to check.
         *
         * @retrun Returns true if value is a native function, else false.
         */
        isNative(value: any): value is Function;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isNative
         */
        isNative(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isNative
         */
        isNative(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isNil
    interface LoDashStatic {
        /**
         * Checks if `value` is `null` or `undefined`.
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to check.
         * @returns {boolean} Returns `true` if `value` is nullish, else `false`.
         * @example
         *
         * lodash_isNil(null);
         * // => true
         *
         * lodash_isNil(void 0);
         * // => true
         *
         * lodash_isNil(NaN);
         * // => false
         */
        isNil(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isNil
         */
        isNil(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isNil
         */
        isNil(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isNull
    interface LoDashStatic {
        /**
         * Checks if value is null.
         *
         * @param value The value to check.
         * @return Returns true if value is null, else false.
         */
        isNull(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isNull
         */
        isNull(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isNull
         */
        isNull(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isNumber
    interface LoDashStatic {
        /**
         * Checks if value is classified as a Number primitive or object.
         *
         * Note: To exclude Infinity, -Infinity, and NaN, which are classified as numbers, use the lodash_isFinite method.
         *
         * @param value The value to check.
         * @return Returns true if value is correctly classified, else false.
         */
        isNumber(value?: any): value is number;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isNumber
         */
        isNumber(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isNumber
         */
        isNumber(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isObject
    interface LoDashStatic {
        /**
         * Checks if value is the language type of Object. (e.g. arrays, functions, objects, regexes, new Number(0),
         * and new String(''))
         *
         * @param value The value to check.
         * @return Returns true if value is an object, else false.
         */
        isObject(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isObject
         */
        isObject(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isObject
         */
        isObject(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isObjectLike
    interface LoDashStatic {
        /**
         * Checks if `value` is object-like. A value is object-like if it's not `null`
         * and has a `typeof` result of "object".
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to check.
         * @returns {boolean} Returns `true` if `value` is object-like, else `false`.
         * @example
         *
         * lodash_isObjectLike({});
         * // => true
         *
         * lodash_isObjectLike([1, 2, 3]);
         * // => true
         *
         * lodash_isObjectLike(lodash_noop);
         * // => false
         *
         * lodash_isObjectLike(null);
         * // => false
         */
        isObjectLike(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isObjectLike
         */
        isObjectLike(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isObjectLike
         */
        isObjectLike(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isPlainObject
    interface LoDashStatic {
        /**
         * Checks if value is a plain object, that is, an object created by the Object constructor or one with a
         * [[Prototype]] of null.
         *
         * Note: This method assumes objects created by the Object constructor have no inherited enumerable properties.
         *
         * @param value The value to check.
         * @return Returns true if value is a plain object, else false.
         */
        isPlainObject(value?: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isPlainObject
         */
        isPlainObject(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isPlainObject
         */
        isPlainObject(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isRegExp
    interface LoDashStatic {
        /**
         * Checks if value is classified as a RegExp object.
         * @param value The value to check.
         *
         * @return Returns true if value is correctly classified, else false.
         */
        isRegExp(value?: any): value is RegExp;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isRegExp
         */
        isRegExp(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isRegExp
         */
        isRegExp(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isSafeInteger
    interface LoDashStatic {
        /**
         * Checks if `value` is a safe integer. An integer is safe if it's an IEEE-754
         * double precision number which isn't the result of a rounded unsafe integer.
         *
         * **Note:** This method is based on [`Number.isSafeInteger`](https://mdn.io/Number/isSafeInteger).
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to check.
         * @returns {boolean} Returns `true` if `value` is a safe integer, else `false`.
         * @example
         *
         * lodash_isSafeInteger(3);
         * // => true
         *
         * lodash_isSafeInteger(Number.MIN_VALUE);
         * // => false
         *
         * lodash_isSafeInteger(Infinity);
         * // => false
         *
         * lodash_isSafeInteger('3');
         * // => false
         */
        isSafeInteger(value: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isSafeInteger
         */
        isSafeInteger(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isSafeInteger
         */
        isSafeInteger(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isSet
    interface LoDashStatic {
        /**
         * Checks if value is classified as a Set object.
         *
         * @param value The value to check.
         * @returns Returns true if value is correctly classified, else false.
         */
        isSet<T>(value?: any): value is Set<T>;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isSet
         */
        isSet(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isSet
         */
        isSet(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isString
    interface LoDashStatic {
        /**
         * Checks if value is classified as a String primitive or object.
         *
         * @param value The value to check.
         * @return Returns true if value is correctly classified, else false.
         */
        isString(value?: any): value is string;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isString
         */
        isString(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isString
         */
        isString(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isSymbol
    interface LoDashStatic {
        /**
         * Checks if `value` is classified as a `Symbol` primitive or object.
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to check.
         * @returns {boolean} Returns `true` if `value` is correctly classified, else `false`.
         * @example
         *
         * lodash_isSymbol(Symbol.iterator);
         * // => true
         *
         * lodash_isSymbol('abc');
         * // => false
         */
        isSymbol(value: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isSymbol
         */
        isSymbol(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isSymbol
         */
        isSymbol(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isTypedArray
    interface LoDashStatic {
        /**
         * Checks if value is classified as a typed array.
         *
         * @param value The value to check.
         * @return Returns true if value is correctly classified, else false.
         */
        isTypedArray(value: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isTypedArray
         */
        isTypedArray(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isTypedArray
         */
        isTypedArray(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isUndefined
    interface LoDashStatic {
        /**
         * Checks if value is undefined.
         *
         * @param value The value to check.
         * @return Returns true if value is undefined, else false.
         */
        isUndefined(value: any): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isUndefined
         */
        isUndefined(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * see lodash_isUndefined
         */
        isUndefined(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isWeakMap
    interface LoDashStatic {
        /**
         * Checks if value is classified as a WeakMap object.
         *
         * @param value The value to check.
         * @returns Returns true if value is correctly classified, else false.
         */
        isWeakMap<K, V>(value?: any): value is WeakMap<K, V>;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isSet
         */
        isWeakMap(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isSet
         */
        isWeakMap(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_isWeakSet
    interface LoDashStatic {
        /**
         * Checks if value is classified as a WeakSet object.
         *
         * @param value The value to check.
         * @returns Returns true if value is correctly classified, else false.
         */
        isWeakSet<T>(value?: any): value is WeakSet<T>;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isWeakSet
         */
        isWeakSet(): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_isWeakSet
         */
        isWeakSet(): LoDashExplicitWrapper<boolean>;
    }

    //lodash_lt
    interface LoDashStatic {
        /**
         * Checks if value is less than other.
         *
         * @param value The value to compare.
         * @param other The other value to compare.
         * @return Returns true if value is less than other, else false.
         */
        lt(
            value: any,
            other: any
        ): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_lt
         */
        lt(other: any): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_lt
         */
        lt(other: any): LoDashExplicitWrapper<boolean>;
    }

    //lodash_lte
    interface LoDashStatic {
        /**
         * Checks if value is less than or equal to other.
         *
         * @param value The value to compare.
         * @param other The other value to compare.
         * @return Returns true if value is less than or equal to other, else false.
         */
        lte(
            value: any,
            other: any
        ): boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_lte
         */
        lte(other: any): boolean;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_lte
         */
        lte(other: any): LoDashExplicitWrapper<boolean>;
    }

    //lodash_toArray
    interface LoDashStatic {
        /**
         * Converts value to an array.
         *
         * @param value The value to convert.
         * @return Returns the converted array.
         */
        toArray<T>(value: List<T>|Dictionary<T>|NumericDictionary<T>): T[];

        /**
         * @see lodash_toArray
         */
        toArray<TValue, TResult>(value: TValue): TResult[];

        /**
         * @see lodash_toArray
         */
        toArray<TResult>(value?: any): TResult[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_toArray
         */
        toArray<TResult>(): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_toArray
         */
        toArray(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_toArray
         */
        toArray<TResult>(): LoDashImplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_toArray
         */
        toArray<TResult>(): LoDashExplicitArrayWrapper<TResult>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_toArray
         */
        toArray(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_toArray
         */
        toArray<TResult>(): LoDashExplicitArrayWrapper<TResult>;
    }

    //lodash_toPlainObject
    interface LoDashStatic {
        /**
         * Converts value to a plain object flattening inherited enumerable properties of value to own properties
         * of the plain object.
         *
         * @param value The value to convert.
         * @return Returns the converted plain object.
         */
        toPlainObject<TResult extends {}>(value?: any): TResult;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_toPlainObject
         */
        toPlainObject<TResult extends {}>(): LoDashImplicitObjectWrapper<TResult>;
    }

    //lodash_toInteger
    interface LoDashStatic {
        /**
         * Converts `value` to an integer.
         *
         * **Note:** This function is loosely based on [`ToInteger`](http://www.ecma-international.org/ecma-262/6.0/#sec-tointeger).
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to convert.
         * @returns {number} Returns the converted integer.
         * @example
         *
         * lodash_toInteger(3);
         * // => 3
         *
         * lodash_toInteger(Number.MIN_VALUE);
         * // => 0
         *
         * lodash_toInteger(Infinity);
         * // => 1.7976931348623157e+308
         *
         * lodash_toInteger('3');
         * // => 3
         */
        toInteger(value: any): number;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_toInteger
         */
        toInteger(): LoDashImplicitWrapper<number>;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_toInteger
         */
        toInteger(): LoDashExplicitWrapper<number>;
    }

    //lodash_toLength
    interface LoDashStatic {
        /**
         * Converts `value` to an integer suitable for use as the length of an
         * array-like object.
         *
         * **Note:** This method is based on [`ToLength`](http://ecma-international.org/ecma-262/6.0/#sec-tolength).
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to convert.
         * @return {number} Returns the converted integer.
         * @example
         *
         * lodash_toLength(3);
         * // => 3
         *
         * lodash_toLength(Number.MIN_VALUE);
         * // => 0
         *
         * lodash_toLength(Infinity);
         * // => 4294967295
         *
         * lodash_toLength('3');
         * // => 3
         */
        toLength(value: any): number;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_toLength
         */
        toLength(): LoDashImplicitWrapper<number>;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_toLength
         */
        toLength(): LoDashExplicitWrapper<number>;
    }

    //lodash_toNumber
    interface LoDashStatic {
        /**
         * Converts `value` to a number.
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to process.
         * @returns {number} Returns the number.
         * @example
         *
         * lodash_toNumber(3);
         * // => 3
         *
         * lodash_toNumber(Number.MIN_VALUE);
         * // => 5e-324
         *
         * lodash_toNumber(Infinity);
         * // => Infinity
         *
         * lodash_toNumber('3');
         * // => 3
         */
        toNumber(value: any): number;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_toNumber
         */
        toNumber(): LoDashImplicitWrapper<number>;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_toNumber
         */
        toNumber(): LoDashExplicitWrapper<number>;
    }

    //lodash_toSafeInteger
    interface LoDashStatic {
        /**
         * Converts `value` to a safe integer. A safe integer can be compared and
         * represented correctly.
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to convert.
         * @returns {number} Returns the converted integer.
         * @example
         *
         * lodash_toSafeInteger(3);
         * // => 3
         *
         * lodash_toSafeInteger(Number.MIN_VALUE);
         * // => 0
         *
         * lodash_toSafeInteger(Infinity);
         * // => 9007199254740991
         *
         * lodash_toSafeInteger('3');
         * // => 3
         */
        toSafeInteger(value: any): number;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_toSafeInteger
         */
        toSafeInteger(): LoDashImplicitWrapper<number>;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_toSafeInteger
         */
        toSafeInteger(): LoDashExplicitWrapper<number>;
    }

    //lodash_toString DUMMY
    interface LoDashStatic {
        /**
         * Converts `value` to a string if it's not one. An empty string is returned
         * for `null` and `undefined` values. The sign of `-0` is preserved.
         *
         * @static
         * @memberOf _
         * @category Lang
         * @param {*} value The value to process.
         * @returns {string} Returns the string.
         * @example
         *
         * lodash_toString(null);
         * // => ''
         *
         * lodash_toString(-0);
         * // => '-0'
         *
         * lodash_toString([1, 2, 3]);
         * // => '1,2,3'
         */
        toString(value: any): string;
    }

    /********
     * Math *
     ********/

        //lodash_add
    interface LoDashStatic {
        /**
         * Adds two numbers.
         *
         * @param augend The first number to add.
         * @param addend The second number to add.
         * @return Returns the sum.
         */
        add(
            augend: number,
            addend: number
        ): number;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_add
         */
        add(addend: number): number;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_add
         */
        add(addend: number): LoDashExplicitWrapper<number>;
    }

    //lodash_ceil
    interface LoDashStatic {
        /**
         * Calculates n rounded up to precision.
         *
         * @param n The number to round up.
         * @param precision The precision to round up to.
         * @return Returns the rounded up number.
         */
        ceil(
            n: number,
            precision?: number
        ): number;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_ceil
         */
        ceil(precision?: number): number;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_ceil
         */
        ceil(precision?: number): LoDashExplicitWrapper<number>;
    }

    //lodash_floor
    interface LoDashStatic {
        /**
         * Calculates n rounded down to precision.
         *
         * @param n The number to round down.
         * @param precision The precision to round down to.
         * @return Returns the rounded down number.
         */
        floor(
            n: number,
            precision?: number
        ): number;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_floor
         */
        floor(precision?: number): number;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_floor
         */
        floor(precision?: number): LoDashExplicitWrapper<number>;
    }

    //lodash_max
    interface LoDashStatic {
        /**
         * Computes the maximum value of `array`. If `array` is empty or falsey
         * `undefined` is returned.
         *
         * @static
         * @memberOf _
         * @category Math
         * @param {Array} array The array to iterate over.
         * @returns {*} Returns the maximum value.
         */
        max<T>(
            collection: List<T>
        ): T;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_max
         */
        max(): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_max
         */
        max<T>(): T;
    }

    //lodash_maxBy
    interface LoDashStatic {
        /**
         * This method is like `lodash_max` except that it accepts `iteratee` which is
         * invoked for each element in `array` to generate the criterion by which
         * the value is ranked. The iteratee is invoked with one argument: (value).
         *
         * @static
         * @memberOf _
         * @category Math
         * @param {Array} array The array to iterate over.
         * @param {Function|Object|string} [iteratee=lodash_identity] The iteratee invoked per element.
         * @returns {*} Returns the maximum value.
         * @example
         *
         * var objects = [{ 'n': 1 }, { 'n': 2 }];
         *
         * lodash_maxBy(objects, function(o) { return o.a; });
         * // => { 'n': 2 }
         *
         * // using the `lodash_property` iteratee shorthand
         * lodash_maxBy(objects, 'n');
         * // => { 'n': 2 }
         */
        maxBy<T>(
            collection: List<T>,
            iteratee?: ListIterator<T, any>
        ): T;

        /**
         * @see lodash_maxBy
         */
        maxBy<T>(
            collection: Dictionary<T>,
            iteratee?: DictionaryIterator<T, any>
        ): T;

        /**
         * @see lodash_maxBy
         */
        maxBy<T>(
            collection: List<T>|Dictionary<T>,
            iteratee?: string
        ): T;

        /**
         * @see lodash_maxBy
         */
        maxBy<TObject extends {}, T>(
            collection: List<T>|Dictionary<T>,
            whereValue?: TObject
        ): T;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_maxBy
         */
        maxBy(
            iteratee?: ListIterator<T, any>
        ): T;

        /**
         * @see lodash_maxBy
         */
        maxBy(
            iteratee?: string
        ): T;

        /**
         * @see lodash_maxBy
         */
        maxBy<TObject extends {}>(
            whereValue?: TObject
        ): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_maxBy
         */
        maxBy<T>(
            iteratee?: ListIterator<T, any>|DictionaryIterator<T, any>
        ): T;

        /**
         * @see lodash_maxBy
         */
        maxBy<T>(
            iteratee?: string
        ): T;

        /**
         * @see lodash_maxBy
         */
        maxBy<TObject extends {}, T>(
            whereValue?: TObject
        ): T;
    }

    //lodash_mean
    interface LoDashStatic {
        /**
         * Computes the mean of the values in `array`.
         *
         * @static
         * @memberOf _
         * @category Math
         * @param {Array} array The array to iterate over.
         * @returns {number} Returns the mean.
         * @example
         *
         * lodash_mean([4, 2, 8, 6]);
         * // => 5
         */
        mean<T>(
            collection: List<T>
        ): number;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_mean
         */
        mean<T>(): number;

        /**
         * @see lodash_mean
         */
        mean(): number;
    }

    //lodash_min
    interface LoDashStatic {
        /**
         * Computes the minimum value of `array`. If `array` is empty or falsey
         * `undefined` is returned.
         *
         * @static
         * @memberOf _
         * @category Math
         * @param {Array} array The array to iterate over.
         * @returns {*} Returns the minimum value.
         */
        min<T>(
            collection: List<T>
        ): T;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_min
         */
        min(): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_min
         */
        min<T>(): T;
    }

    //lodash_minBy
    interface LoDashStatic {
        /**
         * This method is like `lodash_min` except that it accepts `iteratee` which is
         * invoked for each element in `array` to generate the criterion by which
         * the value is ranked. The iteratee is invoked with one argument: (value).
         *
         * @static
         * @memberOf _
         * @category Math
         * @param {Array} array The array to iterate over.
         * @param {Function|Object|string} [iteratee=lodash_identity] The iteratee invoked per element.
         * @returns {*} Returns the minimum value.
         * @example
         *
         * var objects = [{ 'n': 1 }, { 'n': 2 }];
         *
         * lodash_minBy(objects, function(o) { return o.a; });
         * // => { 'n': 1 }
         *
         * // using the `lodash_property` iteratee shorthand
         * lodash_minBy(objects, 'n');
         * // => { 'n': 1 }
         */
        minBy<T>(
            collection: List<T>,
            iteratee?: ListIterator<T, any>
        ): T;

        /**
         * @see lodash_minBy
         */
        minBy<T>(
            collection: Dictionary<T>,
            iteratee?: DictionaryIterator<T, any>
        ): T;

        /**
         * @see lodash_minBy
         */
        minBy<T>(
            collection: List<T>|Dictionary<T>,
            iteratee?: string
        ): T;

        /**
         * @see lodash_minBy
         */
        minBy<TObject extends {}, T>(
            collection: List<T>|Dictionary<T>,
            whereValue?: TObject
        ): T;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_minBy
         */
        minBy(
            iteratee?: ListIterator<T, any>
        ): T;

        /**
         * @see lodash_minBy
         */
        minBy(
            iteratee?: string
        ): T;

        /**
         * @see lodash_minBy
         */
        minBy<TObject extends {}>(
            whereValue?: TObject
        ): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_minBy
         */
        minBy<T>(
            iteratee?: ListIterator<T, any>|DictionaryIterator<T, any>
        ): T;

        /**
         * @see lodash_minBy
         */
        minBy<T>(
            iteratee?: string
        ): T;

        /**
         * @see lodash_minBy
         */
        minBy<TObject extends {}, T>(
            whereValue?: TObject
        ): T;
    }

    //lodash_round
    interface LoDashStatic {
        /**
         * Calculates n rounded to precision.
         *
         * @param n The number to round.
         * @param precision The precision to round to.
         * @return Returns the rounded number.
         */
        round(
            n: number,
            precision?: number
        ): number;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_round
         */
        round(precision?: number): number;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_round
         */
        round(precision?: number): LoDashExplicitWrapper<number>;
    }

    //lodash_sum
    interface LoDashStatic {
        /**
         * Computes the sum of the values in `array`.
         *
         * @static
         * @memberOf _
         * @category Math
         * @param {Array} array The array to iterate over.
         * @returns {number} Returns the sum.
         * @example
         *
         * lodash_sum([4, 2, 8, 6]);
         * // => 20
         */
        sum<T>(collection: List<T>): number;

        /**
         * @see lodash_sum
         */
        sum(collection: List<number>|Dictionary<number>): number;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_sum
         */
        sum(): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_sum
         **/
        sum<TValue>(): number;

        /**
         * @see lodash_sum
         */
        sum(): number;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_sum
         */
        sum(): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_sum
         */
        sum<TValue>(): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sum
         */
        sum(): LoDashExplicitWrapper<number>;
    }

    //lodash_sumBy
    interface LoDashStatic {
        /**
         * This method is like `lodash_sum` except that it accepts `iteratee` which is
         * invoked for each element in `array` to generate the value to be summed.
         * The iteratee is invoked with one argument: (value).
         *
         * @static
         * @memberOf _
         * @category Math
         * @param {Array} array The array to iterate over.
         * @param {Function|Object|string} [iteratee=lodash_identity] The iteratee invoked per element.
         * @returns {number} Returns the sum.
         * @example
         *
         * var objects = [{ 'n': 4 }, { 'n': 2 }, { 'n': 8 }, { 'n': 6 }];
         *
         * lodash_sumBy(objects, function(o) { return o.n; });
         * // => 20
         *
         * // using the `lodash_property` iteratee shorthand
         * lodash_sumBy(objects, 'n');
         * // => 20
         */
        sumBy<T>(
            collection: List<T>,
            iteratee: ListIterator<T, number>
        ): number;

        /**
         * @see lodash_sumBy
         */
        sumBy(
            collection: List<{}>,
            iteratee: string
        ): number;

        /**
         * @see lodash_sumBy
         */
        sumBy(
            collection: List<number>
        ): number;

        /**
         * @see lodash_sumBy
         */
        sumBy(
            collection: List<{}>,
            iteratee: Dictionary<{}>
        ): number;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_sumBy
         */
        sumBy(
            iteratee: ListIterator<T, number>
        ): number;

        /**
         * @see lodash_sumBy
         */
        sumBy(iteratee: string): number;

        /**
         * @see lodash_sumBy
         */
        sumBy(iteratee: Dictionary<{}>): number;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_sumBy
         */
        sumBy(
            iteratee: ListIterator<{}, number>
        ): number;

        /**
         * @see lodash_sumBy
         */
        sumBy(iteratee: string): number;

        /**
         * @see lodash_sumBy
         */
        sumBy(iteratee: Dictionary<{}>): number;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_sumBy
         */
        sumBy(
            iteratee: ListIterator<T, number>
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sumBy
         */
        sumBy(iteratee: string): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sumBy
         */
        sumBy(): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sumBy
         */
        sumBy(iteratee: Dictionary<{}>): LoDashExplicitWrapper<number>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_sumBy
         */
        sumBy(
            iteratee: ListIterator<{}, number>
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sumBy
         */
        sumBy(iteratee: string): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_sumBy
         */
        sumBy(iteratee: Dictionary<{}>): LoDashExplicitWrapper<number>;
    }

    /**********
     * Number *
     **********/

        //lodash_subtract
    interface LoDashStatic {
        /**
         * Subtract two numbers.
         *
         * @static
         * @memberOf _
         * @category Math
         * @param {number} minuend The first number in a subtraction.
         * @param {number} subtrahend The second number in a subtraction.
         * @returns {number} Returns the difference.
         * @example
         *
         * lodash_subtract(6, 4);
         * // => 2
         */
        subtract(
            minuend: number,
            subtrahend: number
        ): number;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_subtract
         */
        subtract(
            subtrahend: number
        ): number;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_subtract
         */
        subtract(
            subtrahend: number
        ): LoDashExplicitWrapper<number>;
    }

    //lodash_clamp
    interface LoDashStatic {
        /**
         * Clamps `number` within the inclusive `lower` and `upper` bounds.
         *
         * @static
         * @memberOf _
         * @category Number
         * @param {number} number The number to clamp.
         * @param {number} [lower] The lower bound.
         * @param {number} upper The upper bound.
         * @returns {number} Returns the clamped number.
         * @example
         *
         * lodash_clamp(-10, -5, 5);
         * // => -5
         *
         * lodash_clamp(10, -5, 5);
         * // => 5
         */
        clamp(
            number: number,
            lower: number,
            upper: number
        ): number;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_clamp
         */
        clamp(
            lower: number,
            upper: number
        ): number;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_clamp
         */
        clamp(
            lower: number,
            upper: number
        ): LoDashExplicitWrapper<number>;
    }

    //lodash_inRange
    interface LoDashStatic {
        /**
         * Checks if n is between start and up to but not including, end. If end is not specified it’s set to start
         * with start then set to 0.
         *
         * @param n The number to check.
         * @param start The start of the range.
         * @param end The end of the range.
         * @return Returns true if n is in the range, else false.
         */
        inRange(
            n: number,
            start: number,
            end: number
        ): boolean;


        /**
         * @see lodash_inRange
         */
        inRange(
            n: number,
            end: number
        ): boolean;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_inRange
         */
        inRange(
            start: number,
            end: number
        ): boolean;

        /**
         * @see lodash_inRange
         */
        inRange(end: number): boolean;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_inRange
         */
        inRange(
            start: number,
            end: number
        ): LoDashExplicitWrapper<boolean>;

        /**
         * @see lodash_inRange
         */
        inRange(end: number): LoDashExplicitWrapper<boolean>;
    }

    //lodash_random
    interface LoDashStatic {
        /**
         * Produces a random number between min and max (inclusive). If only one argument is provided a number between
         * 0 and the given number is returned. If floating is true, or either min or max are floats, a floating-point
         * number is returned instead of an integer.
         *
         * @param min The minimum possible value.
         * @param max The maximum possible value.
         * @param floating Specify returning a floating-point number.
         * @return Returns the random number.
         */
        random(
            min?: number,
            max?: number,
            floating?: boolean
        ): number;

        /**
         * @see lodash_random
         */
        random(
            min?: number,
            floating?: boolean
        ): number;

        /**
         * @see lodash_random
         */
        random(floating?: boolean): number;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_random
         */
        random(
            max?: number,
            floating?: boolean
        ): number;

        /**
         * @see lodash_random
         */
        random(floating?: boolean): number;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_random
         */
        random(
            max?: number,
            floating?: boolean
        ): LoDashExplicitWrapper<number>;

        /**
         * @see lodash_random
         */
        random(floating?: boolean): LoDashExplicitWrapper<number>;
    }

    /**********
     * Object *
     **********/

        //lodash_assign
    interface LoDashStatic {
        /**
         * Assigns own enumerable properties of source objects to the destination
         * object. Source objects are applied from left to right. Subsequent sources
         * overwrite property assignments of previous sources.
         *
         * **Note:** This method mutates `object` and is loosely based on
         * [`Object.assign`](https://mdn.io/Object/assign).
         *
         * @static
         * @memberOf _
         * @category Object
         * @param {Object} object The destination object.
         * @param {...Object} [sources] The source objects.
         * @returns {Object} Returns `object`.
         * @example
         *
         * function Foo() {
         *   this.c = 3;
         * }
         *
         * function Bar() {
         *   this.e = 5;
         * }
         *
         * Foo.prototype.d = 4;
         * Bar.prototype.f = 6;
         *
         * lodash_assign({ 'a': 1 }, new Foo, new Bar);
         * // => { 'a': 1, 'c': 3, 'e': 5 }
         */
        assign<TObject, TSource>(
            object: TObject,
            source: TSource
        ): TSource;

        /**
         * @see assign
         */
        assign<TObject, TSource1, TSource2>(
            object: TObject,
            source1: TSource1,
            source2: TSource2
        ): TSource2;

        /**
         * @see assign
         */
        assign<TObject, TSource1, TSource2, TSource3>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): TSource3;

        /**
         * @see assign
         */
        assign<TObject, TSource1, TSource2, TSource3, TSource4>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): TSource4;

        /**
         * @see lodash_assign
         */
        assign<TObject>(object: TObject): TObject;

        /**
         * @see lodash_assign
         */
        assign<TResult>(
            object: any,
            ...otherArgs: any[]
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_assign
         */
        assign<TSource>(
            source: TSource
        ): LoDashImplicitObjectWrapper<TSource>;

        /**
         * @see assign
         */
        assign<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2
        ): LoDashImplicitObjectWrapper<TSource2>;

        /**
         * @see assign
         */
        assign<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): LoDashImplicitObjectWrapperTSource3>;

        /**
         * @see assign
         */
        assign<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): LoDashImplicitObjectWrapper<TSource4>;

        /**
         * @see lodash_assign
         */
        assign(): LoDashImplicitObjectWrapper<T>;

        /**
         * @see lodash_assign
         */
        assign<TResult>(...otherArgs: any[]): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_assign
         */
        assign<TSource>(
            source: TSource
        ): LoDashExplicitObjectWrapper<TSource>;

        /**
         * @see assign
         */
        assign<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2
        ): LoDashExplicitObjectWrapper<TSource2>;

        /**
         * @see assign
         */
        assign<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): LoDashExplicitObjectWrapper<TSource3>;

        /**
         * @see assign
         */
        assign<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): LoDashExplicitObjectWrapper<T>;

        /**
         * @see lodash_assign
         */
        assign(): LoDashExplicitObjectWrapper<T>;

        /**
         * @see lodash_assign
         */
        assign<TResult>(...otherArgs: any[]): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_assignWith
    interface AssignCustomizer {
        (objectValue: any, sourceValue: any, key?: string, object?: {}, source?: {}): any;
    }

    interface LoDashStatic {
        /**
         * This method is like `lodash_assign` except that it accepts `customizer` which
         * is invoked to produce the assigned values. If `customizer` returns `undefined`
         * assignment is handled by the method instead. The `customizer` is invoked
         * with five arguments: (objValue, srcValue, key, object, source).
         *
         * **Note:** This method mutates `object`.
         *
         * @static
         * @memberOf _
         * @category Object
         * @param {Object} object The destination object.
         * @param {...Object} sources The source objects.
         * @param {Function} [customizer] The function to customize assigned values.
         * @returns {Object} Returns `object`.
         * @example
         *
         * function customizer(objValue, srcValue) {
         *   return lodash_isUndefined(objValue) ? srcValue : objValue;
         * }
         *
         * var defaults = lodash_partialRight(lodash_assignWith, customizer);
         *
         * defaults({ 'a': 1 }, { 'b': 2 }, { 'a': 3 });
         * // => { 'a': 1, 'b': 2 }
         */
        assignWith<TObject, TSource>(
            object: TObject,
            source: TSource,
            customizer: AssignCustomizer
        ): TSource;

        /**
         * @see assignWith
         */
        assignWith<TObject, TSource1, TSource2>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            customizer: AssignCustomizer
        ):  TSource2;

        /**
         * @see assignWith
         */
        assignWith<TObject, TSource1, TSource2, TSource3>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            customizer: AssignCustomizer
        ):  TSource3;

        /**
         * @see assignWith
         */
        assignWith<TObject, TSource1, TSource2, TSource3, TSource4>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4,
            customizer: AssignCustomizer
        ):  TSource4;

        /**
         * @see lodash_assignWith
         */
        assignWith<TObject>(object: TObject): TObject;

        /**
         * @see lodash_assignWith
         */
        assignWith<TResult>(
            object: any,
            ...otherArgs: any[]
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_assignWith
         */
        assignWith<TSource>(
            source: TSource,
            customizer: AssignCustomizer
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see assignWith
         */
        assignWith<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2,
            customizer: AssignCustomizer
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see assignWith
         */
        assignWith<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            customizer: AssignCustomizer
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see assignWith
         */
        assignWith<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4,
            customizer: AssignCustomizer
        ): LoDashImplicitObjectWrapper<T>;

        /**
         * @see lodash_assignWith
         */
        assignWith(): LoDashImplicitObjectWrapper<T>;

        /**
         * @see lodash_assignWith
         */
        assignWith<TResult>(...otherArgs: any[]): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_assignWith
         */
        assignWith<TSource>(
            source: TSource,
            customizer: AssignCustomizer
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see assignWith
         */
        assignWith<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2,
            customizer: AssignCustomizer
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see assignWith
         */
        assignWith<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            customizer: AssignCustomizer
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see assignWith
         */
        assignWith<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4,
            customizer: AssignCustomizer
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see lodash_assignWith
         */
        assignWith(): LoDashExplicitObjectWrapper<T>;

        /**
         * @see lodash_assignWith
         */
        assignWith<TResult>(...otherArgs: any[]): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_assignIn
    interface LoDashStatic {
        /**
         * This method is like `lodash_assign` except that it iterates over own and
         * inherited source properties.
         *
         * **Note:** This method mutates `object`.
         *
         * @static
         * @memberOf _
         * @alias extend
         * @category Object
         * @param {Object} object The destination object.
         * @param {...Object} [sources] The source objects.
         * @returns {Object} Returns `object`.
         * @example
         *
         * function Foo() {
         *   this.b = 2;
         * }
         *
         * function Bar() {
         *   this.d = 4;
         * }
         *
         * Foo.prototype.c = 3;
         * Bar.prototype.e = 5;
         *
         * lodash_assignIn({ 'a': 1 }, new Foo, new Bar);
         * // => { 'a': 1, 'b': 2, 'c': 3, 'd': 4, 'e': 5 }
         */
        assignIn<TObject, TSource>(
            object: TObject,
            source: TSource
        ): TSource;

        /**
         * @see assignIn
         */
        assignIn<TObject, TSource1, TSource2>(
            object: TObject,
            source1: TSource1,
            source2: TSource2
        ): TSource2;

        /**
         * @see assignIn
         */
        assignIn<TObject, TSource1, TSource2, TSource3>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): TSource2 ;

        /**
         * @see assignIn
         */
        assignIn<TObject, TSource1, TSource2, TSource3, TSource4>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): TSource2  ;

        /**
         * @see lodash_assignIn
         */
        assignIn<TObject>(object: TObject): TObject;

        /**
         * @see lodash_assignIn
         */
        assignIn<TResult>(
            object: any,
            ...otherArgs: any[]
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_assignIn
         */
        assignIn<TSource>(
            source: TSource
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see assignIn
         */
        assignIn<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see assignIn
         */
        assignIn<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): LoDashImplicitObjectWrapper<T  >;

        /**
         * @see assignIn
         */
        assignIn<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): LoDashImplicitObjectWrapper<T   >;

        /**
         * @see lodash_assignIn
         */
        assignIn(): LoDashImplicitObjectWrapper<T>;

        /**
         * @see lodash_assignIn
         */
        assignIn<TResult>(...otherArgs: any[]): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_assignIn
         */
        assignIn<TSource>(
            source: TSource
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see assignIn
         */
        assignIn<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see assignIn
         */
        assignIn<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): LoDashExplicitObjectWrapper<T  >;

        /**
         * @see assignIn
         */
        assignIn<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): LoDashExplicitObjectWrapper<T   >;

        /**
         * @see lodash_assignIn
         */
        assignIn(): LoDashExplicitObjectWrapper<T>;

        /**
         * @see lodash_assignIn
         */
        assignIn<TResult>(...otherArgs: any[]): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_assignInWith
    interface AssignCustomizer {
        (objectValue: any, sourceValue: any, key?: string, object?: {}, source?: {}): any;
    }

    interface LoDashStatic {
        /**
         * This method is like `lodash_assignIn` except that it accepts `customizer` which
         * is invoked to produce the assigned values. If `customizer` returns `undefined`
         * assignment is handled by the method instead. The `customizer` is invoked
         * with five arguments: (objValue, srcValue, key, object, source).
         *
         * **Note:** This method mutates `object`.
         *
         * @static
         * @memberOf _
         * @alias extendWith
         * @category Object
         * @param {Object} object The destination object.
         * @param {...Object} sources The source objects.
         * @param {Function} [customizer] The function to customize assigned values.
         * @returns {Object} Returns `object`.
         * @example
         *
         * function customizer(objValue, srcValue) {
         *   return lodash_isUndefined(objValue) ? srcValue : objValue;
         * }
         *
         * var defaults = lodash_partialRight(lodash_assignInWith, customizer);
         *
         * defaults({ 'a': 1 }, { 'b': 2 }, { 'a': 3 });
         * // => { 'a': 1, 'b': 2 }
         */
        assignInWith<TObject, TSource>(
            object: TObject,
            source: TSource,
            customizer: AssignCustomizer
        ): TSource;

        /**
         * @see assignInWith
         */
        assignInWith<TObject, TSource1, TSource2>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            customizer: AssignCustomizer
        ): TSource2;

        /**
         * @see assignInWith
         */
        assignInWith<TObject, TSource1, TSource2, TSource3>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            customizer: AssignCustomizer
        ): TSource2 ;

        /**
         * @see assignInWith
         */
        assignInWith<TObject, TSource1, TSource2, TSource3, TSource4>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4,
            customizer: AssignCustomizer
        ): TSource2  ;

        /**
         * @see lodash_assignInWith
         */
        assignInWith<TObject>(object: TObject): TObject;

        /**
         * @see lodash_assignInWith
         */
        assignInWith<TResult>(
            object: any,
            ...otherArgs: any[]
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_assignInWith
         */
        assignInWith<TSource>(
            source: TSource,
            customizer: AssignCustomizer
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see assignInWith
         */
        assignInWith<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2,
            customizer: AssignCustomizer
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see assignInWith
         */
        assignInWith<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            customizer: AssignCustomizer
        ): LoDashImplicitObjectWrapper<T  >;

        /**
         * @see assignInWith
         */
        assignInWith<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4,
            customizer: AssignCustomizer
        ): LoDashImplicitObjectWrapper<T   >;

        /**
         * @see lodash_assignInWith
         */
        assignInWith(): LoDashImplicitObjectWrapper<T>;

        /**
         * @see lodash_assignInWith
         */
        assignInWith<TResult>(...otherArgs: any[]): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_assignInWith
         */
        assignInWith<TSource>(
            source: TSource,
            customizer: AssignCustomizer
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see assignInWith
         */
        assignInWith<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2,
            customizer: AssignCustomizer
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see assignInWith
         */
        assignInWith<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            customizer: AssignCustomizer
        ): LoDashExplicitObjectWrapper<T  >;

        /**
         * @see assignInWith
         */
        assignInWith<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4,
            customizer: AssignCustomizer
        ): LoDashExplicitObjectWrapper<T   >;

        /**
         * @see lodash_assignInWith
         */
        assignInWith(): LoDashExplicitObjectWrapper<T>;

        /**
         * @see lodash_assignInWith
         */
        assignInWith<TResult>(...otherArgs: any[]): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_create
    interface LoDashStatic {
        /**
         * Creates an object that inherits from the given prototype object. If a properties object is provided its own
         * enumerable properties are assigned to the created object.
         *
         * @param prototype The object to inherit from.
         * @param properties The properties to assign to the object.
         * @return Returns the new object.
         */
        create<T extends Object, U extends Object>(
            prototype: T,
            properties?: U
        ): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_create
         */
        create<U extends Object>(properties?: U): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_create
         */
        create<U extends Object>(properties?: U): LoDashExplicitObjectWrapper<T>;
    }


    //lodash_defaults
    interface LoDashStatic {
        /**
         * Assigns own enumerable properties of source object(s) to the destination object for all destination
         * properties that resolve to undefined. Once a property is set, additional values of the same property are
         * ignored.
         *
         * Note: This method mutates object.
         *
         * @param object The destination object.
         * @param sources The source objects.
         * @return The destination object.
         */
        defaults<TObject, TSource>(
            object: TObject,
            source: TSource
        ): TSource;

        /**
         * @see lodash_defaults
         */
        defaults<TObject, TSource1, TSource2>(
            object: TObject,
            source1: TSource1,
            source2: TSource2
        ): TObject;

        /**
         * @see lodash_defaults
         */
        defaults<TObject, TSource1, TSource2, TSource3>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): TObject;

        /**
         * @see lodash_defaults
         */
        defaults<TObject, TSource1, TSource2, TSource3, TSource4>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): TObject;

        /**
         * @see lodash_defaults
         */
        defaults<TObject>(object: TObject): TObject;

        /**
         * @see lodash_defaults
         */
        defaults<TResult>(
            object: any,
            ...sources: any[]
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_defaults
         */
        defaults<TSource>(
            source: TSource
        ): LoDashImplicitObjectWrapper<T>;

        /**
         * @see lodash_defaults
         */
        defaults<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2
        ): LoDashImplicitObjectWrapper<T>;

        /**
         * @see lodash_defaults
         */
        defaults<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): LoDashImplicitObjectWrapper<T>;

        /**
         * @see lodash_defaults
         */
        defaults<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): LoDashImplicitObjectWrapper<T>;

        /**
         * @see lodash_defaults
         */
        defaults(): LoDashImplicitObjectWrapper<T>;

        /**
         * @see lodash_defaults
         */
        defaults<TResult>(...sources: any[]): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_defaults
         */
        defaults<TSource>(
            source: TSource
        ): LoDashExplicitObjectWrapper<T>;

        /**
         * @see lodash_defaults
         */
        defaults<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2
        ): LoDashExplicitObjectWrapper<T>;

        /**
         * @see lodash_defaults
         */
        defaults<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): LoDashExplicitObjectWrapper<T>;

        /**
         * @see lodash_defaults
         */
        defaults<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): LoDashExplicitObjectWrapper<T>;

        /**
         * @see lodash_defaults
         */
        defaults(): LoDashExplicitObjectWrapper<T>;

        /**
         * @see lodash_defaults
         */
        defaults<TResult>(...sources: any[]): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_defaultsDeep
    interface LoDashStatic {
        /**
         * This method is like lodash_defaults except that it recursively assigns default properties.
         * @param object The destination object.
         * @param sources The source objects.
         * @return Returns object.
         **/
        defaultsDeep<T, TResult>(
            object: T,
            ...sources: any[]): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_defaultsDeep
         **/
        defaultsDeep<TResult>(...sources: any[]): LoDashImplicitObjectWrapper<TResult>
    }

    // lodash_extend
    interface LoDashStatic {
        /**
         * @see lodash_assignIn
         */
        extend<TObject, TSource>(
            object: TObject,
            source: TSource
        ): TSource;

        /**
         * @see lodash_assignIn
         */
        extend<TObject, TSource1, TSource2>(
            object: TObject,
            source1: TSource1,
            source2: TSource2
        ): TSource2;

        /**
         * @see lodash_assignIn
         */
        extend<TObject, TSource1, TSource2, TSource3>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): TSource2 ;

        /**
         * @see lodash_assignIn
         */
        extend<TObject, TSource1, TSource2, TSource3, TSource4>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): TSource2  ;

        /**
         * @see lodash_assignIn
         */
        extend<TObject>(object: TObject): TObject;

        /**
         * @see lodash_assignIn
         */
        extend<TResult>(
            object: any,
            ...otherArgs: any[]
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_assignIn
         */
        extend<TSource>(
            source: TSource
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see lodash_assignIn
         */
        extend<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see lodash_assignIn
         */
        extend<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): LoDashImplicitObjectWrapper<T  >;

        /**
         * @see lodash_assignIn
         */
        extend<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): LoDashImplicitObjectWrapper<T   >;

        /**
         * @see lodash_assignIn
         */
        extend(): LoDashImplicitObjectWrapper<T>;

        /**
         * @see lodash_assignIn
         */
        extend<TResult>(...otherArgs: any[]): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_assignIn
         */
        extend<TSource>(
            source: TSource
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see lodash_assignIn
         */
        extend<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see lodash_assignIn
         */
        extend<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): LoDashExplicitObjectWrapper<T  >;

        /**
         * @see lodash_assignIn
         */
        extend<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): LoDashExplicitObjectWrapper<T   >;

        /**
         * @see lodash_assignIn
         */
        extend(): LoDashExplicitObjectWrapper<T>;

        /**
         * @see lodash_assignIn
         */
        extend<TResult>(...otherArgs: any[]): LoDashExplicitObjectWrapper<TResult>;
    }

    interface LoDashStatic {
        /**
         * @see lodash_assignInWith
         */
        extendWith<TObject, TSource>(
            object: TObject,
            source: TSource,
            customizer: AssignCustomizer
        ): TSource;

        /**
         * @see lodash_assignInWith
         */
        extendWith<TObject, TSource1, TSource2>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            customizer: AssignCustomizer
        ): TSource2;

        /**
         * @see lodash_assignInWith
         */
        extendWith<TObject, TSource1, TSource2, TSource3>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            customizer: AssignCustomizer
        ): TSource2 ;

        /**
         * @see lodash_assignInWith
         */
        extendWith<TObject, TSource1, TSource2, TSource3, TSource4>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4,
            customizer: AssignCustomizer
        ): TSource2  ;

        /**
         * @see lodash_assignInWith
         */
        extendWith<TObject>(object: TObject): TObject;

        /**
         * @see lodash_assignInWith
         */
        extendWith<TResult>(
            object: any,
            ...otherArgs: any[]
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_assignInWith
         */
        extendWith<TSource>(
            source: TSource,
            customizer: AssignCustomizer
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see lodash_assignInWith
         */
        extendWith<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2,
            customizer: AssignCustomizer
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see lodash_assignInWith
         */
        extendWith<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            customizer: AssignCustomizer
        ): LoDashImplicitObjectWrapper<T  >;

        /**
         * @see lodash_assignInWith
         */
        extendWith<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4,
            customizer: AssignCustomizer
        ): LoDashImplicitObjectWrapper<T   >;

        /**
         * @see lodash_assignInWith
         */
        extendWith(): LoDashImplicitObjectWrapper<T>;

        /**
         * @see lodash_assignInWith
         */
        extendWith<TResult>(...otherArgs: any[]): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_assignInWith
         */
        extendWith<TSource>(
            source: TSource,
            customizer: AssignCustomizer
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see lodash_assignInWith
         */
        extendWith<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2,
            customizer: AssignCustomizer
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see lodash_assignInWith
         */
        extendWith<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            customizer: AssignCustomizer
        ): LoDashExplicitObjectWrapper<T  >;

        /**
         * @see lodash_assignInWith
         */
        extendWith<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4,
            customizer: AssignCustomizer
        ): LoDashExplicitObjectWrapper<T   >;

        /**
         * @see lodash_assignInWith
         */
        extendWith(): LoDashExplicitObjectWrapper<T>;

        /**
         * @see lodash_assignInWith
         */
        extendWith<TResult>(...otherArgs: any[]): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_findKey
    interface LoDashStatic {
        /**
         * This method is like lodash_find except that it returns the key of the first element predicate returns truthy for
         * instead of the element itself.
         *
         * If a property name is provided for predicate the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for predicate the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * @param object The object to search.
         * @param predicate The function invoked per iteration.
         * @param thisArg The this binding of predicate.
         * @return Returns the key of the matched element, else undefined.
         */
        findKey<TValues, TObject>(
            object: TObject,
            predicate?: DictionaryIterator<TValues, boolean>
        ): string;

        /**
         * @see lodash_findKey
         */
        findKey<TObject>(
            object: TObject,
            predicate?: ObjectIterator<any, boolean>
        ): string;

        /**
         * @see lodash_findKey
         */
        findKey<TObject>(
            object: TObject,
            predicate?: string
        ): string;

        /**
         * @see lodash_findKey
         */
        findKey<TWhere extends Dictionary<any>, TObject>(
            object: TObject,
            predicate?: TWhere
        ): string;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_findKey
         */
        findKey<TValues>(
            predicate?: DictionaryIterator<TValues, boolean>
        ): string;

        /**
         * @see lodash_findKey
         */
        findKey(
            predicate?: ObjectIterator<any, boolean>
        ): string;

        /**
         * @see lodash_findKey
         */
        findKey(
            predicate?: string
        ): string;

        /**
         * @see lodash_findKey
         */
        findKey<TWhere extends Dictionary<any>>(
            predicate?: TWhere
        ): string;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_findKey
         */
        findKey<TValues>(
            predicate?: DictionaryIterator<TValues, boolean>
        ): LoDashExplicitWrapper<string>;

        /**
         * @see lodash_findKey
         */
        findKey(
            predicate?: ObjectIterator<any, boolean>
        ): LoDashExplicitWrapper<string>;

        /**
         * @see lodash_findKey
         */
        findKey(
            predicate?: string
        ): LoDashExplicitWrapper<string>;

        /**
         * @see lodash_findKey
         */
        findKey<TWhere extends Dictionary<any>>(
            predicate?: TWhere
        ): LoDashExplicitWrapper<string>;
    }

    //lodash_findLastKey
    interface LoDashStatic {
        /**
         * This method is like lodash_findKey except that it iterates over elements of a collection in the opposite order.
         *
         * If a property name is provided for predicate the created lodash_property style callback returns the property
         * value of the given element.
         *
         * If a value is also provided for thisArg the created lodash_matchesProperty style callback returns true for
         * elements that have a matching property value, else false.
         *
         * If an object is provided for predicate the created lodash_matches style callback returns true for elements that
         * have the properties of the given object, else false.
         *
         * @param object The object to search.
         * @param predicate The function invoked per iteration.
         * @param thisArg The this binding of predicate.
         * @return Returns the key of the matched element, else undefined.
         */
        findLastKey<TValues, TObject>(
            object: TObject,
            predicate?: DictionaryIterator<TValues, boolean>
        ): string;

        /**
         * @see lodash_findLastKey
         */
        findLastKey<TObject>(
            object: TObject,
            predicate?: ObjectIterator<any, boolean>
        ): string;

        /**
         * @see lodash_findLastKey
         */
        findLastKey<TObject>(
            object: TObject,
            predicate?: string
        ): string;

        /**
         * @see lodash_findLastKey
         */
        findLastKey<TWhere extends Dictionary<any>, TObject>(
            object: TObject,
            predicate?: TWhere
        ): string;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_findLastKey
         */
        findLastKey<TValues>(
            predicate?: DictionaryIterator<TValues, boolean>
        ): string;

        /**
         * @see lodash_findLastKey
         */
        findLastKey(
            predicate?: ObjectIterator<any, boolean>
        ): string;

        /**
         * @see lodash_findLastKey
         */
        findLastKey(
            predicate?: string
        ): string;

        /**
         * @see lodash_findLastKey
         */
        findLastKey<TWhere extends Dictionary<any>>(
            predicate?: TWhere
        ): string;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_findLastKey
         */
        findLastKey<TValues>(
            predicate?: DictionaryIterator<TValues, boolean>
        ): LoDashExplicitWrapper<string>;

        /**
         * @see lodash_findLastKey
         */
        findLastKey(
            predicate?: ObjectIterator<any, boolean>
        ): LoDashExplicitWrapper<string>;

        /**
         * @see lodash_findLastKey
         */
        findLastKey(
            predicate?: string
        ): LoDashExplicitWrapper<string>;

        /**
         * @see lodash_findLastKey
         */
        findLastKey<TWhere extends Dictionary<any>>(
            predicate?: TWhere
        ): LoDashExplicitWrapper<string>;
    }

    //lodash_forIn
    interface LoDashStatic {
        /**
         * Iterates over own and inherited enumerable properties of an object invoking iteratee for each property. The
         * iteratee is bound to thisArg and invoked with three arguments: (value, key, object). Iteratee functions may
         * exit iteration early by explicitly returning false.
         *
         * @param object The object to iterate over.
         * @param iteratee The function invoked per iteration.
         * @param thisArg The this binding of iteratee.
         * @return Returns object.
         */
        forIn<T>(
            object: Dictionary<T>,
            iteratee?: DictionaryIterator<T, any>
        ): Dictionary<T>;

        /**
         * @see lodash_forIn
         */
        forIn<T extends {}>(
            object: T,
            iteratee?: ObjectIterator<any, any>
        ): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_forIn
         */
        forIn<TValue>(
            iteratee?: DictionaryIterator<TValue, any>
        ): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_forIn
         */
        forIn<TValue>(
            iteratee?: DictionaryIterator<TValue, any>
        ): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_forInRight
    interface LoDashStatic {
        /**
         * This method is like lodash_forIn except that it iterates over properties of object in the opposite order.
         *
         * @param object The object to iterate over.
         * @param iteratee The function invoked per iteration.
         * @param thisArg The this binding of iteratee.
         * @return Returns object.
         */
        forInRight<T>(
            object: Dictionary<T>,
            iteratee?: DictionaryIterator<T, any>
        ): Dictionary<T>;

        /**
         * @see lodash_forInRight
         */
        forInRight<T extends {}>(
            object: T,
            iteratee?: ObjectIterator<any, any>
        ): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_forInRight
         */
        forInRight<TValue>(
            iteratee?: DictionaryIterator<TValue, any>
        ): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_forInRight
         */
        forInRight<TValue>(
            iteratee?: DictionaryIterator<TValue, any>
        ): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_forOwn
    interface LoDashStatic {
        /**
         * Iterates over own enumerable properties of an object invoking iteratee for each property. The iteratee is
         * bound to thisArg and invoked with three arguments: (value, key, object). Iteratee functions may exit
         * iteration early by explicitly returning false.
         *
         * @param object The object to iterate over.
         * @param iteratee The function invoked per iteration.
         * @param thisArg The this binding of iteratee.
         * @return Returns object.
         */
        forOwn<T>(
            object: Dictionary<T>,
            iteratee?: DictionaryIterator<T, any>
        ): Dictionary<T>;

        /**
         * @see lodash_forOwn
         */
        forOwn<T extends {}>(
            object: T,
            iteratee?: ObjectIterator<any, any>
        ): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_forOwn
         */
        forOwn<TValue>(
            iteratee?: DictionaryIterator<TValue, any>
        ): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_forOwn
         */
        forOwn<TValue>(
            iteratee?: DictionaryIterator<TValue, any>
        ): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_forOwnRight
    interface LoDashStatic {
        /**
         * This method is like lodash_forOwn except that it iterates over properties of object in the opposite order.
         *
         * @param object The object to iterate over.
         * @param iteratee The function invoked per iteration.
         * @param thisArg The this binding of iteratee.
         * @return Returns object.
         */
        forOwnRight<T>(
            object: Dictionary<T>,
            iteratee?: DictionaryIterator<T, any>
        ): Dictionary<T>;

        /**
         * @see lodash_forOwnRight
         */
        forOwnRight<T extends {}>(
            object: T,
            iteratee?: ObjectIterator<any, any>
        ): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_forOwnRight
         */
        forOwnRight<TValue>(
            iteratee?: DictionaryIterator<TValue, any>
        ): LoDashImplicitObjectWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_forOwnRight
         */
        forOwnRight<TValue>(
            iteratee?: DictionaryIterator<TValue, any>
        ): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_functions
    interface LoDashStatic {
        /**
         * Creates an array of function property names from own enumerable properties
         * of `object`.
         *
         * @static
         * @memberOf _
         * @category Object
         * @param {Object} object The object to inspect.
         * @returns {Array} Returns the new array of property names.
         * @example
         *
         * function Foo() {
         *   this.a = lodash_constant('a');
         *   this.b = lodash_constant('b');
         * }
         *
         * Foo.prototype.c = lodash_constant('c');
         *
         * lodash_functions(new Foo);
         * // => ['a', 'b']
         */
        functions<T extends {}>(object: any): string[];
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_functions
         */
        functions(): LoDashImplicitArrayWrapper<string>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_functions
         */
        functions(): LoDashExplicitArrayWrapper<string>;
    }

    //lodash_functionsIn
    interface LoDashStatic {
        /**
         * Creates an array of function property names from own and inherited
         * enumerable properties of `object`.
         *
         * @static
         * @memberOf _
         * @category Object
         * @param {Object} object The object to inspect.
         * @returns {Array} Returns the new array of property names.
         * @example
         *
         * function Foo() {
         *   this.a = lodash_constant('a');
         *   this.b = lodash_constant('b');
         * }
         *
         * Foo.prototype.c = lodash_constant('c');
         *
         * lodash_functionsIn(new Foo);
         * // => ['a', 'b', 'c']
         */
        functionsIn<T extends {}>(object: any): string[];
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_functionsIn
         */
        functionsIn(): LoDashImplicitArrayWrapper<string>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_functionsIn
         */
        functionsIn(): LoDashExplicitArrayWrapper<string>;
    }

    //lodash_get
    interface LoDashStatic {
        /**
         * Gets the property value at path of object. If the resolved value is undefined the defaultValue is used
         * in its place.
         *
         * @param object The object to query.
         * @param path The path of the property to get.
         * @param defaultValue The value returned if the resolved value is undefined.
         * @return Returns the resolved value.
         */
        get<TObject, TResult>(
            object: TObject,
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: TResult
        ): TResult;

        /**
         * @see lodash_get
         */
        get<TResult>(
            object: any,
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: TResult
        ): TResult;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_get
         */
        get<TResult>(
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: TResult
        ): TResult;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_get
         */
        get<TResult>(
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: TResult
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_get
         */
        get<TResult>(
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: TResult
        ): TResult;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_get
         */
        get<TResultWrapper>(
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: any
        ): TResultWrapper;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_get
         */
        get<TResultWrapper>(
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: any
        ): TResultWrapper;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_get
         */
        get<TResultWrapper>(
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: any
        ): TResultWrapper;
    }

    //lodash_has
    interface LoDashStatic {
        /**
         * Checks if `path` is a direct property of `object`.
         *
         * @static
         * @memberOf _
         * @category Object
         * @param {Object} object The object to query.
         * @param {Array|string} path The path to check.
         * @returns {boolean} Returns `true` if `path` exists, else `false`.
         * @example
         *
         * var object = { 'a': { 'b': { 'c': 3 } } };
         * var other = lodash_create({ 'a': lodash_create({ 'b': lodash_create({ 'c': 3 }) }) });
         *
         * lodash_has(object, 'a');
         * // => true
         *
         * lodash_has(object, 'a.b.c');
         * // => true
         *
         * lodash_has(object, ['a', 'b', 'c']);
         * // => true
         *
         * lodash_has(other, 'a');
         * // => false
         */
        has<T extends {}>(
            object: T,
            path: StringRepresentable|StringRepresentable[]
        ): boolean;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_has
         */
        has(path: StringRepresentable|StringRepresentable[]): boolean;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_has
         */
        has(path: StringRepresentable|StringRepresentable[]): LoDashExplicitWrapper<boolean>;
    }

    //lodash_hasIn
    interface LoDashStatic {
        /**
         * Checks if `path` is a direct or inherited property of `object`.
         *
         * @static
         * @memberOf _
         * @category Object
         * @param {Object} object The object to query.
         * @param {Array|string} path The path to check.
         * @returns {boolean} Returns `true` if `path` exists, else `false`.
         * @example
         *
         * var object = lodash_create({ 'a': lodash_create({ 'b': lodash_create({ 'c': 3 }) }) });
         *
         * lodash_hasIn(object, 'a');
         * // => true
         *
         * lodash_hasIn(object, 'a.b.c');
         * // => true
         *
         * lodash_hasIn(object, ['a', 'b', 'c']);
         * // => true
         *
         * lodash_hasIn(object, 'b');
         * // => false
         */
        hasIn<T extends {}>(
            object: T,
            path: StringRepresentable|StringRepresentable[]
        ): boolean;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_hasIn
         */
        hasIn(path: StringRepresentable|StringRepresentable[]): boolean;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_hasIn
         */
        hasIn(path: StringRepresentable|StringRepresentable[]): LoDashExplicitWrapper<boolean>;
    }

    //lodash_invert
    interface LoDashStatic {
        /**
         * Creates an object composed of the inverted keys and values of object. If object contains duplicate values,
         * subsequent values overwrite property assignments of previous values unless multiValue is true.
         *
         * @param object The object to invert.
         * @param multiValue Allow multiple values per key.
         * @return Returns the new inverted object.
         */
        invert<T extends {}, TResult extends {}>(
            object: T,
            multiValue?: boolean
        ): TResult;

        /**
         * @see lodash_invert
         */
        invert<TResult extends {}>(
            object: Object,
            multiValue?: boolean
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_invert
         */
        invert<TResult extends {}>(multiValue?: boolean): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_invert
         */
        invert<TResult extends {}>(multiValue?: boolean): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_inverBy
    interface InvertByIterator<T> {
        (value: T): any;
    }

    interface LoDashStatic {
        /**
         * This method is like lodash_invert except that the inverted object is generated from the results of running each
         * element of object through iteratee. The corresponding inverted value of each inverted key is an array of
         * keys responsible for generating the inverted value. The iteratee is invoked with one argument: (value).
         *
         * @param object The object to invert.
         * @param interatee The iteratee invoked per element.
         * @return Returns the new inverted object.
         */
        invertBy(
            object: Object,
            interatee?: InvertByIterator<any>|string
        ): Dictionary<string[]>;

        /**
         * @see lodash_invertBy
         */
        invertBy<T>(
            object: Dictionary<T>|lodash_NumericDictionary<T>,
            interatee?: InvertByIterator<T>|string
        ): Dictionary<string[]>;

        /**
         * @see lodash_invertBy
         */
        invertBy<W>(
            object: Object,
            interatee?: W
        ): Dictionary<string[]>;

        /**
         * @see lodash_invertBy
         */
        invertBy<T, W>(
            object: Dictionary<T>,
            interatee?: W
        ): Dictionary<string[]>;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_invertBy
         */
        invertBy(
            interatee?: InvertByIterator<any>
        ): LoDashImplicitObjectWrapper<Dictionary<string[]>>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_invertBy
         */
        invertBy(
            interatee?: InvertByIterator<T>|string
        ): LoDashImplicitObjectWrapper<Dictionary<string[]>>;

        /**
         * @see lodash_invertBy
         */
        invertBy<W>(
            interatee?: W
        ): LoDashImplicitObjectWrapper<Dictionary<string[]>>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_invertBy
         */
        invertBy(
            interatee?: InvertByIterator<any>|string
        ): LoDashImplicitObjectWrapper<Dictionary<string[]>>;

        /**
         * @see lodash_invertBy
         */
        invertBy<W>(
            interatee?: W
        ): LoDashImplicitObjectWrapper<Dictionary<string[]>>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_invertBy
         */
        invertBy(
            interatee?: InvertByIterator<any>
        ): LoDashExplicitObjectWrapper<Dictionary<string[]>>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_invertBy
         */
        invertBy(
            interatee?: InvertByIterator<T>|string
        ): LoDashExplicitObjectWrapper<Dictionary<string[]>>;

        /**
         * @see lodash_invertBy
         */
        invertBy<W>(
            interatee?: W
        ): LoDashExplicitObjectWrapper<Dictionary<string[]>>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_invertBy
         */
        invertBy(
            interatee?: InvertByIterator<any>|string
        ): LoDashExplicitObjectWrapper<Dictionary<string[]>>;

        /**
         * @see lodash_invertBy
         */
        invertBy<W>(
            interatee?: W
        ): LoDashExplicitObjectWrapper<Dictionary<string[]>>;
    }

    //lodash_keys
    interface LoDashStatic {
        /**
         * Creates an array of the own enumerable property names of object.
         *
         * Note: Non-object values are coerced to objects. See the ES spec for more details.
         *
         * @param object The object to query.
         * @return Returns the array of property names.
         */
        keys(object?: any): string[];
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_keys
         */
        keys(): LoDashImplicitArrayWrapper<string>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_keys
         */
        keys(): LoDashExplicitArrayWrapper<string>;
    }

    //lodash_keysIn
    interface LoDashStatic {
        /**
         * Creates an array of the own and inherited enumerable property names of object.
         *
         * Note: Non-object values are coerced to objects.
         *
         * @param object The object to query.
         * @return An array of property names.
         */
        keysIn(object?: any): string[];
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_keysIn
         */
        keysIn(): LoDashImplicitArrayWrapper<string>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_keysIn
         */
        keysIn(): LoDashExplicitArrayWrapper<string>;
    }

    //lodash_mapKeys
    interface LoDashStatic {
        /**
         * The opposite of lodash_mapValues; this method creates an object with the same values as object and keys generated
         * by running each own enumerable property of object through iteratee.
         *
         * @param object The object to iterate over.
         * @param iteratee The function invoked per iteration.
         * @param thisArg The this binding of iteratee.
         * @return Returns the new mapped object.
         */
        mapKeys<T, TKey>(
            object: List<T>,
            iteratee?: ListIterator<T, TKey>
        ): Dictionary<T>;

        /**
         * @see lodash_mapKeys
         */
        mapKeys<T, TKey>(
            object: Dictionary<T>,
            iteratee?: DictionaryIterator<T, TKey>
        ): Dictionary<T>;

        /**
         * @see lodash_mapKeys
         */
        mapKeys<T, TObject extends {}>(
            object: List<T>|Dictionary<T>,
            iteratee?: TObject
        ): Dictionary<T>;

        /**
         * @see lodash_mapKeys
         */
        mapKeys<T>(
            object: List<T>|Dictionary<T>,
            iteratee?: string
        ): Dictionary<T>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_mapKeys
         */
        mapKeys<TKey>(
            iteratee?: ListIterator<T, TKey>
        ): LoDashImplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_mapKeys
         */
        mapKeys<TObject extends {}>(
            iteratee?: TObject
        ): LoDashImplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_mapKeys
         */
        mapKeys(
            iteratee?: string
        ): LoDashImplicitObjectWrapper<Dictionary<T>>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_mapKeys
         */
        mapKeys<TResult, TKey>(
            iteratee?: ListIterator<TResult, TKey>|DictionaryIterator<TResult, TKey>
        ): LoDashImplicitObjectWrapper<Dictionary<TResult>>;

        /**
         * @see lodash_mapKeys
         */
        mapKeys<TResult, TObject extends {}>(
            iteratee?: TObject
        ): LoDashImplicitObjectWrapper<Dictionary<TResult>>;

        /**
         * @see lodash_mapKeys
         */
        mapKeys<TResult>(
            iteratee?: string
        ): LoDashImplicitObjectWrapper<Dictionary<TResult>>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_mapKeys
         */
        mapKeys<TKey>(
            iteratee?: ListIterator<T, TKey>
        ): LoDashExplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_mapKeys
         */
        mapKeys<TObject extends {}>(
            iteratee?: TObject
        ): LoDashExplicitObjectWrapper<Dictionary<T>>;

        /**
         * @see lodash_mapKeys
         */
        mapKeys(
            iteratee?: string
        ): LoDashExplicitObjectWrapper<Dictionary<T>>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_mapKeys
         */
        mapKeys<TResult, TKey>(
            iteratee?: ListIterator<TResult, TKey>|DictionaryIterator<TResult, TKey>
        ): LoDashExplicitObjectWrapper<Dictionary<TResult>>;

        /**
         * @see lodash_mapKeys
         */
        mapKeys<TResult, TObject extends {}>(
            iteratee?: TObject
        ): LoDashExplicitObjectWrapper<Dictionary<TResult>>;

        /**
         * @see lodash_mapKeys
         */
        mapKeys<TResult>(
            iteratee?: string
        ): LoDashExplicitObjectWrapper<Dictionary<TResult>>;
    }

    //lodash_mapValues
    interface LoDashStatic {
        /**
         * Creates an object with the same keys as object and values generated by running each own
         * enumerable property of object through iteratee. The iteratee function is bound to thisArg
         * and invoked with three arguments: (value, key, object).
         *
         * If a property name is provided iteratee the created "lodash_property" style callback returns
         * the property value of the given element.
         *
         * If a value is also provided for thisArg the creted "lodash_matchesProperty" style callback returns
         * true for elements that have a matching property value, else false;.
         *
         * If an object is provided for iteratee the created "lodash_matches" style callback returns true
         * for elements that have the properties of the given object, else false.
         *
         * @param {Object} object The object to iterate over.
         * @param {Function|Object|string} [iteratee=lodash_identity]  The function invoked per iteration.
         * @param {Object} [thisArg] The `this` binding of `iteratee`.
         * @return {Object} Returns the new mapped object.
         */
        mapValues<T, TResult>(obj: Dictionary<T>, callback: ObjectIterator<T, TResult>): Dictionary<TResult>;
        mapValues<T>(obj: Dictionary<T>, where: Dictionary<T>): Dictionary<boolean>;
        mapValues<T, TMapped>(obj: T, pluck: string): TMapped;
        mapValues<T>(obj: T, callback: ObjectIterator<any, any>): T;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_mapValues
         * TValue is the type of the property values of T.
         * TResult is the type output by the ObjectIterator function
         */
        mapValues<TValue, TResult>(callback: ObjectIterator<TValue, TResult>): LoDashImplicitObjectWrapper<Dictionary<TResult>>;

        /**
         * @see lodash_mapValues
         * TResult is the type of the property specified by pluck.
         * T should be a Dictionary<Dictionary<TResult>>
         */
        mapValues<TResult>(pluck: string): LoDashImplicitObjectWrapper<Dictionary<TResult>>;

        /**
         * @see lodash_mapValues
         * TResult is the type of the properties of each object in the values of T
         * T should be a Dictionary<Dictionary<TResult>>
         */
        mapValues<TResult>(where: Dictionary<TResult>): LoDashImplicitArrayWrapper<boolean>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_mapValues
         * TValue is the type of the property values of T.
         * TResult is the type output by the ObjectIterator function
         */
        mapValues<TValue, TResult>(callback: ObjectIterator<TValue, TResult>): LoDashExplicitObjectWrapper<Dictionary<TResult>>;

        /**
         * @see lodash_mapValues
         * TResult is the type of the property specified by pluck.
         * T should be a Dictionary<Dictionary<TResult>>
         */
        mapValues<TResult>(pluck: string): LoDashExplicitObjectWrapper<Dictionary<TResult>>;

        /**
         * @see lodash_mapValues
         * TResult is the type of the properties of each object in the values of T
         * T should be a Dictionary<Dictionary<TResult>>
         */
        mapValues<TResult>(where: Dictionary<TResult>): LoDashExplicitObjectWrapper<boolean>;
    }

    //lodash_merge
    interface LoDashStatic {
        /**
         * Recursively merges own and inherited enumerable properties of source
         * objects into the destination object, skipping source properties that resolve
         * to `undefined`. Array and plain object properties are merged recursively.
         * Other objects and value types are overridden by assignment. Source objects
         * are applied from left to right. Subsequent sources overwrite property
         * assignments of previous sources.
         *
         * **Note:** This method mutates `object`.
         *
         * @static
         * @memberOf _
         * @category Object
         * @param {Object} object The destination object.
         * @param {...Object} [sources] The source objects.
         * @returns {Object} Returns `object`.
         * @example
         *
         * var users = {
         *   'data': [{ 'user': 'barney' }, { 'user': 'fred' }]
         * };
         *
         * var ages = {
         *   'data': [{ 'age': 36 }, { 'age': 40 }]
         * };
         *
         * lodash_merge(users, ages);
         * // => { 'data': [{ 'user': 'barney', 'age': 36 }, { 'user': 'fred', 'age': 40 }] }
         */
        merge<TObject, TSource>(
            object: TObject,
            source: TSource
        ): TSource;

        /**
         * @see lodash_merge
         */
        merge<TObject, TSource1, TSource2>(
            object: TObject,
            source1: TSource1,
            source2: TSource2
        ): TSource2;

        /**
         * @see lodash_merge
         */
        merge<TObject, TSource1, TSource2, TSource3>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): TSource2 ;

        /**
         * @see lodash_merge
         */
        merge<TObject, TSource1, TSource2, TSource3, TSource4>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): TSource2  ;

        /**
         * @see lodash_merge
         */
        merge<TResult>(
            object: any,
            ...otherArgs: any[]
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_merge
         */
        merge<TSource>(
            source: TSource
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see lodash_merge
         */
        merge<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see lodash_merge
         */
        merge<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): LoDashImplicitObjectWrapper<T  >;

        /**
         * @see lodash_merge
         */
        merge<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4
        ): LoDashImplicitObjectWrapper<T   >;

        /**
         * @see lodash_merge
         */
        merge<TResult>(
            ...otherArgs: any[]
        ): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_merge
         */
        merge<TSource>(
            source: TSource
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see lodash_merge
         */
        merge<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2
        ): LoDashExplicitObjectWrapper<T >;

        /**
         * @see lodash_merge
         */
        merge<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3
        ): LoDashExplicitObjectWrapper<T  >;

        /**
         * @see lodash_merge
         */
        merge<TSource1, TSource2, TSource3, TSource4>(
        ): LoDashExplicitObjectWrapper<T   >;

        /**
         * @see lodash_merge
         */
        merge<TResult>(
            ...otherArgs: any[]
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_mergeWith
    interface MergeWithCustomizer {
        (value: any, srcValue: any, key?: string, object?: Object, source?: Object): any;
    }

    interface LoDashStatic {
        /**
         * This method is like `lodash_merge` except that it accepts `customizer` which
         * is invoked to produce the merged values of the destination and source
         * properties. If `customizer` returns `undefined` merging is handled by the
         * method instead. The `customizer` is invoked with seven arguments:
         * (objValue, srcValue, key, object, source, stack).
         *
         * @static
         * @memberOf _
         * @category Object
         * @param {Object} object The destination object.
         * @param {...Object} sources The source objects.
         * @param {Function} customizer The function to customize assigned values.
         * @returns {Object} Returns `object`.
         * @example
         *
         * function customizer(objValue, srcValue) {
         *   if (lodash_isArray(objValue)) {
         *     return objValue.concat(srcValue);
         *   }
         * }
         *
         * var object = {
         *   'fruits': ['apple'],
         *   'vegetables': ['beet']
         * };
         *
         * var other = {
         *   'fruits': ['banana'],
         *   'vegetables': ['carrot']
         * };
         *
         * lodash_merge(object, other, customizer);
         * // => { 'fruits': ['apple', 'banana'], 'vegetables': ['beet', 'carrot'] }
         */
        mergeWith<TObject, TSource>(
            object: TObject,
            source: TSource,
            customizer: MergeWithCustomizer
        ): TSource;

        /**
         * @see lodash_mergeWith
         */
        mergeWith<TObject, TSource1, TSource2>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            customizer: MergeWithCustomizer
        ): TSource2;

        /**
         * @see lodash_mergeWith
         */
        mergeWith<TObject, TSource1, TSource2, TSource3>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            customizer: MergeWithCustomizer
        ): TSource2 ;

        /**
         * @see lodash_mergeWith
         */
        mergeWith<TObject, TSource1, TSource2, TSource3, TSource4>(
            object: TObject,
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4,
            customizer: MergeWithCustomizer
        ): TSource2  ;

        /**
         * @see lodash_mergeWith
         */
        mergeWith<TResult>(
            object: any,
            ...otherArgs: any[]
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_mergeWith
         */
        mergeWith<TSource>(
            source: TSource,
            customizer: MergeWithCustomizer
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see lodash_mergeWith
         */
        mergeWith<TSource1, TSource2>(
            source1: TSource1,
            source2: TSource2,
            customizer: MergeWithCustomizer
        ): LoDashImplicitObjectWrapper<T >;

        /**
         * @see lodash_mergeWith
         */
        mergeWith<TSource1, TSource2, TSource3>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            customizer: MergeWithCustomizer
        ): LoDashImplicitObjectWrapper<T  >;

        /**
         * @see lodash_mergeWith
         */
        mergeWith<TSource1, TSource2, TSource3, TSource4>(
            source1: TSource1,
            source2: TSource2,
            source3: TSource3,
            source4: TSource4,
            customizer: MergeWithCustomizer
        ): LoDashImplicitObjectWrapper<T   >;

        /**
         * @see lodash_mergeWith
         */
        mergeWith<TResult>(
            ...otherArgs: any[]
        ): LoDashImplicitObjectWrapper<TResult>;
    }

    //lodash_omit
    interface LoDashStatic {
        /**
         * The opposite of `lodash_pick`; this method creates an object composed of the
         * own and inherited enumerable properties of `object` that are not omitted.
         *
         * @static
         * @memberOf _
         * @category Object
         * @param {Object} object The source object.
         * @param {...(string|string[])} [props] The property names to omit, specified
         *  individually or in arrays..
         * @returns {Object} Returns the new object.
         * @example
         *
         * var object = { 'a': 1, 'b': '2', 'c': 3 };
         *
         * lodash_omit(object, ['a', 'c']);
         * // => { 'b': '2' }
         */

        omit<TResult extends {}, T extends {}>(
            object: T,
            ...predicate: (StringRepresentable|StringRepresentable[])[]
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {

        /**
         * @see lodash_omit
         */
        omit<TResult extends {}>(
            ...predicate: (StringRepresentable|StringRepresentable[])[]
        ): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {

        /**
         * @see lodash_omit
         */
        omit<TResult extends {}>(
            ...predicate: (StringRepresentable|StringRepresentable[])[]
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_omitBy
    interface LoDashStatic {
        /**
         * The opposite of `lodash_pickBy`; this method creates an object composed of the
         * own and inherited enumerable properties of `object` that `predicate`
         * doesn't return truthy for.
         *
         * @static
         * @memberOf _
         * @category Object
         * @param {Object} object The source object.
         * @param {Function|Object|string} [predicate=lodash_identity] The function invoked per property.
         * @returns {Object} Returns the new object.
         * @example
         *
         * var object = { 'a': 1, 'b': '2', 'c': 3 };
         *
         * lodash_omitBy(object, lodash_isNumber);
         * // => { 'b': '2' }
         */
        omitBy<TResult extends {}, T extends {}>(
            object: T,
            predicate: ObjectIterator<any, boolean>
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_omitBy
         */
        omitBy<TResult extends {}>(
            predicate: ObjectIterator<any, boolean>
        ): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_omitBy
         */
        omitBy<TResult extends {}>(
            predicate: ObjectIterator<any, boolean>
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_pick
    interface LoDashStatic {
        /**
         * Creates an object composed of the picked `object` properties.
         *
         * @static
         * @memberOf _
         * @category Object
         * @param {Object} object The source object.
         * @param {...(string|string[])} [props] The property names to pick, specified
         *  individually or in arrays.
         * @returns {Object} Returns the new object.
         * @example
         *
         * var object = { 'a': 1, 'b': '2', 'c': 3 };
         *
         * lodash_pick(object, ['a', 'c']);
         * // => { 'a': 1, 'c': 3 }
         */
        pick<TResult extends {}, T extends {}>(
            object: T,
            ...predicate: (StringRepresentable|StringRepresentable[])[]
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_pick
         */
        pick<TResult extends {}>(
            ...predicate: (StringRepresentable|StringRepresentable[])[]
        ): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_pick
         */
        pick<TResult extends {}>(
            ...predicate: (StringRepresentable|StringRepresentable[])[]
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_pickBy
    interface LoDashStatic {
        /**
         * Creates an object composed of the `object` properties `predicate` returns
         * truthy for. The predicate is invoked with one argument: (value).
         *
         * @static
         * @memberOf _
         * @category Object
         * @param {Object} object The source object.
         * @param {Function|Object|string} [predicate=lodash_identity] The function invoked per property.
         * @returns {Object} Returns the new object.
         * @example
         *
         * var object = { 'a': 1, 'b': '2', 'c': 3 };
         *
         * lodash_pickBy(object, lodash_isNumber);
         * // => { 'a': 1, 'c': 3 }
         */
        pickBy<TResult extends {}, T extends {}>(
            object: T,
            predicate?: ObjectIterator<any, boolean>
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_pickBy
         */
        pickBy<TResult extends {}>(
            predicate?: ObjectIterator<any, boolean>
        ): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_pickBy
         */
        pickBy<TResult extends {}>(
            predicate?: ObjectIterator<any, boolean>
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_result
    interface LoDashStatic {
        /**
         * This method is like lodash_get except that if the resolved value is a function it’s invoked with the this binding
         * of its parent object and its result is returned.
         *
         * @param object The object to query.
         * @param path The path of the property to resolve.
         * @param defaultValue The value returned if the resolved value is undefined.
         * @return Returns the resolved value.
         */
        result<TObject, TResult>(
            object: TObject,
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: TResult|((...args: any[]) => TResult)
        ): TResult;

        /**
         * @see lodash_result
         */
        result<TResult>(
            object: any,
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: TResult|((...args: any[]) => TResult)
        ): TResult;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_result
         */
        result<TResult>(
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: TResult|((...args: any[]) => TResult)
        ): TResult;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_result
         */
        result<TResult>(
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: TResult|((...args: any[]) => TResult)
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_result
         */
        result<TResult>(
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: TResult|((...args: any[]) => TResult)
        ): TResult;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_result
         */
        result<TResultWrapper>(
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: any
        ): TResultWrapper;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_result
         */
        result<TResultWrapper>(
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: any
        ): TResultWrapper;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_result
         */
        result<TResultWrapper>(
            path: StringRepresentable|StringRepresentable[],
            defaultValue?: any
        ): TResultWrapper;
    }

    //lodash_set
    interface LoDashStatic {
        /**
         * Sets the value at path of object. If a portion of path doesn’t exist it’s created. Arrays are created for
         * missing index properties while objects are created for all other missing properties. Use lodash_setWith to
         * customize path creation.
         *
         * @param object The object to modify.
         * @param path The path of the property to set.
         * @param value The value to set.
         * @return Returns object.
         */
        set<TResult>(
            object: Object,
            path: StringRepresentable|StringRepresentable[],
            value: any
        ): TResult;

        /**
         * @see lodash_set
         */
        set<V, TResult>(
            object: Object,
            path: StringRepresentable|StringRepresentable[],
            value: V
        ): TResult;

        /**
         * @see lodash_set
         */
        set<O, V, TResult>(
            object: O,
            path: StringRepresentable|StringRepresentable[],
            value: V
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_set
         */
        set<TResult>(
            path: StringRepresentable|StringRepresentable[],
            value: any
        ): LoDashImplicitObjectWrapper<TResult>;

        /**
         * @see lodash_set
         */
        set<V, TResult>(
            path: StringRepresentable|StringRepresentable[],
            value: V
        ): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_set
         */
        set<TResult>(
            path: StringRepresentable|StringRepresentable[],
            value: any
        ): LoDashExplicitObjectWrapper<TResult>;

        /**
         * @see lodash_set
         */
        set<V, TResult>(
            path: StringRepresentable|StringRepresentable[],
            value: V
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_setWith
    interface SetWithCustomizer<T> {
        (nsValue: any, key: string, nsObject: T): any;
    }

    interface LoDashStatic {
        /**
         * This method is like lodash_set except that it accepts customizer which is invoked to produce the objects of
         * path. If customizer returns undefined path creation is handled by the method instead. The customizer is
         * invoked with three arguments: (nsValue, key, nsObject).
         *
         * @param object The object to modify.
         * @param path The path of the property to set.
         * @param value The value to set.
         * @parem customizer The function to customize assigned values.
         * @return Returns object.
         */
        setWith<TResult>(
            object: Object,
            path: StringRepresentable|StringRepresentable[],
            value: any,
            customizer?: SetWithCustomizer<Object>
        ): TResult;

        /**
         * @see lodash_setWith
         */
        setWith<V, TResult>(
            object: Object,
            path: StringRepresentable|StringRepresentable[],
            value: V,
            customizer?: SetWithCustomizer<Object>
        ): TResult;

        /**
         * @see lodash_setWith
         */
        setWith<O, V, TResult>(
            object: O,
            path: StringRepresentable|StringRepresentable[],
            value: V,
            customizer?: SetWithCustomizer<O>
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_setWith
         */
        setWith<TResult>(
            path: StringRepresentable|StringRepresentable[],
            value: any,
            customizer?: SetWithCustomizer<T>
        ): LoDashImplicitObjectWrapper<TResult>;

        /**
         * @see lodash_setWith
         */
        setWith<V, TResult>(
            path: StringRepresentable|StringRepresentable[],
            value: V,
            customizer?: SetWithCustomizer<T>
        ): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_setWith
         */
        setWith<TResult>(
            path: StringRepresentable|StringRepresentable[],
            value: any,
            customizer?: SetWithCustomizer<T>
        ): LoDashExplicitObjectWrapper<TResult>;

        /**
         * @see lodash_setWith
         */
        setWith<V, TResult>(
            path: StringRepresentable|StringRepresentable[],
            value: V,
            customizer?: SetWithCustomizer<T>
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_toPairs
    interface LoDashStatic {
        /**
         * Creates an array of own enumerable key-value pairs for object.
         *
         * @param object The object to query.
         * @return Returns the new array of key-value pairs.
         */
        toPairs<T extends {}>(object?: T): any[][];

        toPairs<T extends {}, TResult>(object?: T): TResult[][];
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_toPairs
         */
        toPairs<TResult>(): LoDashImplicitArrayWrapper<TResult[]>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_toPairs
         */
        toPairs<TResult>(): LoDashExplicitArrayWrapper<TResult[]>;
    }

    //lodash_toPairsIn
    interface LoDashStatic {
        /**
         * Creates an array of own and inherited enumerable key-value pairs for object.
         *
         * @param object The object to query.
         * @return Returns the new array of key-value pairs.
         */
        toPairsIn<T extends {}>(object?: T): any[][];

        toPairsIn<T extends {}, TResult>(object?: T): TResult[][];
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_toPairsIn
         */
        toPairsIn<TResult>(): LoDashImplicitArrayWrapper<TResult[]>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_toPairsIn
         */
        toPairsIn<TResult>(): LoDashExplicitArrayWrapper<TResult[]>;
    }

    //lodash_transform
    interface LoDashStatic {
        /**
         * An alternative to lodash_reduce; this method transforms object to a new accumulator object which is the result of
         * running each of its own enumerable properties through iteratee, with each invocation potentially mutating
         * the accumulator object. The iteratee is bound to thisArg and invoked with four arguments: (accumulator,
         * value, key, object). Iteratee functions may exit iteration early by explicitly returning false.
         *
         * @param object The object to iterate over.
         * @param iteratee The function invoked per iteration.
         * @param accumulator The custom accumulator value.
         * @param thisArg The this binding of iteratee.
         * @return Returns the accumulated value.
         */
        transform<T, TResult>(
            object: T[],
            iteratee?: MemoVoidArrayIterator<T, TResult[]>,
            accumulator?: TResult[]
        ): TResult[];

        /**
         * @see lodash_transform
         */
        transform<T, TResult>(
            object: T[],
            iteratee?: MemoVoidArrayIterator<T, Dictionary<TResult>>,
            accumulator?: Dictionary<TResult>
        ): Dictionary<TResult>;

        /**
         * @see lodash_transform
         */
        transform<T, TResult>(
            object: Dictionary<T>,
            iteratee?: MemoVoidDictionaryIterator<T, Dictionary<TResult>>,
            accumulator?: Dictionary<TResult>
        ): Dictionary<TResult>;

        /**
         * @see lodash_transform
         */
        transform<T, TResult>(
            object: Dictionary<T>,
            iteratee?: MemoVoidDictionaryIterator<T, TResult[]>,
            accumulator?: TResult[]
        ): TResult[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_transform
         */
        transform<TResult>(
            iteratee?: MemoVoidArrayIterator<T, TResult[]>,
            accumulator?: TResult[]
        ): LoDashImplicitArrayWrapper<TResult>;

        /**
         * @see lodash_transform
         */
        transform<TResult>(
            iteratee?: MemoVoidArrayIterator<T, Dictionary<TResult>>,
            accumulator?: Dictionary<TResult>
        ): LoDashImplicitObjectWrapper<Dictionary<TResult>>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_transform
         */
        transform<T, TResult>(
            iteratee?: MemoVoidDictionaryIterator<T, Dictionary<TResult>>,
            accumulator?: Dictionary<TResult>
        ): LoDashImplicitObjectWrapper<Dictionary<TResult>>;

        /**
         * @see lodash_transform
         */
        transform<T, TResult>(
            iteratee?: MemoVoidDictionaryIterator<T, TResult[]>,
            accumulator?: TResult[]
        ): LoDashImplicitArrayWrapper<TResult>;
    }

    //lodash_unset
    interface LoDashStatic {
        /**
         * Removes the property at path of object.
         *
         * Note: This method mutates object.
         *
         * @param object The object to modify.
         * @param path The path of the property to unset.
         * @return Returns true if the property is deleted, else false.
         */
        unset<T>(
            object: T,
            path: StringRepresentable|StringRepresentable[]
        ): boolean;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_unset
         */
        unset(path: StringRepresentable|StringRepresentable[]): LoDashImplicitWrapper<boolean>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_unset
         */
        unset(path: StringRepresentable|StringRepresentable[]): LoDashExplicitWrapper<boolean>;
    }

    //lodash_update
    interface LoDashStatic {
        /**
         * This method is like lodash_set except that accepts updater to produce the value to set. Use lodash_updateWith to
         * customize path creation. The updater is invoked with one argument: (value).
         *
         * @param object The object to modify.
         * @param path The path of the property to set.
         * @param updater The function to produce the updated value.
         * @return Returns object.
         */
        update<TResult>(
            object: Object,
            path: StringRepresentable|StringRepresentable[],
            updater: Function
        ): TResult;

        /**
         * @see lodash_update
         */
        update<U extends Function, TResult>(
            object: Object,
            path: StringRepresentable|StringRepresentable[],
            updater: U
        ): TResult;

        /**
         * @see lodash_update
         */
        update<O extends {}, TResult>(
            object: O,
            path: StringRepresentable|StringRepresentable[],
            updater: Function
        ): TResult;

        /**
         * @see lodash_update
         */
        update<O, U extends Function, TResult>(
            object: O,
            path: StringRepresentable|StringRepresentable[],
            updater: U
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_update
         */
        update<TResult>(
            path: StringRepresentable|StringRepresentable[],
            updater: any
        ): LoDashImplicitObjectWrapper<TResult>;

        /**
         * @see lodash_update
         */
        update<U extends Function, TResult>(
            path: StringRepresentable|StringRepresentable[],
            updater: U
        ): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_update
         */
        update<TResult>(
            path: StringRepresentable|StringRepresentable[],
            updater: any
        ): LoDashExplicitObjectWrapper<TResult>;

        /**
         * @see lodash_update
         */
        update<U extends Function, TResult>(
            path: StringRepresentable|StringRepresentable[],
            updater: U
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_values
    interface LoDashStatic {
        /**
         * Creates an array of the own enumerable property values of object.
         *
         * @param object The object to query.
         * @return Returns an array of property values.
         */
        values<T>(object?: Dictionary<T>): T[];

        /**
         * @see lodash_values
         */
        values<T>(object?: any): T[];
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_values
         */
        values<T>(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_values
         */
        values<T>(): LoDashExplicitArrayWrapper<T>;
    }

    //lodash_valuesIn
    interface LoDashStatic {
        /**
         * Creates an array of the own and inherited enumerable property values of object.
         *
         * @param object The object to query.
         * @return Returns the array of property values.
         */
        valuesIn<T>(object?: Dictionary<T>): T[];

        /**
         * @see lodash_valuesIn
         */
        valuesIn<T>(object?: any): T[];
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_valuesIn
         */
        valuesIn<T>(): LoDashImplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_valuesIn
         */
        valuesIn<T>(): LoDashExplicitArrayWrapper<T>;
    }

    /**********
     * String *
     **********/

        //lodash_camelCase
    interface LoDashStatic {
        /**
         * Converts string to camel case.
         *
         * @param string The string to convert.
         * @return Returns the camel cased string.
         */
        camelCase(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_camelCase
         */
        camelCase(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_camelCase
         */
        camelCase(): LoDashExplicitWrapper<string>;
    }

    //lodash_capitalize
    interface LoDashStatic {
        /**
         * Converts the first character of string to upper case and the remaining to lower case.
         *
         * @param string The string to capitalize.
         * @return Returns the capitalized string.
         */
        capitalize(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_capitalize
         */
        capitalize(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_capitalize
         */
        capitalize(): LoDashExplicitWrapper<string>;
    }

    //lodash_deburr
    interface LoDashStatic {
        /**
         * Deburrs string by converting latin-1 supplementary letters to basic latin letters and removing combining
         * diacritical marks.
         *
         * @param string The string to deburr.
         * @return Returns the deburred string.
         */
        deburr(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_deburr
         */
        deburr(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_deburr
         */
        deburr(): LoDashExplicitWrapper<string>;
    }

    //lodash_endsWith
    interface LoDashStatic {
        /**
         * Checks if string ends with the given target string.
         *
         * @param string The string to search.
         * @param target The string to search for.
         * @param position The position to search from.
         * @return Returns true if string ends with target, else false.
         */
        endsWith(
            string?: string,
            target?: string,
            position?: number
        ): boolean;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_endsWith
         */
        endsWith(
            target?: string,
            position?: number
        ): boolean;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_endsWith
         */
        endsWith(
            target?: string,
            position?: number
        ): LoDashExplicitWrapper<boolean>;
    }

    // lodash_escape
    interface LoDashStatic {
        /**
         * Converts the characters "&", "<", ">", '"', "'", and "`" in string to their corresponding HTML entities.
         *
         * Note: No other characters are escaped. To escape additional characters use a third-party library like he.
         *
         * hough the ">" character is escaped for symmetry, characters like ">" and "/" don’t need escaping in HTML
         * and have no special meaning unless they're part of a tag or unquoted attribute value. See Mathias Bynens’s
         * article (under "semi-related fun fact") for more details.
         *
         * Backticks are escaped because in IE < 9, they can break out of attribute values or HTML comments. See #59,
         * #102, #108, and #133 of the HTML5 Security Cheatsheet for more details.
         *
         * When working with HTML you should always quote attribute values to reduce XSS vectors.
         *
         * @param string The string to escape.
         * @return Returns the escaped string.
         */
        escape(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_escape
         */
        escape(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_escape
         */
        escape(): LoDashExplicitWrapper<string>;
    }

    // lodash_escapeRegExp
    interface LoDashStatic {
        /**
         * Escapes the RegExp special characters "^", "$", "\", ".", "*", "+", "?", "(", ")", "[", "]",
         * "{", "}", and "|" in string.
         *
         * @param string The string to escape.
         * @return Returns the escaped string.
         */
        escapeRegExp(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_escapeRegExp
         */
        escapeRegExp(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_escapeRegExp
         */
        escapeRegExp(): LoDashExplicitWrapper<string>;
    }

    //lodash_kebabCase
    interface LoDashStatic {
        /**
         * Converts string to kebab case.
         *
         * @param string The string to convert.
         * @return Returns the kebab cased string.
         */
        kebabCase(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_kebabCase
         */
        kebabCase(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_kebabCase
         */
        kebabCase(): LoDashExplicitWrapper<string>;
    }

    //lodash_lowerCase
    interface LoDashStatic {
        /**
         * Converts `string`, as space separated words, to lower case.
         *
         * @param string The string to convert.
         * @return Returns the lower cased string.
         */
        lowerCase(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_lowerCase
         */
        lowerCase(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_lowerCase
         */
        lowerCase(): LoDashExplicitWrapper<string>;
    }

    //lodash_lowerFirst
    interface LoDashStatic {
        /**
         * Converts the first character of `string` to lower case.
         *
         * @param string The string to convert.
         * @return Returns the converted string.
         */
        lowerFirst(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_lowerFirst
         */
        lowerFirst(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_lowerFirst
         */
        lowerFirst(): LoDashExplicitWrapper<string>;
    }

    //lodash_pad
    interface LoDashStatic {
        /**
         * Pads string on the left and right sides if it’s shorter than length. Padding characters are truncated if
         * they can’t be evenly divided by length.
         *
         * @param string The string to pad.
         * @param length The padding length.
         * @param chars The string used as padding.
         * @return Returns the padded string.
         */
        pad(
            string?: string,
            length?: number,
            chars?: string
        ): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_pad
         */
        pad(
            length?: number,
            chars?: string
        ): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_pad
         */
        pad(
            length?: number,
            chars?: string
        ): LoDashExplicitWrapper<string>;
    }

    //lodash_padEnd
    interface LoDashStatic {
        /**
         * Pads string on the right side if it’s shorter than length. Padding characters are truncated if they exceed
         * length.
         *
         * @param string The string to pad.
         * @param length The padding length.
         * @param chars The string used as padding.
         * @return Returns the padded string.
         */
        padEnd(
            string?: string,
            length?: number,
            chars?: string
        ): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_padEnd
         */
        padEnd(
            length?: number,
            chars?: string
        ): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_padEnd
         */
        padEnd(
            length?: number,
            chars?: string
        ): LoDashExplicitWrapper<string>;
    }

    //lodash_padStart
    interface LoDashStatic {
        /**
         * Pads string on the left side if it’s shorter than length. Padding characters are truncated if they exceed
         * length.
         *
         * @param string The string to pad.
         * @param length The padding length.
         * @param chars The string used as padding.
         * @return Returns the padded string.
         */
        padStart(
            string?: string,
            length?: number,
            chars?: string
        ): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_padStart
         */
        padStart(
            length?: number,
            chars?: string
        ): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_padStart
         */
        padStart(
            length?: number,
            chars?: string
        ): LoDashExplicitWrapper<string>;
    }

    //lodash_parseInt
    interface LoDashStatic {
        /**
         * Converts string to an integer of the specified radix. If radix is undefined or 0, a radix of 10 is used
         * unless value is a hexadecimal, in which case a radix of 16 is used.
         *
         * Note: This method aligns with the ES5 implementation of parseInt.
         *
         * @param string The string to convert.
         * @param radix The radix to interpret value by.
         * @return Returns the converted integer.
         */
        parseInt(
            string: string,
            radix?: number
        ): number;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_parseInt
         */
        parseInt(radix?: number): number;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_parseInt
         */
        parseInt(radix?: number): LoDashExplicitWrapper<number>;
    }

    //lodash_repeat
    interface LoDashStatic {
        /**
         * Repeats the given string n times.
         *
         * @param string The string to repeat.
         * @param n The number of times to repeat the string.
         * @return Returns the repeated string.
         */
        repeat(
            string?: string,
            n?: number
        ): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_repeat
         */
        repeat(n?: number): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_repeat
         */
        repeat(n?: number): LoDashExplicitWrapper<string>;
    }

    //lodash_replace
    interface LoDashStatic {
        /**
         * Replaces matches for pattern in string with replacement.
         *
         * Note: This method is based on String#replace.
         *
         * @param string
         * @param pattern
         * @param replacement
         * @return Returns the modified string.
         */
        replace(
            string: string,
            pattern: RegExp|string,
            replacement: Function|string
        ): string;

        /**
         * @see lodash_replace
         */
        replace(
            pattern?: RegExp|string,
            replacement?: Function|string
        ): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_replace
         */
        replace(
            pattern?: RegExp|string,
            replacement?: Function|string
        ): string;

        /**
         * @see lodash_replace
         */
        replace(
            replacement?: Function|string
        ): string;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_replace
         */
        replace(
            pattern?: RegExp|string,
            replacement?: Function|string
        ): string;

        /**
         * @see lodash_replace
         */
        replace(
            replacement?: Function|string
        ): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_replace
         */
        replace(
            pattern?: RegExp|string,
            replacement?: Function|string
        ): LoDashExplicitWrapper<string>;

        /**
         * @see lodash_replace
         */
        replace(
            replacement?: Function|string
        ): LoDashExplicitWrapper<string>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_replace
         */
        replace(
            pattern?: RegExp|string,
            replacement?: Function|string
        ): LoDashExplicitWrapper<string>;

        /**
         * @see lodash_replace
         */
        replace(
            replacement?: Function|string
        ): LoDashExplicitWrapper<string>;
    }

    //lodash_snakeCase
    interface LoDashStatic {
        /**
         * Converts string to snake case.
         *
         * @param string The string to convert.
         * @return Returns the snake cased string.
         */
        snakeCase(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_snakeCase
         */
        snakeCase(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_snakeCase
         */
        snakeCase(): LoDashExplicitWrapper<string>;
    }

    //lodash_split
    interface LoDashStatic {
        /**
         * Splits string by separator.
         *
         * Note: This method is based on String#split.
         *
         * @param string
         * @param separator
         * @param limit
         * @return Returns the new array of string segments.
         */
        split(
            string: string,
            separator?: RegExp|string,
            limit?: number
        ): string[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_split
         */
        split(
            separator?: RegExp|string,
            limit?: number
        ): LoDashImplicitArrayWrapper<string>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_split
         */
        split(
            separator?: RegExp|string,
            limit?: number
        ): LoDashExplicitArrayWrapper<string>;
    }

    //lodash_startCase
    interface LoDashStatic {
        /**
         * Converts string to start case.
         *
         * @param string The string to convert.
         * @return Returns the start cased string.
         */
        startCase(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_startCase
         */
        startCase(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_startCase
         */
        startCase(): LoDashExplicitWrapper<string>;
    }

    //lodash_startsWith
    interface LoDashStatic {
        /**
         * Checks if string starts with the given target string.
         *
         * @param string The string to search.
         * @param target The string to search for.
         * @param position The position to search from.
         * @return Returns true if string starts with target, else false.
         */
        startsWith(
            string?: string,
            target?: string,
            position?: number
        ): boolean;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_startsWith
         */
        startsWith(
            target?: string,
            position?: number
        ): boolean;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_startsWith
         */
        startsWith(
            target?: string,
            position?: number
        ): LoDashExplicitWrapper<boolean>;
    }

    //lodash_template
    interface TemplateOptions extends TemplateSettings {
        /**
         * The sourceURL of the template's compiled source.
         */
        sourceURL?: string;
    }

    interface TemplateExecutor {
        (data?: Object): string;
        source: string;
    }

    interface LoDashStatic {
        /**
         * Creates a compiled template function that can interpolate data properties in "interpolate" delimiters,
         * HTML-escape interpolated data properties in "escape" delimiters, and execute JavaScript in "evaluate"
         * delimiters. Data properties may be accessed as free variables in the template. If a setting object is
         * provided it takes precedence over lodash_templateSettings values.
         *
         * Note: In the development build lodash_template utilizes
         * [sourceURLs](http://www.html5rocks.com/en/tutorials/developertools/sourcemaps/#toc-sourceurl) for easier
         * debugging.
         *
         * For more information on precompiling templates see
         * [lodash's custom builds documentation](https://lodash.com/custom-builds).
         *
         * For more information on Chrome extension sandboxes see
         * [Chrome's extensions documentation](https://developer.chrome.com/extensions/sandboxingEval).
         *
         * @param string The template string.
         * @param options The options object.
         * @param options.escape The HTML "escape" delimiter.
         * @param options.evaluate The "evaluate" delimiter.
         * @param options.imports An object to import into the template as free variables.
         * @param options.interpolate The "interpolate" delimiter.
         * @param options.sourceURL The sourceURL of the template's compiled source.
         * @param options.variable The data object variable name.
         * @return Returns the compiled template function.
         */
        template(
            string: string,
            options?: TemplateOptions
        ): TemplateExecutor;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_template
         */
        template(options?: TemplateOptions): TemplateExecutor;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_template
         */
        template(options?: TemplateOptions): LoDashExplicitObjectWrapper<TemplateExecutor>;
    }

    //lodash_toLower
    interface LoDashStatic {
        /**
         * Converts `string`, as a whole, to lower case.
         *
         * @param string The string to convert.
         * @return Returns the lower cased string.
         */
        toLower(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_toLower
         */
        toLower(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_toLower
         */
        toLower(): LoDashExplicitWrapper<string>;
    }

    //lodash_toUpper
    interface LoDashStatic {
        /**
         * Converts `string`, as a whole, to upper case.
         *
         * @param string The string to convert.
         * @return Returns the upper cased string.
         */
        toUpper(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_toUpper
         */
        toUpper(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_toUpper
         */
        toUpper(): LoDashExplicitWrapper<string>;
    }

    //lodash_trim
    interface LoDashStatic {
        /**
         * Removes leading and trailing whitespace or specified characters from string.
         *
         * @param string The string to trim.
         * @param chars The characters to trim.
         * @return Returns the trimmed string.
         */
        trim(
            string?: string,
            chars?: string
        ): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_trim
         */
        trim(chars?: string): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_trim
         */
        trim(chars?: string): LoDashExplicitWrapper<string>;
    }

    //lodash_trimEnd
    interface LoDashStatic {
        /**
         * Removes trailing whitespace or specified characters from string.
         *
         * @param string The string to trim.
         * @param chars The characters to trim.
         * @return Returns the trimmed string.
         */
        trimEnd(
            string?: string,
            chars?: string
        ): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_trimEnd
         */
        trimEnd(chars?: string): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_trimEnd
         */
        trimEnd(chars?: string): LoDashExplicitWrapper<string>;
    }

    //lodash_trimStart
    interface LoDashStatic {
        /**
         * Removes leading whitespace or specified characters from string.
         *
         * @param string The string to trim.
         * @param chars The characters to trim.
         * @return Returns the trimmed string.
         */
        trimStart(
            string?: string,
            chars?: string
        ): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_trimStart
         */
        trimStart(chars?: string): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_trimStart
         */
        trimStart(chars?: string): LoDashExplicitWrapper<string>;
    }

    //lodash_truncate
    interface TruncateOptions {
        /** The maximum string length. */
        length?: number;
        /** The string to indicate text is omitted. */
        omission?: string;
        /** The separator pattern to truncate to. */
        separator?: string|RegExp;
    }

    interface LoDashStatic {
        /**
         * Truncates string if it’s longer than the given maximum string length. The last characters of the truncated
         * string are replaced with the omission string which defaults to "…".
         *
         * @param string The string to truncate.
         * @param options The options object or maximum string length.
         * @return Returns the truncated string.
         */
        truncate(
            string?: string,
            options?: TruncateOptions
        ): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_truncate
         */
        truncate(options?: TruncateOptions): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_truncate
         */
        truncate(options?: TruncateOptions): LoDashExplicitWrapper<string>;
    }

    //lodash_unescape
    interface LoDashStatic {
        /**
         * The inverse of lodash_escape; this method converts the HTML entities &amp;, &lt;, &gt;, &quot;, &#39;, and &#96;
         * in string to their corresponding characters.
         *
         * Note: No other HTML entities are unescaped. To unescape additional HTML entities use a third-party library
         * like he.
         *
         * @param string The string to unescape.
         * @return Returns the unescaped string.
         */
        unescape(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_unescape
         */
        unescape(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_unescape
         */
        unescape(): LoDashExplicitWrapper<string>;
    }

    //lodash_upperCase
    interface LoDashStatic {
        /**
         * Converts `string`, as space separated words, to upper case.
         *
         * @param string The string to convert.
         * @return Returns the upper cased string.
         */
        upperCase(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_upperCase
         */
        upperCase(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_upperCase
         */
        upperCase(): LoDashExplicitWrapper<string>;
    }

    //lodash_upperFirst
    interface LoDashStatic {
        /**
         * Converts the first character of `string` to upper case.
         *
         * @param string The string to convert.
         * @return Returns the converted string.
         */
        upperFirst(string?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_upperFirst
         */
        upperFirst(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_upperFirst
         */
        upperFirst(): LoDashExplicitWrapper<string>;
    }

    //lodash_words
    interface LoDashStatic {
        /**
         * Splits `string` into an array of its words.
         *
         * @param string The string to inspect.
         * @param pattern The pattern to match words.
         * @return Returns the words of `string`.
         */
        words(
            string?: string,
            pattern?: string|RegExp
        ): string[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_words
         */
        words(pattern?: string|RegExp): string[];
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_words
         */
        words(pattern?: string|RegExp): LoDashExplicitArrayWrapper<string>;
    }

    /***********
     * Utility *
     ***********/

        //lodash_attempt
    interface LoDashStatic {
        /**
         * Attempts to invoke func, returning either the result or the caught error object. Any additional arguments
         * are provided to func when it’s invoked.
         *
         * @param func The function to attempt.
         * @return Returns the func result or error object.
         */
        attempt<TResult>(func: (...args: any[]) => TResult, ...args: any[]): TResult|Error;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_attempt
         */
        attempt<TResult>(...args: any[]): TResult|Error;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_attempt
         */
        attempt<TResult>(...args: any[]): LoDashExplicitObjectWrapper<TResult|Error>;
    }

    //lodash_constant
    interface LoDashStatic {
        /**
         * Creates a function that returns value.
         *
         * @param value The value to return from the new function.
         * @return Returns the new function.
         */
        constant<T>(value: T): () => T;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_constant
         */
        constant<TResult>(): LoDashImplicitObjectWrapper<() => TResult>;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_constant
         */
        constant<TResult>(): LoDashExplicitObjectWrapper<() => TResult>;
    }

    //lodash_identity
    interface LoDashStatic {
        /**
         * This method returns the first argument provided to it.
         *
         * @param value Any value.
         * @return Returns value.
         */
        identity<T>(value?: T): T;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_identity
         */
        identity(): T;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_identity
         */
        identity(): T[];
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_identity
         */
        identity(): T;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_identity
         */
        identity(): LoDashExplicitWrapper<T>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_identity
         */
        identity(): LoDashExplicitArrayWrapper<T>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_identity
         */
        identity(): LoDashExplicitObjectWrapper<T>;
    }

    //lodash_iteratee
    interface LoDashStatic {
        /**
         * Creates a function that invokes `func` with the arguments of the created
         * function. If `func` is a property name the created callback returns the
         * property value for a given element. If `func` is an object the created
         * callback returns `true` for elements that contain the equivalent object properties, otherwise it returns `false`.
         *
         * @static
         * @memberOf _
         * @category Util
         * @param {*} [func=lodash_identity] The value to convert to a callback.
         * @returns {Function} Returns the callback.
         * @example
         *
         * var users = [
         *   { 'user': 'barney', 'age': 36 },
         *   { 'user': 'fred',   'age': 40 }
         * ];
         *
         * // create custom iteratee shorthands
         * lodash_iteratee = lodash_wrap(lodash_iteratee, function(callback, func) {
         *   var p = /^(\S+)\s*([<>])\s*(\S+)$/.exec(func);
         *   return !p ? callback(func) : function(object) {
         *     return (p[2] == '>' ? object[p[1]] > p[3] : object[p[1]] < p[3]);
         *   };
         * });
         *
         * lodash_filter(users, 'age > 36');
         * // => [{ 'user': 'fred', 'age': 40 }]
         */
        iteratee<TResult>(
            func: Function
        ): (...args: any[]) => TResult;

        /**
         * @see lodash_iteratee
         */
        iteratee<TResult>(
            func: string
        ): (object: any) => TResult;

        /**
         * @see lodash_iteratee
         */
        iteratee(
            func: Object
        ): (object: any) => boolean;

        /**
         * @see lodash_iteratee
         */
        iteratee<TResult>(): (value: TResult) => TResult;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_iteratee
         */
        iteratee<TResult>(): LoDashImplicitObjectWrapper<(object: any) => TResult>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_iteratee
         */
        iteratee(): LoDashImplicitObjectWrapper<(object: any) => boolean>;

        /**
         * @see lodash_iteratee
         */
        iteratee<TResult>(): LoDashImplicitObjectWrapper<(...args: any[]) => TResult>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_iteratee
         */
        iteratee<TResult>(): LoDashExplicitObjectWrapper<(object: any) => TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_iteratee
         */
        iteratee(): LoDashExplicitObjectWrapper<(object: any) => boolean>;

        /**
         * @see lodash_iteratee
         */
        iteratee<TResult>(): LoDashExplicitObjectWrapper<(...args: any[]) => TResult>;
    }

    //lodash_matches
    interface LoDashStatic {
        /**
         * Creates a function that performs a deep comparison between a given object and source, returning true if the
         * given object has equivalent property values, else false.
         *
         * Note: This method supports comparing arrays, booleans, Date objects, numbers, Object objects, regexes, and
         * strings. Objects are compared by their own, not inherited, enumerable properties. For comparing a single own
         * or inherited property value see lodash_matchesProperty.
         *
         * @param source The object of property values to match.
         * @return Returns the new function.
         */
        matches<T>(source: T): (value: any) => boolean;

        /**
         * @see lodash_matches
         */
        matches<T, V>(source: T): (value: V) => boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_matches
         */
        matches<V>(): LoDashImplicitObjectWrapper<(value: V) => boolean>;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_matches
         */
        matches<V>(): LoDashExplicitObjectWrapper<(value: V) => boolean>;
    }

    //lodash_matchesProperty
    interface LoDashStatic {
        /**
         * Creates a function that compares the property value of path on a given object to value.
         *
         * Note: This method supports comparing arrays, booleans, Date objects, numbers, Object objects, regexes, and
         * strings. Objects are compared by their own, not inherited, enumerable properties.
         *
         * @param path The path of the property to get.
         * @param srcValue The value to match.
         * @return Returns the new function.
         */
        matchesProperty<T>(
            path: StringRepresentable|StringRepresentable[],
            srcValue: T
        ): (value: any) => boolean;

        /**
         * @see lodash_matchesProperty
         */
        matchesProperty<T, V>(
            path: StringRepresentable|StringRepresentable[],
            srcValue: T
        ): (value: V) => boolean;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_matchesProperty
         */
        matchesProperty<SrcValue>(
            srcValue: SrcValue
        ): LoDashImplicitObjectWrapper<(value: any) => boolean>;

        /**
         * @see lodash_matchesProperty
         */
        matchesProperty<SrcValue, Value>(
            srcValue: SrcValue
        ): LoDashImplicitObjectWrapper<(value: Value) => boolean>;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_matchesProperty
         */
        matchesProperty<SrcValue>(
            srcValue: SrcValue
        ): LoDashExplicitObjectWrapper<(value: any) => boolean>;

        /**
         * @see lodash_matchesProperty
         */
        matchesProperty<SrcValue, Value>(
            srcValue: SrcValue
        ): LoDashExplicitObjectWrapper<(value: Value) => boolean>;
    }

    //lodash_method
    interface LoDashStatic {
        /**
         * Creates a function that invokes the method at path on a given object. Any additional arguments are provided
         * to the invoked method.
         *
         * @param path The path of the method to invoke.
         * @param args The arguments to invoke the method with.
         * @return Returns the new function.
         */
        method<TObject, TResult>(
            path: string|StringRepresentable[],
            ...args: any[]
        ): (object: TObject) => TResult;

        /**
         * @see lodash_method
         */
        method<TResult>(
            path: string|StringRepresentable[],
            ...args: any[]
        ): (object: any) => TResult;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_method
         */
        method<TObject, TResult>(...args: any[]): LoDashImplicitObjectWrapper<(object: TObject) => TResult>;

        /**
         * @see lodash_method
         */
        method<TResult>(...args: any[]): LoDashImplicitObjectWrapper<(object: any) => TResult>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_method
         */
        method<TObject, TResult>(...args: any[]): LoDashImplicitObjectWrapper<(object: TObject) => TResult>;

        /**
         * @see lodash_method
         */
        method<TResult>(...args: any[]): LoDashImplicitObjectWrapper<(object: any) => TResult>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_method
         */
        method<TObject, TResult>(...args: any[]): LoDashExplicitObjectWrapper<(object: TObject) => TResult>;

        /**
         * @see lodash_method
         */
        method<TResult>(...args: any[]): LoDashExplicitObjectWrapper<(object: any) => TResult>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_method
         */
        method<TObject, TResult>(...args: any[]): LoDashExplicitObjectWrapper<(object: TObject) => TResult>;

        /**
         * @see lodash_method
         */
        method<TResult>(...args: any[]): LoDashExplicitObjectWrapper<(object: any) => TResult>;
    }

    //lodash_methodOf
    interface LoDashStatic {
        /**
         * The opposite of lodash_method; this method creates a function that invokes the method at a given path on object.
         * Any additional arguments are provided to the invoked method.
         *
         * @param object The object to query.
         * @param args The arguments to invoke the method with.
         * @return Returns the new function.
         */
        methodOf<TObject extends {}, TResult>(
            object: TObject,
            ...args: any[]
        ): (path: StringRepresentable|StringRepresentable[]) => TResult;

        /**
         * @see lodash_methodOf
         */
        methodOf<TResult>(
            object: {},
            ...args: any[]
        ): (path: StringRepresentable|StringRepresentable[]) => TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_methodOf
         */
        methodOf<TResult>(
            ...args: any[]
        ): LoDashImplicitObjectWrapper<(path: StringRepresentable|StringRepresentable[]) => TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_methodOf
         */
        methodOf<TResult>(
            ...args: any[]
        ): LoDashExplicitObjectWrapper<(path: StringRepresentable|StringRepresentable[]) => TResult>;
    }

    //lodash_mixin
    interface MixinOptions {
        chain?: boolean;
    }

    interface LoDashStatic {
        /**
         * Adds all own enumerable function properties of a source object to the destination object. If object is a
         * function then methods are added to its prototype as well.
         *
         * Note: Use lodash_runInContext to create a pristine lodash function to avoid conflicts caused by modifying
         * the original.
         *
         * @param object The destination object.
         * @param source The object of functions to add.
         * @param options The options object.
         * @param options.chain Specify whether the functions added are chainable.
         * @return Returns object.
         */
        mixin<TResult, TObject>(
            object: TObject,
            source: Dictionary<Function>,
            options?: MixinOptions
        ): TResult;

        /**
         * @see lodash_mixin
         */
        mixin<TResult>(
            source: Dictionary<Function>,
            options?: MixinOptions
        ): TResult;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_mixin
         */
        mixin<TResult>(
            source: Dictionary<Function>,
            options?: MixinOptions
        ): LoDashImplicitObjectWrapper<TResult>;

        /**
         * @see lodash_mixin
         */
        mixin<TResult>(
            options?: MixinOptions
        ): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_mixin
         */
        mixin<TResult>(
            source: Dictionary<Function>,
            options?: MixinOptions
        ): LoDashExplicitObjectWrapper<TResult>;

        /**
         * @see lodash_mixin
         */
        mixin<TResult>(
            options?: MixinOptions
        ): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_noConflict
    interface LoDashStatic {
        /**
         * Reverts the _ variable to its previous value and returns a reference to the lodash function.
         *
         * @return Returns the lodash function.
         */
        noConflict(): typeof _;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_noConflict
         */
        noConflict(): typeof _;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_noConflict
         */
        noConflict(): LoDashExplicitObjectWrapper<typeof _>;
    }

    //lodash_noop
    interface LoDashStatic {
        /**
         * A no-operation function that returns undefined regardless of the arguments it receives.
         *
         * @return undefined
         */
        noop(...args: any[]): void;
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_noop
         */
        noop(...args: any[]): void;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_noop
         */
        noop(...args: any[]): lodash_LoDashExplicitWrapper<void>;
    }

    //lodash_nthArg
    interface LoDashStatic {
        /**
         * Creates a function that returns its nth argument.
         *
         * @param n The index of the argument to return.
         * @return Returns the new function.
         */
        nthArg<TResult extends Function>(n?: number): TResult;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_nthArg
         */
        nthArg<TResult extends Function>(): LoDashImplicitObjectWrapper<TResult>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_nthArg
         */
        nthArg<TResult extends Function>(): LoDashExplicitObjectWrapper<TResult>;
    }

    //lodash_over
    interface LoDashStatic {
        /**
         * Creates a function that invokes iteratees with the arguments provided to the created function and returns
         * their results.
         *
         * @param iteratees The iteratees to invoke.
         * @return Returns the new function.
         */
        over<TResult>(...iteratees: (Function|Function[])[]): (...args: any[]) => TResult[];
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_over
         */
        over<TResult>(...iteratees: (Function|Function[])[]): LoDashImplicitObjectWrapper<(...args: any[]) => TResult[]>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_over
         */
        over<TResult>(...iteratees: (Function|Function[])[]): LoDashImplicitObjectWrapper<(...args: any[]) => TResult[]>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_over
         */
        over<TResult>(...iteratees: (Function|Function[])[]): LoDashExplicitObjectWrapper<(...args: any[]) => TResult[]>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_over
         */
        over<TResult>(...iteratees: (Function|Function[])[]): LoDashExplicitObjectWrapper<(...args: any[]) => TResult[]>;
    }

    //lodash_overEvery
    interface LoDashStatic {
        /**
         * Creates a function that checks if all of the predicates return truthy when invoked with the arguments
         * provided to the created function.
         *
         * @param predicates The predicates to check.
         * @return Returns the new function.
         */
        overEvery(...predicates: (Function|Function[])[]): (...args: any[]) => boolean;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_overEvery
         */
        overEvery(...predicates: (Function|Function[])[]): LoDashImplicitObjectWrapper<(...args: any[]) => boolean>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_overEvery
         */
        overEvery(...predicates: (Function|Function[])[]): LoDashImplicitObjectWrapper<(...args: any[]) => boolean>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_overEvery
         */
        overEvery(...predicates: (Function|Function[])[]): LoDashExplicitObjectWrapper<(...args: any[]) => boolean>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_overEvery
         */
        overEvery(...predicates: (Function|Function[])[]): LoDashExplicitObjectWrapper<(...args: any[]) => boolean>;
    }

    //lodash_overSome
    interface LoDashStatic {
        /**
         * Creates a function that checks if any of the predicates return truthy when invoked with the arguments
         * provided to the created function.
         *
         * @param predicates The predicates to check.
         * @return Returns the new function.
         */
        overSome(...predicates: (Function|Function[])[]): (...args: any[]) => boolean;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_overSome
         */
        overSome(...predicates: (Function|Function[])[]): LoDashImplicitObjectWrapper<(...args: any[]) => boolean>;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_overSome
         */
        overSome(...predicates: (Function|Function[])[]): LoDashImplicitObjectWrapper<(...args: any[]) => boolean>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_overSome
         */
        overSome(...predicates: (Function|Function[])[]): LoDashExplicitObjectWrapper<(...args: any[]) => boolean>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_overSome
         */
        overSome(...predicates: (Function|Function[])[]): LoDashExplicitObjectWrapper<(...args: any[]) => boolean>;
    }

    //lodash_property
    interface LoDashStatic {
        /**
         * Creates a function that returns the property value at path on a given object.
         *
         * @param path The path of the property to get.
         * @return Returns the new function.
         */
        property<TObj, TResult>(path: StringRepresentable|StringRepresentable[]): (obj: TObj) => TResult;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_property
         */
        property<TObj, TResult>(): LoDashImplicitObjectWrapper<(obj: TObj) => TResult>;
    }

    interface LoDashImplicitArrayWrapper<T> {
        /**
         * @see lodash_property
         */
        property<TObj, TResult>(): LoDashImplicitObjectWrapper<(obj: TObj) => TResult>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_property
         */
        property<TObj, TResult>(): LoDashExplicitObjectWrapper<(obj: TObj) => TResult>;
    }

    interface LoDashExplicitArrayWrapper<T> {
        /**
         * @see lodash_property
         */
        property<TObj, TResult>(): LoDashExplicitObjectWrapper<(obj: TObj) => TResult>;
    }

    //lodash_propertyOf
    interface LoDashStatic {
        /**
         * The opposite of lodash_property; this method creates a function that returns the property value at a given path
         * on object.
         *
         * @param object The object to query.
         * @return Returns the new function.
         */
        propertyOf<T extends {}>(object: T): (path: string|string[]) => any;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_propertyOf
         */
        propertyOf(): LoDashImplicitObjectWrapper<(path: string|string[]) => any>;
    }

    interface LoDashExplicitObjectWrapper<T> {
        /**
         * @see lodash_propertyOf
         */
        propertyOf(): LoDashExplicitObjectWrapper<(path: string|string[]) => any>;
    }

    //lodash_range
    interface LoDashStatic {
        /**
         * Creates an array of numbers (positive and/or negative) progressing from start up to, but not including, end.
         * If end is not specified it’s set to start with start then set to 0. If end is less than start a zero-length
         * range is created unless a negative step is specified.
         *
         * @param start The start of the range.
         * @param end The end of the range.
         * @param step The value to increment or decrement by.
         * @return Returns a new range array.
         */
        range(
            start: number,
            end: number,
            step?: number
        ): number[];

        /**
         * @see lodash_range
         */
        range(
            end: number,
            step?: number
        ): number[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_range
         */
        range(
            end?: number,
            step?: number
        ): LoDashImplicitArrayWrapper<number>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_range
         */
        range(
            end?: number,
            step?: number
        ): LoDashExplicitArrayWrapper<number>;
    }

    //lodash_rangeRight
    interface LoDashStatic {
        /**
         * This method is like `lodash_range` except that it populates values in
         * descending order.
         *
         * @static
         * @memberOf _
         * @category Util
         * @param {number} [start=0] The start of the range.
         * @param {number} end The end of the range.
         * @param {number} [step=1] The value to increment or decrement by.
         * @returns {Array} Returns the new array of numbers.
         * @example
         *
         * lodash_rangeRight(4);
         * // => [3, 2, 1, 0]
         *
         * lodash_rangeRight(-4);
         * // => [-3, -2, -1, 0]
         *
         * lodash_rangeRight(1, 5);
         * // => [4, 3, 2, 1]
         *
         * lodash_rangeRight(0, 20, 5);
         * // => [15, 10, 5, 0]
         *
         * lodash_rangeRight(0, -4, -1);
         * // => [-3, -2, -1, 0]
         *
         * lodash_rangeRight(1, 4, 0);
         * // => [1, 1, 1]
         *
         * lodash_rangeRight(0);
         * // => []
         */
        rangeRight(
            start: number,
            end: number,
            step?: number
        ): number[];

        /**
         * @see lodash_rangeRight
         */
        rangeRight(
            end: number,
            step?: number
        ): number[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_rangeRight
         */
        rangeRight(
            end?: number,
            step?: number
        ): LoDashImplicitArrayWrapper<number>;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_rangeRight
         */
        rangeRight(
            end?: number,
            step?: number
        ): LoDashExplicitArrayWrapper<number>;
    }

    //lodash_runInContext
    interface LoDashStatic {
        /**
         * Create a new pristine lodash function using the given context object.
         *
         * @param context The context object.
         * @return Returns a new lodash function.
         */
        runInContext(context?: Object): typeof _;
    }

    interface LoDashImplicitObjectWrapper<T> {
        /**
         * @see lodash_runInContext
         */
        runInContext(): typeof _;
    }

    //lodash_times
    interface LoDashStatic {
        /**
         * Invokes the iteratee function n times, returning an array of the results of each invocation. The iteratee
         * is invoked with one argument; (index).
         *
         * @param n The number of times to invoke iteratee.
         * @param iteratee The function invoked per iteration.
         * @return Returns the array of results.
         */
        times<TResult>(
            n: number,
            iteratee: (num: number) => TResult
        ): TResult[];

        /**
         * @see lodash_times
         */
        times(n: number): number[];
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_times
         */
        times<TResult>(
            iteratee: (num: number) => TResult
        ): TResult[];

        /**
         * @see lodash_times
         */
        times(): number[];
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_times
         */
        times<TResult>(
            iteratee: (num: number) => TResult
        ): LoDashExplicitArrayWrapper<TResult>;

        /**
         * @see lodash_times
         */
        times(): LoDashExplicitArrayWrapper<number>;
    }

    //lodash_toPath
    interface LoDashStatic {
        /**
         * Converts `value` to a property path array.
         *
         * @static
         * @memberOf _
         * @category Util
         * @param {*} value The value to convert.
         * @returns {Array} Returns the new property path array.
         * @example
         *
         * lodash_toPath('a.b.c');
         * // => ['a', 'b', 'c']
         *
         * lodash_toPath('a[0].b.c');
         * // => ['a', '0', 'b', 'c']
         *
         * var path = ['a', 'b', 'c'],
         *     newPath = lodash_toPath(path);
         *
         * console.log(newPath);
         * // => ['a', 'b', 'c']
         *
         * console.log(path === newPath);
         * // => false
         */
        toPath(value: any): string[];
    }

    interface LoDashImplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_toPath
         */
        toPath(): LoDashImplicitWrapper<string[]>;
    }

    interface LoDashExplicitWrapperBase<T, TWrapper> {
        /**
         * @see lodash_toPath
         */
        toPath(): LoDashExplicitWrapper<string[]>;
    }

    //lodash_uniqueId
    interface LoDashStatic {
        /**
         * Generates a unique ID. If prefix is provided the ID is appended to it.
         *
         * @param prefix The value to prefix the ID with.
         * @return Returns the unique ID.
         */
        uniqueId(prefix?: string): string;
    }

    interface LoDashImplicitWrapper<T> {
        /**
         * @see lodash_uniqueId
         */
        uniqueId(): string;
    }

    interface LoDashExplicitWrapper<T> {
        /**
         * @see lodash_uniqueId
         */
        uniqueId(): LoDashExplicitWrapper<string>;
    }

    interface ListIterator<T, TResult> {
        (value: T, index: number, collection: List<T>): TResult;
    }

    interface DictionaryIterator<T, TResult> {
        (value: T, key?: string, collection?: Dictionary<T>): TResult;
    }

    interface NumericDictionaryIterator<T, TResult> {
        (value: T, key?: number, collection?: Dictionary<T>): TResult;
    }

    interface ObjectIterator<T, TResult> {
        (element: T, key?: string, collection?: any): TResult;
    }

    interface StringIterator<TResult> {
        (char: string, index?: number, string?: string): TResult;
    }

    interface MemoVoidIterator<T, TResult> {
        (prev: TResult, curr: T, indexOrKey?: any, list?: T[]): void;
    }
    interface MemoIterator<T, TResult> {
        (prev: TResult, curr: T, indexOrKey?: any, list?: T[]): TResult;
    }

    interface MemoVoidArrayIterator<T, TResult> {
        (acc: TResult, curr: T, index?: number, arr?: T[]): void;
    }
    interface MemoVoidDictionaryIterator<T, TResult> {
        (acc: TResult, curr: T, key?: string, dict?: Dictionary<T>): void;
    }

    //interface Collection<T> {}

    // Common interface between Arrays and jQuery objects
    interface List<T> {
        [index: number]: T;
        length: number;
    }

    interface Dictionary<T> {
        [index: string]: T;
    }

    interface NumericDictionary<T> {
        [index: number]: T;
    }

    interface StringRepresentable {
        toString(): string;
    }

    interface Cancelable {
        cancel(): void;
        flush(): void;
    }
}

// Named exports

declare module "lodash/after" {
    const after: typeof lodash_after;
    export = after;
}


declare module "lodash/ary" {
    const ary: typeof lodash_ary;
    export = ary;
}


declare module "lodash/assign" {
    const assign: typeof lodash_assign;
    export = assign;
}


declare module "lodash/assignIn" {
    const assignIn: typeof lodash_assignIn;
    export = assignIn;
}


declare module "lodash/assignInWith" {
    const assignInWith: typeof lodash_assignInWith;
    export = assignInWith;
}


declare module "lodash/assignWith" {
    const assignWith: typeof lodash_assignWith;
    export = assignWith;
}


declare module "lodash/at" {
    const at: typeof lodash_at;
    export = at;
}


declare module "lodash/before" {
    const before: typeof lodash_before;
    export = before;
}


declare module "lodash/bind" {
    const bind: typeof lodash_bind;
    export = bind;
}


declare module "lodash/bindAll" {
    const bindAll: typeof lodash_bindAll;
    export = bindAll;
}


declare module "lodash/bindKey" {
    const bindKey: typeof lodash_bindKey;
    export = bindKey;
}


declare module "lodash/castArray" {
    const castArray: typeof lodash_castArray;
    export = castArray;
}


declare module "lodash/chain" {
    const chain: typeof lodash_chain;
    export = chain;
}


declare module "lodash/chunk" {
    const chunk: typeof lodash_chunk;
    export = chunk;
}


declare module "lodash/compact" {
    const compact: typeof lodash_compact;
    export = compact;
}


declare module "lodash/concat" {
    const concat: typeof lodash_concat;
    export = concat;
}

/**
 * uncoment it if definition exists
 */
/*
 declare module "lodash/cond" {
 const cond: typeof lodash_cond;
 export = cond;
 }
 */

/**
 * uncoment it if definition exists
 */
/*
 declare module "lodash/conforms" {
 const conforms: typeof lodash_conforms;
 export = conforms;
 }
 */

declare module "lodash/constant" {
    const constant: typeof lodash_constant;
    export = constant;
}


declare module "lodash/countBy" {
    const countBy: typeof lodash_countBy;
    export = countBy;
}


declare module "lodash/create" {
    const create: typeof lodash_create;
    export = create;
}


declare module "lodash/curry" {
    const curry: typeof lodash_curry;
    export = curry;
}


declare module "lodash/curryRight" {
    const curryRight: typeof lodash_curryRight;
    export = curryRight;
}


declare module "lodash/debounce" {
    const debounce: typeof lodash_debounce;
    export = debounce;
}


declare module "lodash/defaults" {
    const defaults: typeof lodash_defaults;
    export = defaults;
}


declare module "lodash/defaultsDeep" {
    const defaultsDeep: typeof lodash_defaultsDeep;
    export = defaultsDeep;
}


declare module "lodash/defer" {
    const defer: typeof lodash_defer;
    export = defer;
}


declare module "lodash/delay" {
    const delay: typeof lodash_delay;
    export = delay;
}


declare module "lodash/difference" {
    const difference: typeof lodash_difference;
    export = difference;
}


declare module "lodash/differenceBy" {
    const differenceBy: typeof lodash_differenceBy;
    export = differenceBy;
}


declare module "lodash/differenceWith" {
    const differenceWith: typeof lodash_differenceWith;
    export = differenceWith;
}


declare module "lodash/drop" {
    const drop: typeof lodash_drop;
    export = drop;
}


declare module "lodash/dropRight" {
    const dropRight: typeof lodash_dropRight;
    export = dropRight;
}


declare module "lodash/dropRightWhile" {
    const dropRightWhile: typeof lodash_dropRightWhile;
    export = dropRightWhile;
}


declare module "lodash/dropWhile" {
    const dropWhile: typeof lodash_dropWhile;
    export = dropWhile;
}


declare module "lodash/fill" {
    const fill: typeof lodash_fill;
    export = fill;
}


declare module "lodash/filter" {
    const filter: typeof lodash_filter;
    export = filter;
}


declare module "lodash/flatMap" {
    const flatMap: typeof lodash_flatMap;
    export = flatMap;
}

/**
 * uncoment it if definition exists
 */
/*
 declare module "lodash/flatMapDeep" {
 const flatMapDeep: typeof lodash_flatMapDeep;
 export = flatMapDeep;
 }
 */
/**
 * uncoment it if definition exists
 */
/*
 declare module "lodash/flatMapDepth" {
 const flatMapDepth: typeof lodash_flatMapDepth;
 export = flatMapDepth;
 }
 */

declare module "lodash/flatten" {
    const flatten: typeof lodash_flatten;
    export = flatten;
}


declare module "lodash/flattenDeep" {
    const flattenDeep: typeof lodash_flattenDeep;
    export = flattenDeep;
}

declare module "lodash/flattenDepth" {
    const flattenDepth: typeof lodash_flattenDepth;
    export = flattenDepth;
}

declare module "lodash/flip" {
    const flip: typeof lodash_flip;
    export = flip;
}


declare module "lodash/flow" {
    const flow: typeof lodash_flow;
    export = flow;
}


declare module "lodash/flowRight" {
    const flowRight: typeof lodash_flowRight;
    export = flowRight;
}


declare module "lodash/fromPairs" {
    const fromPairs: typeof lodash_fromPairs;
    export = fromPairs;
}


declare module "lodash/functions" {
    const functions: typeof lodash_functions;
    export = functions;
}


declare module "lodash/functionsIn" {
    const functionsIn: typeof lodash_functionsIn;
    export = functionsIn;
}


declare module "lodash/groupBy" {
    const groupBy: typeof lodash_groupBy;
    export = groupBy;
}


declare module "lodash/initial" {
    const initial: typeof lodash_initial;
    export = initial;
}


declare module "lodash/intersection" {
    const intersection: typeof lodash_intersection;
    export = intersection;
}


declare module "lodash/intersectionBy" {
    const intersectionBy: typeof lodash_intersectionBy;
    export = intersectionBy;
}


declare module "lodash/intersectionWith" {
    const intersectionWith: typeof lodash_intersectionWith;
    export = intersectionWith;
}


declare module "lodash/invert" {
    const invert: typeof lodash_invert;
    export = invert;
}


declare module "lodash/invertBy" {
    const invertBy: typeof lodash_invertBy;
    export = invertBy;
}


declare module "lodash/invokeMap" {
    const invokeMap: typeof lodash_invokeMap;
    export = invokeMap;
}


declare module "lodash/iteratee" {
    const iteratee: typeof lodash_iteratee;
    export = iteratee;
}


declare module "lodash/keyBy" {
    const keyBy: typeof lodash_keyBy;
    export = keyBy;
}


declare module "lodash/keys" {
    const keys: typeof lodash_keys;
    export = keys;
}


declare module "lodash/keysIn" {
    const keysIn: typeof lodash_keysIn;
    export = keysIn;
}


declare module "lodash/map" {
    const map: typeof lodash_map;
    export = map;
}


declare module "lodash/mapKeys" {
    const mapKeys: typeof lodash_mapKeys;
    export = mapKeys;
}


declare module "lodash/mapValues" {
    const mapValues: typeof lodash_mapValues;
    export = mapValues;
}


declare module "lodash/matches" {
    const matches: typeof lodash_matches;
    export = matches;
}


declare module "lodash/matchesProperty" {
    const matchesProperty: typeof lodash_matchesProperty;
    export = matchesProperty;
}


declare module "lodash/memoize" {
    const memoize: typeof lodash_memoize;
    export = memoize;
}


declare module "lodash/merge" {
    const merge: typeof lodash_merge;
    export = merge;
}


declare module "lodash/mergeWith" {
    const mergeWith: typeof lodash_mergeWith;
    export = mergeWith;
}


declare module "lodash/method" {
    const method: typeof lodash_method;
    export = method;
}


declare module "lodash/methodOf" {
    const methodOf: typeof lodash_methodOf;
    export = methodOf;
}


declare module "lodash/mixin" {
    const mixin: typeof lodash_mixin;
    export = mixin;
}


declare module "lodash/negate" {
    const negate: typeof lodash_negate;
    export = negate;
}


declare module "lodash/nthArg" {
    const nthArg: typeof lodash_nthArg;
    export = nthArg;
}


declare module "lodash/omit" {
    const omit: typeof lodash_omit;
    export = omit;
}


declare module "lodash/omitBy" {
    const omitBy: typeof lodash_omitBy;
    export = omitBy;
}


declare module "lodash/once" {
    const once: typeof lodash_once;
    export = once;
}


declare module "lodash/orderBy" {
    const orderBy: typeof lodash_orderBy;
    export = orderBy;
}


declare module "lodash/over" {
    const over: typeof lodash_over;
    export = over;
}


declare module "lodash/overArgs" {
    const overArgs: typeof lodash_overArgs;
    export = overArgs;
}


declare module "lodash/overEvery" {
    const overEvery: typeof lodash_overEvery;
    export = overEvery;
}


declare module "lodash/overSome" {
    const overSome: typeof lodash_overSome;
    export = overSome;
}


declare module "lodash/partial" {
    const partial: typeof lodash_partial;
    export = partial;
}


declare module "lodash/partialRight" {
    const partialRight: typeof lodash_partialRight;
    export = partialRight;
}


declare module "lodash/partition" {
    const partition: typeof lodash_partition;
    export = partition;
}


declare module "lodash/pick" {
    const pick: typeof lodash_pick;
    export = pick;
}


declare module "lodash/pickBy" {
    const pickBy: typeof lodash_pickBy;
    export = pickBy;
}


declare module "lodash/property" {
    const property: typeof lodash_property;
    export = property;
}


declare module "lodash/propertyOf" {
    const propertyOf: typeof lodash_propertyOf;
    export = propertyOf;
}


declare module "lodash/pull" {
    const pull: typeof lodash_pull;
    export = pull;
}


declare module "lodash/pullAll" {
    const pullAll: typeof lodash_pullAll;
    export = pullAll;
}


declare module "lodash/pullAllBy" {
    const pullAllBy: typeof lodash_pullAllBy;
    export = pullAllBy;
}

/**
 * uncoment it if definition exists
 */
/*
 declare module "lodash/pullAllWith" {
 const pullAllWith: typeof lodash_pullAllWith;
 export = pullAllWith;
 }
 */

declare module "lodash/pullAt" {
    const pullAt: typeof lodash_pullAt;
    export = pullAt;
}


declare module "lodash/range" {
    const range: typeof lodash_range;
    export = range;
}


declare module "lodash/rangeRight" {
    const rangeRight: typeof lodash_rangeRight;
    export = rangeRight;
}


declare module "lodash/rearg" {
    const rearg: typeof lodash_rearg;
    export = rearg;
}


declare module "lodash/reject" {
    const reject: typeof lodash_reject;
    export = reject;
}


declare module "lodash/remove" {
    const remove: typeof lodash_remove;
    export = remove;
}


declare module "lodash/rest" {
    const rest: typeof lodash_rest;
    export = rest;
}


declare module "lodash/reverse" {
    const reverse: typeof lodash_reverse;
    export = reverse;
}


declare module "lodash/sampleSize" {
    const sampleSize: typeof lodash_sampleSize;
    export = sampleSize;
}


declare module "lodash/set" {
    const set: typeof lodash_set;
    export = set;
}


declare module "lodash/setWith" {
    const setWith: typeof lodash_setWith;
    export = setWith;
}


declare module "lodash/shuffle" {
    const shuffle: typeof lodash_shuffle;
    export = shuffle;
}


declare module "lodash/slice" {
    const slice: typeof lodash_slice;
    export = slice;
}


declare module "lodash/sortBy" {
    const sortBy: typeof lodash_sortBy;
    export = sortBy;
}


declare module "lodash/sortedUniq" {
    const sortedUniq: typeof lodash_sortedUniq;
    export = sortedUniq;
}


declare module "lodash/sortedUniqBy" {
    const sortedUniqBy: typeof lodash_sortedUniqBy;
    export = sortedUniqBy;
}


declare module "lodash/split" {
    const split: typeof lodash_split;
    export = split;
}


declare module "lodash/spread" {
    const spread: typeof lodash_spread;
    export = spread;
}


declare module "lodash/tail" {
    const tail: typeof lodash_tail;
    export = tail;
}


declare module "lodash/take" {
    const take: typeof lodash_take;
    export = take;
}


declare module "lodash/takeRight" {
    const takeRight: typeof lodash_takeRight;
    export = takeRight;
}


declare module "lodash/takeRightWhile" {
    const takeRightWhile: typeof lodash_takeRightWhile;
    export = takeRightWhile;
}


declare module "lodash/takeWhile" {
    const takeWhile: typeof lodash_takeWhile;
    export = takeWhile;
}


declare module "lodash/tap" {
    const tap: typeof lodash_tap;
    export = tap;
}


declare module "lodash/throttle" {
    const throttle: typeof lodash_throttle;
    export = throttle;
}


declare module "lodash/thru" {
    const thru: typeof lodash_thru;
    export = thru;
}


declare module "lodash/toArray" {
    const toArray: typeof lodash_toArray;
    export = toArray;
}


declare module "lodash/toPairs" {
    const toPairs: typeof lodash_toPairs;
    export = toPairs;
}


declare module "lodash/toPairsIn" {
    const toPairsIn: typeof lodash_toPairsIn;
    export = toPairsIn;
}


declare module "lodash/toPath" {
    const toPath: typeof lodash_toPath;
    export = toPath;
}


declare module "lodash/toPlainObject" {
    const toPlainObject: typeof lodash_toPlainObject;
    export = toPlainObject;
}


declare module "lodash/transform" {
    const transform: typeof lodash_transform;
    export = transform;
}


declare module "lodash/unary" {
    const unary: typeof lodash_unary;
    export = unary;
}


declare module "lodash/union" {
    const union: typeof lodash_union;
    export = union;
}


declare module "lodash/unionBy" {
    const unionBy: typeof lodash_unionBy;
    export = unionBy;
}


declare module "lodash/unionWith" {
    const unionWith: typeof lodash_unionWith;
    export = unionWith;
}


declare module "lodash/uniq" {
    const uniq: typeof lodash_uniq;
    export = uniq;
}


declare module "lodash/uniqBy" {
    const uniqBy: typeof lodash_uniqBy;
    export = uniqBy;
}


declare module "lodash/uniqWith" {
    const uniqWith: typeof lodash_uniqWith;
    export = uniqWith;
}


declare module "lodash/unset" {
    const unset: typeof lodash_unset;
    export = unset;
}


declare module "lodash/unzip" {
    const unzip: typeof lodash_unzip;
    export = unzip;
}


declare module "lodash/unzipWith" {
    const unzipWith: typeof lodash_unzipWith;
    export = unzipWith;
}


declare module "lodash/update" {
    const update: typeof lodash_update;
    export = update;
}

/**
 * uncoment it if definition exists
 */
/*
 declare module "lodash/updateWith" {
 const updateWith: typeof lodash_updateWith;
 export = updateWith;
 }
 */

declare module "lodash/values" {
    const values: typeof lodash_values;
    export = values;
}


declare module "lodash/valuesIn" {
    const valuesIn: typeof lodash_valuesIn;
    export = valuesIn;
}


declare module "lodash/without" {
    const without: typeof lodash_without;
    export = without;
}


declare module "lodash/words" {
    const words: typeof lodash_words;
    export = words;
}


declare module "lodash/wrap" {
    const wrap: typeof lodash_wrap;
    export = wrap;
}


declare module "lodash/xor" {
    const xor: typeof lodash_xor;
    export = xor;
}


declare module "lodash/xorBy" {
    const xorBy: typeof lodash_xorBy;
    export = xorBy;
}


declare module "lodash/xorWith" {
    const xorWith: typeof lodash_xorWith;
    export = xorWith;
}


declare module "lodash/zip" {
    const zip: typeof lodash_zip;
    export = zip;
}


declare module "lodash/zipObject" {
    const zipObject: typeof lodash_zipObject;
    export = zipObject;
}

/**
 * uncoment it if definition exists
 */
/*
 declare module "lodash/zipObjectDeep" {
 const zipObjectDeep: typeof lodash_zipObjectDeep;
 export = zipObjectDeep;
 }
 */


declare module "lodash/zipWith" {
    const zipWith: typeof lodash_zipWith;
    export = zipWith;
}

/**
 * uncoment it if definition exists
 */
/*
 declare module "lodash/entries" {
 const entries: typeof lodash_entries;
 export = entries;
 }
 */
/**
 * uncoment it if definition exists
 */
/*
 declare module "lodash/entriesIn" {
 const entriesIn: typeof lodash_entriesIn;
 export = entriesIn;
 }
 */


declare module "lodash/extend" {
    const extend: typeof lodash_extend;
    export = extend;
}


declare module "lodash/extendWith" {
    const extendWith: typeof lodash_extendWith;
    export = extendWith;
}


declare module "lodash/add" {
    const add: typeof lodash_add;
    export = add;
}


declare module "lodash/attempt" {
    const attempt: typeof lodash_attempt;
    export = attempt;
}


declare module "lodash/camelCase" {
    const camelCase: typeof lodash_camelCase;
    export = camelCase;
}


declare module "lodash/capitalize" {
    const capitalize: typeof lodash_capitalize;
    export = capitalize;
}


declare module "lodash/ceil" {
    const ceil: typeof lodash_ceil;
    export = ceil;
}


declare module "lodash/clamp" {
    const clamp: typeof lodash_clamp;
    export = clamp;
}


declare module "lodash/clone" {
    const clone: typeof lodash_clone;
    export = clone;
}


declare module "lodash/cloneDeep" {
    const cloneDeep: typeof lodash_cloneDeep;
    export = cloneDeep;
}


declare module "lodash/cloneDeepWith" {
    const cloneDeepWith: typeof lodash_cloneDeepWith;
    export = cloneDeepWith;
}


declare module "lodash/cloneWith" {
    const cloneWith: typeof lodash_cloneWith;
    export = cloneWith;
}


declare module "lodash/deburr" {
    const deburr: typeof lodash_deburr;
    export = deburr;
}

/**
 * uncoment it if definition exists
 */
/*
 declare module "lodash/divide" {
 const divide: typeof lodash_divide;
 export = divide;
 }
 */

declare module "lodash/endsWith" {
    const endsWith: typeof lodash_endsWith;
    export = endsWith;
}


declare module "lodash/eq" {
    const eq: typeof lodash_eq;
    export = eq;
}


declare module "lodash/escape" {
    const escape: typeof lodash_escape;
    export = escape;
}


declare module "lodash/escapeRegExp" {
    const escapeRegExp: typeof lodash_escapeRegExp;
    export = escapeRegExp;
}


declare module "lodash/every" {
    const every: typeof lodash_every;
    export = every;
}


declare module "lodash/find" {
    const find: typeof lodash_find;
    export = find;
}


declare module "lodash/findIndex" {
    const findIndex: typeof lodash_findIndex;
    export = findIndex;
}


declare module "lodash/findKey" {
    const findKey: typeof lodash_findKey;
    export = findKey;
}


declare module "lodash/findLast" {
    const findLast: typeof lodash_findLast;
    export = findLast;
}


declare module "lodash/findLastIndex" {
    const findLastIndex: typeof lodash_findLastIndex;
    export = findLastIndex;
}


declare module "lodash/findLastKey" {
    const findLastKey: typeof lodash_findLastKey;
    export = findLastKey;
}


declare module "lodash/floor" {
    const floor: typeof lodash_floor;
    export = floor;
}


declare module "lodash/forEach" {
    const forEach: typeof lodash_forEach;
    export = forEach;
}


declare module "lodash/forEachRight" {
    const forEachRight: typeof lodash_forEachRight;
    export = forEachRight;
}


declare module "lodash/forIn" {
    const forIn: typeof lodash_forIn;
    export = forIn;
}


declare module "lodash/forInRight" {
    const forInRight: typeof lodash_forInRight;
    export = forInRight;
}


declare module "lodash/forOwn" {
    const forOwn: typeof lodash_forOwn;
    export = forOwn;
}


declare module "lodash/forOwnRight" {
    const forOwnRight: typeof lodash_forOwnRight;
    export = forOwnRight;
}


declare module "lodash/get" {
    const get: typeof lodash_get;
    export = get;
}


declare module "lodash/gt" {
    const gt: typeof lodash_gt;
    export = gt;
}


declare module "lodash/gte" {
    const gte: typeof lodash_gte;
    export = gte;
}


declare module "lodash/has" {
    const has: typeof lodash_has;
    export = has;
}


declare module "lodash/hasIn" {
    const hasIn: typeof lodash_hasIn;
    export = hasIn;
}


declare module "lodash/head" {
    const head: typeof lodash_head;
    export = head;
}


declare module "lodash/identity" {
    const identity: typeof lodash_identity;
    export = identity;
}


declare module "lodash/includes" {
    const includes: typeof lodash_includes;
    export = includes;
}


declare module "lodash/indexOf" {
    const indexOf: typeof lodash_indexOf;
    export = indexOf;
}


declare module "lodash/inRange" {
    const inRange: typeof lodash_inRange;
    export = inRange;
}


declare module "lodash/invoke" {
    const invoke: typeof lodash_invoke;
    export = invoke;
}


declare module "lodash/isArguments" {
    const isArguments: typeof lodash_isArguments;
    export = isArguments;
}


declare module "lodash/isArray" {
    const isArray: typeof lodash_isArray;
    export = isArray;
}


declare module "lodash/isArrayBuffer" {
    const isArrayBuffer: typeof lodash_isArrayBuffer;
    export = isArrayBuffer;
}


declare module "lodash/isArrayLike" {
    const isArrayLike: typeof lodash_isArrayLike;
    export = isArrayLike;
}


declare module "lodash/isArrayLikeObject" {
    const isArrayLikeObject: typeof lodash_isArrayLikeObject;
    export = isArrayLikeObject;
}


declare module "lodash/isBoolean" {
    const isBoolean: typeof lodash_isBoolean;
    export = isBoolean;
}


declare module "lodash/isBuffer" {
    const isBuffer: typeof lodash_isBuffer;
    export = isBuffer;
}


declare module "lodash/isDate" {
    const isDate: typeof lodash_isDate;
    export = isDate;
}


declare module "lodash/isElement" {
    const isElement: typeof lodash_isElement;
    export = isElement;
}


declare module "lodash/isEmpty" {
    const isEmpty: typeof lodash_isEmpty;
    export = isEmpty;
}


declare module "lodash/isEqual" {
    const isEqual: typeof lodash_isEqual;
    export = isEqual;
}


declare module "lodash/isEqualWith" {
    const isEqualWith: typeof lodash_isEqualWith;
    export = isEqualWith;
}


declare module "lodash/isError" {
    const isError: typeof lodash_isError;
    export = isError;
}


declare module "lodash/isFinite" {
    const isFinite: typeof lodash_isFinite;
    export = isFinite;
}


declare module "lodash/isFunction" {
    const isFunction: typeof lodash_isFunction;
    export = isFunction;
}


declare module "lodash/isInteger" {
    const isInteger: typeof lodash_isInteger;
    export = isInteger;
}


declare module "lodash/isLength" {
    const isLength: typeof lodash_isLength;
    export = isLength;
}


declare module "lodash/isMap" {
    const isMap: typeof lodash_isMap;
    export = isMap;
}


declare module "lodash/isMatch" {
    const isMatch: typeof lodash_isMatch;
    export = isMatch;
}


declare module "lodash/isMatchWith" {
    const isMatchWith: typeof lodash_isMatchWith;
    export = isMatchWith;
}


declare module "lodash/isNaN" {
    const isNaN: typeof lodash_isNaN;
    export = isNaN;
}


declare module "lodash/isNative" {
    const isNative: typeof lodash_isNative;
    export = isNative;
}


declare module "lodash/isNil" {
    const isNil: typeof lodash_isNil;
    export = isNil;
}


declare module "lodash/isNull" {
    const isNull: typeof lodash_isNull;
    export = isNull;
}


declare module "lodash/isNumber" {
    const isNumber: typeof lodash_isNumber;
    export = isNumber;
}


declare module "lodash/isObject" {
    const isObject: typeof lodash_isObject;
    export = isObject;
}


declare module "lodash/isObjectLike" {
    const isObjectLike: typeof lodash_isObjectLike;
    export = isObjectLike;
}


declare module "lodash/isPlainObject" {
    const isPlainObject: typeof lodash_isPlainObject;
    export = isPlainObject;
}


declare module "lodash/isRegExp" {
    const isRegExp: typeof lodash_isRegExp;
    export = isRegExp;
}


declare module "lodash/isSafeInteger" {
    const isSafeInteger: typeof lodash_isSafeInteger;
    export = isSafeInteger;
}


declare module "lodash/isSet" {
    const isSet: typeof lodash_isSet;
    export = isSet;
}


declare module "lodash/isString" {
    const isString: typeof lodash_isString;
    export = isString;
}


declare module "lodash/isSymbol" {
    const isSymbol: typeof lodash_isSymbol;
    export = isSymbol;
}


declare module "lodash/isTypedArray" {
    const isTypedArray: typeof lodash_isTypedArray;
    export = isTypedArray;
}


declare module "lodash/isUndefined" {
    const isUndefined: typeof lodash_isUndefined;
    export = isUndefined;
}


declare module "lodash/isWeakMap" {
    const isWeakMap: typeof lodash_isWeakMap;
    export = isWeakMap;
}


declare module "lodash/isWeakSet" {
    const isWeakSet: typeof lodash_isWeakSet;
    export = isWeakSet;
}


declare module "lodash/join" {
    const join: typeof lodash_join;
    export = join;
}


declare module "lodash/kebabCase" {
    const kebabCase: typeof lodash_kebabCase;
    export = kebabCase;
}


declare module "lodash/last" {
    const last: typeof lodash_last;
    export = last;
}


declare module "lodash/lastIndexOf" {
    const lastIndexOf: typeof lodash_lastIndexOf;
    export = lastIndexOf;
}


declare module "lodash/lowerCase" {
    const lowerCase: typeof lodash_lowerCase;
    export = lowerCase;
}


declare module "lodash/lowerFirst" {
    const lowerFirst: typeof lodash_lowerFirst;
    export = lowerFirst;
}


declare module "lodash/lt" {
    const lt: typeof lodash_lt;
    export = lt;
}


declare module "lodash/lte" {
    const lte: typeof lodash_lte;
    export = lte;
}


declare module "lodash/max" {
    const max: typeof lodash_max;
    export = max;
}


declare module "lodash/maxBy" {
    const maxBy: typeof lodash_maxBy;
    export = maxBy;
}


declare module "lodash/mean" {
    const mean: typeof lodash_mean;
    export = mean;
}

/**
 * uncoment it if definition exists
 */
/*
 declare module "lodash/meanBy" {
 const meanBy: typeof lodash_meanBy;
 export = meanBy;
 }
 */

declare module "lodash/min" {
    const min: typeof lodash_min;
    export = min;
}


declare module "lodash/minBy" {
    const minBy: typeof lodash_minBy;
    export = minBy;
}

/**
 * uncoment it if definition exists
 */
/*
 declare module "lodash/multiply" {
 const multiply: typeof lodash_multiply;
 export = multiply;
 }
 */

/**
 * uncoment it if definition exists
 */
/*
 declare module "lodash/nth" {
 const nth: typeof lodash_nth;
 export = nth;
 }
 */

declare module "lodash/noConflict" {
    const noConflict: typeof lodash_noConflict;
    export = noConflict;
}


declare module "lodash/noop" {
    const noop: typeof lodash_noop;
    export = noop;
}


declare module "lodash/now" {
    const now: typeof lodash_now;
    export = now;
}


declare module "lodash/pad" {
    const pad: typeof lodash_pad;
    export = pad;
}


declare module "lodash/padEnd" {
    const padEnd: typeof lodash_padEnd;
    export = padEnd;
}


declare module "lodash/padStart" {
    const padStart: typeof lodash_padStart;
    export = padStart;
}


declare module "lodash/parseInt" {
    const parseInt: typeof lodash_parseInt;
    export = parseInt;
}


declare module "lodash/random" {
    const random: typeof lodash_random;
    export = random;
}


declare module "lodash/reduce" {
    const reduce: typeof lodash_reduce;
    export = reduce;
}


declare module "lodash/reduceRight" {
    const reduceRight: typeof lodash_reduceRight;
    export = reduceRight;
}


declare module "lodash/repeat" {
    const repeat: typeof lodash_repeat;
    export = repeat;
}


declare module "lodash/replace" {
    const replace: typeof lodash_replace;
    export = replace;
}


declare module "lodash/result" {
    const result: typeof lodash_result;
    export = result;
}


declare module "lodash/round" {
    const round: typeof lodash_round;
    export = round;
}


declare module "lodash/runInContext" {
    const runInContext: typeof lodash_runInContext;
    export = runInContext;
}


declare module "lodash/sample" {
    const sample: typeof lodash_sample;
    export = sample;
}


declare module "lodash/size" {
    const size: typeof lodash_size;
    export = size;
}


declare module "lodash/snakeCase" {
    const snakeCase: typeof lodash_snakeCase;
    export = snakeCase;
}


declare module "lodash/some" {
    const some: typeof lodash_some;
    export = some;
}


declare module "lodash/sortedIndex" {
    const sortedIndex: typeof lodash_sortedIndex;
    export = sortedIndex;
}


declare module "lodash/sortedIndexBy" {
    const sortedIndexBy: typeof lodash_sortedIndexBy;
    export = sortedIndexBy;
}


declare module "lodash/sortedIndexOf" {
    const sortedIndexOf: typeof lodash_sortedIndexOf;
    export = sortedIndexOf;
}


declare module "lodash/sortedLastIndex" {
    const sortedLastIndex: typeof lodash_sortedLastIndex;
    export = sortedLastIndex;
}


declare module "lodash/sortedLastIndexBy" {
    const sortedLastIndexBy: typeof lodash_sortedLastIndexBy;
    export = sortedLastIndexBy;
}


declare module "lodash/sortedLastIndexOf" {
    const sortedLastIndexOf: typeof lodash_sortedLastIndexOf;
    export = sortedLastIndexOf;
}


declare module "lodash/startCase" {
    const startCase: typeof lodash_startCase;
    export = startCase;
}


declare module "lodash/startsWith" {
    const startsWith: typeof lodash_startsWith;
    export = startsWith;
}


declare module "lodash/subtract" {
    const subtract: typeof lodash_subtract;
    export = subtract;
}


declare module "lodash/sum" {
    const sum: typeof lodash_sum;
    export = sum;
}


declare module "lodash/sumBy" {
    const sumBy: typeof lodash_sumBy;
    export = sumBy;
}


declare module "lodash/template" {
    const template: typeof lodash_template;
    export = template;
}


declare module "lodash/times" {
    const times: typeof lodash_times;
    export = times;
}


declare module "lodash/toInteger" {
    const toInteger: typeof lodash_toInteger;
    export = toInteger;
}


declare module "lodash/toLength" {
    const toLength: typeof lodash_toLength;
    export = toLength;
}


declare module "lodash/toLower" {
    const toLower: typeof lodash_toLower;
    export = toLower;
}


declare module "lodash/toNumber" {
    const toNumber: typeof lodash_toNumber;
    export = toNumber;
}


declare module "lodash/toSafeInteger" {
    const toSafeInteger: typeof lodash_toSafeInteger;
    export = toSafeInteger;
}


declare module "lodash/toString" {
    const toString: typeof lodash_toString;
    export = toString;
}


declare module "lodash/toUpper" {
    const toUpper: typeof lodash_toUpper;
    export = toUpper;
}


declare module "lodash/trim" {
    const trim: typeof lodash_trim;
    export = trim;
}


declare module "lodash/trimEnd" {
    const trimEnd: typeof lodash_trimEnd;
    export = trimEnd;
}


declare module "lodash/trimStart" {
    const trimStart: typeof lodash_trimStart;
    export = trimStart;
}


declare module "lodash/truncate" {
    const truncate: typeof lodash_truncate;
    export = truncate;
}


declare module "lodash/unescape" {
    const unescape: typeof lodash_unescape;
    export = unescape;
}


declare module "lodash/uniqueId" {
    const uniqueId: typeof lodash_uniqueId;
    export = uniqueId;
}


declare module "lodash/upperCase" {
    const upperCase: typeof lodash_upperCase;
    export = upperCase;
}


declare module "lodash/upperFirst" {
    const upperFirst: typeof lodash_upperFirst;
    export = upperFirst;
}


declare module "lodash/each" {
    const each: typeof lodash_each;
    export = each;
}


declare module "lodash/eachRight" {
    const eachRight: typeof lodash_eachRight;
    export = eachRight;
}


declare module "lodash/first" {
    const first: typeof lodash_first;
    export = first;
}

declare module "lodash/fp" {
    export = _;
}

declare module "lodash" {
    export = _;
}

// Backward compatibility with --target es5
interface Set<T> {}
interface Map<K, V> {}
interface WeakSet<T> {}
interface WeakMap<K, V> {}
# Options

GraalVM JavaScript can be configured with several options provided when starting the engine.

### GraalVM JavaScript Launcher Options

These options are to control the behaviour of the `js` launcher.
* `-e, --eval CODE `: evaluate the passed in JavaScript source code, then exit the engine.
```
js -e 'print(1+2);'
```
* `-f, --file FILE`: load and executed the provided script file. Note that the `-f` flag is optional and can be omitted in most cases, as any additional argument to `js` will be interpreted as file anyway.
```
js -f myfile.js
```
* `--version`: print the version information of GraalVM JavaScript and then exit.
* `--strict`: execute the engine in JavaScript's _strict mode_.

###  GraalVM JavaScript Engine Options

These options are to configure the behaviour of the GraalVM JavaScript engine.
Depending on how the engine is started, the options can be passed to the launcher or programmatically.
Note that most of these options are experimental and require an `--experimental-options` flag.

To the launcher, the options are passed with `--js.<option-name>=<value>`:
```
js --js.ecmascript-version=6
```

The following options are currently available:
   * `--js.annex-b`: enable ECMAScript Annex B web compatibility features. Boolean value, default is `true`.
   * `--js.array-sort-inherited`: define whether `Array.protoype.sort` should sort inherited keys (implementation-defined behavior). Boolean value, default is `true`.
   * `--js.atomics`: enable *ES2017 Atomics*. Boolean value, default is `true`.
   * `--js.ecmascript-version`: emulate a specific ECMAScript version. Integer value (`5`-`9`), default is the latest version.
   * `--js.intl-402`: enable ECMAScript Internationalization API. Boolean value, default is `false`.
   * `--js.regexp-static-result`: provide static `RegExp` properties containing results of the last successful match, e.g.: `RegExp.$1` (legacy). Boolean value, default is `true`.
   * `--js.shared-array-buffer`: enable *ES2017 SharedArrayBuffer*. Boolean value, default is `false`.
   * `--js.strict`: enable strict mode for all scripts. Boolean value, default is `false`.
   * `--js.timezone`: set the local time zone. String value, default is the system default.
   * `--js.v8-compat`: provide better compatibility with Google's V8 engine. Boolean value, default is `false`.

When started from Java via GraalVM's Polyglot feature, the options are passed programmatically to the `Context` object:

```java
Context context = Context.newBuilder("js")
                         .option("js.ecmascript-version", "6")
                         .build();
context.eval("js", "42");
```
See the [Polyglot Programming](https://www.graalvm.org/docs/reference-manual/polyglot-programming/) reference for information on how to set options programmatically.

### Stable and Experimental Options

The available options are distinguished in stable and experimental options.
If an experimental option is used, an extra flag has to be provided upfront.

In the native launchers (`js`, `node`), `--experimental-options` has to be passed before all experimental options.
When using a `Context`, the option `allowExperimentalOptions(true)` has to be called on the `Context.Builder`.
See [ScriptEngine.md](ScriptEngine.md) on how to use experimental options with a `ScriptEngine`.

### ECMAScript Version

It provides compatibility to a specific version of the ECMAScript specification.
It expects an integer value, where both the counting version numbers (`5` to `11`) and the publication years (starting from `2015`) are supported.
The default is the latest finalized version of the specification, currently the [`ECMAScript 2020 specification`](http://www.ecma-international.org/ecma-262/11.0/index.html).
Starting with GraalVM 20.1.0, the default will be moved to the draft ECMAScript 2020 specification.
Graal.js implements some features of the future draft specification and of open proposals, if you explicitly select that version and/or enable specific experimental flags.
For production settings, it is recommended to set the `ecmascript-version` to an existing, finalized version of the specification.

Available versions are:
* `5` for ECMAScript 5.x
* `6` or `2015` for ECMAScript 2015
* `7` or `2016` for ECMAScript 2016
* `8` or `2017` for ECMAScript 2017
* `9` or `2018` for ECMAScript 2018
* `10` or `2019` for ECMAScript 2019
* `11` or `2020` for ECMAScript 2020 (default, latest finalized version of the specification)
* `12` or `2021` for ECMAScript 2021 (currently in draft stage, some proposals are already supported by GraalVM JavaScript)

### intl-402

It enables ECMAScript's [Internationalization API](https://tc39.github.io/ecma402/).
It expects a Boolean value, the default is `false`.

### Strict Mode

It enables JavaScript's strict mode for all scripts.
It expects a boolean value, default is `false`.

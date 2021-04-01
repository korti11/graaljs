/*
 * Copyright (c) 2018, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

var assert = require('assert');
var child_process = require('child_process');
var spawnSync = child_process.spawnSync;

function checkTheAnswerToLifeTheUniverseAndEverything(answer) {
    assert.strictEqual(answer.stderr.toString(), '');
    assert.strictEqual(answer.stdout.toString(), '42\n');
    assert.strictEqual(answer.status, 0);
}

describe('Spawn', function () {
    this.timeout(20000);
    it('should spawn a child node process when env. variables are cleared', function () {
        var result = spawnSync(process.execPath, ['-p', '6*7'], {env: {}});
        checkTheAnswerToLifeTheUniverseAndEverything(result);
    });
    it('should accept max_old_space_size option', function () {
        var result = spawnSync(process.execPath, ['--max_old_space_size=1024', '-p', '6*7']);
        checkTheAnswerToLifeTheUniverseAndEverything(result);
    });
    it('should accept --stack-trace-limit option', function () {
        var result = spawnSync(process.execPath, ['--stack-trace-limit=42', '-p', 'Error.stackTraceLimit']);
        checkTheAnswerToLifeTheUniverseAndEverything(result);
    });
    it('should accept --stack-trace-limit option in NODE_OPTIONS', function () {
        var result = spawnSync(process.execPath, ['-p', 'Error.stackTraceLimit'], {env: { NODE_OPTIONS: '--stack-trace-limit=42' }});
        checkTheAnswerToLifeTheUniverseAndEverything(result);
    });
    it('should survive duplicates in envPairs', function (done) {
        // Copy the current content of process.env
        var envPairs = [];
        for (var key in process.env) {
            envPairs.push(key + '=' + process.env[key]);
        }

        // Add duplicates
        envPairs.push('MY_VAR=1');
        envPairs.push('MY_VAR=2');

        // Spawn a new process with these envPairs and try to list keys of process.env
        var myProcess = new child_process.ChildProcess();
        myProcess.on('exit', function (code) {
            assert.strictEqual(code, 0);
            done();
        });
        myProcess.spawn({
            file: process.execPath,
            args: ['node', '-e', 'Object.keys(process.env)'],
            stdio: 'inherit',
            envPairs: envPairs
        });
    });
    ['--help', '-h', '--v8-options'].forEach(function (option) {
        it('should print help when ' + option + ' option is used', function () {
            var result = spawnSync(process.execPath, [option]);
            assert.strictEqual(result.status, 0);
            assert.match(result.stdout.toString(), /Options:/);
        });
    });
    if (typeof java === 'object') {
        it('should finish gracefully when a native method is called from a wrong thread', function () {
            var code = `var vm = require('vm');
                        var sandbox = {};
                        vm.runInNewContext("var f = function() { console.log('crash'); }; var t = new java.lang.Thread(f);", sandbox);
                        var t = sandbox.t;
                        t.start();
                        t.join();`;
            code = code.replace(/\n\s*/g, ' ');
            var result = spawnSync(process.execPath, ['-e', code]);
            assert.ok(result.stderr.toString().indexOf('thread') !== -1);
            assert.strictEqual(result.stdout.toString(), '');
            assert.strictEqual(result.status, 1);
        });
    }
});

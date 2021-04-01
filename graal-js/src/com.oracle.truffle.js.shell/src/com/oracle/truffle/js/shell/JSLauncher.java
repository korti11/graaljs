/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.js.shell;

import static com.oracle.truffle.js.shell.JSLauncher.PreprocessResult.Consumed;
import static com.oracle.truffle.js.shell.JSLauncher.PreprocessResult.MissingValue;
import static com.oracle.truffle.js.shell.JSLauncher.PreprocessResult.Unhandled;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.graalvm.launcher.AbstractLanguageLauncher;
import org.graalvm.options.OptionCategory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public class JSLauncher extends AbstractLanguageLauncher {
    static final String MODULE_MIME_TYPE = "application/javascript+module";
    private static final String PROMPT = "> ";

    public static void main(String[] args) {
        new JSLauncher().launch(args);
    }

    boolean printResult = false;
    boolean fuzzilliREPRL = false;
    String[] programArgs;
    final List<UnparsedSource> unparsedSources = new LinkedList<>();
    private VersionAction versionAction = VersionAction.None;

    @Override
    protected void launch(Context.Builder contextBuilder) {
        if (fuzzilliREPRL) {
            System.exit(JSFuzzilliRunner.runFuzzilliREPRL(contextBuilder));
        } else {
            System.exit(executeScripts(contextBuilder));
        }
    }

    @Override
    protected String getLanguageId() {
        return "js";
    }

    protected void preEval(@SuppressWarnings("unused") Context context) {
    }

    @Override
    protected List<String> preprocessArguments(List<String> arguments, Map<String, String> polyglotOptions) {
        final List<String> unrecognizedOptions = new ArrayList<>();

        ListIterator<String> iterator = arguments.listIterator();
        while (iterator.hasNext()) {
            String arg = iterator.next();
            if (arg.length() >= 2 && arg.startsWith("-")) {
                if (arg.equals("--")) {
                    break;
                }
                String flag;
                if (arg.startsWith("--")) {
                    flag = arg.substring(2);
                } else {
                    flag = arg.substring(1);
                    if (flag.length() == 1) {
                        String longFlag = expandShortFlag(flag.charAt(0));
                        if (longFlag != null) {
                            flag = longFlag;
                        }
                    }
                }

                switch (preprocessArgument(flag)) {
                    case Consumed:
                        continue;
                    case MissingValue:
                        throw new RuntimeException("Should not reach here");
                }

                String value;
                int equalsIndex = flag.indexOf('=');
                if (equalsIndex > 0) {
                    value = flag.substring(equalsIndex + 1);
                    flag = flag.substring(0, equalsIndex);
                } else if (iterator.hasNext()) {
                    value = iterator.next();
                } else {
                    value = null;
                }

                switch ((preprocessArgument(flag, value))) {
                    case Consumed:
                        continue;
                    case MissingValue:
                        throw abort("Missing argument for " + arg);
                }

                unrecognizedOptions.add(arg);
                if (equalsIndex < 0 && value != null) {
                    iterator.previous();
                }
            } else {
                addFile(arg);
            }
        }
        List<String> programArgsList = arguments.subList(iterator.nextIndex(), arguments.size());
        programArgs = programArgsList.toArray(new String[programArgsList.size()]);
        return unrecognizedOptions;
    }

    public enum PreprocessResult {
        Consumed,
        Unhandled,
        MissingValue
    }

    protected PreprocessResult preprocessArgument(String argument) {
        switch (argument) {
            case "printResult":
            case "print-result":
                printResult = true;
                return Consumed;
            case "show-version":
                versionAction = VersionAction.PrintAndContinue;
                return Consumed;
            case "version":
                versionAction = VersionAction.PrintAndExit;
                return Consumed;
            case "fuzzilli-reprl":
                fuzzilliREPRL = true;
                return Consumed;
        }
        return Unhandled;
    }

    protected PreprocessResult preprocessArgument(String argument, String value) {
        switch (argument) {
            case "eval":
                if (value == null) {
                    return MissingValue;
                }
                addEval(value);
                return Consumed;
            case "file":
                if (value == null) {
                    return MissingValue;
                }
                addFile(value);
                return Consumed;
            case "module":
                if (value == null) {
                    return MissingValue;
                }
                addModule(value);
                return Consumed;
            case "strict-file":
                if (value == null) {
                    return MissingValue;
                }
                addStrictFile(value);
                return Consumed;
        }
        return Unhandled;
    }

    protected String expandShortFlag(char f) {
        switch (f) {
            case 'e':
                return "eval";
            case 'f':
                // some other engines use a "-f filename" syntax.
                return "file";
        }
        return null;
    }

    boolean hasSources() {
        return unparsedSources.size() > 0;
    }

    Source[] parseSources() {
        Source[] sources = new Source[unparsedSources.size()];
        int i = 0;
        for (UnparsedSource unparsedSource : unparsedSources) {
            try {
                sources[i++] = unparsedSource.parse();
            } catch (IOException e) {
                System.err.println(String.format("Error: Error loading file %s. %s", unparsedSource.src, e.getMessage()));
                return new Source[0];
            }
        }
        return sources;
    }

    void addFile(String file) {
        unparsedSources.add(new UnparsedSource(file, SourceType.FILE));
    }

    void addEval(String str) {
        unparsedSources.add(new UnparsedSource(str, SourceType.EVAL));
    }

    void addModule(String file) {
        unparsedSources.add(new UnparsedSource(file, SourceType.MODULE));
    }

    void addStrictFile(String file) {
        unparsedSources.add(new UnparsedSource(file, SourceType.STRICT));
    }

    @Override
    protected void validateArguments(Map<String, String> polyglotOptions) {
        if (!hasSources() && printResult) {
            throw abort("Error: cannot print the return value when no FILE is passed.", 6);
        }
    }

    @Override
    protected void printHelp(OptionCategory maxCategory) {
        // @formatter:off
        System.out.println();
        System.out.println("Usage: js [OPTION]... [FILE]...");
        System.out.println("Run JavaScript FILEs on the Graal.js engine. Run an interactive JavaScript shell if no FILE nor --eval is specified.\n");
        System.out.println("Arguments that are mandatory for long options are also mandatory for short options.\n");
        System.out.println("Basic Options:");
        printOption("-e, --eval CODE",      "evaluate the code");
        printOption("-f, --file FILE",      "load script file");
        printOption("--module FILE",        "load module file");
        printOption("--syntax-extensions",  "enable non-spec syntax extensions");
        printOption("--print-result",       "print the return value of each FILE");
        printOption("--scripting",          "enable scripting features (Nashorn compatibility option)");
        printOption("--strict",             "run in strict mode");
        printOption("--version",            "print the version and exit");
        printOption("--show-version",       "print the version and continue");
        // @formatter:on
    }

    @Override
    protected void collectArguments(Set<String> args) {
        args.addAll(Arrays.asList(
                        "-e", "--eval",
                        "-f", "--file",
                        "--syntax-extensions",
                        "--print-result",
                        "--version",
                        "--show-version",
                        "--scripting",
                        "--strict"));
    }

    protected static void printOption(String option, String description) {
        String opt;
        if (option.length() >= 22) {
            System.out.println(String.format("%s%s", "  ", option));
            opt = "";
        } else {
            opt = option;
        }
        System.out.println(String.format("  %-22s%s", opt, description));
    }

    protected int executeScripts(Context.Builder contextBuilder) {
        int status;
        contextBuilder.arguments("js", programArgs);
        contextBuilder.option("js.shell", "true");
        try (Context context = contextBuilder.build()) {
            runVersionAction(versionAction, context.getEngine());
            preEval(context);
            if (hasSources()) {
                // Every engine runs different Source objects.
                Source[] sources = parseSources();
                status = -1;
                for (Source source : sources) {
                    try {
                        Value result = context.eval(source);
                        if (printResult) {
                            System.out.println("Result: " + result.toString());
                        }
                        status = 0;
                    } catch (PolyglotException e) {
                        status = handlePolyglotException(e);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        status = 8;
                    }
                }
            } else {
                status = runREPL(context);
            }
        } catch (PolyglotException e) {
            status = handlePolyglotException(e);
        }
        System.out.flush();
        System.err.flush();
        return status;
    }

    private static int handlePolyglotException(PolyglotException e) {
        int status;
        if (e.isExit()) {
            status = e.getExitStatus();
            if (status != 0) {
                printError(e, System.err);
            }
        } else if (e.isSyntaxError()) {
            printError(e, System.err);
            status = 7;
        } else if (!e.isInternalError()) {
            printStackTraceSkipTrailingHost(e, System.err);
            status = 7;
        } else {
            e.printStackTrace();
            status = 8;
        }
        return status;
    }

    private static int runREPL(Context context) {
        ConsoleHandler console;
        try {
            console = setupConsole();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return 1;
        }

        int lineNumber = 0;

        for (;;) {
            try {
                String line = console.readLine();
                if (line == null) {
                    return 0;
                }
                if (line.equals("")) {
                    continue;
                }

                context.eval(Source.newBuilder("js", line, "<shell>:" + (++lineNumber)).interactive(true).build());
            } catch (PolyglotException e) {
                if (e.isExit()) {
                    return e.getExitStatus();
                } else if (e.isSyntaxError()) {
                    printError(e, System.err);
                } else if (!e.isInternalError()) {
                    printStackTraceSkipTrailingHost(e, System.err);
                } else {
                    e.printStackTrace();
                    return 8;
                }
            } catch (Throwable t) {
                t.printStackTrace();
                return 8;
            }
        }
    }

    private static boolean isInteractiveTerminal() {
        return System.console() != null;
    }

    private static ConsoleHandler setupConsole() throws IOException {
        if (isInteractiveTerminal()) {
            return new JLineConsoleHandler(System.in, System.out, PROMPT);
        }
        return new DefaultConsoleHandler(System.in, System.out, null);
    }

    private static void printError(Throwable e, PrintStream output) {
        String message = e.getMessage();
        if (message != null && !message.isEmpty()) {
            output.println(message);
        }
    }

    private static void printStackTraceSkipTrailingHost(PolyglotException e, PrintStream output) {
        List<PolyglotException.StackFrame> stackTrace = new ArrayList<>();
        for (PolyglotException.StackFrame s : e.getPolyglotStackTrace()) {
            stackTrace.add(s);
        }
        // remove trailing host frames
        for (ListIterator<PolyglotException.StackFrame> iterator = stackTrace.listIterator(stackTrace.size()); iterator.hasPrevious();) {
            PolyglotException.StackFrame s = iterator.previous();
            if (s.isHostFrame()) {
                iterator.remove();
            } else {
                break;
            }
        }
        output.println(e.isHostException() ? e.asHostException().toString() : e.getMessage());
        for (PolyglotException.StackFrame s : stackTrace) {
            output.println("\tat " + s);
        }
    }

    private enum SourceType {
        FILE,
        EVAL,
        MODULE,
        STRICT,
    }

    private static final class UnparsedSource {
        private final String src;
        private final SourceType type;

        private UnparsedSource(String src, SourceType type) {
            this.src = src;
            this.type = type;
        }

        private Source parse() throws IOException {
            switch (type) {
                case FILE:
                    return Source.newBuilder("js", new File(src)).build();
                case EVAL:
                    return Source.newBuilder("js", src, "<eval_script>").buildLiteral();
                case MODULE:
                    return Source.newBuilder("js", new File(src)).mimeType(MODULE_MIME_TYPE).build();
                case STRICT:
                    return Source.newBuilder("js", new File(src)).content("\"use strict\";" + new String(Files.readAllBytes(Paths.get(src)), "UTF-8")).build();
                default:
                    throw new IllegalStateException();
            }
        }
    }
}

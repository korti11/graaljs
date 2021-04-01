suite = {
  "mxversion" : "5.256.0",

  "name" : "graal-js",

  "version" : "20.3.0",
  "release" : False,
  "groupId" : "org.graalvm.js",
  "url" : "http://www.graalvm.org/",
  "developer" : {
    "name" : "Truffle and Graal developers",
    "email" : "graalvm-users@oss.oracle.com",
    "organization" : "Graal",
    "organizationUrl" : "http://www.graalvm.org/",
  },
  "scm" : {
    "url" : "https://github.com/graalvm/graaljs",
    "read" : "https://github.com/graalvm/graaljs.git",
    "write" : "git@github.com:graalvm/graaljs.git",
  },

  "imports" : {
    "suites" : [
        {
           "name" : "regex",
           "subdir" : True,
           "version" : "683d5bdb11baace6319c4c8bb7b2a363a1a4d1a8",
           "urls" : [
                {"url" : "https://github.com/oracle/graal.git", "kind" : "git"},
                {"url" : "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind" : "binary"},
            ]
        },
    ],
  },

  "repositories" : {
    "graaljs-lafo" : {
      "snapshotsUrl" : "https://curio.ssw.jku.at/nexus/content/repositories/snapshots",
      "releasesUrl": "https://curio.ssw.jku.at/nexus/content/repositories/releases",
      "licenses" : ["UPL", "MIT", "GPLv2-CPE"]
    },
  },

  "licenses" : {
    "UPL" : { #bulk of the code
      "name" : "Universal Permissive License, Version 1.0",
      "url" : "http://opensource.org/licenses/UPL",
    },
  },

  "defaultLicense" : "UPL",

  "javac.lint.overrides" : "none",

  "libraries" : {
    "NETBEANS_PROFILER" : {
      "urls" : ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/truffle/js/org-netbeans-lib-profiler-8.2-201609300101.jar"],
      "sha1" : "4b52bd03014f6d080ef0528865c1ee50621e35c6",
    },

    "ICU4J" : {
      "moduleName" : "com.ibm.icu",
      "sha1" : "2e159807158095566726600534034030dddab6d0",
      "sourceSha1" : "eb549b85cf9012f129d7d0489cd52fd49e9d9fc4",
      "maven" : {
        "groupId" : "com.ibm.icu",
        "artifactId" : "icu4j",
        "version" : "67.1",
      },
    },

    "ASM-7.1" : {
      "moduleName" : "org.objectweb.asm",
      "sha1" : "fa29aa438674ff19d5e1386d2c3527a0267f291e",
      "sourceSha1" : "9d170062d595240da35301362b079e5579c86f49",
      "maven" : {
        "groupId" : "org.ow2.asm",
        "artifactId" : "asm",
        "version" : "7.1",
      },
    },

    "ASM_TREE-7.1" : {
      "moduleName" : "org.objectweb.asm.tree",
      "sha1" : "a3662cf1c1d592893ffe08727f78db35392fa302",
      "sourceSha1" : "157238292b551de8680505fa2d19590d136e25b9",
      "maven" : {
        "groupId" : "org.ow2.asm",
        "artifactId" : "asm-tree",
        "version" : "7.1",
      },
      "dependencies" : [
        "ASM-7.1",
      ],
    },

    "ASM_ANALYSIS-7.1" : {
      "moduleName" : "org.objectweb.asm.tree.analysis",
      "sha1" : "379e0250f7a4a42c66c5e94e14d4c4491b3c2ed3",
      "sourceSha1" : "36789198124eb075f1a5efa18a0a7812fb16f47f",
      "maven" : {
        "groupId" : "org.ow2.asm",
        "artifactId" : "asm-analysis",
        "version" : "7.1",
      },
      "dependencies" : [
        "ASM_TREE-7.1",
      ],
    },

    "ASM_COMMONS-7.1" : {
      "moduleName" : "org.objectweb.asm.commons",
      "sha1" : "431dc677cf5c56660c1c9004870de1ed1ea7ce6c",
      "sourceSha1" : "a62ff3ae6e37affda7c6fb7d63b89194c6d006ee",
      "maven" : {
        "groupId" : "org.ow2.asm",
        "artifactId" : "asm-commons",
        "version" : "7.1",
      },
      "dependencies" : [
        "ASM-7.1",
        "ASM_TREE-7.1",
        "ASM_ANALYSIS-7.1",
      ],
    },

    "ASM_UTIL-7.1" : {
      "moduleName" : "org.objectweb.asm.util",
      "sha1" : "5b0b0f8cdb6c90582302ffcf5c20447206122f48",
      "sourceSha1" : "728ab1171a60dd2c408e6dd1d12fceb560635346",
      "maven" : {
        "groupId" : "org.ow2.asm",
        "artifactId" : "asm-util",
        "version" : "7.1",
      },
      "dependencies" : [
        "ASM-7.1",
        "ASM_TREE-7.1",
        "ASM_ANALYSIS-7.1",
      ],
    },

    "TEST262" : {
      "sha1" : "551f5233714aea646d97cfc97c1f4cc7f34bd683",
      "urls" : ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/truffle/js/test262-538fcd88.tar.bz2"],
    },

    "TESTNASHORN" : {
      "sha1" : "1a31d35e485247e0edf2738a248e1bc2b97f1054",
      "urls" : ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/truffle/js/testnashorn-e118c818dbf8.tar.bz2"],
    },

    "TESTNASHORN_EXTERNAL" : {
      "sha1" : "3e3edc251d800bc74f28c78f75844c7086cb5216",
      "urls" : ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/truffle/js/testnashorn-external-0f91116bb4bd.tar.bz2"],
    },

    "NASHORN_INTERNAL_TESTS" : {
      "sha1" : "b5840706cc8ce639fcafeab1bc61da2d8aa37afd",
      "urls" : ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/truffle/js/nashorn-internal-tests-700f5e3f5ff2.jar"],
    },

    "TESTV8" : {
      "sha1" : "11c15b8c2f0ef3b6cce1162a6c52057bebab45d7",
      "urls" : ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/truffle/js/testv8-20200722.tar.gz"],
    },

    "JACKSON_CORE" : {
      "sha1" : "2ef7b1cc34de149600f5e75bc2d5bf40de894e60",
      "maven" : {
        "groupId" : "com.fasterxml.jackson.core",
        "artifactId" : "jackson-core",
        "version" : "2.8.6",
      },
    },

    "JACKSON_ANNOTATIONS" : {
      "sha1" : "9577018f9ce3636a2e1cb0a0c7fe915e5098ded5",
      "maven" : {
        "groupId" : "com.fasterxml.jackson.core",
        "artifactId" : "jackson-annotations",
        "version" : "2.8.6",
      },
    },

    "JACKSON_DATABIND" : {
      "sha1" : "c43de61f74ecc61322ef8f402837ba65b0aa2bf4",
      "maven" : {
        "groupId" : "com.fasterxml.jackson.core",
        "artifactId" : "jackson-databind",
        "version" : "2.8.6",
      },
    },
  },

  "projects" : {
    "com.oracle.truffle.js" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.js.parser",
        "com.oracle.truffle.js.annotations",
        "com.oracle.truffle.js.codec",
        "com.oracle.truffle.js.runtime.doubleconv",
        "truffle:TRUFFLE_API",
        "ASM-7.1",
        "ASM_COMMONS-7.1",
        "ASM_UTIL-7.1",
        "ICU4J",
      ],
      "annotationProcessors" : ["truffle:TRUFFLE_DSL_PROCESSOR", "TRUFFLE_JS_FACTORY_PROCESSOR"],
      "jacoco" : "include",
      "javaCompliance" : "8+",
      "checkstyleVersion" : "8.8",
      "workingSets" : "Truffle,JavaScript",
    },

    "com.oracle.truffle.js.jdk8" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.truffle.js",
      ],
      "overlayTarget" : "com.oracle.truffle.js",
      "javaCompliance" : "8",
      "checkstyle" : "com.oracle.truffle.js",
      "workingSets" : "Truffle,JavaScript",
    },

    "com.oracle.truffle.js.jdk11" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.truffle.js",
      ],
      "overlayTarget" : "com.oracle.truffle.js",
      "multiReleaseJarVersion" : "11",
      "javaCompliance" : "11+",
      "checkstyle" : "com.oracle.truffle.js",
      "workingSets" : "Truffle,JavaScript",
    },

    "com.oracle.truffle.js.parser" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.truffle.js",
      ],
      "jacoco" : "include",
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "annotationProcessors" : ["truffle:TRUFFLE_DSL_PROCESSOR"],
      "workingSets" : "Truffle,JavaScript",
    },

    "com.oracle.js.parser" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "sdk:GRAAL_SDK"
      ],
      "jacoco" : "include",
      "javaCompliance" : "8+",
      "checkstyleVersion" : "8.8",
      "workingSets" : "Truffle,JavaScript",
    },

    "com.oracle.truffle.js.shell" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "sdk:LAUNCHER_COMMON",
      ],
      "jacoco" : "include",
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript",
    },

    "com.oracle.truffle.js.annotations" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [],
      "jacoco" : "include",
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript",
    },

    "com.oracle.truffle.js.codec" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [],
      "jacoco" : "include",
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript",
    },

    "com.oracle.truffle.js.snapshot" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.truffle.js.parser",
      ],
      "jacoco" : "include",
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript",
    },

    "com.oracle.truffle.js.factory.processor" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.truffle.js.annotations",
        "com.oracle.truffle.js.codec",
        "truffle:TRUFFLE_API",
      ],
      "jacoco" : "include",
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript",
    },

    "com.oracle.truffle.js.runtime.doubleconv" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [],
      "jacoco" : "include",
      "findbugs" : "false",
#     checkstyle and findbugs turned off to keep the source aligned
#     with the original nashorn version as much as possible
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript",
    },

    "com.oracle.truffle.js.stats" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.truffle.js.shell",
        "NETBEANS_PROFILER",
        "com.oracle.truffle.js",
      ],
      "jacoco" : "include",
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript",
    },

    "com.oracle.truffle.js.test" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "sdk:GRAAL_SDK",
        "mx:JUNIT",
        "GRAALJS",
        "truffle:TRUFFLE_TCK",
        "com.oracle.truffle.js.snapshot",
      ],
      "annotationProcessors" : ["truffle:TRUFFLE_DSL_PROCESSOR"],
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript",
      "testProject" : True,
    },

    "com.oracle.truffle.js.test.instrumentation" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "mx:JUNIT",
        "com.oracle.truffle.js",
        "com.oracle.truffle.js.parser",
      ],
      "annotationProcessors" : ["truffle:TRUFFLE_DSL_PROCESSOR"],
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript",
      "testProject" : True,
    },

    "com.oracle.truffle.js.test.threading" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "mx:JUNIT",
        "sdk:GRAAL_SDK",
      ],
      "annotationProcessors" : ["truffle:TRUFFLE_DSL_PROCESSOR"],
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript",
      "testProject" : True,
    },

    "com.oracle.truffle.js.scriptengine" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "sdk:GRAAL_SDK",
      ],
      "jacoco" : "include",
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript",
    },

    "com.oracle.truffle.js.scriptengine.test" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.oracle.truffle.js.scriptengine",
        "sdk:GRAAL_SDK",
        "mx:JUNIT",
        "GRAALJS",
      ],
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript",
      "testProject" : True,
    },

    "com.oracle.truffle.js.jmh" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "sdk:GRAAL_SDK",
        "GRAALJS",
        "mx:JMH_1_21"
      ],
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "annotationProcessors" : ["mx:JMH_1_21"],
      "spotbugsIgnoresGenerated" : True,
    },

    "com.oracle.truffle.js.test.external" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "graal-js:GRAALJS",
        "mx:JUNIT",
        "JACKSON_CORE",
        "JACKSON_ANNOTATIONS",
        "JACKSON_DATABIND",
        "NASHORN_INTERNAL_TESTS",
      ],
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript,Test",
      "testProject" : True,
    },

    "com.oracle.truffle.js.test.sdk" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "mx:JUNIT",
        "sdk:POLYGLOT_TCK"
      ],
      "checkstyle" : "com.oracle.truffle.js",
      "javaCompliance" : "8+",
      "workingSets" : "Truffle,JavaScript,Test",
      "testProject" : True,
    },

  },

  "distributions" : {
    "GRAALJS" : {
      "moduleInfo" : {
        "name" : "org.graalvm.js",
        "requiresConcealed" : {
          "org.graalvm.truffle" : [
            "com.oracle.truffle.api"
          ],
        },
        "exports" : [
          "com.oracle.truffle.js.lang to org.graalvm.truffle",
          "com.oracle.truffle.js.runtime.java.adapter",
        ],
      },
      "subDir" : "src",
      "dependencies" : [
        "com.oracle.truffle.js",
        "com.oracle.truffle.js.parser",
      ],
      "distDependencies" : [
        "regex:TREGEX",
        "truffle:TRUFFLE_API",
        "sdk:GRAAL_SDK",
      ],
      "exclude": [
        "ASM-7.1",
        "ASM_TREE-7.1",
        "ASM_ANALYSIS-7.1",
        "ASM_COMMONS-7.1",
        "ASM_UTIL-7.1",
        "ICU4J",
      ],
      "description" : "Graal JavaScript engine",
      "maven" : {
        "artifactId" : "js",
      },
      "license": [
        "UPL",  # Main code
        "MIT",  # JONI regexp engine
      ],
      "allowsJavadocWarnings": True,
    },

    "GRAALJS_LAUNCHER" : {
      "moduleInfo" : {
        "name" : "org.graalvm.js.launcher",
        "exports" : [],
      },
      "subDir" : "src",
      "dependencies" : ["com.oracle.truffle.js.shell"],
      "mainClass" : "com.oracle.truffle.js.shell.JSLauncher",
      "distDependencies" : ["sdk:LAUNCHER_COMMON"],
      "description" : "Graal JavaScript Launcher",
      "maven" : {
        "artifactId" : "js-launcher",
      },
      "allowsJavadocWarnings": True,
    },

    "GRAALJS_SCRIPTENGINE" : {
      "moduleInfo" : {
        "name" : "org.graalvm.js.scriptengine",
        "requires" : ["java.scripting"],
        "exports" : [
          "com.oracle.truffle.js.scriptengine",
        ],
      },
      "subDir" : "src",
      "dependencies" : ["com.oracle.truffle.js.scriptengine"],
      "distDependencies" : [
        "sdk:GRAAL_SDK"
      ],
      "description" : "Graal JavaScript ScriptEngine",
      "maven" : {
        "artifactId" : "js-scriptengine",
      },
      "allowsJavadocWarnings": True,
    },

    "TRUFFLE_JS_FACTORY_PROCESSOR" : {
      "subDir" : "src",
      "dependencies" : ["com.oracle.truffle.js.factory.processor"],
      "distDependencies" : [
        "truffle:TRUFFLE_API",
        "sdk:GRAAL_SDK"
      ],
      "maven" : False,
      "overlaps" : ["GRAALJS"],
    },

    "TRUFFLE_JS_SNAPSHOT_TOOL" : {
      "subDir" : "src",
      "dependencies" : ["com.oracle.truffle.js.snapshot"],
      "mainClass" : "com.oracle.truffle.js.snapshot.SnapshotTool",
      "distDependencies" : [
        "GRAALJS",
      ],
      "maven" : False,
    },

    "TRUFFLE_STATS" : {
      "subDir" : "src",
      "mainClass" : "com.oracle.truffle.js.stats.heap.HeapDumpAnalyzer",
      "dependencies" : ["com.oracle.truffle.js.stats"],
      "distDependencies" : [
        "GRAALJS",
        "NETBEANS_PROFILER",
        "GRAALJS_LAUNCHER"
      ],
      "maven" : False,
    },

    "GRAALJS_SCRIPTENGINE_TESTS" : {
      "subDir" : "src",
      "dependencies" : ["com.oracle.truffle.js.scriptengine.test"],
      "distDependencies" : [
        "mx:JUNIT",
        "sdk:GRAAL_SDK",
        "GRAALJS",
        "GRAALJS_SCRIPTENGINE",
      ],
      "maven" : False,
    },

    "TRUFFLE_JS_TESTS" : {
      "dependencies" : ["com.oracle.truffle.js.test", "com.oracle.truffle.js.test.external", "com.oracle.truffle.js.test.instrumentation", "com.oracle.truffle.js.test.threading"],
      "exclude" : [
        "mx:HAMCREST",
        "mx:JUNIT",
        "JACKSON_CORE",
        "JACKSON_ANNOTATIONS",
        "JACKSON_DATABIND",
        "NASHORN_INTERNAL_TESTS",
      ],
      "distDependencies" : [
        "GRAALJS",
        "truffle:TRUFFLE_TCK",
        "TRUFFLE_JS_SNAPSHOT_TOOL",
      ],
      "license": [
        "UPL",
      ],
      "maven" : False,
      "description" : "Graal JavaScript Tests",
      "allowsJavadocWarnings": True,
    },

    "SDK_JS_TESTS" : {
      "subDir" : "src",
      "javaCompliance" : "8+",
      "dependencies" : ["com.oracle.truffle.js.test.sdk"],
      "exclude" : [
        "mx:JUNIT",
      ],
      "distDependencies" : [
        "sdk:POLYGLOT_TCK"
      ],
      "maven" : False
    },

    "GRAALJS_GRAALVM_SUPPORT" : {
      "native" : True,
      "description" : "Graal.js support distribution for the GraalVM",
      "layout" : {
        "native-image.properties": "file:mx.graal-js/native-image.properties",
        "./": [
          "file:README.md",
        ],
      },
    },

    "JS_INTEROP_MICRO_BENCHMARKS" : {
      "subDir" : "src",
      "description" : "Graal.js JMH Interop Suite",
      "dependencies" : ["com.oracle.truffle.js.jmh"],
      "exclude" : [
        "mx:JUNIT"
      ],
      "distDependencies" : [
        "sdk:GRAAL_SDK",
        "GRAALJS"
      ]
    }
  }
}

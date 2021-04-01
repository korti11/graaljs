# Copyright 2012 the V8 project authors. All rights reserved.
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.
{
  'variables': {
    'V8_ROOT': '../../deps/v8',
    'v8_code': 1,
    'v8_random_seed%': 314159265,
    'v8_vector_stores%': 0,
    'v8_embed_script%': "",
    'mksnapshot_exec': '<(PRODUCT_DIR)/<(EXECUTABLE_PREFIX)mksnapshot<(EXECUTABLE_SUFFIX)',
    'v8_os_page_size%': 0,
    'generate_bytecode_output_root': '<(SHARED_INTERMEDIATE_DIR)/generate-bytecode-output-root',
    'generate_bytecode_builtins_list_output': '<(generate_bytecode_output_root)/builtins-generated/bytecodes-builtins-list.h',
    'torque_files': [
      "<(V8_ROOT)/src/builtins/array-copywithin.tq",
      "<(V8_ROOT)/src/builtins/array-every.tq",
      "<(V8_ROOT)/src/builtins/array-filter.tq",
      "<(V8_ROOT)/src/builtins/array-find.tq",
      "<(V8_ROOT)/src/builtins/array-findindex.tq",
      "<(V8_ROOT)/src/builtins/array-foreach.tq",
      "<(V8_ROOT)/src/builtins/array-from.tq",
      "<(V8_ROOT)/src/builtins/array-isarray.tq",
      "<(V8_ROOT)/src/builtins/array-join.tq",
      "<(V8_ROOT)/src/builtins/array-lastindexof.tq",
      "<(V8_ROOT)/src/builtins/array-map.tq",
      "<(V8_ROOT)/src/builtins/array-of.tq",
      "<(V8_ROOT)/src/builtins/array-reduce-right.tq",
      "<(V8_ROOT)/src/builtins/array-reduce.tq",
      "<(V8_ROOT)/src/builtins/array-reverse.tq",
      "<(V8_ROOT)/src/builtins/array-shift.tq",
      "<(V8_ROOT)/src/builtins/array-slice.tq",
      "<(V8_ROOT)/src/builtins/array-some.tq",
      "<(V8_ROOT)/src/builtins/array-splice.tq",
      "<(V8_ROOT)/src/builtins/array-unshift.tq",
      "<(V8_ROOT)/src/builtins/array.tq",
      "<(V8_ROOT)/src/builtins/base.tq",
      "<(V8_ROOT)/src/builtins/bigint.tq",
      "<(V8_ROOT)/src/builtins/boolean.tq",
      "<(V8_ROOT)/src/builtins/builtins-string.tq",
      "<(V8_ROOT)/src/builtins/collections.tq",
      "<(V8_ROOT)/src/builtins/cast.tq",
      "<(V8_ROOT)/src/builtins/convert.tq",
      "<(V8_ROOT)/src/builtins/console.tq",
      "<(V8_ROOT)/src/builtins/data-view.tq",
      "<(V8_ROOT)/src/builtins/finalization-registry.tq",
      "<(V8_ROOT)/src/builtins/frames.tq",
      "<(V8_ROOT)/src/builtins/frame-arguments.tq",
      "<(V8_ROOT)/src/builtins/growable-fixed-array.tq",
      "<(V8_ROOT)/src/builtins/ic-callable.tq",
      "<(V8_ROOT)/src/builtins/ic.tq",
      "<(V8_ROOT)/src/builtins/internal-coverage.tq",
      "<(V8_ROOT)/src/builtins/iterator.tq",
      "<(V8_ROOT)/src/builtins/math.tq",
      "<(V8_ROOT)/src/builtins/number.tq",
      "<(V8_ROOT)/src/builtins/object-fromentries.tq",
      "<(V8_ROOT)/src/builtins/object.tq",
      "<(V8_ROOT)/src/builtins/promise-abstract-operations.tq",
      "<(V8_ROOT)/src/builtins/promise-all.tq",
      "<(V8_ROOT)/src/builtins/promise-all-element-closure.tq",
      "<(V8_ROOT)/src/builtins/promise-any.tq",
      "<(V8_ROOT)/src/builtins/promise-constructor.tq",
      "<(V8_ROOT)/src/builtins/promise-finally.tq",
      "<(V8_ROOT)/src/builtins/promise-misc.tq",
      "<(V8_ROOT)/src/builtins/promise-race.tq",
      "<(V8_ROOT)/src/builtins/promise-reaction-job.tq",
      "<(V8_ROOT)/src/builtins/promise-resolve.tq",
      "<(V8_ROOT)/src/builtins/promise-then.tq",
      "<(V8_ROOT)/src/builtins/promise-jobs.tq",
      "<(V8_ROOT)/src/builtins/proxy-constructor.tq",
      "<(V8_ROOT)/src/builtins/proxy-delete-property.tq",
      "<(V8_ROOT)/src/builtins/proxy-get-property.tq",
      "<(V8_ROOT)/src/builtins/proxy-get-prototype-of.tq",
      "<(V8_ROOT)/src/builtins/proxy-has-property.tq",
      "<(V8_ROOT)/src/builtins/proxy-is-extensible.tq",
      "<(V8_ROOT)/src/builtins/proxy-prevent-extensions.tq",
      "<(V8_ROOT)/src/builtins/proxy-revocable.tq",
      "<(V8_ROOT)/src/builtins/proxy-revoke.tq",
      "<(V8_ROOT)/src/builtins/proxy-set-property.tq",
      "<(V8_ROOT)/src/builtins/proxy-set-prototype-of.tq",
      "<(V8_ROOT)/src/builtins/proxy.tq",
      "<(V8_ROOT)/src/builtins/reflect.tq",
      "<(V8_ROOT)/src/builtins/regexp-exec.tq",
      "<(V8_ROOT)/src/builtins/regexp-match-all.tq",
      "<(V8_ROOT)/src/builtins/regexp-match.tq",
      "<(V8_ROOT)/src/builtins/regexp-replace.tq",
      "<(V8_ROOT)/src/builtins/regexp-search.tq",
      "<(V8_ROOT)/src/builtins/regexp-source.tq",
      "<(V8_ROOT)/src/builtins/regexp-split.tq",
      "<(V8_ROOT)/src/builtins/regexp-test.tq",
      "<(V8_ROOT)/src/builtins/regexp.tq",
      "<(V8_ROOT)/src/builtins/string-endswith.tq",
      "<(V8_ROOT)/src/builtins/string-html.tq",
      "<(V8_ROOT)/src/builtins/string-iterator.tq",
      "<(V8_ROOT)/src/builtins/string-pad.tq",
      "<(V8_ROOT)/src/builtins/string-repeat.tq",
      "<(V8_ROOT)/src/builtins/string-replaceall.tq",
      "<(V8_ROOT)/src/builtins/string-slice.tq",
      "<(V8_ROOT)/src/builtins/string-startswith.tq",
      "<(V8_ROOT)/src/builtins/string-substring.tq",
      "<(V8_ROOT)/src/builtins/string-substr.tq",
      "<(V8_ROOT)/src/builtins/symbol.tq",
      "<(V8_ROOT)/src/builtins/torque-internal.tq",
      "<(V8_ROOT)/src/builtins/typed-array-createtypedarray.tq",
      "<(V8_ROOT)/src/builtins/typed-array-every.tq",
      "<(V8_ROOT)/src/builtins/typed-array-filter.tq",
      "<(V8_ROOT)/src/builtins/typed-array-find.tq",
      "<(V8_ROOT)/src/builtins/typed-array-findindex.tq",
      "<(V8_ROOT)/src/builtins/typed-array-foreach.tq",
      "<(V8_ROOT)/src/builtins/typed-array-from.tq",
      "<(V8_ROOT)/src/builtins/typed-array-of.tq",
      "<(V8_ROOT)/src/builtins/typed-array-reduce.tq",
      "<(V8_ROOT)/src/builtins/typed-array-reduceright.tq",
      "<(V8_ROOT)/src/builtins/typed-array-set.tq",
      "<(V8_ROOT)/src/builtins/typed-array-slice.tq",
      "<(V8_ROOT)/src/builtins/typed-array-some.tq",
      "<(V8_ROOT)/src/builtins/typed-array-sort.tq",
      "<(V8_ROOT)/src/builtins/typed-array-subarray.tq",
      "<(V8_ROOT)/src/builtins/typed-array.tq",
      "<(V8_ROOT)/src/builtins/wasm.tq",
      "<(V8_ROOT)/src/ic/handler-configuration.tq",
      "<(V8_ROOT)/src/objects/allocation-site.tq",
      "<(V8_ROOT)/src/objects/api-callbacks.tq",
      "<(V8_ROOT)/src/objects/arguments.tq",
      "<(V8_ROOT)/src/objects/cell.tq",
      "<(V8_ROOT)/src/objects/code.tq",
      "<(V8_ROOT)/src/objects/contexts.tq",
      "<(V8_ROOT)/src/objects/data-handler.tq",
      "<(V8_ROOT)/src/objects/debug-objects.tq",
      "<(V8_ROOT)/src/objects/descriptor-array.tq",
      "<(V8_ROOT)/src/objects/embedder-data-array.tq",
      "<(V8_ROOT)/src/objects/feedback-cell.tq",
      "<(V8_ROOT)/src/objects/feedback-vector.tq",
      "<(V8_ROOT)/src/objects/fixed-array.tq",
      "<(V8_ROOT)/src/objects/foreign.tq",
      "<(V8_ROOT)/src/objects/free-space.tq",
      "<(V8_ROOT)/src/objects/heap-number.tq",
      "<(V8_ROOT)/src/objects/heap-object.tq",
      "<(V8_ROOT)/src/objects/js-aggregate-error.tq",
      "<(V8_ROOT)/src/objects/js-array-buffer.tq",
      "<(V8_ROOT)/src/objects/js-array.tq",
      "<(V8_ROOT)/src/objects/js-collection-iterator.tq",
      "<(V8_ROOT)/src/objects/js-collection.tq",
      "<(V8_ROOT)/src/objects/js-generator.tq",
      "<(V8_ROOT)/src/objects/js-objects.tq",
      "<(V8_ROOT)/src/objects/js-promise.tq",
      "<(V8_ROOT)/src/objects/js-proxy.tq",
      "<(V8_ROOT)/src/objects/js-regexp-string-iterator.tq",
      "<(V8_ROOT)/src/objects/js-regexp.tq",
      "<(V8_ROOT)/src/objects/js-weak-refs.tq",
      "<(V8_ROOT)/src/objects/literal-objects.tq",
      "<(V8_ROOT)/src/objects/map.tq",
      "<(V8_ROOT)/src/objects/microtask.tq",
      "<(V8_ROOT)/src/objects/module.tq",
      "<(V8_ROOT)/src/objects/name.tq",
      "<(V8_ROOT)/src/objects/oddball.tq",
      "<(V8_ROOT)/src/objects/ordered-hash-table.tq",
      "<(V8_ROOT)/src/objects/primitive-heap-object.tq",
      "<(V8_ROOT)/src/objects/promise.tq",
      "<(V8_ROOT)/src/objects/property-array.tq",
      "<(V8_ROOT)/src/objects/property-cell.tq",
      "<(V8_ROOT)/src/objects/property-descriptor-object.tq",
      "<(V8_ROOT)/src/objects/prototype-info.tq",
      "<(V8_ROOT)/src/objects/regexp-match-info.tq",
      "<(V8_ROOT)/src/objects/scope-info.tq",
      "<(V8_ROOT)/src/objects/script.tq",
      "<(V8_ROOT)/src/objects/shared-function-info.tq",
      "<(V8_ROOT)/src/objects/source-text-module.tq",
      "<(V8_ROOT)/src/objects/stack-frame-info.tq",
      "<(V8_ROOT)/src/objects/string.tq",
      "<(V8_ROOT)/src/objects/struct.tq",
      "<(V8_ROOT)/src/objects/synthetic-module.tq",
      "<(V8_ROOT)/src/objects/template-objects.tq",
      "<(V8_ROOT)/src/objects/template.tq",
      "<(V8_ROOT)/src/wasm/wasm-objects.tq",
      "<(V8_ROOT)/test/torque/test-torque.tq",
      "<(V8_ROOT)/third_party/v8/builtins/array-sort.tq",
    ],
    'torque_output_root': '<(SHARED_INTERMEDIATE_DIR)/torque-output-root',
    'torque_files_replaced': ['<!@pymod_do_main(ForEachReplace ".tq" "-tq-csa" <@(torque_files))'],
    'torque_outputs': ['<!@pymod_do_main(ForEachFormat "<(torque_output_root)/torque-generated/%s.cc" <@(torque_files_replaced))'],
    'torque_outputs+': ['<!@pymod_do_main(ForEachFormat "<(torque_output_root)/torque-generated/%s.h" <@(torque_files_replaced))'],
    'v8_compiler_sources': ['<!@pymod_do_main(GN-scraper "<(V8_ROOT)/BUILD.gn"  "v8_compiler_sources = ")'],

    'conditions': [
      ['v8_enable_i18n_support', {
        'torque_files': [
          "<(V8_ROOT)/src/objects/intl-objects.tq",
        ]
      }]
    ],
  },
  'includes': ['toolchain.gypi', 'features.gypi'],
  'target_defaults': {
    'msvs_settings': {
      'VCCLCompilerTool': {
        'AdditionalOptions': ['/utf-8']
      }
    },
  },
  'targets': [
    {
      'target_name': 'run_torque',
      'type': 'none',
      'conditions': [
        ['want_separate_host_toolset', {
          'dependencies': ['torque#host'],
          'toolsets': ['host', 'target'],
        }, {
          'dependencies': ['torque'],
        }],
      ],
      'hard_dependency': 1,
      'direct_dependent_settings': {
        'include_dirs': [
          '<(torque_output_root)',
        ],
      },
      'actions': [
        {
          'action_name': 'run_torque_action',
          'inputs': [  # Order matters.
            '<(PRODUCT_DIR)/<(EXECUTABLE_PREFIX)torque<(EXECUTABLE_SUFFIX)',
            '<@(torque_files)',
          ],
          'outputs': [
            '<(torque_output_root)/torque-generated/bit-fields-tq.h',
            '<(torque_output_root)/torque-generated/builtin-definitions-tq.h',
            '<(torque_output_root)/torque-generated/interface-descriptors-tq.inc',
            '<(torque_output_root)/torque-generated/factory-tq.cc',
            '<(torque_output_root)/torque-generated/factory-tq.inc',
            '<(torque_output_root)/torque-generated/field-offsets-tq.h',
            '<(torque_output_root)/torque-generated/class-verifiers-tq.cc',
            '<(torque_output_root)/torque-generated/class-verifiers-tq.h',
            '<(torque_output_root)/torque-generated/enum-verifiers-tq.cc',
            '<(torque_output_root)/torque-generated/objects-printer-tq.cc',
            '<(torque_output_root)/torque-generated/objects-body-descriptors-tq-inl.inc',
            '<(torque_output_root)/torque-generated/class-definitions-tq.cc',
            '<(torque_output_root)/torque-generated/class-definitions-tq-inl.h',
            '<(torque_output_root)/torque-generated/class-definitions-tq.h',
            '<(torque_output_root)/torque-generated/class-debug-readers-tq.cc',
            '<(torque_output_root)/torque-generated/class-debug-readers-tq.h',
            '<(torque_output_root)/torque-generated/exported-macros-assembler-tq.cc',
            '<(torque_output_root)/torque-generated/exported-macros-assembler-tq.h',
            '<(torque_output_root)/torque-generated/csa-types-tq.h',
            '<(torque_output_root)/torque-generated/instance-types-tq.h',
            '<@(torque_outputs)',
          ],
          'action': [
            '<@(_inputs)',
            '-o', '<(torque_output_root)/torque-generated',
            '-v8-root', '<(V8_ROOT)'
          ],
        },
      ],
    },  # run_torque
    {
      'target_name': 'v8_maybe_icu',
      'type': 'none',
      'hard_dependency': 1,
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
        ['v8_enable_i18n_support', {
          'dependencies': [
            '<(icu_gyp_path):icui18n',
            '<(icu_gyp_path):icuuc',
          ],
          'export_dependent_settings': [
            '<(icu_gyp_path):icui18n',
            '<(icu_gyp_path):icuuc',
          ],
        }],
      ],
    },  # v8_maybe_icu
    {
      'target_name': 'torque_generated_initializers',
      'type': 'none',
      'hard_dependency': 1,
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
      ],
      'dependencies': [
        'generate_bytecode_builtins_list',
        'run_torque',
        'v8_maybe_icu',
      ],
      'direct_dependent_settings': {
        'sources': [
          '<(torque_output_root)/torque-generated/csa-types-tq.h',
          '<(torque_output_root)/torque-generated/enum-verifiers-tq.cc',
          '<(torque_output_root)/torque-generated/exported-macros-assembler-tq.cc',
          '<(torque_output_root)/torque-generated/exported-macros-assembler-tq.h',
          '<(V8_ROOT)/src/torque/runtime-support.h',
          '<@(torque_outputs)',
        ],
      }
    },  # torque_generated_initializers
    {
      'target_name': 'torque_generated_definitions',
      'type': 'none',
      'hard_dependency': 1,
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
      ],
      'dependencies': [
        'generate_bytecode_builtins_list',
        'run_torque',
        'v8_maybe_icu',
      ],
      'direct_dependent_settings': {
        'sources': [
          '<(torque_output_root)/torque-generated/class-definitions-tq.cc',
          '<(torque_output_root)/torque-generated/class-verifiers-tq.cc',
          '<(torque_output_root)/torque-generated/class-verifiers-tq.h',
          '<(torque_output_root)/torque-generated/factory-tq.cc',
          '<(torque_output_root)/torque-generated/objects-printer-tq.cc',
        ],
        'include_dirs': [
          '<(torque_output_root)',
        ],
      },
    },  # torque_generated_definitions
    {
      'target_name': 'generate_bytecode_builtins_list',
      'type': 'none',
      'hard_dependency': 1,
      'conditions': [
        ['want_separate_host_toolset', {
          'dependencies': ['bytecode_builtins_list_generator#host'],
          'toolsets': ['host', 'target'],
        }, {
          'dependencies': ['bytecode_builtins_list_generator'],
        }],
      ],
      'direct_dependent_settings': {
        'sources': [
          '<(generate_bytecode_builtins_list_output)',
        ],
        'include_dirs': [
          '<(generate_bytecode_output_root)',
          '<(torque_output_root)',
        ],
      },
      'actions': [
        {
          'action_name': 'generate_bytecode_builtins_list_action',
          'inputs': [
            '<(PRODUCT_DIR)/<(EXECUTABLE_PREFIX)bytecode_builtins_list_generator<(EXECUTABLE_SUFFIX)',
          ],
          'outputs': [
            '<(generate_bytecode_builtins_list_output)',
          ],
          'action': [
            'python',
            '<(V8_ROOT)/tools/run.py',
            '<@(_inputs)',
            '<@(_outputs)',
          ],
        },
      ],
    },  # generate_bytecode_builtins_list
    {
      'target_name': 'v8_init',
      'type': 'static_library',
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
      ],
      'dependencies': [
        'generate_bytecode_builtins_list',
        'run_torque',
        'v8_initializers',
        'v8_maybe_icu',
      ],
      'sources': [
        ### gcmole(all) ###
        '<(V8_ROOT)/src/init/setup-isolate-full.cc',

        # '<(generate_bytecode_builtins_list_output)',
      ],
    },  # v8_init
    {
      'target_name': 'v8_initializers',
      'type': 'static_library',
      'dependencies': [
        'torque_generated_initializers',
      ],
      'include_dirs': [
        '<(torque_output_root)',
        '<(generate_bytecode_output_root)',
      ],
      'sources': [
        '<!@pymod_do_main(GN-scraper "<(V8_ROOT)/BUILD.gn"  "\\"v8_initializers.*?sources = ")',

        '<@(torque_outputs)',
      ],
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
        ['v8_target_arch=="ia32"', {
          'sources': [
            '<(V8_ROOT)/src/builtins/ia32/builtins-ia32.cc',
          ],
        }],
        ['v8_target_arch=="x64"', {
          'sources': [
            '<(V8_ROOT)/src/builtins/x64/builtins-x64.cc',
          ],
        }],
        ['v8_target_arch=="arm"', {
          'sources': [
            '<(V8_ROOT)/src/builtins/arm/builtins-arm.cc',
          ],
        }],
        ['v8_target_arch=="arm64"', {
          'sources': [
            '<(V8_ROOT)/src/builtins/arm64/builtins-arm64.cc',
          ],
        }],
        ['v8_target_arch=="mips" or v8_target_arch=="mipsel"', {
          'sources': [
            '<(V8_ROOT)/src/builtins/mips/builtins-mips.cc',
          ],
        }],
        ['v8_target_arch=="mips64" or v8_target_arch=="mips64el"', {
          'sources': [
            '<(V8_ROOT)/src/builtins/mips64/builtins-mips64.cc',
          ],
        }],
        ['v8_target_arch=="ppc"', {
          'sources': [
            '<(V8_ROOT)/src/builtins/ppc/builtins-ppc.cc',
          ],
        }],
        ['v8_target_arch=="ppc64"', {
          'sources': [
            '<(V8_ROOT)/src/builtins/ppc/builtins-ppc.cc',
          ],
        }],
        ['v8_target_arch=="s390x"', {
          'sources': [
            '<(V8_ROOT)/src/builtins/s390/builtins-s390.cc',
          ],
        }],
        ['v8_enable_i18n_support==1', {
          'dependencies': [
            '<(icu_gyp_path):icui18n',
            '<(icu_gyp_path):icuuc',
          ],
        }, {
           'sources!': [
             '<(V8_ROOT)/src/builtins/builtins-intl-gen.cc',
           ],
         }],
        ['OS=="win"', {
          'msvs_precompiled_header': '<(V8_ROOT)/../../tools/msvs/pch/v8_pch.h',
          'msvs_precompiled_source': '<(V8_ROOT)/../../tools/msvs/pch/v8_pch.cc',
          'sources': [
            '<(_msvs_precompiled_header)',
            '<(_msvs_precompiled_source)',
          ],
        }],
      ],
    },  # v8_initializers
    {
      'target_name': 'v8_snapshot',
      'type': 'static_library',
      'toolsets': ['target'],
      'conditions': [
        ['want_separate_host_toolset', {
          'conditions': [
            ['v8_target_arch=="arm64"', {
              'msvs_enable_marmasm': 1,
            }]
          ],
          'dependencies': [
            'v8_base_without_compiler',
          ]
        }, {
          'dependencies': [
            'v8_base_without_compiler',
          ]
        }],
      ],
      'sources': [
        '<(V8_ROOT)/src/init/setup-isolate-deserialize.cc',
      ],
      'xcode_settings': {
        # V8 7.4 over macOS10.11 compatibility
        # Refs: https://github.com/nodejs/node/pull/26685
        'GCC_GENERATE_DEBUGGING_SYMBOLS': 'NO',
      },
      'actions': [],
    },  # v8_snapshot
    {
      'target_name': 'v8_version',
      'type': 'none',
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
      ],
      'direct_dependent_settings': {
        'sources': [
          '<(V8_ROOT)/include/v8-value-serializer-version.h',
          '<(V8_ROOT)/include/v8-version-string.h',
          '<(V8_ROOT)/include/v8-version.h',
        ],
      },
    },  # v8_version
    {
      'target_name': 'v8_headers',
      'type': 'none',
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
      ],
      'dependencies': [
        'v8_version',
      ],
      'direct_dependent_settings': {
        'sources': [
          '<(V8_ROOT)/include/v8-fast-api-calls.h',
          '<(V8_ROOT)/include/v8-internal.h',
          '<(V8_ROOT)/include/v8.h',
          '<(V8_ROOT)/include/v8config.h',

          # The following headers cannot be platform-specific. The include validation
          # of `gn gen $dir --check` requires all header files to be available on all
          # platforms.
          '<(V8_ROOT)/include/v8-wasm-trap-handler-posix.h',
          '<(V8_ROOT)/include/v8-wasm-trap-handler-win.h',
        ],
      },
    },  # v8_headers
    {
      'target_name': 'v8_shared_internal_headers',
      'type': 'none',
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
      ],
      'dependencies': [
        'v8_headers',
      ],
      'direct_dependent_settings': {
        'sources': [
          '<(V8_ROOT)/src/common/globals.h',
        ],
      },
    },  # v8_shared_internal_headers
    {
      'target_name': 'v8_compiler_opt',
      'type': 'static_library',
      'dependencies': [
        'generate_bytecode_builtins_list',
        'run_torque',
        'v8_maybe_icu',
      ],
      'sources': ['<@(v8_compiler_sources)'],
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
        ['OS=="win"', {
          'msvs_precompiled_header': '<(V8_ROOT)/../../tools/msvs/pch/v8_pch.h',
          'msvs_precompiled_source': '<(V8_ROOT)/../../tools/msvs/pch/v8_pch.cc',
          'sources': [
            '<(_msvs_precompiled_header)',
            '<(_msvs_precompiled_source)',
          ],
        }],
      ],
    },  # v8_compiler_opt
    {
      'target_name': 'v8_compiler',
      'type': 'static_library',
      'dependencies': [
        'generate_bytecode_builtins_list',
        'run_torque',
        'v8_maybe_icu',
      ],
      'sources': ['<@(v8_compiler_sources)'],
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
        ['OS=="win"', {
          'msvs_precompiled_header': '<(V8_ROOT)/../../tools/msvs/pch/v8_pch.h',
          'msvs_precompiled_source': '<(V8_ROOT)/../../tools/msvs/pch/v8_pch.cc',
          'sources': [
            '<(_msvs_precompiled_header)',
            '<(_msvs_precompiled_source)',
          ],
        }],
      ],
    },  # v8_compiler
    {
      'target_name': 'v8_compiler_for_mksnapshot',
      'type': 'none',
      'hard_dependency': 1,
      'dependencies': [
        'generate_bytecode_builtins_list',
        'run_torque',
        'v8_maybe_icu',
      ],
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
        ['is_component_build and not v8_optimized_debug and v8_enable_fast_mksnapshot', {
          'dependencies': [
            'v8_compiler_opt',
          ],
          'export_dependent_settings': [
            'v8_compiler_opt',
          ],
        }, {
           'dependencies': [
             'v8_compiler',
           ],
           'export_dependent_settings': [
             'v8_compiler',
           ],
         }],
      ],
    },  # v8_compiler_for_mksnapshot
    {
      'target_name': 'v8_base_without_compiler',
      'type': 'static_library',
      # Since this target is a static-library, but as a side effect it generates
      # header files, it needs to be a hard dependency.
      'hard_dependency': 1,
      'dependencies': [
        'v8_libbase',
        'v8_libsampler',
      ],
      'direct_dependent_settings': {
        'include_dirs': ['<(SHARED_INTERMEDIATE_DIR)'],
      },
      'variables': {
        'optimize': 'max',
      },
      'include_dirs': [
        '../../deps/uv/include',
        '<(java_home)/include/',
        '<(DEPTH)',
        '<(SHARED_INTERMEDIATE_DIR)'
      ],
      'conditions': [
        ['OS=="linux"', {
          'include_dirs+': [
            '<(java_home)/include/linux/',
          ],
        }],
        ['OS=="win"', {
          'include_dirs+': [
            '<(java_home)/include/win32/',
          ],
        }],
        ['OS=="solaris"', {
          'include_dirs+': [
            '<(java_home)/include/solaris/',
          ],
        }],
        ['OS=="mac"', {
          'include_dirs+': [
            '<(java_home)/include/darwin/',
          ],
        }],
        ['OS != "win"', {
          'defines': [ '__POSIX__' ],
        }],
      ],
      'link_settings': {
        'conditions' : [
          ['OS=="linux" or OS=="solaris"', {
            'libraries': [
              '-L<(java_home)/lib',
              "-Wl,-rpath='$$ORIGIN/../../../lib/'",
              "-Wl,-rpath='$$ORIGIN/../../../../lib/'",
              "-Wl,-rpath='$$ORIGIN/../../../../jre/lib/'",
              "-Wl,-rpath='$$ORIGIN/../../../../jre/languages/R/lib/'",
            ],
          }],
          ['OS=="linux" and target_arch=="x64"', {
            'libraries': [
              '-L<(java_home)/jre/lib/amd64/server -L<(java_home)/jre/lib/amd64',
              "-Wl,-rpath='$$ORIGIN/../../../../lib/amd64/'",
              "-Wl,-rpath='$$ORIGIN/../../../../jre/lib/amd64/'",
            ],
          }],
          ['OS=="solaris" or (OS=="linux" and target_arch=="sparcv9")', {
            'libraries': [
              '-L<(java_home)/jre/lib/sparcv9/server -L<(java_home)/jre/lib/sparcv9',
              "-Wl,-rpath='$$ORIGIN/../../../../lib/sparcv9/'",
              "-Wl,-rpath='$$ORIGIN/../../../../jre/lib/sparcv9/'",
            ],
          }],
          ['OS=="mac"', {
            'libraries': [
              '-L<(java_home)/jre/lib/server -L<(java_home)/jre/lib -L<(java_home)/lib',
              "-Wl,-rpath,'@loader_path/../../../lib/'",
              "-Wl,-rpath,'@loader_path/../../../../lib/'",
              "-Wl,-rpath,'@loader_path/../../../../jre/lib/'",
              "-Wl,-rpath,'@loader_path/../../../../jre/languages/R/lib/'",
            ],
          }],
          ['OS == "win"', {
            'libraries': [
              '-lDbghelp',
            ],
          }],
          ['OS != "win"', {
            'libraries': [
              '-ljsig',
              '-ldl',
            ],
          }],
       ]},
      'sources': [
        '<(V8_ROOT)/include/v8-platform.h',
        '<(V8_ROOT)/include/v8-profiler.h',
        '<(V8_ROOT)/include/v8-testing.h',
        '<(V8_ROOT)/include/v8-util.h',
        '<(V8_ROOT)/include/v8-value-serializer-version.h',
        '<(V8_ROOT)/include/v8-version-string.h',
        '<(V8_ROOT)/include/v8-version.h',
        '<(V8_ROOT)/include/v8.h',
        '<(V8_ROOT)/include/v8config.h',
        '<(V8_ROOT)/src/graal/callbacks.cc',
        '<(V8_ROOT)/src/graal/graal_array.cc',
        '<(V8_ROOT)/src/graal/graal_array_buffer.cc',
        '<(V8_ROOT)/src/graal/graal_array_buffer_view.cc',
        '<(V8_ROOT)/src/graal/graal_big_int.cc',
        '<(V8_ROOT)/src/graal/graal_boolean.cc',
        '<(V8_ROOT)/src/graal/graal_context.cc',
        '<(V8_ROOT)/src/graal/graal_date.cc',
        '<(V8_ROOT)/src/graal/graal_external.cc',
        '<(V8_ROOT)/src/graal/graal_function.cc',
        '<(V8_ROOT)/src/graal/graal_function_template.cc',
        '<(V8_ROOT)/src/graal/graal_handle_content.cc',
        '<(V8_ROOT)/src/graal/graal_isolate.cc',
        '<(V8_ROOT)/src/graal/graal_map.cc',
        '<(V8_ROOT)/src/graal/graal_message.cc',
        '<(V8_ROOT)/src/graal/graal_missing_primitive.cc',
        '<(V8_ROOT)/src/graal/graal_module.cc',
        '<(V8_ROOT)/src/graal/graal_number.cc',
        '<(V8_ROOT)/src/graal/graal_object.cc',
        '<(V8_ROOT)/src/graal/graal_object_template.cc',
        '<(V8_ROOT)/src/graal/graal_primitive_array.cc',
        '<(V8_ROOT)/src/graal/graal_promise.cc',
        '<(V8_ROOT)/src/graal/graal_proxy.cc',
        '<(V8_ROOT)/src/graal/graal_regexp.cc',
        '<(V8_ROOT)/src/graal/graal_script.cc',
        '<(V8_ROOT)/src/graal/graal_script_or_module.cc',
        '<(V8_ROOT)/src/graal/graal_set.cc',
        '<(V8_ROOT)/src/graal/graal_stack_frame.cc',
        '<(V8_ROOT)/src/graal/graal_stack_trace.cc',
        '<(V8_ROOT)/src/graal/graal_string.cc',
        '<(V8_ROOT)/src/graal/graal_symbol.cc',
        '<(V8_ROOT)/src/graal/graal_template.cc',
        '<(V8_ROOT)/src/graal/graal_unbound_script.cc',
        '<(V8_ROOT)/src/graal/graal_value.cc',
        '<(V8_ROOT)/src/graal/v8.cc'
      ],
    },  # v8_base_without_compiler
    {
      'target_name': 'v8_base',
      'type': 'none',
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
      ],
      'dependencies': [
        'v8_base_without_compiler',
      ],
    },  # v8_base
    {
      'target_name': 'torque_base',
      'type': 'static_library',
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
      ],
      'sources': [
        '<!@pymod_do_main(GN-scraper "<(V8_ROOT)/BUILD.gn"  "\\"torque_base.*?sources = ")',
      ],
      'dependencies': [
        'v8_shared_internal_headers',
        'v8_libbase',
      ],
      'defines!': [
        '_HAS_EXCEPTIONS=0',
        'BUILDING_V8_SHARED=1',
      ],
      'cflags_cc!': ['-fno-exceptions'],
      'cflags_cc': ['-fexceptions'],
      'xcode_settings': {
        'GCC_ENABLE_CPP_EXCEPTIONS': 'YES',  # -fexceptions
      },
      'msvs_settings': {
        'VCCLCompilerTool': {
          'RuntimeTypeInfo': 'true',
          'ExceptionHandling': 1,
        },
      },
    },  # torque_base
    {
      'target_name': 'torque_ls_base',
      'type': 'static_library',
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
      ],
      'sources': [
        '<!@pymod_do_main(GN-scraper "<(V8_ROOT)/BUILD.gn"  "\\"torque_ls_base.*?sources = ")',
      ],
      'dependencies': [
        'torque_base',
      ],
      'defines!': [
        '_HAS_EXCEPTIONS=0',
        'BUILDING_V8_SHARED=1',
      ],
      'cflags_cc!': ['-fno-exceptions'],
      'cflags_cc': ['-fexceptions'],
      'xcode_settings': {
        'GCC_ENABLE_CPP_EXCEPTIONS': 'YES',  # -fexceptions
      },
      'msvs_settings': {
        'VCCLCompilerTool': {
          'RuntimeTypeInfo': 'true',
          'ExceptionHandling': 1,
        },
      },
    },  # torque_ls_base
    {
      'target_name': 'v8_libbase',
      'type': 'none',
      'toolsets': ['host', 'target'],
      'variables': {
        'optimize': 'max',
      },
      'include_dirs': [
        '..',
      ],
      'direct_dependent_settings': {
        'include_dirs': ['..'],
      },
      'sources': [
      ],
    },  # v8_libbase
    {
      'target_name': 'v8_libplatform',
      'type': 'none',
      'dependencies': [
        'v8_libbase',
      ],
      'sources': [
        '<(V8_ROOT)/base/trace_event/common/trace_event_common.h',
        '<(V8_ROOT)/include/libplatform/libplatform-export.h',
        '<(V8_ROOT)/include/libplatform/libplatform.h',
        '<(V8_ROOT)/include/libplatform/v8-tracing.h',
        '<(V8_ROOT)/src/libplatform/default-foreground-task-runner.cc',
        '<(V8_ROOT)/src/libplatform/default-foreground-task-runner.h',
        '<(V8_ROOT)/src/libplatform/default-job.cc',
        '<(V8_ROOT)/src/libplatform/default-job.h',
        '<(V8_ROOT)/src/libplatform/default-platform.cc',
        '<(V8_ROOT)/src/libplatform/default-platform.h',
        '<(V8_ROOT)/src/libplatform/default-worker-threads-task-runner.cc',
        '<(V8_ROOT)/src/libplatform/default-worker-threads-task-runner.h',
        '<(V8_ROOT)/src/libplatform/delayed-task-queue.cc',
        '<(V8_ROOT)/src/libplatform/delayed-task-queue.h',
        '<(V8_ROOT)/src/libplatform/task-queue.cc',
        '<(V8_ROOT)/src/libplatform/task-queue.h',
        '<(V8_ROOT)/src/libplatform/tracing/trace-buffer.cc',
        '<(V8_ROOT)/src/libplatform/tracing/trace-buffer.h',
        '<(V8_ROOT)/src/libplatform/tracing/trace-config.cc',
        '<(V8_ROOT)/src/libplatform/tracing/trace-object.cc',
        '<(V8_ROOT)/src/libplatform/tracing/trace-writer.cc',
        '<(V8_ROOT)/src/libplatform/tracing/trace-writer.h',
        '<(V8_ROOT)/src/libplatform/tracing/tracing-controller.cc',
        '<(V8_ROOT)/src/libplatform/worker-thread.cc',
        '<(V8_ROOT)/src/libplatform/worker-thread.h',
      ],
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
        ['component=="shared_library"', {
          'direct_dependent_settings': {
            'defines': ['USING_V8_PLATFORM_SHARED'],
          },
          'defines': ['BUILDING_V8_PLATFORM_SHARED'],
        }],
        ['v8_use_perfetto', {
          'sources': [
            '<(V8_ROOT)/src/libplatform/tracing/json-trace-event-listener.cc',
            '<(V8_ROOT)/src/libplatform/tracing/json-trace-event-listener.h',
            '<(V8_ROOT)/src/libplatform/tracing/trace-event-listener.cc',
            '<(V8_ROOT)/src/libplatform/tracing/trace-event-listener.h',
          ],
          'dependencies': [
            '<(V8_ROOT)/third_party/perfetto:libperfetto',
            '<(V8_ROOT)/third_party/perfetto/protos/perfetto/trace:lite',
          ],
        }],
      ],
      'direct_dependent_settings': {
        'include_dirs': [
          '<(V8_ROOT)/include',
        ],
      },
    },  # v8_libplatform
    {
      'target_name': 'v8_libsampler',
      'type': 'none',
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
      ],
      'dependencies': [
        'v8_libbase',
      ],
      'sources': [
      ],
    },  # v8_libsampler
    {
      'target_name': 'bytecode_builtins_list_generator',
      'type': 'executable',
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host'],
        }],
      ],
      'defines!': [
        'BUILDING_V8_SHARED=1',
      ],
      'dependencies': [
        "v8_libbase",
        # "build/win:default_exe_manifest",
      ],
      'sources': [
        "<(V8_ROOT)/src/builtins/generate-bytecodes-builtins-list.cc",
        "<(V8_ROOT)/src/interpreter/bytecode-operands.cc",
        "<(V8_ROOT)/src/interpreter/bytecode-operands.h",
        "<(V8_ROOT)/src/interpreter/bytecodes.cc",
        "<(V8_ROOT)/src/interpreter/bytecodes.h",
      ],
    },  # bytecode_builtins_list_generator
    {
      'target_name': 'mksnapshot',
      'type': 'executable',
      'dependencies': [
        'v8_base_without_compiler',
        'v8_compiler_for_mksnapshot',
        'v8_init',
        'v8_libbase',
        'v8_libplatform',
        'v8_maybe_icu',
        # "build/win:default_exe_manifest",
      ],
      'sources': [
        '<!@pymod_do_main(GN-scraper "<(V8_ROOT)/BUILD.gn"  "\\"mksnapshot.*?sources = ")',
      ],
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host'],
        }],
      ],
    },  # mksnapshot
    {
      'target_name': 'torque',
      'type': 'executable',
      'dependencies': [
        'torque_base',
        # "build/win:default_exe_manifest",
      ],
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host'],
        }],
      ],
      'defines!': [
        '_HAS_EXCEPTIONS=0',
        'BUILDING_V8_SHARED=1',
      ],
      'cflags_cc!': ['-fno-exceptions'],
      'cflags_cc': ['-fexceptions'],
      'xcode_settings': {
        'GCC_ENABLE_CPP_EXCEPTIONS': 'YES',  # -fexceptions
      },
      'msvs_settings': {
        'VCCLCompilerTool': {
          'RuntimeTypeInfo': 'true',
          'ExceptionHandling': 1,
        },
        'VCLinkerTool': {
          'AdditionalDependencies': [
            'dbghelp.lib',
            'winmm.lib',
            'ws2_32.lib'
          ]
        }
      },
      'sources': [
        "<(V8_ROOT)/src/torque/torque.cc",
      ],
    },  # torque
    {
      'target_name': 'torque-language-server',
      'type': 'executable',
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host'],
        }],
      ],
      'dependencies': [
        'torque_base',
        'torque_ls_base',
        # "build/win:default_exe_manifest",
      ],
      'defines!': [
        '_HAS_EXCEPTIONS=0',
        'BUILDING_V8_SHARED=1',
      ],
      'msvs_settings': {
        'VCCLCompilerTool': {
          'RuntimeTypeInfo': 'true',
          'ExceptionHandling': 1,
        },
      },
      'sources': [
        "<(V8_ROOT)/src/torque/ls/torque-language-server.cc",
      ],
    },  # torque-language-server
    {
      'target_name': 'gen-regexp-special-case',
      'type': 'executable',
      'dependencies': [
        'v8_libbase',
        # "build/win:default_exe_manifest",
        'v8_maybe_icu',
      ],
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host'],
        }],
      ],
      'sources': [
        "<(V8_ROOT)/src/regexp/gen-regexp-special-case.cc",
      ],
    },  # gen-regexp-special-case
    {
      'target_name': 'run_gen-regexp-special-case',
      'type': 'none',
      'conditions': [
        ['want_separate_host_toolset', {
          'dependencies': ['gen-regexp-special-case#host'],
          'toolsets': ['host', 'target'],
        }, {
          'dependencies': ['gen-regexp-special-case'],
        }],
      ],
      'actions': [
        {
          'action_name': 'run_gen-regexp-special-case_action',
          'inputs': [
            '<(PRODUCT_DIR)/<(EXECUTABLE_PREFIX)gen-regexp-special-case<(EXECUTABLE_SUFFIX)',
          ],
          'outputs': [
            '<(SHARED_INTERMEDIATE_DIR)/src/regexp/special-case.cc',
          ],
          'action': [
            'python',
            '<(V8_ROOT)/tools/run.py',
            '<@(_inputs)',
            '<@(_outputs)',
          ],
        },
      ],
    },  # run_gen-regexp-special-case

    ###############################################################################
    # Public targets
    #

    {
      'target_name': 'v8',
      'hard_dependency': 1,
      'toolsets': ['target'],
      'dependencies': [
        'v8_snapshot',
      ],
      'conditions': [
        ['component=="shared_library"', {
          'type': '<(component)',
          'sources': [
            # Note: on non-Windows we still build this file so that gyp
            # has some sources to link into the component.
            '<(V8_ROOT)/src/utils/v8dll-main.cc',
          ],
          'defines': [
            'BUILDING_V8_SHARED',
          ],
          'direct_dependent_settings': {
            'defines': [
              'USING_V8_SHARED',
            ],
          },
          'conditions': [
            ['OS=="mac"', {
              'xcode_settings': {
                'OTHER_LDFLAGS': ['-dynamiclib', '-all_load']
              },
            }],
            ['soname_version!=""', {
              'product_extension': 'so.<(soname_version)',
            }],
          ],
        },
         {
           'type': 'static_library',
         }],
      ],
      'direct_dependent_settings': {
        'include_dirs': [
          '<(V8_ROOT)/include',
        ],
      },
      'actions': [
        {
          'action_name': 'v8_dump_build_config',
          'inputs': [
            '<(V8_ROOT)/tools/testrunner/utils/dump_build_config_gyp.py',
          ],
          'outputs': [
            '<(PRODUCT_DIR)/v8_build_config.json',
          ],
          'variables': {
            'v8_dump_build_config_args': [
              '<(PRODUCT_DIR)/v8_build_config.json',
              'dcheck_always_on=<(dcheck_always_on)',
              'is_android=<(is_android)',
              'is_asan=<(asan)',
              'is_cfi=<(cfi_vptr)',
              'is_clang=<(clang)',
              'is_component_build=<(component)',
              'is_debug=<(CONFIGURATION_NAME)',
              # Not available in gyp.
              'is_gcov_coverage=0',
              'is_msan=<(msan)',
              'is_tsan=<(tsan)',
              # Not available in gyp.
              'is_ubsan_vptr=0',
              'target_cpu=<(target_arch)',
              'v8_enable_i18n_support=<(v8_enable_i18n_support)',
              'v8_enable_verify_predictable=<(v8_enable_verify_predictable)',
              'v8_target_cpu=<(v8_target_arch)',
              'v8_use_siphash=<(v8_use_siphash)',
              'v8_enable_verify_csa=<(v8_enable_verify_csa)',
              'v8_enable_lite_mode=<(v8_enable_lite_mode)',
              'v8_enable_pointer_compression=<(v8_enable_pointer_compression)',
            ]
          },
          'conditions': [
            ['v8_target_arch=="mips" or v8_target_arch=="mipsel" \
              or v8_target_arch=="mips64" or v8_target_arch=="mips64el"', {
              'v8_dump_build_config_args': [
                'mips_arch_variant=<(mips_arch_variant)',
                'mips_use_msa=<(mips_use_msa)',
              ],
            }],
          ],
          'action': [
            'python', '<(V8_ROOT)/tools/testrunner/utils/dump_build_config_gyp.py',
            '<@(v8_dump_build_config_args)',
          ],
        },
      ],
    },  # v8
    # missing a bunch of fuzzer targets

    ###############################################################################
    # Protobuf targets, used only when building outside of chromium.
    #

    {
      'target_name': 'postmortem-metadata',
      'type': 'none',
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
      ],
      'dependencies': ['run_torque'],
      'variables': {
        'heapobject_files': [
          '<(V8_ROOT)/src/objects/objects.h',
          '<(V8_ROOT)/src/objects/objects-inl.h',
          '<(V8_ROOT)/src/objects/allocation-site-inl.h',
          '<(V8_ROOT)/src/objects/allocation-site.h',
          '<(V8_ROOT)/src/objects/cell-inl.h',
          '<(V8_ROOT)/src/objects/cell.h',
          '<(V8_ROOT)/src/objects/code-inl.h',
          '<(V8_ROOT)/src/objects/code.h',
          '<(V8_ROOT)/src/objects/data-handler.h',
          '<(V8_ROOT)/src/objects/data-handler-inl.h',
          '<(V8_ROOT)/src/objects/descriptor-array.h',
          '<(V8_ROOT)/src/objects/descriptor-array-inl.h',
          '<(V8_ROOT)/src/objects/feedback-cell.h',
          '<(V8_ROOT)/src/objects/feedback-cell-inl.h',
          '<(V8_ROOT)/src/objects/fixed-array-inl.h',
          '<(V8_ROOT)/src/objects/fixed-array.h',
          '<(V8_ROOT)/src/objects/heap-number-inl.h',
          '<(V8_ROOT)/src/objects/heap-number.h',
          '<(V8_ROOT)/src/objects/heap-object-inl.h',
          '<(V8_ROOT)/src/objects/heap-object.h',
          '<(V8_ROOT)/src/objects/instance-type.h',
          '<(V8_ROOT)/src/objects/js-array-inl.h',
          '<(V8_ROOT)/src/objects/js-array.h',
          '<(V8_ROOT)/src/objects/js-aggregate-error-inl.h',
          '<(V8_ROOT)/src/objects/js-aggregate-error.h',
          '<(V8_ROOT)/src/objects/js-array-buffer-inl.h',
          '<(V8_ROOT)/src/objects/js-array-buffer.h',
          '<(V8_ROOT)/src/objects/js-objects-inl.h',
          '<(V8_ROOT)/src/objects/js-objects.h',
          '<(V8_ROOT)/src/objects/js-promise-inl.h',
          '<(V8_ROOT)/src/objects/js-promise.h',
          '<(V8_ROOT)/src/objects/js-regexp-inl.h',
          '<(V8_ROOT)/src/objects/js-regexp.cc',
          '<(V8_ROOT)/src/objects/js-regexp.h',
          '<(V8_ROOT)/src/objects/js-regexp-string-iterator-inl.h',
          '<(V8_ROOT)/src/objects/js-regexp-string-iterator.h',
          '<(V8_ROOT)/src/objects/map.h',
          '<(V8_ROOT)/src/objects/map.cc',
          '<(V8_ROOT)/src/objects/map-inl.h',
          '<(V8_ROOT)/src/objects/js-objects.cc',
          '<(V8_ROOT)/src/objects/name.h',
          '<(V8_ROOT)/src/objects/name-inl.h',
          '<(V8_ROOT)/src/objects/oddball-inl.h',
          '<(V8_ROOT)/src/objects/oddball.h',
          '<(V8_ROOT)/src/objects/primitive-heap-object.h',
          '<(V8_ROOT)/src/objects/primitive-heap-object-inl.h',
          '<(V8_ROOT)/src/objects/scope-info.h',
          '<(V8_ROOT)/src/objects/script.h',
          '<(V8_ROOT)/src/objects/script-inl.h',
          '<(V8_ROOT)/src/objects/shared-function-info.h',
          '<(V8_ROOT)/src/objects/shared-function-info-inl.h',
          '<(V8_ROOT)/src/objects/string.cc',
          '<(V8_ROOT)/src/objects/string.h',
          '<(V8_ROOT)/src/objects/string-comparator.cc',
          '<(V8_ROOT)/src/objects/string-comparator.h',
          '<(V8_ROOT)/src/objects/string-inl.h',
          '<(V8_ROOT)/src/objects/struct.h',
          '<(V8_ROOT)/src/objects/struct-inl.h',
          '<(torque_output_root)/torque-generated/instance-types-tq.h',
        ],
      },
      'actions': [
        {
          'action_name': 'gen-postmortem-metadata',
          'inputs': [
            '<(V8_ROOT)/tools/gen-postmortem-metadata.py',
            '<@(heapobject_files)',
          ],
          'outputs': [
            '<(SHARED_INTERMEDIATE_DIR)/debug-support.cc',
          ],
          'action': [
            'python',
            '<(V8_ROOT)/tools/gen-postmortem-metadata.py',
            '<@(_outputs)',
            '<@(heapobject_files)'
          ],
        },
      ],
      'direct_dependent_settings': {
        'sources': ['<(SHARED_INTERMEDIATE_DIR)/debug-support.cc', ],
      },
    },  # postmortem-metadata

    {
      'target_name': 'v8_zlib',
      'type': 'static_library',
      'conditions': [
        ['want_separate_host_toolset', {
          'toolsets': ['host', 'target'],
        }],
        ['OS=="win"', {
          'conditions': [
            ['"<(target_arch)"=="arm64" and _toolset=="target"', {
              'defines': ['CPU_NO_SIMD']
            }, {
              'defines': ['X86_WINDOWS']
            }]
          ]
        }],
      ],
      'direct_dependent_settings': {
        'include_dirs': [
          '<(V8_ROOT)/third_party/zlib',
          '<(V8_ROOT)/third_party/zlib/google',
        ],
      },
      'defines': [ 'ZLIB_IMPLEMENTATION' ],
      'include_dirs': [
        '<(V8_ROOT)/third_party/zlib',
        '<(V8_ROOT)/third_party/zlib/google',
      ],
      'sources': [
        '<(V8_ROOT)/third_party/zlib/adler32.c',
        '<(V8_ROOT)/third_party/zlib/chromeconf.h',
        '<(V8_ROOT)/third_party/zlib/compress.c',
        '<(V8_ROOT)/third_party/zlib/contrib/optimizations/insert_string.h',
        '<(V8_ROOT)/third_party/zlib/contrib/optimizations/insert_string.h',
        '<(V8_ROOT)/third_party/zlib/cpu_features.c',
        '<(V8_ROOT)/third_party/zlib/cpu_features.h',
        '<(V8_ROOT)/third_party/zlib/crc32.c',
        '<(V8_ROOT)/third_party/zlib/crc32.h',
        '<(V8_ROOT)/third_party/zlib/deflate.c',
        '<(V8_ROOT)/third_party/zlib/deflate.h',
        '<(V8_ROOT)/third_party/zlib/gzclose.c',
        '<(V8_ROOT)/third_party/zlib/gzguts.h',
        '<(V8_ROOT)/third_party/zlib/gzlib.c',
        '<(V8_ROOT)/third_party/zlib/gzread.c',
        '<(V8_ROOT)/third_party/zlib/gzwrite.c',
        '<(V8_ROOT)/third_party/zlib/infback.c',
        '<(V8_ROOT)/third_party/zlib/inffast.c',
        '<(V8_ROOT)/third_party/zlib/inffast.h',
        '<(V8_ROOT)/third_party/zlib/inffixed.h',
        '<(V8_ROOT)/third_party/zlib/inflate.c',
        '<(V8_ROOT)/third_party/zlib/inflate.h',
        '<(V8_ROOT)/third_party/zlib/inftrees.c',
        '<(V8_ROOT)/third_party/zlib/inftrees.h',
        '<(V8_ROOT)/third_party/zlib/trees.c',
        '<(V8_ROOT)/third_party/zlib/trees.h',
        '<(V8_ROOT)/third_party/zlib/uncompr.c',
        '<(V8_ROOT)/third_party/zlib/zconf.h',
        '<(V8_ROOT)/third_party/zlib/zlib.h',
        '<(V8_ROOT)/third_party/zlib/zutil.c',
        '<(V8_ROOT)/third_party/zlib/zutil.h',
        '<(V8_ROOT)/third_party/zlib/google/compression_utils_portable.cc',
        '<(V8_ROOT)/third_party/zlib/google/compression_utils_portable.h',
      ],
    },  # v8_zlib
  ],
}

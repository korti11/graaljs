local graalJs = import 'graal-js/ci.jsonnet';
local graalNodeJs = import 'graal-nodejs/ci.jsonnet';
local common = import 'common.jsonnet';

{
  // Used to run fewer jobs
  local debug = false,

  local overlay = 'f1cd0aa76ad3c3739174d30212436a29f13409f6',

  local no_overlay = 'cb733e564850cd37b685fcef6f3c16b59802b22c',

  overlay: if debug then no_overlay else overlay,

  local deployBinary = {
    setup+: [
      ['mx', '-p', 'graal-nodejs', 'sversions'],
      ['mx', '-p', 'graal-nodejs', 'build', '--force-javac'],
    ],
    run+: [
      ['mx', '-p', 'graal-js', 'deploy-binary-if-master', '--skip-existing', 'graaljs-binary-snapshots'],
      ['mx', '-p', 'graal-nodejs', 'deploy-binary-if-master', '--skip-existing', 'graalnodejs-binary-snapshots'],
    ],
    timelimit: '15:00',
  },

  builds: graalJs.builds + graalNodeJs.builds + [
    common.jdk8 + deployBinary + common.deploy + common.postMerge + common.ol65 + {name: 'js-deploybinary-ol65-amd64'},
    common.jdk8 + deployBinary + common.deploy + common.postMerge + common.darwin + {name: 'js-deploybinary-darwin-amd64'},
  ],
}

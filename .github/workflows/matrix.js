// The script generates a random subset of valid jdk, os, timezone, and other axes.
// You can preview the results by running "node matrix.js"
// See https://github.com/vlsi/github-actions-random-matrix
let {MatrixBuilder} = require('./matrix_builder');
const matrix = new MatrixBuilder();
matrix.addAxis({
  name: 'jdk',
  title: x => x.version + ', ' + x.group,
  values: [
    // Zulu
    {group: 'Zulu', version: '11', distribution: 'zulu', jit: 'hotspot'},
    {group: 'Zulu', version: '17', distribution: 'zulu', jit: 'hotspot'},

    // Adopt
    {group: 'Adopt Hotspot', version: '11', distribution: 'adopt-hotspot', jit: 'hotspot'},
    {group: 'Adopt Hotspot', version: '17', distribution: 'adopt-hotspot', jit: 'hotspot'},

    // Adopt OpenJ9
    // TODO: Replace these hard coded versions with something that dynamically picks the most recent
    {group: 'Adopt OpenJ9', version: '11', distribution: 'adopt-openj9', jit: 'openj9'},
    {group: 'Adopt OpenJ9', version: '17', distribution: 'adopt-openj9', jit: 'openj9'},

    // Amazon Corretto
    {
      group: 'Corretto',
      version: '17',
      jit: 'hotspot',
      distribution: 'jdkfile',
      url: 'https://corretto.aws/downloads/latest/amazon-corretto-17-x64-linux-jdk.tar.gz'
    },
    {
      group: 'Corretto',
      version: '11',
      jit: 'hotspot',
      distribution: 'jdkfile',
      url: 'https://corretto.aws/downloads/latest/amazon-corretto-11-x64-linux-jdk.tar.gz'
    },
    // Microsoft
    {
      group: 'Microsoft',
      version: '11',
      jit: 'hotspot',
      distribution: 'jdkfile',
      url: 'https://aka.ms/download-jdk/microsoft-jdk-11.0.11.9.1-linux-x64.tar.gz'
    },
    {
      group: 'Microsoft',
      version: '17',
      jit: 'hotspot',
      distribution: 'jdkfile',
      url: 'https://aka.ms/download-jdk/microsoft-jdk-17.0.1.12.1-linux-x64.tar.gz'
    },
    // Liberica
    {
      group: 'Liberica',
      version: '17',
      jit: 'hotspot',
      distribution: 'jdkfile',
      url: 'https://download.bell-sw.com/java/17.0.1+12/bellsoft-jdk17.0.1+12-linux-amd64.tar.gz'
    },
    {
      group: 'Liberica',
      version: '11',
      jit: 'hotspot',
      distribution: 'jdkfile',
      url: 'https://download.bell-sw.com/java/11.0.13+8/bellsoft-jdk11.0.13+8-linux-amd64.tar.gz'
    },
  ]
});
matrix.addAxis({
  name: 'tz',
  values: [
    'America/New_York',
    'Pacific/Chatham',
    'UTC'
  ]
});
matrix.addAxis({
  name: 'os',
  title: x => x.replace('-latest', ''),
  values: [
    'ubuntu-latest',
    'windows-latest',
    'macos-latest'
  ]
});
matrix.addAxis({
  name: 'hash',
  values: [
    {value: 'regular', title: '', weight: 42},
    {value: 'same', title: 'same hashcode', weight: 1}
  ]
});
matrix.addAxis({
  name: 'locale',
  title: x => x.language + '_' + x.country,
  values: [
    {language: 'de', country: 'DE'},
    {language: 'fr', country: 'FR'},
    {language: 'ru', country: 'RU'},
    {language: 'tr', country: 'TR'},
  ]
});

matrix.setNamePattern(['jdk', 'hash', 'os', 'tz', 'locale']);

// TODO: figure out how "same hashcode" could be configured in OpenJ9
matrix.exclude({hash: {value: 'same'}, jdk: {jit: 'openj9'}});
matrix.exclude({jdk: {distribution: 'jdkfile'}, os: ['windows-latest', 'macos-latest']});
// Ensure at least one job with "same" hashcode exists
matrix.generateRow({hash: {value: 'same'}});
// Ensure at least one windows and at least one linux job is present (macos is almost the same as linux)
matrix.generateRow({os: 'windows-latest'});
matrix.generateRow({os: 'ubuntu-latest'});
// Ensure there will be at least one job with Java 17
matrix.generateRow({jdk: {version: 17}});
// Ensure there will be at least one job with Java 11
matrix.generateRow({jdk: {version: 11}});
const include = matrix.generateRows(process.env.MATRIX_JOBS || 5);
if (include.length === 0) {
  throw new Error('Matrix list is empty');
}
include.sort((a, b) => a.name.localeCompare(b.name, undefined, {numeric: true}));
include.forEach(v => {
  let jvmArgs = [];
  if (v.hash.value === 'same') {
    jvmArgs.push('-XX:+UnlockExperimentalVMOptions', '-XX:hashCode=2');
  }
  // Gradle does not work in tr_TR locale, so pass locale to test only: https://github.com/gradle/gradle/issues/17361
  jvmArgs.push(`-Duser.country=${v.locale.country}`);
  jvmArgs.push(`-Duser.language=${v.locale.language}`);
  if (v.jdk.jit === 'hotspot' && Math.random() > 0.5) {
    // The following options randomize instruction selection in JIT compiler
    // so it might reveal missing synchronization in TestNG code
    v.name += ', stress JIT';
    jvmArgs.push('-XX:+UnlockDiagnosticVMOptions');
    // Randomize instruction scheduling in GCM
    // share/opto/c2_globals.hpp
    jvmArgs.push('-XX:+StressGCM');
    // Randomize instruction scheduling in LCM
    // share/opto/c2_globals.hpp
    jvmArgs.push('-XX:+StressLCM');
    if (v.jdk.version >= 16) {
      // Randomize worklist traversal in IGVN
      // share/opto/c2_globals.hpp
      jvmArgs.push('-XX:+StressIGVN');
    }
    if (v.jdk.version >= 17) {
      // Randomize worklist traversal in CCP
      // share/opto/c2_globals.hpp
      jvmArgs.push('-XX:+StressCCP');
    }
  }
  v.testExtraJvmArgs = jvmArgs.join(' ');
  delete v.hash;
});

console.log(include);
console.log('::set-output name=matrix::' + JSON.stringify({include}));

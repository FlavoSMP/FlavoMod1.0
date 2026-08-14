#!/usr/bin/env bash

APP_NAME="Gradle"
APP_BASE_NAME=${0##*/}
APP_HOME=$( cd "${APP_HOME:-$(dirname "${BASH_SOURCE[0]}")}" && pwd -P ) || exit

set -o errexit
set -o nounset
set +o allexport

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
JAVA_OPTS="${JAVA_OPTS:-}"
GRADLE_OPTS="${GRADLE_OPTS:-}"

GRADLE_USER_HOME="${GRADLE_USER_HOME:-$(dirname "$APP_HOME")/.gradle}"
export GRADLE_USER_HOME

for arg in "$@"; do
  if [[ "$arg" == \-\-debug* ]] || [[ "$arg" == \-\-info* ]]; then
    set -x
  fi
done

DEFAULT_JVM_OPTS=$(printf '%s\n' "${DEFAULT_JVM_OPTS[@]}" | xargs)

exec "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"

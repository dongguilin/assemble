#!/bin/bash

################################
# constants
################################
ASSEMBLE_CLASS="com.htjc.assemble.Assemble"

CLEAN_FLAG=1
################################
# functions
################################

info() {
  if [ ${CLEAN_FLAG} -ne 0 ]; then
    local msg=$1
    echo "Info: $msg" >&2
  fi
}

warn() {
  if [ ${CLEAN_FLAG} -ne 0 ]; then
    local msg=$1
    echo "Warning: $msg" >&2
  fi
}

error() {
  local msg=$1
  local exit_code=$2

  echo "Error: $msg" >&2

  if [ -n "$exit_code" ] ; then
    exit $exit_code
  fi
}

display_help() {
  cat <<EOF
usage: assemble [-D] [-f <arg>] [-h]
 -D                     use value for given property
 -f,--conf-file <arg>   specify a config file
 -h,--help              display help text
EOF
}

set_LD_LIBRARY_PATH(){
  if [ -n "${ASSEMBLE_JAVA_LIBRARY_PATH}" ]; then
    export LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:${ASSEMBLE_JAVA_LIBRARY_PATH}"
  fi
}

run_assemble() {
  local ASSEMBLE_APPLICATION_CLASS

  if [ "$#" -gt 0 ]; then
    ASSEMBLE_APPLICATION_CLASS=$1
    shift
  else
    error "Must specify assemble application class" 1
  fi

  if [ ${CLEAN_FLAG} -ne 0 ]; then
    set -x
  fi
  $EXEC $JAVA_HOME/bin/java $JAVA_OPTS $ASSEMBLE_JAVA_OPTS "${arr_java_props[@]}" -cp "$ASSEMBLE_CLASSPATH:$ASSEMBLE_HOME/conf" \
      -Djava.library.path=$ASSEMBLE_JAVA_LIBRARY_PATH "$ASSEMBLE_APPLICATION_CLASS" $*
}

################################
# main
################################

# set default params
ASSEMBLE_CLASSPATH=""
ASSEMBLE_JAVA_LIBRARY_PATH=""
JAVA_OPTS="-Xms50m -Xmx120m"
LD_LIBRARY_PATH=""

opt_classpath=""
arr_java_props=()
arr_java_props_ct=0


args=""
while [ -n "$*" ] ; do
  arg=$1
  shift

  case "$arg" in
    --classpath|-C)
      [ -n "$1" ] || error "Option --classpath requires an argument" 1
      opt_classpath=$1
      shift
      ;;
    -D*)
      arr_java_props[arr_java_props_ct]=$arg
      ((++arr_java_props_ct))
      ;;
    -X*)
      arr_java_props[arr_java_props_ct]=$arg
      ((++arr_java_props_ct))
      ;;
    *)
      args="$args $arg"
      ;;
  esac
done

# prepend command-line classpath to env script classpath
if [ -n "${opt_classpath}" ]; then
  if [ -n "${ASSEMBLE_CLASSPATH}" ]; then
    ASSEMBLE_CLASSPATH="${opt_classpath}:${ASSEMBLE_CLASSPATH}"
  else
    ASSEMBLE_CLASSPATH="${opt_classpath}"
  fi
fi

if [ -z "${ASSEMBLE_HOME}" ]; then
  ASSEMBLE_HOME=$(cd $(dirname $0)/..; pwd)
fi

# prepend $ASSEMBLE_HOME/lib jars to the specified classpath (if any)
if [ -n "${ASSEMBLE_CLASSPATH}" ] ; then
  ASSEMBLE_CLASSPATH="${ASSEMBLE_HOME}/lib/*:$ASSEMBLE_CLASSPATH"
else
  ASSEMBLE_CLASSPATH="${ASSEMBLE_HOME}/lib/*"
fi


# find java
if [ -z "${JAVA_HOME}" ] ; then
  warn "JAVA_HOME is not set!"
  # Try to use Bigtop to autodetect JAVA_HOME if it's available
  if [ -e /usr/libexec/bigtop-detect-javahome ] ; then
    . /usr/libexec/bigtop-detect-javahome
  elif [ -e /usr/lib/bigtop-utils/bigtop-detect-javahome ] ; then
    . /usr/lib/bigtop-utils/bigtop-detect-javahome
  fi

  # Using java from path if bigtop is not installed or couldn't find it
  if [ -z "${JAVA_HOME}" ] ; then
    JAVA_DEFAULT=$(type -p java)
    [ -n "$JAVA_DEFAULT" ] || error "Unable to find java executable. Is it in your PATH?" 1
    JAVA_HOME=$(cd $(dirname $JAVA_DEFAULT)/..; pwd)
  fi
fi

# finally, invoke the appropriate command
run_assemble $ASSEMBLE_CLASS $args

exit 0



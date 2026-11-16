# Shared environment for the demo helper scripts. SOURCED by the others, not run.
#
# Pin Java 21: Quarkus 3.40 / Roq 2.2 do not support Java 25 yet. Override with
# JAVA_HOME_21=/path/to/jdk21 if your JDK lives elsewhere.
: "${JAVA_HOME_21:=$HOME/.sdkman/candidates/java/21.0.9-tem}"
if [ ! -x "$JAVA_HOME_21/bin/java" ]; then
  # fall back to the newest JDK 21 installed via SDKMAN
  JAVA_HOME_21="$(ls -d "$HOME"/.sdkman/candidates/java/21* 2>/dev/null | sort -V | tail -1 || true)"
fi
if [ ! -x "${JAVA_HOME_21:-}/bin/java" ]; then
  echo "ERROR: no JDK 21 found. Install one (sdk install java 21.0.9-tem) or set JAVA_HOME_21." >&2
  return 1 2>/dev/null || exit 1
fi
export JAVA_HOME="$JAVA_HOME_21"
export PATH="$JAVA_HOME/bin:$PATH"

# Ports: Roq uses 8099 (the raffle app owns 8080); static preview uses 8097.
ROQ_PORT="${ROQ_PORT:-8099}"
PREVIEW_PORT="${PREVIEW_PORT:-8097}"

# Where the private raffle admin app lives (its Postgres is our data source).
RAFFLE_DIR="${RAFFLE_DIR:-$HOME/work/jug-vienna-raffle}"

# Kill whatever currently holds a TCP port (quietly).
free_port() { lsof -ti "tcp:$1" 2>/dev/null | xargs kill -9 2>/dev/null || true; }

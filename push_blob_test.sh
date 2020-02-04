#!/bin/sh

set -o errexit
set -o pipefail

# BLOb to push. You can just use any large file. The name, path, and contents
# don't matter at all.
echo "Enter path to a large file to upload."
echo "Note you can easily generate a 40mb file with the following command:"
echo "    $ dd bs=1M count=40 < /dev/urandom > 40mb.file"
echo -n "Path? "
read BLOB_PATH

if [ ! -f "${BLOB_PATH}" ]; then
  echo "Halting: ${BLOB_PATH} does not exist."
  exit 1
fi

# Your Docker Hub image repository
echo -n "Your Docker Hub account? "
read USERNAME
echo -n "Your Docker Hub password? "
read -s PASSWORD
echo
echo -n "Your Docker Hub target repo (e.g., myaccount/myrepo)? "
read REPO
echo -n "Verbose curl logging (y/[N])? "
read OPTION
if [ "${OPTION}" = y -o "${OPTION}" = Y ]; then
  VERBOSE=-v
fi


echoGreen() {
  echo "$(tput setaf 2; tput bold)$1$(tput sgr0)"
}

################################################################################
# STEP 1: Auth with Docker Hub for push
################################################################################

AUTH=$( echo -n "${USERNAME}:${PASSWORD}" | base64 - )

JSON_OUTPUT=$( curl ${VERBOSE} --compressed \
    -H 'Accept: */*' -H 'Accept-Encoding: gzip' \
    -H "Authorization: Basic ${AUTH}" \
    -H 'User-Agent: jib 1.5.1 jib-maven-plugin Google-HTTP-Java-Client/1.30.0 (gzip)' \
    -- "https://auth.docker.io/token?service=registry.docker.io&scope=repository:${REPO}:pull,push" )
# The above will return an auth token in JSON. For example:
#    {
#        "token": "THIS TOKEN IS WHAT WE WANT TO EXTRACT",
#        "access_token": "...",
#        "expires_in": 300,
#        "issued_at": "2019-09-11T15:26:24.113210766Z"
#    }
# (Seems like Docker Hub tokens expire in 5 minutes.)

# Extract the "token" field.
TOKEN=$( echo "${JSON_OUTPUT}" | jq -r '.token' )

echo
echoGreen ">>> Got token (don't reveal this in public): ${TOKEN}"
echo


################################################################################
# STEP 2: Ask Docker Hub where I can push a layer (HTTP POST)
################################################################################

HEADER_OUTPUT=$( curl ${VERBOSE} -I --compressed -X POST \
    -H 'Accept: ' -H 'Accept-Encoding: gzip' \
    -H "Authorization: Bearer ${TOKEN}" \
    -H 'User-Agent: jib 1.5.1 jib-maven-plugin Google-HTTP-Java-Client/1.30.0 (gzip)' \
    -- "https://registry-1.docker.io/v2/${REPO}/blobs/uploads/" )
# Server will return where to push a BLOb. For example,
# < Location: https://registry-1.docker.io/v2/<your repo>/blobs/uploads/...

PUSH_LOCATION=$( echo "${HEADER_OUTPUT}" \
    | grep '^Location: ' | tr -d '\r' | sed 's/Location: //' )

echo
echoGreen ">>> Got push location: "${PUSH_LOCATION}
echo

################################################################################
# STEP 3: Push a BLOb (HTTP PATCH)
###############################################################################

echo "Now pushing a BLOb: ${BLOB_PATH}"
echo

# Now we are actually going to push a large BLOb (layer). Let's time it.
time curl ${VERBOSE} --compressed -X PATCH \
    -H 'Accept: ' -H 'Accept-Encoding: gzip' \
    -H "Authorization: Bearer ${TOKEN}" \
    -H 'User-Agent: jib 1.5.1 jib-maven-plugin Google-HTTP-Java-Client/1.30.0 (gzip)' \
    -H 'Content-Type: application/octet-stream' \
    --data-binary "@${BLOB_PATH}" \
    -- "${PUSH_LOCATION}"

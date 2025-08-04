#!/bin/bash

hyperfine \
 --parameter-list branch cbarlin/issue88,main \
 --setup "git switch {branch}" \
 -m 15 \
 "mvn clean install"
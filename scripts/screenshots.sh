#!/usr/bin/env bash
# Captures README screenshots of the sample app on a running emulator.
# Taps and typing can't be timed reliably through adb, so the sample opens each screen from the `scene` extra with a
# code already entered and a steady cursor, without the keyboard, so every capture is the same.
# Every capture is checked for the expected text and for a blank image.
#
#   bash scripts/screenshots.sh tablet   # pixel_tablet in landscape: the transfer confirmation card, light and dark
#   bash scripts/screenshots.sh phone    # pixel_7: code, wrong code and PIN screens, light and dark
set -euo pipefail
source "$(dirname "$0")/screenshot-lib.sh"

device="${1:-phone}"

# The text each scene must show; the capture fails without it.
# "attempts left" only appears after a wrong code.
expected_text() {
  case "$1" in
    otp) echo "Enter the 6-digit code" ;;
    error) echo "attempts left" ;;
    pin) echo "Enter your PIN" ;;
    tablet) echo "Confirm your transfer" ;;
  esac
}

suffix() {
  if [ "$1" = dark ]; then echo "-dark"; else echo ""; fi
}

install_sample
if [ "$device" = tablet ]; then
  ensure_landscape
  for mode in light dark; do
    set_night_mode "$mode"
    fresh_launch --es scene tablet
    capture "tablet-verify$(suffix "$mode")" "$(expected_text tablet)"
  done
else
  for mode in light dark; do
    set_night_mode "$mode"
    for scene in otp error pin; do
      fresh_launch --es scene "$scene"
      capture "phone-$scene$(suffix "$mode")" "$(expected_text "$scene")"
    done
  done
fi

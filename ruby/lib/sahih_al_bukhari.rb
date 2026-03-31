# frozen_string_literal: true

# FIX: the original file contained only:
#   require_relative '../sahih_al_bukhari'
# which tried to load ruby/sahih_al_bukhari.rb via a relative path that breaks
# once the gem is installed (the installed gem's require_paths is ['lib'], so
# `require 'sahih_al_bukhari'` resolves to THIS file, not the root-level one).
#
# The correct approach: this IS the library entry point.  Load version first,
# then the main implementation file that lives alongside it.

require_relative 'sahih_al_bukhari/version'
require_relative 'sahih_al_bukhari/bukhari'
